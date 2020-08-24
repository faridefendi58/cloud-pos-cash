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
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.otaliastudios.autocomplete.Autocomplete;
import com.slightsite.app.R;
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
import com.slightsite.app.domain.shipping.CargoListDialog;
import com.slightsite.app.techicalservices.AutoCompleteAdapter;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Tools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

public class ShippingFragment extends Fragment {

    private View root;
    private Autocomplete userAutocomplete;
    private Autocomplete phoneAutocomplete;
    private CustomerCatalog customerCatalog;
    private ParamCatalog paramCatalog;
    private List<Customer> customers;
    private Customer cust;
    private Register register;

    private EditText name;
    private EditText phone;
    private EditText email;
    private EditText address;
    private EditText shipping_method;
    private EditText cargo_location;
    private AutoCompleteTextView shipping_date;
    private AutoCompleteTextView shipping_warehouse;
    private EditText shipping_address;
    private EditText shipping_name;
    private EditText shipping_phone;
    private EditText shipping_invoice_number;
    private TextView inv_prefiks;
    private LinearLayout shipping_name_container;
    private LinearLayout shipping_invoice_container;
    private SwitchCompat switch_use_customer_data;

    private Checkout c_data;
    private String customer_name;
    private String customer_phone;
    private String customer_email;
    private String customer_address;
    private int selected_cust_id = -1;

    private String[] ship_methods = new String[]{};
    private String[] cargo_locations = new String[]{};

    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private String current_warehouse_name;

    private HashMap<String, Customer> customerHashMap = new HashMap<String, Customer>();

    private Shipping ship;

    private AutoCompleteTextView customer_name_autocomplete;
    private AutoCompleteTextView customer_phone_autocomplete;

    private Boolean need_time_picker = false;
    private String cargo_type = null;

