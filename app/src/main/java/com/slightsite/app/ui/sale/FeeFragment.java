package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.sale.Fee;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class FeeFragment extends UpdatableFragment {

    private Register register;
    private ParamCatalog paramCatalog;

    private MainActivity main;
    private ViewPager viewPager;

    private View root;

    private RecyclerView feeListRecycle;

    private TextView fee_report_title;
    private TextView total_omzet;
    private TextView total_fee;
    private TextView total_transaction;

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

        initView();
        buildListFee();
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

    private void initView() {
        fee_report_title = (TextView) root.findViewById(R.id.fee_report_title);
        total_fee = (TextView) root.findViewById(R.id.total_fee);
        total_omzet = (TextView) root.findViewById(R.id.total_omzet);
        total_transaction = (TextView) root.findViewById(R.id.total_transaction);
    }

    private void buildListFee() {
        feeListRecycle = (RecyclerView) root.findViewById(R.id.feeListRecycle);
        feeListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));
        feeListRecycle.setHasFixedSize(true);
        feeListRecycle.setNestedScrollingEnabled(false);

        // dummy data
        ArrayList<Fee> listFee = new ArrayList<Fee>();
        for (int m = 1; m < 30; m++) {
            int tot = m*1000;
            Fee _fee = new Fee("2019-10-"+m, m*3, Double.parseDouble(tot+""));
            listFee.add(_fee);
        }
        AdapterListFee adapter = new AdapterListFee(main.getApplicationContext(), listFee);
        feeListRecycle.setAdapter(adapter);
    }
}
