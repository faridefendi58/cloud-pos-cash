package com.slightsite.app.ui.sale;

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
    private AdapterListCart mAdapter;
    private Register register;
    private ArrayList<Map<String, String>> saleList;
    private RecyclerView saleListView;
    private Resources res;
    private TextView totalPrice;

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
    }

    private void initAction() {
        //c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        c_data = (Checkout) getArguments().getSerializable("checkout_data");

        if(register.hasSale()){
            showList(register.getCurrentSale().getAllLineItem());

            totalPrice.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
        }
    }

    private void initDefaultValue() {
        try {
            if (!c_data.getCustomer().equals(null)) {
                customer = c_data.getCustomer();
                conf_customer_name.setText(customer.getName());
                conf_customer_address.setText(customer.getAddress());
            }
            showPaymentList(c_data.getPaymentItems());
        } catch (Exception e) { }
    }

    private void showPaymentList(List<PaymentItem> list) {
        paymentList = new ArrayList<Map<String, String>>();
        for(PaymentItem line : list) {
            paymentList.add(line.toMap());
        }
        Log.e(getTag(), "paymentList : "+ paymentList.toString());

        pAdapter = new AdapterListPayment(getContext(), list);
        paymentListView.setAdapter(pAdapter);
    }

    private void showList(List<LineItem> list) {
        saleList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            saleList.add(line.toMap());
        }

        mAdapter = new AdapterListCart(getContext(), list, register, totalPrice);
        saleListView.setAdapter(mAdapter);

        mAdapter.setOnItemClickListener(new AdapterListCart.OnItemClickListener() {
            @Override
            public void onItemClick(View view, LineItem obj, int position) {
            }
        });
    }
}
