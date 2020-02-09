package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PurchaseDetailActivity extends Activity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseDetailActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private ActionBar actionBar;
    private String issue_id;
    private String issue_formated_number;
    private TextView issue_number;
    private TextView created_at;
    private TextView created_by;
    private RecyclerView itemListRecycle;
    private List<PurchaseLineItem> purchase_data = new ArrayList<PurchaseLineItem>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        if (getIntent().hasExtra("issue_id")) { // has sale data from server
            this.issue_id = getIntent().getStringExtra("issue_id");
            try {
                getDetailFromServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        initUI(savedInstanceState);
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_stock_in_out));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_purchase_detail);

        issue_number = (TextView) findViewById(R.id.issue_number);
        created_at = (TextView) findViewById(R.id.created_at);
        created_by = (TextView) findViewById(R.id.created_by);
        initiateActionBar();

        itemListRecycle = (RecyclerView) findViewById(R.id.itemListRecycle);
        itemListRecycle.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        itemListRecycle.setHasFixedSize(true);
        itemListRecycle.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getDetailFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("id", issue_id + "");

        String url = Server.URL + "transfer/history-detail?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            Log.e(getClass().getSimpleName(), "jObj : " + jObj.toString());
                            // Check for error node in json
                            if (success == 1) {
                                JSONObject server_data = jObj.getJSONObject("data");
                                if (server_data != null) {
                                    if (server_data.has("tr_number")) {
                                        issue_number.setText(server_data.getString("tr_number"));
                                        issue_formated_number = server_data.getString("tr_number");
                                    } else if (server_data.has("ti_number")) {
                                        issue_number.setText(server_data.getString("ti_number"));
                                        issue_formated_number = server_data.getString("ti_number");
                                    } else if (server_data.has("ii_number")) {
                                        issue_number.setText(server_data.getString("ii_number"));
                                        issue_formated_number = server_data.getString("ii_number");
                                    } else if (server_data.has("issue_number")) {
                                        issue_number.setText(server_data.getString("issue_number"));
                                        issue_formated_number = server_data.getString("issue_number");
                                    }

                                    if (server_data.has("created_at")) {
                                        created_at.setText(server_data.getString("created_at"));
                                    }

                                    if (server_data.has("created_by_name")) {
                                        created_by.setText(server_data.getString("created_by_name"));
                                    }

                                    if (issue_formated_number.length() > 0) {
                                        actionBar.setTitle(issue_formated_number);
                                    }

                                    JSONObject configs = server_data.getJSONObject("configs");
                                    if (configs.has("items")) {
                                        JSONArray items = configs.getJSONArray("items");
                                        if (items.length() > 0) {
                                            for (int i = 0; i < items.length(); i++) {
                                                JSONObject data_n = items.getJSONObject(i);
                                                Product product = new Product(data_n.getInt("barcode"), data_n.getString("title"), data_n.getString("barcode"), 0.0);
                                                PurchaseLineItem lineItem = new PurchaseLineItem(product, data_n.getInt("quantity"), data_n.getInt("quantity"));
                                                purchase_data.add(lineItem);
                                            }
                                        }
                                    }

                                    if (purchase_data.size() > 0) {
                                        AdapterListPurchaseConfirm pAdap = new AdapterListPurchaseConfirm(PurchaseDetailActivity.this, purchase_data);
                                        pAdap.notifyDataSetChanged();
                                        itemListRecycle.setAdapter(pAdap);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
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
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

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
        }) {
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
}