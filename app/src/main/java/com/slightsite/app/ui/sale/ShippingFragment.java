package com.slightsite.app.ui.sale;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.otaliastudios.autocomplete.Autocomplete;
import com.otaliastudios.autocomplete.AutocompleteCallback;
import com.otaliastudios.autocomplete.AutocompletePresenter;
import com.slightsite.app.R;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.AutoCompleteAdapter;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.inventory.ProductServerActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.content.Context.INPUT_METHOD_SERVICE;

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
    private int selected_cust_id = -1;

    private String[] ship_methods = new String[]{};

    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private String current_warehouse_name;

    private HashMap<String, Customer> customerHashMap = new HashMap<String, Customer>();

    private Shipping ship;

    private AutoCompleteTextView customer_name_autocomplete;
    private AutoCompleteTextView customer_phone_autocomplete;

    public ShippingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_shipping, container, false);

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            ship_methods = ((CheckoutActivity)getActivity()).getShippingMethods();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        customers = customerCatalog.getAllCustomer();
        setupUserAutocomplete();
        setupPhoneAutoComplete();
        initAction();

        try {
            String customer_name = getArguments().getString("customer_name");
            String customer_phone = getArguments().getString("customer_phone");
            String customer_email = getArguments().getString("customer_email");
            String customer_address = getArguments().getString("customer_address");

            customer_name_autocomplete.setText(customer_name);
            customer_phone_autocomplete.setText(customer_phone);
            name.setText(customer_name);
            phone.setText(customer_phone);
            email.setText(customer_email);
            address.setText(customer_address);

            String _shipping_method = getArguments().getString("shipping_method");
            String _shipping_warehouse = getArguments().getString("shipping_warehouse_pickup");
            int _shipping_warehouse_id = 0;
            if (getArguments().get("shipping_warehouse_id") != null && getArguments().get("shipping_warehouse_id").toString() != "0") {
                _shipping_warehouse_id = getArguments().getInt("shipping_warehouse_id");
            }
            String _shipping_date = getArguments().getString("shipping_date");
            String _shipping_address = getArguments().getString("shipping_address");
            int _shipping_method_id = Arrays.asList(ship_methods).indexOf(_shipping_method);

            if (_shipping_method != null) {
                shipping_method.setText(_shipping_method);
                // also update the shipping
                ship.setMethod(_shipping_method_id);
                ship.setWarehouseName(_shipping_warehouse);
                ship.setWarehouseId(_shipping_warehouse_id);
                ship.setDate(_shipping_date);
                ship.setAddress(_shipping_address);
                ((CheckoutActivity)getActivity()).setShipping(ship, c_data);
            }
            shipping_warehouse.setText(_shipping_warehouse);
            shipping_date.setText(_shipping_date);
            shipping_address.setText(_shipping_address);

            setupShippingForm(_shipping_method_id);
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
        /*AutocompletePresenter<Customer> presenter = new UserPresenter(getContext(), customers);
        AutocompleteCallback<Customer> callback = new AutocompleteCallback<Customer>() {
            @Override
            public boolean onPopupItemClicked(Editable editable, Customer item) {
                editable.clear();
                editable.append(item.getName());
                phone.setText(item.getPhone());
                email.setText(item.getEmail());
                address.setText(item.getAddress());
                //customer_id.setText(item.getId());
                selected_cust_id = item.getId();
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
                .build();*/

        customer_name_autocomplete = (AutoCompleteTextView) root.findViewById(R.id.customer_name_autocomplete);
        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getContext(), R.layout.spinner_item);
        customer_name_autocomplete.setAdapter(adapter);
        adapter.setShippingFragment(this);

        setAutoCompleteListener(customer_name_autocomplete);
    }

    private void setAutoCompleteListener(final AutoCompleteTextView actv) {
        actv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    if (customerHashMap.size() > 0) {
                        cust = customerHashMap.get(actv.getText().toString());
                        if (cust != null) {
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                            customer_name = cust.getName();
                            customer_email = cust.getEmail();
                            customer_address = cust.getAddress();

                            name.setText(cust.getName());
                            customer_name_autocomplete.setText(cust.getName());
                            customer_phone_autocomplete.setText(cust.getPhone());
                            phone.setText(cust.getPhone());
                            email.setText(cust.getEmail());
                            if (cust.getAddress() != null) {
                                address.setText(cust.getAddress());
                            }

                            hideSoftKeyboard();
                        }
                    }
                } catch (Exception e){e.printStackTrace();}
            }
        });
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
                    int customer_id = c_data.getCustomer().getId();
                    if (selected_cust_id > 0) {
                        cust.setId(selected_cust_id);
                    }
                    Log.e(getClass().getSimpleName(), "Sebelum setup cust "+ cust.toMap().toString());
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
            //ship.setDate(Tools.getFormattedDateFlat(date));
            ship.setDate(Tools.getFormattedDateTimeFlat(date));
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

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        builder.setSingleChoiceItems(ship_methods, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ship.setMethod(i);
                if (cust != null && cust.getName() != null) {
                    c_data.setCustomer(cust);
                }
                Log.e(getTag(), "c_data pas buka dialog shipping : "+ c_data.getCustomer().toMap().toString());
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                setupShippingForm(i);
                ((EditText) v).setText(ship_methods[i]);
                dialogInterface.dismiss();
            }
        });
        builder.show();
    }

    private void dialogDatePickerLight(final View v) {
        final Calendar cur_calender = Calendar.getInstance();

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

                new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        Calendar cur_time = Calendar.getInstance();
                        cur_time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        cur_time.set(Calendar.MINUTE, minute);
                        long date2 = cur_time.getTimeInMillis();
                        ((EditText) v).setText(Tools.getFormattedDateTimeShort(date2));
                        ship.setDate(Tools.getFormattedDateFlat(date2));
                        ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                    }
                }, cur_calender.get(Calendar.HOUR_OF_DAY), cur_calender.get(Calendar.MINUTE), false).show();
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

    private void setupShippingForm(int i) {
        if (i == 0) {
            shipping_date.setVisibility(View.GONE);
            shipping_address.setVisibility(View.GONE);
            shipping_warehouse.setVisibility(View.GONE);
        } else if (i == 1) {
            shipping_date.setVisibility(View.VISIBLE);
            shipping_address.setVisibility(View.GONE);
            shipping_warehouse.setVisibility(View.VISIBLE);
            if (current_warehouse_name != null) {
                shipping_warehouse.setText(current_warehouse_name);
            }
        } else if (i > 1) {
            shipping_date.setVisibility(View.VISIBLE);
            shipping_address.setVisibility(View.VISIBLE);
            shipping_warehouse.setVisibility(View.GONE);
        }
    }

    private void setupPhoneAutoComplete() {
        /*float elevation = 6f;
        Drawable backgroundDrawable = new ColorDrawable(Color.WHITE);
        AutocompletePresenter<Customer> presenter = new PhonePresenter(getContext(), customers);
        AutocompleteCallback<Customer> callback = new AutocompleteCallback<Customer>() {
            @Override
            public boolean onPopupItemClicked(Editable editable, Customer item) {
                editable.clear();
                editable.append(item.getPhone());
                name.setText(item.getName());
                phone.setText(item.getPhone());
                email.setText(item.getEmail());
                address.setText(item.getAddress());
                selected_cust_id = item.getId();

                ((CheckoutActivity) getActivity()).setCustomer(item);
                ((CheckoutActivity) getActivity()).hideKeyboard(getActivity());
                return true;
            }

            public void onPopupVisibilityChanged(boolean shown) {}
        };

        phoneAutocomplete = Autocomplete.<Customer>on(phone)
                .with(elevation)
                .with(backgroundDrawable)
                .with(presenter)
                .with(callback)
                .build();*/

        customer_phone_autocomplete = (AutoCompleteTextView) root.findViewById(R.id.customer_phone_autocomplete);
        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getContext(), R.layout.spinner_item);
        adapter.setDelimeter("telephone");
        adapter.setShippingFragment(this);
        customer_phone_autocomplete.setAdapter(adapter);

        setAutoCompleteListener(customer_phone_autocomplete);
    }

    public void setCustomerHashMap(HashMap<String, Customer> cust_map) {
        this.customerHashMap = cust_map;
    }

    public void hideSoftKeyboard() {

        if(getActivity().getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getActivity().getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
        }
    }
}
