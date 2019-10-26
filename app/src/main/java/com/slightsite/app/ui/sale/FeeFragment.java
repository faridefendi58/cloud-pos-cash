package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.slightsite.app.R;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

@SuppressLint("ValidFragment")
public class FeeFragment extends UpdatableFragment {

    private Register register;
    private ParamCatalog paramCatalog;

    private MainActivity main;
    private ViewPager viewPager;

    private View root;

    public FeeFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
            paramCatalog = ParamService.getInstance().getParamCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        root = inflater.inflate(R.layout.fragment_fee, container, false);
        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        return root;
    }

    @Override
    public void update() {
        buildListFee();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    private void buildListFee() {

    }
}
