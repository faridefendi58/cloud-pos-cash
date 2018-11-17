package com.slightsite.app.ui.inventory;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
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
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.techicalservices.NoDaoSetException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private Spinner available_warehouse;
    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private JSONArray warehouse_data;

    private ProductCatalog productCatalog;
    private String warehouse_name;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(res.getString(R.string.action_syncronize));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
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
                this.finish();
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
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        getWarehouseList();
    }

    private void getWarehouseList() {

        Map<String, String> params = new HashMap<String, String>();

        warehouse_items.clear();

        String url = paramCatalog.getParamByName("server_url").getValue() + "warehouse/list?api-key=" + paramCatalog.getParamByName("server_api_key").getValue();
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                warehouse_data = jObj.getJSONArray("data");
                                for(int n = 0; n < warehouse_data.length(); n++)
                                {
                                    JSONObject data_n = warehouse_data.getJSONObject(n);
                                    warehouse_items.add(data_n.getString("title"));
                                }
                            }

                            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                                    getApplicationContext(),
                                    R.layout.spinner_item, warehouse_items);
                            whAdapter.notifyDataSetChanged();
                            available_warehouse.setAdapter(whAdapter);

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
        params.put("warehouse", warehouse_name);

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        String url = paramCatalog.getParamByName("server_url").getValue() + "product/list?api-key=" + paramCatalog.getParamByName("server_api_key").getValue();
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
                                Log.e(TAG, "List Product : " + data.toString());
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    items.add(data_n.getString("title"));
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(data_n.getString("id"));
                                    } catch (Exception e) {}
                                    if (pd == null) {
                                        try {
                                            productCatalog.addProduct(
                                                    data_n.getString("title"),
                                                    data_n.getString("id"),
                                                    Double.parseDouble("90000"));
                                        } catch (Exception e) {
                                            Log.e(TAG, e.getMessage());
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

    public void syncronizeToServer(View view)
    {
        warehouse_name = available_warehouse.getSelectedItem().toString();
        CheckBox sync_product = findViewById(R.id.sync_product);
        CheckBox sync_customer = findViewById(R.id.sync_customer);

        if (sync_product.isChecked()) {
            try {
                insert_new_product();
                Toast.makeText(getBaseContext(), getResources().getString(R.string.success),
                        Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
