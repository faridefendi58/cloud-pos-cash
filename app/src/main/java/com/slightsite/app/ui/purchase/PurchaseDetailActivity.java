package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.internal.safeparcel.SafeParcelable;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.notification.NotificationActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

public class PurchaseDetailActivity extends Activity {
    ProgressDialog pDialog;
    int success;

    private static final String TAG = PurchaseDetailActivity.class.getSimpleName();
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    private SharedPreferences sharedpreferences;
    private ParamCatalog paramCatalog;
    private Integer warehouse_id;
    private ActionBar actionBar;
    private String issue_id;
    private String issue_formated_number;
    private TextView label_stock_in_out_number;
    private TextView issue_number;
    private TextView label_date_in_out;
    private AutoCompleteTextView created_at;
    private TextView created_at_txt;
    private TextView label_received_by;
    private TextView created_by;
    private TextView label_origin_destination;
    private TextView origin_destination;
    private RecyclerView itemListRecycle;
    private LinearLayout complete_button_container;
    private Button btn_confirm;
    private Button btn_update;
    private Button btn_cancel;
    private Button btn_remove;
    private LinearLayout notes_header_container;
    private CardView notes_container;
    private TextView label_notes;
    private EditText purchase_notes;
    private List<PurchaseLineItem> purchase_data = new ArrayList<PurchaseLineItem>();
    private List<PurchaseLineItem> purchase_data2 = new ArrayList<PurchaseLineItem>();
    private String purchase_date;
    private Boolean do_update_data = false;
    private JSONObject server_data = new JSONObject();
    // related data
    private CardView related_data_container;
    private TextView label_stock_in_out_number_rel;
    private TextView issue_number_rel;
    private TextView label_date_in_out_rel;
    private TextView created_at_rel;
    private TextView label_received_by_rel;
    private TextView created_by_rel;
    private TextView label_origin_destination_rel;
    private TextView origin_destination_rel;
    private TextView verified_by_rel;
    private Boolean is_update_qty = false;
    private Boolean is_manager = false;
    private Class prev_activity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
            paramCatalog = ParamService.getInstance().getParamCatalog();
            Params whParam = paramCatalog.getParamByName("warehouse_id");
            if (whParam != null) {
                warehouse_id = Integer.parseInt(whParam.getValue());
            }
            String role = paramCatalog.getParamByName("role").getValue();
            Log.e("CUK", "role : "+ role);
            if (role != null && role.equals("manager")) {
                is_manager = true;
            }
        } catch (Exception e){e.printStackTrace();}

        if (getIntent().hasExtra("issue_id")) { // has sale data from server
            this.issue_id = getIntent().getStringExtra("issue_id");
            try {
                getDetailFromServer();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (getIntent().hasExtra("prev_activity")) {
            if (getIntent().getStringExtra("prev_activity").equals("NotificationActivity")) {
                this.prev_activity = NotificationActivity.class;
            } else {
                this.prev_activity = MainActivity.class;
            }
        }

        initUI(savedInstanceState);
        initAction();
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.title_stock_in_out));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    private void initUI(Bundle savedInstanceState) {
        setContentView(R.layout.layout_purchase_detail);

        label_stock_in_out_number = (TextView) findViewById(R.id.label_stock_in_out_number);
        issue_number = (TextView) findViewById(R.id.issue_number);
        label_date_in_out = (TextView) findViewById(R.id.label_date_in_out);
        created_at = (AutoCompleteTextView) findViewById(R.id.created_at);
        created_at_txt = (TextView) findViewById(R.id.created_at_txt);
        label_received_by = (TextView) findViewById(R.id.label_received_by);
        created_by = (TextView) findViewById(R.id.created_by);
        label_origin_destination = (TextView) findViewById(R.id.label_origin_destination);
        origin_destination = (TextView) findViewById(R.id.origin_destination);
        complete_button_container = (LinearLayout) findViewById(R.id.complete_button_container);
        btn_confirm = (Button) findViewById(R.id.btn_confirm);
        btn_update = (Button) findViewById(R.id.btn_update);
        btn_cancel = (Button) findViewById(R.id.btn_cancel);
        btn_remove = (Button) findViewById(R.id.btn_remove);
        notes_header_container = (LinearLayout) findViewById(R.id.notes_header_container);
        notes_container = (CardView) findViewById(R.id.notes_container);
        label_notes = (TextView) findViewById(R.id.label_notes);
        purchase_notes = (EditText) findViewById(R.id.purchase_notes);

        // related data
        related_data_container = (CardView) findViewById(R.id.related_data_container);
        label_stock_in_out_number_rel = (TextView) findViewById(R.id.label_stock_in_out_number_rel);
        issue_number_rel = (TextView) findViewById(R.id.issue_number_rel);
        label_date_in_out_rel = (TextView) findViewById(R.id.label_date_in_out_rel);
        created_at_rel = (TextView) findViewById(R.id.created_at_rel);
        label_received_by_rel = (TextView) findViewById(R.id.label_received_by_rel);
        created_by_rel = (TextView) findViewById(R.id.created_by_rel);
        label_origin_destination_rel = (TextView) findViewById(R.id.label_origin_destination_rel);
        origin_destination_rel = (TextView) findViewById(R.id.origin_destination_rel);
        verified_by_rel = (TextView) findViewById(R.id.verified_by_rel);
        // endof related data

        initiateActionBar();

        itemListRecycle = (RecyclerView) findViewById(R.id.itemListRecycle);
        itemListRecycle.setLayoutManager(new LinearLayoutManager(getBaseContext()));
        itemListRecycle.setHasFixedSize(true);
        itemListRecycle.setNestedScrollingEnabled(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (prev_activity == null) {
                    finish();
                } else {
                    Intent _intent = new Intent(getApplicationContext(), prev_activity);
                    finish();
                    startActivity(_intent);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void getDetailFromServer() {
        Map<String, String> params = new HashMap<String, String>();

        String admin_id = sharedpreferences.getString(TAG_ID, null);
        params.put("admin_id", admin_id);
        params.put("id", issue_id + "");

        String url = Server.URL + "transfer/history-detail?api-key=" + Server.API_KEY;
        _string_request(
                Request.Method.GET,
                url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            Log.e(getClass().getSimpleName(), "jObj : " + jObj.toString());
                            // Check for error node in json
                            if (success == 1) {
                                server_data = jObj.getJSONObject("data");
                                if (server_data != null) {
                                    String status = "Pending Confirmation";
                                    if (server_data.getInt("status") == 0) {
                                        status = "Pending Confirmation";
                                        complete_button_container.setVisibility(View.VISIBLE);
                                        if (server_data.has("group_master") && server_data.getInt("group_master") > 0) {
                                            btn_update.setVisibility(View.VISIBLE);
                                        }
                                        btn_cancel.setVisibility(View.VISIBLE);
                                        if (server_data.has("group_master") && server_data.getInt("group_master") == 0) {
                                            btn_confirm.setVisibility(View.VISIBLE);
                                        }
                                        if (is_manager) {
                                            complete_button_container.setVisibility(View.VISIBLE);
                                            btn_cancel.setVisibility(View.VISIBLE);
                                            btn_confirm.setVisibility(View.VISIBLE);
                                            btn_update.setVisibility(View.GONE);
                                        }
                                    } else if (server_data.getInt("status") == 1) {
                                        status = "Complete";
                                        created_at.setVisibility(View.GONE);
                                        created_at_txt.setVisibility(View.VISIBLE);
                                        purchase_notes.setVisibility(View.GONE);
                                    } else if (server_data.getInt("status") == -1) {
                                        status = "Need Check";
                                        created_at.setVisibility(View.GONE);
                                        created_at_txt.setVisibility(View.VISIBLE);
                                        if (is_manager) {
                                            complete_button_container.setVisibility(View.VISIBLE);
                                            btn_cancel.setVisibility(View.VISIBLE);
                                            btn_confirm.setVisibility(View.VISIBLE);
                                            btn_update.setVisibility(View.GONE);
                                        }
                                    } else if (server_data.getInt("status") == -2) {
                                        status = "Canceled";
                                        complete_button_container.setVisibility(View.VISIBLE);
                                        btn_remove.setVisibility(View.VISIBLE);
                                        created_at.setVisibility(View.GONE);
                                        created_at_txt.setVisibility(View.VISIBLE);
                                    }
                                    JSONObject detail_data = new JSONObject();
                                    if (server_data.has("type")) {
                                        if (server_data.getString("type").equals("stock_in")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_in_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_in));
                                            label_received_by.setText(getResources().getString(R.string.label_received_by));
                                            if (server_data.has("group_master") && server_data.getInt("group_master") == 0) {
                                                label_received_by.setText(getResources().getString(R.string.label_sent_by));
                                            }
                                            label_origin_destination.setText(getResources().getString(R.string.label_stock_origin));
                                            if (server_data.has("warehouse_from_name")) {
                                                origin_destination.setText(server_data.getString("warehouse_from_name"));
                                            }
                                            actionBar.setTitle(getResources().getString(R.string.label_stock_in)+" ("+ status +")");
                                        } else if (server_data.getString("type").equals("stock_out")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_out_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_out));
                                            label_received_by.setText(getResources().getString(R.string.label_received_by));
                                            if (server_data.has("group_master") && server_data.getInt("group_master") > 0) {
                                                label_received_by.setText(getResources().getString(R.string.label_sent_by));
                                            }
                                            label_origin_destination.setText(getResources().getString(R.string.label_stock_destination));
                                            if (server_data.has("warehouse_to_name")) {
                                                origin_destination.setText(server_data.getString("warehouse_to_name"));
                                            }
                                            actionBar.setTitle(getResources().getString(R.string.label_stock_out)+" ("+ status +")");
                                        } else if (server_data.getString("type").equals("transfer_receipt")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_in_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_in));
                                            label_received_by.setText(getResources().getString(R.string.label_received_by));
                                            label_origin_destination.setText(getResources().getString(R.string.label_stock_origin));
                                            if (server_data.has("warehouse_to_name")) {
                                                label_origin_destination.setText("");
                                                origin_destination.setText(server_data.getString("warehouse_to_name"));
                                            }
                                            if (server_data.has("title")) {
                                                actionBar.setTitle(server_data.getString("title"));
                                            } else {
                                                actionBar.setTitle(getResources().getString(R.string.label_stock_in));
                                            }
                                            if (server_data.has("detail")) {
                                                detail_data = server_data.getJSONObject("detail");
                                                if (detail_data.has("tr_number")) {
                                                    issue_number.setText(detail_data.getString("tr_number"));
                                                    issue_formated_number = detail_data.getString("tr_number");
                                                }
                                            }
                                        } else if (server_data.getString("type").equals("transfer_issue")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_out_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_out));
                                            label_received_by.setText(getResources().getString(R.string.label_sent_by));
                                            label_origin_destination.setText(getResources().getString(R.string.label_stock_destination));
                                            if (server_data.has("warehouse_from_name")) {
                                                label_origin_destination.setText("");
                                                origin_destination.setText(server_data.getString("warehouse_from_name"));
                                            }
                                            if (server_data.has("title")) {
                                                actionBar.setTitle(server_data.getString("title"));
                                            } else {
                                                actionBar.setTitle(getResources().getString(R.string.label_stock_out));
                                            }
                                            if (server_data.has("detail")) {
                                                detail_data = server_data.getJSONObject("detail");
                                                if (detail_data.has("ti_number")) {
                                                    issue_number.setText(detail_data.getString("ti_number"));
                                                    issue_formated_number = detail_data.getString("ti_number");
                                                }
                                            }
                                        }  else if (server_data.getString("type").equals("inventory_issue")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_out_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_out));
                                            label_received_by.setText(getResources().getString(R.string.label_processed_by));
                                            LinearLayout origin_destination_container = (LinearLayout) findViewById(R.id.origin_destination_container);
                                            origin_destination_container.setVisibility(View.GONE);
                                            if (server_data.has("title")) {
                                                actionBar.setTitle(server_data.getString("title"));
                                            } else {
                                                actionBar.setTitle(getResources().getString(R.string.label_stock_out));
                                            }
                                            if (server_data.has("detail")) {
                                                detail_data = server_data.getJSONObject("detail");
                                                if (detail_data.has("ii_number")) {
                                                    issue_number.setText(detail_data.getString("ii_number"));
                                                    issue_formated_number = detail_data.getString("ii_number");
                                                }
                                            }
                                        } else if (server_data.getString("type").equals("purchase_order")) {
                                            label_stock_in_out_number.setText(getResources().getString(R.string.label_stock_in_number));
                                            label_date_in_out.setText(getResources().getString(R.string.label_date_in));
                                            label_received_by.setText(getResources().getString(R.string.label_received_by));
                                            label_origin_destination.setText(getResources().getString(R.string.label_stock_origin));

                                            if (server_data.has("title")) {
                                                actionBar.setTitle(server_data.getString("title"));
                                            } else {
                                                actionBar.setTitle(getResources().getString(R.string.label_stock_in));
                                            }
                                            if (server_data.has("detail")) {
                                                detail_data = server_data.getJSONObject("detail");
                                                if (detail_data.has("po_number")) {
                                                    issue_number.setText(detail_data.getString("po_number"));
                                                    issue_formated_number = detail_data.getString("po_number");
                                                }
                                                if (detail_data.has("supplier_name")) {
                                                    origin_destination.setText(detail_data.getString("supplier_name"));
                                                }
                                            }
                                        }
                                    }
                                    if (server_data.has("tr_number")) {
                                        issue_number.setText(server_data.getString("tr_number"));
                                        issue_formated_number = server_data.getString("tr_number");
                                    } else if (server_data.has("ti_number")) {
                                        issue_number.setText(server_data.getString("ti_number"));
                                        issue_formated_number = server_data.getString("ti_number");
                                    } else if (server_data.has("ii_number")) {
                                        issue_number.setText(server_data.getString("ii_number"));
                                        issue_formated_number = server_data.getString("ii_number");
                                    } else if (server_data.has("issue_number")) {
                                        issue_number.setText(server_data.getString("issue_number"));
                                        issue_formated_number = server_data.getString("issue_number");
                                    }

                                    if (server_data.has("created_at")) {
                                        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
                                        String _created_at = DateTimeStrategy.parseDate(server_data.getString("created_at"), "yyyy-MM-dd");
                                        created_at.setText(_created_at);
                                        created_at_txt.setText(_created_at);
                                    }

                                    if (server_data.has("created_by_name")) {
                                        created_by.setText(server_data.getString("created_by_name"));
                                    }
                                    if (server_data.has("finished_by_name")
                                            && server_data.getString("finished_by_name").length() > 0
                                            && !server_data.getString("finished_by_name").equals("null")) {
                                        created_by.setText(server_data.getString("finished_by_name"));
                                    }

                                    JSONObject configs = server_data.getJSONObject("configs");
                                    if (configs.has("items")) {
                                        JSONArray items = configs.getJSONArray("items");
                                        if (items.length() > 0) {
                                            for (int i = 0; i < items.length(); i++) {
                                                JSONObject data_n = items.getJSONObject(i);
                                                Product product = new Product(data_n.getInt("barcode"), data_n.getString("title"), data_n.getString("barcode"), data_n.getDouble("unit_price"));
                                                PurchaseLineItem lineItem = new PurchaseLineItem(product, data_n.getInt("quantity"), data_n.getInt("quantity"));
                                                purchase_data.add(lineItem);
                                            }
                                        }
                                    }

                                    if (configs.has("effective_date")) {
                                        created_at.setText(configs.getString("effective_date"));
                                        created_at_txt.setText(configs.getString("effective_date"));
                                    }

                                    if (server_data.getInt("status") == 0 || server_data.getInt("status") == -1) {
                                        notes_header_container.setVisibility(View.VISIBLE);
                                        notes_container.setVisibility(View.VISIBLE);
                                        if (configs.has("notes")) {
                                            label_notes.setText(configs.getString("notes"));
                                        }
                                    } else {
                                        if (configs.has("notes")) {
                                            notes_header_container.setVisibility(View.VISIBLE);
                                            notes_container.setVisibility(View.VISIBLE);
                                            label_notes.setText(configs.getString("notes"));
                                        }
                                    }

                                    if (purchase_data.size() > 0) {
                                        AdapterListPurchaseConfirm pAdap = new AdapterListPurchaseConfirm(PurchaseDetailActivity.this, purchase_data);
                                        pAdap.setIsDetail();
                                        if (server_data.getInt("status") == 1 || server_data.getInt("status") == -2 || server_data.getInt("status") == -1) {
                                            pAdap.setIsEditable(false);
                                            // no need to show price for security reason
                                            /*if (server_data.getString("type").equals("purchase_order")) {
                                                pAdap.showPriceText();
                                            }*/
                                        }
                                        pAdap.notifyDataSetChanged();
                                        itemListRecycle.setAdapter(pAdap);
                                    }

                                    if (server_data.has("related")) {
                                        JSONObject related_data = server_data.getJSONObject("related");
                                        if (related_data != null) {
                                            related_data_container.setVisibility(View.VISIBLE);
                                            if (related_data.getString("type").equals("transfer_issue")) {
                                                label_stock_in_out_number_rel.setText(getResources().getString(R.string.label_stock_out_number));
                                                if (related_data.has("issue_number")) {
                                                    issue_number_rel.setText(related_data.getString("issue_number"));
                                                }
                                                label_date_in_out_rel.setText(getResources().getString(R.string.label_date_out));
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
                                                String _created_at = DateTimeStrategy.parseDate(related_data.getString("created_at"), "yyyy-MM-dd");
                                                created_at_rel.setText(_created_at);
                                                if (related_data.has("configs")) {
                                                    JSONObject configs_rel = related_data.getJSONObject("configs");
                                                    if (configs_rel.has("effective_date")) {
                                                        created_at_rel.setText(configs_rel.getString("effective_date"));
                                                    }
                                                }
                                                label_received_by_rel.setText(getResources().getString(R.string.label_sent_by));
                                                if (related_data.has("finished_by_name")
                                                        && related_data.getString("finished_by_name").length() > 0
                                                        && related_data.getInt("group_master") == 0) {
                                                    created_by_rel.setText(related_data.getString("finished_by_name"));
                                                } else {
                                                    created_by_rel.setText(related_data.getString("created_by_name"));
                                                }
                                                label_origin_destination_rel.setText(getResources().getString(R.string.label_stock_origin));
                                                origin_destination_rel.setText(related_data.getString("warehouse_from_name"));
                                                if (related_data.has("checked_by_name") && !related_data.getString("checked_by_name").equals("null")) {
                                                    verified_by_rel.setText(related_data.getString("checked_by_name"));
                                                }
                                            } else if (related_data.getString("type").equals("transfer_receipt")) {
                                                label_stock_in_out_number_rel.setText(getResources().getString(R.string.label_stock_in_number));
                                                if (related_data.has("issue_number")) {
                                                    issue_number_rel.setText(related_data.getString("issue_number"));
                                                }
                                                label_date_in_out_rel.setText(getResources().getString(R.string.label_date_in));
                                                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd  hh:mm:ss");
                                                String _created_at = DateTimeStrategy.parseDate(related_data.getString("created_at"), "yyyy-MM-dd");
                                                created_at_rel.setText(_created_at);
                                                if (related_data.has("configs")) {
                                                    JSONObject configs_rel = related_data.getJSONObject("configs");
                                                    if (configs_rel.has("effective_date")) {
                                                        created_at_rel.setText(configs_rel.getString("effective_date"));
                                                    }
                                                }
                                                label_received_by_rel.setText(getResources().getString(R.string.label_received_by));
                                                if (related_data.has("finished_by_name") && related_data.getString("finished_by_name").length() > 0) {
                                                    created_by_rel.setText(related_data.getString("finished_by_name"));
                                                } else {
                                                    created_by_rel.setText(related_data.getString("created_by_name"));
                                                }
                                                label_origin_destination_rel.setText(getResources().getString(R.string.label_stock_destination));
                                                origin_destination_rel.setText(related_data.getString("warehouse_to_name"));
                                                /*if (related_data.has("verified_by_name")) {
                                                    verified_by_rel.setText(related_data.getString("verified_by_name"));
                                                }*/
                                                if (related_data.has("checked_by_name") && !related_data.getString("checked_by_name").equals("null")) {
                                                    verified_by_rel.setText(related_data.getString("checked_by_name"));
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
            }
        }) {
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void initAction() {
        created_at.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogDatePickerLight(v);
            }
        });
    }

    public void updatePurchaseData(int i, String attr, String val) {
        try {
            if (attr == "quantity") {
                purchase_data.get(i).setQuantity(Integer.parseInt(val));
                this.is_update_qty = true;
            } else if (attr == "price") {
                purchase_data.get(i).setUnitPriceAtSale(Double.parseDouble(val));
            }
            doUpdateData();
        } catch (Exception e){e.printStackTrace();}
    }

    public void updatePurchase(View v) {
        Map<String, Object> mObj = new HashMap<String, Object>();

        String _url = Server.URL + "transfer/in-out-update?api-key=" + Server.API_KEY;
        // build the items
        ArrayList arrItems = new ArrayList();
        for (int i = 0; i < purchase_data.size(); i++){
            Map<String, String> mItem = new HashMap<String, String>();
            try {
                // use map
                mItem.put("id", purchase_data.get(i).getProduct().getId()+"");
                mItem.put("barcode", purchase_data.get(i).getProduct().getBarcode());
                mItem.put("name", purchase_data.get(i).getProduct().getName());
                mItem.put("title", purchase_data.get(i).getProduct().getName());
                mItem.put("quantity", purchase_data.get(i).getQuantity()+"");
                mItem.put("unit_price", purchase_data.get(i).getPriceAtSale()+"");
                arrItems.add(mItem);
            } catch (Exception e) {}
        }
        mObj.put("items", arrItems);

        String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
        _url += "&"+ qry;

        Map<String, String> params = new HashMap<String, String>();
        String admin_id = sharedpreferences.getString(TAG_ID, null);
        Params adminParam = paramCatalog.getParamByName("admin_id");
        if (adminParam != null) {
            admin_id = adminParam.getValue();
        }

        params.put("admin_id", admin_id);
        if (purchase_date != null && purchase_date.length() > 0) {
            params.put("effective_date", purchase_date);
        }
        params.put("id", issue_id);
        if (is_update_qty) {
            params.put("is_update_qty", "1");
        }
        if (purchase_notes.getText().toString().length() > 0) {
            params.put("notes", purchase_notes.getText().toString());
        }

        _string_request(
                Request.Method.POST,
                _url,
                params,
                true,
                new VolleyCallback(){
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            success = jObj.getInt(TAG_SUCCESS);
                            // Check for error node in json
                            if (success == 1) {
                                String message = jObj.getString(TAG_MESSAGE);
                                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                do_update_data = false;
                                btn_update.setVisibility(View.GONE);
                                if (server_data != null && server_data.has("status")) {
                                    if (server_data.getInt("status") == 0 && server_data.getInt("group_master") == 0) {
                                        btn_confirm.setVisibility(View.VISIBLE);
                                    }
                                }
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        hideDialog();
                    }
                });
    }

    private void dialogDatePickerLight(final View v) {
        final Calendar cur_calender = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(PurchaseDetailActivity.this,
                new DatePickerDialog.OnDateSetListener() {

                    @Override
                    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                        final Calendar newDate = Calendar.getInstance();
                        newDate.set(year, monthOfYear, dayOfMonth);
                        long date = newDate.getTimeInMillis();
                        ((EditText) v).setText(Tools.getFormattedDateStandard(date));
                        purchase_date = Tools.getFormattedDateStandard(date);
                        doUpdateData();
                    }

                }, cur_calender.get(Calendar.YEAR), cur_calender.get(Calendar.MONTH), cur_calender.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.show();
    }

    public void confirmIssue(View view) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                PurchaseDetailActivity.this);
        quitDialog.setTitle(getResources().getString(R.string.dialog_confirm_data));

        quitDialog.setPositiveButton(getResources().getString(R.string.button_confirm), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String _url = Server.URL + "transfer/in-out-update?api-key=" + Server.API_KEY;

                Map<String, String> params = new HashMap<String, String>();
                String admin_id = sharedpreferences.getString(TAG_ID, null);
                Params adminParam = paramCatalog.getParamByName("admin_id");
                if (adminParam != null) {
                    admin_id = adminParam.getValue();
                }

                params.put("admin_id", admin_id);
                params.put("id", issue_id);
                params.put("status", "1");
                if (purchase_notes.getText().toString().length() > 0) {
                    params.put("notes", purchase_notes.getText().toString());
                }
                if (is_manager) {
                    params.put("force_confirm", "1");
                }

                _string_request(
                        Request.Method.POST,
                        _url,
                        params,
                        true,
                        new VolleyCallback(){
                            @Override
                            public void onSuccess(String result) {
                                try {
                                    JSONObject jObj = new JSONObject(result);
                                    success = jObj.getInt(TAG_SUCCESS);
                                    // Check for error node in json
                                    if (success == 1) {
                                        String message = jObj.getString(TAG_MESSAGE);
                                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                        Intent newActivity = new Intent(PurchaseDetailActivity.this,
                                                PurchaseHistoryActivity.class);
                                        finish();
                                        startActivity(newActivity);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                                hideDialog();
                            }
                        });
            }
        });

        quitDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    public void updateIssue(View view) {
        try {
            updatePurchase(view);
        } catch (Exception e){e.printStackTrace();}
    }

    public void cancelIssue(View view) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                PurchaseDetailActivity.this);
        quitDialog.setTitle(getResources().getString(R.string.dialog_reject_data));

        quitDialog.setPositiveButton(getResources().getString(R.string.button_cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String _url = Server.URL + "transfer/in-out-update?api-key=" + Server.API_KEY;

                    Map<String, String> params = new HashMap<String, String>();
                    String admin_id = sharedpreferences.getString(TAG_ID, null);
                    Params adminParam = paramCatalog.getParamByName("admin_id");
                    if (adminParam != null) {
                        admin_id = adminParam.getValue();
                    }

                    params.put("admin_id", admin_id);
                    params.put("id", issue_id);
                    params.put("status", "-2");
                    if (purchase_notes.getText().toString().length() > 0) {
                        params.put("notes", purchase_notes.getText().toString());
                    }

                    _string_request(
                            Request.Method.POST,
                            _url,
                            params,
                            true,
                            new VolleyCallback(){
                                @Override
                                public void onSuccess(String result) {
                                    try {
                                        JSONObject jObj = new JSONObject(result);
                                        success = jObj.getInt(TAG_SUCCESS);
                                        // Check for error node in json
                                        if (success == 1) {
                                            String message = jObj.getString(TAG_MESSAGE);
                                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                                            Intent newActivity = new Intent(PurchaseDetailActivity.this,
                                                    PurchaseHistoryActivity.class);
                                            finish();
                                            startActivity(newActivity);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    hideDialog();
                                }
                            });
                } catch (Exception e){e.printStackTrace();}
            }
        });

        quitDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    public void removeIssue(View view) {
        AlertDialog.Builder quitDialog = new AlertDialog.Builder(
                PurchaseDetailActivity.this);
        quitDialog.setTitle(getResources().getString(R.string.dialog_remove_data));
        quitDialog.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    String _url = Server.URL + "transfer/history-delete/"+ issue_id +"?api-key=" + Server.API_KEY;

                    Map<String, String> params = new HashMap<String, String>();
                    String admin_id = sharedpreferences.getString(TAG_ID, null);
                    Params adminParam = paramCatalog.getParamByName("admin_id");
                    if (adminParam != null) {
                        admin_id = adminParam.getValue();
                    }

                    params.put("admin_id", admin_id);
                    params.put("id", issue_id);

                    _string_request(
                            Request.Method.POST,
                            _url,
                            params,
                            true,
                            new VolleyCallback(){
                                @Override
                                public void onSuccess(String result) {
                                    try {
                                        JSONObject jObj = new JSONObject(result);
                                        success = jObj.getInt(TAG_SUCCESS);
                                        // Check for error node in json
                                        if (success == 1) {
                                            String message = jObj.getString(TAG_MESSAGE);
                                            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();

                                            Intent newActivity = new Intent(PurchaseDetailActivity.this,
                                                    PurchaseHistoryActivity.class);
                                            finish();
                                            startActivity(newActivity);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    hideDialog();
                                }
                            });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        quitDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        quitDialog.show();
    }

    private void doUpdateData() {
        this.do_update_data = true;
        btn_confirm.setVisibility(View.GONE);
        btn_update.setVisibility(View.VISIBLE);
    }
}