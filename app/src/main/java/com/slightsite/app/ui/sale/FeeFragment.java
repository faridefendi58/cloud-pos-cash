package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Fee;
import com.slightsite.app.domain.sale.ItemCounter;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.fee.AdapterListPaymentOn;
import com.slightsite.app.ui.fee.AdapterListSaleCounter;
import com.slightsite.app.ui.fee.FeeOnDateActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class FeeFragment extends UpdatableFragment {

    private Register register;
    private ParamCatalog paramCatalog;

    private MainActivity main;
    private ViewPager viewPager;

    private View root;

    private RecyclerView feeListRecycle;
    private RecyclerView paymentitemListView;
    private RecyclerView refundListRecycle;
    private RecyclerView saleItemListRecycle;
    private RecyclerView minOrder10ItemListRecycle;
    private RecyclerView minOrder5ItemListRecycle;
    private RecyclerView eceranItemListRecycle;
    private RecyclerView saleReturItemListRecycle;
    private LinearLayout fee_information_container;
    private LinearLayout refund_detail_container;
    private LinearLayout grosirListContainer;
    private LinearLayout semiGrosirListContainer;
    private LinearLayout eceranListContainer;
    private LinearLayout saleReturItemContainer;
    private RelativeLayout no_data_container;
    private Button button_fee_research;
    private Button button_fee_reset_to_default;

    private TextView fee_report_title;
    private TextView total_omzet;
    private TextView total_fee;
    private TextView total_transaction;

    public FeeFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
            paramCatalog = ParamService.getInstance().getParamCatalog();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        root = inflater.inflate(R.layout.fragment_fee, container, false);
        setHasOptionsMenu(true);

        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        initView();
        buildListFee();
        return root;
    }

    @Override
    public void update() {
        buildListFee();
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_filter, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_filter :
                filterDialog();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initView() {
        fee_report_title = (TextView) root.findViewById(R.id.fee_report_title);
        total_fee = (TextView) root.findViewById(R.id.total_fee);
        total_omzet = (TextView) root.findViewById(R.id.total_omzet);
        total_transaction = (TextView) root.findViewById(R.id.total_transaction);
        fee_information_container = (LinearLayout) root.findViewById(R.id.fee_information_container);
        refund_detail_container = (LinearLayout) root.findViewById(R.id.refund_detail_container);
        grosirListContainer = (LinearLayout) root.findViewById(R.id.grosirListContainer);
        semiGrosirListContainer = (LinearLayout) root.findViewById(R.id.semiGrosirListContainer);
        eceranListContainer = (LinearLayout) root.findViewById(R.id.eceranListContainer);
        saleReturItemContainer = (LinearLayout) root.findViewById(R.id.saleReturItemContainer);
        no_data_container = (RelativeLayout) root.findViewById(R.id.no_fee_data_container);
        button_fee_research = (Button) root.findViewById(R.id.button_fee_research);
        button_fee_reset_to_default = (Button) root.findViewById(R.id.button_fee_reset_to_default);

        button_fee_reset_to_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_result.clear();
                update();
            }
        });

        button_fee_research.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterDialog();
            }
        });
    }

    private String date_from;
    private String date_to;
    private String selected_month;

    private void buildListFee() {
        try {
            String ym = DateTimeStrategy.parseDate(DateTimeStrategy.getCurrentTime(), "yyyy-MM");
            int last_day = Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
            if (filter_result.size() == 0) {
                selected_month = DateTimeStrategy.parseDate(DateTimeStrategy.getCurrentTime(), "MMM yyyy");
                date_from = ym + "-01";
            } else {
                if (filter_result.containsKey("filter_month")) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date convertedDate = dateFormat.parse(filter_result.get("filter_month"));
                    Calendar c = Calendar.getInstance();

                    // convert format
                    Date d = dateFormat.parse(filter_result.get("filter_month"));
                    dateFormat.applyPattern("yyyy-MM-dd");
                    ym = DateTimeStrategy.parseDate(dateFormat.format(d), "yyyy-MM");
                    date_from = ym + "-01";
                    c.setTime(convertedDate);
                    last_day = c.getActualMaximum(Calendar.DAY_OF_MONTH);
                    selected_month = DateTimeStrategy.parseDate(dateFormat.format(d), "MMM yyyy");
                }
            }
            if (last_day < 10) {
                date_to = ym + "-0" + last_day;
            } else {
                date_to = ym + "-" + last_day;
            }
        } catch (Exception e){}

        feeListRecycle = (RecyclerView) root.findViewById(R.id.feeListRecycle);
        feeListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));
        feeListRecycle.setHasFixedSize(true);
        feeListRecycle.setNestedScrollingEnabled(false);

        paymentitemListView = (RecyclerView) root.findViewById(R.id.paymentListRecycle);
        paymentitemListView.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));
        //paymentitemListView.setHasFixedSize(true);
        //paymentitemListView.setNestedScrollingEnabled(false);

        refundListRecycle = (RecyclerView) root.findViewById(R.id.refundListRecycle);
        refundListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));
        //refundListRecycle.setHasFixedSize(true);
        //refundListRecycle.setNestedScrollingEnabled(false);

        saleItemListRecycle = (RecyclerView) root.findViewById(R.id.saleItemListRecycle);
        saleItemListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));

        minOrder10ItemListRecycle = (RecyclerView) root.findViewById(R.id.minOrder10ItemListRecycle);
        minOrder10ItemListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));

        minOrder5ItemListRecycle = (RecyclerView) root.findViewById(R.id.minOrder5ItemListRecycle);
        minOrder5ItemListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));

        eceranItemListRecycle = (RecyclerView) root.findViewById(R.id.eceranItemListRecycle);
        eceranItemListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));

        saleReturItemListRecycle = (RecyclerView) root.findViewById(R.id.saleReturItemListRecycle);
        saleReturItemListRecycle.setLayoutManager(new LinearLayoutManager(main.getApplicationContext()));

        int warehouse_id = Integer.parseInt(paramCatalog.getParamByName("warehouse_id").getValue());
        final Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_id", warehouse_id + "");
        params.put("created_at_from", date_from);
        params.put("created_at_to", date_to);

        String url = Server.URL + "transaction/list-fee?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                ArrayList<Fee> listFee = new ArrayList<Fee>();
                                ArrayList<Payment> paymentList = new ArrayList<Payment>();
                                ArrayList<Payment> refundList = new ArrayList<Payment>();
                                if (success == 1) {
                                    JSONObject data = jObj.getJSONObject("data");
                                    JSONArray items_data = data.getJSONArray("items");
                                    for(int n = 0; n < items_data.length(); n++) {
                                        JSONObject item_data = items_data.getJSONObject(n);
                                        Fee _fee = new Fee(
                                                item_data.getString("created_date"),
                                                item_data.getInt("total_transaction"),
                                                Double.parseDouble(item_data.getString("total_fee")),
                                                Double.parseDouble(item_data.getString("total_revenue")));
                                        _fee.setTotalRefund(Double.parseDouble(item_data.getString("total_refund")));
                                        listFee.add(_fee);
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    //total_omzet.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_revenue")));
                                    Double total_revenue = summary_data.getDouble("total_revenue");
                                    total_fee.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_fee")));
                                    total_transaction.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_transaction")));
                                    fee_report_title.setText(getResources().getString(R.string.title_fee_report)+" "+ selected_month);

                                    JSONObject payments = summary_data.getJSONObject("payments");
                                    Log.e(getTag(), "payments : "+ payments.toString());
                                    if (payments.length() > 0) {
                                        Iterator<String> pkeys = payments.keys();
                                        int no = 1;
                                        while(pkeys.hasNext()) {
                                            String key = pkeys.next();
                                            try {
                                                Payment pym = new Payment(no, key, payments.getDouble(key));
                                                paymentList.add(pym);
                                                no = no + 1;
                                            } catch (Exception e){}
                                        }

                                        if (summary_data.has("change_due") && summary_data.getInt("change_due") > 0) {
                                            Payment pym = new Payment(paymentList.size() + 1, "change_due", summary_data.getDouble("change_due"));
                                            paymentList.add(pym);
                                        }
                                    }

                                    if (summary_data.has("refunds")) {
                                        JSONObject refunds = summary_data.getJSONObject("refunds");
                                        Log.e(getTag(), "refunds : "+ refunds.toString());
                                        if (refunds.length() > 0) {
                                            Iterator<String> rkeys = refunds.keys();
                                            int no = 1;
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    Payment pym = new Payment(no, key, refunds.getDouble(key));
                                                    refundList.add(pym);
                                                    // merge refund data on normal payment
                                                    Payment pym_minus = new Payment(no, "refund_"+key, refunds.getDouble(key));
                                                    paymentList.add(pym_minus);
                                                    no = no + 1;
                                                    // calculate net total revenue
                                                    total_revenue = total_revenue - refunds.getDouble(key);
                                                } catch (Exception e){}
                                            }
                                        }
                                    }

                                    // omzet = revenue - refunds
                                    total_omzet.setText(CurrencyController.getInstance().moneyFormat(total_revenue));
                                }

                                if (listFee.size() > 0) {
                                    fee_information_container.setVisibility(View.VISIBLE);
                                    no_data_container.setVisibility(View.GONE);

                                    AdapterListFee adapter = new AdapterListFee(main.getApplicationContext(), listFee);
                                    feeListRecycle.setAdapter(adapter);

                                    adapter.setOnItemClickListener(new AdapterListFee.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, Fee obj, int position) {
                                            Log.e(getTag(), "position : " + position);
                                            Intent newActivity = new Intent(getActivity().getBaseContext(), FeeOnDateActivity.class);
                                            newActivity.putExtra("date", obj.getDate());
                                            startActivity(newActivity);
                                        }
                                    });

                                    AdapterListPaymentOn pAdap = new AdapterListPaymentOn(paymentList);
                                    paymentitemListView.setAdapter(pAdap);

                                    if (refundList.size() > 0) {
                                        // Deactive refund section due to combined in payment section
                                        /*AdapterListPaymentOn rfAdap = new AdapterListPaymentOn(refundList);
                                        refundListRecycle.setAdapter(rfAdap);

                                        refund_detail_container.setVisibility(View.VISIBLE);*/
                                    }
                                    buildTheItemSummary(params);
                                } else {
                                    fee_information_container.setVisibility(View.GONE);
                                    no_data_container.setVisibility(View.VISIBLE);
                                }
                            } else {
                                Toast.makeText(getContext(), "Failed!, No product data in ",
                                        Toast.LENGTH_LONG).show();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Log.e(getClass().getSimpleName(), "ada error : "+ error.getMessage());
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        try {
            AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private BottomSheetDialog bottomSheetDialog;
    private Button finish_submit_button;
    private Button button_reset_to_default;
    private Map<String, String> filter_result = new HashMap<String, String>();
    private Spinner month_spinner;
    private Spinner year_spinner;
    private String[] years;
    private int sel_month = -1;
    private int sel_year = -1;

    private void filterDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_fee, null);
        bottomSheetDialog.setContentView(sheetView);

        if (filter_result.containsKey("filter_month")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            // convert format
            try {
                Date d = dateFormat.parse(filter_result.get("filter_month"));
                dateFormat.applyPattern("yyyy-MM-dd");
            } catch (Exception e){e.printStackTrace();}
        }

        month_spinner = (Spinner) sheetView.findViewById(R.id.month_spinner);
        year_spinner = (Spinner) sheetView.findViewById(R.id.year_spinner);
        String[] months = new String[]{"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, months);
        month_spinner.setAdapter(monthAdapter);

        Calendar cur_calender = Calendar.getInstance();
        List<String> arr_years = new ArrayList<String>();
        for (int y=cur_calender.get(Calendar.YEAR)-3; y<cur_calender.get(Calendar.YEAR)+1; y++) {
            arr_years.add(y+"");
        }
        years = new String[arr_years.size()];
        years = arr_years.toArray(years);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_dropdown_item, years);
        year_spinner.setAdapter(yearAdapter);

        finish_submit_button = (Button) sheetView.findViewById(R.id.finish_submit_button);
        button_reset_to_default = (Button) sheetView.findViewById(R.id.reset_default_button);

        bottomSheetDialog.show();

        triggerBottomDialogButton(sheetView);
    }

    private void triggerBottomDialogButton(View view) {

        finish_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
                bottomSheetDialog.dismiss();
            }
        });

        button_reset_to_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_result.clear();
                sel_month = -1;
                sel_year = -1;
                update();
                bottomSheetDialog.dismiss();
            }
        });

        if (sel_month < 1) {
            month_spinner.setSelection(Calendar.getInstance().get(Calendar.MONTH));
        } else {
            month_spinner.setSelection(sel_month);
        }

        if (sel_year < 1) {
            year_spinner.setSelection(years.length - 1);
        } else {
            year_spinner.setSelection(sel_year);
        }
        month_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sel_month = i;
                int j = i+1;
                String month = j+"";
                if (j<10) {
                    month = "0"+ j;
                }
                String date = "01-" + month + "-"+ year_spinner.getSelectedItem().toString();
                filter_result.put("filter_month", date);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        year_spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sel_year = i;
                int pos = month_spinner.getSelectedItemPosition() + 1;
                String month = pos+"";
                if (pos<10) {
                    month = "0"+ pos;
                }
                String date = "01-" + month + "-"+ years[i];
                filter_result.put("filter_month", date);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void buildTheItemSummary(Map<String, String> params) {
        String url = Server.URL + "transaction/list-sale-counter?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : " + result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                // Check for error node in json
                                ArrayList<ItemCounter> listSaleEceran = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleSemiGrosir = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleGrosir = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleSummary = new ArrayList<ItemCounter>();
                                ArrayList<ItemCounter> listSaleReturSummary = new ArrayList<ItemCounter>();
                                if (success == 1) {
                                    JSONObject data = jObj.getJSONObject("data");
                                    JSONObject items_data = data.getJSONObject("items");
                                    if (items_data.has("eceran")) {
                                        JSONObject eceran = items_data.getJSONObject("eceran");
                                        if (eceran.length() > 0) {
                                            Iterator<String> rkeys = eceran.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, eceran.getInt(key));
                                                    listSaleEceran.add(_item_counter);
                                                } catch (Exception e){}
                                            }

                                            eceranListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleEceran);
                                            eceranItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            eceranListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    if (items_data.has("semi_grosir")) {
                                        JSONObject semi_grosir = items_data.getJSONObject("semi_grosir");
                                        if (semi_grosir.length() > 0) {
                                            Iterator<String> rkeys = semi_grosir.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, semi_grosir.getInt(key));
                                                    listSaleSemiGrosir.add(_item_counter);
                                                } catch (Exception e){}
                                            }
                                            semiGrosirListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleSemiGrosir);
                                            minOrder5ItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            semiGrosirListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    if (items_data.has("grosir")) {
                                        JSONObject grosir = items_data.getJSONObject("grosir");
                                        if (grosir.length() > 0) {
                                            Iterator<String> rkeys = grosir.keys();
                                            while(rkeys.hasNext()) {
                                                String key = rkeys.next();
                                                try {
                                                    ItemCounter _item_counter = new ItemCounter(key, grosir.getInt(key));
                                                    listSaleGrosir.add(_item_counter);
                                                } catch (Exception e){}
                                            }
                                            grosirListContainer.setVisibility(View.VISIBLE);
                                            AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleGrosir);
                                            minOrder10ItemListRecycle.setAdapter(smAdap);
                                        } else {
                                            grosirListContainer.setVisibility(View.GONE);
                                        }
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    if (summary_data.length() > 0) {
                                        Iterator<String> rkeys = summary_data.keys();
                                        while(rkeys.hasNext()) {
                                            String key = rkeys.next();
                                            try {
                                                ItemCounter _item_counter = new ItemCounter(key, summary_data.getInt(key));
                                                listSaleSummary.add(_item_counter);
                                            } catch (Exception e){}
                                        }
                                    }
                                    AdapterListSaleCounter smAdap = new AdapterListSaleCounter(listSaleSummary);
                                    saleItemListRecycle.setAdapter(smAdap);


                                    if (data.has("returs")) {
                                        Boolean is_json_valid = Tools.isJSONObject(data.getString("returs"));
                                        if (is_json_valid) { // check is json object
                                            JSONObject returs_data = data.getJSONObject("returs");
                                            if (returs_data.length() > 0) {
                                                Iterator<String> rkeys = returs_data.keys();
                                                while (rkeys.hasNext()) {
                                                    String key = rkeys.next();
                                                    try {
                                                        JSONObject returs_product = returs_data.getJSONObject(key);
                                                        if (returs_product.length() > 0) {
                                                            Iterator<String> rkeys2 = returs_product.keys();
                                                            while (rkeys2.hasNext()) {
                                                                String key2 = rkeys2.next();
                                                                if (returs_product.getInt(key2) < 0) {
                                                                    ItemCounter _item_counter2 = new ItemCounter(key2, returs_product.getInt(key2));
                                                                    listSaleReturSummary.add(_item_counter2);
                                                                }
                                                            }
                                                        }
                                                    } catch (Exception e) {}
                                                }
                                                if (listSaleReturSummary.size() > 0) {
                                                    AdapterListSaleCounter rtAdap = new AdapterListSaleCounter(listSaleReturSummary);
                                                    saleReturItemListRecycle.setAdapter(rtAdap);
                                                    saleReturItemContainer.setVisibility(View.VISIBLE);
                                                } else {
                                                    saleReturItemContainer.setVisibility(View.GONE);
                                                }
                                            }
                                        } else {
                                            saleReturItemContainer.setVisibility(View.GONE);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e){e.printStackTrace();}
                    }
                });
    }
}
