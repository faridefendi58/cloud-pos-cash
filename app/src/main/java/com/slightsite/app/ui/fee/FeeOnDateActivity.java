package com.slightsite.app.ui.fee;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
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
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Fee;
import com.slightsite.app.domain.sale.FeeOn;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.sale.AdapterListFee;
import com.slightsite.app.ui.sale.AdapterListPaymentSimple;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class FeeOnDateActivity extends AppCompatActivity {

    private Register register;
    private ParamCatalog paramCatalog;

    private String date_fee = "";
    private RecyclerView feeListRecycle;
    private RecyclerView paymentitemListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_on_date);

        Intent intent = getIntent();
        if (intent.hasExtra("date")) {
            date_fee = intent.getExtras().get("date").toString();
        }

        try {
            register = Register.getInstance();
            paramCatalog = ParamService.getInstance().getParamCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initToolbar();
        buildListFee();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.invoice_detail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        if (date_fee.length() > 0) {
            toolbar_title.setText("Penjualan "+ DateTimeStrategy.parseDate(date_fee, "dd MMM yyyy"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private TextView fee_report_title;
    private TextView total_omzet;
    private TextView total_fee;
    private TextView total_transaction;

    private void buildListFee() {
        feeListRecycle = (RecyclerView) findViewById(R.id.feeListRecycle);
        feeListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        feeListRecycle.setHasFixedSize(true);
        feeListRecycle.setNestedScrollingEnabled(false);

        paymentitemListView = (RecyclerView) findViewById(R.id.paymentListRecycle);
        paymentitemListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        paymentitemListView.setHasFixedSize(true);
        paymentitemListView.setNestedScrollingEnabled(false);

        fee_report_title = (TextView) findViewById(R.id.fee_report_title);
        total_omzet = (TextView) findViewById(R.id.total_omzet);
        total_fee = (TextView) findViewById(R.id.total_fee);
        total_transaction = (TextView) findViewById(R.id.total_transaction);

        int warehouse_id = Integer.parseInt(paramCatalog.getParamByName("warehouse_id").getValue());
        Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_id", warehouse_id + "");
        params.put("created_at", date_fee);
        params.put("group_by", "invoice_id");

        String url = Server.URL + "transaction/list-fee-on?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : "+ result);
                            if (result.contains("success")) {
                                final JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                final Map<Integer, Integer> invoice_ids = new HashMap<Integer, Integer>();;
                                ArrayList<FeeOn> listFee = new ArrayList<FeeOn>();
                                ArrayList<Payment> paymentList = new ArrayList<Payment>();
                                final Map<Integer, JSONObject> items_datas = new HashMap<Integer, JSONObject>();;
                                if (success == 1) {
                                    JSONObject data = jObj.getJSONObject("data");
                                    JSONArray items_data = data.getJSONArray("items");
                                    for(int n = 0; n < items_data.length(); n++) {
                                        JSONObject item_data = items_data.getJSONObject(n);
                                        FeeOn _fee = new FeeOn(
                                                item_data.getString("paid_at"),
                                                item_data.getString("invoice_number"),
                                                Double.parseDouble(item_data.getString("total_fee")),
                                                Double.parseDouble(item_data.getString("total_revenue")));
                                        listFee.add(_fee);
                                        invoice_ids.put(n, item_data.getInt("invoice_id"));
                                        items_datas.put(n, item_data);
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    fee_report_title.setText(getResources().getString(R.string.transaction)+" "+ DateTimeStrategy.parseDate(date_fee, "dd MMM yyyy"));
                                    total_omzet.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_revenue")));
                                    total_fee.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_fee")));
                                    total_transaction.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_transaction")) +" transaksi");

                                    JSONObject payments = summary_data.getJSONObject("payments");
                                    if (payments.length() > 0) {
                                        Iterator<String> pkeys = payments.keys();
                                        int no = 1;
                                        while(pkeys.hasNext()) {
                                            String key = pkeys.next();
                                            try {
                                                Payment pym = new Payment(no, key, payments.getDouble(key));
                                                paymentList.add(pym);
                                                no = no + 1;
                                            } catch (Exception e){}
                                        }
                                    }
                                }

                                AdapterListFeeOn adapter = new AdapterListFeeOn(getApplicationContext(), listFee);
                                feeListRecycle.setAdapter(adapter);

                                adapter.setOnItemClickListener(new AdapterListFeeOn.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, FeeOn obj, int position) {
                                        Intent newActivity = new Intent(getBaseContext(), FeeDetailActivity.class);
                                        newActivity.putExtra("invoice_id", invoice_ids.get(position));
                                        newActivity.putExtra("fee_data", items_datas.get(position).toString());
                                        startActivity(newActivity);
                                    }
                                });

                                AdapterListPaymentSimple pAdap = new AdapterListPaymentSimple(paymentList);
                                paymentitemListView.setAdapter(pAdap);
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

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {

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
                Log.e(getClass().getSimpleName(), "ada error : "+ error.getMessage());
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

    public interface VolleyCallback {
        void onSuccess(String result);
    }
}
