package com.slightsite.app.ui.deposit;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.retur.Retur;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.deposit.AdapterListProductTake;
import com.slightsite.app.ui.printer.PrintPreviewActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class DepositActivity extends AppCompatActivity {

    private static final String TAG = DepositActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    ProgressDialog pDialog;
    int success;
    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;

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
    private Map<Integer, Integer> avail_product_qty_stacks = new HashMap<Integer, Integer>();
    private List<JSONObject> take_history_stacks = new ArrayList<JSONObject>();

    private String line_items_intent;
    private String payment_intent;
    private List<LineItem> lineItems;
    private ProductCatalog productCatalog;

    private Register register;
    private Boolean is_local_data = false;
    private int total_inv_qty = 0;

    private RecyclerView lineitemListRecycle;
    private RecyclerView originalItemListRecycle;
    private RecyclerView takeGoodHistoryRecycle;
    private LinearLayout take_history_container;

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
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            saleLedger = SaleLedger.getInstance();
            register = Register.getInstance();
            productCatalog = Inventory.getInstance().getProductCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra("sale_intent")) { // has sale data from server
            sale = (Sale) getIntent().getSerializableExtra("sale_intent");
            //Log.e(getClass().getSimpleName(), "sale : "+ sale.toMap().toString());
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
                            if (p != null && line_object.getInt("qty") > 0) {
                                LineItem lineItem = new LineItem(
                                        p,
                                        line_object.getInt("qty"),
                                        line_object.getInt("qty")
                                );
                                lineItem.setUnitPriceAtSale(line_object.getDouble("unit_price"));
                                lineItems.add(lineItem);
                                total_inv_qty = total_inv_qty + line_object.getInt("qty");
                                product_qty_stacks.put(Integer.parseInt(p.getBarcode()), line_object.getInt("qty"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    sale.setAllLineItem(lineItems);
                }
            }

            if (getIntent().hasExtra("payment_intent")) {
                payment_intent = getIntent().getStringExtra("payment_intent");
            }

            if (getIntent().hasExtra("shipping_intent")) {
                shipping_intent = (Shipping) getIntent().getSerializableExtra("shipping_intent");
            }

            showOriginalList(sale.getAllLineItem());
        } else {
            saleId = Integer.parseInt(getIntent().getStringExtra("saleId"));
            is_local_data = true;
            try {
                sale = saleLedger.getSaleById(saleId);
                customer = saleLedger.getCustomerBySaleId(saleId);
                showOriginalList(sale.getAllLineItem());
            } catch (Exception e){e.printStackTrace();}
        }

        if (sale != null) {
            for (LineItem line : sale.getAllLineItem()) {
                int p_id = Integer.parseInt(line.getProduct().getBarcode());
                product_price_stacks.put(p_id, line.getPriceAtSale());
                product_name_stacks.put(p_id, line.getProduct().getName());
            }
        }

        getTakeGoodHistory();
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

        originalItemListRecycle = (RecyclerView) findViewById(R.id.originalItemListRecycle);
        originalItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        originalItemListRecycle.setHasFixedSize(true);
        originalItemListRecycle.setNestedScrollingEnabled(false);

        takeGoodHistoryRecycle = (RecyclerView) findViewById(R.id.takeGoodHistoryRecycle);
        takeGoodHistoryRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        takeGoodHistoryRecycle.setHasFixedSize(true);
        takeGoodHistoryRecycle.setNestedScrollingEnabled(false);

        take_good_notes = (EditText) findViewById(R.id.take_good_notes);
        take_history_container = (LinearLayout) findViewById(R.id.take_history_container);
    }

    private void showOriginalList(List<LineItem> list) {
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            Map<String,String> _map = new HashMap<String, String>();
            _map.put("title", line.getProduct().getName());
            _map.put("quantity", line.getQuantity()+"");
            lineitemList.add(_map);
        }

        AdapterListSimple sAdap = new AdapterListSimple(DepositActivity.this, lineitemList);
        originalItemListRecycle.setAdapter(sAdap);
    }

    public void updateProductTakeStacks(int product_id, int qty) {
        if (qty > 0) {
            product_take_stacks.put(product_id, qty);
        } else {
            product_take_stacks.remove(product_id);
        }
    }

    private Deposit deposit;

    public void proceedTakeGood(View v) {
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
                        mObj.put("invoice_id", sale.getServerInvoiceId()+"");

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

                        mObj.put("items_available", avail_product_qty_stacks);

                        deposit = new Deposit(sale.getServerInvoiceId());
                        deposit.setCustomer(customer);
                        if (mObj.containsKey("notes")) {
                            deposit.setNotes(mObj.get("notes").toString());
                        }
                        deposit.setItems(arrRefundList);
                        deposit.setAvailableQty(avail_product_qty_stacks);

                        createTakeGoodHistory(mObj);
                        Log.e(getClass().getSimpleName(), "mObj : "+ mObj.toString());
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
            Toast.makeText(getBaseContext(),
                        getResources().getString(R.string.error_empty_take_good), Toast.LENGTH_LONG)
                        .show();
        }
    }

    private void getTakeGoodHistory() {
        Map<String, Object> mObj = new HashMap<String, Object>();
        mObj.put("invoice_id", sale.getServerInvoiceId());
        String _url = Server.URL + "transaction/list-deposit-take?api-key=" + Server.API_KEY;

        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);

        _string_request(
                Request.Method.GET,
                _url,
                params,
                false,
                new VolleyCallback(){
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            Log.e(TAG, "getTakeGoodHistory : "+ jObj.toString());
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            List<LineItem> list = sale.getAllLineItem();
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                if (jObj.has("data")) {
                                    for (int m = 0; m < data.length(); m++) {
                                        JSONObject data_m = data.getJSONObject(m);
                                        take_history_stacks.add(data_m);
                                        JSONArray items = data_m.getJSONArray("items");
                                        for (int n = 0; n < items.length(); n++) {
                                            JSONObject item_data = items.getJSONObject(n);
                                            if (item_data.has("quantity_before") && item_data.has("quantity") && item_data.has("product_id")) {
                                                int avail = item_data.getInt("quantity_before") - item_data.getInt("quantity");
                                                if ((avail_product_qty_stacks != null) && avail_product_qty_stacks.containsKey(item_data.getInt("product_id"))) {
                                                    avail = avail_product_qty_stacks.get(item_data.getInt("product_id")) - item_data.getInt("quantity");
                                                }
                                                avail_product_qty_stacks.put(item_data.getInt("product_id"), avail);
                                                product_qty_stacks.put(item_data.getInt("product_id"), avail);
                                            }
                                        }
                                    }

                                    for (int p = 0; p < list.size(); p++) {
                                        LineItem line = list.get(p);
                                        int bc = Integer.parseInt(line.getProduct().getBarcode());
                                        if (avail_product_qty_stacks.containsKey(bc)) {
                                            line.setQuantity(avail_product_qty_stacks.get(bc));
                                        }
                                        list.set(p, line);
                                    }
                                } else { //never take item
                                    for (int p = 0; p < list.size(); p++) {
                                        LineItem line = list.get(p);
                                        int bc = Integer.parseInt(line.getProduct().getBarcode());
                                        avail_product_qty_stacks.put(bc, line.getQuantity());
                                        product_qty_stacks.put(bc, line.getQuantity());
                                        list.set(p, line);
                                    }
                                }

                                if (take_history_stacks.size() > 0) {
                                    AdapterListTakeGood hAdap = new AdapterListTakeGood(DepositActivity.this, take_history_stacks);
                                    takeGoodHistoryRecycle.setAdapter(hAdap);
                                    take_history_container.setVisibility(View.VISIBLE);
                                }
                            } else {
                                for (int p = 0; p < list.size(); p++) {
                                    LineItem line = list.get(p);
                                    int bc = Integer.parseInt(line.getProduct().getBarcode());
                                    avail_product_qty_stacks.put(bc, line.getQuantity());
                                    list.set(p, line);
                                }
                            }

                            AdapterListProductTake sAdap = new AdapterListProductTake(DepositActivity.this, list, register);
                            lineitemListRecycle.setAdapter(sAdap);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void createTakeGoodHistory(Map<String, Object> _mObj) {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }
        params.put("admin_id", admin_id);
        if (_mObj.containsKey("notes")) {
            params.put("notes", _mObj.get("notes").toString());
            _mObj.remove("notes");
        }
        if (_mObj.containsKey("invoice_id")) {
            params.put("invoice_id", _mObj.get("invoice_id").toString());
            _mObj.remove("invoice_id");
        }

        String _url = Server.URL + "transaction/create-deposit-take?api-key=" + Server.API_KEY;

        String qry = URLBuilder.httpBuildQuery(_mObj, "UTF-8");
        _url += "&"+ qry;
        Log.e(TAG, "params : "+ params.toString());

        _string_request(
                Request.Method.POST,
                _url,
                params,
                true,
                new VolleyCallback(){
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            Log.e(TAG, "createTakeGoodHistory : "+ jObj.toString());
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                                Boolean is_complete = false;
                                if (jObj.has("available_qty")) {
                                    if (jObj.getInt("available_qty") == 0) {
                                        is_complete = true;
                                    }
                                }

                                if (is_complete) {
                                    // go to normal print preview
                                    Intent newActivity = new Intent(DepositActivity.this, PrintPreviewActivity.class);
                                    Sale new_sale = new Sale(saleId, sale.getEndTime());
                                    new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
                                    new_sale.setServerInvoiceId(sale.getServerInvoiceId());
                                    new_sale.setCustomerId(sale.getCustomerId());
                                    new_sale.setStatus(sale.getStatus());
                                    new_sale.setDiscount(sale.getDiscount());
                                    new_sale.setDeliveredAt(sale.getDeliveredAt());
                                    new_sale.setDeliveredByName(sale.getDeliveredByName());

                                    newActivity.putExtra("sale_intent", new_sale);
                                    newActivity.putExtra("customer_intent", customer_intent);
                                    newActivity.putExtra("shipping_intent", shipping_intent);
                                    newActivity.putExtra("payment_intent", payment_intent);
                                    newActivity.putExtra("line_items_intent", line_items_intent);
                                    newActivity.putExtra("deposit_order", true);
                                    finish();
                                    startActivity(newActivity);
                                } else {
                                    Intent newActivity = new Intent(DepositActivity.this, PrintDepositActivity.class);
                                    newActivity.putExtra("deposit_intent", deposit);
                                    newActivity.putExtra("just_print", true);
                                    finish();
                                    startActivity(newActivity);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        hideDialog();
                    }
                });
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void showDialog() {
        if (pDialog != null && !pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        try {
            AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
        } catch (Exception e) {
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }
}
