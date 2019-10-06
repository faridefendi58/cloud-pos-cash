package com.slightsite.app.ui.retur;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.sale.AdapterListOrder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReturActivity extends AppCompatActivity {

    private static final String TAG = ReturActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private Sale sale;
    private int saleId;
    private SaleLedger saleLedger;
    private Customer customer;
    private Sale sale_intent;
    private Customer customer_intent;
    private Shipping shipping_intent;
    private List<Map<String, String>> lineitemList = new ArrayList<Map<String, String>>();
    private List<LineItem> cartitemList = new ArrayList<LineItem>();
    private Map<Integer, Integer> product_qty_stacks = new HashMap<Integer, Integer>();
    private Map<Integer, Double> product_price_stacks = new HashMap<Integer, Double>();
    private Map<Integer, Integer> product_retur_stacks = new HashMap<Integer, Integer>();
    private Map<Integer, Integer> stock_retur_stacks = new HashMap<Integer, Integer>();

    private String line_items_intent;
    private List<LineItem> lineItems;
    private ProductCatalog productCatalog;

    private SharedPreferences sharedpreferences;
    private Register register;
    private Boolean is_local_data = false;

    private RecyclerView lineitemListRecycle;
    private BottomSheetDialog bottomSheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retur);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        initView();

        try {
            saleLedger = SaleLedger.getInstance();
            register = Register.getInstance();
            productCatalog = Inventory.getInstance().getProductCatalog();
            /*paramCatalog = ParamService.getInstance().getParamCatalog();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();*/
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        if (getIntent().hasExtra("sale_intent")) { // has sale data from server
            sale = (Sale) getIntent().getSerializableExtra("sale_intent");
            Log.e(getClass().getSimpleName(), "sale : "+ sale.toMap().toString());
            sale_intent = sale;
            saleId = sale.getId();
            // check if any data in local db
            try {
                Sale local_sale = saleLedger.getSaleByServerInvoiceId(saleId);
                if (local_sale != null) {
                    sale = local_sale;
                    saleId = sale.getId();
                    is_local_data = true;
                }
            } catch (Exception e){e.printStackTrace();}

            if (getIntent().hasExtra("customer_intent")) {
                customer = (Customer) getIntent().getSerializableExtra("customer_intent");
                customer_intent = customer;
            }

            if (getIntent().hasExtra("line_items_intent")) { // has line item data from server
                line_items_intent = getIntent().getStringExtra("line_items_intent");
                JSONArray arrLineItems = null;
                try {
                    arrLineItems = new JSONArray(getIntent().getStringExtra("line_items_intent"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (arrLineItems != null) {
                    lineItems = new ArrayList<LineItem>();
                    for (int m = 0; m < arrLineItems.length(); m++) {
                        JSONObject line_object = null;
                        try {
                            line_object = arrLineItems.getJSONObject(m);
                            Product p = productCatalog.getProductByBarcode(line_object.getString("barcode"));
                            if (p != null) {
                                LineItem lineItem = new LineItem(
                                        p,
                                        line_object.getInt("qty"),
                                        line_object.getInt("qty")
                                );
                                lineItem.setUnitPriceAtSale(line_object.getDouble("unit_price"));
                                lineItems.add(lineItem);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    sale.setAllLineItem(lineItems);
                }
            }

            showList(sale.getAllLineItem());
        } else {
            saleId = Integer.parseInt(getIntent().getStringExtra("saleId"));
            is_local_data = true;
            try {
                sale = saleLedger.getSaleById(saleId);
                customer = saleLedger.getCustomerBySaleId(saleId);
                showList(sale.getAllLineItem());
            } catch (Exception e){e.printStackTrace();}
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.menu_share, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        lineitemListRecycle = (RecyclerView) findViewById(R.id.lineitemListRecycle);
        lineitemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        lineitemListRecycle.setHasFixedSize(true);
        lineitemListRecycle.setNestedScrollingEnabled(false);
    }

    private void showList(List<LineItem> list) {
        lineitemList = new ArrayList<Map<String, String>>();
        for(LineItem line : list) {
            product_qty_stacks.put(line.getProduct().getId(), line.getQuantity());
            product_price_stacks.put(line.getProduct().getId(), line.getPriceAtSale());
            lineitemList.add(line.toMap());
        }

        AdapterListProductRetur sAdap = new AdapterListProductRetur(ReturActivity.this, list, register);
        lineitemListRecycle.setAdapter(sAdap);
    }

    public void updateProductQtyStacks(int product_id, int qty) {
        product_qty_stacks.put(product_id, qty);
        Log.e(getClass().getSimpleName(), "product_qty_stacks : "+ product_qty_stacks.toString());
    }

    public void updateProductReturStacks(int product_id, int qty) {
        if (qty > 0) {
            product_retur_stacks.put(product_id, qty);
        } else {
            product_retur_stacks.remove(product_id);
        }
        Log.e(getClass().getSimpleName(), "product_retur_stacks : "+ product_retur_stacks.toString());
    }

    public void updateReturStockStacks(int product_id, int qty) {
        if (qty > 0) {
            stock_retur_stacks.put(product_id, qty);
        } else {
            stock_retur_stacks.remove(product_id);
        }
    }

    public Map<Integer, Integer> getReturStockStacks() {
        return stock_retur_stacks;
    }

    private Double refund_must_pay = 0.0;

    public void setRefundMustPay(Double amount) {
        this.refund_must_pay = amount;
    }

    private TextView transfer_bank_header;
    private LinearLayout transfer_bank_container;
    private Button finish_submit_button;
    private EditText cash_receive;
    private EditText nominal_bca;
    private EditText nominal_mandiri;
    private EditText nominal_bri;
    private List<PaymentItem> payment_items;
    private RecyclerView returItemListRecycle;

    public void proceedRetur(View v) {
        if (product_retur_stacks.size() > 0) {
            bottomSheetDialog = new BottomSheetDialog(ReturActivity.this);
            View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_proceed_retur, null);
            bottomSheetDialog.setContentView(sheetView);

            // get total retur price
            Double tot_price = 0.0;
            try {
                if (product_retur_stacks.size() > 0) {
                    for (Map.Entry<Integer, Integer> entry : product_retur_stacks.entrySet()) {
                        tot_price = tot_price + product_price_stacks.get(entry.getKey()) * entry.getValue();
                    }
                }
                ((TextView) sheetView.findViewById(R.id.debt_must_pay)).setText(CurrencyController.getInstance().moneyFormat(tot_price));
            } catch (Exception e){e.printStackTrace();}

            refund_must_pay = tot_price;

            transfer_bank_header = (TextView) sheetView.findViewById(R.id.transfer_bank_header);
            transfer_bank_container = (LinearLayout) sheetView.findViewById(R.id.transfer_bank_container);
            finish_submit_button = (Button) sheetView.findViewById(R.id.finish_submit_button);
            cash_receive = (EditText) sheetView.findViewById(R.id.cash_receive);
            nominal_bca = (EditText) sheetView.findViewById(R.id.nominal_bca);
            nominal_mandiri = (EditText) sheetView.findViewById(R.id.nominal_mandiri);
            nominal_bri = (EditText) sheetView.findViewById(R.id.nominal_bri);

            returItemListRecycle = (RecyclerView) sheetView.findViewById(R.id.returItemListRecycle);
            returItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
            returItemListRecycle.setHasFixedSize(true);
            returItemListRecycle.setNestedScrollingEnabled(false);

            cartitemList.clear();
            for(LineItem line : sale.getAllLineItem()) {
                if (product_retur_stacks.containsKey(line.getProduct().getId())) {
                    line.setQuantity(product_qty_stacks.get(line.getProduct().getId()));
                    cartitemList.add(line);
                }
            }

            AdapterListConfirmRetur sAdap = new AdapterListConfirmRetur(ReturActivity.this, cartitemList, register);
            sAdap.setSheetView(sheetView);
            sAdap.setTotalRefund(tot_price);
            returItemListRecycle.setAdapter(sAdap);

            bottomSheetDialog.show();

            payment_items =  new ArrayList<PaymentItem>();
            triggerBottomDialogButton(sheetView);
        } else {
            // no item to be retured
        }
    }

    private void triggerBottomDialogButton(View view) {
        setTextChangeListener(cash_receive, "cashReceive");
        setTextChangeListener(nominal_mandiri, "nominal_mandiri");
        setTextChangeListener(nominal_bca, "nominal_bca");
        setTextChangeListener(nominal_bri, "nominal_bri");

        transfer_bank_header.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (transfer_bank_container.getVisibility() == View.GONE) {
                    transfer_bank_container.setVisibility(View.VISIBLE);
                    transfer_bank_header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_black, 0);
                } else {
                    transfer_bank_container.setVisibility(View.GONE);
                    transfer_bank_header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_black, 0);
                }
            }
        });

        finish_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                payment_items.clear();
                // get all total data
                String cash = cash_receive.getText().toString();
                String bca = nominal_bca.getText().toString();
                String mandiri = nominal_mandiri.getText().toString();
                String bri = nominal_bri.getText().toString();
                Double tot_payment = 0.0;
                if (cash.length() > 0) {
                    cash = cash.replaceAll("\\.", "");
                    PaymentItem pi_cash = new PaymentItem("cash_receive", Double.parseDouble(cash));
                    payment_items.add(pi_cash);
                    tot_payment = tot_payment + Double.parseDouble(cash);
                }

                if (bca.length() > 0) {
                    if (bca.contains(".")) {
                        bca = bca.replaceAll("\\.", "");
                    }
                    PaymentItem pi_bca = new PaymentItem("nominal_bca", Double.parseDouble(bca));
                    payment_items.add(pi_bca);
                    tot_payment = tot_payment + Double.parseDouble(bca);
                }

                if (mandiri.length() > 0) {
                    if (mandiri.contains(".")) {
                        mandiri = mandiri.replaceAll("\\.", "");
                    }
                    PaymentItem pi_mandiri = new PaymentItem("nominal_mandiri", Double.parseDouble(mandiri));
                    payment_items.add(pi_mandiri);
                    tot_payment = tot_payment + Double.parseDouble(mandiri);
                }

                if (bri.length() > 0) {
                    if (bri.contains(".")) {
                        bri = bri.replaceAll("\\.", "");
                    }
                    PaymentItem pi_bri = new PaymentItem("pi_bri", Double.parseDouble(bri));
                    payment_items.add(pi_bri);
                    tot_payment = tot_payment + Double.parseDouble(bri);
                }

                if (tot_payment < refund_must_pay) {
                    Toast.makeText(getApplicationContext(),
                            "Pembayaran masih kurang " + (refund_must_pay - tot_payment),
                            Toast.LENGTH_SHORT).show();
                    payment_items.clear();
                } else {
                    // ready to submit
                    try {
                        Map<String, Object> mObj = new HashMap<String, Object>();
                        mObj.put("invoice_id", sale.getServerInvoiceId());
                        ArrayList arrPaymentList = new ArrayList();
                        for (PaymentItem pi : payment_items) {
                            Map<String, String> arrPayment2 = new HashMap<String, String>();
                            arrPayment2.put("type", pi.getTitle());
                            arrPayment2.put("amount_tendered", ""+ pi.getNominal());

                            arrPaymentList.add(arrPayment2);
                        }

                        // the items data include id, price, qty, and how much item to be changed
                        ArrayList arrRefundList = new ArrayList();
                        for (Map.Entry<Integer, Integer> entry : product_qty_stacks.entrySet()) {
                            Map<String, String> arrRefundList2 = new HashMap<String, String>();
                            arrRefundList2.put("product_id", entry.getKey()+"");
                            arrRefundList2.put("quantity", entry.getValue()+"");
                            arrRefundList2.put("price", product_price_stacks.get(entry.getKey())+"");
                            int change_item = 0;
                            if (stock_retur_stacks.containsKey(entry.getKey())) {
                                change_item = stock_retur_stacks.get(entry.getKey());
                            }
                            arrRefundList2.put("change_item", change_item+"");

                            arrRefundList.add(arrRefundList2);
                        }
                        mObj.put("payment", arrPaymentList);
                        mObj.put("items", arrRefundList);
                        Log.e(getClass().getSimpleName(), "mObj : "+ mObj.toString());
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void setTextChangeListener(final EditText etv, final String setType) {
        etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private String current_val;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current_val)){
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (cleanString.length() >= 3) {
                        etv.removeTextChangedListener(this);

                        double parsed = Double.parseDouble(cleanString);
                        String formatted = CurrencyController.getInstance().moneyFormat(parsed);

                        current_val = formatted;
                        etv.setText(formatted);
                        etv.setSelection(formatted.length());
                        etv.addTextChangedListener(this);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String cleanString = s.toString().replaceAll("[.]", "");

                    if (setType == "card_number") {
                        current_val = s.toString();
                    } else {
                        if (cleanString.length() >= 3) {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = CurrencyController.getInstance().moneyFormat(parsed);
                            current_val = formatted;
                        } else {
                            current_val = s.toString();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
