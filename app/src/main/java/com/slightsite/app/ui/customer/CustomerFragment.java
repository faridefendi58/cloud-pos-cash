package com.slightsite.app.ui.customer;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.slightsite.app.R;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.DatabaseExecutor;
import com.slightsite.app.techicalservices.Demo;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.ButtonAdapter;
import com.slightsite.app.ui.component.UpdatableFragment;

/**
 *
 * @author Farid Efendi
 *
 */
@SuppressLint("ValidFragment")
public class CustomerFragment extends UpdatableFragment {

    protected static final int SEARCH_LIMIT = 0;
    private ListView customerListView;
    private CustomerCatalog customerCatalog;
    private List<Map<String, String>> customerList;
    private com.github.clans.fab.FloatingActionButton addCustomerButton;
    private EditText searchCustomerBox;

    private ViewPager viewPager;
    private Register register;
    private MainActivity main;

    private UpdatableFragment saleFragment;
    private Resources res;

    /**
     * Construct a new CustomerFragment.
     * @param saleFragment
     */
    public CustomerFragment(UpdatableFragment saleFragment) {
        super();
        this.saleFragment = saleFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.layout_customer, container, false);

        res = getResources();
        customerListView = (ListView) view.findViewById(R.id.customerListView);
        addCustomerButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.addCustomerButton);
        searchCustomerBox = (EditText) view.findViewById(R.id.searchCustomerBox);

        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        initUI();
        return view;
    }

    /**
     * Initiate this UI.
     */
    private void initUI() {

        addCustomerButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showPopup(v);
            }
        });

        searchCustomerBox.addTextChangedListener(new TextWatcher(){
            public void afterTextChanged(Editable s) {
                if (s.length() >= SEARCH_LIMIT) {
                    search();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){}
        });

        customerListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
                int id = Integer.parseInt(customerList.get(position).get("id").toString());

                //Log.e("Customer Fragment", "Customer ID : "+ customerCatalog.getCustomerById(id).toString());
                register.setCustomer(customerCatalog.getCustomerById(id));
                //saleFragment.update();
                viewPager.setCurrentItem(1);
                if (register.getCustomer() instanceof Customer) {
                    //Log.e("Customer Fragment", "Customer name : "+ register.getCustomer().getName());
                    TextView customer_name_box = (TextView) viewPager.findViewById(R.id.customer_name_box);
                    customer_name_box.setText(register.getCustomer().getName());
                    customer_name_box.setVisibility(View.VISIBLE);

                    TextView customer_id_box = (TextView) viewPager.findViewById(R.id.customer_id_box);
                    customer_id_box.setText(""+ id);
                }
            }
        });

    }

    /**
     * Show list.
     * @param list
     */
    private void showList(List<Customer> list) {

        customerList = new ArrayList<Map<String, String>>();
        for(Customer customer : list) {
            customerList.add(customer.toMap());
        }

        ButtonAdapter sAdap = new ButtonAdapter(getActivity().getBaseContext(), customerList,
                R.layout.listview_customer, new String[]{"name"}, new int[] {R.id.customer_name}, R.id.customerOptionView, "id");
        customerListView.setAdapter(sAdap);
    }

    /**
     * Search.
     */
    private void search() {
        String search = searchCustomerBox.getText().toString();

        if (search.equals("/demo")) {
            //testAddCustomer();
            searchCustomerBox.setText("");
        } else if (search.equals("/clear")) {
            DatabaseExecutor.getInstance().dropAllData();
            searchCustomerBox.setText("");
        }
        else if (search.equals("")) {
            try {
                showList(customerCatalog.getAllCustomer());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<Customer> result = customerCatalog.searchCustomer(search);
            showList(result);
            if (result.isEmpty()) {

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult scanningResult = IntentIntegrator.parseActivityResult(
                requestCode, resultCode, intent);

        if (scanningResult != null) {
            String scanContent = scanningResult.getContents();
            searchCustomerBox.setText(scanContent);
        } else {
            Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.fail),
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Test adding product
     */
    protected void testAddProduct() {
        Demo.testProduct(getActivity());
        Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.success),
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Show popup.
     * @param anchorView
     */
    public void showPopup(View anchorView) {
        AddCustomerDialogFragment newFragment = new AddCustomerDialogFragment(CustomerFragment.this);
        newFragment.show(getFragmentManager(), "");
    }

    @Override
    public void update() {
        search();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

}
