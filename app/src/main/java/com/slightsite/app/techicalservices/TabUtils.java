package com.slightsite.app.techicalservices;

import android.app.ActionBar;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.slightsite.app.R;

import org.w3c.dom.Text;

public class TabUtils {
    public static View renderTabView(Context context, int titleResource, int backgroundResource, int badgeNumber) {
        FrameLayout view = (FrameLayout) LayoutInflater.from(context).inflate(R.layout.tab_badge, null);
        // We need to manually set the LayoutParams here because we don't have a view root
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        TextView tab_text = (TextView) view.findViewById(R.id.tab_text);
        //tab_text.setText(titleResource);
        tab_text.setVisibility(View.INVISIBLE);
        //((TextView) view.findViewById(R.id.tab_text)).setBackgroundResource(backgroundResource);
        ((ImageView) view.findViewById(R.id.tab_icon)).setImageDrawable(context.getDrawable(backgroundResource));
        updateTabBadge((TextView) view.findViewById(R.id.tab_badge), badgeNumber);
        return view;
    }

    public static void updateTabBadge(ActionBar.Tab tab, int badgeNumber) {
        updateTabBadge((TextView) tab.getCustomView().findViewById(R.id.tab_badge), badgeNumber);
    }

    private static void updateTabBadge(TextView view, int badgeNumber) {
        if (badgeNumber > 0) {
            view.setVisibility(View.VISIBLE);
            view.setText(Integer.toString(badgeNumber));
        }
        else {
            view.setVisibility(View.GONE);
        }
    }
}
