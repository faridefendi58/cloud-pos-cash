package com.slightsite.app.ui.customer;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.customer.CustomerCatalog;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.techicalservices.NoDaoSetException;

/**
 * UI for shows the datails of each Customer.
 * @author Farid Efendi
 *
 */
@SuppressLint("NewApi")
public class CustomerDetailActivity extends Activity {

    private CustomerCatalog customerCatalog;
    private Customer customer;
    private EditText nameBox;
    private EditText emailBox;
    private EditText phoneBox;
    private EditText addressBox;
    private Button submitEditButton;
    private Button cancelEditButton;
    private Button openEditButton;
    private TabHost mTabHost;
    private String id;
    private String[] remember;
    private AlertDialog.Builder popDialog;
    private LayoutInflater inflater ;
    private Resources res;
    private Button confirmButton;
    private Button clearButton;
    private View Viewlayout;
    private AlertDialog alert;

    private SaleLedger saleLedger;
    List<Map<String, String>> saleList;
    private ListView saleLedgerListView;
    private TextView totalBox;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        return true;
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(res.getString(R.string.product_detail));
            //actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#33B5E5")));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        res = getResources();
        initiateActionBar();

        try {
            customerCatalog = CustomerService.getInstance().getCustomerCatalog();
            saleLedger = SaleLedger.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        id = getIntent().getStringExtra("id");
        customer = customerCatalog.getCustomerById(Integer.parseInt(id));

        initUI(savedInstanceState);
        remember = new String[4];
        nameBox.setText(customer.getName());
        emailBox.setText(customer.getEmail());
        phoneBox.setText(customer.getPhone());
        addressBox.setText(customer.getAddress());

    }

    /**
     * Initiate this UI.
     * @param savedInstanceState
     */
    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_customerdetail_main);

        nameBox = (EditText) findViewById(R.id.nameBox);
        emailBox = (EditText) findViewById(R.id.emailBox);
        phoneBox = (EditText) findViewById(R.id.phoneBox);
        addressBox = (EditText) findViewById(R.id.addressBox);
        saleLedgerListView = (ListView) findViewById(R.id.orderListView);
        totalBox = (TextView) findViewById(R.id.totalBox);

        submitEditButton = (Button) findViewById(R.id.submitEditButton);
        submitEditButton.setVisibility(View.INVISIBLE);
        cancelEditButton = (Button) findViewById(R.id.cancelEditButton);
        cancelEditButton.setVisibility(View.INVISIBLE);
        openEditButton = (Button) findViewById(R.id.openEditButton);
        openEditButton.setVisibility(View.VISIBLE);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(res.getString(R.string.customer_detail))
                .setContent(R.id.tab1));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(res.getString(R.string.customer_order))
                .setContent(R.id.tab2));

        mTabHost.setCurrentTab(0);
        popDialog = new AlertDialog.Builder(this);
        inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);

        List<Sale> list = null;
        list = saleLedger.getAllSaleByCustomerId(customer.getId());
        double total = 0;
        for (Sale sale : list)
            total += sale.getTotal();

        totalBox.setText(CurrencyController.getInstance().moneyFormat(total) + "");
        showList(list);

        openEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                edit();
            }
        });

        submitEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                submitEdit();
            }
        });

        cancelEditButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                cancelEdit();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        int customerId = Integer.parseInt(id);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            case R.id.action_edit:
                edit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Submit editing.
     */
    private void submitEdit() {
        nameBox.setFocusable(false);
        nameBox.setFocusableInTouchMode(false);
        nameBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        emailBox.setFocusable(false);
        emailBox.setFocusableInTouchMode(false);
        emailBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        phoneBox.setFocusable(false);
        phoneBox.setFocusableInTouchMode(false);
        phoneBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        addressBox.setFocusable(false);
        addressBox.setFocusableInTouchMode(false);
        addressBox.setBackgroundColor(Color.parseColor("#87CEEB"));

        customer.setName(nameBox.getText().toString());
        customer.setEmail(emailBox.getText().toString());
        customer.setPhone(phoneBox.getText().toString());
        customer.setAddress(addressBox.getText().toString());
        customerCatalog.editCustomer(customer);

        submitEditButton.setVisibility(View.INVISIBLE);
        cancelEditButton.setVisibility(View.INVISIBLE);
        openEditButton.setVisibility(View.VISIBLE);
    }

    /**
     * Cancel editing.
     */
    private void cancelEdit() {
        nameBox.setFocusable(false);
        nameBox.setFocusableInTouchMode(false);
        nameBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        emailBox.setFocusable(false);
        emailBox.setFocusableInTouchMode(false);
        emailBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        phoneBox.setFocusable(false);
        phoneBox.setFocusableInTouchMode(false);
        phoneBox.setBackgroundColor(Color.parseColor("#87CEEB"));
        addressBox.setFocusable(false);
        addressBox.setFocusableInTouchMode(false);
        addressBox.setBackgroundColor(Color.parseColor("#87CEEB"));

        submitEditButton.setVisibility(View.INVISIBLE);
        cancelEditButton.setVisibility(View.INVISIBLE);
        nameBox.setText(remember[0]);
        emailBox.setText(remember[1]);
        phoneBox.setText(remember[2]);
        addressBox.setText(remember[2]);

        openEditButton.setVisibility(View.VISIBLE);
    }

    /**
     * Edit
     */
    private void edit() {
        nameBox.setFocusable(true);
        nameBox.setFocusableInTouchMode(true);
        nameBox.setBackgroundColor(Color.parseColor("#FFBB33"));
        emailBox.setFocusable(true);
        emailBox.setFocusableInTouchMode(true);
        emailBox.setBackgroundColor(Color.parseColor("#FFBB33"));
        phoneBox.setFocusable(true);
        phoneBox.setFocusableInTouchMode(true);
        phoneBox.setBackgroundColor(Color.parseColor("#FFBB33"));
        addressBox.setFocusable(true);
        addressBox.setFocusableInTouchMode(true);
        addressBox.setBackgroundColor(Color.parseColor("#FFBB33"));

        remember[0] = nameBox.getText().toString();
        remember[1] = emailBox.getText().toString();
        remember[2] = phoneBox.getText().toString();
        remember[3] = addressBox.getText().toString();

        submitEditButton.setVisibility(View.VISIBLE);
        cancelEditButton.setVisibility(View.VISIBLE);
        openEditButton.setVisibility(View.INVISIBLE);
    }

    private void showList(List<Sale> list) {

        saleList = new ArrayList<Map<String, String>>();
        for (Sale sale : list) {
            saleList.add(sale.toMap());
        }

        SimpleAdapter sAdap = new SimpleAdapter(this.getBaseContext() , saleList,
                R.layout.listview_report, new String[] { "id", "startTime", "total"},
                new int[] { R.id.sid, R.id.startTime , R.id.total});

        saleLedgerListView.setAdapter(sAdap);
    }
}
