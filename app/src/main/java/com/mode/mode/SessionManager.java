package com.mode.mode;

/**
 * Created by allanmarube on 5/5/15.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.UUID;

/**
 * Created by allanmarube on 5/5/15.
 */
public class SessionManager {
    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "CRITIQ";

    private static final String KEY_IS_LOGGEDIN = "isLoggedIn";

    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();

        if (!isSet()) {
            String personalId = UUID.randomUUID().toString();
            editor.putString("id", personalId);
            editor.putBoolean("set", true);
            editor.putBoolean("location", true);
            editor.commit();
        }
    }

    public boolean isSet(){
        return pref.getBoolean("set", false);
    }

    public String getId(){
        return pref.getString("id", null);
    }


    public void clear(){editor.clear(); editor.commit();}

}

