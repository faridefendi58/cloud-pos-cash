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
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.design.widget.BottomSheetDialog;
import android.util.ArrayMap;
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
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.printer.GridListAdapter;
import com.slightsite.app.ui.printer.PrintPic;
import com.slightsite.app.ui.sale.SaleDetailActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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

        print_button_container.setVisibility(View.GONE);
        finish_and_print_button.setVisibility(View.GONE);

        try {
            buildDataFromServer();
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
                just_print(false);
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
                finishAndPrint();
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
            //current_time = sale.getEndTime();
            current_time = DateTimeStrategy.parseDate(current_time, "dd MMM yyyy hh:mm");
            sale_total = sale.getTotal();
        } catch (Exception e) { e.printStackTrace(); }

        //String[] separated = current_time.split(" ");
        res += "<tr class=\"ft-18\"><td>"+ getResources().getString(R.string.label_date)+ "</td>" +
                "<td colspan=\"3\" class=\"ft-17\"> : "+ current_time +"</td>";
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
            if (no_nota.contains("PAID")) {
                no_nota = no_nota.replace("PAID", "REFUND");
            }
        } else {
            if (admin_id != null) {
                no_nota = date_transaction + "/" + admin_id + "/" + sale.getId();
            }
        }

        res += "<tr class=\"ft-17\"><td>"+ getResources().getString(R.string.label_no_nota)+ "</td><td colspan=\"3\"> : "+ no_nota +"</td></tr>";

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

        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

        List<Map<String, String >> list = retur.getItems();
        int sub_total = 0;
        Map<String,Integer> list_tukar_barang = new HashMap<>();
        for (Map<String, String> entry : list) {
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

            sub_total = sub_total + tot;
        }

        if (list_tukar_barang.size() > 0) {
            res += "<tr><td colspan=\"4\"><b>Penukaran Barang</b></td></tr>";
            res += "<tr><td colspan=\"4\"><hr/></td></tr></table>";
            res += "<table width=\"100%\" style=\"margin-bottom:25px;\">";
            for (Map.Entry<String, Integer> tb_entry : list_tukar_barang.entrySet()) {
                res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ tb_entry.getKey() +"</td></tr>";
                res += "<tr class=\"ft-17\"><td colspan=\"4\" style=\"padding-left:10px;\">" + tb_entry.getValue() + " pcs tukar barang</td>";
            }
        } else {
            res += "</table><table width=\"100%\" style=\"margin-bottom:25px;\">";
        }

        if (sub_total > 0) {
            if (list_tukar_barang.size() > 0) {
                res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";
            }
            res += "<tr><td colspan=\"4\"><b>Pengembalian Uang</b></td></tr>";
            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
            for (Map<String, String> entry2 : list) {
                int qty = Integer.parseInt(entry2.get("quantity"));
                int change_qty = Integer.parseInt(entry2.get("change_item"));
                int refund_qty = qty - change_qty;
                String str_price = entry2.get("price");
                if (str_price.contains(".")) {
                    str_price = str_price.substring(0, str_price.indexOf("."));
                }
                int prc = Integer.parseInt(str_price);
                int tot = prc * refund_qty;
                if (refund_qty > 0) {
                    res += "<tr class=\"ft-17\"><td colspan=\"4\">" + entry2.get("title") + "</td></tr>";
                    res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">" + refund_qty + " x " + CurrencyController.getInstance().moneyFormat(prc) + "</td>";
                    res += "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(tot) + "</td></tr>";
                }
            }
        }

        res += "<tr><td colspan=\"4\"><hr/></td></tr>";

        int grand_total = sub_total;

        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">"+ getResources().getString(R.string.total_refund) +" :</td>" +
                "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(grand_total) +"</td>";

        List<Map<String, String >> list_change = retur.getItemsChange();
        if (list_change.size() > 0) {
            res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";
            res += "<tr><td colspan=\"4\" style=\"text-align:left;\"><b>Penukaran Dengan Item Lain</b></td></tr>";
            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
            int tot_ctot = 0;
            for (Map<String, String> c_entry : list_change) {
                res += "<tr class=\"ft-17\"><td colspan=\"4\">"+ c_entry.get("title") +"</td></tr>";
                int cqty = Integer.parseInt(c_entry.get("quantity"));
                String str_price = c_entry.get("price");
                if (str_price.contains(".")) {
                    str_price = str_price.substring(0, str_price.indexOf("."));
                }
                int cprc = Integer.parseInt(str_price);
                int ctot = cprc * cqty;
                tot_ctot = tot_ctot + ctot;
                res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"padding-left:10px;\">" + c_entry.get("quantity") + " x -"+ CurrencyController.getInstance().moneyFormat(cprc) +"</td>";
                res += "<td style=\"text-align:right;\">-" + CurrencyController.getInstance().moneyFormat(ctot) + "</td></tr>";
            }
            res += "<tr><td colspan=\"4\"><hr/></td></tr>";
            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">Total Harga :</td>" +
                    "<td style=\"text-align:right;\">-" + CurrencyController.getInstance().moneyFormat(tot_ctot) + "</td>";
            int sisa = grand_total - tot_ctot;
            res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">Sisa Pengembalian :</td>" +
                    "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(sisa) + "</td>";
        }

        if (grand_total > 0) {
            res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";
            res += "<tr><td colspan=\"4\" style=\"text-align:right;\"><b>Cara Pengembalian</b></td></tr>";
            res += "<tr><td colspan=\"4\">&nbsp;</td></tr>";

            List<Map<String, String>> payments = retur.getPayment();
            Log.e(getClass().getSimpleName(), "payments : " + payments.toString());
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
                } catch (Exception e) {
                    Log.e(getClass().getSimpleName(), e.getMessage());
                }

                if (amnt > 0) {
                    try {
                        String pay_channel = payment.get("type");
                        res += "<tr class=\"ft-17\"><td colspan=\"3\" style=\"text-align:right;\">" + getResources().getString(getPaymentChannel(pay_channel)) + " :</td>" +
                                "<td style=\"text-align:right;\">" + CurrencyController.getInstance().moneyFormat(amnt) + "</td>";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
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

                                        print_button_container.setVisibility(View.GONE);
                                        finish_and_print_button.setText(getResources().getString(R.string.button_finish_retur_and_print));
                                        finish_and_print_button.setVisibility(View.VISIBLE);
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
                            //finish_and_print_button.startAnimation(animate);

                            finish_and_print_button.setBackgroundColor(getResources().getColor(R.color.grey_800));
                            finish_and_print_button.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.ic_check_white_24dp), null, null, null);
                            finish_and_print_button.setPadding(10, 0, 0, 0);
                        } else {
                            //print_button.startAnimation(animate);

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

    private Boolean is_finished = false;

    private void finishAndPrint() {
        if (!is_finished) {
            try {
                Map<String, Object> mObj = new HashMap<String, Object>();
                mObj.put("invoice_id", serverInvoiceId);

                List<Map<String, String >> list = retur.getItems();
                ArrayList arrItems = new ArrayList();
                for (Map<String, String> entry : list) {
                    int qty = Integer.parseInt(entry.get("quantity"));
                    int change_qty = Integer.parseInt(entry.get("change_item"));
                    int refund_qty = qty - change_qty;
                    String str_price = entry.get("price");
                    if (str_price.contains(".")) {
                        str_price = str_price.substring(0, str_price.indexOf("."));
                    }
                    int prc = Integer.parseInt(str_price);
                    //int tot = prc * refund_qty;

                    Map<String, String> mItem = new HashMap<String, String>();
                    mItem.put("name", entry.get("title"));
                    mItem.put("total_qty", qty +"");
                    mItem.put("returned_qty", change_qty +"");
                    mItem.put("refunded_qty", refund_qty +"");
                    mItem.put("price", prc +"");
                    arrItems.add(mItem);
                }
                mObj.put("items", arrItems);

                ArrayList arrPaymentList = new ArrayList();

                List<Map<String, String>> payments = retur.getPayment();
                for (Map<String, String> payment : payments) {
                    int amnt = 0;
                    String amnt_str = payment.get("amount_tendered");
                    if (amnt_str.contains(".")) {
                        amnt_str = amnt_str.substring(0, amnt_str.indexOf("."));
                    }
                    Map<String, String> arrPayment = new HashMap<String, String>();
                    arrPayment.put("type", payment.get("type"));
                    arrPayment.put("amount", amnt_str);
                    arrPaymentList.add(arrPayment);
                }
                mObj.put("payments", arrPaymentList);

                List<Map<String, String >> change_item_list = retur.getItemsChange();
                if (change_item_list.size() > 0) {
                    ArrayList arrChangeItems = new ArrayList();
                    for (Map<String, String> entry : change_item_list) {
                        String str_price = entry.get("price");
                        if (str_price.contains(".")) {
                            str_price = str_price.substring(0, str_price.indexOf("."));
                        }
                        int prc = Integer.parseInt(str_price);

                        Map<String, String> mItem = new HashMap<String, String>();
                        mItem.put("name", entry.get("title"));
                        mItem.put("quantity", entry.get("quantity"));
                        mItem.put("quantity_total", entry.get("quantity_total"));
                        mItem.put("price", prc +"");
                        arrChangeItems.add(mItem);
                    }
                    mObj.put("items_change", arrChangeItems);
                }

                ArrayList<String> retur_reasons = retur.getItemsReason();
                if (retur_reasons.size() > 0) {
                    mObj.put("reasons", retur_reasons);
                }
                String retur_notes = retur.getNotes();
                if (retur_notes !=null && retur_notes.length() > 0) {
                    mObj.put("notes", retur_notes);
                }
                Log.e(getClass().getSimpleName(), "mObj : "+ mObj.toString());

                _create_retur_inv(mObj);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (finish_and_print_button.getVisibility() == View.VISIBLE) {
                finish_and_print_button.setText(getResources().getString(R.string.button_new_transaction));
                is_finished = true;
            }
        } else {
            Intent intent = new Intent(PrintReturActivity.this, MainActivity.class);
            intent.putExtra("refreshStock", true);
            finish();
            startActivity(intent);
        }
    }

    private void _create_retur_inv(Map mObj) {
        String _url = Server.URL + "transaction/refund?api-key=" + Server.API_KEY;
        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;
        Log.e(getClass().getSimpleName(), "url : "+ qry);

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
                                // and then trigger print the invoice
                                just_print(false);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }
}
