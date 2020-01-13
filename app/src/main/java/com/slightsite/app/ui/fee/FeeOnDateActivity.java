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
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Fee;
import com.slightsite.app.domain.sale.FeeOn;
import com.slightsite.app.domain.sale.ItemCounter;
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
    private LinearLayout refund_detail_container;
    private RecyclerView refundListRecycle;

    private RecyclerView saleItemListRecycle;
    private RecyclerView minOrder10ItemListRecycle;
    private RecyclerView minOrder5ItemListRecycle;
    private RecyclerView eceranItemListRecycle;
    private RecyclerView saleReturItemListRecycle;

    private LinearLayout grosirListContainer;
    private LinearLayout semiGrosirListContainer;
    private LinearLayout eceranListContainer;
    private LinearLayout saleReturItemContainer;

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
        initView();
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

    private void initView() {
        grosirListContainer = (LinearLayout) findViewById(R.id.grosirListContainer);
        semiGrosirListContainer = (LinearLayout) findViewById(R.id.semiGrosirListContainer);
        eceranListContainer = (LinearLayout) findViewById(R.id.eceranListContainer);
        saleReturItemContainer = (LinearLayout) findViewById(R.id.saleReturItemContainer);

        saleItemListRecycle = (RecyclerView) findViewById(R.id.saleItemListRecycle);
        saleItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        minOrder10ItemListRecycle = (RecyclerView) findViewById(R.id.minOrder10ItemListRecycle);
        minOrder10ItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        minOrder5ItemListRecycle = (RecyclerView) findViewById(R.id.minOrder5ItemListRecycle);
        minOrder5ItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        eceranItemListRecycle = (RecyclerView) findViewById(R.id.eceranItemListRecycle);
        eceranItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        saleReturItemListRecycle = (RecyclerView) findViewById(R.id.saleReturItemListRecycle);
        saleReturItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
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

        refundListRecycle = (RecyclerView) findViewById(R.id.refundListRecycle);
        refundListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        refundListRecycle.setHasFixedSize(true);
        refundListRecycle.setNestedScrollingEnabled(false);

        fee_report_title = (TextView) findViewById(R.id.fee_report_title);
        total_omzet = (TextView) findViewById(R.id.total_omzet);
        total_fee = (TextView) findViewById(R.id.total_fee);
        total_transaction = (TextView) findViewById(R.id.total_transaction);
        refund_detail_container = (LinearLayout) findViewById(R.id.refund_detail_container);

        int warehouse_id = Integer.parseInt(paramCatalog.getParamByName("warehouse_id").getValue());
        final Map<String, String> params = new HashMap<String, String>();
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
                                ArrayList<Payment> refundList = new ArrayList<Payment>();
                                final Map<Integer, JSONObject> items_datas = new HashMap<Integer, JSONObject>();;
                                if (success == 1) {
                                    JSONObject data = jObj.getJSONObject("data");
                                    JSONArray items_data = data.getJSONArray("items");
                                    for(int n = 0; n < items_data.length(); n++) {
                                        JSONObject item_data = items_data.getJSONObject(n);
                                        FeeOn _fee = new FeeOn(
                                                item_data.getString("delivered_at"),
                                                item_data.getString("invoice_number"),
                                                Double.parseDouble(item_data.getString("total_fee")),
                                                Double.parseDouble(item_data.getString("total_revenue")));
                                        _fee.setTotalRefund(Double.parseDouble(item_data.getString("total_refund")));
                                        listFee.add(_fee);
                                        invoice_ids.put(n, item_data.getInt("invoice_id"));
                                        items_datas.put(n, item_data);
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    fee_report_title.setText(getResources().getString(R.string.transaction)+" "+ DateTimeStrategy.parseDate(date_fee, "dd MMM yyyy"));
                                    Double total_revenue = summary_data.getDouble("total_revenue");
                                    //total_omzet.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_revenue")));
                                    total_fee.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_fee")));
                                    total_transaction.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_transaction")) +"");

                                    JSONObject payments = summary_data.getJSONObject("payments");
                                    if (payments.length() > 0) {
                                        Iterator<String> pkeys = payments.keys();
                                        int no = 1;
                                        Double change_due = 0.0;
                                        while(pkeys.hasNext()) {
                                            String key = pkeys.next();
                                            try {
                                                Payment pym = new Payment(no, key, payments.getDouble(key));
                                                paymentList.add(pym);
                                                no = no + 1;
                                            } catch (Exception e){}
                                        }

                                        if (summary_data.has("change_due") && summary_data.getInt("change_due") > 0) {
                                            Payment pym = new Payment(paymentList.size() + 1, "change_due", summary_data.getDouble("change_due"));
                                            paymentList.add(pym);
                                        }
                                    }

                                    if (summary_data.has("refunds")) {
                                        JSONObject refunds = summary_data.getJSONObject("refunds");
                                        if (refunds.length() > 0) {
                                            Iterator<String> rkeys = refunds.keys();
                                            int no = 1;
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    Payment pym = new Payment(no, key, refunds.getDouble(key));
                                                    refundList.add(pym);
                                                    // merge refund data on normal payment
                                                    Payment pym_minus = new Payment(no, "refund_"+key, refunds.getDouble(key));
                                                    paymentList.add(pym_minus);
                                                    no = no + 1;
                                                    // calculate net total revenue
                                                    total_revenue = total_revenue - refunds.getDouble(key);
                                                } catch (Exception e){}
                                            }
                                        }
                                    }

                                    // omzet = revenue - refunds
                                    total_omzet.setText(CurrencyController.getInstance().moneyFormat(total_revenue));
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

                                AdapterListPaymentOn pAdap = new AdapterListPaymentOn(paymentList);
                                paymentitemListView.setAdapter(pAdap);

                                if (refundList.size() > 0) {
                                    // Deactive refund section due to combined in payment section
                                    /*AdapterListPaymentOn rfAdap = new AdapterListPaymentOn(refundList);
                                    refundListRecycle.setAdapter(rfAdap);

                                    refund_detail_container.setVisibility(View.VISIBLE);*/
                                }

                                // build sales counter summary
                                Map<String, String> params2 = new HashMap<String, String>();
                                params2.put("created_at_from", params.get("created_at"));
                                params2.put("warehouse_id", params.get("warehouse_id"));
                                buildTheItemSummary(params2);
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

    private void buildTheItemSummary(Map<String, String> params) {
        String url = Server.URL + "transaction/list-sale-counter?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : " + result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                ArrayList<ItemCounter> listSaleEceran = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleSemiGrosir = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleGrosir = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleSummary = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleReturSummary = new ArrayList<ItemCounter>();
                                if (success == 1) {
                                    JSONObject data = jObj.getJSONObject("data");
                                    JSONObject items_data = data.getJSONObject("items");
                                    if (items_data.has("eceran")) {
                                        JSONObject eceran = items_data.getJSONObject("eceran");
                                        if (eceran.length() > 0) {
                                            Iterator<String> rkeys = eceran.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, eceran.getInt(key));
                                                    listSaleEceran.add(_item_counter);
                                                } catch (Exception e){}
                                            }

                                            eceranListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleEceran);
                                            eceranItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            eceranListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    if (items_data.has("semi_grosir")) {
                                        JSONObject semi_grosir = items_data.getJSONObject("semi_grosir");
                                        if (semi_grosir.length() > 0) {
                                            Iterator<String> rkeys = semi_grosir.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, semi_grosir.getInt(key));
                                                    listSaleSemiGrosir.add(_item_counter);
                                                } catch (Exception e){}
                                            }
                                            semiGrosirListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleSemiGrosir);
                                            minOrder5ItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            semiGrosirListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    if (items_data.has("grosir")) {
                                        JSONObject grosir = items_data.getJSONObject("grosir");
                                        if (grosir.length() > 0) {
                                            Iterator<String> rkeys = grosir.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, grosir.getInt(key));
                                                    listSaleGrosir.add(_item_counter);
                                                } catch (Exception e){}
                                            }
                                            grosirListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleGrosir);
                                            minOrder10ItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            grosirListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    if (summary_data.length() > 0) {
                                        Iterator<String> rkeys = summary_data.keys();
                                        while(rkeys.hasNext()) {
                                            String key = rkeys.next();
                                            try {
                                                ItemCounter _item_counter = new ItemCounter(key, summary_data.getInt(key));
                                                listSaleSummary.add(_item_counter);
                                            } catch (Exception e){}
                                        }
                                    }
                                    AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleSummary);
                                    saleItemListRecycle.setAdapter(smAdap);


                                    if (data.has("returs")) {
                                        Boolean is_json_valid = Tools.isJSONObject(data.getString("returs"));
                                        if (is_json_valid) { // check is json object
                                            JSONObject returs_data = data.getJSONObject("returs");
                                            if (returs_data.length() > 0) {
                                                Iterator<String> rkeys = returs_data.keys();
                                                while (rkeys.hasNext()) {
                                                    String key = rkeys.next();
                                                    try {
                                                        JSONObject returs_product = returs_data.getJSONObject(key);
                                                        if (returs_product.length() > 0) {
                                                            Iterator<String> rkeys2 = returs_product.keys();
                                                            while (rkeys2.hasNext()) {
                                                                String key2 = rkeys2.next();
                                                                if (returs_product.getInt(key2) < 0) {
                                                                    ItemCounter _item_counter2 = new ItemCounter(key2, returs_product.getInt(key2));
                                                                    listSaleReturSummary.add(_item_counter2);
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {}
                                                }
                                                if (listSaleReturSummary.size() > 0) {
                                                    AdapterListSaleCounter rtAdap = new AdapterListSaleCounter(listSaleReturSummary);
                                                    saleReturItemListRecycle.setAdapter(rtAdap);
                                                    saleReturItemContainer.setVisibility(View.VISIBLE);
                                                } else {
                                                    saleReturItemContainer.setVisibility(View.GONE);
                                                }
                                            }
                                        } else {
                                            saleReturItemContainer.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e){e.printStackTrace();}
                    }
                });
    }
}
