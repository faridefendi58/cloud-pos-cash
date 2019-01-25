package com.slightsite.app.ui.sale;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
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
import com.slightsite.app.techicalservices.NoDaoSetException;

import java.util.List;

public class ShippingFragment extends Fragment {

    private View root;
    private Autocomplete userAutocomplete;
    private CustomerCatalog customerCatalog;
    private List<Customer> customers;


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

        return root;
    }

    private void setupUserAutocomplete() {
        EditText edit = (EditText) root.findViewById(R.id.customer_name);
        final EditText phone = (EditText) root.findViewById(R.id.customer_phone);
        final EditText email = (EditText) root.findViewById(R.id.customer_email);
        final EditText address = (EditText) root.findViewById(R.id.customer_address);
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
                Log.e(getTag(), "customer id : "+ item.getId());
                //customer_id.setText(item.getId());
                return true;
            }

            public void onPopupVisibilityChanged(boolean shown) {}
        };

        userAutocomplete = Autocomplete.<Customer>on(edit)
                .with(elevation)
                .with(backgroundDrawable)
                .with(presenter)
                .with(callback)
                .build();
    }
}
