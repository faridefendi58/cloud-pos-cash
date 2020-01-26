package com.slightsite.app.ui.purchase;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.slightsite.app.R;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.ui.sale.AdapterListOrder;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class PurchaseOrderActivity extends AppCompatActivity {

    private List<PurchaseLineItem> purchase_data = new ArrayList<PurchaseLineItem>();
    private RecyclerView purchaseListView;
    private Button btn_proceed;
    private RadioGroup radioTransactionType;
    private Boolean is_purchase_order = true;
    private Boolean is_inventory_issue = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order);

        Intent intent = getIntent();
        if (intent.hasExtra("purchase_data")) {
            String str_purchase_data = intent.getStringExtra("purchase_data");

            Gson gson = new Gson();
            Type type = new TypeToken<List<PurchaseLineItem>>(){}.getType();
            purchase_data = gson.fromJson(str_purchase_data, type);
        }

        initToolbar();
        initView();
        initAction();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_stock_purchase));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(getResources().getString(R.string.title_stock_in_out));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        purchaseListView = (RecyclerView) findViewById(R.id.purchaseListView);
        purchaseListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        purchaseListView.setHasFixedSize(true);
        purchaseListView.setNestedScrollingEnabled(false);

        btn_proceed = (Button) findViewById(R.id.btn_proceed);
        radioTransactionType = (RadioGroup) findViewById(R.id.radioTransactionType);
    }

    private void initAction() {
        AdapterListPurchaseConfirm pAdap = new AdapterListPurchaseConfirm(getBaseContext(), purchase_data);
        pAdap.notifyDataSetChanged();
        purchaseListView.setAdapter(pAdap);

        radioTransactionType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                View radioButton = radioTransactionType.findViewById(checkedId);
                int index = radioTransactionType.indexOfChild(radioButton);
                switch (index) {
                    case 0: // first button
                        btn_proceed.setText(getResources().getString(R.string.button_proceed_stock_in));
                        setIsPurchaseOrder();
                        break;
                    case 1: // secondbutton
                        btn_proceed.setText(getResources().getString(R.string.button_proceed_stock_out));
                        setIsInventoryIssue();
                        break;
                }
            }
        });
    }

    private void setIsPurchaseOrder() {
        this.is_purchase_order = true;
        this.is_inventory_issue = false;
    }

    private void setIsInventoryIssue() {
        this.is_inventory_issue = true;
        this.is_purchase_order = false;
    }

    public void proceedPurchase(View v) {

    }
}
