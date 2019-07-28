package com.slightsite.app.ui.printer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

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
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.LoginActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PrinterActivity extends AppCompatActivity {
    public static final String TAG = PrinterActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    ProgressDialog pDialog;
    int success;

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
    private ParamCatalog paramCatalog;

    private SharedPreferences sharedpreferences;
    private ContentValues adminData;

    private Sale sale;
    private int saleId;
    private SaleLedger saleLedger;
    private PaymentCatalog paymentCatalog;
    private WarehouseCatalog warehouseCatalog;
    private CustomerCatalog customerCatalog;
    private List<Payment> paymentList;
    private List<Map<String, String>> lineitemList;
    private int warehouse_id;
    private Warehouses warehouse;
    private Customer customer;
    private JSONObject server_invoice_data;

    private PrintPreviewFragment PreviewFr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        InitDeviceList();
        InitPrinterConfigs();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        // geting the transaction data
        try {
            saleId = Integer.parseInt(getIntent().getExtras().get("saleId").toString());
            saleLedger = SaleLedger.getInstance();
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

            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //Tools.setSystemBarColor(this, android.R.color.white);
        //Tools.setSystemBarLight(this);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
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
                try {
                    PreviewFr.shareInvoice();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void IntentPrint(String txtvalue) {
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

    //Setting View Pager
    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putStringArrayList("bluetoothDeviceList", bluetoothDeviceList);
        ListViewFragment ListViewFr = new ListViewFragment();
        ListViewFr.setArguments(bundle);
        // preview fragment
        PreviewFr = new PrintPreviewFragment();
        PreviewFr.setArguments(bundle);
        // config fragment
        ConfigsFragment ConfigFr = new ConfigsFragment();
        ConfigFr.setArguments(bundle);

        adapter.addFrag(PreviewFr, getResources().getString(R.string.title_print_preview));
        adapter.addFrag(ListViewFr, getResources().getString(R.string.title_list_printer));
        adapter.addFrag(ConfigFr, getResources().getString(R.string.title_print_config));
        viewPager.setAdapter(adapter);
    }

    public void setBluetoothDeviceName(String bDeviceName) {
        this.bluetoothDeviceName = bDeviceName;
    }

    public String getFormatedReceipt() {
        // geting the transaction data
        try {
            saleId = Integer.parseInt(getIntent().getExtras().get("saleId").toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            saleLedger = SaleLedger.getInstance();
            paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
            if (saleId > 0) {
                paymentList = paymentCatalog.getPaymentBySaleId(saleId);
            }

            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();

            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam instanceof Params) {
                warehouse_id = Integer.parseInt(whParam.getValue());
                warehouse = warehouseCatalog.getWarehouseByWarehouseId(warehouse_id);
            }
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        int char_length = Integer.parseInt(printerConfigs.get("char_length"));

        String res = "\n";
        if (warehouse != null) {
            Params store_name = paramCatalog.getParamByName("store_name");
            if (store_name instanceof Params) {
                res += String.format("%s%n", store_name.getValue() +" "+warehouse.getTitle());
            } else {
                res += String.format("%s%n", warehouse.getTitle());
            }
            res += String.format("%s%n", warehouse.getAddress());
            res += String.format("%s%n", warehouse.getPhone());
        } else {
            res += printerConfigs.get("header");
        }

        res += String.format("%s%n", str_repeat("=", char_length));

        String current_time =  DateTimeStrategy.getCurrentTime();

        sale = saleLedger.getSaleById(saleId);
        Double sale_total = 0.00;
        Double amount_tendered = 0.00;
        try {
            current_time = sale.getEndTime();
            sale_total = sale.getTotal();
        } catch (Exception e) { e.printStackTrace(); }

        String[] separated = current_time.split(" ");
        res += String.format("%1$-7s %2$-4s %3$-10s%n",
                getResources().getString(R.string.label_date), ":", separated[0]);
        String date_transaction = sale.getEndTime();
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MMM-dd  hh:mm a");
            date_transaction = DateTimeStrategy.parseDate(sale.getEndTime(), "yyMMdd");

        } catch (Exception e) {

        }

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
        String admin_id = ParamsController.getInstance().getParam("admin_id");

        String no_nota = date_transaction+"/"+sale.getId();
        if (admin_id != null) {
            //no_nota = date_transaction+"/"+ sharedpreferences.getString(LoginActivity.TAG_ID, null) +"/"+sale.getId();
            no_nota = date_transaction+"/"+ admin_id +"/"+sale.getId();
        }

        res += String.format("%1$-7s %2$-4s %3$-10s%n",
                getResources().getString(R.string.label_no_nota), ":", no_nota);
        res += String.format("%1$-7s %2$-4s %3$-10s%n",
                getResources().getString(R.string.label_hour), ":", separated[1]);


        if (adminData != null) {
            res += String.format("%1$-7s %2$-4s %3$-10s%n",
                    getResources().getString(R.string.label_cashier), ":", adminData.getAsString(LoginActivity.TAG_NAME));
        }

        List<LineItem> list = sale.getAllLineItem();
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            lineitemList.add(line.toMap());
        }

        res += String.format("%s%n%n", str_repeat("=", char_length));
        int sub_total = 0;
        int ppn = 0;
        for (int i = 0; i < lineitemList.size(); ++i) {
            res += String.format("%-4s%n", lineitemList.get(i).get("name"));
            int qty = Integer.parseInt(lineitemList.get(i).get("quantity"));
            int prc = Integer.parseInt(lineitemList.get(i).get("price").replace(".", ""));
            int tot = prc * qty;
            res += String.format("%4d %2s %,4d %,10d%n%n",
                    qty, "x",
                    prc,
                    tot);
            sub_total = sub_total + tot;
        }
        res += String.format("%s%n", str_repeat("=", char_length));
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "Subtotal", ":", sub_total);
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "PPN", ":", ppn);
        int grand_total = sub_total + ppn;
        if (sale_total > 0) {
            Double myDouble = Double.valueOf(sale_total);
            Integer int_sale_total = Integer.valueOf(myDouble.intValue());
            if (grand_total != int_sale_total) {
                grand_total = int_sale_total;
            }
        }

        int cash = grand_total;
        int change_due = grand_total - cash;
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "Grand Total", ":", grand_total);

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
                res += String.format("%1$-12s %2$-4s %3$,-2d%n",
                        getResources().getString(getPaymentChannel(py.getPaymentChannel())), ":", amnt);
            }
            change_due = payment_total - grand_total;
        } else {
            res += String.format("%1$-12s %2$-4s %3$,-2d%n",
                    getResources().getString(R.string.payment_cash), ":", cash);
        }

        if (change_due >= 0) {
            res += String.format("%1$-12s %2$-4s %3$,-2d%n%n",
                    getResources().getString(R.string.label_change_due), ":", change_due);
        } else {
            res += String.format("%1$-12s %2$-4s %3$,-2d%n%n",
                    getResources().getString(R.string.label_dept), ":", change_due);
        }

        res += printerConfigs.get("footer");
        res += "\n";

        return res;
    }

    public String getFormatedReceiptHtml() {
        String res = "<style>p, table td{font-size:14px !important;}</style>";
        if (warehouse != null) {
            Params store_name = paramCatalog.getParamByName("store_name");
            if (store_name instanceof Params) {
                res += "<table width=\"100%\" style=\"margin-top:30px;\"><tr><td><center><b>"+ store_name.getValue() +" "+warehouse.getTitle() +"</b></center></td></tr>";
            } else {
                res += String.format("%s%n", warehouse.getTitle());
            }
            res += "<tr><td><center>"+ warehouse.getAddress() +"</center></td></tr>";
            res += "<tr><td><center>"+ warehouse.getPhone() +"</center></td></tr></table>";
        } else {
            res += printerConfigs.get("header");
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
        adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
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
            res += "<tr><td>"+ getResources().getString(R.string.label_customer_name)+ "</td><td> : "+ customer.getName() +"</td></tr>";
            res += "<tr><td>"+ getResources().getString(R.string.label_customer_address)+ "</td><td> : "+ customer.getAddress() +"</td></tr>";
            res += "<tr><td>"+ getResources().getString(R.string.label_customer_phone)+ "</td><td> : "+ customer.getPhone() +"</td></tr>";
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
        /*if (sale_total > 0) {
            Double myDouble = Double.valueOf(sale_total);
            Integer int_sale_total = Integer.valueOf(myDouble.intValue());
            if (grand_total != int_sale_total) {
                grand_total = int_sale_total;
            }
        }*/

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
            res += "<tr><td colspan=\"2\" style=\"text-align:right;\">"+ getResources().getString(R.string.label_dept) +" :</td>" +
                    "<td style=\"text-align:right;\">"+ CurrencyController.getInstance().moneyFormat(debt) +"</td>";
        }
        res += "</table>";

        res += "<table style=\"width:100%;margin-top:30px;\"><tr><td><center>"+ printerConfigs.get("footer") +"</center></td></tr></table>";

        return res;
    }

    private static String str_repeat(String val, int count){
        StringBuilder buf = new StringBuilder(val.length() * count);
        while (count-- > 0) {
            buf.append(val);
        }
        return buf.toString();
    }

    public static String centerString (int width, String s) {
        return String.format("%-" + width  + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
    }

    public void InitPrinterConfigs() {
        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        int char_length = 32;
        printerConfigs.put("char_length", char_length+"");
        // header
        String header = "\n";
        if (paramCatalog.getParamByName("store_name") != null)
            header += String.format("%s%n", centerString(char_length, paramCatalog.getParamByName("store_name").getValue()));
        else
            header += String.format("%s%n", centerString(char_length, "UCOK DURIAN"));

        if (paramCatalog.getParamByName("store_address") != null)
            header += String.format("%s%n", centerString(char_length, paramCatalog.getParamByName("store_address").getValue()));
        else
            header += String.format("%s%n", centerString(char_length, "Please set the store address"));

        if (paramCatalog.getParamByName("store_address2") != null)
            header += String.format("%s%n", centerString(char_length, paramCatalog.getParamByName("store_address2").getValue()));
        else
            header += String.format("%s%n", centerString(char_length, "Please set the store address 2"));

        if (paramCatalog.getParamByName("store_phone") != null)
            header += String.format("%s%n", centerString(char_length, paramCatalog.getParamByName("store_phone").getValue()));
        else
            header += String.format("%s%n", centerString(char_length, "(0000) 000 000"));

        printerConfigs.put("header", header);
        // footer
        String footer = "\n";
        footer += String.format("%s%n", centerString(32, "Terimakasih"));
        footer += String.format("%s%n%n", centerString(32, "Selamat belanja kembali"));
        printerConfigs.put("footer", footer);
    }

    public Map getPrinterConfigs() {
        return printerConfigs;
    }

    public static String parseDate(String inputDateString, SimpleDateFormat inputDateFormat, SimpleDateFormat outputDateFormat) {
        Date date = null;
        String outputDateString = null;
        try {
            date = inputDateFormat.parse(inputDateString);
            outputDateString = outputDateFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return outputDateString;
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

    public void buildDataFromServer(final WebView print_webview) {
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

                                // setup the sale
                                sale.setServerInvoiceNumber(server_invoice_data.getString("invoice_number"));
                                sale.setCreatedBy(server_invoice_data.getInt("created_by"));
                                sale.setCreatedByName(server_invoice_data.getString("created_by_name"));
                                sale.setPaidBy(server_invoice_data.getInt("paid_by"));
                                sale.setPaidByName(server_invoice_data.getString("paid_by_name"));
                                sale.setRefundedBy(server_invoice_data.getInt("refunded_by"));
                                sale.setRefundedByName(server_invoice_data.getString("refunded_by_name"));

                                String formated_receipt = getFormatedReceiptHtml();

                                print_webview.loadDataWithBaseURL(null, "<html><body>"+ formated_receipt +"</body></html>", "text/html", "utf-8", null);
                                //print_webview.setVerticalScrollBarEnabled(false);
                                print_webview.setVisibility(View.VISIBLE);

                                PreviewFr.setInvoiceNumber(sale.getServerInvoiceNumber());
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }
}
