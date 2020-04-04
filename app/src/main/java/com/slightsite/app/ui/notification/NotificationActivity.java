package com.slightsite.app.ui.notification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.purchase.PurchaseDetailActivity;
import com.slightsite.app.ui.purchase.PurchaseHistoryActivity;

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

public class NotificationActivity extends AppCompatActivity {

    private static final String TAG = NotificationActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private RecyclerView notificationListView;
    private ParamCatalog paramCatalog;
    private Integer warehouse_id;

    ProgressDialog pDialog;
    int success;

    private BottomSheetDialog bottomSheetDialog;
    private Button finish_submit_button;
    private Button button_reset_to_default;
    private SwipeRefreshLayout swipeRefresh;
    private Map<String, String> filter_result = new HashMap<String, String>();
    private Spinner month_spinner;
    private Spinner year_spinner;
    private String[] years;
    private int sel_month = -1;
    private int sel_year = -1;
    private Spinner filter_status;
    private ArrayList<String> status_items = new ArrayList<String>();
    private Map<String, String> notif_status_map = new HashMap<String, String>();
    private Map<String, String> notif_status_map_inv = new HashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initToolbar();
        initView();
        initAction();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_filter, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            intent.putExtra("refreshStock", "1");
            finish();
            startActivity(intent);
        } else if (item.getItemId() == R.id.nav_filter) {
            filterDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_stock_purchase));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(getResources().getString(R.string.title_notification));
    }

    private void initView() {
        notificationListView = (RecyclerView) findViewById(R.id.notificationListView);
        notificationListView.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        notificationListView.setHasFixedSize(true);
        notificationListView.setNestedScrollingEnabled(false);

        swipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
    }

    private void initAction() {
        try {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            notif_status_map = Tools.getNotificationStatusList();
            for (Map.Entry<String, String> entry : notif_status_map.entrySet()) {
                status_items.add(entry.getValue());
                notif_status_map_inv.put(entry.getValue(), entry.getKey());
            }

            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    buildTheNotifList();
                    swipeRefresh.setRefreshing(false);
                }
            });
            buildTheNotifList();
        } catch (Exception e){e.printStackTrace();}
    }

    private ArrayList<JSONObject> list_items = new ArrayList<JSONObject>();
    private Map<Integer, JSONObject> notif_data = new HashMap<Integer, JSONObject>();

    final Map<String, String> list_issues = new HashMap<String, String>();
    final Map<String, String> list_activities = new HashMap<String, String>();
    private String date_from;
    private String date_to;
    private String selected_month;

    private void buildTheNotifList() {
        notif_data.clear();
        list_items.clear();

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        params.put("admin_id", admin_id);
        params.put("warehouse_id", warehouse_id+"");
        params.put("limit", "50");

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
        } catch (Exception e){}

        final ArrayList<String> descs = new ArrayList<String>();
        _string_request(
                Request.Method.GET,
                Server.URL + "notification/list?api-key=" + Server.API_KEY,
                params,
                true,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        hideDialog();
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Log.e(TAG, "result : "+ result);
                                JSONArray data = jObj.getJSONArray("data");
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = new JSONObject(data.getString(n));
                                    notif_data.put(n, data_n);
                                    list_items.add(data_n);
                                }

                                AdapterListNotification pAdap = new AdapterListNotification(NotificationActivity.this, list_items);
                                pAdap.notifyDataSetChanged();
                                notificationListView.setAdapter(pAdap);

                                pAdap.setOnItemClickListener(new AdapterListNotification.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, JSONObject jsonObject, int position) {
                                        try {
                                            Intent intent2 = new Intent(getApplicationContext(), NotificationActivity.class);
                                            if (jsonObject != null) {
                                                mark_as_viewed(jsonObject.getString("id"));
                                                if (jsonObject.getString("rel_activity").equals("PurchaseHistoryActivity")) {
                                                    if (jsonObject.has("rel_id") && jsonObject.getInt("rel_id") > 0) {
                                                        intent2 = new Intent(getApplicationContext(), PurchaseDetailActivity.class);
                                                        intent2.putExtra("issue_id", jsonObject.getString("rel_id"));
                                                        intent2.putExtra("prev_activity", "NotificationActivity");
                                                    } else {
                                                        intent2 = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
                                                        intent2.putExtra("prev_activity", "NotificationActivity");
                                                    }
                                                } else if (jsonObject.getString("rel_activity").equals("PurchaseDetailActivity")) {
                                                    if (jsonObject.has("rel_id") && jsonObject.getInt("rel_id") > 0) {
                                                        intent2 = new Intent(getApplicationContext(), PurchaseDetailActivity.class);
                                                        intent2.putExtra("issue_id", jsonObject.getString("rel_id"));
                                                        intent2.putExtra("prev_activity", "NotificationActivity");
                                                    } else {
                                                        intent2 = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
                                                        intent2.putExtra("prev_activity", "NotificationActivity");
                                                    }
                                                } else if (jsonObject.getString("rel_activity").equals("MainActivity")) {
                                                    intent2 = new Intent(getApplicationContext(), MainActivity.class);
                                                }
                                            }
                                            finish();
                                            startActivity(intent2);
                                        } catch (Exception e){e.printStackTrace();}
                                    }
                                });
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                url += "&" + pair.getKey() + "=" + pair.getValue();
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
                Log.e(TAG, "Request Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
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
        AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
    }

    public interface VolleyCallback{
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

    private void mark_as_viewed(String notification_id)
    {
        Log.e(TAG, "Id : "+ notification_id);
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        params.put("admin_id", admin_id);
        params.put("notification_id", notification_id);
        params.put("warehouse_id", warehouse_id+"");

        _string_request(
                Request.Method.POST,
                Server.URL + "notification/read?api-key=" + Server.API_KEY,
                params,
                false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        Log.e(TAG, "Response: " + result.toString());
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                Log.e(TAG, jObj.getString(TAG_MESSAGE));
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void filterDialog() {
        bottomSheetDialog = new BottomSheetDialog(NotificationActivity.this);
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

        LinearLayout transaction_type_container = (LinearLayout) sheetView.findViewById(R.id.transaction_type_container);
        transaction_type_container.setVisibility(View.GONE);
        month_spinner = (Spinner) sheetView.findViewById(R.id.month_spinner);
        year_spinner = (Spinner) sheetView.findViewById(R.id.year_spinner);
        String[] months = new String[]{"Januari", "Februari", "Maret", "April", "Mei", "Juni", "Juli", "Agustus", "September", "Oktober", "November", "Desember"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<String>(NotificationActivity.this, android.R.layout.simple_spinner_dropdown_item, months);
        month_spinner.setAdapter(monthAdapter);

        Calendar cur_calender = Calendar.getInstance();
        List<String> arr_years = new ArrayList<String>();
        for (int y=cur_calender.get(Calendar.YEAR)-3; y<cur_calender.get(Calendar.YEAR)+1; y++) {
            arr_years.add(y+"");
        }
        years = new String[arr_years.size()];
        years = arr_years.toArray(years);
        ArrayAdapter<String> yearAdapter = new ArrayAdapter<String>(NotificationActivity.this, android.R.layout.simple_spinner_dropdown_item, years);
        year_spinner.setAdapter(yearAdapter);

        filter_status = (Spinner) sheetView.findViewById(R.id.filter_status);
        ArrayAdapter<String> stAdapter = new ArrayAdapter<String>(
                NotificationActivity.this,
                R.layout.spinner_item, status_items);
        stAdapter.notifyDataSetChanged();
        filter_status.setAdapter(stAdapter);

        if (filter_result.containsKey("status")) {
            int selected_status_id = 1;
            if (notif_status_map_inv.containsKey(filter_result.get("status"))) {
                selected_status_id = status_items.indexOf(filter_result.get("status"));
            }

            filter_status.setSelection(selected_status_id);
        } else {
            int selected_status_id = status_items.indexOf("Semua");;
            filter_status.setSelection(selected_status_id);
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
                buildTheNotifList();
                bottomSheetDialog.dismiss();
            }
        });

        button_reset_to_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_result.clear();
                sel_month = -1;
                sel_year = -1;
                buildTheNotifList();
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
                String status = notif_status_map_inv.get(status_items.get(i));
                filter_result.put("status", status);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}