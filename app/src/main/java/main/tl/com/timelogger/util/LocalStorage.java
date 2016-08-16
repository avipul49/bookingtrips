package main.tl.com.timelogger.util;

import android.content.Context;
import android.content.SharedPreferences;

public class LocalStorage {
    public static void saveString(Context context, String key, String value) {
        SharedPreferences sharedPref = context.getSharedPreferences("timelogger", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.commit();
    }

    public static String getString(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences("timelogger", Context.MODE_PRIVATE);
        return sharedPref.getString(key, null);
    }

    public static void removeKey(Context context, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences("timelogger", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.remove(key);
        editor.commit();
    }

    public static String escapeEmail(String email) {
        return (email).replace('.', ',');
    }

    public static String unescapeEmail(String email) {
        return (email).replace(',', '.');
    }
}
