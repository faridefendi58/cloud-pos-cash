package com.slightsite.app.ui.sale;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ConfirmationFragment extends Fragment {

    private View root;
    private TextView conf_customer_name;
    private TextView conf_customer_address;

    private Checkout c_data;
    private Customer customer;
    private RecyclerView paymentListView;
    private ArrayList<Map<String, String>> paymentList;
    private AdapterListPayment pAdapter;
    private AdapterListOrder mAdapter;
    private Register register;
    private ArrayList<Map<String, String>> saleList;
    private RecyclerView saleListView;
    private Resources res;
    private TextView totalPrice;
    private TextView edit_customer;
    private TextView edit_payment;
    private TextView edit_cart;
    private TextView totalPayment;
    private TextView changeDue;

    public ConfirmationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        root = inflater.inflate(R.layout.fragment_confirmation, container, false);

        initView();
        initAction();
        initDefaultValue();

        return root;
    }

    private void initView() {
        conf_customer_name = (TextView) root.findViewById(R.id.conf_customer_name);
        conf_customer_address = (TextView) root.findViewById(R.id.conf_customer_address);
        totalPrice = (TextView) root.findViewById(R.id.totalPrice);

        paymentListView = (RecyclerView) root.findViewById(R.id.payment_List);
        paymentListView.setLayoutManager(new LinearLayoutManager(getContext()));
        paymentListView.setHasFixedSize(true);
        paymentListView.setNestedScrollingEnabled(false);


        res = getResources();
        saleListView = (RecyclerView) root.findViewById(R.id.sale_List);
        saleListView.setLayoutManager(new LinearLayoutManager(getContext()));
        saleListView.setHasFixedSize(true);
        saleListView.setNestedScrollingEnabled(false);

        edit_customer  = (TextView) root.findViewById(R.id.edit_customer);
        edit_payment  = (TextView) root.findViewById(R.id.edit_payment);
        edit_cart  = (TextView) root.findViewById(R.id.edit_cart);
        totalPayment  = (TextView) root.findViewById(R.id.totalPayment);
        changeDue  = (TextView) root.findViewById(R.id.changeDue);
    }

    private void initAction() {
        //c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        c_data = (Checkout) getArguments().getSerializable("checkout_data");

        if(register.hasSale()){
            showList(register.getCurrentSale().getAllLineItem());

            totalPrice.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
        }

        showPaymentList(c_data.getPaymentItems());

        edit_customer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckoutActivity) getActivity()).goToFragment(0);
            }
        });

        edit_payment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((CheckoutActivity) getActivity()).goToFragment(1);
            }
        });

        edit_cart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //((CheckoutActivity) getActivity()).recreate();
                getActivity().finish();
            }
        });
    }

    private void initDefaultValue() {
        try {
            if (!c_data.getCustomer().equals(null)) {
                customer = c_data.getCustomer();
                conf_customer_name.setText(customer.getName());
                conf_customer_address.setText(customer.getAddress());
            }
            if (c_data.getTotalPaymentReceived() > 0) {
                totalPayment.setText(CurrencyController.getInstance().moneyFormat(c_data.getTotalPaymentReceived()));
                Double change_due = c_data.getTotalPaymentReceived() - register.getTotal();
                changeDue.setText(CurrencyController.getInstance().moneyFormat(change_due));
            }
        } catch (Exception e) { }
    }

    private void showPaymentList(List<PaymentItem> list) {
        pAdapter = new AdapterListPayment(getContext(), list, c_data.getPaymentTypes());
        paymentListView.setAdapter(pAdapter);
    }

    private void showList(List<LineItem> list) {
        saleList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            saleList.add(line.toMap());
        }

        mAdapter = new AdapterListOrder(getContext(), list, register, totalPrice);
        saleListView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterListOrder.OnItemClickListener() {
            @Override
            public void onItemClick(View view, LineItem obj, int position) {
            }
        });
    }
}
