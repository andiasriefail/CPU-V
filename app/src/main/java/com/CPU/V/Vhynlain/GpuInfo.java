package com.CPU.V.Vhynlain;

import android.opengl.EGL14;
import android.opengl.EGLConfig;
import android.opengl.EGLContext;
import android.opengl.EGLDisplay;
import android.opengl.EGLSurface;
import android.opengl.GLES20;

public final class GpuInfo {
    private static String[] cache;

    private GpuInfo() {
    }

    private static String safe(String s) {
        return s == null || s.isEmpty() ? "-" : s;
    }

    public static String extensionCount() {
        return get()[3];
    }

    public static String extensionList() {
        return get()[4];
    }

    public static synchronized String[] get() {
        if (cache != null) {
            return cache;
        }
        String[] result = {"-", "-", "-", "-", "-"};
        try {
            EGLDisplay display = EGL14.eglGetDisplay(EGL14.EGL_DEFAULT_DISPLAY);
            int[] version = new int[2];
            if (EGL14.eglInitialize(display, version, 0, version, 1)) {
                int[] attrs = {
                        EGL14.EGL_RENDERABLE_TYPE, EGL14.EGL_OPENGL_ES2_BIT,
                        EGL14.EGL_SURFACE_TYPE, EGL14.EGL_PBUFFER_BIT,
                        EGL14.EGL_NONE
                };
                EGLConfig[] configs = new EGLConfig[1];
                int[] count = new int[1];
                EGL14.eglChooseConfig(display, attrs, 0, configs, 0, 1, count, 0);
                if (count[0] > 0) {
                    int[] surfaceAttrs = {EGL14.EGL_WIDTH, 1, EGL14.EGL_HEIGHT, 1, EGL14.EGL_NONE};
                    EGLSurface surface = EGL14.eglCreatePbufferSurface(display, configs[0], surfaceAttrs, 0);
                    int[] contextAttrs = {EGL14.EGL_CONTEXT_CLIENT_VERSION, 2, EGL14.EGL_NONE};
                    EGLContext context = EGL14.eglCreateContext(display, configs[0], EGL14.EGL_NO_CONTEXT, contextAttrs, 0);
                    if (EGL14.eglMakeCurrent(display, surface, surface, context)) {
                        result[0] = safe(GLES20.glGetString(GLES20.GL_RENDERER));
                        result[1] = safe(GLES20.glGetString(GLES20.GL_VENDOR));
                        result[2] = safe(GLES20.glGetString(GLES20.GL_VERSION));
                        String ext = GLES20.glGetString(GLES20.GL_EXTENSIONS);
                        result[4] = ext == null || ext.isEmpty() ? "-" : ext.trim().replace(" ", "\n");
                        result[3] = ext == null || ext.isEmpty() ? "-" : String.valueOf(ext.trim().split("\\s+").length);
                    }
                    EGL14.eglMakeCurrent(display, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_SURFACE, EGL14.EGL_NO_CONTEXT);
                    EGL14.eglDestroyContext(display, context);
                    EGL14.eglDestroySurface(display, surface);
                }
            }
        } catch (Throwable ignored) {
        }
        cache = result;
        return result;
    }
}
