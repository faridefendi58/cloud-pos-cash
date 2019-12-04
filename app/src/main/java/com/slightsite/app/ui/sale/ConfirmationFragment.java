package com.slightsite.app.ui.sale;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Typeface;
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
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfirmationFragment extends Fragment {

    private View root;
    private TextView conf_customer_name;
    private TextView conf_customer_phone;
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
    private TextView total_discount;
    private TextView edit_customer;
    private TextView edit_payment;
    private TextView edit_cart;
    private TextView totalPayment;
    private TextView changeDue;
    private TextView change_due_label;
    private TextView gograbfood_discount;
    private TextView gograbfood_total_price;
    private TextView gograbfood_discount_label;
    private LinearLayout change_due_container;
    private LinearLayout gograbfood_discount_container;
    private LinearLayout main_discount_container;
    private Payment payment;

    /** shipping detail */
    private TextView shipping_method;
    private TextView shipping_date;
    private TextView shipping_warehouse;
    private TextView label_shipping_warehouse;
    private LinearLayout recipient_name_container;
    private TextView shipping_recipient_name;
    private TextView shipping_recipient_phone;
    private Shipping shipping;
    private String[] ship_methods;

    public ConfirmationFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
            ship_methods = ((CheckoutActivity)getActivity()).getShippingMethods();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        root = inflater.inflate(R.layout.fragment_confirmation, container, false);

        initView();
        initAction();

        try {
            register.setPaymentItems(c_data.getPaymentItems());
            // also setup the shipping
            Shipping _ship = c_data.getShipping();
            _ship.setSaleId(register.getCurrentSale().getId());
            _ship.setDateAdded(register.getCurrentSale().getStartTime());
            register.setShipping(_ship);
            // setup the discount
            register.getCurrentSale().setDiscount(c_data.getDiscount());
            register.setDiscount();

            c_data.setShipping(_ship);
            Sale test = register.getCurrentSale();
            //Log.e(getTag(), "Shipping data on onCreateView conf fragment: "+ register.getShipping().toMap().toString());
        } catch (Exception e){ }

        initDefaultValue();
        return root;
    }

    private void initView() {
        conf_customer_name = (TextView) root.findViewById(R.id.conf_customer_name);
        conf_customer_phone = (TextView) root.findViewById(R.id.conf_customer_phone);
        conf_customer_address = (TextView) root.findViewById(R.id.conf_customer_address);
        totalPrice = (TextView) root.findViewById(R.id.totalPrice);
        total_discount = (TextView) root.findViewById(R.id.total_discount);

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
        change_due_label  = (TextView) root.findViewById(R.id.change_due_label);
        change_due_container  = (LinearLayout) root.findViewById(R.id.change_due_container);
        gograbfood_discount_container  = (LinearLayout) root.findViewById(R.id.gograbfood_discount_container);
        main_discount_container  = (LinearLayout) root.findViewById(R.id.main_discount_container);
        gograbfood_discount = (TextView) root.findViewById(R.id.gograbfood_discount);
        gograbfood_total_price = (TextView) root.findViewById(R.id.gograbfood_total_price);
        gograbfood_discount_label = (TextView) root.findViewById(R.id.gograbfood_discount_label);

        shipping_method = (TextView) root.findViewById(R.id.shipping_method);
        shipping_date = (TextView) root.findViewById(R.id.shipping_date);
        shipping_warehouse = (TextView) root.findViewById(R.id.shipping_warehouse);
        label_shipping_warehouse = (TextView) root.findViewById(R.id.label_shipping_warehouse);
        shipping_recipient_name = (TextView) root.findViewById(R.id.shipping_recipient_name);
        shipping_recipient_phone = (TextView) root.findViewById(R.id.shipping_recipient_phone);
        recipient_name_container = (LinearLayout) root.findViewById(R.id.recipient_name_container);
    }

    private void initAction() {
        //c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        c_data = (Checkout) getArguments().getSerializable("checkout_data");

        if(register.hasSale()){
            showList(register.getCurrentSale().getAllLineItem());

            totalPrice.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
            if (c_data.getDiscount() > 0) {
                total_discount.setText(CurrencyController.getInstance().moneyFormat(Double.parseDouble(c_data.getDiscount()+"")) + "");
            }
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
                conf_customer_phone.setText(customer.getPhone());
                conf_customer_address.setText(customer.getAddress());
                if (customer.getId() <= 0 && customer.getPhone() == "0000") {
                    conf_customer_phone.setVisibility(View.GONE);
                    conf_customer_address.setVisibility(View.GONE);
                }
            }
            if (c_data.getTotalPaymentReceived() > 0) {
                totalPayment.setText(CurrencyController.getInstance().moneyFormat(c_data.getTotalPaymentReceived()));
                Double change_due = c_data.getTotalPaymentReceived() - (register.getTotal() - c_data.getDiscount());
                try {
                    c_data.setChangeDue(change_due);
                    ((CheckoutActivity)getActivity()).setCheckoutData(c_data);
                } catch (Exception e){e.printStackTrace();}

                changeDue.setText(CurrencyController.getInstance().moneyFormat(change_due));
                if (change_due < 0) {
                    change_due_label.setText(getResources().getString(R.string.label_dept));
                    change_due_label.setTypeface(Typeface.DEFAULT_BOLD);
                    change_due_label.setTextColor(getResources().getColor(R.color.red_300));
                    Double change_due2 = -1 * change_due;
                    changeDue.setText(CurrencyController.getInstance().moneyFormat(change_due2));
                    changeDue.setTypeface(Typeface.DEFAULT_BOLD);
                    changeDue.setTextColor(getResources().getColor(R.color.red_300));
                } else {
                    change_due_label.setText(getResources().getString(R.string.label_change_due));
                    if (change_due == 0) {
                        change_due_container.setVisibility(View.GONE);
                    }
                }

                if (c_data.getUseGoFood() || c_data.getUseGrabFood()) {
                    gograbfood_discount_container.setVisibility(View.VISIBLE);
                    gograbfood_discount.setText(CurrencyController.getInstance().moneyFormat(change_due));
                    if (c_data.getUseGoFood()) {
                        if (change_due <= 0) {
                            gograbfood_discount_label.setText(getActivity().getResources().getString(R.string.label_gofood_discount));
                        } else {
                            gograbfood_discount_label.setText(getActivity().getResources().getString(R.string.label_gofood_fee));
                        }
                        try {
                            Double _total_price = Double.parseDouble(c_data.getTotalGoFoodInvoice()) - Double.parseDouble(c_data.getGofoodDiscount());
                            gograbfood_total_price.setText(CurrencyController.getInstance().moneyFormat(_total_price));
                        } catch (Exception e){}
                        main_discount_container.setVisibility(View.GONE);
                    } else if (c_data.getUseGrabFood()) {
                        if (change_due <= 0) {
                            gograbfood_discount_label.setText(getActivity().getResources().getString(R.string.label_grabfood_discount));
                        } else {
                            gograbfood_discount_label.setText(getActivity().getResources().getString(R.string.label_grabfood_fee));
                        }
                        try {
                            Double _total_price = Double.parseDouble(c_data.getTotalGrabFoodInvoice()) - Double.parseDouble(c_data.getGrabfoodDiscount());
                            gograbfood_total_price.setText(CurrencyController.getInstance().moneyFormat(_total_price));
                        } catch (Exception e){}
                        main_discount_container.setVisibility(View.GONE);
                    }
                    change_due_container.setVisibility(View.GONE);
                }
            } else {
                change_due_label.setText(getResources().getString(R.string.label_dept));
                change_due_label.setTypeface(Typeface.DEFAULT_BOLD);
                change_due_label.setTextColor(getResources().getColor(R.color.red_300));
                Double dept = register.getTotal() - c_data.getDiscount();
                changeDue.setText(CurrencyController.getInstance().moneyFormat(dept));
                changeDue.setTypeface(Typeface.DEFAULT_BOLD);
                changeDue.setTextColor(getResources().getColor(R.color.red_300));
            }

            Log.e(getTag(), "c_data.getShipping() : "+ c_data.getShipping().toMap().toString());
            if (!c_data.getShipping().equals(null)) {
                shipping = c_data.getShipping();
                Log.e(getTag(), "Shipping data on confirmation :"+ shipping.toMap().toString());
                shipping_method.setText(ship_methods[shipping.getMethod()]);
                if (shipping.getDate().equals(null) || shipping.getDate().length() == 0) {
                    DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
                    if (shipping.getMethod() == 0) {
                        df = new SimpleDateFormat("dd MMM yyyy");
                    }
                    String date = df.format(Calendar.getInstance().getTime());
                    shipping.setDate(date);
                }
                shipping_date.setText(shipping.getDate());
                shipping_warehouse.setText(shipping.getWarehouseName());
                if (shipping.getAddress() != null && shipping.getMethod() > 1) {
                    shipping_warehouse.setText(shipping.getAddress());
                    label_shipping_warehouse.setText(getResources().getString(R.string.label_shipping_address));

                    shipping_date.setText(shipping.getPickupDate());
                    if (shipping.getAddress().length() >= 25) {
                        label_shipping_warehouse.setText(getResources().getString(R.string.address));
                    }
                }

                if (shipping.getName() != null && shipping.getMethod() > 1) {
                    recipient_name_container.setVisibility(View.VISIBLE);
                    shipping_recipient_name.setText(shipping.getName());
                    shipping_recipient_phone.setText(shipping.getPhone());
                }
            }
        } catch (Exception e) {e.printStackTrace();}
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
