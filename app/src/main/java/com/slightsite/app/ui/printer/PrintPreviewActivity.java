package com.slightsite.app.ui.printer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintJob;
import android.print.PrintManager;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.content.ContextCompat;
import android.support.v4.print.PrintHelper;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
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
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.payment.PaymentCatalog;
import com.slightsite.app.domain.payment.PaymentService;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PrintPreviewActivity extends Activity {

    private static final String TAG = PrintPreviewActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    ProgressDialog pDialog;
    int success;

    private Sale sale;
    private int saleId;
    private SaleLedger saleLedger;

    private ParamCatalog paramCatalog;
    private PaymentCatalog paymentCatalog;
    private WarehouseCatalog warehouseCatalog;
    private CustomerCatalog customerCatalog;
    private ProductCatalog productCatalog;
    private List<Payment> paymentList;
    private List<Map<String, String>> lineitemList;
    private int warehouse_id;
    private Warehouses warehouse;
    private Customer customer;
    private JSONObject server_invoice_data;

    private WebView print_webview;
    private LinearLayout print_preview_container;
    private Button home_button;
    private Button print_button;
    private LinearLayout print_button_container;
    private Button finish_and_print_button;

    BluetoothAdapter bluetoothAdapter;
    BluetoothSocket socket;
    BluetoothDevice bluetoothDevice;
    OutputStream outputStream;
    InputStream inputStream;
    String value = "";
    String bluetoothDeviceName = "58Printer";
    ArrayList<String> bluetoothDeviceList;

    private String formated_receipt;
    private int shipping_method = 0;
    private List<LineItem> lineItems;

    private int screen_width = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            saleLedger = SaleLedger.getInstance();
            saleId = getIntent().getIntExtra("saleId", 0);
            productCatalog = Inventory.getInstance().getProductCatalog();
            if (getIntent().hasExtra("sale_intent")) { // has sale data from server
                sale = (Sale) getIntent().getSerializableExtra("sale_intent");
                if (getIntent().hasExtra("line_items_intent")) { // has line item data from server
                    JSONArray arrLineItems = null;
                    try {
                        arrLineItems = new JSONArray(getIntent().getStringExtra("line_items_intent"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (arrLineItems != null) {
                        lineItems = new ArrayList<LineItem>();
                        for (int m = 0; m < arrLineItems.length(); m++) {
                            JSONObject line_object = null;
                            try {
                                line_object = arrLineItems.getJSONObject(m);
                                Product p = productCatalog.getProductByBarcode(line_object.getString("barcode"));
                                if (p != null) {
                                    LineItem lineItem = new LineItem(
                                            p,
                                            line_object.getInt("qty"),
                                            line_object.getInt("qty")
                                    );
                                    lineItem.setUnitPriceAtSale(line_object.getDouble("unit_price"));
                                    lineItems.add(lineItem);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        sale.setAllLineItem(lineItems);
                    }
                }
            } else {
                sale = saleLedger.getSaleById(saleId);
            }

            paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
            if (saleId > 0) {
                if (getIntent().hasExtra("payment_intent")) {
                    JSONArray arrPayment = null;
                    try {
                        arrPayment = new JSONArray(getIntent().getStringExtra("payment_intent"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (arrPayment != null) {
                        paymentList = new ArrayList<Payment>();
                        for (int m = 0; m < arrPayment.length(); m++) {
                            JSONObject pay_method = null;
                            try {
                                pay_method = arrPayment.getJSONObject(m);
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
                } else {
                    paymentList = paymentCatalog.getPaymentBySaleId(saleId);
                }
            }

            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();

            if (sale.getCustomerId() > 0) {
                if (getIntent().hasExtra("customer_intent")) { // has sale data from server
                    customer = (Customer) getIntent().getSerializableExtra("customer_intent");
                } else {
                    customer = customerCatalog.getCustomerById(sale.getCustomerId());
                }
            }

            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }

            shipping_method = getIntent().getIntExtra("shipping_method", 0);

            Params bParam = paramCatalog.getParamByName("bluetooth_device_name");
            if (bParam != null) {
                bluetoothDeviceName = bParam.getValue();
            }
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initUI(savedInstanceState);
        initTriggerButton();
        InitDeviceList();
    }

    /**
     * Initiate actionbar.
     */
    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_print_preview));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_share, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (!getIntent().hasExtra("delayed")) {
                    this.finish();
                } else {
                    Intent intent = new Intent(PrintPreviewActivity.this, MainActivity.class);
                    intent.putExtra("refreshStock", true);
                    finish();
                    startActivity(intent);
                }
                return true;
            case R.id.nav_share :
                shareInvoice();
                return true;
            case R.id.action_print_setting :
                printerSetting();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_print_preview);

        initiateActionBar();

        print_preview_container = (LinearLayout) findViewById(R.id.print_preview_container);

        print_webview = (WebView) findViewById(R.id.print_webview);
        print_webview.setVerticalScrollBarEnabled(false);
        print_webview.setHorizontalScrollBarEnabled(false);
        print_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;

        if (screen_width > 980) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(400, LinearLayout.LayoutParams.WRAP_CONTENT);
            print_webview.setLayoutParams(params);
        }

        /*print_webview.getSettings().setLoadWithOverviewMode(true);
        print_webview.getSettings().setUseWideViewPort(true);
        print_webview.getSettings().setMinimumFontSize(60);
        print_webview.getSettings().setSupportZoom(true);
        print_webview.getSettings().setBuiltInZoomControls(true);
        print_webview.getSettings().setDisplayZoomControls(true);*/

        try {
            print_webview.loadDataWithBaseURL(null, "<html><body>Loading ...</body></html>", "text/html", "utf-8", null);
            Intent intent = getIntent();
            if (intent.hasExtra("delayed")) {
                new android.os.Handler().postDelayed(
                        new Runnable() {
                            public void run() {
                                sale = saleLedger.getSaleById(saleId); // reinit the sale to get server data
                                buildDataFromServer();
                            }
                        },
                        2000);
            } else {
                buildDataFromServer();
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        home_button = (Button) findViewById(R.id.home_button);
        print_button = (Button) findViewById(R.id.print_button);

        print_button_container = (LinearLayout) findViewById(R.id.print_button_container);
        finish_and_print_button = (Button) findViewById(R.id.finish_and_print_button);

        if (shipping_method == 0 || shipping_method == 3) { //directly and tokopedia
            // bug #15-8, harusnya jangan tampil lg klo dah complete
            print_button_container.setVisibility(View.GONE);
            finish_and_print_button.setVisibility(View.VISIBLE);
        }
    }

    private void initTriggerButton() {
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrintPreviewActivity.this, MainActivity.class);
                intent.putExtra("refreshStock", true);
                finish();
                startActivity(intent);
            }
        });

        print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                just_print(true);
            }
        });

        finish_and_print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishAndPrint();
            }
        });
    }

    private int counter = 0;
    public void buildDataFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        if (sharedpreferences == null) {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        }
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("invoice_id", sale.getServerInvoiceId()+"");

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

                                    if (server_invoice_data.has("status") && server_invoice_data.has("delivered")) {
                                        int status = server_invoice_data.getInt("status");
                                        int delivered = server_invoice_data.getInt("delivered");
                                        if (status > 0 && delivered > 0) {
                                            print_button_container.setVisibility(View.VISIBLE);
                                            finish_and_print_button.setVisibility(View.GONE);
                                        }
                                    }
                                } else {
                                    counter = counter + 1;
                                    if (counter <= 5) {
                                        sale = saleLedger.getSaleById(saleId);
                                        new android.os.Handler().postDelayed(
                                                new Runnable() {
                                                    public void run() {
                                                        buildDataFromServer();
                                                    }
                                                },
                                                2000);
                                    }
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
        String res = "";
        if (warehouse != null) {
            Params store_name = paramCatalog.getParamByName("store_name");
            if (store_name instanceof Params) {
                res += "<table width=\"100%\" style=\"margin-top:20px;\"><tr class=\"ft-18\"><td colspan=\"4\"><center><b>"+ store_name.getValue() +" "+warehouse.getTitle() +"</b></center></td></tr>";
            } else {
                res += String.format("%s%n", warehouse.getTitle());
            }
            res += "<tr class=\"ft-16\"><td colspan=\"4\" style=\"padding-top:10px;\"><center>"+ warehouse.getAddress() +"</center></td></tr>";
            res += "<tr class=\"ft-16\"><td colspan=\"4\"><center>"+ warehouse.getPhone() +"</center></td></tr>";
        }

        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

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
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + sale.getPaidByName() + "</td></tr>";
            }
        } else {
            if (adminData != null) {
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_created_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
            }
        }

        if (customer != null) {
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.customer)+ "</td><td colspan=\"3\"> : "+ customer.getName() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_address)+ "</td><td colspan=\"3\"> : "+ customer.getAddress() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_phone)+ "</td><td colspan=\"3\"> : "+ customer.getPhone() +"</td></tr>";
        }

        if (sale.getPaidBy() > 0) {
            res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.status) + "</td><td colspan=\"3\"> : <b>" + getResources().getString(R.string.message_paid) + "</b></td></tr>";
        } else {
            res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.status) + "</td><td colspan=\"3\"> : <b style=\"color:red;\" class=\"ft-26\">" + getResources().getString(R.string.message_unpaid) + "</b></td></tr>";
        }

        List<LineItem> list = sale.getAllLineItem();
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
                    amnt = Integer.parseInt(amnt_str);
                    payment_total = payment_total + amnt;
                } catch (Exception e){
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }

                res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(getPaymentChannel(py.getPaymentChannel())) +" :</td>" +
                        "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(amnt) +"</td>";

                /*res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ py.toMap().get("formated_payment_channel") +" :</td>" +
                        "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(amnt) +"</td>";*/
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
        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

        res += "<tr class=\"ft-17\" style=\"padding-bottom:25px;\"><td colspan=\"4\"><center>Terimakasih.<br />Selamat belanja kembali.</center></td></tr></table>";

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

        return result.get(channel);
    }

    private Bitmap screenShot(WebView view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        /*if (screen_width > 980) {
            float aspectRatio = view.getWidth() /
                    (float) view.getHeight();
            int width = 400; //400 lebar kertas
            int height = Math.round(width / aspectRatio);
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }*/
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static File saveBitmap(Bitmap bm, String fileName){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void shareInvoice() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        Bitmap bm = screenShot(print_webview);
        File file = saveBitmap(bm, sale.getServerInvoiceNumber() +".png");

        Uri uri = Uri.fromFile(new File(file.getAbsolutePath()));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, sale.getServerInvoiceNumber());
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "share via"));
    }

    private BottomSheetDialog bottomSheetDialog;
    private ListView print_list_view;

    private void printerSetting() {
        InitDeviceList();

        bottomSheetDialog = new BottomSheetDialog(PrintPreviewActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_bluetooth_devices, null);
        bottomSheetDialog.setContentView(sheetView);

        print_list_view = (ListView) sheetView.findViewById(R.id.print_list_view);
        GridListAdapter badapter = new GridListAdapter(PrintPreviewActivity.this, bluetoothDeviceList, true);
        int selected = bluetoothDeviceList.indexOf(bluetoothDeviceName);
        if (selected >= 0) {
            badapter.setSelectedPosition(selected);
        }
        print_list_view.setAdapter(badapter);

        bottomSheetDialog.show();
    }

    public void InitDeviceList() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                bluetoothDeviceList = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    bluetoothDeviceList.add(device.getName());
                }
            } else {
                value += "No Devices found";
                bluetoothDeviceList = new ArrayList<>();
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception ex) {
            value += ex.toString() + "\n" + " InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        }
    }

    private Boolean initPrinter() {
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        try {
            if (!bluetoothAdapter.isEnabled()) {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();

            if (pairedDevices.size() > 0) {
                bluetoothDeviceList = new ArrayList<>();
                for (BluetoothDevice device : pairedDevices) {
                    bluetoothDeviceList.add(device.getName());
                    if (device.getName().equals(bluetoothDeviceName)) //Note, you will need to change this to match the name of your device
                    {
                        bluetoothDevice = device;
                        break;
                    }
                }

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                Boolean is_connected = true;
                try {
                    socket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
                    /*Method m = bluetoothDevice.getClass().getMethod(
                            "createRfcommSocket", new Class[] { int.class });
                    socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);*/
                    bluetoothAdapter.cancelDiscovery();
                    socket.connect();
                } catch (IOException e){
                    e.printStackTrace();
                    is_connected = false;
                }

                if (is_connected) {
                    outputStream = socket.getOutputStream();
                    inputStream = socket.getInputStream();
                }
            } else {
                value += "No Devices found";
                bluetoothDeviceList = new ArrayList<>();
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
                return false;
            }
        } catch (Exception ex) {
            value = ex.toString() + "\n" + " InitPrinter \n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    public void setBluetoothDevice(String device_name) {
        this.bluetoothDeviceName = device_name;
        Params bParam = paramCatalog.getParamByName("bluetooth_device_name");
        if (bParam == null) {
            paramCatalog.addParam("bluetooth_device_name", device_name, "text", "Bluetooth device for printing");
        } else {
            bParam.setValue(device_name);
            paramCatalog.editParam(bParam);
        }
    }

    private void finishAndPrint() {
        try {
            Map<String, Object> mObj = new HashMap<String, Object>();
            mObj.put("invoice_id", sale.getServerInvoiceId());
            _complete_inv(mObj);
        } catch (Exception e){e.printStackTrace();}

        Intent intent = new Intent(PrintPreviewActivity.this, MainActivity.class);
        intent.putExtra("refreshStock", true);
        finish();
        startActivity(intent);
    }

    private void _complete_inv(Map mObj) {
        String _url = Server.URL + "transaction/complete?api-key=" + Server.API_KEY;
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
                                String server_invoice_number = jObj.getString("invoice_number");
                                sale.setServerInvoiceNumber(server_invoice_number);

                                saleLedger.setFinished(sale);
                                // and then trigger print the invoice
                                just_print(true);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }

    private void printHtml(String fileName, WebView webView) {
        if (PrintHelper.systemSupportsPrint()) {
            PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter();
            PrintManager printManager = (PrintManager) getSystemService(Context.PRINT_SERVICE);
            printManager.print(fileName, adapter, null);
        } else {
            Toast.makeText(this, "この端末では印刷をサポートしていません", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void just_print(Boolean animated) {
        try {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());

            Bitmap bm = screenShot(print_webview);
            //File file = saveBitmap(bm, sale.getServerInvoiceNumber() +".png");

            Boolean is_connect = initPrinter();

            if (is_connect) {
                byte[] printformat = {0x1B, 0x21, 0x10};
                outputStream.write(printformat);

                try {
                    Bitmap bitmap = bm;
                    Log.e(getClass().getSimpleName(), "old bitmap.getHeight() : " + bitmap.getHeight());
                    Log.e(getClass().getSimpleName(), "old bitmap.getWidth() : " + bitmap.getWidth());
                    float aspectRatio = bitmap.getWidth() /
                            (float) bitmap.getHeight();
                    int width = 400; //400 lebar kertas
                    int height = Math.round(width / aspectRatio);

                    bitmap = getResizedBitmap(bitmap, width, height);
                    Log.e(getClass().getSimpleName(), "bitmap.getHeight() : " + bitmap.getHeight());
                    Log.e(getClass().getSimpleName(), "bitmap.getWidth() : " + bitmap.getWidth());

                    PrintPic printPic = PrintPic.getInstance();
                    printPic.init(bitmap);
                    byte[] bitmapdata = printPic.printDraw();

                    outputStream.write(bitmapdata);

                    if (animated) {
                        Log.e(getClass().getSimpleName(), "finish_and_print_button.getVisibility() : "+ finish_and_print_button.getVisibility());
                        if (finish_and_print_button.getVisibility() == View.VISIBLE) {
                            finish_and_print_button.setBackgroundColor(getResources().getColor(R.color.grey_800));
                            finish_and_print_button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check_white_24dp), null, null, null);
                            finish_and_print_button.setPadding(10, 0, 0, 0);
                        } else {
                            print_button.setBackgroundColor(getResources().getColor(R.color.grey_800));
                            print_button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check_white_24dp), null, null, null);
                            print_button.setPadding(10, 0, 0, 0);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String encodeTobase64(Bitmap image) {
        Bitmap immagex = image;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        immagex.compress(Bitmap.CompressFormat.PNG, 90, baos);
        byte[] b = baos.toByteArray();
        String imageEncoded = Base64.encodeToString(b, Base64.DEFAULT);
        return imageEncoded;
    }

    private Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
