package com.slightsite.app.ui.retur;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.SimpleAdapter;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.sale.AdapterListOrder;

import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public class ReturActivity extends AppCompatActivity {

    private static final String TAG = ReturActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private Sale sale;
    private int saleId;
    private SaleLedger saleLedger;
    private Customer customer;
    private Sale sale_intent;
    private Customer customer_intent;
    private Shipping shipping_intent;
    private List<Map<String, String>> lineitemList = new ArrayList<Map<String, String>>();

    private SharedPreferences sharedpreferences;
    private Register register;
    private Boolean is_local_data = false;

    private RecyclerView lineitemListRecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retur);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        initView();

        try {
            saleLedger = SaleLedger.getInstance();
            /*paramCatalog = ParamService.getInstance().getParamCatalog();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            register = Register.getInstance();
            productCatalog = Inventory.getInstance().getProductCatalog();*/
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra("sale_intent")) { // has sale data from server
            sale = (Sale) getIntent().getSerializableExtra("sale_intent");
            sale_intent = sale;
            saleId = sale.getId();
            // check if any data in local db
            try {
                Sale local_sale = saleLedger.getSaleByServerInvoiceId(saleId);
                if (local_sale != null) {
                    sale = local_sale;
                    saleId = sale.getId();
                    is_local_data = true;
                }
            } catch (Exception e){e.printStackTrace();}

            if (getIntent().hasExtra("customer_intent")) {
                customer = (Customer) getIntent().getSerializableExtra("customer_intent");
                customer_intent = customer;
            }
        } else {
            saleId = Integer.parseInt(getIntent().getStringExtra("saleId"));
            is_local_data = true;
            try {
                sale = saleLedger.getSaleById(saleId);
                customer = saleLedger.getCustomerBySaleId(saleId);
                showList(sale.getAllLineItem());
            } catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_share, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        lineitemListRecycle = (RecyclerView) findViewById(R.id.lineitemListRecycle);
        lineitemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lineitemListRecycle.setHasFixedSize(true);
        lineitemListRecycle.setNestedScrollingEnabled(false);
    }

    private void showList(List<LineItem> list) {
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            lineitemList.add(line.toMap());
        }

        //AdapterListOrder sAdap = new AdapterListOrder(ReturActivity.this, list, register, totalBox);
        //lineitemListRecycle.setAdapter(sAdap);
    }
}
