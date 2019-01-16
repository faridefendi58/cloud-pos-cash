package com.slightsite.app.ui.sale;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slightsite.app.R;

public class ShippingFragment extends Fragment {

    public ShippingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shipping, container, false);

        return root;
    }
}
