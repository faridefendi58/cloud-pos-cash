package com.slightsite.app.ui.sale;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.otaliastudios.autocomplete.Autocomplete;
import com.otaliastudios.autocomplete.AutocompleteCallback;
import com.otaliastudios.autocomplete.AutocompletePresenter;
import com.slightsite.app.R;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.techicalservices.NoDaoSetException;

import java.util.List;
import java.util.Objects;

public class ShippingFragment extends Fragment {

    private View root;
    private Autocomplete userAutocomplete;
    private CustomerCatalog customerCatalog;
    private List<Customer> customers;
    private Customer cust;

    private EditText name;
    private EditText phone;
    private EditText email;
    private EditText address;

    private Checkout c_data;
    private String customer_name;
    private String customer_phone;
    private String customer_email;
    private String customer_address;

    public ShippingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_shipping, container, false);

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        customers = customerCatalog.getAllCustomer();
        setupUserAutocomplete();
        initAction();

        try {
            String customer_name = getArguments().getString("customer_name");
            String customer_phone = getArguments().getString("customer_phone");
            String customer_email = getArguments().getString("customer_email");
            String customer_address = getArguments().getString("customer_address");

            name.setText(customer_name);
            phone.setText(customer_phone);
            email.setText(customer_email);
            address.setText(customer_address);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return root;
    }

    private void setupUserAutocomplete() {
        name = (EditText) root.findViewById(R.id.customer_name);
        phone = (EditText) root.findViewById(R.id.customer_phone);
        email = (EditText) root.findViewById(R.id.customer_email);
        address = (EditText) root.findViewById(R.id.customer_address);
        final TextView customer_id = (TextView) root.findViewById(R.id.customer_id);
        float elevation = 6f;
        Drawable backgroundDrawable = new ColorDrawable(Color.WHITE);
        AutocompletePresenter<Customer> presenter = new UserPresenter(getContext(), customers);
        AutocompleteCallback<Customer> callback = new AutocompleteCallback<Customer>() {
            @Override
            public boolean onPopupItemClicked(Editable editable, Customer item) {
                editable.clear();
                editable.append(item.getName());
                phone.setText(item.getPhone());
                email.setText(item.getEmail());
                address.setText(item.getAddress());
                //customer_id.setText(item.getId());
                ((CheckoutActivity) getActivity()).setCustomer(item);
                ((CheckoutActivity) getActivity()).hideKeyboard(getActivity());
                return true;
            }

            public void onPopupVisibilityChanged(boolean shown) {}
        };

        userAutocomplete = Autocomplete.<Customer>on(name)
                .with(elevation)
                .with(backgroundDrawable)
                .with(presenter)
                .with(callback)
                .build();
    }

    private void setTextChangeListener(EditText etv, final String setType) {
        etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    customer_name = name.getText().toString();
                    customer_email = email.getText().toString();
                    customer_address = address.getText().toString();
                    if (setType == "phone") {
                        if (!cust.getPhone().equals(s.toString())) {
                            cust.setPhone(s.toString());
                            cust.setName(customer_name);
                            if (customer_email.equals(null)) {
                                cust.setEmail(customer_email);
                            } else {
                                cust.setEmail("-");
                            }
                            if (customer_address.equals(null)) {
                                cust.setAddress(customer_address);
                            }
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }
                    if (setType == "address") {
                        if (!cust.getAddress().equals(s.toString())) {
                            cust.setAddress(s.toString());
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initAction() {
        c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        cust = c_data.getCustomer();
        setTextChangeListener(phone, "phone");
        setTextChangeListener(address, "address");
    }
}