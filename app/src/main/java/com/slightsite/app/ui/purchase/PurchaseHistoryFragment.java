package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
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
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.purchase.PurchaseItem;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

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

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

@SuppressLint("ValidFragment")
public class PurchaseHistoryFragment extends UpdatableFragment {

    private MainActivity main;
    private ViewPager viewPager;
    private View fragment_view;
    private Resources res;

    private RecyclerView purchaseHistoryListView;
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseHistoryFragment.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;
    private Integer warehouse_id;
    private List<PurchaseItem> history_data = new ArrayList<PurchaseItem>();

    private BottomSheetDialog bottomSheetDialog;
    private Button finish_submit_button;
    private Button button_reset_to_default;
    private Map<String, String> filter_result = new HashMap<String, String>();
    private Spinner month_spinner;
    private Spinner year_spinner;
    private String[] years;
    private int sel_month = -1;
    private int sel_year = -1;
    private Spinner filter_status;
    private ArrayList<String> status_items = new ArrayList<String>();
    private Map<String, String> purchase_status_map = new HashMap<String, String>();
    private Map<String, String> purchase_status_map_inv = new HashMap<String, String>();

    private Spinner filter_type;
    private ArrayList<String> type_items = new ArrayList<String>();
    private Map<String, String> purchase_type_map = new HashMap<String, String>();
    private Map<String, String> purchase_type_map_inv = new HashMap<String, String>();

    public PurchaseHistoryFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_purchase_history, container, false);
        setHasOptionsMenu(true);

        fragment_view = view;

        res = getResources();

        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        initView();
        initAction();

        return view;
    }

    @Override
    public void update() {
        buildListHistory();
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
        purchaseHistoryListView = (RecyclerView) fragment_view.findViewById(R.id.purchaseHistoryListView);
        purchaseHistoryListView.setLayoutManager(new LinearLayoutManager(getContext()));
        purchaseHistoryListView.setHasFixedSize(true);
        purchaseHistoryListView.setNestedScrollingEnabled(false);
    }

    private void initAction() {
        try {
            sharedpreferences = getActivity().getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            purchase_status_map = Tools.getPurchaseStatusList();
            for (Map.Entry<String, String> entry : purchase_status_map.entrySet()) {
                status_items.add(entry.getValue());
                purchase_status_map_inv.put(entry.getValue(), entry.getKey());
            }

            purchase_type_map = Tools.getPurchaseTypeList();
            for (Map.Entry<String, String> entry : purchase_type_map.entrySet()) {
                type_items.add(entry.getValue());
                purchase_type_map_inv.put(entry.getValue(), entry.getKey());
            }
        } catch (Exception e){e.printStackTrace();}
    }

    private String date_from;
    private String date_to;
    private String selected_month;

    private void buildListHistory() {
        history_data.clear();

        final Map<String, String> params = new HashMap<String, String>();
        params.put("warehouse_id", warehouse_id + "");

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
                    params.put("date_start", date_from);
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
            params.put("date_end", date_to);
            if (filter_result.containsKey("status")) {
                if (!filter_result.get("status").equals("-")) {
                    params.put("status", filter_result.get("status"));
                }
            }

            if (filter_result.containsKey("type")) {
                if (!filter_result.get("type").equals("-")) {
                    params.put("type", filter_result.get("type"));
                }
            }
        } catch (Exception e){}

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }
        params.put("admin_id", admin_id);
        params.put("limit", "30");
        String url = Server.URL + "transfer/history?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            Log.e(getClass().getSimpleName(), "result : "+ result);
                            if (result.contains("success")) {
                                JSONObject jObj = new JSONObject(result);
                                int success = jObj.getInt("success");
                                if (success == 1) {
                                    JSONArray detail_data = jObj.getJSONArray("data");
                                    for(int n = 0; n < detail_data.length(); n++) {
                                        JSONObject detail_obj = detail_data.getJSONObject(n);
                                        PurchaseItem pi = new PurchaseItem(detail_obj.getInt("id"));
                                        pi.setCreatedAt(detail_obj.getString("created_at"));
                                        //pi.setIssueNumber(detail_obj.getString("po_number"));
                                        pi.setTitle(detail_obj.getString("title"));
                                        pi.setType(detail_obj.getString("type"));
                                        pi.setNotes(detail_obj.getString("description"));
                                        pi.setStatus(detail_obj.getString("status"));
                                        pi.setCreatedBy(detail_obj.getString("created_by_name"));
                                        history_data.add(pi);
                                    }

                                    AdapterListPurchaseHistory pAdap = new AdapterListPurchaseHistory(getContext(), history_data);
                                    Boolean is_manager = main.getIsManager();
                                    if (is_manager) {
                                        pAdap.setIsManager();
                                    }
                                    pAdap.notifyDataSetChanged();
                                    purchaseHistoryListView.setAdapter(pAdap);

                                    pAdap.setOnItemClickListener(new AdapterListPurchaseHistory.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(View view, PurchaseItem obj, int position) {
                                            Intent intent = new Intent(getContext(), PurchaseDetailActivity.class);
                                            intent.putExtra("issue_id", obj.getIssueId()+"");
                                            startActivity(intent);
                                        }
                                    });
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(getContext());
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

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
                Toast.makeText(getContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
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
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }

    private void filterDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_purchase, null);
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

        filter_status = (Spinner) sheetView.findViewById(R.id.filter_status);
        ArrayAdapter<String> stAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.spinner_item, status_items);
        stAdapter.notifyDataSetChanged();
        filter_status.setAdapter(stAdapter);

        if (filter_result.containsKey("status")) {
            int selected_status_id = 2;
            if (purchase_status_map_inv.containsKey(filter_result.get("status"))) {
                selected_status_id = status_items.indexOf(filter_result.get("status"));
            }

            filter_status.setSelection(selected_status_id);
        } else {
            int selected_status_id = status_items.indexOf("Semua");;
            filter_status.setSelection(selected_status_id);
        }

        filter_type = (Spinner) sheetView.findViewById(R.id.filter_type);
        ArrayAdapter<String> tpAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.spinner_item, type_items);
        tpAdapter.notifyDataSetChanged();
        filter_type.setAdapter(tpAdapter);

        if (filter_result.containsKey("type")) {
            int selected_type_id = 2;
            if (purchase_status_map_inv.containsKey(filter_result.get("type"))) {
                selected_type_id = type_items.indexOf(filter_result.get("type"));
            }

            filter_type.setSelection(selected_type_id);
        } else {
            int selected_type_id = type_items.indexOf("Semua");;
            filter_type.setSelection(selected_type_id);
        }

        finish_submit_button = (Button) sheetView.findViewById(R.id.finish_submit_button);
        button_reset_to_default = (Button) sheetView.findViewById(R.id.reset_default_button);

        bottomSheetDialog.show();

        triggerBottomDialogButton(sheetView);
    }

    private void triggerBottomDialogButton(View view) {

        finish_submit_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                buildListHistory();
                bottomSheetDialog.dismiss();
            }
        });

        button_reset_to_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_result.clear();
                sel_month = -1;
                sel_year = -1;
                buildListHistory();
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

        filter_status.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String status = purchase_status_map_inv.get(status_items.get(i));
                filter_result.put("status", status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        filter_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String _type = purchase_type_map_inv.get(type_items.get(i));
                filter_result.put("type", _type);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
