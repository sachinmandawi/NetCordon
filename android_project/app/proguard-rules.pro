# Add project specific ProGuard rules here.
# Keep Shizuku reflection methods (Shizuku.newProcess)
-keep class rikka.shizuku.** { *; }
-keep class dev.rikka.shizuku.** { *; }

# Keep NetCordon data models & enums
-keep class com.sachinmandawi.netcordon.AppIsolationMode { *; }
-keep class com.sachinmandawi.netcordon.FirewallSchedule { *; }
-keep class com.sachinmandawi.netcordon.UpdateCheckResult { *; }
