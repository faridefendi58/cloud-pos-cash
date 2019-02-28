package com.slightsite.app.ui.sale;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
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
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.inventory.ProductServerActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ShippingFragment extends Fragment {

    private View root;
    private Autocomplete userAutocomplete;
    private Autocomplete phoneAutocomplete;
    private CustomerCatalog customerCatalog;
    private List<Customer> customers;
    private Customer cust;

    private EditText name;
    private EditText phone;
    private EditText email;
    private EditText address;
    private EditText shipping_method;
    private AutoCompleteTextView shipping_date;
    private AutoCompleteTextView shipping_warehouse;
    private EditText shipping_address;

    private Checkout c_data;
    private String customer_name;
    private String customer_phone;
    private String customer_email;
    private String customer_address;

    private String[] ship_methods = new String[]{};
    private enum SMethod {DIRECT, LATER, GOJEK, GRAB, CURRIER }

    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private String current_warehouse_name;

    private Shipping ship;

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
        shipping_method = (EditText) root.findViewById(R.id.shipping_method);
        shipping_date = (AutoCompleteTextView) root.findViewById(R.id.shipping_date);
        shipping_address = (EditText) root.findViewById(R.id.shipping_address);
        shipping_warehouse = (AutoCompleteTextView) root.findViewById(R.id.shipping_warehouse);

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
                    if (setType == "shipping_address") {
                        ship.setAddress(s.toString());
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

        if (ship == null) {
            ship = c_data.getShipping();
        }
        ship.setMethod(0);
        CheckoutActivity c_act = ((CheckoutActivity) getActivity());
        int current_wh_id = c_act.getCurrentWarehouseId();
        ship.setWarehouseId(current_wh_id);
        ship.setWarehouseName(c_act.getCurrentWarehouseName());
        try {
            long date = Calendar.getInstance().getTimeInMillis();
            ship.setDate(Tools.getFormattedDateFlat(date));
        } catch (Exception e) {}

        c_data.setShipping(ship);
        ((CheckoutActivity) getActivity()).setShipping(ship, c_data);

        shipping_method.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShippingMethodDialog(v);
            }
        });
        shipping_warehouse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShippingWarehouseDialog(v);
            }
        });

        shipping_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight(v);
            }
        });

        setTextChangeListener(shipping_warehouse, "shipping_address");
    }

    private void showShippingMethodDialog(final View v) {
        current_warehouse_name = ((CheckoutActivity)getActivity()).getCurrentWarehouseName();
        ship_methods = ((CheckoutActivity)getActivity()).getShippingMethods();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setSingleChoiceItems(ship_methods, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ship.setMethod(i);
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                if (i == 0) {
                    shipping_date.setVisibility(View.GONE);
                    shipping_address.setVisibility(View.GONE);
                    shipping_warehouse.setVisibility(View.GONE);
                } else if (i == 1) {
                    shipping_date.setVisibility(View.VISIBLE);
                    shipping_address.setVisibility(View.GONE);
                    shipping_warehouse.setVisibility(View.VISIBLE);
                    shipping_warehouse.setText(current_warehouse_name);
                } else if (i > 1) {
                    shipping_date.setVisibility(View.VISIBLE);
                    shipping_address.setVisibility(View.VISIBLE);
                    shipping_warehouse.setVisibility(View.GONE);
                }
                ((EditText) v).setText(ship_methods[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void dialogDatePickerLight(final View v) {
        Calendar cur_calender = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                long date = newDate.getTimeInMillis();
                ((EditText) v).setText(Tools.getFormattedDateShort(date));
                ship.setDate(Tools.getFormattedDateFlat(date));
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
            }

        }, cur_calender.get(Calendar.YEAR), cur_calender.get(Calendar.MONTH), cur_calender.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    private void showShippingWarehouseDialog(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final String[] whs = getWarehouses();
        warehouse_ids = ((CheckoutActivity)getActivity()).getWarehouseIds();
        builder.setSingleChoiceItems(whs, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(whs[i]);
                try {
                    ship.setWarehouseId(Integer.parseInt(warehouse_ids.get(whs[i])));
                } catch (Exception e) {}

                ship.setWarehouseName(whs[i]);
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);

                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private String[] getWarehouses() {
        warehouse_items = ((CheckoutActivity)getActivity()).getWarehouseItems();
        String[] namesArr = warehouse_items.toArray(new String[warehouse_items.size()]);

        return namesArr;
    }
}
