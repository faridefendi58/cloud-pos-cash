package com.slightsite.app.ui.sale;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.techicalservices.Tools;

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
    public Fragment current_fragment = null;
    public Checkout checkout_data;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

        (findViewById(R.id.lyt_next)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (idx_state == array_state.length - 1) return;
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
}
