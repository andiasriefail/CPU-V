package com.CPU.V.Vhynlain;

import android.content.Context;
import android.content.SharedPreferences;

public final class PrefUtil {
    private PrefUtil() {
    }

    private static SharedPreferences prefs(Context c) {
        return c.getApplicationContext().getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public static int theme(Context c) {
        return prefs(c).getInt("theme", 0);
    }

    public static void setTheme(Context c, int value) {
        prefs(c).edit().putInt("theme", value).apply();
    }

    public static int testResult(Context c, String id) {
        return prefs(c).getInt("test_" + id, 0);
    }

    public static void setTestResult(Context c, String id, int value) {
        prefs(c).edit().putInt("test_" + id, value).apply();
    }
}
