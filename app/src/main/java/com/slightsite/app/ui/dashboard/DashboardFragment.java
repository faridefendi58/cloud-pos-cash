package com.slightsite.app.ui.dashboard;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Calendar;
import java.util.List;

import com.slightsite.app.R;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.techicalservices.NoDaoSetException;

public class DashboardFragment extends Fragment {

    private TextView txt_store_name;
    private TextView txt_income_today;
    private TextView txt_income_yesterday;
    private TextView txt_income_this_month;
    private TextView txt_income_last_month;
    private TextView txt_transaction_today;
    private TextView txt_transaction_yesterday;
    private TextView txt_transaction_this_month;
    private TextView txt_transaction_last_month;

    private ParamCatalog paramCatalog;
    private Sale sale;
    private SaleLedger saleLedger;
    private Calendar currentTime;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, null);

        txt_store_name = (TextView) view.findViewById(R.id.txt_store_name);
        txt_income_today = (TextView) view.findViewById(R.id.txt_income_today);
        txt_income_yesterday = (TextView) view.findViewById(R.id.txt_income_yesterday);
        txt_income_this_month = (TextView) view.findViewById(R.id.txt_income_this_month);
        txt_income_last_month = (TextView) view.findViewById(R.id.txt_income_last_month);

        txt_transaction_today = (TextView) view.findViewById(R.id.txt_transaction_today);
        txt_transaction_yesterday = (TextView) view.findViewById(R.id.txt_transaction_yesterday);
        txt_transaction_this_month = (TextView) view.findViewById(R.id.txt_transaction_this_month);
        txt_transaction_last_month = (TextView) view.findViewById(R.id.txt_transaction_last_month);

        initUi();

        return view;
    }

    private void initUi() {
        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            /*if (paramCatalog.getParamByName("store_name") != null) {
                actionBar.setTitle(paramCatalog.getParamByName("store_name").getValue());
                actionBar.setIcon(getResources().getDrawable(R.drawable.ic_launcher));
                //getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }*/
            saleLedger = SaleLedger.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        txt_store_name.setText(paramCatalog.getParamByName("store_name").getValue());


        int today_income = saleLedger.getTotalIncome("today");
        int yesterday_income = saleLedger.getTotalIncome("yesterday");
        int this_month_income = saleLedger.getTotalIncome("this_month");
        int last_month_income = saleLedger.getTotalIncome("last_month");

        txt_income_today.setText(""+ today_income);
        txt_income_yesterday.setText(""+ yesterday_income);
        txt_income_this_month.setText(""+ this_month_income);
        txt_income_last_month.setText(""+ last_month_income);

        int today_transaction = saleLedger.getTotalTransaction("today");
        int yesterday_transaction = saleLedger.getTotalTransaction("yesterday");
        int this_month_transaction = saleLedger.getTotalTransaction("this_month");
        int last_month_transaction = saleLedger.getTotalTransaction("last_month");

        txt_transaction_today.setText(""+ today_transaction);
        txt_transaction_yesterday.setText(""+ yesterday_transaction);
        txt_transaction_this_month.setText(""+ this_month_transaction);
        txt_transaction_last_month.setText(""+ last_month_transaction);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
