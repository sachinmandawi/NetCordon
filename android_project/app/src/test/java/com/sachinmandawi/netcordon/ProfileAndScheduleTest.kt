package com.sachinmandawi.netcordon

import org.json.JSONArray
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.Calendar

class ProfileAndScheduleTest {

    @Test
    fun testJsonExportStructure() {
        val json = JSONObject().apply {
            put("version", 1)
            put("timestamp", System.currentTimeMillis())
            put("profileName", "Default Shield")
            put("wifiBlockedPackages", JSONArray(listOf("com.instagram.android", "com.zhiliaoapp.musically")))
            put("dataBlockedPackages", JSONArray(listOf("com.google.android.youtube")))
            put("blackoutPackages", JSONArray(listOf("com.facebook.katana")))
            put("dailyQuotas", JSONObject().apply {
                put("com.google.android.youtube", 524288000L) // 500MB
            })
        }

        assertEquals(1, json.getInt("version"))
        assertEquals("Default Shield", json.getString("profileName"))
        assertEquals(2, json.getJSONArray("wifiBlockedPackages").length())
        assertEquals(1, json.getJSONArray("dataBlockedPackages").length())
        assertEquals(1, json.getJSONArray("blackoutPackages").length())
        assertEquals(524288000L, json.getJSONObject("dailyQuotas").getLong("com.google.android.youtube"))
    }

    @Test
    fun testScheduleDaysCalculation() {
        // Monday = Calendar.MONDAY (2), Wednesday = 4, Friday = 6
        val days = setOf(Calendar.MONDAY, Calendar.WEDNESDAY, Calendar.FRIDAY)
        assertTrue(days.contains(Calendar.MONDAY))
        assertTrue(days.contains(Calendar.WEDNESDAY))
        assertTrue(days.contains(Calendar.FRIDAY))
        assertEquals(3, days.size)
    }

    @Test
    fun testScheduleModelIntegrity() {
        val schedule = FirewallSchedule(
            id = "bedtime_rule",
            title = "Bedtime Shield",
            mode = AppIsolationMode.TOTAL_BLACKOUT,
            startHour = 23,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(1, 2, 3, 4, 5, 6, 7),
            targetPackages = setOf("com.whatsapp", "com.instagram.android"),
            isEnabled = true
        )

        assertEquals("bedtime_rule", schedule.id)
        assertEquals(AppIsolationMode.TOTAL_BLACKOUT, schedule.mode)
        assertEquals(23, schedule.startHour)
        assertEquals(0, schedule.startMinute)
        assertEquals(7, schedule.endHour)
        assertEquals(0, schedule.endMinute)
        assertEquals(7, schedule.daysOfWeek.size)
        assertEquals(2, schedule.targetPackages.size)
        assertTrue(schedule.isEnabled)
    }

    @Test
    fun testAppIsolationModeEnum() {
        val modes = AppIsolationMode.values()
        assertEquals(3, modes.size)
        assertTrue(modes.contains(AppIsolationMode.ALLOWED))
        assertTrue(modes.contains(AppIsolationMode.SMART_SHIELD))
        assertTrue(modes.contains(AppIsolationMode.TOTAL_BLACKOUT))
    }

    @Test
    fun testUserProvidedJsonProfileParsing() {
        val userJson = """
        {
          "version": 1,
          "exportDate": 1788469151184,
          "appName": "NetCordon",
          "blackoutPackages": [],
          "smartShieldPackages": [
            "com.whatsapp",
            "com.whatsapp.w4b"
          ],
          "schedules": [
            {
              "id": "sched_1788440926101",
              "title": "Custom Schedule",
              "startH": 22,
              "startM": 0,
              "endH": 7,
              "endM": 0,
              "days": [1, 2, 3, 4, 5, 6, 7],
              "pkgs": [
                "com.whatsapp",
                "com.whatsapp.w4b"
              ],
              "mode": "SMART_SHIELD",
              "enabled": false
            }
          ],
          "dailyQuotas": {},
          "settings": {
            "screenOffShield": false,
            "blockNotifications": true,
            "leakAlertsEnabled": false,
            "minNotif": false,
            "startOnBoot": true,
            "showPkg": true,
            "floatingSpeedometer": false
          }
        }
        """.trimIndent()

        val summary = PrefsManager.parseProfileSummary(userJson)
        assertNotNull(summary)
        assertEquals(0, summary?.blackoutCount)
        assertEquals(2, summary?.smartShieldCount)
        assertEquals(1, summary?.schedulesCount)
        assertEquals(0, summary?.quotasCount)
    }

