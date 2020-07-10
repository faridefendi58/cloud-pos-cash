package com.slightsite.app.ui.customer;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.BottomSheetDialog;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressLint("ValidFragment")
public class CustomerFragment extends UpdatableFragment {

    protected static final int SEARCH_LIMIT = 0;

    private ViewPager viewPager;
    private Register register;
    private MainActivity main;

    private Resources res;

    private Map<Integer, Integer> stacks = new HashMap<Integer, Integer>();

    private View fragment_view;
    private static final String TAG = CustomerFragment.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private RecyclerView customerListView;
    private ParamCatalog paramCatalog;
    private Integer warehouse_id;

    ProgressDialog pDialog;
    int success;

    private BottomSheetDialog bottomSheetDialog;
    private Button finish_submit_button;
    private Button button_reset_to_default;
    private Map<String, String> filter_result = new HashMap<String, String>();
    private SwipeRefreshLayout swipeRefresh;
    private Spinner filter_type;
    private EditText filter_customer_name;
    private EditText filter_customer_phone;
    private EditText filter_customer_email;
    private Spinner filter_order_by;
    private ArrayList<String> customer_type_items = new ArrayList<String>();
    private Map<String, String> customer_type_map = new HashMap<String, String>();
    private Map<String, String> customer_type_map_inv = new HashMap<String, String>();
    private ArrayList<String> customer_order_items = new ArrayList<String>();
    private Map<String, String> customer_order_map = new HashMap<String, String>();
    private Map<String, String> customer_order_map_inv = new HashMap<String, String>();

    public CustomerFragment() {
        super();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        View view = inflater.inflate(R.layout.layout_customer, container, false);
        setHasOptionsMenu(true);
        fragment_view = view;
        res = getResources();
        main = (MainActivity) getActivity();
        viewPager = main.getViewPager();

        initView();
        initAction();
        return view;
    }

    private void initView() {
        customerListView = (RecyclerView) fragment_view.findViewById(R.id.customerListView);
        customerListView.setLayoutManager(new LinearLayoutManager(getContext()));
        customerListView.setHasFixedSize(true);
        customerListView.setNestedScrollingEnabled(false);

        swipeRefresh = (SwipeRefreshLayout) fragment_view.findViewById(R.id.swipeRefresh);
    }

