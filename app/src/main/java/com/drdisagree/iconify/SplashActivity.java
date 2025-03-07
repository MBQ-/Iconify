package com.drdisagree.iconify;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.ui.activity.HomePage;
import com.drdisagree.iconify.ui.activity.WelcomePage;
import com.drdisagree.iconify.utils.ModuleUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.RootUtil;
import com.google.android.material.color.DynamicColors;
import com.topjohnwu.superuser.Shell;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private static SplashActivity mContext;

    static {
        Shell.enableVerboseLogging = BuildConfig.DEBUG;
        if (Shell.getCachedShell() == null)
            Shell.setDefaultBuilder(Shell.Builder.create().setFlags(Shell.FLAG_REDIRECT_STDERR).setTimeout(20));
    }

    private boolean keepShowing = true;
    private final Runnable runner = new Runnable() {
        @Override
        public void run() {
            Shell.getShell(shell -> {
                mContext = SplashActivity.this;

                Intent intent;

                if (RootUtil.isDeviceRooted() && RootUtil.isMagiskInstalled() && ModuleUtil.moduleExists() && OverlayUtil.overlayExists() && (BuildConfig.VERSION_CODE == Prefs.getInt("versionCode"))) {
                    keepShowing = false;
                    intent = new Intent(SplashActivity.this, HomePage.class);
                } else {
                    keepShowing = false;
                    intent = new Intent(SplashActivity.this, WelcomePage.class);
                }

                startActivity(intent);
                finish();
            });
        }
    };

    public static SplashActivity getContext() {
        return mContext;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        splashScreen.setKeepOnScreenCondition(() -> keepShowing);
        DynamicColors.applyToActivitiesIfAvailable(getApplication());

        Thread thread = new Thread(runner);
        thread.start();
    }
}