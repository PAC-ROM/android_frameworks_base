package com.android.systemui.pac.appcirclesidebar;

import android.view.View;
import android.widget.AbsListView;

public abstract class ViewModifier {
    abstract void applyToView(View v, AbsListView parent);
}
