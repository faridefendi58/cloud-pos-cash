package com.slightsite.app.ui.purchase;

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
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PurchaseOrderActivity extends AppCompatActivity {

    private List<PurchaseLineItem> purchase_data = new ArrayList<PurchaseLineItem>();
    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;
    private WarehouseCatalog warehouseCatalog;
    private int warehouse_id;
    private String admin_id;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseOrderActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private RecyclerView purchaseListView;
    private Button btn_proceed;
    private Boolean is_stock_in = true;
    private Boolean is_stock_out = false;
    private EditText purchase_notes;
    private TextView heading_stock_in_out_items;
    private EditText wh_options;
    private List<Warehouses> warehousesList;
    private List<Warehouses> warehousesListIn = new ArrayList<Warehouses>();
    private List<Warehouses> warehousesListOut = new ArrayList<Warehouses>();
    private JSONObject non_transaction_type_list = new JSONObject();
    private int selected_wh_id = -1;
    private String selected_wh_name;
    private int selected_non_transaction_id = -1;
    private String selected_non_transaction_name;
    private int supplier_id = -1;
    private String supplier_name;
    private String non_transaction_type;
    private Boolean is_virtual_staff = false;
    private Vibrator vibe;
    private Map<Integer, Double> unit_prices = new HashMap<Integer, Double>();

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

        if (intent.hasExtra("is_stock_out")) {
            setIsStockOut();
        }

        try {
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            warehousesList = warehouseCatalog.getAllWarehouses();
            String role = paramCatalog.getParamByName("role").getValue();
            if (role != null) {
                if (role.equals("virtualstaff")) {
                    is_virtual_staff = true;
                }
            }
            getAvailableWH();
            if (is_virtual_staff && is_stock_in) {
                getAvailableSupplier();
            }
            getTransactionTypes();
            vibe = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);
        } catch (Exception e){e.printStackTrace();}

        initToolbar();
        initView();
        initAction();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        if (is_stock_in) {
            getSupportActionBar().setTitle(getResources().getString(R.string.title_stock_in));
        } else {
            getSupportActionBar().setTitle(getResources().getString(R.string.title_stock_out));
        }
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        if (is_stock_in) {
            toolbar_title.setText(getResources().getString(R.string.title_stock_in));
        } else {
            toolbar_title.setText(getResources().getString(R.string.title_stock_out));
        }
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

        purchase_notes = (EditText) findViewById(R.id.purchase_notes);
        heading_stock_in_out_items = (TextView) findViewById(R.id.heading_stock_in_out_items);
        wh_options = (EditText) findViewById(R.id.wh_options);

        btn_proceed = (Button) findViewById(R.id.btn_proceed);
        if (is_stock_in) {
            heading_stock_in_out_items.setText(getResources().getString(R.string.heading_stock_in_items));
            btn_proceed.setText(getResources().getString(R.string.button_proceed_stock_in));
        } else {
            heading_stock_in_out_items.setText(getResources().getString(R.string.heading_stock_out_items));
            btn_proceed.setText(getResources().getString(R.string.button_proceed_stock_out));
        }
    }

    private void initAction() {
        buildListPurchase(false);

        if (is_stock_in) {
            wh_options.setText(getResources().getString(R.string.origin));
        } else if (is_stock_out) {
            wh_options.setText(getResources().getString(R.string.destination));
        }

        wh_options.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //showOutletOptionsDialog(v);
                showStockOptions(wh_options);
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    private void buildListPurchase(Boolean show_price) {
        AdapterListPurchaseConfirm pAdap = new AdapterListPurchaseConfirm(PurchaseOrderActivity.this, purchase_data);
        if (show_price && is_stock_in && is_virtual_staff) {
            pAdap.showPrice();
        }
        pAdap.notifyDataSetChanged();
        purchaseListView.setAdapter(pAdap);
        if (!show_price) {
            unit_prices.clear();
        }
    }

    private void setIsStockIn() {
        this.is_stock_in = true;
        this.is_stock_out = false;
    }

    private void setIsStockOut() {
        this.is_stock_out = true;
        this.is_stock_in = false;
    }

    public void updatePurchaseData(int i, String attr, String val) {
        try {
            if (attr == "quantity") {
                purchase_data.get(i).setQuantity(Integer.parseInt(val));
            } else if (attr == "price") {
                purchase_data.get(i).setUnitPriceAtSale(Double.parseDouble(val));
                setUnitPrices(purchase_data.get(i).getProduct().getId(), Double.parseDouble(val));
            }
        } catch (Exception e){e.printStackTrace();}
    }

    public void proceedPurchase(View v) {
        Map<String, Object> mObj = new HashMap<String, Object>();

        String notes = purchase_notes.getText().toString();
        if (notes.length() > 0) {
            mObj.put("notes", notes);
        }
        if (warehouse_id > 0) {
            mObj.put("warehouse_id", warehouse_id);
        }

        if (selected_wh_id > 0) {
            if (is_stock_in) {
                if (supplier_id < 0) {
                    mObj.put("warehouse_from", selected_wh_id);
                } else {
                    mObj.put("supplier_id", supplier_id);
                }
                mObj.put("warehouse_to", warehouse_id);
            } else if (is_stock_out) {
                mObj.put("warehouse_from", warehouse_id);
                mObj.put("warehouse_to", selected_wh_id);
            }
        } else {
            if (non_transaction_type != null) {
                mObj.put("type", non_transaction_type);
            } else {
                if (selected_wh_id < 0) {
                    Toast.makeText(getApplicationContext(),
                            "Mohon pilih salah satu " + wh_options.getText().toString(), Toast.LENGTH_LONG).show();
                    vibe.vibrate(200);
                    return;
                }
            }
        }
        // force completed
        mObj.put("force_complete", "1");

        String _url = "";
        if (is_stock_in) {
            if (mObj.containsKey("supplier_id")) {
                _url = Server.URL + "purchase/create-v2?api-key=" + Server.API_KEY;
            } else {
                _url = Server.URL + "transfer/incoming?api-key=" + Server.API_KEY;
            }
        } else if (is_stock_out) {
            if (selected_wh_id > 0) {
                //_url = Server.URL + "inventory/create-v2?api-key=" + Server.API_KEY;
                _url = Server.URL + "transfer/outgoing?api-key=" + Server.API_KEY;
            } else {
                //_url = Server.URL + "transfer/create-v2?api-key=" + Server.API_KEY;
                _url = Server.URL + "inventory/create-v2?api-key=" + Server.API_KEY;
            }
        }
        // build the items
        ArrayList arrItems = new ArrayList();
        Integer is_empty_unit_price = 0;
        for (int i = 0; i < purchase_data.size(); i++){
            Map<String, String> mItem = new HashMap<String, String>();
            try {
                // use map
                mItem.put("id", purchase_data.get(i).getProduct().getId()+"");
                mItem.put("barcode", purchase_data.get(i).getProduct().getBarcode());
                mItem.put("name", purchase_data.get(i).getProduct().getName());
                mItem.put("title", purchase_data.get(i).getProduct().getName());
                mItem.put("quantity", purchase_data.get(i).getQuantity()+"");
                mItem.put("unit_price", purchase_data.get(i).getPriceAtSale()+"");
                if (!unit_prices.containsKey(purchase_data.get(i).getProduct().getId())) {
                    is_empty_unit_price = is_empty_unit_price + 1;
                }
                arrItems.add(mItem);
            } catch (Exception e) {}
        }
        if (is_empty_unit_price > 0 && mObj.containsKey("supplier_id")) {
            Toast.makeText(getApplicationContext(),
                    "Harga beli tidak boleh dikosongi.", Toast.LENGTH_LONG).show();
            vibe.vibrate(200);
            return;
        }

        mObj.put("items", arrItems);

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
                                final int server_id = jObj.getInt(TAG_ID);
                                final String issue_number = jObj.getString("issue_number");

                                Toast.makeText(getApplicationContext(),
                                        "Successfully create new transaction with issue_number "+ issue_number, Toast.LENGTH_LONG).show();

                                new android.os.Handler().postDelayed(
                                        new Runnable() {
                                            public void run() {
                                                Intent intent = new Intent(PurchaseOrderActivity.this, PurchaseHistoryActivity.class);
                                                intent.putExtra("issue_number", issue_number);
                                                intent.putExtra("issue_id", server_id+"");
                                                finish();
                                                startActivity(intent);
                                            }
                                        },
                                        2000);
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
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
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

    private String[] warehouses = new String[]{};
    private HashMap<String, Integer> warehouse_ids = new HashMap<String, Integer>();

    public void showOutletOptionsDialog(final View v) {
        warehouse_ids.clear();
        AlertDialog.Builder builder = new AlertDialog.Builder(PurchaseOrderActivity.this);
        try {
            if (is_stock_in) {
                ArrayList<String> stringArrayList = new ArrayList<String>();
                if (warehousesListIn.size() > 0) {
                    for (Warehouses wh : warehousesListIn) {
                        if (wh.getId() != warehouse_id) {
                            stringArrayList.add(wh.getTitle());
                            warehouse_ids.put(wh.getTitle(), wh.getId());
                        }
                    }
                    warehouses = stringArrayList.toArray(new String[stringArrayList.size()]);
                }
            } else if (is_stock_out) {
                warehouse_ids.put(getResources().getString(R.string.origin), 0);
                ArrayList<String> stringArrayList = new ArrayList<String>();
                stringArrayList.add(getResources().getString(R.string.reject));
                if (warehousesListOut.size() > 0) {
                    for (Warehouses wh : warehousesListOut) {
                        if (wh.getId() != warehouse_id) {
                            stringArrayList.add(wh.getTitle());
                            warehouse_ids.put(wh.getTitle(), wh.getId());
                        }
                    }
                    warehouses = stringArrayList.toArray(new String[stringArrayList.size()]);
                }
            }

            builder.setSingleChoiceItems(warehouses, 0, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                    ((EditText) v).setText(warehouses[i]);
                    // starting to do updating the data
                    selected_wh_name = warehouses[i];
                    selected_wh_id = warehouse_ids.get(selected_wh_name);
                }
            });
            builder.show();
        } catch (Exception e){e.printStackTrace();}
    }

    private HashMap<String, List<Warehouses>> grouped_warehouse_list_in = new HashMap<String, List<Warehouses>>();
    private HashMap<String, List<Warehouses>> grouped_warehouse_list_out = new HashMap<String, List<Warehouses>>();

    private void getAvailableWH() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);
        params.put("warehouse_id", warehouse_id+"");

        String _url = Server.URL + "warehouse/list-transfer?api-key=" + Server.API_KEY;

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
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject data = jObj.getJSONObject("data");
                                if (data.has("in")) {
                                    JSONArray data_in = data.getJSONArray("in");
                                    if (data_in.length() > 0) {
                                        for (int i = 0; i < data_in.length(); i++) {
                                            JSONObject data_n = data_in.getJSONObject(i);
                                            Warehouses wh = new Warehouses(data_n.getInt("warehouse_rel_id"), data_n.getString("title"), data_n.getString("address"), data_n.getString("phone"), data_n.getInt("active"));
                                            warehousesListIn.add(wh);
                                            JSONObject configs = data_n.getJSONObject("configs");
                                            if (configs.has("category")) {
                                                String category = configs.getString("category");
                                                if (grouped_warehouse_list_in.containsKey(category)) {
                                                    List<Warehouses> the_list = grouped_warehouse_list_in.get(category);
                                                    the_list.add(wh);
                                                } else {
                                                    List<Warehouses> the_list = new ArrayList<Warehouses>();
                                                    the_list.add(wh);
                                                    grouped_warehouse_list_in.put(category, the_list);
                                                }
                                            }
                                        }
                                    }
                                }

                                if (data.has("out")) {
                                    JSONArray data_out = data.getJSONArray("out");
                                    if (data_out.length() > 0) {
                                        for (int i = 0; i < data_out.length(); i++) {
                                            JSONObject data_n = data_out.getJSONObject(i);
                                            Warehouses wh = new Warehouses(data_n.getInt("warehouse_rel_id"), data_n.getString("title"), data_n.getString("address"), data_n.getString("phone"), data_n.getInt("active"));
                                            warehousesListOut.add(wh);
                                            JSONObject configs = data_n.getJSONObject("configs");
                                            if (configs.has("category")) {
                                                String category = configs.getString("category");
                                                if (grouped_warehouse_list_out.containsKey(category)) {
                                                    List<Warehouses> the_list = grouped_warehouse_list_out.get(category);
                                                    the_list.add(wh);
                                                } else {
                                                    List<Warehouses> the_list = new ArrayList<Warehouses>();
                                                    the_list.add(wh);
                                                    grouped_warehouse_list_out.put(category, the_list);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void showStockOptions(EditText editText){
        Bundle bundle = new Bundle();
        if (is_stock_in) {
            bundle.putString("title", getResources().getString(R.string.origin));
        } else {
            bundle.putString("title", getResources().getString(R.string.destination));
        }

        StockOptionsDialog newFragment = new StockOptionsDialog(PurchaseOrderActivity.this, editText);
        newFragment.setArguments(bundle);
        if (is_stock_in) {
            newFragment.setListData(grouped_warehouse_list_in);
            newFragment.setIsStockIn();
            newFragment.setSelectedWarehouseId(selected_wh_id);
        } else {
            newFragment.setListData(grouped_warehouse_list_out);
            newFragment.setIsStockOut();
            newFragment.setSelectedWarehouseId(selected_wh_id);
            newFragment.setNonTransactionTypes(non_transaction_type_list);
            if (selected_non_transaction_id > -1) {
                newFragment.setSelectedNonTransactionId(selected_non_transaction_id);
            }
        }
        newFragment.show(getSupportFragmentManager(), "");
    }

    public void setSelectedWH(int warehouse_id, String warehouse_name) {
        this.selected_wh_id = warehouse_id;
        this.selected_wh_name = warehouse_name;
        if (warehouse_id > 0) {
            this.selected_non_transaction_id = -1;
            this.non_transaction_type = null;
            this.selected_non_transaction_name = null;
            buildListPurchase(false);
        }
    }

    public void setSelectedNonTransaction(int _id, String _name) {
        this.selected_wh_id = 0;
        this.selected_wh_name = _name;
        this.selected_non_transaction_id = _id;
        this.selected_non_transaction_name = _name;
        this.non_transaction_type = nonTransactionTypeKeys.get(_name).toString();
    }

    public void setSupplier(int _supplier_id, String _supplier_name) {
        this.supplier_id = _supplier_id;
        this.supplier_name = _supplier_name;
        if (_supplier_id >= 0) {
            buildListPurchase(true);
        }
    }

    private void getAvailableSupplier() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);
        params.put("warehouse_id", warehouse_id+"");

        String _url = Server.URL + "supplier/list?api-key=" + Server.API_KEY;

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
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                if (data.length() > 0) {
                                    for (int i = 0; i < data.length(); i++) {
                                        JSONObject data_n = data.getJSONObject(i);
                                        Warehouses wh = new Warehouses(data_n.getInt("id"), data_n.getString("name"), data_n.getString("address"), data_n.getString("phone"), data_n.getInt("active"));
                                        if (grouped_warehouse_list_in.containsKey("supplier")) {
                                            List<Warehouses> the_list = grouped_warehouse_list_in.get("supplier");
                                            the_list.add(wh);
                                        } else {
                                            List<Warehouses> the_list = new ArrayList<Warehouses>();
                                            the_list.add(wh);
                                            grouped_warehouse_list_in.put("supplier", the_list);
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private Map<String, String> nonTransactionTypeKeys = new HashMap<String, String>();

    private void getTransactionTypes() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);

        String _url = Server.URL + "inventory/non-transaction-type?api-key=" + Server.API_KEY;

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
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                non_transaction_type_list = jObj.getJSONObject("data");
                                Iterator<String> keys = non_transaction_type_list.keys();
                                Integer j = 0;
                                while (keys.hasNext()) {
                                    String key = keys.next();
                                    nonTransactionTypeKeys.put(non_transaction_type_list.getString(key), key);
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void setUnitPrices(Integer product_id, Double _price) {
        unit_prices.put(product_id, _price);
    }
}
