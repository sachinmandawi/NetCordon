package com.sachinmandawi.netcordon

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import java.util.Calendar

class ScheduleReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "ScheduleReceiver"
        const val ACTION_TRIGGER_START = "com.sachinmandawi.netcordon.ACTION_SCHEDULE_START"
        const val ACTION_TRIGGER_END   = "com.sachinmandawi.netcordon.ACTION_SCHEDULE_END"
        const val EXTRA_SCHEDULE_ID    = "extra_schedule_id"

        fun schedule(context: Context, schedule: FirewallSchedule) {
            cancel(context, schedule.id)
            if (!schedule.isEnabled) return

            // If schedule is active right now, apply rules immediately
            if (schedule.isCurrentlyActive()) {
                for (pkg in schedule.targetPackages) {
                    PrefsManager.applyScheduledIsolationMode(context, pkg, schedule.mode)
                }
                notifyService(context)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            // 1. Calculate next start time
            val startTime = getNextTriggerTime(schedule.startHour, schedule.startMinute, schedule.daysOfWeek)
            val startIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_TRIGGER_START
                putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            }
            val startPI = PendingIntent.getBroadcast(
                context,
                (schedule.id + "_start").hashCode(),
                startIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            // 2. Calculate next end time
            val isOvernight = (schedule.startHour * 60 + schedule.startMinute) > (schedule.endHour * 60 + schedule.endMinute)
            val endDays = if (isOvernight && schedule.daysOfWeek.isNotEmpty()) {
                schedule.daysOfWeek.map { if (it == Calendar.SATURDAY) Calendar.SUNDAY else it + 1 }.toSet()
            } else {
                schedule.daysOfWeek
            }
            val endTime = getNextTriggerTime(schedule.endHour, schedule.endMinute, endDays)
            val endIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_TRIGGER_END
                putExtra(EXTRA_SCHEDULE_ID, schedule.id)
            }
            val endPI = PendingIntent.getBroadcast(
                context,
                (schedule.id + "_end").hashCode(),
                endIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            try {
                val canExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    alarmManager.canScheduleExactAlarms()
                } else {
                    true
                }
                if (canExact) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    } else {
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.setExact(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    }
                    Log.d(TAG, "Scheduled exact ${schedule.title} Start: $startTime, End: $endTime")
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    }
                    Log.d(TAG, "Scheduled inexact ${schedule.title} Start: $startTime, End: $endTime")
                }
            } catch (e: SecurityException) {
                Log.w(TAG, "Missing exact alarm permission, falling back to setAndAllowWhileIdle", e)
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    } else {
                        alarmManager.set(AlarmManager.RTC_WAKEUP, startTime, startPI)
                        alarmManager.set(AlarmManager.RTC_WAKEUP, endTime, endPI)
                    }
                } catch (e2: Exception) {
                    Log.e(TAG, "Failed to schedule inexact fallback alarm", e2)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun cancel(context: Context, scheduleId: String) {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val startIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_TRIGGER_START
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            }
            val startPI = PendingIntent.getBroadcast(
                context,
                (scheduleId + "_start").hashCode(),
                startIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (startPI != null) alarmManager.cancel(startPI)

            val endIntent = Intent(context, ScheduleReceiver::class.java).apply {
                action = ACTION_TRIGGER_END
                putExtra(EXTRA_SCHEDULE_ID, scheduleId)
            }
            val endPI = PendingIntent.getBroadcast(
                context,
                (scheduleId + "_end").hashCode(),
                endIntent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (endPI != null) alarmManager.cancel(endPI)

            // Release any active firewall rules applied by this schedule while respecting other active schedules
            val schedule = PrefsManager.getSchedules(context).find { it.id == scheduleId }
            if (schedule != null) {
                PrefsManager.restoreBaseFirewallRules(context, schedule.targetPackages, excludingScheduleId = scheduleId)
                notifyService(context)
            }
        }

        fun rescheduleAll(context: Context) {
            val schedules = PrefsManager.getSchedules(context)
            for (s in schedules) {
                schedule(context, s)
            }
        }

        internal fun getNextTriggerTime(hour: Int, minute: Int, daysOfWeek: Set<Int>, baseCalendar: Calendar? = null): Long {
            val now = baseCalendar?.let { it.clone() as Calendar } ?: Calendar.getInstance()
            val target = (now.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            if (target.timeInMillis <= now.timeInMillis) {
                target.add(Calendar.DAY_OF_YEAR, 1)
            }

            if (daysOfWeek.isNotEmpty()) {
                var safety = 0
                while (!daysOfWeek.contains(target.get(Calendar.DAY_OF_WEEK)) && safety < 8) {
                    target.add(Calendar.DAY_OF_YEAR, 1)
                    safety++
                }
            }

            return target.timeInMillis
        }

        internal fun notifyService(context: Context) {
            if (!PrefsManager.isServiceEnabled(context)) return

            val shielded = PrefsManager.getShieldedPackages(context)
            val uids = PrefsManager.getPackageUidMap(context)

            val intent = Intent(context, AppShieldService::class.java).apply {
                action = AppShieldService.ACTION_UPDATE_RULES
                putStringArrayListExtra(AppShieldService.EXTRA_PACKAGES, ArrayList(shielded))
                putIntegerArrayListExtra(AppShieldService.EXTRA_UIDS, ArrayList(shielded.mapNotNull { uids[it] }))
            }

            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        if (context == null || intent == null) return
        val scheduleId = intent.getStringExtra(EXTRA_SCHEDULE_ID) ?: return
        val schedule = PrefsManager.getSchedules(context).find { it.id == scheduleId } ?: return
        if (!schedule.isEnabled) return

        Log.d(TAG, "Alarm fired: ${intent.action} for schedule: ${schedule.title}")

        var wakeLock: android.os.PowerManager.WakeLock? = null
        try {
            val powerManager = context.getSystemService(Context.POWER_SERVICE) as? android.os.PowerManager
            wakeLock = powerManager?.newWakeLock(android.os.PowerManager.PARTIAL_WAKE_LOCK, "NetCordon:ScheduleWakeLock")?.apply {
                setReferenceCounted(false)
                acquire(10_000L)
            }
        } catch (e: Exception) {
            Log.w(TAG, "WakeLock error", e)
        }

        try {
            when (intent.action) {
                ACTION_TRIGGER_START -> {
                    for (pkg in schedule.targetPackages) {
                        PrefsManager.applyScheduledIsolationMode(context, pkg, schedule.mode)
                    }
                    notifyService(context)
                    schedule(context, schedule)
                }
                ACTION_TRIGGER_END -> {
                    PrefsManager.restoreBaseFirewallRules(context, schedule.targetPackages, excludingScheduleId = schedule.id)
                    notifyService(context)
                    schedule(context, schedule)
                }
            }
        } finally {
            try {
                if (wakeLock?.isHeld == true) {
                    wakeLock.release()
                }
            } catch (_: Exception) {
            }
        }
    }
}
