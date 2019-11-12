package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
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
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.fee.FeeOnDateActivity;
import com.slightsite.app.ui.printer.PrinterActivity;

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
import java.util.Locale;
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
        paymentitemListView.setHasFixedSize(true);
        paymentitemListView.setNestedScrollingEnabled(false);

        int warehouse_id = Integer.parseInt(paramCatalog.getParamByName("warehouse_id").getValue());
        Map<String, String> params = new HashMap<String, String>();
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
                                        listFee.add(_fee);
                                    }

                                    JSONObject summary_data = data.getJSONObject("summary");
                                    total_omzet.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_revenue")));
                                    total_fee.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_fee")));
                                    total_transaction.setText(CurrencyController.getInstance().moneyFormat(summary_data.getDouble("total_transaction")));
                                    fee_report_title.setText(getResources().getString(R.string.title_fee_report)+" "+ selected_month);

                                    JSONObject payments = summary_data.getJSONObject("payments");
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
                                    }
                                }

                                AdapterListFee adapter = new AdapterListFee(main.getApplicationContext(), listFee);
                                feeListRecycle.setAdapter(adapter);

                                adapter.setOnItemClickListener(new AdapterListFee.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, Fee obj, int position) {
                                        Log.e(getTag(), "position : "+ position);
                                        Intent newActivity = new Intent(getActivity().getBaseContext(), FeeOnDateActivity.class);
                                        newActivity.putExtra("date", obj.getDate());
                                        startActivity(newActivity);
                                    }
                                });

                                AdapterListPaymentSimple pAdap = new AdapterListPaymentSimple(paymentList);
                                paymentitemListView.setAdapter(pAdap);
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
    private AutoCompleteTextView filter_month;

    private void filterDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_fee, null);
        bottomSheetDialog.setContentView(sheetView);

        filter_month = (AutoCompleteTextView) sheetView.findViewById(R.id.filter_month);
        if (filter_result.containsKey("filter_month")) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            // convert format
            try {
                Date d = dateFormat.parse(filter_result.get("filter_month"));
                dateFormat.applyPattern("yyyy-MM-dd");
                filter_month.setText(DateTimeStrategy.parseDate(dateFormat.format(d), "MMM, yyyy"));
            } catch (Exception e){e.printStackTrace();}
        }

        finish_submit_button = (Button) sheetView.findViewById(R.id.finish_submit_button);
        button_reset_to_default = (Button) sheetView.findViewById(R.id.reset_default_button);

        bottomSheetDialog.show();

        triggerBottomDialogButton(sheetView);
    }

    private void triggerBottomDialogButton(View view) {
        filter_month.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight(v, "filter_month");
            }
        });

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
                update();
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void dialogDatePickerLight(final View v, final String fiter_name) {
        final Calendar cur_calender = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(),
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

                        Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        long date = newDate.getTimeInMillis();
                        ((EditText) v).setText(Tools.getFormattedDateShort(date));
                        filter_result.put(fiter_name, Tools.getFormattedDateFlat(date));

                    }

                }, cur_calender.get(Calendar.YEAR), cur_calender.get(Calendar.MONTH), cur_calender.get(Calendar.DAY_OF_MONTH));

        // modify date
        /*try {
            java.lang.reflect.Field[] datePickerDialogFields = datePickerDialog.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                Log.e(getTag(), "datePickerDialogField.getName() : "+ datePickerDialogField.getName());
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(datePickerDialog);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.e("test", datePickerField.getName());
                        if ("MODE_SPINNER".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            Log.e(getTag(), "dayPicker : "+ dayPicker.toString());
                            //((View) dayPicker).setVisibility(View.GONE);

                        }
                    }
                }
            }
        } catch (Exception ex) { ex.printStackTrace();}*/

        datePickerDialog.show();
    }
}
