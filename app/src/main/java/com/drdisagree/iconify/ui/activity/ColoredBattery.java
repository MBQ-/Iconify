package com.drdisagree.iconify.ui.activity;

import static com.drdisagree.iconify.common.References.FRAMEWORK_PACKAGE;
import static com.drdisagree.iconify.common.References.SYSTEM_UI_PACKAGE;
import static com.drdisagree.iconify.utils.ColorUtil.ColorToSpecialHex;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.jaredrummler.android.colorpicker.ColorPickerDialog;
import com.jaredrummler.android.colorpicker.ColorPickerDialogListener;

import java.util.List;
import java.util.Objects;

public class ColoredBattery extends AppCompatActivity implements ColorPickerDialogListener {

    private static String colorBackground, colorFilled;
    private final List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    ColorPickerDialog.Builder colorPickerDialogBackground, colorPickerDialogFilled;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_colored_battery);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_colored_battery));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Enable colored battery
        @SuppressLint("UseSwitchCompatOrMaterialCode") Switch enable_colored_battery = findViewById(R.id.enable_colored_battery);
        enable_colored_battery.setChecked(OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay") || OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay") || Prefs.getBoolean("coloredBattery"));
        enable_colored_battery.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                if (OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay")) {
                    FabricatedOverlayUtil.buildAndEnableOverlay(FRAMEWORK_PACKAGE, "coloredBattery", "bool", "config_batterymeterDualTone", "1");
                    Prefs.putBoolean("coloredBattery", true);
                } else {
                    if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay"))
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_lorn_colored_battery), Toast.LENGTH_SHORT).show();
                    else if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay"))
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_plumpy_colored_battery), Toast.LENGTH_SHORT).show();
                }
            } else {
                if (OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay") && OverlayUtil.isOverlayDisabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay")) {
                    FabricatedOverlayUtil.disableOverlay("coloredBattery");
                    Prefs.putBoolean("coloredBattery", false);
                } else {
                    if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI2.overlay"))
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_lorn_colored_battery), Toast.LENGTH_SHORT).show();
                    else if (OverlayUtil.isOverlayEnabled(EnabledOverlays, "IconifyComponentIPSUI4.overlay"))
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_plumpy_colored_battery), Toast.LENGTH_SHORT).show();
                }

                FabricatedOverlayUtil.disableOverlay("batteryColorBackground");
                FabricatedOverlayUtil.disableOverlay("batteryColorFilled");
            }
        });

        if (!Objects.equals(Prefs.getString("batteryColorBackground"), "null"))
            colorBackground = Prefs.getString("batteryColorBackground");
        else
            colorBackground = String.valueOf(Color.parseColor("#FFF0F0F0"));

        if (!Objects.equals(Prefs.getString("batteryColorFilled"), "null"))
            colorFilled = Prefs.getString("batteryColorFilled");
        else
            colorFilled = String.valueOf(Color.parseColor("#FFF0F0F0"));

        // Battery background and filled color
        colorPickerDialogBackground = ColorPickerDialog.newBuilder();
        colorPickerDialogFilled = ColorPickerDialog.newBuilder();

        colorPickerDialogBackground.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorBackground)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);
        colorPickerDialogFilled.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorFilled)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

        LinearLayout battery_background_color = findViewById(R.id.battery_background_color);
        battery_background_color.setOnClickListener(v -> colorPickerDialogBackground.show(ColoredBattery.this));

        LinearLayout battery_filled_color = findViewById(R.id.battery_filled_color);
        battery_filled_color.setOnClickListener(v -> colorPickerDialogFilled.show(ColoredBattery.this));

        updateColorPreview();
    }

    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case 1:

                colorBackground = String.valueOf(color);
                Prefs.putString("batteryColorBackground", colorBackground);
                updateColorPreview();
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "batteryColorBackground", "color", "light_mode_icon_color_dual_tone_background", ColorToSpecialHex(Integer.parseInt(colorBackground)));
                colorPickerDialogBackground.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorBackground)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(1).setShowAlphaSlider(false).setShowColorShades(true);

                break;
            case 2:

                colorFilled = String.valueOf(color);
                Prefs.putString("batteryColorFilled", colorFilled);
                updateColorPreview();
                FabricatedOverlayUtil.buildAndEnableOverlay(SYSTEM_UI_PACKAGE, "batteryColorFilled", "color", "light_mode_icon_color_dual_tone_fill", ColorToSpecialHex(Integer.parseInt(colorFilled)));
                colorPickerDialogFilled.setDialogStyle(R.style.ColorPicker).setColor(Integer.parseInt(colorFilled)).setDialogType(ColorPickerDialog.TYPE_CUSTOM).setAllowCustom(false).setAllowPresets(true).setDialogId(2).setShowAlphaSlider(false).setShowColorShades(true);

                break;
        }
    }

    @Override
    public void onDialogDismissed(int dialogId) {
    }

    private void updateColorPreview() {
        View preview_color_picker_background = findViewById(R.id.preview_color_picker_background);
        View preview_color_picker_fill = findViewById(R.id.preview_color_picker_fill);
        GradientDrawable gd;

        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorBackground), Integer.parseInt(colorBackground)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_background.setBackgroundDrawable(gd);


        gd = new GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, new int[]{Integer.parseInt(colorFilled), Integer.parseInt(colorFilled)});
        gd.setCornerRadius(24 * getResources().getDisplayMetrics().density);
        preview_color_picker_fill.setBackgroundDrawable(gd);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}