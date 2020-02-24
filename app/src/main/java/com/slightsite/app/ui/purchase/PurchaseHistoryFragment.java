package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

@SuppressLint("ValidFragment")
public class PurchaseHistoryFragment extends UpdatableFragment {

    private MainActivity main;
    private ViewPager viewPager;
    private View fragment_view;
    private Resources res;

    public PurchaseHistoryFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_purchase_history, container, false);
        setHasOptionsMenu(true);

        fragment_view = view;

        res = getResources();

        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        //initUI();

        return view;
    }

    @Override
    public void update() {
        search();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void search() {

    }
}
