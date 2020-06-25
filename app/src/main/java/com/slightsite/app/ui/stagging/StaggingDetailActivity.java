package com.slightsite.app.ui.stagging;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.JsonArray;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.sale.SaleDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class StaggingDetailActivity extends Activity{

    private RecyclerView lineitemListRecycle;
    private List<Map<String, String>> lineitemList;
    private String order_key;
    private TextView tv_order_key;
    private TextView tv_total_order;
    private TextView tv_created_at;
    private TextView tv_status;
    private TextView tv_customer_name;
    private TextView tv_customer_address;
    private TextView tv_customer_phone;
    private TextView tv_shipping_method;
    private TextView tv_shipping_date;
    private LinearLayout complete_button_container;

    private ParamCatalog paramCatalog;
    private ProductCatalog productCatalog;
    private SaleLedger saleLedger;
    private Sale sale;
    private Customer customer;
    private Shipping shipping;

    ProgressDialog pDialog;
    int success;

    private final HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
    private JSONObject server_stagging_data;

    private static final String TAG = StaggingDetailActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private Register register;

    private int FRAGMENT_STAGGING = 4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            productCatalog = Inventory.getInstance().getProductCatalog();
            register = Register.getInstance();
            saleLedger = SaleLedger.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra("order_key")) { // has sale data from server
            order_key = getIntent().getStringExtra("order_key");
        }

        if (getIntent().hasExtra("fragment")) {
            FRAGMENT_STAGGING = Integer.parseInt(getIntent().getStringExtra("fragment"));
        }

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

        String dt = DateTimeStrategy.getCurrentTime();

        initUI(savedInstanceState);
    }


    /**
     * Initiate actionbar.
     */
    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_stagging));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_delete, menu);
        return true;
    }


    /**
     * Initiate this UI.
     * @param savedInstanceState
     */
    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_stagging_detail);

        initiateActionBar();

        tv_order_key = (TextView) findViewById(R.id.tv_order_key);
        tv_total_order = (TextView) findViewById(R.id.tv_total_order);
        tv_created_at = (TextView) findViewById(R.id.tv_created_at);
        tv_status = (TextView) findViewById(R.id.tv_status);
        tv_customer_name = (TextView) findViewById(R.id.tv_customer_name);
        tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
        tv_customer_phone = (TextView) findViewById(R.id.tv_customer_phone);
        tv_shipping_method = (TextView) findViewById(R.id.tv_shipping_method);
        tv_shipping_date = (TextView) findViewById(R.id.tv_shipping_date);

        complete_button_container = (LinearLayout) findViewById(R.id.complete_button_container);

        lineitemListRecycle = (RecyclerView) findViewById(R.id.lineitemListRecycle);
        lineitemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lineitemListRecycle.setHasFixedSize(true);
        lineitemListRecycle.setNestedScrollingEnabled(false);

        try {
            getDetailFromServer();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent act = new Intent(StaggingDetailActivity.this, MainActivity.class);
                act.putExtra("fragment", FRAGMENT_STAGGING);
                finish();
                startActivity(act);
                return true;
            case R.id.nav_delete:
                _remove_order();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Update UI.
     */
    public void update() {

    }

    @Override
    protected void onResume() {
        super.onResume();
        update();
    }

    public void removeOrder(View v) {
        _remove_order();
    }

    private void _remove_order() {
        LayoutInflater inflater2 = this.getLayoutInflater();

        View titleView = inflater2.inflate(R.layout.dialog_custom_title, null);
        ((TextView) titleView.findViewById(R.id.dialog_title)).setText(getResources().getString(R.string.remove_order));
        ((TextView) titleView.findViewById(R.id.dialog_content)).setText(getResources().getString(R.string.dialog_remove_order));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(titleView);
        dialog.setPositiveButton(getResources().getString(R.string.button_remove), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Map<String, Object> mObj = new HashMap<String, Object>();
                    mObj.put("order_key", order_key);
                    _server_remove_order(mObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                Intent newActivity = new Intent(StaggingDetailActivity.this,
                        MainActivity.class);
                startActivity(newActivity);
            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) {
            String qry = URLBuilder.httpBuildQuery(params, "UTF-8");
            url += "&" + qry;
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
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    private void _string_request2(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
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
            e.printStackTrace();
        }
    }

    private void getDetailFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("order_key", order_key);

        String url = Server.URL + "transaction/detail-stagging?api-key=" + Server.API_KEY;
        _string_request2(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            Log.e(getClass().getSimpleName(), "jObj : "+ jObj.toString());
                            // Check for error node in json
                            if (success == 1) {
                                server_stagging_data = jObj.getJSONObject("data");
                                if (server_stagging_data.has("invoice_number") && !server_stagging_data.isNull("invoice_number")) {
                                    tv_order_key.setText(server_stagging_data.getString("invoice_number"));
                                } else {
                                    tv_order_key.setText(server_stagging_data.getString("order_key"));
                                }
                                if (server_stagging_data.has("total") && !server_stagging_data.isNull("total")) {
                                    String tot_order = CurrencyController.getInstance().moneyFormat(server_stagging_data.getDouble("total"));
                                    tv_total_order.setText(tot_order);
                                }
                                tv_created_at.setText(server_stagging_data.getString("created_at"));
                                if (server_stagging_data.getInt("status") > 0) {
                                    tv_status.setText("Finish");
                                } else {
                                    tv_status.setText("Pending");
                                    complete_button_container.setVisibility(View.VISIBLE);
                                }
                                tv_customer_name.setText(server_stagging_data.getString("name"));
                                tv_customer_address.setText(server_stagging_data.getString("address"));
                                tv_customer_phone.setText(server_stagging_data.getString("phone"));
                                if (server_stagging_data.getString("shipping_method").equals("ambil_nanti")) {
                                    tv_shipping_method.setText("Ambil Nanti");
                                } else if (server_stagging_data.getString("shipping_method").equals("gosend")) {
                                    tv_shipping_method.setText("GoSend");
                                }
                                tv_shipping_date.setText(server_stagging_data.getString("created_at"));

                                buildListItems();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void buildListItems() {
        lineitemList = new ArrayList<Map<String, String>>();
        List<LineItem> lineItems = new ArrayList<LineItem>();

        try {
            JSONArray _items = server_stagging_data.getJSONArray("items");
            for (int m = 0; m < _items.length(); m++) {
                JSONObject line_object = null;
                try {
                    line_object = _items.getJSONObject(m);
                    Product p = null;
                    if (line_object.has("barcode")) {
                        p = productCatalog.getProductByBarcode(line_object.getString("barcode"));
                    } else {
                        p = new Product(line_object.getInt("order_item_id"), line_object.getString("order_item_name"), line_object.getString("order_item_id"), line_object.getDouble("order_item_price"));
                    }
                    if (p != null) {
                        LineItem lineItem = new LineItem(
                                p,
                                line_object.getInt("order_item_qty"),
                                line_object.getInt("order_item_qty")
                        );
                        lineItem.setUnitPriceAtSale(line_object.getDouble("order_item_price"));
                        lineItems.add(lineItem);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        AdapterListProductStagging sAdap = new AdapterListProductStagging(StaggingDetailActivity.this, lineItems, register);
        lineitemListRecycle.setAdapter(sAdap);
    }

    public void proceedOrder(View v) {
        LayoutInflater inflater2 = this.getLayoutInflater();

        View titleView = inflater2.inflate(R.layout.dialog_custom_title, null);
        ((TextView) titleView.findViewById(R.id.dialog_title)).setText(getResources().getString(R.string.button_mark_as_complete));
        ((TextView) titleView.findViewById(R.id.dialog_content)).setText(getResources().getString(R.string.dialog_proceed_order));

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setCustomTitle(titleView);
        dialog.setPositiveButton(getResources().getString(R.string.label_proceed_now), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    Map<String, Object> mObj = new HashMap<String, Object>();
                    mObj.put("order_key", order_key);
                    _server_proceed_order(mObj);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        dialog.show();
    }

    private void _server_remove_order(Map mObj) {
        String _url = Server.URL + "transaction/delete-stagging?api-key=" + Server.API_KEY;
        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);

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
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Intent act = new Intent(StaggingDetailActivity.this, MainActivity.class);
                                finish();
                                startActivity(act);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }

    private void _server_proceed_order(Map mObj) {
        String _url = Server.URL + "transaction/proceed-stagging?api-key=" + Server.API_KEY;
        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);

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
                            Log.e(TAG, jObj.toString());
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Intent act = new Intent(StaggingDetailActivity.this, MainActivity.class);
                                if (jObj.has("id")) {
                                    act = new Intent(StaggingDetailActivity.this, SaleDetailActivity.class);

                                    sale = new Sale(jObj.getInt("id"), server_stagging_data.getString("created_at"));
                                    sale.setDeliveredPlanAt(server_stagging_data.getString("created_at"));
                                    sale.setServerInvoiceNumber(jObj.getString("invoice_number"));
                                    sale.setServerInvoiceId(jObj.getInt("id"));
                                    sale.setCustomerId(jObj.getInt("customer_id"));
                                    sale.setStatus("unpaid");

                                    JSONObject config = jObj.getJSONObject("config");
                                    JSONObject cust_dt = config.getJSONObject("customer");
                                    if (cust_dt.has("name")) {
                                        customer = new Customer(
                                                sale.getCustomerId(),
                                                cust_dt.getString("name"),
                                                "-",
                                                cust_dt.getString("phone"),
                                                cust_dt.getString("address"),
                                                1
                                        );
                                    }

                                    //act.putExtra("id", sale.getId());
                                    act.putExtra("sale_intent", sale);
                                    if (customer != null) {
                                        act.putExtra("customer_intent", customer);
                                    }

                                    // build the shipping
                                    JSONArray arrShip = config.getJSONArray("shipping");
                                    if (arrShip.length() > 0) {
                                        JSONObject ship_method = arrShip.getJSONObject(0);
                                        if (ship_method != null) {
                                            shipping = new Shipping(
                                                    ship_method.getInt("method"),
                                                    ship_method.getString("date_added"),
                                                    ship_method.getString("address"),
                                                    ship_method.getInt("warehouse_id")
                                            );
                                            if (ship_method.has("warehouse_name")) {
                                                shipping.setWarehouseName(ship_method.getString("warehouse_name"));
                                            }
                                            if (ship_method.has("recipient_name")) {
                                                shipping.setName(ship_method.getString("recipient_name"));
                                            }
                                            if (ship_method.has("recipient_phone")) {
                                                shipping.setPhone(ship_method.getString("recipient_phone"));
                                            }
                                            if (ship_method.has("pickup_date")) {
                                                shipping.setPickupDate(ship_method.getString("pickup_date"));
                                            }
                                        }
                                    }

                                    if (shipping != null) {
                                        act.putExtra("shipping_intent", shipping);
                                    }

                                    // build the payment
                                    JSONArray arrPayment = config.getJSONArray("payment");
                                    if (arrPayment.length() > 0) {
                                        act.putExtra("payment_intent", arrPayment.toString());
                                    }

                                    // build the line item data
                                    JSONArray arrItemsBelanja = config.getJSONArray("items_belanja");
                                    if (arrItemsBelanja.length() > 0) {
                                        act.putExtra("line_items_intent", arrItemsBelanja.toString());
                                    }
                                }
                                finish();
                                startActivity(act);
                            } else {
                                if (jObj.has(TAG_MESSAGE)) {
                                    Toast.makeText(getApplicationContext(),
                                            jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }
}
