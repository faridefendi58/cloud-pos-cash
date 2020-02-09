package com.slightsite.app.ui.purchase;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
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
import android.view.MenuItem;
import android.view.View;
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
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.purchase.PurchaseItem;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PurchaseHistoryActivity extends AppCompatActivity {

    private String issue_number;
    private Integer issue_id;
    private RecyclerView purchaseHistoryListView;

    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseHistoryActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;
    private Integer warehouse_id;
    private List<PurchaseItem> history_data = new ArrayList<PurchaseItem>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order_history);

        Intent intent = getIntent();
        if (intent.hasExtra("issue_number") && intent.hasExtra("issue_id")) {
            issue_number = intent.getStringExtra("issue_number");
            issue_id = Integer.parseInt(intent.getStringExtra("issue_id"));
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
        toolbar_title.setText(getResources().getString(R.string.title_stock_in_out_history));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(PurchaseHistoryActivity.this, MainActivity.class);
                intent.putExtra("refreshStock", "1");
                finish();
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        purchaseHistoryListView = (RecyclerView) findViewById(R.id.purchaseHistoryListView);
        purchaseHistoryListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        purchaseHistoryListView.setHasFixedSize(true);
        purchaseHistoryListView.setNestedScrollingEnabled(false);
    }

    private void initAction() {
        try {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
        } catch (Exception e){e.printStackTrace();}
        buildListHistory();
    }

    private void buildListHistory() {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_id", warehouse_id + "");
        params.put("all_status", "1");
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }
        params.put("admin_id", admin_id);
        params.put("limit", "30");
        String url = Server.URL + "transfer/history?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                if (success == 1) {
                                    JSONArray detail_data = jObj.getJSONArray("data");
                                    for(int n = 0; n < detail_data.length(); n++) {
                                        JSONObject detail_obj = detail_data.getJSONObject(n);
                                        PurchaseItem pi = new PurchaseItem(detail_obj.getInt("id"));
                                        pi.setCreatedAt(detail_obj.getString("created_at"));
                                        //pi.setIssueNumber(detail_obj.getString("po_number"));
                                        pi.setTitle(detail_obj.getString("title"));
                                        pi.setType(detail_obj.getString("type"));
                                        pi.setNotes(detail_obj.getString("description"));
                                        pi.setStatus(detail_obj.getString("status"));
                                        pi.setCreatedBy(detail_obj.getString("created_by_name"));
                                        history_data.add(pi);
                                    }

                                    AdapterListPurchaseHistory pAdap = new AdapterListPurchaseHistory(PurchaseHistoryActivity.this, history_data);
                                    pAdap.notifyDataSetChanged();
                                    purchaseHistoryListView.setAdapter(pAdap);

                                    pAdap.setOnItemClickListener(new AdapterListPurchaseHistory.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, PurchaseItem obj, int position) {
                                            Intent intent = new Intent(PurchaseHistoryActivity.this, PurchaseDetailActivity.class);
                                            intent.putExtra("issue_id", obj.getIssueId()+"");
                                            //finish();
                                            startActivity(intent);
                                        }
                                    });
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "Failed!, No product data in ",
                                        Toast.LENGTH_LONG).show();
                            }

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
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }
}
