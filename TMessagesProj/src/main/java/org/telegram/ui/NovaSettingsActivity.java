package org.telegram.ui;

import android.content.Context;
import android.view.View;

import org.telegram.messenger.R;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;
import org.telegram.ui.Components.UniversalFragment;
import org.telegram.ui.Components.RecyclerListView;

import java.util.ArrayList;

/** NOVA's first settings preview. The switches are intentionally demo-only. */
public class NovaSettingsActivity extends UniversalFragment {
    private boolean demoAnimations = true;
    private boolean demoExperimental = false;

    @Override
    protected boolean useSimpleActionBar() {
        return true;
    }

    @Override
    public View createView(Context context) {
        setHasOwnBackground(true);
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

    @Override
    protected void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaPreview)));
        items.add(UItem.asCheck(1, LocaleController.getString(R.string.NovaDemoAnimations)).setChecked(demoAnimations));
        items.add(UItem.asCheck(2, LocaleController.getString(R.string.NovaDemoExperimental)).setChecked(demoExperimental));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaDemoHint)));
        items.add(UItem.asHeader(LocaleController.getString(R.string.NovaAbout)));
        items.add(UItem.asShadow(LocaleController.getString(R.string.NovaAboutText)));
    }

    @Override
    protected void onClick(UItem item, View view, int position, float x, float y) {
        if (item.id == 1) {
            demoAnimations = !demoAnimations;
        } else if (item.id == 2) {
            demoExperimental = !demoExperimental;
        } else {
            return;
        }
        listView.adapter.update(true);
    }

    @Override
    protected boolean onLongClick(UItem item, View view, int position, float x, float y) {
        return false;
    }
}
