package com.drinker.core.util;

import android.app.Application;

/**
 * Created by liuzhuolin on 2016/11/4.
 */

public class AppUtils {

    private static final Application INSTANCE;

    static {
        Application app = null;
        try {
            app = (Application) Class.forName("android.app.AppGlobals").getMethod("getInitialApplication").invoke(null);
            if (app == null) {
                throw new IllegalStateException("Static initialization of Applications must be on main thread.");
            }
        } catch (final Exception e) {
            try {
                app = (Application) Class.forName("android.app.ActivityThread").getMethod("currentApplication").invoke(null);
            } catch (final Exception ex) {
            }
        } finally {
            INSTANCE = app;
        }
    }

    public static Application getApp() {
        return INSTANCE;
    }
}
