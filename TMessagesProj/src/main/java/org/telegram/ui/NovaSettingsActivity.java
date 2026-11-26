package org.telegram.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;
import org.telegram.ui.Components.SeekBarView;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.utils.glass.NovaGlassSettings;
import org.telegram.utils.ghost.NovaGhostMode;
import org.telegram.utils.glass.NovaUiStyle;

import java.util.ArrayList;

/** NOVA settings. Theme section switches real Telegram themes; the demo switches stay demo-only. */
public class NovaSettingsActivity extends UniversalFragment {
    private static final String NOVA_LIGHT = "NOVA Light";
    private static final String NOVA_DARK = "NOVA Dark";
    private static final String PREF_SYSTEM_THEME = "nova_theme_system";

    private static final int ID_NOVA_LIGHT = 10;
    private static final int ID_NOVA_DARK = 11;
    private static final int ID_NOVA_SYSTEM = 12;
    private static final int ID_CUSTOM_THEMES = 13;
    private static final int ID_ALL_THEMES = 14;
    private static final int ID_CURRENT_THEME = 15;
    private static final int ID_GLASS_ENABLED = 20;
    private static final int ID_STYLE_CLASSIC = 30;
    private static final int ID_STYLE_GLASS = 31;
    private static final int ID_GHOST = 40;

    private boolean demoAnimations = true;
    private boolean demoExperimental = false;

