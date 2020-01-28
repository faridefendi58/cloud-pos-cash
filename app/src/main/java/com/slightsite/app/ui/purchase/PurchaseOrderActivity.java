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
import com.google.gson.reflect.TypeToken;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PurchaseOrderActivity extends AppCompatActivity {

    private List<PurchaseLineItem> purchase_data = new ArrayList<PurchaseLineItem>();
    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;
    private int warehouse_id;
    private String admin_id;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseOrderActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private RecyclerView purchaseListView;
    private Button btn_proceed;
    private RadioGroup radioTransactionType;
    private Boolean is_purchase_order = true;
    private Boolean is_inventory_issue = false;
    private EditText purchase_notes;

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

        try {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
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

        purchase_notes = (EditText) findViewById(R.id.purchase_notes);

        btn_proceed = (Button) findViewById(R.id.btn_proceed);
        radioTransactionType = (RadioGroup) findViewById(R.id.radioTransactionType);
    }

    private void initAction() {
        AdapterListPurchaseConfirm pAdap = new AdapterListPurchaseConfirm(PurchaseOrderActivity.this, purchase_data);
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

    public void updatePurchaseData(int i, String attr, String val) {
        try {
            if (attr == "quantity") {
                purchase_data.get(i).setQuantity(Integer.parseInt(val));
            } else if (attr == "price") {
                purchase_data.get(i).setUnitPriceAtSale(Double.parseDouble(val));
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
        // force completed
        mObj.put("force_complete", "1");

        String _url = Server.URL + "purchase/create-v2?api-key=" + Server.API_KEY;
        if (is_purchase_order) {
            mObj.put("type", "purchase_order");
        } else if (is_inventory_issue) {
            mObj.put("type", "inventory_issue");
            _url = Server.URL + "purchase/create-v2?api-key=" + Server.API_KEY;
        }
        // build the items
        ArrayList arrItems = new ArrayList();
        for (int i = 0; i < purchase_data.size(); i++){
            Map<String, String> mItem = new HashMap<String, String>();
            try {
                // use map
                mItem.put("id", purchase_data.get(i).getProduct().getId()+"");
                mItem.put("barcode", purchase_data.get(i).getProduct().getBarcode());
                mItem.put("name", purchase_data.get(i).getProduct().getName());
                mItem.put("quantity", purchase_data.get(i).getQuantity()+"");
                mItem.put("unit_price", purchase_data.get(i).getPriceAtSale()+"");
                arrItems.add(mItem);
            } catch (Exception e) {}
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
}