    private void initAction() {
        try {
            sharedpreferences = main.getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            customer_type_items.clear();
            customer_type_items.add("All Customer");
            customer_type_map_inv.clear();
            customer_type_map_inv.put("All Customer", "");
            customer_type_map = Tools.getCustomerTypeList();
            for (Map.Entry<String, String> entry : customer_type_map.entrySet()) {
                customer_type_items.add(entry.getValue());
                customer_type_map_inv.put(entry.getValue(), entry.getKey());
            }

            customer_order_items.clear();
            customer_type_map_inv.clear();
            customer_order_map = Tools.getCustomerOrderList();
            for (Map.Entry<String, String> entry : customer_order_map.entrySet()) {
                customer_order_items.add(entry.getValue());
                customer_order_map_inv.put(entry.getValue(), entry.getKey());
            }
            //update();
            swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    update();
                    swipeRefresh.setRefreshing(false);
                }
            });
        } catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void update() {
        buildTheCustomerList(false);
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
                return false;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private ArrayList<JSONObject> list_items = new ArrayList<JSONObject>();
    private Map<Integer, JSONObject> customer_data = new HashMap<Integer, JSONObject>();

    private void buildTheCustomerList(final Boolean show_dialog) {
        customer_data.clear();
        list_items.clear();

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString("id", null);
        params.put("admin_id", admin_id);
        params.put("limit", "50");

        try {
            if (filter_result.containsKey("group_id")) {
                if ((filter_result.get("group_id") != null) && !filter_result.get("group_id").equals("-")) {
                    params.put("group_id", filter_result.get("group_id"));
                }
            }
            if (filter_result.containsKey("name")) {
                if (filter_result.get("name") != null) {
                    params.put("name", filter_result.get("name"));
                }
            }
            if (filter_result.containsKey("email")) {
                if (filter_result.get("email") != null) {
                    params.put("email", filter_result.get("email"));
                }
            }
            if (filter_result.containsKey("order_by")) {
                if ((filter_result.get("order_by") != null) && !filter_result.get("order_by").equals("-")) {
                    params.put("order_by", filter_result.get("order_by"));
                }
            }
        } catch (Exception e){e.printStackTrace();}
        Log.e(TAG, "search params : "+ params.toString());

        _string_request(
                Request.Method.GET,
                Server.URL + "customer/list?api-key=" + Server.API_KEY,
                params,
                show_dialog,
                new CustomerDetailActivity.VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        if (show_dialog) {
                            hideDialog();
                        }
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
                                    customer_data.put(n, data_n);
                                    list_items.add(data_n);
                                }

                                AdapterListCustomer pAdap = new AdapterListCustomer(getContext(), list_items);
                                pAdap.notifyDataSetChanged();
                                customerListView.setAdapter(pAdap);

                                pAdap.setOnItemClickListener(new AdapterListCustomer.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(View view, JSONObject jsonObject, int position) {
                                        try {
                                            Intent intent2 = new Intent(getContext(), CustomerDetailActivity.class);
                                            if (jsonObject != null) {
                                                intent2 = new Intent(getContext(), CustomerDetailActivity.class);
                                                intent2.putExtra("id", jsonObject.getString("id"));
                                                int current_tab = ((MainActivity)getActivity()).getCurrentTabPosition();
                                                intent2.putExtra("fragment", current_tab +"");
                                            }
                                            main.finish();
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

    public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final CustomerDetailActivity.VolleyCallback callback) {
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
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
    }

    private void hideDialog() {
        if (pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }

    private void filterDialog() {
        bottomSheetDialog = new BottomSheetDialog(getContext());
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_filter_customer, null);
        bottomSheetDialog.setContentView(sheetView);

        filter_type = (Spinner) sheetView.findViewById(R.id.filter_type);
        ArrayAdapter<String> stAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.spinner_item, customer_type_items);
        stAdapter.notifyDataSetChanged();
        filter_type.setAdapter(stAdapter);

        filter_customer_name = (EditText) sheetView.findViewById(R.id.customer_name);
        filter_customer_phone = (EditText) sheetView.findViewById(R.id.customer_phone);
        filter_customer_email = (EditText) sheetView.findViewById(R.id.customer_email);
        filter_order_by = (Spinner) sheetView.findViewById(R.id.filter_order_by);
        ArrayAdapter<String> obAdapter = new ArrayAdapter<String>(
                getContext(),
                R.layout.spinner_item, customer_order_items);
        stAdapter.notifyDataSetChanged();
        filter_order_by.setAdapter(obAdapter);

        if (filter_result.containsKey("group_id")) {
            int selected_status_id = 1;
            if (customer_type_map_inv.containsKey(filter_result.get("group_id"))) {
                selected_status_id = customer_type_items.indexOf(filter_result.get("group_id"));
            }

            filter_type.setSelection(selected_status_id);
        } else {
            int selected_status_id = customer_type_items.indexOf("Semua");;
            filter_type.setSelection(selected_status_id);
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
                String cust_name = filter_customer_name.getText().toString();
                if (cust_name.length() > 0) {
                    filter_result.put("name", cust_name);
                }
                String cust_phone = filter_customer_phone.getText().toString();
                if (cust_phone.length() > 0) {
                    filter_result.put("telephone", cust_phone);
                }
                String cust_email = filter_customer_email.getText().toString();
                if (cust_email.length() > 0) {
                    filter_result.put("email", cust_email);
                }
                buildTheCustomerList(true);
                bottomSheetDialog.dismiss();
            }
        });

        button_reset_to_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filter_result.clear();
                buildTheCustomerList(true);
                bottomSheetDialog.dismiss();
            }
        });

        filter_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String group_id = customer_type_map_inv.get(customer_type_items.get(i));
                filter_result.put("group_id", group_id);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        filter_order_by.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String o_by = customer_order_map_inv.get(customer_order_items.get(i));
                filter_result.put("order_by", o_by);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }
}
