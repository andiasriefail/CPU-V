package com.CPU.V.Vhynlain;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothStatusCodes;
import android.hardware.Sensor;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaDrm;
import android.net.DhcpInfo;
import android.content.pm.FeatureInfo;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.DisplayMetrics;
import android.util.Size;
import android.util.SizeF;
import android.view.Display;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;

public final class SystemInfo {
    private SystemInfo() {
    }

    public static List<String[]> rows(Context c, String type) {
        switch (type) {
            case "device":
                return device(c);
            case "system":
                return system(c);
            case "cpu":
                return cpu(c);
            case "battery":
                return battery(c);
            case "network":
                return network(c);
            case "display":
                return display(c);
            case "memory":
                return memory(c);
            case "connectivity":
                return connectivity(c);
            case "camera":
                return camera(c);
            case "bluetooth":
                return bluetooth(c);
            case "thermal":
                return thermal(c);
            case "audio":
                return audio(c);
            case "features":
                return features(c);
            default:
                return new ArrayList<>();
        }
    }

    public static String[] permissions(String type) {
        if (type.equals("network")) {
            return new String[]{Manifest.permission.ACCESS_FINE_LOCATION};
        }
        if (type.equals("bluetooth") && Build.VERSION.SDK_INT >= 31) {
            return new String[]{Manifest.permission.BLUETOOTH_CONNECT};
        }
        return new String[0];
    }

    private static boolean bluetoothAllowed(Context c) {
        return Build.VERSION.SDK_INT < 31 || granted(c, Manifest.permission.BLUETOOTH_CONNECT);
    }

    private static String onOff(Context c, boolean v) {
        return v ? s(c, R.string.on) : s(c, R.string.off);
    }

    private static String supportedText(Context c, boolean v) {
        return v ? s(c, R.string.supported) : s(c, R.string.not_supported);
    }

    public static boolean granted(Context c, String permission) {
        return ContextCompat.checkSelfPermission(c, permission) == PackageManager.PERMISSION_GRANTED;
    }

    private static String s(Context c, int id) {
        return c.getString(id);
    }

    private static void add(List<String[]> l, String key, String value) {
        l.add(new String[]{key, value == null || value.isEmpty() ? "-" : value});
    }

    public static String chipsetName() {
        if (Build.VERSION.SDK_INT >= 31) {
            String m = Build.SOC_MODEL;
            if (m != null && !m.isEmpty() && !m.equalsIgnoreCase("unknown")) {
                String v = Build.SOC_MANUFACTURER;
                return (v == null || v.equalsIgnoreCase("unknown") ? "" : v + " ") + m;
            }
        }
        return Build.HARDWARE;
    }

    private static List<String[]> device(Context c) {
        List<String[]> l = new ArrayList<>();
        add(l, "Device name", deviceName(c));
        add(l, s(c, R.string.model), Build.MODEL);
        add(l, s(c, R.string.manufacturer), Build.MANUFACTURER);
        add(l, s(c, R.string.brand), Build.BRAND);
        add(l, s(c, R.string.device), Build.DEVICE);
        add(l, s(c, R.string.board), Build.BOARD);
        add(l, s(c, R.string.hardware), Build.HARDWARE);
        add(l, "Android device ID", androidId(c));
        add(l, "Fingerprint", Build.FINGERPRINT);
        l.addAll(phoneRows(c));
        return l;
    }

