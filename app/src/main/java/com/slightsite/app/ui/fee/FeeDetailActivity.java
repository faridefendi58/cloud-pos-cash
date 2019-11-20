package com.slightsite.app.ui.fee;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Display;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
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
import com.slightsite.app.domain.ParamsController;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.retur.Retur;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class FeeDetailActivity extends AppCompatActivity {

    private static final String TAG = FeeDetailActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    ProgressDialog pDialog;
    int success;

    private Register register;
    private ParamCatalog paramCatalog;
    private ProductCatalog productCatalog;
    private WarehouseCatalog warehouseCatalog;
    private int invoice_id = 0;
    private WebView print_webview;
    private LinearLayout print_preview_container;
    private int screen_width = 0;
    private Sale sale;
    private SaleLedger saleLedger;
    private int warehouse_id;
    private Warehouses warehouse;
    private JSONObject fee_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_detail);

        Intent intent = getIntent();
        if (intent.hasExtra("invoice_id") && intent.hasExtra("fee_data")) {
            try {
                invoice_id = Integer.parseInt(intent.getExtras().get("invoice_id").toString());
                fee_data = new JSONObject(getIntent().getStringExtra("fee_data"));
            } catch (Exception e){e.printStackTrace();}
        }

        try {
            register = Register.getInstance();
            paramCatalog = ParamService.getInstance().getParamCatalog();
            saleLedger = SaleLedger.getInstance();
            productCatalog = Inventory.getInstance().getProductCatalog();
            paramCatalog = ParamService.getInstance().getParamCatalog();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initToolbar();
        initView();
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
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void initView() {
        print_preview_container = (LinearLayout) findViewById(R.id.print_preview_container);

        print_webview = (WebView) findViewById(R.id.print_webview);
        print_webview.setVerticalScrollBarEnabled(false);
        print_webview.setHorizontalScrollBarEnabled(false);
        print_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;

        Log.e(getClass().getSimpleName(), "screen_width : "+ screen_width);
        if (screen_width > 900) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(640, LinearLayout.LayoutParams.WRAP_CONTENT);
            print_webview.setLayoutParams(params);
        }

        try {
            print_webview.loadDataWithBaseURL(null, "<html><body>Loading ...</body></html>", "text/html", "utf-8", null);
            buildDataFromServer();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private int counter = 0;
    private int is_delivered = 0;
    private JSONObject server_invoice_data;
    private List<Payment> paymentList;
    private String formated_receipt;
    private Customer customer;
    private Boolean should_be_finished = false;
    private List<Map<String, String>> lineitemList;
    private Retur retur;

    public void buildDataFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        if (sharedpreferences == null) {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        }
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("invoice_id", invoice_id+"");

        String url = Server.URL + "transaction/detail?api-key=" + Server.API_KEY;
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
                                server_invoice_data = jObj.getJSONObject("data");
                                if (server_invoice_data.has("shipping")) {
                                    JSONArray arr_shipping = server_invoice_data.getJSONArray("shipping");
                                    JSONObject obj_shipping = arr_shipping.getJSONObject(0);
                                    sale = new Sale(invoice_id, server_invoice_data.getString("created_at"));
                                    // instead of using local data, its better build payment data from server
                                    // in order to get latest data
                                    JSONArray arr_payment = server_invoice_data.getJSONArray("payment");
                                    if (arr_payment.length() > 0) {
                                        paymentList = new ArrayList<Payment>();
                                        for (int m = 0; m < arr_payment.length(); m++) {
                                            JSONObject pay_method = null;
                                            try {
                                                pay_method = arr_payment.getJSONObject(m);
                                                Payment payment = new Payment(
                                                        -1,
                                                        sale.getId(),
                                                        pay_method.getString("type"),
                                                        pay_method.getDouble("amount_tendered")
                                                );
                                                paymentList.add(payment);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }

                                if (server_invoice_data.has("items_belanja")) {
                                    JSONArray items_belanja = server_invoice_data.getJSONArray("items_belanja");
                                    if (items_belanja.length() > 0) {
                                        int qty_total = server_invoice_data.getInt("total_quantity");

                                        List<LineItem> theLineItemList = new ArrayList<LineItem>();
                                        for (int b = 0; b < items_belanja.length(); b++) {
                                            JSONObject item_belanja = null;
                                            try {
                                                item_belanja = items_belanja.getJSONObject(b);
                                                Product product = productCatalog.getProductByBarcode(item_belanja.getString("barcode"));
                                                LineItem lineItem = new LineItem(product, item_belanja.getInt("qty"), qty_total);
                                                theLineItemList.add(lineItem);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                        sale.setAllLineItem(theLineItemList);
                                    }
                                }

                                if (server_invoice_data.has("created_by") && !server_invoice_data.getString("created_by").toString().equals("null")) {
                                    // setup the sale
                                    sale.setServerInvoiceNumber(server_invoice_data.getString("invoice_number"));
                                    sale.setCreatedBy(server_invoice_data.getInt("created_by"));
                                    sale.setCreatedByName(server_invoice_data.getString("created_by_name"));
                                    sale.setPaidBy(server_invoice_data.getInt("paid_by"));
                                    sale.setPaidByName(server_invoice_data.getString("paid_by_name"));
                                    sale.setRefundedBy(server_invoice_data.getInt("refunded_by"));
                                    sale.setRefundedByName(server_invoice_data.getString("refunded_by_name"));
                                    sale.setDeliveredByName(server_invoice_data.getString("delivered_by_name"));

                                    // force to be delivered if trigger from proceed Order Button
                                    if (getIntent().hasExtra("process_order")) {
                                        is_delivered = 1;
                                    }

                                    if (server_invoice_data.has("status") && server_invoice_data.has("delivered")) {
                                        int delivered = server_invoice_data.getInt("delivered");
                                        is_delivered = delivered;
                                    }

                                    if (server_invoice_data.has("refund") && server_invoice_data.getString("refund") != null) {
                                        JSONObject refundObj = new JSONObject();
                                        try {
                                            refundObj = server_invoice_data.getJSONObject("refund");
                                            if (refundObj.length() > 0) {
                                                retur = new Retur(refundObj.getInt("id"));
                                                JSONArray arrRefunds = refundObj.getJSONArray("items");

                                                ArrayList arrRefundList = new ArrayList();
                                                for (int ir = 0; ir < arrRefunds.length(); ++ir) {
                                                    JSONObject ref = arrRefunds.getJSONObject(ir);
                                                    Map<String, String> arrRefundList2 = new HashMap<String, String>();
                                                    arrRefundList2.put("title", ref.getString("name"));
                                                    arrRefundList2.put("product_id", ref.getString("id"));
                                                    arrRefundList2.put("quantity", ref.getString("total_qty"));
                                                    arrRefundList2.put("price", ref.getString("price"));
                                                    arrRefundList2.put("change_item", ref.getString("returned_qty"));

                                                    arrRefundList.add(arrRefundList2);
                                                }

                                                ArrayList arrChangeItemList = new ArrayList();
                                                JSONArray arrChanges = refundObj.getJSONArray("items_change");
                                                if (arrChanges.length() > 0) {
                                                    for (int ic = 0; ic < arrChanges.length(); ++ic) {
                                                        JSONObject chg = arrChanges.getJSONObject(ic);
                                                        Map<String, String> arrChangeItemList2 = new HashMap<String, String>();
                                                        arrChangeItemList2.put("id", ic+"");
                                                        if (chg.has("id")) {
                                                            arrChangeItemList2.put("product_id", chg.getString("id"));
                                                        } else {
                                                            arrChangeItemList2.put("product_id", ic+"");
                                                        }
                                                        arrChangeItemList2.put("title", chg.getString("name"));
                                                        arrChangeItemList2.put("quantity", chg.getString("quantity"));
                                                        arrChangeItemList2.put("price", chg.getString("price"));
                                                        arrChangeItemList2.put("quantity_total", chg.getString("quantity_total"));
                                                        if (chg.has("fee")) {
                                                            arrChangeItemList2.put("fee", chg.getString("fee"));
                                                        }
                                                        arrChangeItemList.add(arrChangeItemList2);
                                                    }
                                                }
                                                retur.setItems(arrRefundList);
                                                if (arrChangeItemList.size() > 0) {
                                                    retur.setItemsChange(arrChangeItemList);
                                                }
                                            }
                                        } catch (Exception e){}
                                    }

                                    formated_receipt = getFormatedReceiptHtml();

                                    String style = "<style>";
                                    style += ".ft-14{font-size:10px !important;}";
                                    style += ".ft-16{font-size:16px !important;}";
                                    style += ".ft-17{font-size:17px !important;}";
                                    style += ".ft-18{font-size:18px !important;}";
                                    style += ".ft-20{font-size:20px !important;}";
                                    style += ".ft-22{font-size:22px !important;}";
                                    style += ".ft-24{font-size:24px !important;}";
                                    style += ".ft-26{font-size:26px !important;}";
                                    style += "</style>";
                                    print_webview.loadDataWithBaseURL(null, "<html>"+ style +"<body>" + formated_receipt + "</body></html>", "text/html", "utf-8", null);
                                }
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
            e.printStackTrace();
        }
    }

    public String getFormatedReceiptHtml() {
        String res = "<table width=\"100%\" style=\"margin-top:20px;\">";

        String current_time =  DateTimeStrategy.getCurrentTime();

        Double sale_total = 0.00;
        Double amount_tendered = 0.00;
        try {
            current_time = sale.getEndTime();
            sale_total = sale.getTotal();
        } catch (Exception e) { e.printStackTrace(); }

        String[] separated = current_time.split(" ");
        res += "<tr class=\"ft-18\"><td>"+ getResources().getString(R.string.label_date)+ "</td>" +
                "<td colspan=\"3\" class=\"ft-17\"> : "+ separated[0] +"</td>";
        String date_transaction = sale.getEndTime();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MMM-dd  hh:mm a");
            date_transaction = DateTimeStrategy.parseDate(sale.getEndTime(), "yyMMdd");

        } catch (Exception e) { e.printStackTrace(); }

        if (sharedpreferences == null) {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        }
        ContentValues adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
        String admin_id = ParamsController.getInstance().getParam("admin_id");

        String no_nota = date_transaction+"/"+sale.getId();
        if (sale.getServerInvoiceNumber() != null) {
            no_nota = sale.getServerInvoiceNumber();
        } else {
            if (admin_id != null) {
                no_nota = date_transaction + "/" + admin_id + "/" + sale.getId();
            }
        }

        res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_no_nota)+ "</td><td colspan=\"3\"> : "+ no_nota +"</td></tr>";
        res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_hour)+ "</td><td colspan=\"3\"> : "+ separated[1] +"</td></tr>";

        if (sale.getCreatedBy() > 0) {
            res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_created_by) + "</td><td colspan=\"3\"> : " + sale.getCreatedByName() + "</td></tr>";
            if (sale.getPaidBy() > 0) {
                if (is_delivered > 0 || should_be_finished) {
                    res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                }
            } else {
                if (should_be_finished) {
                    res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                }
            }
        } else {
            if (adminData != null) {
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_created_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                if (is_delivered > 0) {
                    res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                }
            }
        }

        if (customer != null) {
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.customer)+ "</td><td colspan=\"3\"> : "+ customer.getName() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_address)+ "</td><td colspan=\"3\"> : "+ customer.getAddress() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_phone)+ "</td><td colspan=\"3\"> : "+ customer.getPhone() +"</td></tr>";
        }

        List<LineItem> list = sale.getAllLineItem();
        Log.e(getClass().getSimpleName(), "list line item :"+ list.toString());
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            lineitemList.add(line.toMap());
        }

        res += "<tr><td colspan=\"4\"><hr/></td></tr></table>";
        res += "<table width=\"100%\" style=\"margin-bottom:25px;\">";

        int sub_total = 0;
        int ppn = 0;
        for (int i = 0; i < lineitemList.size(); ++i) {
            res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ lineitemList.get(i).get("name") +"</td></tr>";
            int qty = Integer.parseInt(lineitemList.get(i).get("quantity"));
            int prc = Integer.parseInt(lineitemList.get(i).get("price").replace(".", ""));
            int tot = prc * qty;
            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">"+ qty +" x "+ CurrencyController.getInstance().moneyFormat(prc) +"</td>";
            res += "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(tot) +"</td></tr>";

            sub_total = sub_total + tot;
        }
        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_sub_total) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(sub_total) +"</td>";
        //res += "<tr><td colspan=\"2\" style=\"text-align:right;\">PPN :</td><td style=\"text-align:right;\">"+ ppn +"</td>";
        int discount = sale.getDiscount();
        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_discount) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(discount) +"</td>";

        int grand_total = sub_total + ppn - discount;

        int cash = grand_total;
        int change_due = grand_total - cash;
        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_grand_total) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(grand_total) +"</td>";

        if (paymentList != null && !paymentList.isEmpty()) {
            int payment_total = 0;
            for (int j = 0; j < paymentList.size(); ++j) {
                Payment py = paymentList.get(j);
                int amnt = 0;
                String amnt_str = String.format("%.0f", py.getAmount());
                try {
                    if (amnt_str != "0" || amnt_str != "0.0") {
                        amnt = Integer.parseInt(amnt_str);
                        payment_total = payment_total + amnt;
                    }
                } catch (Exception e){
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }

                if (amnt > 0) {
                    try {
                        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">" + getResources().getString(getPaymentChannel(py.getPaymentChannel())) + " :</td>" +
                                "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(amnt) + "</td>";
                    } catch (Exception e){e.printStackTrace();}
                }
            }
            change_due = payment_total - grand_total;
        } else {
            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.payment_cash) +" :</td>" +
                    "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(cash) +"</td>";
        }

        if (change_due > 0) {
            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_change_due) +" :</td>" +
                    "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(change_due) +"</td>";
        } else {
            int debt = -1 * change_due;
            if (debt < 0) {
                res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\"><b>" + getResources().getString(R.string.label_dept) + " :</b></td>" +
                        "<td style=\"text-align:right;\"><b>" + CurrencyController.getInstance().moneyFormat(debt) + "</b></td>";
            }
        }

        if (fee_data != null) {
            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
            res += "<tr><td colspan=\"4\"><b>Fee Penjualan</b></td></tr>";
            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
            JSONObject item_configs = new JSONObject();
            try {
                item_configs = fee_data.getJSONObject("configs");
            } catch (Exception e){}

            Map<String,Integer> product_fees = new HashMap<>();
            // print the items if any
            if (item_configs.length() > 0) {
                Double sub_total_fee = 0.0;
                Iterator<String> keys = item_configs.keys();
                while(keys.hasNext()) {
                    String key = keys.next();
                    try {
                        JSONObject cfg = item_configs.getJSONObject(key);
                        if (cfg instanceof JSONObject) {
                            res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ cfg.getString("name") +"</td></tr>";
                            int qty = Integer.parseInt(cfg.getString("qty"));
                            String str_fee = cfg.getString("fee");
                            int fee = 0; int original_fee = 0;
                            if (str_fee.contains(".")) {
                                original_fee = Integer.parseInt(cfg.getString("fee").replace(".", ""));
                                fee = original_fee / qty;
                            } else {
                                original_fee = Integer.parseInt(str_fee);
                                fee = original_fee / qty;
                            }
                            sub_total_fee = sub_total_fee + original_fee;
                            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">"+ qty +" x "+ CurrencyController.getInstance().moneyFormat(fee) +"</td>";
                            res += "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(original_fee) +"</td></tr>";
                            // build product fees for retur
                            product_fees.put(cfg.getString("name"), fee);
                        }
                    } catch (Exception e){e.printStackTrace();}
                }

                // retur if any
                if (retur != null) {
                    Log.e(getClass().getSimpleName(), "retur.getItems() : "+ retur.getItems().toString());
                    res += "<tr><td colspan=\"4\"><hr/></td></tr>";

                    List<Map<String, String >> rlist = retur.getItems();
                    int sub_total_r = 0;
                    Map<String,Integer> list_tukar_barang = new HashMap<>();
                    for (Map<String, String> entry : rlist) {
                        int qty = Integer.parseInt(entry.get("quantity"));
                        int change_qty = Integer.parseInt(entry.get("change_item"));
                        int refund_qty = qty - change_qty;
                        String str_price = entry.get("price");
                        if (str_price.contains(".")) {
                            str_price = str_price.substring(0, str_price.indexOf("."));
                        }
                        int prc = Integer.parseInt(str_price);
                        int tot = prc * refund_qty;
                        if (change_qty > 0) {
                            list_tukar_barang.put(entry.get("title"), change_qty);
                        }

                        sub_total_r = sub_total_r + tot;
                    }

                    if (sub_total_r > 0) {
                        res += "<tr><td colspan=\"4\"><b>Fee Retur</b></td></tr>";
                        res += "<tr><td colspan=\"4\"><hr/></td></tr>";
                        for (Map<String, String> entry2 : rlist) {
                            int qty = Integer.parseInt(entry2.get("quantity"));
                            int change_qty = Integer.parseInt(entry2.get("change_item"));
                            int refund_qty = qty - change_qty;
                            String str_price = entry2.get("price");
                            if (str_price.contains(".")) {
                                str_price = str_price.substring(0, str_price.indexOf("."));
                            }
                            int rprc = 0; //Integer.parseInt(str_price);
                            if (product_fees.containsKey(entry2.get("title"))) {
                                rprc = product_fees.get(entry2.get("title"));
                            }
                            int rtot = rprc * refund_qty;
                            if (refund_qty > 0) {
                                res += "<tr class=\"ft-17\"><td colspan=\"4\">" + entry2.get("title") + "</td></tr>";
                                res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">" + refund_qty + " x -" + CurrencyController.getInstance().moneyFormat(rprc) + "</td>";
                                res += "<td style=\"text-align:right;\">-" + CurrencyController.getInstance().moneyFormat(rtot) + "</td></tr>";
                                sub_total_fee = sub_total_fee - rtot;
                            }
                        }
                    }

                    List<Map<String, String >> list_change = retur.getItemsChange();
                    if (list_change.size() > 0) {
                        if (sub_total_r == 0) {
                            res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";
                            res += "<tr><td colspan=\"4\" style=\"text-align:left;\"><b>Penukaran Dengan Item Lain</b></td></tr>";
                            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
                        }
                        int tot_ctot = 0;
                        for (Map<String, String> c_entry : list_change) {
                            res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ c_entry.get("title") +"</td></tr>";
                            int cqty = Integer.parseInt(c_entry.get("quantity"));
                            String str_price = c_entry.get("price");
                            if (str_price.contains(".")) {
                                str_price = str_price.substring(0, str_price.indexOf("."));
                            }
                            int cprc = 0; //Integer.parseInt(str_price);
                            if (product_fees.containsKey(c_entry.get("title"))) {
                                cprc = product_fees.get(c_entry.get("title"));
                            } else {
                                if (c_entry.containsKey("fee")) {
                                    String str_fee = c_entry.get("fee");
                                    if (str_fee.contains(".")) {
                                        str_fee = str_fee.substring(0, str_fee.indexOf("."));
                                    }
                                    cprc = Integer.parseInt(str_fee);
                                    cprc = cprc/cqty;
                                }
                            }
                            int ctot = cprc * cqty;
                            tot_ctot = tot_ctot + ctot;
                            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">" + c_entry.get("quantity") + " x "+ CurrencyController.getInstance().moneyFormat(cprc) +"</td>";
                            res += "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(ctot) + "</td></tr>";
                        }
                        sub_total_fee = sub_total_fee + tot_ctot;
                    }
                }

                if (sub_total_fee > 0) {
                    res += "<tr><td colspan=\"4\"><hr/></td></tr>";
                    res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_total_fee) +" :</td>" +
                            "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(sub_total_fee) +"</td>";
                }
            }
        }

        res += "</table>";

        return res;
    }

    public Integer getPaymentChannel(String channel) {
        Map<String, Integer> result = new HashMap<>();

        result.put("cash_receive", R.string.payment_cash);
        result.put("nominal_mandiri", R.string.payment_mandiri);
        result.put("nominal_bca", R.string.payment_bca);
        result.put("nominal_bri", R.string.payment_bri);
        result.put("nominal_edc", R.string.payment_edc);
        result.put("nominal_wallet_tokopedia", R.string.payment_wallet_tokopedia);
        result.put("wallet_tokopedia", R.string.payment_wallet_tokopedia);
        result.put("wallet_gofood", R.string.payment_wallet_gofood);
        result.put("wallet_grabfood", R.string.payment_wallet_grab_food);

        return result.get(channel);
    }
}
