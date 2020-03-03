package com.slightsite.app.ui.notification;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

        initToolbar();
        initView();
        initAction();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(NotificationActivity.this, MainActivity.class);
            intent.putExtra("refreshStock", "1");
            finish();
            startActivity(intent);
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
    }

    private void initAction() {
        try {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            buildTheNotifList();
        } catch (Exception e){e.printStackTrace();}
    }

    private ArrayList<JSONObject> list_items = new ArrayList<JSONObject>();
    private Map<Integer, JSONObject> notif_data = new HashMap<Integer, JSONObject>();

    final Map<String, String> list_issues = new HashMap<String, String>();
    final Map<String, String> list_activities = new HashMap<String, String>();

    private void buildTheNotifList() {
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        params.put("admin_id", admin_id);

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

    private void itemListener(final ListView list) {
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Log.e(TAG, "click i : "+ i);
                try {
                    Intent intent2 = new Intent(getApplicationContext(), NotificationActivity.class);
                    if (notif_data.containsKey(i)) {
                        JSONObject jsonObject = notif_data.get(i);
                        if (jsonObject != null) {
                            //mark_as_viewed(id.getText().toString());
                            if (jsonObject.getString("rel_activity").equals("PurchaseHistoryActivity")) {
                                if (jsonObject.has("rel_id") && jsonObject.getInt("rel_id") > 0) {
                                    intent2 = new Intent(getApplicationContext(), PurchaseDetailActivity.class);
                                    intent2.putExtra("issue_id", jsonObject.getString("rel_id"));
                                } else {
                                    intent2 = new Intent(getApplicationContext(), PurchaseHistoryActivity.class);
                                }
                            } else if (jsonObject.getString("rel_activity").equals("MainActivity")) {
                                intent2 = new Intent(getApplicationContext(), MainActivity.class);
                            }
                        }
                    }
                    finish();
                    startActivity(intent2);
                } catch (Exception e){e.printStackTrace();}
            }
        });
    }

    private void mark_as_viewed(String notification_id)
    {
        Log.e(TAG, "Id : "+ notification_id);
        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        params.put("admin_id", admin_id);
        params.put("notification_id", notification_id);

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
}