    @SuppressWarnings("HardwareIds")
    private static String androidId(Context c) {
        try {
            return Settings.Secure.getString(c.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Throwable e) {
            return "-";
        }
    }

    private static boolean rootBinary() {
        String[] paths = {"/system/bin/su", "/system/xbin/su", "/sbin/su", "/system/sbin/su", "/vendor/bin/su", "/su/bin/su", "/data/local/xbin/su", "/data/local/bin/su"};
        for (String p : paths) {
            if (new java.io.File(p).exists()) return true;
        }
        return false;
    }

    private static String yesNo(Context c, boolean v) {
        return v ? s(c, R.string.supported) : s(c, R.string.not_supported);
    }

    private static String encryption(Context c) {
        try {
            android.app.admin.DevicePolicyManager dpm = (android.app.admin.DevicePolicyManager) c.getSystemService(Context.DEVICE_POLICY_SERVICE);
            int st = dpm.getStorageEncryptionStatus();
            return st == android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE
                    || st == android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER ? "Active"
                    : st == android.app.admin.DevicePolicyManager.ENCRYPTION_STATUS_UNSUPPORTED ? "Unsupported" : "Inactive";
        } catch (Throwable e) {
            return "-";
        }
    }

    private static boolean secureLock(Context c) {
        try {
            android.app.KeyguardManager km = (android.app.KeyguardManager) c.getSystemService(Context.KEYGUARD_SERVICE);
            return Build.VERSION.SDK_INT >= 23 && km.isDeviceSecure();
        } catch (Throwable e) {
            return false;
        }
    }

    private static String webView(Context c) {
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                android.content.pm.PackageInfo pi = android.webkit.WebView.getCurrentWebViewPackage();
                if (pi != null) return pi.versionName;
            }
        } catch (Throwable ignored) {
        }
        return "-";
    }

    private static String deviceName(Context c) {
        try {
            String n = Settings.Global.getString(c.getContentResolver(), "device_name");
            return n == null || n.isEmpty() ? Build.MODEL : n;
        } catch (Throwable e) {
            return Build.MODEL;
        }
    }

    private static List<String[]> phoneRows(Context c) {
        List<String[]> l = new ArrayList<>();
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            int t = tm.getPhoneType();
            add(l, "Phone type", t == TelephonyManager.PHONE_TYPE_GSM ? "GSM" : t == TelephonyManager.PHONE_TYPE_CDMA ? "CDMA" : t == TelephonyManager.PHONE_TYPE_SIP ? "SIP" : "None");
        }
        boolean esim = Build.VERSION.SDK_INT >= 28 && c.getPackageManager().hasSystemFeature("android.hardware.telephony.euicc");
        add(l, "eSIM", supportedText(c, esim));
        return l;
    }

    private static String prop(String key) {
        try {
            Process p = new ProcessBuilder("getprop", key).redirectErrorStream(true).start();
            BufferedReader r = new BufferedReader(new java.io.InputStreamReader(p.getInputStream()));
            String v = r.readLine();
            r.close();
            return v == null ? "" : v.trim();
        } catch (Throwable e) {
            return "";
        }
    }

    public static String androidReleaseDate(int api) {
        String[] d = {"Nov 4, 2014", "Mar 9, 2015", "Oct 5, 2015", "Aug 22, 2016", "Oct 4, 2016", "Aug 21, 2017", "Dec 5, 2017",
                "Aug 6, 2018", "Sep 3, 2019", "Sep 8, 2020", "Oct 4, 2021", "Mar 7, 2022", "Aug 15, 2022", "Oct 4, 2023",
                "Sep 3, 2024", "Jun 10, 2025"};
        int i = api - 21;
        return i >= 0 && i < d.length ? d[i] : "-";
    }

    public static String androidName(int api) {
        switch (api) {
            case 21: case 22: return "Android " + (api == 21 ? "5.0" : "5.1") + " - Lollipop";
            case 23: return "Android 6 - Marshmallow";
            case 24: case 25: return "Android " + (api == 24 ? "7.0" : "7.1") + " - Nougat";
            case 26: case 27: return "Android " + (api == 26 ? "8.0" : "8.1") + " - Oreo";
            case 28: return "Android 9 - Pie";
            case 29: return "Android 10 - Quince Tart";
            case 30: return "Android 11 - Red Velvet Cake";
            case 31: case 32: return "Android " + (api == 31 ? "12" : "12L") + " - Snow Cone";
            case 33: return "Android 13 - Tiramisu";
            case 34: return "Android 14 - Upside Down Cake";
            case 35: return "Android 15 - Vanilla Ice Cream";
            case 36: return "Android 16 - Baklava";
            default: return "Android " + Build.VERSION.RELEASE;
        }
    }

    private static String sdkExtension() {
        try {
            if (Build.VERSION.SDK_INT >= 30) {
                return String.valueOf(android.os.ext.SdkExtensions.getExtensionVersion(Build.VERSION_CODES.R));
            }
        } catch (Throwable ignored) {
        }
        return "-";
    }

    private static String releasedWith() {
        try {
            int api = Integer.parseInt(prop("ro.product.first_api_level"));
            return androidName(api).replace(" - ", " (") + (androidName(api).contains(" - ") ? ")" : "");
        } catch (Exception e) {
            return "-";
        }
    }

    private static String uiVersion() {
        String hyper = prop("ro.mi.os.version.name");
        if (!hyper.isEmpty()) return hyper + " " + prop("ro.mi.os.version.incremental");
        String miui = prop("ro.miui.ui.version.name");
        if (!miui.isEmpty()) return "MIUI " + miui;
        String oneui = prop("ro.build.version.oneui");
        if (!oneui.isEmpty()) return "One UI " + oneui;
        return "";
    }

    private static String mainlineUpdate(Context c) {
        try {
            return c.getPackageManager().getPackageInfo("com.google.android.modulemetadata", 0).versionName;
        } catch (Throwable e) {
            return "-";
        }
    }

    private static String bootloaderState() {
        String s = prop("ro.boot.verifiedbootstate");
        String lock = prop("ro.boot.flash.locked");
        if ("0".equals(lock) || "orange".equals(s)) return "Unlocked";
        if ("1".equals(lock) || "green".equals(s)) return "Locked";
        return "-";
    }

    private static List<String[]> system(Context c) {
        List<String[]> l = new ArrayList<>();
        add(l, "Codename", androidName(Build.VERSION.SDK_INT));
        add(l, s(c, R.string.android), Build.VERSION.RELEASE);
        add(l, s(c, R.string.api_level), String.valueOf(Build.VERSION.SDK_INT));
        add(l, "SDK extension", sdkExtension());
        add(l, "Released with", releasedWith());
        add(l, "System UI", uiVersion());
        add(l, s(c, R.string.security_patch), Build.VERSION.SDK_INT >= 23 ? Build.VERSION.SECURITY_PATCH : "-");
        add(l, "Google Play system update", mainlineUpdate(c));
        add(l, s(c, R.string.build), Build.DISPLAY);
        add(l, "Baseband", Build.getRadioVersion());
        add(l, s(c, R.string.kernel), System.getProperty("os.version"));
        add(l, "Bootloader", Build.BOOTLOADER + " (" + bootloaderState() + ")");
        add(l, "Java VM", System.getProperty("java.vm.version"));
        add(l, "Build type", Build.TYPE + " / " + Build.TAGS);
        add(l, "Build date", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(Build.TIME)));
        add(l, "Project Treble", yesNo(c, "true".equals(prop("ro.treble.enabled"))));
        add(l, "A/B partitions", yesNo(c, "true".equals(prop("ro.build.ab_update"))));
        add(l, "Dynamic partitions", yesNo(c, "true".equals(prop("ro.boot.dynamic_partitions"))));
        add(l, "Virtual A/B", yesNo(c, "true".equals(prop("ro.virtual_ab.enabled"))));
        add(l, "Storage encryption", encryption(c));
        add(l, "Secure lock screen", yesNo(c, secureLock(c)));
        add(l, "WebView", webView(c));
        add(l, "Boot time", java.text.DateFormat.getDateTimeInstance().format(new java.util.Date(System.currentTimeMillis() - SystemClock.elapsedRealtime())));
        add(l, "Root (su binary)", yesNo(c, rootBinary()));
        add(l, "SELinux", "1".equals(readText("/sys/fs/selinux/enforce")) ? "Enforcing" : "-");
        add(l, "Google Play services", playServices(c));
        add(l, "Widevine", widevine());
        add(l, s(c, R.string.language), Locale.getDefault().toLanguageTag());
        add(l, "Time zone", TimeZone.getDefault().getID());
        long up = SystemClock.elapsedRealtime() / 1000;
        add(l, "Uptime", String.format(Locale.US, "%dd %dh %dm %ds", up / 86400, (up % 86400) / 3600, (up % 3600) / 60, up % 60));
        return l;
    }

    public static String report(Context c) {
        StringBuilder sb = new StringBuilder("CPU-V report\n");
        for (String type : new String[]{"device", "system", "cpu", "battery", "network", "connectivity", "display", "memory", "camera", "audio"}) {
            sb.append("\n== ").append(type.toUpperCase(Locale.US)).append(" ==\n");
            for (String[] r : rows(c, type)) sb.append(r[0]).append(": ").append(r[1]).append('\n');
        }
        return sb.toString();
    }

    private static String readText(String path) {
        try (BufferedReader r = new BufferedReader(new FileReader(path))) {
            String line = r.readLine();
            return line == null ? null : line.trim();
        } catch (Exception e) {
            return null;
        }
    }

    private static long readLong(String path) {
        try {
            return Long.parseLong(readText(path));
        } catch (Exception e) {
            return -1;
        }
    }

    private static List<String[]> cpu(Context c) {
        List<String[]> l = new ArrayList<>();
        add(l, s(c, R.string.hardware), chipsetName());
        String[] gpu = GpuInfo.get();
        add(l, "GPU", gpu[0]);
        add(l, "GPU " + s(c, R.string.vendor), gpu[1]);
        add(l, "OpenGL ES", gpu[2]);
        add(l, "Vulkan", vulkan(c));
        add(l, s(c, R.string.abi), Arrays.toString(Build.SUPPORTED_ABIS).replace("[", "").replace("]", ""));
        int n = Runtime.getRuntime().availableProcessors();
        add(l, s(c, R.string.cores), String.valueOf(n));
        for (int i = 0; i < n; i++) {
            String base = "/sys/devices/system/cpu/cpu" + i + "/cpufreq/";
            long cur = readLong(base + "scaling_cur_freq");
            long max = readLong(base + "cpuinfo_max_freq");
            String v = cur < 0 ? "-" : (cur / 1000) + " MHz" + (max < 0 ? "" : " / " + (max / 1000) + " MHz");
            add(l, "Core " + i, v);
        }
        java.util.TreeMap<Long, Integer> clusters = new java.util.TreeMap<>(java.util.Collections.reverseOrder());
        for (int i = 0; i < n; i++) {
            long max = readLong("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
            if (max > 0) clusters.put(max, clusters.containsKey(max) ? clusters.get(max) + 1 : 1);
        }
        if (!clusters.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (java.util.Map.Entry<Long, Integer> e : clusters.entrySet()) {
                sb.append(e.getValue()).append(" x ").append(e.getKey() / 1000).append(" MHz + ");
            }
            add(l, "Core clusters", sb.substring(0, sb.length() - 3));
        }
        String gov = readText("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor");
        add(l, "Governor", gov);
        String avail = readText("/sys/devices/system/cpu/cpu0/cpufreq/scaling_available_frequencies");
        if (avail != null && !avail.trim().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String f : avail.trim().split("\\s+")) {
                try {
                    sb.append(Long.parseLong(f) / 1000).append(" MHz, ");
                } catch (NumberFormatException ignored) {
                }
            }
            if (sb.length() > 2) add(l, "Available frequencies", sb.substring(0, sb.length() - 2));
        }
        add(l, "CPU features", cpuFeatures());
        add(l, "OpenGL extensions (" + GpuInfo.extensionCount() + ")", GpuInfo.extensionList());
        return l;
    }

    private static Intent batteryIntent(Context c) {
        return c.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    public static long currentMa(Context c) {
        BatteryManager bm = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
        long v = bm.getLongProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW);
        if (Math.abs(v) >= 10000) {
            v /= 1000;
        }
        return v;
    }

    public static String batterySummary(Context c) {
        Intent i = batteryIntent(c);
        if (i == null) {
            return "-";
        }
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / Math.max(1, i.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
        return s(c, R.string.battery) + ": " + level + "% \u00B7 " + String.format(Locale.US, "%.1f \u00B0C", i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f);
    }

    private static String dataFileSystem() {
        try (BufferedReader r = new BufferedReader(new FileReader("/proc/mounts"))) {
            String line;
            while ((line = r.readLine()) != null) {
                String[] p = line.split(" ");
                if (p.length > 2 && p[1].equals("/data")) return p[2] + " (" + p[0] + ")";
            }
        } catch (Exception ignored) {
        }
        return "-";
    }

    private static String cpuFeatures() {
        try (BufferedReader r = new BufferedReader(new FileReader("/proc/cpuinfo"))) {
            String line;
            while ((line = r.readLine()) != null) {
                String low = line.toLowerCase(Locale.US);
                if (low.startsWith("features") || low.startsWith("flags")) {
                    int i = line.indexOf(':');
                    if (i >= 0) return line.substring(i + 1).trim();
                }
            }
        } catch (Exception ignored) {
        }
        return "-";
    }

    private static List<String[]> battery(Context c) {
        List<String[]> l = new ArrayList<>();
        Intent i = batteryIntent(c);
        if (i == null) {
            return l;
        }
        int level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) * 100 / Math.max(1, i.getIntExtra(BatteryManager.EXTRA_SCALE, 100));
        add(l, s(c, R.string.level), level + "%");
        int st = i.getIntExtra(BatteryManager.EXTRA_STATUS, 0);
        String status = st == BatteryManager.BATTERY_STATUS_CHARGING ? s(c, R.string.charging)
                : st == BatteryManager.BATTERY_STATUS_DISCHARGING ? s(c, R.string.discharging)
                : st == BatteryManager.BATTERY_STATUS_FULL ? s(c, R.string.full)
                : s(c, R.string.not_charging);
        add(l, s(c, R.string.status), status);
        int plugged = i.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0);
        String source = (plugged & BatteryManager.BATTERY_PLUGGED_AC) != 0 ? "AC"
                : (plugged & BatteryManager.BATTERY_PLUGGED_USB) != 0 ? "USB"
                : (plugged & BatteryManager.BATTERY_PLUGGED_WIRELESS) != 0 ? "Wireless"
                : s(c, R.string.battery);
        add(l, s(c, R.string.power_source), source);
        add(l, s(c, R.string.health), i.getIntExtra(BatteryManager.EXTRA_HEALTH, 0) == BatteryManager.BATTERY_HEALTH_GOOD ? s(c, R.string.good) : s(c, R.string.problem));
        add(l, s(c, R.string.technology), i.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY));
        add(l, s(c, R.string.temperature), String.format(Locale.US, "%.1f \u00B0C", i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f));
        add(l, s(c, R.string.voltage), i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) + " mV");
        long ma = currentMa(c);
        add(l, s(c, R.string.current), ma + " mA");
        add(l, s(c, R.string.power), String.format(Locale.US, "%.2f W", i.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) * (double) ma / 1e6));
        BatteryManager bm = (BatteryManager) c.getSystemService(Context.BATTERY_SERVICE);
        int counter = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER);
        if (counter > 0) {
            add(l, "Charge counter", counter / 1000 + " mAh");
            int lv = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            if (lv > 0 && counter > 0) {
                add(l, "Estimated capacity", (counter / 1000L) * 100 / lv + " mAh");
            }
        }
        if (Build.VERSION.SDK_INT >= 34) {
            int cycles = i.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1);
            if (cycles >= 0) {
                add(l, "Cycle count", String.valueOf(cycles));
            }
        }
        return l;
    }

    private static String netName(int t) {
        switch (t) {
            case 1:
            case 2:
            case 4:
            case 7:
            case 11:
            case 16:
                return "2G";
            case 3:
            case 5:
            case 6:
            case 8:
            case 9:
            case 10:
            case 12:
            case 14:
            case 15:
            case 17:
                return "3G";
            case 13:
            case 19:
                return "4G LTE";
            case 20:
                return "5G NR";
            default:
                return "-";
        }
    }

    @SuppressWarnings("deprecation")
    private static List<String[]> network(Context c) {
        List<String[]> l = new ArrayList<>();
        ConnectivityManager cm = (ConnectivityManager) c.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo ni = cm.getActiveNetworkInfo();
        add(l, s(c, R.string.connection), ni != null && ni.isConnected() ? ni.getTypeName() : "-");
        WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wi = wm == null ? null : wm.getConnectionInfo();
        if (wi != null && wi.getNetworkId() != -1) {
            int ip = wi.getIpAddress();
            add(l, s(c, R.string.ip_address), String.format(Locale.US, "%d.%d.%d.%d", ip & 255, (ip >> 8) & 255, (ip >> 16) & 255, (ip >> 24) & 255));
            String ssid = "-";
            if (granted(c, Manifest.permission.ACCESS_FINE_LOCATION) && wi.getSSID() != null && !wi.getSSID().contains("unknown")) {
                ssid = wi.getSSID().replace("\"", "");
            }
            add(l, s(c, R.string.ssid), ssid);
            add(l, s(c, R.string.link_speed), wi.getLinkSpeed() + " Mbps");
            add(l, s(c, R.string.frequency), wi.getFrequency() + " MHz");
            int f = wi.getFrequency();
            add(l, "Channel", String.valueOf(f >= 5000 ? (f - 5000) / 5 : f == 2484 ? 14 : (f - 2407) / 5));
            DhcpInfo di = wm.getDhcpInfo();
            if (di != null) {
                add(l, "Gateway", ipString(di.gateway));
                add(l, "Subnet mask", ipString(di.netmask));
                add(l, "DNS 1", ipString(di.dns1));
                add(l, "DNS 2", ipString(di.dns2));
            }
        }
        TelephonyManager tm = (TelephonyManager) c.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm != null) {
            add(l, s(c, R.string.operator), tm.getNetworkOperatorName());
            if (ni != null && ni.isConnected() && ni.getType() == ConnectivityManager.TYPE_MOBILE) {
                add(l, s(c, R.string.network_type), ni.getSubtypeName());
            }
        }
        return l;
    }

    @SuppressWarnings("deprecation")
    private static List<String[]> display(Context c) {
        List<String[]> l = new ArrayList<>();
        WindowManager wm = (WindowManager) c.getSystemService(Context.WINDOW_SERVICE);
        Display d = wm.getDefaultDisplay();
        DisplayMetrics dm = new DisplayMetrics();
        d.getRealMetrics(dm);
        double inch = Math.sqrt(Math.pow(dm.widthPixels / dm.xdpi, 2) + Math.pow(dm.heightPixels / dm.ydpi, 2));
        add(l, s(c, R.string.resolution), dm.widthPixels + " x " + dm.heightPixels);
        add(l, s(c, R.string.density), dm.densityDpi + " dpi");
        add(l, s(c, R.string.screen_size), String.format(Locale.US, "%.1f\"", inch));
        Set<String> rates = new LinkedHashSet<>();
        if (Build.VERSION.SDK_INT >= 23) {
            for (Display.Mode m : d.getSupportedModes()) {
                rates.add(String.format(Locale.US, "%.0f Hz", m.getRefreshRate()));
            }
        } else {
            rates.add(String.format(Locale.US, "%.0f Hz", d.getRefreshRate()));
        }
        add(l, s(c, R.string.refresh_rate), rates.toString().replace("[", "").replace("]", ""));
        add(l, "Adaptive refresh rate (estimated)", yesNo(c, rates.size() > 2));
        add(l, "Current refresh rate", String.format(Locale.US, "%.0f Hz", d.getRefreshRate()));
        add(l, "xdpi / ydpi", String.format(Locale.US, "%.0f / %.0f", dm.xdpi, dm.ydpi));
        add(l, "HDR", hdr(d));
        boolean wide = Build.VERSION.SDK_INT >= 26 && c.getResources().getConfiguration().isScreenWideColorGamut();
        add(l, "Wide color gamut", wide ? s(c, R.string.supported) : s(c, R.string.not_supported));
        int b = Settings.System.getInt(c.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, -1);
        add(l, "Brightness", b < 0 ? "-" : b * 100 / 255 + "%");
        return l;
    }

    private static ActivityManager.MemoryInfo memInfo(Context c) {
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        return mi;
    }

    public static int ramPercent(Context c) {
        ActivityManager.MemoryInfo mi = memInfo(c);
        return (int) ((mi.totalMem - mi.availMem) * 100 / Math.max(1, mi.totalMem));
    }

    private static List<String[]> memory(Context c) {
        List<String[]> l = new ArrayList<>();
        ActivityManager.MemoryInfo mi = memInfo(c);
        String mem = s(c, R.string.memory) + " ";
        add(l, mem + s(c, R.string.total), Formatter.formatFileSize(c, mi.totalMem));
        add(l, mem + s(c, R.string.used), Formatter.formatFileSize(c, mi.totalMem - mi.availMem));
        add(l, mem + s(c, R.string.free), Formatter.formatFileSize(c, mi.availMem));
        StatFs sf = new StatFs(Environment.getDataDirectory().getPath());
        long total = sf.getBlockCountLong() * sf.getBlockSizeLong();
        long free = sf.getAvailableBlocksLong() * sf.getBlockSizeLong();
        String st = s(c, R.string.storage) + " ";
        add(l, st + s(c, R.string.total), Formatter.formatFileSize(c, total));
        add(l, st + s(c, R.string.used), Formatter.formatFileSize(c, total - free));
        add(l, st + s(c, R.string.free), Formatter.formatFileSize(c, free));
        long swapTotal = meminfoKb("SwapTotal") * 1024;
        long swapFree = meminfoKb("SwapFree") * 1024;
        add(l, "Cached", Formatter.formatFileSize(c, meminfoKb("Cached") * 1024));
        add(l, "Buffers", Formatter.formatFileSize(c, meminfoKb("Buffers") * 1024));
        add(l, "Available (kernel)", Formatter.formatFileSize(c, meminfoKb("MemAvailable") * 1024));
        ActivityManager am = (ActivityManager) c.getSystemService(Context.ACTIVITY_SERVICE);
        add(l, "App heap limit", am.getMemoryClass() + " MB");
        add(l, "App large heap limit", am.getLargeMemoryClass() + " MB");
        add(l, "Low RAM device", yesNo(c, am.isLowRamDevice()));
        try {
            StatFs sys = new StatFs(Environment.getRootDirectory().getPath());
            add(l, "System partition", Formatter.formatFileSize(c, sys.getBlockCountLong() * sys.getBlockSizeLong()));
        } catch (Exception ignored) {
        }
        add(l, "Data file system", dataFileSystem());
        java.io.File[] ext = c.getExternalFilesDirs(null);
        add(l, "Storage volumes", String.valueOf(ext == null ? 1 : ext.length));
        add(l, "Swap " + s(c, R.string.total), Formatter.formatFileSize(c, swapTotal));
        add(l, "Swap " + s(c, R.string.used), Formatter.formatFileSize(c, Math.max(0, swapTotal - swapFree)));
        return l;
    }

    private static List<String[]> connectivity(Context c) {
        List<String[]> l = new ArrayList<>();
        PackageManager pm = c.getPackageManager();
        String yes = s(c, R.string.supported);
        String no = s(c, R.string.not_supported);
        String[][] features = {
                {"Wi-Fi", "android.hardware.wifi"},
                {"Wi-Fi Direct", "android.hardware.wifi.direct"},
                {"Bluetooth", "android.hardware.bluetooth"},
                {"Bluetooth LE", "android.hardware.bluetooth_le"},
                {"NFC", "android.hardware.nfc"},
                {"NFC HCE", "android.hardware.nfc.hce"},
                {"UWB", "android.hardware.uwb"},
                {"USB Host", "android.hardware.usb.host"},
                {"USB Accessory", "android.hardware.usb.accessory"},
                {"GPS", "android.hardware.location.gps"},
                {"Fingerprint", "android.hardware.fingerprint"},
                {"Telephony", "android.hardware.telephony"},
                {"Infrared", "android.hardware.consumer_ir"}
        };
        WifiManager wm = (WifiManager) c.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        for (String[] f : features) {
            add(l, f[0], pm.hasSystemFeature(f[1]) ? yes : no);
            if (f[0].equals("Wi-Fi") && wm != null) {
                add(l, "Wi-Fi 5 GHz", wm.is5GHzBandSupported() ? yes : no);
                add(l, "Wi-Fi 6 GHz", Build.VERSION.SDK_INT >= 30 && wm.is6GHzBandSupported() ? yes : no);
            }
        }
        return l;
    }

    private static List<String[]> bluetooth(Context c) {
        List<String[]> l = new ArrayList<>();
        BluetoothManager bm = (BluetoothManager) c.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter a = bm == null ? null : bm.getAdapter();
        add(l, "Bluetooth", supportedText(c, a != null));
        add(l, "Bluetooth LE", supportedText(c, c.getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")));
        if (a == null || !bluetoothAllowed(c)) {
            return l;
        }
        try {
            add(l, s(c, R.string.status), onOff(c, a.isEnabled()));
            add(l, s(c, R.string.device), a.getName());
            add(l, "Multiple advertisement", supportedText(c, a.isMultipleAdvertisementSupported()));
            add(l, "Offloaded filtering", supportedText(c, a.isOffloadedFilteringSupported()));
            add(l, "Offloaded scan batching", supportedText(c, a.isOffloadedScanBatchingSupported()));
            if (Build.VERSION.SDK_INT >= 26) {
                add(l, "LE 2M PHY", supportedText(c, a.isLe2MPhySupported()));
                add(l, "LE Coded PHY", supportedText(c, a.isLeCodedPhySupported()));
                add(l, "LE extended advertising", supportedText(c, a.isLeExtendedAdvertisingSupported()));
                add(l, "LE periodic advertising", supportedText(c, a.isLePeriodicAdvertisingSupported()));
            }
            if (Build.VERSION.SDK_INT >= 33) {
                add(l, "LE Audio", supportedText(c, a.isLeAudioSupported() == BluetoothStatusCodes.FEATURE_SUPPORTED));
            }
            for (BluetoothDevice d : a.getBondedDevices()) {
                add(l, "Paired", d.getName());
            }
        } catch (SecurityException ignored) {
        }
        return l;
    }

    public static List<String[]> codecRows() {
        List<String[]> decoders = new ArrayList<>();
        List<String[]> encoders = new ArrayList<>();
        for (MediaCodecInfo info : new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos()) {
            String accel = "";
            if (Build.VERSION.SDK_INT >= 29) {
                accel = info.isHardwareAccelerated() ? " \u00B7 HW" : " \u00B7 SW";
            }
            String kind = info.isEncoder() ? "Encoder" : "Decoder";
            String[] row = {kind + accel, info.getName() + "\n" + Arrays.toString(info.getSupportedTypes()).replace("[", "").replace("]", "")};
            (info.isEncoder() ? encoders : decoders).add(row);
        }
        List<String[]> all = new ArrayList<>();
        all.add(new String[]{"Decoders / Encoders", decoders.size() + " / " + encoders.size()});
        all.addAll(decoders);
        all.addAll(encoders);
        return all;
    }

    private static List<String[]> camera(Context c) {
        List<String[]> l = new ArrayList<>();
        try {
            CameraManager cm = (CameraManager) c.getSystemService(Context.CAMERA_SERVICE);
            for (String id : cm.getCameraIdList()) {
                CameraCharacteristics ch = cm.getCameraCharacteristics(id);
                Integer facing = ch.get(CameraCharacteristics.LENS_FACING);
                boolean front = facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT;
                String p = s(c, front ? R.string.front_camera : R.string.back_camera) + " " + id + " \u00B7 ";
                Size px = ch.get(CameraCharacteristics.SENSOR_INFO_PIXEL_ARRAY_SIZE);
                if (px != null) {
                    add(l, p + "MP", String.format(Locale.US, "%.1f MP", px.getWidth() * (double) px.getHeight() / 1e6));
                    add(l, p + s(c, R.string.resolution), px.getWidth() + " x " + px.getHeight());
                }
                float[] focal = ch.get(CameraCharacteristics.LENS_INFO_AVAILABLE_FOCAL_LENGTHS);
                if (focal != null && focal.length > 0) {
                    add(l, p + s(c, R.string.focal_length), focal[0] + " mm");
                }
                float[] aperture = ch.get(CameraCharacteristics.LENS_INFO_AVAILABLE_APERTURES);
                if (aperture != null && aperture.length > 0) {
                    add(l, p + "Aperture", "f/" + aperture[0]);
                }
                SizeF size = ch.get(CameraCharacteristics.SENSOR_INFO_PHYSICAL_SIZE);
                if (size != null) {
                    add(l, p + "Sensor", String.format(Locale.US, "%.2f x %.2f mm", size.getWidth(), size.getHeight()));
                }
                android.hardware.camera2.params.StreamConfigurationMap map = ch.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                if (map != null) {
                    Size[] vs = map.getOutputSizes(android.media.MediaRecorder.class);
                    Size best = null;
                    if (vs != null) for (Size z : vs) if (best == null || (long) z.getWidth() * z.getHeight() > (long) best.getWidth() * best.getHeight()) best = z;
                    if (best != null) add(l, p + "Max video size", best.getWidth() + " x " + best.getHeight());
                }
                Integer level = ch.get(CameraCharacteristics.INFO_SUPPORTED_HARDWARE_LEVEL);
                String[] lv = {"Limited", "Full", "Legacy", "Level 3", "External"};
                add(l, p + "Hardware level", level == null || level < 0 || level >= lv.length ? "-" : lv[level]);
                int[] ois = ch.get(CameraCharacteristics.LENS_INFO_AVAILABLE_OPTICAL_STABILIZATION);
                add(l, p + "OIS", yesNo(c, ois != null && ois.length > 1));
                Float zoom = ch.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM);
                if (zoom != null) add(l, p + "Max digital zoom", zoom + "x");
                android.util.Range<Integer> iso = ch.get(CameraCharacteristics.SENSOR_INFO_SENSITIVITY_RANGE);
                if (iso != null) add(l, p + "ISO range", iso.getLower() + " - " + iso.getUpper());
                int[] caps = ch.get(CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES);
                boolean raw = false;
                if (caps != null) for (int cp : caps) if (cp == CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) raw = true;
                add(l, p + "RAW capture", yesNo(c, raw));
                Boolean flash = ch.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
                add(l, p + s(c, R.string.flash), flash != null && flash ? s(c, R.string.supported) : s(c, R.string.not_supported));
            }
        } catch (Exception ignored) {
        }
        return l;
    }

    private static List<String[]> thermal(Context c) {
        List<String[]> l = new ArrayList<>();
        Intent i = batteryIntent(c);
        if (i != null) {
            add(l, s(c, R.string.battery), String.format(Locale.US, "%.1f \u00B0C", i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10f));
        }
        if (Build.VERSION.SDK_INT >= 29) {
            PowerManager pm = (PowerManager) c.getSystemService(Context.POWER_SERVICE);
            String[] names = {"None", "Light", "Moderate", "Severe", "Critical", "Emergency", "Shutdown"};
            add(l, s(c, R.string.thermal_status), names[Math.max(0, Math.min(6, pm.getCurrentThermalStatus()))]);
        }
        for (int z = 0; z < 40; z++) {
            String base = "/sys/class/thermal/thermal_zone" + z + "/";
            String type = readText(base + "type");
            long temp = readLong(base + "temp");
            if (type != null && temp > 0) {
                add(l, type, String.format(Locale.US, "%.1f \u00B0C", temp > 1000 ? temp / 1000f : temp));
            }
        }
        return l;
    }

    private static List<String[]> audio(Context c) {
        List<String[]> l = new ArrayList<>();
        android.media.AudioManager am = (android.media.AudioManager) c.getSystemService(Context.AUDIO_SERVICE);
        PackageManager pm = c.getPackageManager();
        if (Build.VERSION.SDK_INT >= 17 && am != null) {
            add(l, "Native sample rate", am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE) + " Hz");
            add(l, "Frames per buffer", am.getProperty(android.media.AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER));
        }
        add(l, "Low latency audio", yesNo(c, pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_LOW_LATENCY)));
        add(l, "Pro audio", yesNo(c, pm.hasSystemFeature(PackageManager.FEATURE_AUDIO_PRO)));
        add(l, "Microphone", yesNo(c, pm.hasSystemFeature(PackageManager.FEATURE_MICROPHONE)));
        add(l, "Dolby Digital (AC-3)", yesNo(c, hasCodec("audio/ac3")));
        add(l, "Dolby Digital Plus (E-AC-3)", yesNo(c, hasCodec("audio/eac3")));
        add(l, "Dolby Atmos (E-AC-3 JOC)", yesNo(c, hasCodec("audio/eac3-joc")));
        if (Build.VERSION.SDK_INT >= 23 && am != null) {
            int maxRate = 0;
            for (android.media.AudioDeviceInfo d : am.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)) {
                for (int r : d.getSampleRates()) maxRate = Math.max(maxRate, r);
            }
            add(l, "Hi-Res output (>= 96 kHz)", yesNo(c, maxRate >= 96000));
            for (android.media.AudioDeviceInfo d : am.getDevices(android.media.AudioManager.GET_DEVICES_OUTPUTS)) {
                add(l, "Output", audioType(d.getType()) + (d.getProductName() == null ? "" : " (" + d.getProductName() + ")"));
            }
            for (android.media.AudioDeviceInfo d : am.getDevices(android.media.AudioManager.GET_DEVICES_INPUTS)) {
                add(l, "Input", audioType(d.getType()) + (d.getProductName() == null ? "" : " (" + d.getProductName() + ")"));
            }
        }
        return l;
    }

    private static boolean hasCodec(String mime) {
        for (MediaCodecInfo info : new MediaCodecList(MediaCodecList.ALL_CODECS).getCodecInfos()) {
            for (String type : info.getSupportedTypes()) {
                if (type.equalsIgnoreCase(mime)) return true;
            }
        }
        return false;
    }

    private static String audioType(int t) {
        switch (t) {
            case 2: return "Speaker";
            case 1: return "Earpiece";
            case 3: return "Wired headset";
            case 4: return "Wired headphones";
            case 7: return "Bluetooth SCO";
            case 8: return "Bluetooth A2DP";
            case 11: case 12: return "USB";
            case 15: return "Built-in mic";
            default: return "Type " + t;
        }
    }

    private static List<String[]> features(Context c) {
        List<String[]> l = new ArrayList<>();
        FeatureInfo[] all = c.getPackageManager().getSystemAvailableFeatures();
        List<String> names = new ArrayList<>();
        if (all != null) {
            for (FeatureInfo f : all) {
                if (f.name != null) names.add(f.name.replace("android.hardware.", "").replace("android.software.", "sw.").replace("android.", ""));
            }
        }
        java.util.Collections.sort(names);
        add(l, "Total features", String.valueOf(names.size()));
        for (String n : names) add(l, n, "\u2713");
        return l;
    }

    private static String ipString(int ip) {
        return ip == 0 ? "-" : String.format(Locale.US, "%d.%d.%d.%d", ip & 255, (ip >> 8) & 255, (ip >> 16) & 255, (ip >> 24) & 255);
    }

    private static long meminfoKb(String key) {
        try (BufferedReader r = new BufferedReader(new FileReader("/proc/meminfo"))) {
            String line;
            while ((line = r.readLine()) != null) {
                if (line.startsWith(key + ":")) {
                    return Long.parseLong(line.replaceAll("[^0-9]", ""));
                }
            }
        } catch (Exception ignored) {
        }
        return 0;
    }

    private static String playServices(Context c) {
        try {
            return c.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionName;
        } catch (Exception e) {
            return "-";
        }
    }

    private static String widevine() {
        try {
            MediaDrm drm = new MediaDrm(new UUID(0xEDEF8BA979D64ACEL, 0xA3C827DCD51D21EDL));
            String result = drm.getPropertyString("securityLevel") + " / " + drm.getPropertyString("version");
            drm.release();
            return result;
        } catch (Throwable e) {
            return "-";
        }
    }

    private static String vulkan(Context c) {
        for (FeatureInfo f : c.getPackageManager().getSystemAvailableFeatures()) {
            if ("android.hardware.vulkan.version".equals(f.name)) {
                return (f.version >> 22) + "." + ((f.version >> 12) & 0x3ff);
            }
        }
        return "-";
    }

    @SuppressWarnings("deprecation")
    private static String hdr(Display d) {
        if (Build.VERSION.SDK_INT < 24 || d.getHdrCapabilities() == null) {
            return "-";
        }
        List<String> names = new ArrayList<>();
        for (int t : d.getHdrCapabilities().getSupportedHdrTypes()) {
            names.add(t == 1 ? "Dolby Vision" : t == 2 ? "HDR10" : t == 3 ? "HLG" : t == 4 ? "HDR10+" : String.valueOf(t));
        }
        return names.isEmpty() ? "-" : names.toString().replace("[", "").replace("]", "");
    }

    public static String sensorType(Sensor s) {
        String t = s.getStringType();
        return t == null ? String.valueOf(s.getType()) : t.replace("android.sensor.", "");
    }

    public static List<String[]> sensorRows(Context c, Sensor sensor) {
        List<String[]> l = new ArrayList<>();
        add(l, s(c, R.string.vendor), sensor.getVendor());
        add(l, s(c, R.string.type), sensorType(sensor));
        add(l, s(c, R.string.power), sensor.getPower() + " mA");
        add(l, s(c, R.string.max_range), String.valueOf(sensor.getMaximumRange()));
        add(l, s(c, R.string.resolution), String.valueOf(sensor.getResolution()));
        add(l, s(c, R.string.min_delay), sensor.getMinDelay() + " \u00B5s");
        add(l, "Version", String.valueOf(sensor.getVersion()));
        return l;
    }
}
