package com.slightsite.app.ui.sale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
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
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.domain.shipping.ShippingService;
import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.domain.warehouse.AdminInWarehouseCatalog;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.printer.PrintPreviewActivity;
import com.slightsite.app.ui.printer.PrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class CheckoutActivity extends AppCompatActivity {

    private enum State {
        SHIPPING,
        PAYMENT,
        CONFIRMATION
    }

    State[] array_state = new State[]{State.SHIPPING, State.PAYMENT, State.CONFIRMATION};

    private View line_first, line_second;
    private ImageView image_shipping, image_payment, image_confirm;
    private TextView tv_shipping, tv_payment, tv_confirm, next_checkout_button;

    private int idx_state = 0;

    public Customer customer;
    public Shipping shipping;
    public Fragment current_fragment = null;
    public Checkout checkout_data;
    private Register register;
    private CustomerCatalog customerCatalog;
    private Vibrator vibe;

    ProgressDialog pDialog;
    int success;

    private static final String TAG = CheckoutActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private ParamCatalog paramCatalog;
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
    private JSONArray warehouse_data;
    private int current_warehouse_id = 0;
    private String current_warehouse_name = null;

    private String[] ship_methods = new String[]{};

    private SharedPreferences sharedpreferences;
    private SaleLedger saleLedger;
    private Sale sale;
    private int saleId;
    private List<Map<String, String>> lineitemList;
    private List<Payment> paymentList;
    private PaymentCatalog paymentCatalog;
    private WarehouseCatalog warehouseCatalog;
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private String admin_id;
    private ArrayList<Integer> allowed_warehouses = new ArrayList<Integer>();

    private Double change_due = 0.0;
    private HashMap<String, Bitmap> payment_receipts = new HashMap<String, Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            register = Register.getInstance();
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            saleLedger = SaleLedger.getInstance();

            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                current_warehouse_id = Integer.parseInt(whParam.getValue());
            }

            adminInWarehouseCatalog = AdminInWarehouseService.getInstance().getAdminInWarehouseCatalog();
            admin_id = paramCatalog.getParamByName("admin_id").getValue();
            if (admin_id != null) {
                List<AdminInWarehouse> adminInWarehouses = adminInWarehouseCatalog.getDataByAdminId(Integer.parseInt(admin_id));

                if (adminInWarehouses != null && adminInWarehouses.size() > 0) {
                    for (AdminInWarehouse aiw : adminInWarehouses) {
                        if (aiw.getWarehouseId() > 0) {
                            allowed_warehouses.add(aiw.getWarehouseId());
                        }
                    }
                }
            }

            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            getWarehouseList();
            if (register.getCurrentSale().getStatus().equals("ENDED")) {
                //Log.e(getClass().getSimpleName(), "register.getCustomer() : "+ register.getCustomer().toMap().toString());
                customer = register.getCustomer();
                //Log.e(getClass().getSimpleName(), "customer : "+ customer.toString());
                setCustomer(customer);
                // build the default payment if any
                if (register.getPaymentItems() != null) {
                    HashMap< String, String> banks = new HashMap<String, String>();
                    for(PaymentItem line : register.getPaymentItems()) {
                        if (line.getTitle().equals("cash_receive")) {
                            checkout_data.setCashReceive(line.getNominal().toString());
                        }
                        if (line.getTitle().equals("nominal_mandiri")
                                || line.getTitle().equals("nominal_bca")
                                || line.getTitle().equals("nominal_bri")) {
                            banks.put(line.getTitle(), line.getNominal().toString());
                        }
                    }

                    if (!banks.isEmpty()) {
                        checkout_data.setUseTransferBank(true);
                        checkout_data.setTransferBank(banks);
                    }
                    Log.e(getClass().getSimpleName(), "banks in checkout act :"+ banks.toString());
                }
            }

            ship_methods = AppController.getPaymentMethods();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_checkout);
        initToolbar();
        initComponent();

        displayFragment(State.SHIPPING);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (pDialog != null) {
            pDialog.dismiss();
            pDialog = null;
        }
    }

    private void initComponent() {
        line_first = (View) findViewById(R.id.line_first);
        line_second = (View) findViewById(R.id.line_second);
        image_shipping = (ImageView) findViewById(R.id.image_shipping);
        image_payment = (ImageView) findViewById(R.id.image_payment);
        image_confirm = (ImageView) findViewById(R.id.image_confirm);

        tv_shipping = (TextView) findViewById(R.id.tv_shipping);
        tv_payment = (TextView) findViewById(R.id.tv_payment);
        tv_confirm = (TextView) findViewById(R.id.tv_confirm);
        next_checkout_button = (TextView) findViewById(R.id.next_checkout_button);

        image_payment.setColorFilter(getResources().getColor(R.color.grey_10), PorterDuff.Mode.SRC_ATOP);
        image_confirm.setColorFilter(getResources().getColor(R.color.grey_10), PorterDuff.Mode.SRC_ATOP);

        vibe = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state == array_state.length - 1) {
                    // add customer first
                    if (customer.getId() <= 0) {
                        if (customer.getStatus() == 0) {
                            customer.setStatus(1);
                        }
                        int c_id = customerCatalog.createCustomer2(customer);
                        if (c_id > 0) {
                            customer = customerCatalog.getCustomerById(c_id);
                            register.setCustomer(customer);
                        }
                    }

                    String status = getResources().getString(R.string.message_paid);
                    Boolean lunas = true;
                    Sale current_sale = register.getCurrentSale();
                    Checkout checkout = getCheckoutData();
                    try {
                        Double tot_tagihan = current_sale.getTotal() - current_sale.getDiscount();
                        if (checkout.getTotalPaymentReceived() < tot_tagihan) {
                            status = getResources().getString(R.string.message_unpaid);
                            lunas = false;
                        }
                        if (checkout.getUseGoFood() || checkout.getUseGrabFood()) {
                            status = getResources().getString(R.string.message_paid);
                            lunas = true;
                        }
                    } catch (Exception e){e.printStackTrace();}

                    change_due = checkout.getChangeDue();

                    final int saleId = register.getCurrentSale().getId();
                    register.endSale(DateTimeStrategy.getCurrentTime());

                    pushInvoice(saleId);

                    /** gunakan info sukses yg beda antara lunas dan tidak lunas **/
                    /*if (lunas) {
                        new android.os.Handler().postDelayed(
                                new Runnable() {
                                    public void run() {
                                        Intent newActivity = new Intent(CheckoutActivity.this, PrintPreviewActivity.class);
                                        newActivity.putExtra("saleId", saleId);
                                        finish();
                                        startActivity(newActivity);
                                    }
                                },
                                3000);
                    } else {
                        showDialogPaymentSuccess(current_sale, checkout.getCustomer(), status);
                    }*/

                    /** langsung ke print preview **/
                    final int has_dept = (lunas)? 0 : 1;
                    final int shipping_method = checkout.getShipping().getMethod();
                    // jump to print preview
                    Intent newActivity = new Intent(CheckoutActivity.this, PrintPreviewActivity.class);
                    newActivity.putExtra("saleId", saleId);
                    newActivity.putExtra("hutang", has_dept);
                    newActivity.putExtra("shipping_method", shipping_method);
                    newActivity.putExtra("delayed", true);
                    finish();
                    startActivity(newActivity);

                    return;
                } else {
                    if (array_state[idx_state] == State.SHIPPING) {
                        // cek customer data dl
                        Log.e(TAG, "customer data on SHIPPING : "+ checkout_data.getCustomer().toMap().toString());
                        if (checkout_data.getCustomer().equals("null")
                                || checkout_data.getCustomer().getEmail() == "email@email.com"
                                || (checkout_data.getCustomer().getName().length() == 0)) {
                            Toast.makeText(getBaseContext(),
                                    getResources().getString(R.string.error_empty_customer_data), Toast.LENGTH_SHORT)
                                    .show();
                            vibe.vibrate(200);
                            return;
                        }
                        if (checkout_data.getShipping().getMethod() == 3
                                //|| checkout_data.getShipping().getMethod() == 4
                                //|| checkout_data.getShipping().getMethod() == 5
                        ) {
                            idx_state = idx_state + 1;
                        }
                        // not allowed empty shipping name phone and address for gosend (2), train cargo (7), plane cargo (8)
                        if (checkout_data.getShipping().getMethod() == 2
                                || checkout_data.getShipping().getMethod() == 7
                                || checkout_data.getShipping().getMethod() == 8) {
                            if (checkout_data.getShipping().getName() == null
                                    || checkout_data.getShipping().getPhone() == null
                                    || checkout_data.getShipping().getAddress() == null
                                    || checkout_data.getShipping().getDate() == null
                                    || checkout_data.getShipping().getPhone() == ""
                                    || checkout_data.getShipping().getAddress() == ""
                                    || checkout_data.getShipping().getName().length() == 0
                                    || checkout_data.getShipping().getPhone().length() == 0
                                    || checkout_data.getShipping().getAddress().length() == 0) {
                                Toast.makeText(getBaseContext(),
                                        getResources().getString(R.string.error_empty_shipping_customer_data), Toast.LENGTH_SHORT)
                                        .show();
                                vibe.vibrate(200);
                                return;
                            }
                        }

                        if (checkout_data.getShipping().getMethod() == 3) { // Tokopedia
                            if (checkout_data.getShipping().getDate() == null || checkout_data.getShipping().getInvoiceNumber() == null) {
                                Toast.makeText(getBaseContext(),
                                        getResources().getString(R.string.error_empty_shipping_date), Toast.LENGTH_SHORT)
                                        .show();
                                vibe.vibrate(
                                        200);
                                return;
                            }
                        }
                    } else if (array_state[idx_state] == State.PAYMENT) {
                        if (checkout_data.getTotalPaymentReceived() <= 0) {
                            //not allowed buy directly, buy and deposit not allow debt, 6 jul 20 dont allow debt
                            if (checkout_data.getShipping().getMethod() == 0) {
                                Toast.makeText(getBaseContext(),
                                        getResources().getString(R.string.error_payment_should_be_exact), Toast.LENGTH_LONG)
                                        .show();
                                vibe.vibrate(200);
                                return;
                            }
                            if (checkout_data.getShipping().getMethod() != 3) {
                                checkout_data.setCashReceive("0");
                            }

                            LayoutInflater inflater2 = getLayoutInflater();

                            View titleView = inflater2.inflate(R.layout.dialog_custom_title, null);
                            ((TextView) titleView.findViewById(R.id.dialog_title)).setText(getResources().getString(R.string.payment));
                            ((TextView) titleView.findViewById(R.id.dialog_content)).setText(getResources().getString(R.string.dialog_no_payment));

                            AlertDialog.Builder dialog = new AlertDialog.Builder(CheckoutActivity.this);
                            dialog.setCustomTitle(titleView);
                            dialog.setPositiveButton(getResources().getString(R.string.label_proceed), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    idx_state = idx_state + 1;
                                    displayFragment(State.CONFIRMATION);
                                }
                            });

                            dialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {

                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    displayFragment(State.PAYMENT);
                                }
                            });

                            dialog.show();
                            /*Toast.makeText(getBaseContext(),
                                    getResources().getString(R.string.error_empty_payment_data), Toast.LENGTH_SHORT)
                                    .show();*/

                            vibe.vibrate(200);
                            return;
                        }
                        try {
                            Boolean use_gofood = checkout_data.getUseGoFood();
                            Boolean use_grabfood = checkout_data.getUseGrabFood();
                            int total_tagihan_gograb = 0;
                            Boolean is_gograb_enough = true;
                            if (use_gofood) {
                                int tot_gofood_inv = Integer.parseInt(checkout_data.getTotalGoFoodInvoice());
                                int tot_gofood_discount = Integer.parseInt(checkout_data.getGofoodDiscount());
                                total_tagihan_gograb = tot_gofood_inv - tot_gofood_discount;
                                int _terbayar = Integer.parseInt(checkout_data.getWalletGoFood());
                                if (_terbayar < total_tagihan_gograb) {
                                    is_gograb_enough = false;
                                }
                            }
                            if (use_grabfood) {
                                int tot_grabfood_inv = Integer.parseInt(checkout_data.getTotalGrabFoodInvoice());
                                int tot_grabfood_discount = Integer.parseInt(checkout_data.getGrabfoodDiscount());
                                total_tagihan_gograb = tot_grabfood_inv - tot_grabfood_discount;
                                int _terbayar = Integer.parseInt(checkout_data.getWalletGrabFood());
                                if (_terbayar < total_tagihan_gograb) {
                                    is_gograb_enough = false;
                                }
                            }
                            if (use_gofood || use_grabfood) {
                                if (checkout_data.getTotalPaymentReceived() < total_tagihan_gograb) {
                                    Toast.makeText(getBaseContext(),
                                            getResources().getString(R.string.error_not_enough_payment), Toast.LENGTH_LONG)
                                            .show();

                                    vibe.vibrate(200);
                                    return;
                                } else if (checkout_data.getTotalPaymentReceived() > total_tagihan_gograb) {
                                    if (!checkout_data.getUseCash()) {
                                        Toast.makeText(getBaseContext(),
                                                getResources().getString(R.string.error_payment_should_be_exact), Toast.LENGTH_LONG)
                                                .show();

                                        vibe.vibrate(200);
                                        return;
                                    }
                                }
                                // check gograb pay should be more than tot inv
                                if (checkout_data.getUseCash() && !is_gograb_enough) {
                                    Toast.makeText(getBaseContext(),
                                            getResources().getString(R.string.error_payment_gograb_should_be_exact), Toast.LENGTH_LONG)
                                            .show();

                                    vibe.vibrate(200);
                                    return;
                                }
                            }
                        } catch (Exception e){e.printStackTrace();}
                        // check exact payment
                        try {
                            Double _tot_inv = register.getCurrentSale().getTotal() - register.getCurrentSale().getDiscount();
                            if (checkout_data.getUseEdc()) {
                                // do something
                                if (checkout_data.getTotalPaymentReceived() > _tot_inv) {
                                    Toast.makeText(getBaseContext(),
                                            getResources().getString(R.string.error_payment_should_be_exact), Toast.LENGTH_LONG)
                                            .show();

                                    vibe.vibrate(200);
                                    return;
                                }
                            }
                            //not allowed buy directly, buy and deposit not allow debt, 6 jul 20 dont allow debt
                            if (checkout_data.getShipping().getMethod() == 0) {

                                if (checkout_data.getTotalPaymentReceived() < _tot_inv) {
                                    Toast.makeText(getBaseContext(),
                                            getResources().getString(R.string.error_payment_should_be_exact), Toast.LENGTH_LONG)
                                            .show();
                                    vibe.vibrate(200);
                                    return;
                                }
                            }
                        } catch (Exception e){e.printStackTrace();}
                    }
                }
                idx_state++;
                displayFragment(array_state[idx_state]);
            }
        });

        (findViewById(R.id.lyt_previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state < 1) {finish();return;};
                idx_state--;
                if (checkout_data.getShipping().getMethod() == 3
                        //|| checkout_data.getShipping().getMethod() == 4
                        //|| checkout_data.getShipping().getMethod() == 5
                ) { //special for tokopedia, gofood, grab food
                    idx_state = 0;
                }
                displayFragment(array_state[idx_state]);
            }
        });
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, android.R.color.white);
        Tools.setSystemBarLight(this);
    }

    private void displayFragment(State state) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        Fragment fragment = null;
        refreshStepTitle();

        Bundle bundle = new Bundle();

        checkout_data = getCheckoutData();

        if (state.name().equalsIgnoreCase(State.SHIPPING.name())) {
            fragment = new ShippingFragment();
            try {
                bundle.putString("customer_name", customer.getName());
                bundle.putString("customer_email", customer.getEmail());
                bundle.putString("customer_phone", customer.getPhone());
                bundle.putString("customer_address", customer.getAddress());

                bundle.putString("shipping_method", ship_methods[shipping.getMethod()]);
                int shipping_wh_id = shipping.getWarehouseId();
                if (shipping_wh_id == 0) {
                    Params wh_params = paramCatalog.getParamByName("warehouse_id");
                    if (wh_params instanceof Params) {
                        shipping_wh_id = Integer.parseInt(wh_params.getValue());
                    }
                }
                bundle.putString("shipping_warehouse_pickup", warehouse_names.get(shipping_wh_id));
                bundle.putString("shipping_warehouse_id", shipping_wh_id+"");
                bundle.putString("shipping_date", shipping.getDate());
                bundle.putString("shipping_address", shipping.getAddress());
                bundle.putString("shipping_name", shipping.getName());
                bundle.putString("shipping_phone", shipping.getPhone());
                fragment.setArguments(bundle);

                if (shipping.getWarehouseName() == null && bundle.getString("shipping_warehouse").length() > 0) {
                    shipping.setWarehouseName(bundle.getString("shipping_warehouse"));
                    setCurrentWarehouseName(current_warehouse_name);
                }

                Log.e(TAG, "bundle on displayFragment : "+ bundle.toString());

                checkout_data.setCustomer(customer);
                checkout_data.setShipping(shipping);

                next_checkout_button.setText(getResources().getString(R.string.next_to_payment));
            } catch (Exception e) {}

            tv_shipping.setTextColor(getResources().getColor(R.color.grey_90));
            image_shipping.clearColorFilter();
        } else if (state.name().equalsIgnoreCase(State.PAYMENT.name())) {
            fragment = new PaymentFragment();

            line_first.setBackgroundColor(getResources().getColor(R.color.greenUcok));
            image_shipping.setColorFilter(getResources().getColor(R.color.greenUcok), PorterDuff.Mode.SRC_ATOP);
            image_payment.clearColorFilter();
            tv_payment.setTextColor(getResources().getColor(R.color.grey_90));

            if (!checkout_data.getCustomer().equals(null)) {
                register.setCustomer(checkout_data.getCustomer());
            }

            next_checkout_button.setText(getResources().getString(R.string.label_next));
        } else if (state.name().equalsIgnoreCase(State.CONFIRMATION.name())) {
            fragment = new ConfirmationFragment();
            line_second.setBackgroundColor(getResources().getColor(R.color.greenUcok));
            image_payment.setColorFilter(getResources().getColor(R.color.greenUcok), PorterDuff.Mode.SRC_ATOP);
            image_confirm.clearColorFilter();
            tv_confirm.setTextColor(getResources().getColor(R.color.grey_90));

            next_checkout_button.setText(getResources().getString(R.string.label_checkout_save_order));
        }

        if (fragment == null) return;

        //bundle.putSerializable("checkout_data", checkout_data);
        bundle.putSerializable("checkout_data", getCheckoutData());
        fragment.setArguments(bundle);

        fragmentTransaction.replace(R.id.frame_content, fragment);
        fragmentTransaction.commit();
        current_fragment = fragment;
    }

    private void refreshStepTitle() {
        tv_shipping.setTextColor(getResources().getColor(R.color.grey_20));
        tv_payment.setTextColor(getResources().getColor(R.color.grey_20));
        tv_confirm.setTextColor(getResources().getColor(R.color.grey_20));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        Tools.changeMenuIconColor(menu, getResources().getColor(R.color.grey_60));
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else {
            Toast.makeText(getApplicationContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
        Checkout c_data = new Checkout();
        c_data.setCustomer(customer);
        this.checkout_data = c_data;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Checkout getCheckoutData() {
        if (checkout_data == null) {
            return new Checkout();
        }
        return checkout_data;
    }

    public void setCheckoutData(Checkout _checkout) {
        this.checkout_data = _checkout;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public void goToFragment(int index) {
        idx_state = index;
        displayFragment(array_state[idx_state]);
    }

    private void getWarehouseList() {
        warehouse_items.clear();

        List<Warehouses> whs = warehouseCatalog.getAllWarehouses();
        if (whs != null) {
            if (allowed_warehouses.size() > 0) {
                for (Warehouses wh : whs) {
                    if (allowed_warehouses.contains(wh.getWarehouseId())) {
                        warehouse_items.add(wh.getTitle());
                        warehouse_ids.put(wh.getTitle(), wh.getWarehouseId() + "");
                        warehouse_names.put(wh.getWarehouseId(), wh.getTitle());
                        if (wh.getWarehouseId() == current_warehouse_id) {
                            current_warehouse_name = wh.getTitle();
                        }
                    }
                }
            } else {
                for (Warehouses wh : whs) {
                    warehouse_items.add(wh.getTitle());
                    warehouse_ids.put(wh.getTitle(), wh.getWarehouseId() + "");
                    warehouse_names.put(wh.getWarehouseId(), wh.getTitle());
                    if (wh.getWarehouseId() == current_warehouse_id) {
                        current_warehouse_name = wh.getTitle();
                    }
                }
            }
        } else {
            Map<String, String> params = new HashMap<String, String>();

            String url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
            _string_request(
                    Request.Method.GET,
                    url, params, false,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                JSONObject jObj = new JSONObject(result);
                                success = jObj.getInt(TAG_SUCCESS);
                                // Check for error node in json
                                if (success == 1) {
                                    warehouse_data = jObj.getJSONArray("data");
                                    for(int n = 0; n < warehouse_data.length(); n++)
                                    {
                                        JSONObject data_n = warehouse_data.getJSONObject(n);
                                        warehouse_items.add(data_n.getString("title"));
                                        warehouse_ids.put(data_n.getString("title"), data_n.getString("id"));
                                        warehouse_names.put(data_n.getInt("id"), data_n.getString("title"));
                                        if (data_n.getInt("id") == current_warehouse_id) {
                                            current_warehouse_name = data_n.getString("title");
                                        }
                                    }
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void showDialog() {
        if ((pDialog != null) && !pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if ((pDialog != null) && pDialog.isShowing())
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
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    public ArrayList<String> getWarehouseItems() {
        return warehouse_items;
    }

    public HashMap<String, String> getWarehouseIds() {
        return warehouse_ids;
    }

    public int getCurrentWarehouseId() {
        return current_warehouse_id;
    }

    public String getCurrentWarehouseName() {
        if (current_warehouse_name != null) {
            return current_warehouse_name;
        }
        return warehouse_names.get(current_warehouse_id);
    }

    public void setCurrentWarehouseName(String _current_warehouse_name) {
        this.current_warehouse_name = _current_warehouse_name;
    }

    public void setShipping(Shipping _shipping, Checkout c_data) {
        this.shipping = _shipping;
        c_data.setShipping(shipping);
        this.checkout_data = c_data;
    }

    public Shipping getShipping() {
        return shipping;
    }

    public String[] getShippingMethods() {
        return ship_methods;
    }

    public void pushInvoice(int saleId) {
        lineitemList = new ArrayList<Map<String, String>>();
        try {
            saleId = saleId;
            sale = saleLedger.getSaleById(saleId);
            for(LineItem line : sale.getAllLineItem()) {
                lineitemList.add(line.toMap());
            }
            paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
            paymentList = paymentCatalog.getPaymentBySaleId(saleId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // building payment information
        List<Map<String, String>> pyitemList = new ArrayList<Map<String, String>>();
        if (paymentList.size() > 0) {
            for (Payment payment : paymentList) {
                pyitemList.add(payment.toMap());
            }
        }

        Map<String, Object> mObj = new HashMap<String, Object>();
        ArrayList arrItems = new ArrayList();
        for (int i = 0; i < lineitemList.size(); i++){
            Map<String, String> mItem = new HashMap<String, String>();
            try {
                // use map
                mItem.put("name", lineitemList.get(i).get("name"));
                mItem.put("qty", lineitemList.get(i).get("quantity"));
                mItem.put("unit_price", lineitemList.get(i).get("unit_price"));
                mItem.put("base_price", lineitemList.get(i).get("base_price"));
                mItem.put("id", lineitemList.get(i).get("id"));
                mItem.put("barcode", lineitemList.get(i).get("barcode"));
                arrItems.add(mItem);
            } catch (Exception e) {}
        }

        Map<String, String> arrCust = new HashMap<String, String>();

        ArrayList arrPaymentList = new ArrayList();
        Map<String, String> arrPayment = new HashMap<String, String>();
        ArrayList arrShippingList = new ArrayList();
        Map<String, String> arrMerchant = new HashMap<String, String>();
        ArrayList arrMerchantList = new ArrayList();
        Map<String, String> arrPaymentBank = new HashMap<String, String>();
        ArrayList arrReceiptList = new ArrayList();
        try {
            Customer cust = saleLedger.getCustomerBySaleId(saleId);
            mObj.put("items_belanja", arrItems);

            arrCust.put("email", cust.getEmail());
            arrCust.put("name", cust.getName());
            arrCust.put("phone", cust.getPhone());
            arrCust.put("address", cust.getAddress());
            mObj.put("customer", arrCust);

            Double total_tendered = 0.0;
            if (paymentList != null && paymentList.size() > 0) {
                Boolean has_change_due = false;
                for (Payment py : paymentList) {
                    Map<String, String> arrPayment2 = new HashMap<String, String>();
                    arrPayment2.put("type", py.getPaymentChannel());
                    arrPayment2.put("amount_tendered", ""+ py.getAmount());
                    if (!has_change_due) {
                        arrPayment2.put("change_due", ""+ change_due);
                        has_change_due = true;
                    } else {
                        arrPayment2.put("change_due", "0");
                    }
                    arrPaymentList.add(arrPayment2);
                    total_tendered = total_tendered + py.getAmount();
                }
            } else {
                arrPayment.put("type", "cash_receive");
                arrPayment.put("amount_tendered", "0");
                arrPayment.put("change_due", "0");
                arrPaymentList.add(arrPayment);
            }
            mObj.put("payment", arrPaymentList);
            // set warehouse_id if any
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                mObj.put("warehouse_id", whParam.getValue());
            }

            mObj.put("discount", sale.getDiscount());

            Double tot_tagihan = sale.getTotal() - sale.getDiscount();
            if (total_tendered < tot_tagihan) {
                mObj.put("transaction_type", 0);
            }

            arrShippingList.add(shipping.toMap());
            mObj.put("shipping", arrShippingList);

            if (getCheckoutData().getUseGoFood()) {
                // force to be paid inv for gofood
                mObj.put("transaction_type", 1);

                arrMerchant.put("name", "GoFood");
                arrMerchant.put("total_invoice", getCheckoutData().getTotalGoFoodInvoice());
                arrMerchant.put("total_wallet_tendered", getCheckoutData().getWalletGoFood());
                arrMerchant.put("total_cash_tendered", getCheckoutData().getCashReceive());
            }

            if (getCheckoutData().getUseGrabFood()) {
                // force to be paid inv for gofood
                mObj.put("transaction_type", 1);

                arrMerchant.put("name", "GrabFood");
                arrMerchant.put("total_invoice", getCheckoutData().getTotalGrabFoodInvoice());
                arrMerchant.put("total_wallet_tendered", getCheckoutData().getWalletGrabFood());
                arrMerchant.put("total_cash_tendered", getCheckoutData().getCashReceive());
            }
            if (arrMerchant.size() > 0) {
                mObj.put("merchant", arrMerchant);
            }

            if (getCheckoutData().getOngkir() > 0) {
                mObj.put("ongkir", getCheckoutData().getOngkir());
                if (getCheckoutData().getOngkirCashToDriver()) {
                    mObj.put("ongkir_cash_to_driver", 1);
                } else {
                    mObj.put("ongkir_cash_to_driver", 0);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int server_invoice_id = saleLedger.getServerInvoiceId(saleId);
            if (server_invoice_id <= 0) {
                _execute(mObj);
            } else {
                Toast.makeText(getApplicationContext(),
                        "Data telah tercatat di server dengan id "+ server_invoice_id, Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {

        }
    }

    private void _execute(final Map mObj) {
        Map<String, String> params = new HashMap<String, String>();
        if (payment_receipts.containsKey("mandiri")) {
            String base64 = encodeToBase64("mandiri");
            if (base64 != null) {
                params.put("receipt_mandiri", base64);
            }
        }
        if (payment_receipts.containsKey("bca")) {
            String base64 = encodeToBase64("bca");
            if (base64 != null) {
                params.put("receipt_bca", base64);
            }
        }
        if (payment_receipts.containsKey("bri")) {
            String base64 = encodeToBase64("bri");
            if (base64 != null) {
                params.put("receipt_bri", base64);
            }
        }
        String _url = Server.URL + "transaction/create?api-key=" + Server.API_KEY;
        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;

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
                            int server_invoice_id = jObj.getInt(TAG_ID);
                            String server_invoice_number = jObj.getString("invoice_number");
                            // Check for error node in json
                            if (success == 1) {
                                saleLedger.setServerInvoiceId(sale, server_invoice_id, server_invoice_number);
                                // remove transfer receipt if any
                                if (payment_receipts.containsKey("mandiri")) {
                                    removeReceiptBitmap("mandiri");
                                }
                                if (payment_receipts.containsKey("bca")) {
                                    removeReceiptBitmap("bca");
                                }
                                if (payment_receipts.containsKey("bri")) {
                                    removeReceiptBitmap("bri");
                                }
                                payment_receipts.clear();
                            }
                            /*Toast.makeText(getApplicationContext(),
                                    jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();*/
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }

    private void showDialogPaymentSuccess(Sale sale, Customer customer, String status) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PaymentSuccessFragment newFragment = new PaymentSuccessFragment();
        newFragment.setSale(sale);
        newFragment.setCustomer(customer);
        newFragment.setStatus(status);

        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        transaction.add(R.id.parent_view, newFragment).addToBackStack(null).commit();
        (findViewById(R.id.lyt_next_container)).setVisibility(View.GONE);
        (findViewById(R.id.toolbar)).setVisibility(View.GONE);
    }

    private void removeReceiptBitmap(String bank_name) {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS/" + bank_name + ".jpg";
            File file = new File(bank_name);
            if(file.exists()) {
                deleteFile(path);
            }
            if (payment_receipts.containsKey(bank_name)) {
                payment_receipts.remove(bank_name);
            }
        } catch (Exception e){e.printStackTrace();}
    }

    public void setReceiptBitmap(String bank_name, Bitmap bitmap) {
        payment_receipts.put(bank_name, bitmap);
        /*try {
            String encodedImage = encodeToBase64(bank_name);
        } catch (Exception e) {
            e.printStackTrace();
        }*/
    }

    private String encodeToBase64(final String file_name){
        try {
            String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS/" + file_name +".jpg";
            File file = new File(url);
            byte[] bFile = new byte[(int) file.length()];
            FileInputStream inputStream = new FileInputStream(url);
            inputStream.read(bFile);
            inputStream.close();
            return Base64.encodeToString(bFile, Base64.NO_WRAP);
        } catch (IOException e) { e.printStackTrace();}
        return null;
    }

    public static File saveReceiptBitmap(Bitmap bm, String fileName){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    private HashMap<String, JSONArray> cargo_items = new HashMap<String, JSONArray>();
    private HashMap<String, Integer> cargo_ids = new HashMap<String, Integer>();
    private ArrayList<String> cargo_array_list = new ArrayList<String>();
    private String cargo_location = null;
    private HashMap<String, JSONObject> produck_ongkirs = new HashMap<String, JSONObject>();
    private HashMap<String, JSONObject> cargo_configs = new HashMap<String, JSONObject>();
    private Double total_ongkir = 0.0;

    public void buildCargoLocations(final String type) {
        if (!cargo_items.containsKey(type)) {
            try {
                Map<String, String> params = new HashMap<String, String>();
                params.put("admin_id", admin_id);
                params.put("type", type);
                Params whParam = paramCatalog.getParamByName("warehouse_id");
                if (whParam != null) {
                    params.put("warehouse_id", whParam.getValue());
                }

                String url = Server.URL + "cargo/list?api-key=" + Server.API_KEY;
                _string_request(
                        Request.Method.GET,
                        url, params, false,
                        new VolleyCallback() {
                            @Override
                            public void onSuccess(String result) {
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    // Check for error node in json
                                    if (success == 1) {
                                        JSONArray cargo_data = jObj.getJSONArray("data");
                                        Log.e(TAG, "cargo_data : "+ cargo_data.toString());
                                        for(int n = 0; n < cargo_data.length(); n++)
                                        {
                                            JSONObject data_n = cargo_data.getJSONObject(n);
                                            if (data_n.has("id")) {
                                                String cargo_label = data_n.getString("town")+" - "+data_n.getString("name");
                                                cargo_ids.put(cargo_label, data_n.getInt("id"));
                                                cargo_array_list.add(cargo_label);
                                                if (data_n.has("global_configs")) {
                                                    cargo_configs.put(cargo_label, data_n.getJSONObject("global_configs"));
                                                }

                                                if (data_n.has("ongkir")) {
                                                    produck_ongkirs.put(cargo_label, data_n.getJSONObject("ongkir"));
                                                }
                                            }
                                        }
                                        cargo_items.put(type, cargo_data);
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
            } catch (Exception e){e.printStackTrace();}
        }
    }

    public String[] getCargoLocations(String type) {
        return cargo_array_list.toArray(new String[cargo_array_list.size()]);
    }

    public void setCargoLocation(String location) {
        this.cargo_location = location;
        // just test
        setTotalOngkir();
    }

    public String getCargoLocation() {
        return cargo_location;
    }

    public int getCargoId() {
        return cargo_ids.get(cargo_location);
    }

    public JSONObject getCargoConfigs() {
        if (cargo_location == null) {
            return new JSONObject();
        }
        return cargo_configs.get(cargo_location);
    }

    public JSONObject getProductOngkirs() {
        if (cargo_location == null) {
            return new JSONObject();
        }
        return produck_ongkirs.get(cargo_location);
    }

    private void setTotalOngkir() {
        Double ongkir = 0.0;
        try {
            JSONObject cfg = getCargoConfigs();
            JSONObject bobots = new JSONObject();
            if (cfg.has("bobot")) {
                bobots = cfg.getJSONObject("bobot");
            }

            JSONObject onk = getProductOngkirs();
            for(LineItem line : register.getCurrentSale().getAllLineItem()) {
                String product_id = line.getProduct().getBarcode();
                Double bobot = 2.0;
                if (bobots.has(product_id)) {
                    JSONObject bbt_prod = bobots.getJSONObject(product_id);
                    if (bbt_prod.has("value")) {
                        bobot = bbt_prod.getDouble("value");
                    }
                }
                if (onk.has(product_id)) {
                    JSONArray onk_arr = onk.getJSONArray(product_id);
                    int tot_qty = register.getCurrentSale().getOrders();
                    for(int n = 0; n < onk_arr.length(); n++) {
                        JSONObject onk_n = onk_arr.getJSONObject(n);
                        int _qty = onk_n.getInt("quantity");
                        int _qty_max = onk_n.getInt("quantity_max");
                        if ((tot_qty >= _qty) && (tot_qty <= _qty_max)) {
                            Double _price = onk_n.getDouble("price");
                            //Log.e("CUK1", "_price*bobot*line.getQuantity() : "+ _price+"*"+bobot+"*"+line.getQuantity());
                            ongkir = ongkir + (_price*bobot*line.getQuantity());
                        }
                    }
                } else {
                    //Log.e("CUK3", "ga ada setup ongkir utk produk "+ product_id);
                }
            }
            this.total_ongkir = ongkir;

            int int_ongkir = Integer.valueOf(ongkir.intValue());
            checkout_data.setOngkir(int_ongkir);
        } catch (Exception e){e.printStackTrace();}
    }
}
