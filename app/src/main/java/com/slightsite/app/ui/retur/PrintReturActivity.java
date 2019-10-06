package com.slightsite.app.ui.retur;

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
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetDialog;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
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
import com.slightsite.app.domain.retur.Retur;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.printer.GridListAdapter;
import com.slightsite.app.ui.sale.SaleDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

@SuppressLint("ClickableViewAccessibility")
public class PrintReturActivity extends Activity {

    private static final String TAG = PrintReturActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    ProgressDialog pDialog;
    int success;

    private Retur retur;
    private Sale sale;
    private int serverInvoiceId;

    private ParamCatalog paramCatalog;
    private WarehouseCatalog warehouseCatalog;
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

    private int screen_width = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            if (getIntent().hasExtra("retur_intent")) {
                retur = (Retur) getIntent().getSerializableExtra("retur_intent");
                serverInvoiceId = retur.getServerInvoiceId();
                Log.e(getClass().getSimpleName(), "retur items : "+ retur.getItems().toString());
                customer = retur.getCustomer();
                Log.e(getClass().getSimpleName(), "customer : "+ customer.toMap().toString());
            }

            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();

            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }

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

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        screen_width = size.x;

        Log.e(getClass().getSimpleName(), "screen_width : "+ screen_width);
        if (screen_width > 900) {
            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(640, LinearLayout.LayoutParams.WRAP_CONTENT);
            print_webview.setLayoutParams(params);
        }

        home_button = (Button) findViewById(R.id.home_button);
        print_button = (Button) findViewById(R.id.print_button);

        print_button_container = (LinearLayout) findViewById(R.id.print_button_container);
        finish_and_print_button = (Button) findViewById(R.id.finish_and_print_button);

        try {
            buildDataFromServer();
            /*print_webview.loadDataWithBaseURL(null, "<html><body>Loading ...</body></html>", "text/html", "utf-8", null);
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
            print_webview.loadDataWithBaseURL(null, "<html>"+ style +"<body>" + formated_receipt + "</body></html>", "text/html", "utf-8", null);*/
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initTriggerButton() {
        home_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(PrintReturActivity.this, MainActivity.class);
                intent.putExtra("refreshStock", true);
                finish();
                startActivity(intent);
            }
        });

        print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //just_print(false);
            }
        });

        print_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    print_button.setBackgroundColor(getResources().getColor(R.color.grey_800));
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    print_button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check_white_24dp), null, null, null);
                    print_button.setPadding(10, 0, 0, 0);
                }
                return false;
            }
        });

        finish_and_print_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //finishAndPrint();
            }
        });

        finish_and_print_button.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    finish_and_print_button.setBackgroundColor(getResources().getColor(R.color.grey_800));
                } else if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    finish_and_print_button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check_white_24dp), null, null, null);
                    finish_and_print_button.setPadding(10, 0, 0, 0);
                }
                return false;
            }
        });
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
                //res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + sale.getDeliveredByName() + "</td></tr>";
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
            }
        } else {
            if (adminData != null) {
                res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.label_processed_by) + "</td><td colspan=\"3\"> : " + adminData.getAsString(LoginActivity.TAG_NAME) + "</td></tr>";
            }
        }

        if (customer != null) {
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.customer)+ "</td><td colspan=\"3\"> : "+ customer.getName() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_address)+ "</td><td colspan=\"3\"> : "+ customer.getAddress() +"</td></tr>";
            res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_customer_phone)+ "</td><td colspan=\"3\"> : "+ customer.getPhone() +"</td></tr>";
        }

        /*if (sale.getPaidBy() > 0) {
            res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.status) + "</td><td colspan=\"3\"> : <b>" +
                    getResources().getString(R.string.message_paid_delivered) + "</b></td></tr>";
        } else {
            res += "<tr class=\"ft-17\"><td>" + getResources().getString(R.string.status) + "</td><td colspan=\"3\"> : <b style=\"color:red;\" class=\"ft-26\">" + getResources().getString(R.string.message_unpaid) + "</b></td></tr>";
        }*/

        res += "<tr><td colspan=\"4\"><hr/></td></tr>";
        res += "<tr><td colspan=\"4\"><b>Pengembalian Dana & Ganti Barang</b></td></tr>";
        res += "<tr><td colspan=\"4\"><hr/></td></tr></table>";
        res += "<table width=\"100%\" style=\"margin-bottom:25px;\">";

        List<Map<String, String >> list = retur.getItems();
        int sub_total = 0;
        for (Map<String, String> entry : list) {
            res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ entry.get("title") +"</td></tr>";
            int qty = Integer.parseInt(entry.get("quantity"));
            int change_qty = Integer.parseInt(entry.get("change_item"));
            int refund_qty = qty - change_qty;
            String str_price = entry.get("price");
            if (str_price.contains(".")) {
                str_price = str_price.substring(0, str_price.indexOf("."));
            }
            int prc = Integer.parseInt(str_price);
            int tot = prc * refund_qty;
            if (refund_qty > 0) {
                res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">" + refund_qty + " x " + CurrencyController.getInstance().moneyFormat(prc) + "</td>";
                res += "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(tot) + "</td></tr>";
            }
            if (change_qty > 0) {
                res += "<tr class=\"ft-17\"><td colspan=\"4\" style=\"padding-left:10px;\">" + change_qty + " pcs tukar barang</td>";
            }

            sub_total = sub_total + tot;
        }

        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

        int grand_total = sub_total;

        int cash = grand_total;
        int change_due = grand_total - cash;
        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.total) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(grand_total) +"</td>";

        res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";
        res += "<tr><td colspan=\"4\" style=\"text-align:right;\"><b>Cara Pembayaran</b></td></tr>";
        res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";

        List<Map<String, String >> payments = retur.getPayment();
        Log.e(getClass().getSimpleName(), "payments : "+ payments.toString());
        for (Map<String, String> payment : payments) {
            int amnt = 0;
            String amnt_str = payment.get("amount_tendered");
            try {
                if (amnt_str.contains(".")) {
                    //amnt_str = amnt_str.replace(".", "");
                    amnt_str = amnt_str.substring(0, amnt_str.indexOf("."));
                }
                if (amnt_str != "0" || amnt_str != "0.0") {
                    amnt = Integer.parseInt(amnt_str);
                }
            } catch (Exception e){
                Log.e(getClass().getSimpleName(), e.getMessage());
            }

            if (amnt > 0) {
                try {
                    String pay_channel = payment.get("type");
                    res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">" + getResources().getString(getPaymentChannel(pay_channel)) + " :</td>" +
                            "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(amnt) + "</td>";
                } catch (Exception e){e.printStackTrace();}
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

    private BottomSheetDialog bottomSheetDialog;
    private ListView print_list_view;

    private void printerSetting() {
        InitDeviceList();

        bottomSheetDialog = new BottomSheetDialog(PrintReturActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_bluetooth_devices, null);
        bottomSheetDialog.setContentView(sheetView);

        print_list_view = (ListView) sheetView.findViewById(R.id.print_list_view);
        GridListAdapter badapter = new GridListAdapter(PrintReturActivity.this, bluetoothDeviceList, true);
        int selected = bluetoothDeviceList.indexOf(bluetoothDeviceName);
        if (selected >= 0) {
            badapter.setSelectedPosition(selected);
        }
        print_list_view.setAdapter(badapter);

        bottomSheetDialog.show();
    }

    private int counter = 0;
    private int is_delivered = 0;

    private void buildDataFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        if (sharedpreferences == null) {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        }

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("invoice_id", serverInvoiceId+"");

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
                                /*if (server_invoice_data.has("shipping")) {
                                    JSONArray arr_shipping = server_invoice_data.getJSONArray("shipping");
                                    JSONObject obj_shipping = arr_shipping.getJSONObject(0);
                                }*/
                                if (server_invoice_data.has("created_by") && !server_invoice_data.getString("created_by").toString().equals("null")) {
                                    // setup the sale
                                    sale = new Sale(server_invoice_data.getInt("id"), server_invoice_data.getString("created_at"));
                                    sale.setServerInvoiceNumber(server_invoice_data.getString("invoice_number"));
                                    sale.setCreatedBy(server_invoice_data.getInt("created_by"));
                                    sale.setCreatedByName(server_invoice_data.getString("created_by_name"));
                                    sale.setPaidBy(server_invoice_data.getInt("paid_by"));
                                    sale.setPaidByName(server_invoice_data.getString("paid_by_name"));
                                    sale.setRefundedBy(server_invoice_data.getInt("refunded_by"));
                                    sale.setRefundedByName(server_invoice_data.getString("refunded_by_name"));
                                    sale.setDeliveredByName(server_invoice_data.getString("delivered_by_name"));

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
                                        is_delivered = delivered;

                                        if (status > 0 && delivered > 0) {
                                            print_button_container.setVisibility(View.VISIBLE);
                                            finish_and_print_button.setVisibility(View.GONE);
                                        }
                                    }
                                } else {
                                    counter = counter + 1;
                                    if (counter <= 5) {
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
