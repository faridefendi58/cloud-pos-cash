package com.slightsite.app.ui.inventory;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.inventory.Stock;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.domain.warehouse.AdminInWarehouseCatalog;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressLint("NewApi")
public class ProductServerActivity extends Activity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = ProductServerActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private Resources res;
    private ParamCatalog paramCatalog;
    private WarehouseCatalog warehouseCatalog;
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private Spinner available_warehouse;
    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private JSONArray warehouse_data;

    private ProductCatalog productCatalog;
    private String warehouse_name;
    private Stock stock;

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(res.getString(R.string.action_syncronize));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ConnectivityManager conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }

        res = getResources();
        initiateActionBar();

        setContentView(R.layout.activity_product_server);

        initUi();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(ProductServerActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUi() {
        available_warehouse = (Spinner) findViewById(R.id.available_warehouse);

        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            productCatalog = Inventory.getInstance().getProductCatalog();
            stock = Inventory.getInstance().getStock();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            adminInWarehouseCatalog = AdminInWarehouseService.getInstance().getAdminInWarehouseCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        getWarehouseList();
    }

    private void getWarehouseList() {

        Map<String, String> params = new HashMap<String, String>();

        warehouse_items.clear();

        String url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Params whParam = paramCatalog.getParamByName("warehouse_id");
                            int selected_wh = 0;

                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                warehouse_data = jObj.getJSONArray("data");
                                for(int n = 0; n < warehouse_data.length(); n++)
                                {
                                    JSONObject data_n = warehouse_data.getJSONObject(n);
                                    warehouse_items.add(data_n.getString("title"));
                                    warehouse_ids.put(data_n.getString("title"), data_n.getString("id"));
                                    if (whParam != null) {
                                        if (Integer.parseInt(whParam.getValue()) == Integer.parseInt(data_n.getString("id"))) {
                                            selected_wh = n;
                                        }
                                    }

                                    // updating or inserting the wh data on local
                                    Warehouses whs = warehouseCatalog.getWarehouseByWarehouseId(data_n.getInt("id"));
                                    if (whs == null) {
                                        warehouseCatalog.addWarehouse(
                                                data_n.getInt("id"),
                                                data_n.getString("title"),
                                                data_n.getString("address"),
                                                data_n.getString("phone"),
                                                data_n.getInt("active")
                                                );
                                    } else {
                                        whs.setTitle(data_n.getString("title"));
                                        whs.setAddress(data_n.getString("address"));
                                        whs.setPhone(data_n.getString("phone"));
                                        whs.setStatus(data_n.getInt("active"));
                                        warehouseCatalog.editWarehouse(whs);
                                    }
                                }
                            }

                            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.spinner_item, warehouse_items);
                            whAdapter.notifyDataSetChanged();
                            available_warehouse.setAdapter(whAdapter);
                            available_warehouse.setSelection(selected_wh);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
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
            Log.e(TAG, e.getMessage());
        }
    }

    private void insert_new_product() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");
        params.put("with_discount", "1");
        params.put("warehouse_name", warehouse_name);
        params.put("with_stock", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                ArrayList<String> product_items = new ArrayList<String>();
                                HashMap< Integer, Integer> stocks = new HashMap< Integer, Integer>();
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    product_items.add(data_n.getString("id"));
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(data_n.getString("id"));
                                    } catch (Exception e) {}
                                    if (pd == null) {
                                        try {
                                            productCatalog.addProduct(
                                                    data_n.getString("title"),
                                                    data_n.getString("id"),
                                                    Double.parseDouble(data_n.getString("price")));
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
                                        }
                                    } else {
                                        try {
                                            pd.setName(data_n.getString("title"));
                                            pd.setBarcode(data_n.getString("id"));
                                            pd.setUnitPrice(Double.parseDouble(data_n.getString("price")));
                                            productCatalog.editProduct(pd);
                                        } catch (Exception e) {}
                                    }

                                    stocks.put(data_n.getInt("id"), data_n.getInt("stock"));
                                }
                                // clear the discount data
                                stock.clearProductDiscount();

                                JSONArray discounts = jObj.getJSONArray("discount");
                                for(int p = 0; p < data.length(); p++)
                                {
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(product_items.get(p));
                                    } catch (Exception e) {}

                                    JSONArray discount_items = discounts.getJSONArray(p);
                                    if (discount_items.length() > 0 && pd != null) {
                                        for(int q = 0; q < discount_items.length(); q++)
                                        {
                                            JSONObject data_q = discount_items.getJSONObject(q);
                                            try {
                                                ContentValues disc = stock.getDiscountDataByQuantity(Integer.parseInt(product_items.get(p)),data_q.getInt("quantity"));
                                                if (disc != null) {
                                                    stock.updateProductDiscount(
                                                            disc.getAsInteger("_id"),
                                                            Integer.parseInt(data_q.getString("quantity")),
                                                            Integer.parseInt(data_q.getString("quantity_max")),
                                                            Double.parseDouble(data_q.getString("price")));
                                                } else {
                                                    stock.addProductDiscount(
                                                            DateTimeStrategy.getCurrentTime(),
                                                            Integer.parseInt(data_q.getString("quantity")),
                                                            Integer.parseInt(data_q.getString("quantity_max")),
                                                            pd,
                                                            Double.parseDouble(data_q.getString("price")));
                                                }
                                            } catch (Exception e) {
                                                Log.e(TAG, e.getMessage());
                                            }
                                        }
                                    }
                                }

                                // clear the discount data
                                stock.clearStock();

                                // also insert the stock
                                for (Map.Entry<Integer, Integer> entry : stocks.entrySet()) {
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(entry.getKey().toString());
                                        List lot = stock.getProductLotByProductId(entry.getKey());
                                        if (lot.size() > 0) {
                                            stock.updateStockSum(entry.getKey(), entry.getValue());
                                        } else {
                                            stock.addProductLot(
                                                    DateTimeStrategy.getCurrentTime(),
                                                    entry.getValue(),
                                                    pd,
                                                    pd.getUnitPrice());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void insert_new_stock() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_name", warehouse_name);

        String url = Server.URL + "stock/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");
                                HashMap< Integer, Integer> stocks = new HashMap< Integer, Integer>();
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    if (stocks.get(data_n.getInt("product_id")) != null) {
                                        Integer qty = stocks.get(data_n.getInt("product_id")) + data_n.getInt("quantity");
                                        stocks.put(data_n.getInt("product_id"), qty);
                                    } else {
                                        stocks.put(data_n.getInt("product_id"), data_n.getInt("quantity"));
                                    }
                                }

                                Log.e(TAG, "Stock : "+ stocks.toString());

                                for (Map.Entry<Integer, Integer> entry : stocks.entrySet()) {
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(entry.getKey().toString());
                                        List lot = stock.getProductLotByProductId(entry.getKey());
                                        Log.e(entry.getKey().toString(), "Lot : "+ lot.toString());
                                        Log.e(entry.getKey().toString(), "Lot size : "+ lot.size());
                                        if (lot.size() > 0) {
                                            stock.updateStockSum(entry.getKey(), entry.getValue());
                                        } else {
                                            stock.addProductLot(
                                                    DateTimeStrategy.getCurrentTime(),
                                                    entry.getValue(),
                                                    pd,
                                                    pd.getUnitPrice());
                                        }
                                    } catch (Exception e) {}
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void syncronizeToServer(View view)
    {
        warehouse_name = available_warehouse.getSelectedItem().toString();
        CheckBox sync_product = findViewById(R.id.sync_product);
        CheckBox sync_customer = findViewById(R.id.sync_customer);

        Boolean success = false;
        if (sync_product.isChecked()) {
            try {
                insert_new_product();
                success &= true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        if (sync_customer.isChecked()) {
            try {
                insert_new_stock();
                success &= true;
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }

        try {
            Params pWarehouseId = paramCatalog.getParamByName("warehouse_id");
            if (pWarehouseId instanceof Params) {
                pWarehouseId.setValue(warehouse_ids.get(warehouse_name));
                Boolean saveParam = paramCatalog.editParam(pWarehouseId);
            } else {
                Boolean saveParam = paramCatalog.addParam("warehouse_id", warehouse_ids.get(warehouse_name), "text", warehouse_name);
            }
        } catch (Exception e) { e.printStackTrace(); }

        Toast.makeText(getBaseContext(), getResources().getString(R.string.message_success_syncronize),
                Toast.LENGTH_SHORT).show();

        // insert wh id to wh access list
        String wh_id = warehouse_ids.get(warehouse_name);
        if (wh_id != null) {
            try {
                Params prms = paramCatalog.getParamByName("admin_id");
                if (prms instanceof Params) {
                    int admin_id = Integer.parseInt(prms.getValue());
                    int warehouse_id = Integer.parseInt(wh_id);
                    AdminInWarehouse aiw = adminInWarehouseCatalog.getDataByAdminAndWH(admin_id, warehouse_id);
                    if (aiw == null) {
                        Boolean save = adminInWarehouseCatalog.addAdminInWarehouse(admin_id, warehouse_id, 1);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void backToPrevActivity(View view)
    {
        Intent intent = new Intent(ProductServerActivity.this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}
