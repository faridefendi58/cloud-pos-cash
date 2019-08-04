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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.slightsite.app.domain.inventory.LineItem;
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
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
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
    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    volatile boolean stopWorker;
    String value = "";
    String bluetoothDeviceName = "58Printer";
    ArrayList<String> bluetoothDeviceList;
    Map<String, String> printerConfigs = new HashMap<String, String>();

    private String formated_receipt;
    private int shipping_method = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            saleLedger = SaleLedger.getInstance();
            saleId = getIntent().getIntExtra("saleId", 0);
            sale = saleLedger.getSaleById(saleId);

            paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
            if (saleId > 0) {
                paymentList = paymentCatalog.getPaymentBySaleId(saleId);
            }

            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();

            sale = saleLedger.getSaleById(saleId);

            if (sale.getCustomerId() > 0) {
                customer = customerCatalog.getCustomerById(sale.getCustomerId());
            }

            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }

            shipping_method = getIntent().getIntExtra("shipping_method", 0);
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
                this.finish();
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

        /*print_webview.getSettings().setLoadWithOverviewMode(true);
        print_webview.getSettings().setUseWideViewPort(true);
        print_webview.getSettings().setMinimumFontSize(60);
        print_webview.getSettings().setSupportZoom(true);
        print_webview.getSettings().setBuiltInZoomControls(true);
        print_webview.getSettings().setDisplayZoomControls(true);*/

        try {
            buildDataFromServer();
        } catch (Exception e){
            e.printStackTrace();
        }

        home_button = (Button) findViewById(R.id.home_button);
        print_button = (Button) findViewById(R.id.print_button);

        print_button_container = (LinearLayout) findViewById(R.id.print_button_container);
        finish_and_print_button = (Button) findViewById(R.id.finish_and_print_button);

        if (shipping_method == 0) {
            print_button_container.setVisibility(View.GONE);
            finish_and_print_button.setVisibility(View.VISIBLE);
        }
    }

    private void initTriggerButton() {
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrintPreviewActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

        print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //IntentPrint(print_webview);
                Toast.makeText(getApplicationContext(),
                        "Will be available soon", Toast.LENGTH_LONG).show();
            }
        });
    }

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

                                if (server_invoice_data.has("created_by")) {
                                    // setup the sale
                                    sale.setServerInvoiceNumber(server_invoice_data.getString("invoice_number"));
                                    sale.setCreatedBy(server_invoice_data.getInt("created_by"));
                                    sale.setCreatedByName(server_invoice_data.getString("created_by_name"));
                                    sale.setPaidBy(server_invoice_data.getInt("paid_by"));
                                    sale.setPaidByName(server_invoice_data.getString("paid_by_name"));
                                    sale.setRefundedBy(server_invoice_data.getInt("refunded_by"));
                                    sale.setRefundedByName(server_invoice_data.getString("refunded_by_name"));

                                    formated_receipt = getFormatedReceiptHtml();

                                    print_webview.loadDataWithBaseURL(null, "<html><body>" + formated_receipt + "</body></html>", "text/html", "utf-8", null);
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
        String res = "<style>p, table td{font-size:14px !important;}</style>";
        if (warehouse != null) {
            Params store_name = paramCatalog.getParamByName("store_name");
            if (store_name instanceof Params) {
                res += "<table width=\"100%\" style=\"margin-top:40px;\"><tr><td><center><b>"+ store_name.getValue() +" "+warehouse.getTitle() +"</b></center></td></tr>";
            } else {
                res += String.format("%s%n", warehouse.getTitle());
            }
            res += "<tr><td><center>"+ warehouse.getAddress() +"</center></td></tr>";
            res += "<tr><td><center>"+ warehouse.getPhone() +"</center></td></tr></table>";
        }

        res += "<hr/>";

        String current_time =  DateTimeStrategy.getCurrentTime();

        Double sale_total = 0.00;
        Double amount_tendered = 0.00;
        try {
            current_time = sale.getEndTime();
            sale_total = sale.getTotal();
        } catch (Exception e) { e.printStackTrace(); }

        String[] separated = current_time.split(" ");
        res += "<table>";
        res += "<tr><td>"+ getResources().getString(R.string.label_date)+ "</td><td> : "+ separated[0] +"</td>";
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

        res += "<tr><td>"+ getResources().getString(R.string.label_no_nota)+ "</td><td> : "+ no_nota +"</td></tr>";
        res += "<tr><td>"+ getResources().getString(R.string.label_hour)+ "</td><td> : "+ separated[1] +"</td></tr>";

        if (sale.getCreatedBy() > 0) {
            res += "<tr><td>" + getResources().getString(R.string.label_created_by) + "</td><td> : " + sale.getCreatedByName() + "</td></tr>";
            if (sale.getPaidBy() > 0) {
                res += "<tr><td>" + getResources().getString(R.string.label_processed_by) + "</td><td> : " + sale.getPaidByName() + "</td></tr>";
            }
        } else {
            if (adminData != null) {
                res += "<tr><td>" + getResources().getString(R.string.label_created_by) + "</td><td> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
                res += "<tr><td>" + getResources().getString(R.string.label_processed_by) + "</td><td> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
            }
        }

        if (customer != null) {
            res += "<tr><td>"+ getResources().getString(R.string.customer)+ "</td><td> : "+ customer.getName() +"</td></tr>";
            res += "<tr><td>"+ getResources().getString(R.string.label_customer_address)+ "</td><td> : "+ customer.getAddress() +"</td></tr>";
            res += "<tr><td>"+ getResources().getString(R.string.label_customer_phone)+ "</td><td> : "+ customer.getPhone() +"</td></tr>";
        }

        if (sale.getPaidBy() > 0) {
            res += "<tr><td>" + getResources().getString(R.string.status) + "</td><td> : <b style=\"font-size:16px;\">" + getResources().getString(R.string.message_paid) + "</b></td></tr>";
        } else {
            res += "<tr><td>" + getResources().getString(R.string.status) + "</td><td> : <b style=\"color:red;font-size:16px;\">" + getResources().getString(R.string.message_unpaid) + "</b></td></tr>";
        }

        List<LineItem> list = sale.getAllLineItem();
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            lineitemList.add(line.toMap());
        }

        res += "</table>";
        res += "<hr/>";
        res += "<table width=\"100%\">";

        int sub_total = 0;
        int ppn = 0;
        for (int i = 0; i < lineitemList.size(); ++i) {
            res += "<tr><td colspan=\"2\">"+ lineitemList.get(i).get("name") +"</td></tr>";
            int qty = Integer.parseInt(lineitemList.get(i).get("quantity"));
            int prc = Integer.parseInt(lineitemList.get(i).get("price").replace(".", ""));
            int tot = prc * qty;
            res += "<tr><td style=\"padding-left:20px;\">"+ qty +" x "+ CurrencyController.getInstance().moneyFormat(prc) +"</td>";
            res += "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(tot) +"</td></tr>";

            sub_total = sub_total + tot;
        }
        res += "</table>";
        res += "<hr/>";

        res += "<table width=\"100%\" style=\"margin-top:10px;\">";
        res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_sub_total) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(sub_total) +"</td>";
        //res += "<tr><td colspan=\"2\" style=\"text-align:right;\">PPN :</td><td style=\"text-align:right;\">"+ ppn +"</td>";
        int discount = sale.getDiscount();
        res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_discount) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(discount) +"</td>";

        int grand_total = sub_total + ppn - discount;

        int cash = grand_total;
        int change_due = grand_total - cash;
        res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_grand_total) +" :</td>" +
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

                res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(getPaymentChannel(py.getPaymentChannel())) +" :</td>" +
                        "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(amnt) +"</td>";
            }
            change_due = payment_total - grand_total;
        } else {
            res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.payment_cash) +" :</td>" +
                    "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(cash) +"</td>";
        }

        if (change_due >= 0) {
            res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_change_due) +" :</td>" +
                    "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(change_due) +"</td>";
        } else {
            int debt = -1 * change_due;
            res += "<tr><td colspan=\"2\" style=\"text-align:right;\"><b>"+ getResources().getString(R.string.label_dept) +" :</b></td>" +
                    "<td style=\"text-align:right;\"><b>"+ CurrencyController.getInstance().moneyFormat(debt) +"</b></td>";
        }
        res += "</table>";

        res += "<table style=\"width:100%;margin-top:40px;margin-bottom:50px;\"><tr><td><center>Terimakasih.<br />Selamat belanja kembali.</center></td></tr></table>";

        return res;
    }

    public Integer getPaymentChannel(String channel) {
        Map<String, Integer> result = new HashMap<>();

        result.put("cash_receive", R.string.payment_cash);
        result.put("nominal_mandiri", R.string.payment_mandiri);
        result.put("nominal_bca", R.string.payment_bca);
        result.put("nominal_bri", R.string.payment_bri);
        result.put("nominal_edc", R.string.payment_edc);

        return result.get(channel);
    }

    private Bitmap screenShot(WebView view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
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

    private void printerSetting() {
        /*Intent newActivity = new Intent(PrintPreviewActivity.this,
                PrinterActivity.class);
        newActivity.putExtra("saleId", saleId);
        finish();
        startActivity(newActivity);*/
        Toast.makeText(this, "Will be available soon.", Toast.LENGTH_LONG).show();
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

    void beginListenForData() {
        try {
            final Handler handler = new Handler();

            // this is the ASCII code for a newline character
            final byte delimiter = 10;

            stopWorker = false;
            readBufferPosition = 0;
            readBuffer = new byte[1024];

            workerThread = new Thread(new Runnable() {
                public void run() {

                    while (!Thread.currentThread().isInterrupted() && !stopWorker) {

                        try {

                            int bytesAvailable = inputStream.available();

                            if (bytesAvailable > 0) {

                                byte[] packetBytes = new byte[bytesAvailable];
                                inputStream.read(packetBytes);

                                for (int i = 0; i < bytesAvailable; i++) {

                                    byte b = packetBytes[i];
                                    if (b == delimiter) {

                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(
                                                readBuffer, 0,
                                                encodedBytes, 0,
                                                encodedBytes.length
                                        );

                                        // specify US-ASCII encoding
                                        final String data = new String(encodedBytes, "US-ASCII");
                                        readBufferPosition = 0;

                                        // tell the user data were sent to bluetooth printer device
                                        handler.post(new Runnable() {
                                            public void run() {
                                                Log.d("e", data);
                                            }
                                        });

                                    } else {
                                        readBuffer[readBufferPosition++] = b;
                                    }
                                }
                            }

                        } catch (Exception ex) {
                            stopWorker = true;
                        }

                    }
                }
            });

            workerThread.start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void IntentPrint(WebView view) {
        String txtvalue = "";
        byte[] buffer = txtvalue.getBytes();
        byte[] PrintHeader = {(byte) 0xAA, 0x55, 2, 0};
        PrintHeader[3] = (byte) buffer.length;
        InitPrinter();
        if (PrintHeader.length > 128) {
            value += "\nValue is more than 128 size\n";
            Toast.makeText(this, value, Toast.LENGTH_LONG).show();
        } else {
            try {
                outputStream.write(txtvalue.getBytes());
                outputStream.close();
                socket.close();
            } catch (Exception ex) {
                value += ex.toString() + "\n" + "Excep IntentPrint \n";
                Toast.makeText(this, value, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void InitPrinter() {
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

                UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //Standard SerialPortService ID
                Method m = bluetoothDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) m.invoke(bluetoothDevice, 1);
                bluetoothAdapter.cancelDiscovery();
                socket.connect();
                outputStream = socket.getOutputStream();
                inputStream = socket.getInputStream();
                beginListenForData();
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
}
