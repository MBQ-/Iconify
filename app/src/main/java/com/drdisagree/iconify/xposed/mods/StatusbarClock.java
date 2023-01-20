package com.drdisagree.iconify.xposed.mods;

import static com.drdisagree.iconify.common.References.STATUSBAR_CLOCKBG;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.config.XPrefs.Xprefs;
import static com.drdisagree.iconify.xposed.HookRes.resparams;
import static de.robv.android.xposed.XposedBridge.hookAllMethods;
import static de.robv.android.xposed.XposedBridge.log;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.content.res.ResourcesCompat;

import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.XPrefs;
import com.drdisagree.iconify.xposed.ModPack;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_InitPackageResources;
import de.robv.android.xposed.callbacks.XC_LayoutInflated;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

public class StatusbarClock extends ModPack implements IXposedHookLoadPackage {

    private static final String TAG = "Iconify - StatusbarClock: ";
    private static final String CLASS_CLOCK = SYSTEM_UI_PACKAGE + ".statusbar.policy.Clock";
    boolean showClockChip = false;
    private String rootPackagePath = "";

    public StatusbarClock(Context context) {
        super(context);
        if (!listensTo(context.getPackageName())) return;
    }

    @Override
    public void updatePrefs(String... Key) {
        if (Xprefs == null) return;

        showClockChip = Xprefs.getBoolean(STATUSBAR_CLOCKBG, false);
        setResources();
    }

    @Override
    public boolean listensTo(String packageName) {
        return packageName.equals(SYSTEM_UI_PACKAGE);
    }

    @Override
    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam) {
        if (!lpparam.packageName.equals(SYSTEM_UI_PACKAGE))
            return;

        rootPackagePath = lpparam.appInfo.sourceDir;

        final Class<?> Clock = XposedHelpers.findClass(CLASS_CLOCK, lpparam.classLoader);

        hookAllMethods(Clock, "updateClock", new XC_MethodHook() {
            @Override
            protected void afterHookedMethod(MethodHookParam param) {
                if (!showClockChip)
                    return;

                try {
                    TextView clock = (TextView) param.thisObject;
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics());

                    clock.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) clock.getLayoutParams();
                    params.setMarginEnd((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, mContext.getResources().getDisplayMetrics()));

                    Drawable clock_bg = ResourcesCompat.getDrawable(XPrefs.modRes, R.drawable.xposed_status_bar_clock_bg, mContext.getTheme());
                    clock.setBackground(clock_bg);
                } catch (Throwable t) {
                    log(t);
                }
            }
        });
    }

    private void setResources() {
        XC_InitPackageResources.InitPackageResourcesParam ourResparam = resparams.get(SYSTEM_UI_PACKAGE);
        if (ourResparam == null) return;

        if (!showClockChip)
            return;

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "quick_qs_status_icons", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") LinearLayout clock_container = liparam.view.findViewById(liparam.res.getIdentifier("clock_container", "id", SYSTEM_UI_PACKAGE));
                    clock_container.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock_container.requestLayout();
                }
            });
        } catch (Throwable t) {
            log("Failed to change height: " + t);
        }

        try {
            ourResparam.res.hookLayout(SYSTEM_UI_PACKAGE, "layout", "status_bar", new XC_LayoutInflated() {
                @Override
                public void handleLayoutInflated(XC_LayoutInflated.LayoutInflatedParam liparam) {
                    @SuppressLint("DiscouragedApi") TextView clock = liparam.view.findViewById(liparam.res.getIdentifier("clock", "id", SYSTEM_UI_PACKAGE));
                    int paddingTopBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, mContext.getResources().getDisplayMetrics());
                    int paddingStartEnd = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, mContext.getResources().getDisplayMetrics());
                    clock.setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom);
                    clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                    clock.setGravity(Gravity.CENTER);
                    ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) clock.getLayoutParams();
                    params.setMargins((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, mContext.getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0, mContext.getResources().getDisplayMetrics()),
                            (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, mContext.getResources().getDisplayMetrics()));
                    clock.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    clock.getLayoutParams().height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, mContext.getResources().getDisplayMetrics());
                }
            });
        } catch (Throwable t) {
            log("Failed to change height: " + t);
        }
    }
}