    // Views are created once and reused, so the list can be refilled without losing the slider position.
    private NovaGlassPreviewView glassPreview;
    private SeekBarView glassSeekBar;
    private TextView glassIntensityText;
    private LinearLayout glassSliderView;

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
        createGlassViews(context);
        fragmentView = super.createView(context);
        listView.setSections();
        listView.setSectionsDrawBackground(true);
        return fragmentView;
    }

    @Override
    protected RecyclerListView getListViewForSimpleGlass() {
        return listView;
    }

    @Override
    protected CharSequence getTitle() {
        return LocaleController.getString(R.string.NovaSettings);
    }

    private static SharedPreferences prefs() {
        return MessagesController.getGlobalMainSettings();
    }

    private static boolean isSystemThemeMode() {
        return prefs().getBoolean(PREF_SYSTEM_THEME, false);
    }

    private static boolean systemIsNight() {
        Configuration configuration = ApplicationLoader.applicationContext.getResources().getConfiguration();
        return (configuration.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    }

    private static String currentThemeName() {
        Theme.ThemeInfo current = Theme.getCurrentTheme();
        return current != null && current.name != null ? current.name : "";
    }

    /** Applies a NOVA theme as the day theme, the same way the Telegram theme list does. */
    private void applyNovaTheme(String name, boolean system) {
        Theme.ThemeInfo info = Theme.getTheme(name);
        if (info == null) {
            return;
        }
        prefs().edit().putBoolean(PREF_SYSTEM_THEME, system).apply();
        Theme.applyTheme(info);
        if (listView != null && listView.adapter != null) {
            listView.adapter.update(true);
        }
    }

    private void createGlassViews(Context context) {
        glassPreview = new NovaGlassPreviewView(context);

        glassIntensityText = new TextView(context);
        glassIntensityText.setTextSize(16);
        glassIntensityText.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        glassIntensityText.setPadding(AndroidUtilities.dp(21), AndroidUtilities.dp(10), AndroidUtilities.dp(21), 0);

        glassSeekBar = new SeekBarView(context);
        glassSeekBar.setReportChanges(true);
        glassSeekBar.setProgress(NovaGlassSettings.getIntensity() / (float) NovaGlassSettings.MAX_INTENSITY, false);
        glassSeekBar.setDelegate(new SeekBarView.SeekBarViewDelegate() {
            @Override
            public void onSeekBarDrag(boolean stop, float progress) {
                NovaGlassSettings.setIntensity(Math.round(progress * NovaGlassSettings.MAX_INTENSITY));
                updateGlassViews();
            }
        });

        glassSliderView = new LinearLayout(context);
        glassSliderView.setOrientation(LinearLayout.VERTICAL);
        glassSliderView.addView(glassIntensityText, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT));
        glassSliderView.addView(glassSeekBar, LayoutHelper.createLinear(LayoutHelper.MATCH_PARENT, AndroidUtilities.dp(40)));

        updateGlassViews();
    }

    /** Keeps the slider label and the preview in sync with the stored glass settings. */
    private void updateGlassViews() {
        final int value = NovaGlassSettings.getIntensity();
        if (glassIntensityText != null) {
            glassIntensityText.setText(LocaleController.getString(R.string.NovaGlassIntensity) + ": " + value + "%");
        }
        if (glassPreview != null) {
            glassPreview.setState(NovaGlassSettings.isEnabled(), value);
        }
    }

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        boolean system = isSystemThemeMode();
        String current = currentThemeName();

        final boolean glassStyle = NovaUiStyle.isGlass();
        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaUiStyle)));
        items.add(UItem.asRadio(ID_STYLE_CLASSIC, LocaleController.getString(R.string.NovaUiStyleClassic)).setChecked(!glassStyle));
        items.add(UItem.asRadio(ID_STYLE_GLASS, LocaleController.getString(R.string.NovaUiStyleGlass)).setChecked(glassStyle));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaUiStyleInfo)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaThemes)));
        items.add(UItem.asButton(ID_CURRENT_THEME, LocaleController.getString(R.string.NovaThemeCurrent), current));
        items.add(UItem.asRadio(ID_NOVA_LIGHT, LocaleController.getString(R.string.NovaThemeLight)).setChecked(!system && NOVA_LIGHT.equals(current)));
        items.add(UItem.asRadio(ID_NOVA_DARK, LocaleController.getString(R.string.NovaThemeDark)).setChecked(!system && NOVA_DARK.equals(current)));
        items.add(UItem.asRadio(ID_NOVA_SYSTEM, LocaleController.getString(R.string.NovaThemeSystem)).setChecked(system));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaThemeSystemInfo)));
        items.add(UItem.asButton(ID_CUSTOM_THEMES, LocaleController.getString(R.string.NovaThemeCustom)));
        items.add(UItem.asButton(ID_ALL_THEMES, LocaleController.getString(R.string.NovaThemeAllTelegram)));
        items.add(UItem.asShadow(null));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaGlass)));
        items.add(UItem.asCheck(ID_GLASS_ENABLED, LocaleController.getString(R.string.NovaGlassEnabled)).setChecked(NovaGlassSettings.isEnabled()));
        items.add(UItem.asCustom(glassSliderView, 72));
        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaGlassPreview)));
        items.add(UItem.asCustom(glassPreview, 150));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaGlassInfo)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaGhost)));
        items.add(UItem.asCheck(ID_GHOST, LocaleController.getString(R.string.NovaGhostEnabled)).setChecked(NovaGhostMode.isEnabled()));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaGhostInfo)));

        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaPreview)));
        items.add(UItem.asCheck(1, LocaleController.getString(R.string.NovaDemoAnimations)).setChecked(demoAnimations));
        items.add(UItem.asCheck(2, LocaleController.getString(R.string.NovaDemoExperimental)).setChecked(demoExperimental));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaDemoHint)));
        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaAbout)));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaAboutText)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        switch (item.id) {
            case ID_NOVA_LIGHT:
                applyNovaTheme(NOVA_LIGHT, false);
                break;
            case ID_NOVA_DARK:
                applyNovaTheme(NOVA_DARK, false);
                break;
            case ID_NOVA_SYSTEM:
                applyNovaTheme(systemIsNight() ? NOVA_DARK : NOVA_LIGHT, true);
                break;
            case ID_CURRENT_THEME:
            case ID_ALL_THEMES:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_BASIC));
                break;
            case ID_CUSTOM_THEMES:
                presentFragment(new ThemeActivity(ThemeActivity.THEME_TYPE_OTHER));
                break;
            case ID_GLASS_ENABLED:
                NovaGlassSettings.setEnabled(!NovaGlassSettings.isEnabled());
                updateGlassViews();
                listView.adapter.update(true);
                break;
            case ID_GHOST:
                NovaGhostMode.setEnabled(!NovaGhostMode.isEnabled());
                listView.adapter.update(true);
                break;
            case ID_STYLE_CLASSIC:
            case ID_STYLE_GLASS:
                NovaUiStyle.set(item.id == ID_STYLE_GLASS ? NovaUiStyle.GLASS : NovaUiStyle.CLASSIC);
                listView.adapter.update(true);
                listView.invalidate();
                break;
            case 1:
                demoAnimations = !demoAnimations;
                listView.adapter.update(true);
                break;
            case 2:
                demoExperimental = !demoExperimental;
                listView.adapter.update(true);
                break;
            default:
                break;
        }
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
