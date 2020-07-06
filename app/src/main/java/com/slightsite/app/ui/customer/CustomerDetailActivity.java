package com.slightsite.app.ui.customer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.FeeOn;
import com.slightsite.app.domain.sale.ItemCounter;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.fee.AdapterListPaymentOn;
import com.slightsite.app.ui.fee.AdapterListSaleCounter;
import com.slightsite.app.ui.sale.SaleDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

/**
 * UI for shows the datails of each Customer.
 * @author Farid Efendi
 *
 */
@SuppressLint("NewApi")
public class CustomerDetailActivity extends Activity {

    private Customer customer;
    private Resources res;
    private String id;

    ProgressDialog pDialog;
    int success;
    private SharedPreferences sharedpreferences;
    private Register register;
    private ParamCatalog paramCatalog;

    private static final String TAG = CustomerDetailActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private int FRAGMENT_CUSTOMER = 5;

    private TextView tv_customer_name;
    private TextView tv_customer_email;
    private TextView tv_customer_phone;
    private TextView tv_customer_address;
    private RecyclerView orderListRecycle;
    private RecyclerView paymentListRecycle;
    private TextView total_spend;
    private TextView total_transaction;
    private TextView fee_report_title;
    private RecyclerView refundListRecycle;
    private Button moreOrderListButton;

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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(res.getString(R.string.title_customer));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        res = getResources();
        initiateActionBar();