    public ShippingFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_shipping, container, false);

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            ship_methods = ((CheckoutActivity)getActivity()).getShippingMethods();
            paramCatalog = ParamService.getInstance().getParamCatalog();
            register = Register.getInstance();
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
            if (customer_address == "-" || customer_address == "na") {
                customer_address = "";
            }

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
            } else {
                Params whParam = paramCatalog.getParamByName("warehouse_id");
                if (whParam instanceof Params) {
                    _shipping_warehouse_id = Integer.parseInt(whParam.getValue());
                }
            }

            String _shipping_date = getArguments().getString("shipping_date");
            String _shipping_address = getArguments().getString("shipping_address");
            String _shipping_name = getArguments().getString("shipping_name");
            String _shipping_phone = getArguments().getString("shipping_phone");
            int _shipping_method_id = Arrays.asList(ship_methods).indexOf(_shipping_method);

            if (_shipping_method != null) {
                shipping_method.setText(_shipping_method);
                // also update the shipping
                ship.setMethod(_shipping_method_id);
                ship.setWarehouseName(_shipping_warehouse);
                ship.setWarehouseId(_shipping_warehouse_id);
                ship.setDate(_shipping_date);
                ship.setName(_shipping_name);
                ship.setPhone(_shipping_phone);
                ship.setAddress(_shipping_address);
                ((CheckoutActivity)getActivity()).setShipping(ship, c_data);
            }
            shipping_warehouse.setText(_shipping_warehouse);
            shipping_date.setText(_shipping_date);
            shipping_name.setText(_shipping_name);
            shipping_phone.setText(_shipping_phone);
            shipping_address.setText(_shipping_address);
            switch_use_customer_data.setChecked(ship.getUseCustomerData());

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
        cargo_location = (EditText) root.findViewById(R.id.cargo_location);
        shipping_date = (AutoCompleteTextView) root.findViewById(R.id.shipping_date);
        shipping_address = (EditText) root.findViewById(R.id.shipping_address);
        shipping_name = (EditText) root.findViewById(R.id.shipping_name);
        shipping_phone = (EditText) root.findViewById(R.id.shipping_phone);
        shipping_invoice_number = (EditText) root.findViewById(R.id.shipping_invoice_number);
        inv_prefiks = (TextView) root.findViewById(R.id.inv_prefiks);
        shipping_warehouse = (AutoCompleteTextView) root.findViewById(R.id.shipping_warehouse);
        shipping_name_container = (LinearLayout) root.findViewById(R.id.shipping_name_container);
        shipping_invoice_container = (LinearLayout) root.findViewById(R.id.shipping_invoice_container);
        switch_use_customer_data = (SwitchCompat) root.findViewById(R.id.switch_use_customer_data);

        customer_name_autocomplete = (AutoCompleteTextView) root.findViewById(R.id.customer_name_autocomplete);
        AutoCompleteAdapter adapter = new AutoCompleteAdapter(getContext(), R.layout.spinner_item);
        adapter.notifyDataSetChanged();
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
                        Customer _cust = customerHashMap.get(actv.getText().toString());
                        if (_cust != null) {
                            cust = _cust;
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
                    customer_name = customer_name_autocomplete.getText().toString();
                    customer_email = email.getText().toString();
                    customer_phone = customer_phone_autocomplete.getText().toString();
                    customer_address = address.getText().toString();
                    int customer_id = c_data.getCustomer().getId();
                    Log.e(getClass().getSimpleName(), "c_data.getCustomer() : "+ c_data.getCustomer().toMap().toString());
                    if (selected_cust_id > 0) {
                        cust.setId(selected_cust_id);
                    }

                    if (setType == "phone") {
                        if (s.toString().length() >= 0) {
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
                            Log.e(getClass().getSimpleName(), "setType phone : "+ cust.toMap().toString());
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }
                    if (setType == "address") {
                        if (s.toString().length() >= 0) {
                            cust.setName(customer_name);
                            cust.setEmail(customer_email);
                            cust.setPhone(customer_phone);
                            cust.setAddress(s.toString());
                            Log.e(getClass().getSimpleName(), "setType address : "+ cust.toMap().toString());
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }
                    if (setType == "shipping_address") {
                        if (s.toString().length() > 5) {
                            ship.setAddress(s.toString());
                            ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                        }
                    }
                    if (setType == "shipping_name") {
                        if (s.toString().length() > 2) {
                            ship.setName(s.toString());
                            ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                        } else {
                            ship.setName(s.toString());
                        }
                    }
                    if (setType == "shipping_phone") {
                        if (s.toString().length() > 2) {
                            ship.setPhone(s.toString());
                            ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                        }
                    }

                    if (setType == "customer_name" && selected_cust_id <= 0) {
                        if (s.toString().length() >= 0) {
                            cust.setName(customer_name);
                            if (cust.getEmail().equals("email@email.com")) {
                                cust.setEmail("walkin@email.com");
                            }
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }

                    if (setType == "customer_phone" && selected_cust_id <= 0) {
                        if (s.toString().length() > 2) {
                            cust.setPhone(customer_phone);
                            cust.setEmail(customer_email);
                            ((CheckoutActivity) getActivity()).setCustomer(cust);
                        }
                    }

                    if (setType == "shipping_invoice_number") {
                        if (s.toString().length() > 2) {
                            if (ship.getMethod() == 4) {
                                ship.setInvoiceNumber("F-" + s.toString());
                            } else if (ship.getMethod() == 5) {
                                ship.setInvoiceNumber("GF-" + s.toString());
                            } else {
                                ship.setInvoiceNumber(s.toString());
                            }
                            ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
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
        //setTextChangeListener(phone, "phone");
        setTextChangeListener(customer_name_autocomplete, "customer_name");
        setTextChangeListener(customer_phone_autocomplete, "customer_phone");
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

        cargo_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCargoDialogSearch(v);
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

        setTextChangeListener(shipping_name, "shipping_name");
        setTextChangeListener(shipping_phone, "shipping_phone");
        setTextChangeListener(shipping_address, "shipping_address");
        setTextChangeListener(shipping_invoice_number, "shipping_invoice_number");

        switch_use_customer_data.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    shipping_name.setText(cust.getName());
                    shipping_phone.setText(cust.getPhone());
                    shipping_address.setText(cust.getAddress());

                    ship.setName(cust.getName());
                    ship.setPhone(cust.getPhone());
                    ship.setAddress(cust.getAddress());
                } else {
                    shipping_name.setText("");
                    shipping_phone.setText("");
                    shipping_address.setText("");

                    ship.setName("");
                    ship.setPhone("");
                    ship.setAddress("");
                }

                ship.setUseCustomerData(isChecked);
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
            }
        });
    }

    private void showShippingMethodDialog(final View v) {
        if (cust.getName().length() == 0){
            Boolean has_new_cust_data = false;
            // check once again whether user fill the new customer data
            if (customer_name_autocomplete.getText().length() > 0
                    && customer_phone_autocomplete.getText().length() >0 && address.getText().length() > 0) {
                cust.setName(customer_name_autocomplete.getText().toString());
                cust.setPhone(customer_phone_autocomplete.getText().toString());
                cust.setAddress(address.getText().toString());
                cust.setEmail(email.getText().toString());

                ((CheckoutActivity) getActivity()).setCustomer(cust);
                has_new_cust_data = true;
            }

            customer_name_autocomplete.setFocusable(true);
            if (!has_new_cust_data) {
                Toast.makeText(getActivity().getBaseContext(),
                        getResources().getString(R.string.error_empty_customer_data), Toast.LENGTH_SHORT)
                        .show();
                return;
            }
        }

        current_warehouse_name = ((CheckoutActivity)getActivity()).getCurrentWarehouseName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        int selected_method = 0;
        if (c_data.getShipping() != null) {
            selected_method = c_data.getShipping().getMethod();
        }
        builder.setSingleChoiceItems(ship_methods, selected_method, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ship.setMethod(i);
                if (cust != null && cust.getName() != null) {
                    c_data.setCustomer(cust);
                }

                c_data.setWalletTokopedia("0");
                c_data.setWalletGoFood("0");
                c_data.setWalletGrabFood("0");
                c_data.setUseGoFood(false);
                c_data.setUseGrabFood(false);
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                setupShippingForm(i);
                ((EditText) v).setText(ship_methods[i]);
                cargo_location.setVisibility(View.GONE); //just 7,8 using cargo
                if (i == 1) {
                    need_time_picker = true;
                } else if (i == 2) { //gosend
                    need_time_picker = true;
                } else if (i == 3) { //Tokopedia
                    Double tot_inv = register.getTotal();
                    c_data.setWalletTokopedia(tot_inv + "");
                    ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                } else if (i == 4) {
                    Double tot_inv = register.getTotal();
                    c_data.setWalletGoFood(tot_inv + "");
                    ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                } else if (i == 5) {
                    Double tot_inv = register.getTotal();
                    c_data.setWalletGrabFood(tot_inv + "");
                    ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                } else if (i == 7 || i == 8 || i == 9) { // cargo
                    need_time_picker = true;
                    setCargoList(i);
                } else {
                    c_data.setWalletTokopedia("0");
                    ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                }

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

                final Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                long date = newDate.getTimeInMillis();
                ((EditText) v).setText(Tools.getFormattedDateShort(date));
                ship.setDate(Tools.getFormattedDateFlat(date));
                ship.setPickupDate(Tools.getFormattedDateFlat(date));
                ((CheckoutActivity) getActivity()).setShipping(ship, c_data);

                if (need_time_picker) {
                    new TimePickerDialog(getContext(), new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                            Calendar cur_time = newDate; //Calendar.getInstance();
                            cur_time.set(Calendar.HOUR_OF_DAY, hourOfDay);
                            cur_time.set(Calendar.MINUTE, minute);
                            long date2 = cur_time.getTimeInMillis();
                            ((EditText) v).setText(Tools.getFormattedDateTimeShort(date2));
                            ship.setDate(Tools.getFormattedDateTimeFlat(date2));
                            ship.setPickupDate(Tools.getFormattedDateTimeShort(date2));
                            ((CheckoutActivity) getActivity()).setShipping(ship, c_data);
                        }
                    }, cur_calender.get(Calendar.HOUR_OF_DAY)+1, 0, false).show();
                }
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
        shipping_invoice_container.setVisibility(View.GONE);
        if (i == 0) {
            shipping_date.setVisibility(View.GONE);
            shipping_address.setVisibility(View.GONE);
            shipping_name_container.setVisibility(View.GONE);
            shipping_warehouse.setVisibility(View.GONE);
        } else if (i == 1) {
            shipping_date.setVisibility(View.VISIBLE);
            shipping_address.setVisibility(View.GONE);
            shipping_name_container.setVisibility(View.GONE);
            // hide aja pilihan warehouse krn blm ada kasus pengambilan di luar wh pembuat nota
            shipping_warehouse.setVisibility(View.GONE);
            if (current_warehouse_name != null) {
                shipping_warehouse.setText(current_warehouse_name);
            }
        } else if (i > 1) {
            if (i == 4 || i == 5) {
                shipping_date.setVisibility(View.GONE);
                String cur_time = DateTimeStrategy.getCurrentTime();
                ship.setDate(DateTimeStrategy.parseDate(cur_time, "dd-MM-yyyy"));
                ship.setPickupDate(DateTimeStrategy.parseDate(cur_time, "dd MMM yyyy HH:mm"));
                ship.setAddress(cust.getAddress());
                shipping_name_container.setVisibility(View.GONE);
                shipping_invoice_container.setVisibility(View.VISIBLE);
                if (i == 4) { //GoFood
                    inv_prefiks.setText("F-");
                    inv_prefiks.setVisibility(View.VISIBLE);
                    shipping_invoice_number.setInputType(InputType.TYPE_CLASS_NUMBER);
                } else if (i == 5) { //GrabFood
                    inv_prefiks.setText("GF-");
                    inv_prefiks.setVisibility(View.VISIBLE);
                    shipping_invoice_number.setInputType(InputType.TYPE_CLASS_NUMBER);
                }
            } else if (i == 3) { //Tokopedia
                shipping_date.setVisibility(View.VISIBLE);
                shipping_name_container.setVisibility(View.GONE);
                shipping_invoice_container.setVisibility(View.VISIBLE);
                inv_prefiks.setVisibility(View.GONE);
                shipping_invoice_number.setInputType(InputType.TYPE_CLASS_TEXT);
            } else if (i == 6) {
                shipping_date.setVisibility(View.GONE);
                shipping_address.setVisibility(View.GONE);
                shipping_name_container.setVisibility(View.GONE);
                shipping_warehouse.setVisibility(View.GONE);
            } else {
                shipping_date.setVisibility(View.VISIBLE);
                shipping_name_container.setVisibility(View.VISIBLE);
            }
            shipping_address.setVisibility(View.VISIBLE);
            shipping_warehouse.setVisibility(View.GONE);
        }
    }

    private void setupPhoneAutoComplete() {
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

    private void setCargoList(int i) {
        cargo_location.setVisibility(View.VISIBLE);
        if (i == 7) {
            this.cargo_type = "train";
        } else if (i == 8) {
            this.cargo_type = "plane";
        } else if (i == 9) {
            this.cargo_type = "bus";
        } else {
            this.cargo_type = null;
        }
        ((CheckoutActivity)getActivity()).buildCargoLocations(cargo_type);
    }

    private void showCargoDialog(final View v) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        this.cargo_locations = ((CheckoutActivity)getActivity()).getCargoLocations(cargo_type);
        builder.setSingleChoiceItems(cargo_locations, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ((EditText) v).setText(cargo_locations[i]);
                try {
                    ((CheckoutActivity)getActivity()).setCargoLocation(cargo_locations[i]);
                } catch (Exception e){e.printStackTrace();}
                dialogInterface.dismiss();
            }
        });

        builder.show();
    }

    private void showCargoDialogSearch(final View v) {
        this.cargo_locations = ((CheckoutActivity)getActivity()).getCargoLocations(cargo_type);
        CargoListDialog dialog = new CargoListDialog(getContext(), v, cargo_locations);
        dialog.show();
    }
}
