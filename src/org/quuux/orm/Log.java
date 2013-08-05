package org.quuux.orm;

public class Log  {

    private static String sPrefix;


    public static void setsPrefix(final String prefix) {
        sPrefix = prefix;
    }


    public static String buildTag(final String tag) {
        return sPrefix == null ? tag : sPrefix + ":" + tag;
    }

    public static String buildTag(final Class<?> klass) {
        return buildTag(klass.getName());
    }

    public static void d(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(buildTag(tag), String.format(message, args));
        }
    }

    public static void d(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.d(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void v(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(buildTag(tag), String.format(message, args));
        }
    }

    public static void v(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.v(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void i(final String tag, final String message, final Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(buildTag(tag), String.format(message, args));
        }
    }

    public static void i(final String tag, final String message, final Throwable tr,  Object...args) {
        if (BuildConfig.DEBUG) {
            android.util.Log.i(buildTag(tag), String.format(message, args), tr);
        }
    }

    public static void e(final String tag, final String message, final Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args));
    }

    public static void e(final String tag, final String message, final Throwable tr,  Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args), tr);
    }


    public static void w(final String tag, final String message, final Object...args) {
        android.util.Log.w(buildTag(tag), String.format(message, args));
    }

    public static void w(final String tag, final String message, final Throwable tr,  Object...args) {
        android.util.Log.e(buildTag(tag), String.format(message, args), tr);
    }

}
