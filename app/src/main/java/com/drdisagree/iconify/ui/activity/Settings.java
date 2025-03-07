package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.text.LineBreaker;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.drdisagree.iconify.config.Prefs;
import com.drdisagree.iconify.config.RemotePrefs;
import com.drdisagree.iconify.ui.view.LoadingDialog;
import com.drdisagree.iconify.utils.FabricatedOverlayUtil;
import com.drdisagree.iconify.utils.OverlayUtil;
import com.drdisagree.iconify.utils.SystemUtil;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.topjohnwu.superuser.Shell;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Settings extends AppCompatActivity {

    private static final int REQUESTCODE_IMPORT = 1;
    private static final int REQUESTCODE_EXPORT = 2;
    public static List<String> EnabledOverlays = OverlayUtil.getEnabledOverlayList();
    LoadingDialog loadingDialog;

    public static void disableEverything() {
        SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
        Map<String, ?> map = prefs.getAll();

        for (Map.Entry<String, ?> item : map.entrySet()) {
            if (item.getValue() instanceof Boolean && ((Boolean) item.getValue()) && item.getKey().contains("fabricated")) {
                Prefs.putBoolean(item.getKey(), (Boolean) item.getValue());
                FabricatedOverlayUtil.disableOverlay(item.getKey().replace("fabricated", ""));
            }
        }

        for (String overlay : EnabledOverlays) {
            OverlayUtil.disableOverlay(overlay);
        }

        Prefs.clearAllPrefs();
        Prefs.putString("boot_id", Shell.cmd("cat /proc/sys/kernel/random/boot_id").exec().getOut().toString());
        Prefs.putInt("versionCode", BuildConfig.VERSION_CODE);
        Prefs.putBoolean("firstInstall", false);

        RemotePrefs.clearAllPrefs();

        SystemUtil.restartSystemUI();
    }

    @SuppressLint({"SetTextI18n", "WorldReadableFiles"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Show loading dialog
        loadingDialog = new LoadingDialog(this);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_settings));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Disable Everything
        TextView list_title_disableEverything = findViewById(R.id.list_title_disableEverything);
        TextView list_desc_disableEverything = findViewById(R.id.list_desc_disableEverything);
        Button button_disableEverything = findViewById(R.id.button_disableEverything);

        list_title_disableEverything.setText(getResources().getString(R.string.disable_everything_title));
        list_desc_disableEverything.setText(getResources().getString(R.string.disable_everything_desc));
        list_desc_disableEverything.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_disableEverything.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disable_everything), Toast.LENGTH_SHORT).show());

        button_disableEverything.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            Runnable runnable = () -> {
                disableEverything();

                runOnUiThread(() -> new Handler().postDelayed(() -> {
                    // Hide loading dialog
                    loadingDialog.hide();

                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_disabled_everything), Toast.LENGTH_SHORT).show();
                }, 3000));
            };
            Thread thread = new Thread(runnable);
            thread.start();

            return true;
        });

        // Restart SystemUI
        TextView list_title_restartSysui = findViewById(R.id.list_title_restartSysui);
        TextView list_desc_restartSysui = findViewById(R.id.list_desc_restartSysui);
        Button button_restartSysui = findViewById(R.id.button_restartSysui);

        list_title_restartSysui.setText(getResources().getString(R.string.restart_sysui_title));
        list_desc_restartSysui.setText(getResources().getString(R.string.restart_sysui_desc));
        list_desc_restartSysui.setJustificationMode(LineBreaker.JUSTIFICATION_MODE_INTER_WORD);

        button_restartSysui.setOnClickListener(v -> Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_restart_sysui), Toast.LENGTH_SHORT).show());

        button_restartSysui.setOnLongClickListener(v -> {
            // Show loading dialog
            loadingDialog.show(getResources().getString(R.string.loading_dialog_wait));

            new Handler().postDelayed(() -> {
                // Hide loading dialog
                loadingDialog.hide();

                // Restart SystemUI
                SystemUtil.restartSystemUI();
            }, 1000);

            return true;
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.settings_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemID = item.getItemId();

        if (itemID == android.R.id.home) {
            onBackPressed();
        } else if (itemID == R.id.menu_updates) {
            Intent intent = new Intent(Settings.this, AppUpdates.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_changelog) {
            Intent intent = new Intent(Settings.this, Changelog.class);
            startActivity(intent);
        } else if (itemID == R.id.menu_exportPrefs) {
            exportSettings();
        } else if (itemID == R.id.menu_importPrefs) {
            importSettings();
        } else if (itemID == R.id.menu_experimental_features) {
            Intent intent = new Intent(Settings.this, Experimental.class);
            startActivity(intent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void exportSettings() {
        Intent fileIntent = new Intent();
        fileIntent.setAction(Intent.ACTION_CREATE_DOCUMENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUESTCODE_EXPORT);
    }

    private void importSettings() {
        Intent fileIntent = new Intent();
        fileIntent.setAction(Intent.ACTION_GET_CONTENT);
        fileIntent.setType("*/*");
        startActivityForResult(fileIntent, REQUESTCODE_IMPORT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null)
            return;

        if (resultCode == RESULT_OK) {
            SharedPreferences prefs = Iconify.getAppContext().getSharedPreferences(Iconify.getAppContext().getPackageName(), Context.MODE_PRIVATE);
            switch (requestCode) {
                case REQUESTCODE_IMPORT:
                    AlertDialog alertDialog = new AlertDialog.Builder(Settings.this).create();
                    alertDialog.setTitle(getResources().getString(R.string.confirmation));
                    alertDialog.setMessage(getResources().getString(R.string.you_will_loose_current_setup));
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, getResources().getString(R.string.positive),
                            (dialog, which) -> {
                                dialog.dismiss();
                                try {
                                    Prefs.importPrefs(getContentResolver().openInputStream(data.getData()));
                                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.imported_settings), Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                                    e.printStackTrace();
                                }
                            });
                    alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, getResources().getString(R.string.negative),
                            (dialog, which) -> dialog.dismiss());
                    alertDialog.show();
                    break;
                case REQUESTCODE_EXPORT:
                    try {
                        Prefs.exportPrefs(prefs, getContentResolver().openOutputStream(data.getData()));
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.exported_settings), Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_error), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        loadingDialog.hide();
        super.onDestroy();
    }
}