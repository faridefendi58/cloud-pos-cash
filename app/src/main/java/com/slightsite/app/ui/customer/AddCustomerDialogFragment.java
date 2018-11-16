package com.slightsite.app.ui.customer;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.component.UpdatableFragment;

/**
 * A dialog of adding a Customer.
 *
 * @author Farid Efendi
 *
 */
@SuppressLint("ValidFragment")
public class AddCustomerDialogFragment extends DialogFragment {

    private EditText name;
    private CustomerCatalog customerCatalog;
    private EditText email;
    private EditText phone;
    private EditText address;
    private Button confirmButton;
    private Button clearButton;
    private UpdatableFragment fragment;
    private Resources res;

    /**
     * Construct a new AddProductDialogFragment
     * @param fragment
     */
    public AddCustomerDialogFragment(UpdatableFragment fragment) {

        super();
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        View v = inflater.inflate(R.layout.layout_addcustomer, container,
                false);

        res = getResources();

        name = (EditText) v.findViewById(R.id.nameBox);
        email = (EditText) v.findViewById(R.id.email);
        phone = (EditText) v.findViewById(R.id.phone);
        address = (EditText) v.findViewById(R.id.address);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        clearButton = (Button) v.findViewById(R.id.clearButton);

        getDialog().getWindow().setTitle(res.getString(R.string.add_new_customer));

        initUI();
        return v;
    }

    /**
     * Construct a new 
     */
    private void initUI() {
        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (name.getText().toString().equals("")
                        || phone.getText().toString().equals("")
                        || address.getText().toString().equals("")) {

                    Toast.makeText(getActivity().getBaseContext(),
                            res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
                            .show();

                } else {
                    String emailBox = email.getText().toString();
                    if (emailBox.length() == 0) {
                        emailBox = "-";
                    }
                    boolean success = customerCatalog.addCustomer(
                            name.getText().toString(),
                            emailBox,
                            phone.getText().toString(),
                            address.getText().toString(), 1);

                    if (success) {
                        Toast.makeText(getActivity().getBaseContext(),
                                res.getString(R.string.success) + ", "
                                        + name.getText().toString(),
                                Toast.LENGTH_SHORT).show();

                        fragment.update();
                        clearAllBox();
                        AddCustomerDialogFragment.this.dismiss();

                    } else {
                        Toast.makeText(getActivity().getBaseContext(),
                                res.getString(R.string.fail),
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(name.getText().toString().equals("")
                        && email.getText().toString().equals("")
                        && phone.getText().toString().equals("")
                        && address.getText().toString().equals("")){
                    AddCustomerDialogFragment.this.dismiss();
                } else {
                    clearAllBox();
                }
            }
        });
    }

    /**
     * Clear all box
     */
    private void clearAllBox() {
        name.setText("");
        email.setText("");
        phone.setText("");
        address.setText("");
    }
}

