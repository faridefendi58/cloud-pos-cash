package com.slightsite.app.ui.sale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
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
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.printer.PrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private enum State {
        SHIPPING,
        PAYMENT,
        CONFIRMATION
    }

    State[] array_state = new State[]{State.SHIPPING, State.PAYMENT, State.CONFIRMATION};

    private View line_first, line_second;
    private ImageView image_shipping, image_payment, image_confirm;
    private TextView tv_shipping, tv_payment, tv_confirm;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            register = Register.getInstance();
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();

            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                current_warehouse_id = Integer.parseInt(whParam.getValue());
            }
            getWarehouseList();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        setContentView(R.layout.activity_checkout);
        initToolbar();
        initComponent();

        displayFragment(State.SHIPPING);
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

        image_payment.setColorFilter(getResources().getColor(R.color.grey_10), PorterDuff.Mode.SRC_ATOP);
        image_confirm.setColorFilter(getResources().getColor(R.color.grey_10), PorterDuff.Mode.SRC_ATOP);

        vibe = (Vibrator) getSystemService(getApplicationContext().VIBRATOR_SERVICE);

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state == array_state.length - 1) {
                    // add customer first
                    if (customer.getId() <= 0) {
                        int c_id = customerCatalog.createCustomer(
                                customer.getName(),
                                customer.getEmail(),
                                customer.getPhone(),
                                customer.getAddress(), 1);
                        if (c_id > 0) {
                            customer = customerCatalog.getCustomerById(c_id);
                            register.setCustomer(customer);
                        }
                    }
                    int saleId = register.getCurrentSale().getId();
                    register.endSale(DateTimeStrategy.getCurrentTime());

                    Intent newActivity = new Intent(CheckoutActivity.this, PrinterActivity.class);
                    newActivity.putExtra("saleId", saleId);
                    finish();
                    startActivity(newActivity);
                    return;
                } else {
                    if (array_state[idx_state] == State.SHIPPING) {
                        // cek customer data dl
                        Log.e(TAG, "customer email : "+ checkout_data.getCustomer().getEmail().toString());
                        Log.e(TAG, "customer name : "+ checkout_data.getCustomer().getName().length());
                        Log.e(TAG, "shipping data on SHIPPING : "+ checkout_data.getShipping().toMap().toString());
                        if (checkout_data.getCustomer().equals("null")
                                || checkout_data.getCustomer().getEmail() == "email@email.com"
                                || (checkout_data.getCustomer().getName().length() == 0)) {
                            Toast.makeText(getBaseContext(),
                                    getResources().getString(R.string.error_empty_customer_data), Toast.LENGTH_SHORT)
                                    .show();
                            vibe.vibrate(200);
                            return;
                        }
                    } else if (array_state[idx_state] == State.PAYMENT) {
                        Log.e(TAG, "shipping data on payment : "+ checkout_data.getShipping().toMap().toString());
                        if (checkout_data.getTotalPaymentReceived() <= 0) {
                            Toast.makeText(getBaseContext(),
                                    getResources().getString(R.string.error_empty_payment_data), Toast.LENGTH_SHORT)
                                    .show();

                            vibe.vibrate(200);
                            return;
                        }
                    }
                }
                idx_state++;
                displayFragment(array_state[idx_state]);
            }
        });

        (findViewById(R.id.lyt_previous)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state < 1) return;
                idx_state--;
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

        checkout_data = this.getCheckoutData();

        if (state.name().equalsIgnoreCase(State.SHIPPING.name())) {
            fragment = new ShippingFragment();
            try {
                bundle.putString("customer_name", customer.getName());
                bundle.putString("customer_email", customer.getEmail());
                bundle.putString("customer_phone", customer.getPhone());
                bundle.putString("customer_address", customer.getAddress());
                fragment.setArguments(bundle);

                checkout_data.setCustomer(customer);
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
            Log.e(TAG, "CUK : "+ checkout_data.getShipping().toMap().toString());
        } else if (state.name().equalsIgnoreCase(State.CONFIRMATION.name())) {
            fragment = new ConfirmationFragment();
            line_second.setBackgroundColor(getResources().getColor(R.color.greenUcok));
            image_payment.setColorFilter(getResources().getColor(R.color.greenUcok), PorterDuff.Mode.SRC_ATOP);
            image_confirm.clearColorFilter();
            tv_confirm.setTextColor(getResources().getColor(R.color.grey_90));
        }

        if (fragment == null) return;

        bundle.putSerializable("checkout_data", checkout_data);
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
        Map<String, String> params = new HashMap<String, String>();

        warehouse_items.clear();

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
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    public ArrayList<String> getWarehouseItems() {
        return warehouse_items;
    }

    public int getCurrentWarehouseId() {
        return current_warehouse_id;
    }

    public String getCurrentWarehouseName() {
        return warehouse_names.get(current_warehouse_id);
    }

    public void setShipping(Shipping shipping, Checkout c_data) {
        this.shipping = shipping;
        c_data.setShipping(shipping);
        this.checkout_data = c_data;
    }

    public Shipping getShipping() {
        return shipping;
    }
}
