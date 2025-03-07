package com.drdisagree.iconify.ui.activity;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.drdisagree.iconify.BuildConfig;
import com.drdisagree.iconify.Iconify;
import com.drdisagree.iconify.R;
import com.google.android.material.appbar.CollapsingToolbarLayout;

import java.util.ArrayList;
import java.util.Objects;

public class Info extends AppCompatActivity {

    private static ViewGroup app_info_container, credits_container, contributors_container, translators_container;

    @SuppressLint({"SetTextI18n"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        // Header
        CollapsingToolbarLayout collapsing_toolbar = findViewById(R.id.collapsing_toolbar);
        collapsing_toolbar.setTitle(getResources().getString(R.string.activity_title_info));
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // List of containers
        app_info_container = findViewById(R.id.app_info_section);
        credits_container = findViewById(R.id.credits_section);
        contributors_container = findViewById(R.id.contributors_sections);
        translators_container = findViewById(R.id.translators_section);

        ArrayList<Object[]> app_info = new ArrayList<>();
        ArrayList<Object[]> credits = new ArrayList<>();
        ArrayList<Object[]> contributors = new ArrayList<>();
        ArrayList<Object[]> translators = new ArrayList<>();

        // App info section
        app_info.add(new Object[]{getResources().getString(R.string.info_version_title), BuildConfig.VERSION_NAME, "", R.drawable.ic_info});
        app_info.add(new Object[]{getResources().getString(R.string.info_github_title), getResources().getString(R.string.info_github_desc), "https://github.com/Mahmud0808/Iconify", R.drawable.ic_github});
        app_info.add(new Object[]{getResources().getString(R.string.info_telegram_title), getResources().getString(R.string.info_telegram_desc), "https://t.me/IconifyOfficial", R.drawable.ic_telegram});

        // Credits section
        credits.add(new Object[]{"Icons8.com", getResources().getString(R.string.info_icons8_desc), "https://icons8.com/", R.drawable.ic_link});
        credits.add(new Object[]{"Jai", getResources().getString(R.string.info_shell_desc), "https://t.me/Jai_08", R.drawable.ic_user});
        credits.add(new Object[]{"1perialf", getResources().getString(R.string.info_rro_desc), "https://t.me/Rodolphe06", R.drawable.ic_user});
        credits.add(new Object[]{"Ritesh", getResources().getString(R.string.info_rro_desc), "https://t.me/ModestCat03", R.drawable.ic_user});
        credits.add(new Object[]{"Sanely Insane", getResources().getString(R.string.info_tester_desc), "https://t.me/sanely_insane", R.drawable.ic_user});
        credits.add(new Object[]{"Jaguar", getResources().getString(R.string.info_tester_desc), "https://t.me/Jaguar0066", R.drawable.ic_user});

        // Contributors section
        contributors.add(new Object[]{"Azure-Helper", getResources().getString(R.string.info_contributor_desc), "https://github.com/Azure-Helper", R.drawable.ic_user});
        contributors.add(new Object[]{"HiFIi", getResources().getString(R.string.info_contributor_desc), "https://github.com/HiFIi", R.drawable.ic_user});
        contributors.add(new Object[]{"IzzySoft", getResources().getString(R.string.info_contributor_desc), "https://github.com/IzzySoft", R.drawable.ic_user});

        // Translators section
        translators.add(new Object[]{"Faceless1999", "Persian translation.", "https://github.com/Faceless1999", R.drawable.ic_user});
        translators.add(new Object[]{"B1ays", "Russian translation.", "https://github.com/B1ays", R.drawable.ic_user});

        addItem(app_info, app_info_container);
        addItem(credits, credits_container);
        addItem(contributors, contributors_container);
        addItem(translators, translators_container);

        fixViews();
    }

    // Fix background drawable and remove extra divider
    @SuppressLint("UseCompatLoadingForDrawables")
    private void fixViews() {
        ViewGroup[] viewGroups = {app_info_container, credits_container, contributors_container, translators_container};

        for (ViewGroup viewGroup : viewGroups) {
            if (viewGroup.getChildCount() == 0)
                continue;

            if (viewGroup.getChildCount() == 1) {
                ((LinearLayout) viewGroup.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.container));
                ((LinearLayout) viewGroup.getChildAt(0)).removeViewAt(1);
            } else {
                ((LinearLayout) viewGroup.getChildAt(0)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.container_top));
                ((LinearLayout) viewGroup.getChildAt(viewGroup.getChildCount() - 1)).removeViewAt(1);
                ((LinearLayout) viewGroup.getChildAt(viewGroup.getChildCount() - 1)).getChildAt(0).setBackground(getResources().getDrawable(R.drawable.container_bottom));
            }
        }
    }

    // Function to add new item in list
    private void addItem(ArrayList<Object[]> pack, ViewGroup container) {
        for (int i = 0; i < pack.size(); i++) {
            View list = LayoutInflater.from(this).inflate(R.layout.view_list_info, container, false);

            TextView title = list.findViewById(R.id.title);
            title.setText((String) pack.get(i)[0]);

            TextView desc = list.findViewById(R.id.desc);
            desc.setText((String) pack.get(i)[1]);

            ImageView ic = list.findViewById(R.id.icon);
            ic.setImageResource((int) pack.get(i)[3]);

            int finalI = i;
            if (container != app_info_container || app_info_container.getChildCount() != 0) {
                list.setOnClickListener(v -> {
                    String url = (String) pack.get(finalI)[2];
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                });
            } else {
                list.setOnClickListener(v -> {
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText(getResources().getString(R.string.iconify_clipboard_label_version), getResources().getString(R.string.app_name) + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_name) + ' ' + BuildConfig.VERSION_NAME + "\n" + getResources().getString(R.string.iconify_clipboard_label_version_code) + ' ' + BuildConfig.VERSION_CODE);
                    clipboard.setPrimaryClip(clip);
                    Toast toast = Toast.makeText(Iconify.getAppContext(), getResources().getString(R.string.toast_copied), Toast.LENGTH_SHORT);
                    toast.show();
                });
            }

            container.addView(list);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