    private fun createCalendar(dayOfWeek: Int, hour: Int, minute: Int): Calendar {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, dayOfWeek)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }

    @Test
    fun testScheduleSameDayActiveScenario() {
        // Office Work schedule: Mon to Fri, 09:00 to 17:00
        val officeSched = FirewallSchedule(
            id = "work",
            title = "Work Hours",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
            targetPackages = setOf("com.instagram.android"),
            mode = AppIsolationMode.SMART_SHIELD,
            isEnabled = true
        )

        // 1. Monday 08:30 AM (Before start) -> Inactive
        val calBefore = createCalendar(Calendar.MONDAY, 8, 30)
        org.junit.Assert.assertFalse(officeSched.isCurrentlyActive(calBefore))

        // 2. Monday 09:00 AM (Exact start) -> Active
        val calStart = createCalendar(Calendar.MONDAY, 9, 0)
        assertTrue(officeSched.isCurrentlyActive(calStart))

        // 3. Wednesday 13:30 PM (Midday active window) -> Active
        val calMid = createCalendar(Calendar.WEDNESDAY, 13, 30)
        assertTrue(officeSched.isCurrentlyActive(calMid))

        // 4. Friday 16:59 PM (1 minute before end) -> Active
        val calEndNear = createCalendar(Calendar.FRIDAY, 16, 59)
        assertTrue(officeSched.isCurrentlyActive(calEndNear))

        // 5. Friday 17:00 PM (Exact end) -> Inactive
        val calEnd = createCalendar(Calendar.FRIDAY, 17, 0)
        org.junit.Assert.assertFalse(officeSched.isCurrentlyActive(calEnd))

        // 6. Friday 18:00 PM (After end) -> Inactive
        val calAfter = createCalendar(Calendar.FRIDAY, 18, 0)
        org.junit.Assert.assertFalse(officeSched.isCurrentlyActive(calAfter))

        // 7. Sunday 11:00 AM (Non-scheduled day) -> Inactive
        val calSunday = createCalendar(Calendar.SUNDAY, 11, 0)
        org.junit.Assert.assertFalse(officeSched.isCurrentlyActive(calSunday))
    }

    @Test
    fun testScheduleOvernightActiveScenario() {
        // Night Sleep schedule: Mon to Fri, 22:00 to 07:00
        val nightSched = FirewallSchedule(
            id = "night",
            title = "Night Blackout",
            startHour = 22,
            startMinute = 0,
            endHour = 7,
            endMinute = 0,
            daysOfWeek = setOf(Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY),
            targetPackages = setOf("com.whatsapp"),
            mode = AppIsolationMode.TOTAL_BLACKOUT,
            isEnabled = true
        )

        // 1. Monday 21:59 (Before start) -> Inactive
        val calBefore = createCalendar(Calendar.MONDAY, 21, 59)
        org.junit.Assert.assertFalse(nightSched.isCurrentlyActive(calBefore))

        // 2. Monday 22:00 (Exact start) -> Active
        val calStart = createCalendar(Calendar.MONDAY, 22, 0)
        assertTrue(nightSched.isCurrentlyActive(calStart))

        // 3. Monday 23:30 (Late night) -> Active
        val calLate = createCalendar(Calendar.MONDAY, 23, 30)
        assertTrue(nightSched.isCurrentlyActive(calLate))

        // 4. Tuesday 00:00 (Midnight crossing) -> Active (because Monday was in daysOfWeek)
        val calMidnight = createCalendar(Calendar.TUESDAY, 0, 0)
        assertTrue(nightSched.isCurrentlyActive(calMidnight))

        // 5. Tuesday 04:30 AM (Early morning) -> Active
        val calMorning = createCalendar(Calendar.TUESDAY, 4, 30)
        assertTrue(nightSched.isCurrentlyActive(calMorning))

        // 6. Tuesday 06:59 AM (1 minute before end) -> Active
        val calEndNear = createCalendar(Calendar.TUESDAY, 6, 59)
        assertTrue(nightSched.isCurrentlyActive(calEndNear))

        // 7. Tuesday 07:00 AM (Exact end) -> Inactive
        val calEnd = createCalendar(Calendar.TUESDAY, 7, 0)
        org.junit.Assert.assertFalse(nightSched.isCurrentlyActive(calEnd))

        // 8. Tuesday 12:00 PM (Middle of day) -> Inactive
        val calNoon = createCalendar(Calendar.TUESDAY, 12, 0)
        org.junit.Assert.assertFalse(nightSched.isCurrentlyActive(calNoon))

        // 9. Saturday 03:00 AM -> Active! (because Friday night extends into Saturday morning)
        val calSatMorning = createCalendar(Calendar.SATURDAY, 3, 0)
        assertTrue(nightSched.isCurrentlyActive(calSatMorning))

        // 10. Saturday 23:00 PM -> Inactive! (Saturday is not a scheduled night)
        val calSatNight = createCalendar(Calendar.SATURDAY, 23, 0)
        org.junit.Assert.assertFalse(nightSched.isCurrentlyActive(calSatNight))
    }

    @Test
    fun testScheduleDisabledScenario() {
        val disabledSched = FirewallSchedule(
            id = "disabled",
            title = "Paused Schedule",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = setOf(Calendar.MONDAY),
            targetPackages = setOf("com.whatsapp"),
            mode = AppIsolationMode.TOTAL_BLACKOUT,
            isEnabled = false
        )

        // Monday 12:00 PM inside window -> Must be FALSE because isEnabled is false
        val cal = createCalendar(Calendar.MONDAY, 12, 0)
        org.junit.Assert.assertFalse(disabledSched.isCurrentlyActive(cal))
    }

    @Test
    fun testScheduleEmptyDaysScenario() {
        val emptyDaysSched = FirewallSchedule(
            id = "empty",
            title = "No Days",
            startHour = 9,
            startMinute = 0,
            endHour = 17,
            endMinute = 0,
            daysOfWeek = emptySet(),
            targetPackages = setOf("com.whatsapp"),
            mode = AppIsolationMode.SMART_SHIELD,
            isEnabled = true
        )

        val cal = createCalendar(Calendar.MONDAY, 12, 0)
        org.junit.Assert.assertFalse(emptyDaysSched.isCurrentlyActive(cal))
    }

    @Test
    fun testSchedule12HourConversionScenarios() {
        fun convert12to24(hour12: Int, amPm: String): Int = when {
            amPm == "AM" && hour12 == 12 -> 0
            amPm == "AM" -> hour12
            amPm == "PM" && hour12 == 12 -> 12
            else -> hour12 + 12
        }

        assertEquals(0, convert12to24(12, "AM"))  // 12 AM = 00:00
        assertEquals(1, convert12to24(1, "AM"))   // 1 AM = 01:00
        assertEquals(11, convert12to24(11, "AM")) // 11 AM = 11:00
        assertEquals(12, convert12to24(12, "PM")) // 12 PM = 12:00
        assertEquals(13, convert12to24(1, "PM"))  // 1 PM = 13:00
        assertEquals(22, convert12to24(10, "PM")) // 10 PM = 22:00
        assertEquals(23, convert12to24(11, "PM")) // 11 PM = 23:00
    }

    @Test
    fun testScheduleNextTriggerCalculation() {
        // Base: Monday 10:00 AM
        val baseCal = createCalendar(Calendar.MONDAY, 10, 0)

        // 1. Target is later today on Monday at 14:00 (2 PM)
        val trigger1 = ScheduleReceiver.getNextTriggerTime(14, 0, setOf(Calendar.MONDAY), baseCal)
        val triggerCal1 = Calendar.getInstance().apply { timeInMillis = trigger1 }
        assertEquals(Calendar.MONDAY, triggerCal1.get(Calendar.DAY_OF_WEEK))
        assertEquals(14, triggerCal1.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, triggerCal1.get(Calendar.MINUTE))

        // 2. Target has already passed today (e.g. 08:00 AM on Monday) -> Should advance to next Monday
        val trigger2 = ScheduleReceiver.getNextTriggerTime(8, 0, setOf(Calendar.MONDAY), baseCal)
        val triggerCal2 = Calendar.getInstance().apply { timeInMillis = trigger2 }
        assertEquals(Calendar.MONDAY, triggerCal2.get(Calendar.DAY_OF_WEEK))
        assertEquals(8, triggerCal2.get(Calendar.HOUR_OF_DAY))
        // Verify it advanced by 7 days (6 days and 22 hours difference)
        val hoursDiff = (trigger2 - baseCal.timeInMillis) / (1000 * 60 * 60)
        assertEquals(166L, hoursDiff) // 7 days - 2 hours = 166 hours

        // 3. Target on Wednesday at 15:00
        val trigger3 = ScheduleReceiver.getNextTriggerTime(15, 0, setOf(Calendar.WEDNESDAY), baseCal)
        val triggerCal3 = Calendar.getInstance().apply { timeInMillis = trigger3 }
        assertEquals(Calendar.WEDNESDAY, triggerCal3.get(Calendar.DAY_OF_WEEK))
        assertEquals(15, triggerCal3.get(Calendar.HOUR_OF_DAY))
    }

    @Test
    fun testScheduleAppConfigurationIndependence() {
        val baseBlackout = setOf("com.facebook.katana")
        val baseSmartShield = setOf("com.instagram.android")

        // Schedule configured with completely separate apps
        val customSchedule = FirewallSchedule(
            id = "custom_sched",
            title = "Study Session",
            startHour = 14,
            startMinute = 0,
            endHour = 16,
            endMinute = 0,
            daysOfWeek = setOf(Calendar.MONDAY),
            targetPackages = setOf("com.google.android.youtube", "tv.twitch.android.app"),
            mode = AppIsolationMode.SMART_SHIELD,
            isEnabled = true
        )

        // Verify customSchedule apps are independent from base sets
        org.junit.Assert.assertFalse(customSchedule.targetPackages.containsAll(baseBlackout))
        org.junit.Assert.assertFalse(customSchedule.targetPackages.containsAll(baseSmartShield))
        assertEquals(2, customSchedule.targetPackages.size)
        assertTrue(customSchedule.targetPackages.contains("com.google.android.youtube"))
        assertTrue(customSchedule.targetPackages.contains("tv.twitch.android.app"))
    }
}