        try {
            register = Register.getInstance();
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            id = getIntent().getStringExtra("id");
            if (id != null) {
                getDetailFromServer();
                buildListOrder();
            }
            if (getIntent().hasExtra("fragment")) {
                FRAGMENT_CUSTOMER = Integer.parseInt(getIntent().getStringExtra("fragment"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        initUI(savedInstanceState);
    }

    /**
     * Initiate this UI.
     * @param savedInstanceState
     */
    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_customer_detail);

        tv_customer_name = (TextView) findViewById(R.id.tv_customer_name);
        tv_customer_email = (TextView) findViewById(R.id.tv_customer_email);
        tv_customer_phone = (TextView) findViewById(R.id.tv_customer_phone);
        tv_customer_address = (TextView) findViewById(R.id.tv_customer_address);
        fee_report_title = (TextView) findViewById(R.id.fee_report_title);
        fee_report_title.setText(getResources().getString(R.string.heading_summary));

        orderListRecycle = (RecyclerView) findViewById(R.id.orderListRecycle);
        orderListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        orderListRecycle.setHasFixedSize(true);
        orderListRecycle.setNestedScrollingEnabled(false);

        moreOrderListButton = (Button) findViewById(R.id.moreOrderListButton);

        paymentListRecycle = (RecyclerView) findViewById(R.id.paymentListRecycle);
        paymentListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        paymentListRecycle.setHasFixedSize(true);
        paymentListRecycle.setNestedScrollingEnabled(false);

        total_spend = (TextView) findViewById(R.id.total_spend);
        total_transaction = (TextView) findViewById(R.id.total_transaction);

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

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent act = new Intent(CustomerDetailActivity.this, MainActivity.class);
                act.putExtra("fragment", FRAGMENT_CUSTOMER);
                finish();
                startActivity(act);
                return true;
            case R.id.action_edit:
                return true;
            default:
                return super.onOptionsItemSelected(item);
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

    private void getDetailFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("id", id);

        String url = Server.URL + "customer/detail?api-key=" + Server.API_KEY;
        _string_request(
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
                                JSONObject server_data = jObj.getJSONObject("data");
                                tv_customer_name.setText(server_data.getString("name"));
                                tv_customer_email.setText(server_data.getString("email"));
                                tv_customer_phone.setText(server_data.getString("telephone"));
                                tv_customer_address.setText(server_data.getString("address"));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private Map<Integer, Sale> list_invoices = new HashMap<Integer, Sale>();
    private Map<Integer, Customer> list_customers = new HashMap<Integer, Customer>();
    private Map<Integer, Shipping> list_shippings = new HashMap<Integer, Shipping>();
    private Map<Integer, String> list_payments = new HashMap<Integer, String>();
    private Map<Integer, String> list_items_belanja = new HashMap<Integer, String>();
    private int order_limit = 2;
    private int current_page = 1;
    private ArrayList<FeeOn> listOrder = new ArrayList<FeeOn>();
    private List<List<FeeOn>> order_parts = new ArrayList<>();

    private void buildListOrder() {
        int warehouse_id = Integer.parseInt(paramCatalog.getParamByName("warehouse_id").getValue());
        final Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_id", warehouse_id + "");
        params.put("customer_id", id);
        params.put("group_by", "invoice_id");

        String url = Server.URL + "transaction/list-customer-order?api-key=" + Server.API_KEY;
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
                                        listOrder.add(_fee);
                                        invoice_ids.put(n, item_data.getInt("invoice_id"));
                                        items_datas.put(n, item_data);

                                        // buid bundle for saleDetail
                                        buildBundleForSaleDetail(item_data);
                                        // endof build bundle for saleDetail
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    //fee_report_title.setText(getResources().getString(R.string.transaction)+" "+ DateTimeStrategy.parseDate(date_fee, "dd MMM yyyy"));
                                    Double total_revenue = summary_data.getDouble("total_revenue");
                                    //total_fee.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_fee")));
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
                                    total_spend.setText(CurrencyController.getInstance().moneyFormat(total_revenue));
                                }

                                // chunk list order
                                order_parts = chopped(listOrder, order_limit);
                                if (order_parts.size() > 1) {
                                    moreOrderListButton.setVisibility(View.VISIBLE);
                                }

                                AdapterListCustomerOrder adapter = new AdapterListCustomerOrder(getApplicationContext(), order_parts.get(0));
                                orderListRecycle.setAdapter(adapter);

                                adapter.setOnItemClickListener(new AdapterListCustomerOrder.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, FeeOn obj, int position) {
                                        Intent newActivity = new Intent(getBaseContext(), SaleDetailActivity.class);
                                        int _inv_id = invoice_ids.get(position);
                                        newActivity.putExtra("sale_intent", list_invoices.get(_inv_id));
                                        newActivity.putExtra("customer_intent", list_customers.get(_inv_id));
                                        newActivity.putExtra("shipping_intent", list_shippings.get(_inv_id));
                                        newActivity.putExtra("payment_intent", list_payments.get(_inv_id));
                                        newActivity.putExtra("line_items_intent", list_items_belanja.get(_inv_id));
                                        startActivity(newActivity);
                                    }
                                });

                                AdapterListPaymentOn pAdap = new AdapterListPaymentOn(paymentList);
                                paymentListRecycle.setAdapter(pAdap);

                                // build sales counter summary
                                Map<String, String> params2 = new HashMap<String, String>();
                                params2.put("warehouse_id", params.get("warehouse_id"));
                                params2.put("customer_id", id);
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

    private void buildTheItemSummary(Map<String, String> params) {
        String url = Server.URL + "transaction/list-customer-sale-counter?api-key=" + Server.API_KEY;
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

    private void buildBundleForSaleDetail(JSONObject item_data) {
        try {
            // build the sale
            Sale sale = new Sale(item_data.getInt("invoice_id"), item_data.getString("created_at"));
            if (item_data.has("delivered_at")) {
                sale.setDeliveredPlanAt(item_data.getString("delivered_at"));
            }
            sale.setServerInvoiceNumber(item_data.getString("invoice_number"));
            sale.setServerInvoiceId(item_data.getInt("invoice_id"));
            if (item_data.has("customer_id")) {
                sale.setCustomerId(item_data.getInt("customer_id"));
            }
            if (item_data.has("status")) {
                String _status = "paid";
                if (item_data.getInt("status") == 0) {
                    sale.setStatus("unpaid");
                }
            }
            list_invoices.put(item_data.getInt("invoice_id"), sale);

            // build customer
            JSONObject invoice_configs = item_data.getJSONObject("invoice_configs");
            if (invoice_configs.has("customer")) {
                JSONObject cust_dt = invoice_configs.getJSONObject("customer");
                String _email = "-";
                if (cust_dt.has("email") && cust_dt.getString("email") != null) {
                    _email = cust_dt.getString("email");
                }
                Customer _customer = new Customer(
                        sale.getCustomerId(),
                        cust_dt.getString("name"),
                        _email,
                        cust_dt.getString("phone"),
                        cust_dt.getString("address"),
                        1
                );
                list_customers.put(item_data.getInt("invoice_id"), _customer);
            }

            // build shipping
            if (invoice_configs.has("shipping")) {
                JSONArray arrShip = invoice_configs.getJSONArray("shipping");
                if (arrShip.length() > 0) {
                    JSONObject ship_method = arrShip.getJSONObject(0);
                    if (ship_method != null) {
                        Shipping shipping = new Shipping(
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
                            if (ship_method.getString("recipient_phone") != null && ship_method.getString("recipient_phone") !="null") {
                                shipping.setPhone(ship_method.getString("recipient_phone"));
                            }
                        }
                        if (ship_method.has("pickup_date")) {
                            shipping.setPickupDate(ship_method.getString("pickup_date"));
                        }

                        list_shippings.put(item_data.getInt("invoice_id"), shipping);
                    }
                }
            }

            // build the payment
            if (item_data.has("payments")) {
                JSONArray arrPayment = item_data.getJSONArray("payments");
                if (arrPayment.length() > 0) {
                    list_payments.put(item_data.getInt("invoice_id"), arrPayment.toString());
                }
            }

            // build the line item data
            if (invoice_configs.has("items_belanja")) {
                JSONArray arrItemsBelanja = invoice_configs.getJSONArray("items_belanja");
                if (arrItemsBelanja.length() > 0) {
                    list_items_belanja.put(item_data.getInt("invoice_id"), arrItemsBelanja.toString());
                }
            }
        } catch (Exception e){e.printStackTrace();}
    }

    private static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<List<T>>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<T>(
                    list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    public void showMoreOrder(View view) {
        current_page = current_page + 1;
        if (order_parts.size() >= current_page) {
            int chunk_size = current_page * order_limit;
            List<List<FeeOn>> new_order_parts = chopped(listOrder, chunk_size);
            AdapterListCustomerOrder adapter = new AdapterListCustomerOrder(getApplicationContext(), new_order_parts.get(0));
            orderListRecycle.setAdapter(adapter);
            orderListRecycle.scrollToPosition((chunk_size-1));
            if (new_order_parts.size() <= 1) {
                view.setVisibility(View.GONE);
            }
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
