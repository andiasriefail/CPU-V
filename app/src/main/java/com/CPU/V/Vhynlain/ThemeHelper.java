package com.CPU.V.Vhynlain;

import android.app.Activity;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public final class ThemeHelper {
    private ThemeHelper() {
    }

    public static void apply(Context c) {
        int t = PrefUtil.theme(c);
        int mode = t == 0 ? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                : t == 1 ? AppCompatDelegate.MODE_NIGHT_NO
                : AppCompatDelegate.MODE_NIGHT_YES;
        AppCompatDelegate.setDefaultNightMode(mode);
    }

    public static void setup(Activity a) {
        if (PrefUtil.theme(a) == 3) {
            a.setTheme(R.style.Theme_CPUV_Black);
        }
    }
}
