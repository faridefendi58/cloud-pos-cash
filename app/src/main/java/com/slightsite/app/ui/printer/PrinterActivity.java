package com.slightsite.app.ui.printer;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import com.slightsite.app.R;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.payment.PaymentCatalog;
import com.slightsite.app.domain.payment.PaymentService;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.LoginActivity;

public class PrinterActivity extends AppCompatActivity {
    public static final String TAG = PrinterActivity.class.getSimpleName();

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
    private List<Payment> paymentList;
    private List<Map<String, String>> lineitemList;
    private int cuk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        InitDeviceList();
        InitPrinterConfigs();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        setupViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager
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
        PrintPreviewFragment PreviewFr = new PrintPreviewFragment();
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
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        int char_length = Integer.parseInt(printerConfigs.get("char_length"));

        String res = "\n";
        res += printerConfigs.get("header");

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
        res += String.format("%1$-7s %2$-4s %3$-10s%n",
                getResources().getString(R.string.label_hour), ":", separated[1]);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
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

        if (!paymentList.isEmpty()) {
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
            change_due = grand_total - payment_total;
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
        result.put("nominal_edc", R.string.payment_edc);

        return result.get(channel);
    }
}
