package com.slightsite.app.ui.printer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

public class PrinterActivity extends AppCompatActivity {
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
    String bluetoothDeviceName = ""; //"58Printer";
    ArrayList<String> bluetoothDeviceList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);

        InitDeviceList();

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
        PrintPreviewFragment PreviewFr = new PrintPreviewFragment();
        PreviewFr.setArguments(bundle);

        adapter.addFrag(ListViewFr, getResources().getString(R.string.title_list_printer));
        adapter.addFrag(PreviewFr, getResources().getString(R.string.title_print_preview));
        viewPager.setAdapter(adapter);
    }

    public void setBluetoothDeviceName(String bDeviceName) {
        this.bluetoothDeviceName = bDeviceName;
    }

    public String getFormatedReceipt() {
        String[] name = {"Chapati", "Chicken Chettinad Full", "Chicked Mushroom", "Middle Burger"};
        int[] price = {70000, 13000, 17000, 10000};
        int[] quantity = {1, 1, 3, 10};

        String res = "\n";
        res += String.format("%s%n", centerString(32, "FOOD GARDEN"));
        res += String.format("%s%n", centerString(32,"JOGJA CITY MALL"));
        res += String.format("%s%n", centerString(32,"Jl. Magelang Km 5,8 No. 18"));
        res += String.format("%s%n", centerString(32,"Sinduadi Mlati, Sleman"));
        res += String.format("%s%n", centerString(32,"Yogyakarta"));
        res += String.format("%s%n%n", centerString(32,"(0274) - 530 4250"));

        res += String.format("%s%n", str_repeat("=", 32));
        res += String.format("%1$-7s %2$-4s %3$-10s%n", "Tanggal", ":", "26-12-2018");
        res += String.format("%1$-7s %2$-4s %3$-10s%n", "Jam", ":", "23:09:15");
        res += String.format("%1$-7s %2$-4s %3$-10s%n", "Kasir", ":", "John Due");
        res += String.format("%s%n%n", str_repeat("=", 32));
        for (int i = 0; i < name.length; ++i) {
            res += String.format("%-4s%n", name[i]);
            res += String.format("%4d %2s %,4d %,10d%n%n", quantity[i], "x", price[i], price[i] * quantity[i]);
        }
        res += String.format("%s%n", str_repeat("=", 32));
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "Subtotal", ":", 125000);
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "PPN", ":", 0);
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "Grand Total", ":", 125000);
        res += String.format("%1$-12s %2$-4s %3$,-2d%n", "Cash", ":", 150000);
        res += String.format("%1$-12s %2$-4s %3$,-2d%n%n", "Kembali", ":", 25000);

        res += String.format("%s%n", centerString(32, "Terimakasih"));
        res += String.format("%s%n%n", centerString(32, "Selamat belanja kembali"));
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
}
