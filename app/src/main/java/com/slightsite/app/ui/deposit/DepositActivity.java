package com.slightsite.app.ui.deposit;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.retur.Retur;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.deposit.AdapterListProductTake;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepositActivity extends AppCompatActivity {

    private static final String TAG = DepositActivity.class.getSimpleName();
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
    private Map<Integer, Integer> product_qty_stacks = new HashMap<Integer, Integer>();
    private Map<Integer, Double> product_price_stacks = new HashMap<Integer, Double>();
    private Map<Integer, Integer> product_take_stacks = new HashMap<Integer, Integer>();
    private Map<Integer, String> product_name_stacks = new HashMap<Integer, String>();
    private ArrayList<String> take_good_reason = new ArrayList<String>();

    private String line_items_intent;
    private List<LineItem> lineItems;
    private ProductCatalog productCatalog;

    private SharedPreferences sharedpreferences;
    private Register register;
    private Boolean is_local_data = false;
    private int total_inv_qty = 0;

    private RecyclerView lineitemListRecycle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deposit);

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
            register = Register.getInstance();
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra("sale_intent")) { // has sale data from server
            sale = (Sale) getIntent().getSerializableExtra("sale_intent");
            Log.e(getClass().getSimpleName(), "sale : "+ sale.toMap().toString());
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

            if (getIntent().hasExtra("line_items_intent")) { // has line item data from server
                line_items_intent = getIntent().getStringExtra("line_items_intent");
                JSONArray arrLineItems = null;
                try {
                    arrLineItems = new JSONArray(getIntent().getStringExtra("line_items_intent"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (arrLineItems != null) {
                    lineItems = new ArrayList<LineItem>();
                    for (int m = 0; m < arrLineItems.length(); m++) {
                        JSONObject line_object = null;
                        try {
                            line_object = arrLineItems.getJSONObject(m);
                            Product p = productCatalog.getProductByBarcode(line_object.getString("barcode"));
                            if (p != null) {
                                LineItem lineItem = new LineItem(
                                        p,
                                        line_object.getInt("qty"),
                                        line_object.getInt("qty")
                                );
                                lineItem.setUnitPriceAtSale(line_object.getDouble("unit_price"));
                                lineItems.add(lineItem);
                                total_inv_qty = total_inv_qty + line_object.getInt("qty");
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    sale.setAllLineItem(lineItems);
                }
            }

            showList(sale.getAllLineItem());
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

    private EditText take_good_notes;

    private void initView() {
        lineitemListRecycle = (RecyclerView) findViewById(R.id.lineitemListRecycle);
        lineitemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lineitemListRecycle.setHasFixedSize(true);
        lineitemListRecycle.setNestedScrollingEnabled(false);

        take_good_notes = (EditText) findViewById(R.id.take_good_notes);
    }

    private void showList(List<LineItem> list) {
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            product_qty_stacks.put(line.getProduct().getId(), line.getQuantity());
            product_price_stacks.put(line.getProduct().getId(), line.getPriceAtSale());
            product_name_stacks.put(line.getProduct().getId(), line.getProduct().getName());
            lineitemList.add(line.toMap());
        }

        AdapterListProductTake sAdap = new AdapterListProductTake(DepositActivity.this, list, register);
        lineitemListRecycle.setAdapter(sAdap);
    }

    public void updateProductTakeStacks(int product_id, int qty) {
        if (qty > 0) {
            product_take_stacks.put(product_id, qty);
        } else {
            product_take_stacks.remove(product_id);
        }
        Log.e(getClass().getSimpleName(), "product_take_stacks : "+ product_take_stacks.toString());
    }

    public void proceedTakeGood(View v) {
        // check the reason first
        Boolean has_take_good_reason = hasReturReason();
        Log.e(getClass().getSimpleName(), "has_take_good_reason : "+ has_take_good_reason);
        if (product_take_stacks.size() > 0) {
            LayoutInflater inflater2 = getLayoutInflater();

            View titleView = inflater2.inflate(R.layout.dialog_custom_title, null);
            ((TextView) titleView.findViewById(R.id.dialog_title)).setText(getResources().getString(R.string.button_proceed_take_good));
            ((TextView) titleView.findViewById(R.id.dialog_content)).setText(getResources().getString(R.string.dialog_proceed_take_good));

            AlertDialog.Builder dialog = new AlertDialog.Builder(DepositActivity.this);
            dialog.setCustomTitle(titleView);
            dialog.setPositiveButton(getResources().getString(R.string.label_proceed_now), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        Map<String, Object> mObj = new HashMap<String, Object>();
                        mObj.put("invoice_id", sale.getServerInvoiceId());

                        // the items data include id, price, qty, and how much item to be changed
                        ArrayList arrRefundList = new ArrayList();
                        for (Map.Entry<Integer, Integer> entry : product_take_stacks.entrySet()) {
                            Map<String, String> arrRefundList2 = new HashMap<String, String>();
                            arrRefundList2.put("title", product_name_stacks.get(entry.getKey()));
                            arrRefundList2.put("product_id", entry.getKey()+"");
                            arrRefundList2.put("quantity", entry.getValue()+"");
                            arrRefundList2.put("quantity_before", product_qty_stacks.get(entry.getKey()) +"");
                            arrRefundList2.put("price", product_price_stacks.get(entry.getKey())+"");

                            arrRefundList.add(arrRefundList2);
                        }

                        mObj.put("items", arrRefundList);

                        if (take_good_notes.getText().toString().length() > 0) {
                            mObj.put("notes", take_good_notes.getText().toString());
                        }

                        Log.e(getClass().getSimpleName(), "mObj : "+ mObj.toString());

                /*Intent newActivity = new Intent(DepositActivity.this,
                        PrintDepositActivity.class);
                newActivity.putExtra("retur_intent", retur);
                finish();
                startActivity(newActivity);*/

                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            });

            dialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) { }
            });

            dialog.show();
        } else {
            // no item to be retured
            if (!has_take_good_reason) {
                /*Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.error_empty_retur_reason), Toast.LENGTH_LONG)
                        .show();*/
            }
        }
    }

    private Boolean hasReturReason() {
        take_good_reason.clear();

        if (take_good_reason.size() > 0) {
            return true;
        } else {
            if (take_good_notes.getText().toString().length() > 0) {
                return true;
            }
        }

        return false;
    }
}
