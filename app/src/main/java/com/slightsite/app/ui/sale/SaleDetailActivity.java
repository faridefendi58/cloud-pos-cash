package com.slightsite.app.ui.sale;

import java.io.File;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
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
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.payment.PaymentCatalog;
import com.slightsite.app.domain.payment.PaymentService;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.domain.shipping.ShippingCatalog;
import com.slightsite.app.domain.shipping.ShippingService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.inventory.ProductServerActivity;
import com.slightsite.app.ui.printer.PrintPreviewActivity;
import com.slightsite.app.ui.printer.PrinterActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.slightsite.app.ui.LoginActivity.TAG_ID;

/**
 * UI for showing the detail of Sale in the record.
 *
 */
public class SaleDetailActivity extends Activity{
	
	private TextView totalBox;
	private TextView dateBox;
	private RecyclerView lineitemListRecycle;
	private ListView paymentitemListView;
	private List<Map<String, String>> lineitemList;
	private Sale sale;
	private int saleId;
	private SaleLedger saleLedger;
	private TextView customerBox;
	private Customer customer;
	private TextView status;
	private TextView invoice_number;
	private TextView customer_address;
	private TextView customer_phone;
	private TextView payment_subtotal;
	private TextView payment_discount;
	private TextView payment_grand_total;
	private TextView payment_total_received;
	private LinearLayout payment_debt_container;
	private TextView payment_debt;
	private View spacer_debt;

	/** shipping detail */
	private TextView shipping_method;
	private TextView shipping_date;
	private TextView shipping_warehouse;
	private TextView label_shipping_warehouse;
	private LinearLayout recipient_name_container;
	private TextView shipping_recipient_name;
	private TextView shipping_recipient_phone;
	private TextView created_by;
	private LinearLayout complete_button_container;

	private PaymentCatalog paymentCatalog;
	private List<Payment> paymentList;
	private ParamCatalog paramCatalog;
	private ShippingCatalog shippingCatalog;
	private Shipping shipping;
	private String[] ship_methods;
	private WarehouseCatalog warehouseCatalog;

	ProgressDialog pDialog;
	int success;

	private final HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
	private JSONArray warehouse_data;
	private JSONObject server_invoice_data;

	private static final String TAG = SaleDetailActivity.class.getSimpleName();
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	private SharedPreferences sharedpreferences;
	private Register register;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			saleLedger = SaleLedger.getInstance();
			paramCatalog = ParamService.getInstance().getParamCatalog();
			warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
			register = Register.getInstance();

			getWarehouseList();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		saleId = Integer.parseInt(getIntent().getStringExtra("id"));
		sale = saleLedger.getSaleById(saleId);
		customer = saleLedger.getCustomerBySaleId(saleId);

		sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

		String dt = DateTimeStrategy.getCurrentTime();
		
		initUI(savedInstanceState);
	}


	/**
	 * Initiate actionbar.
	 */
	@SuppressLint("NewApi")
	private void initiateActionBar() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			actionBar.setTitle(getResources().getString(R.string.invoice_detail));
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
			actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (sale.getStatus() != "PUSHED") {
			MenuInflater inflater = getMenuInflater();
			inflater.inflate(R.menu.option_menu, menu);
		}
		return true;
	}
	

	/**
	 * Initiate this UI.
	 * @param savedInstanceState
	 */
	private void initUI(Bundle savedInstanceState) {
		setContentView(R.layout.layout_saledetail);
		
		initiateActionBar();
		
		totalBox = (TextView) findViewById(R.id.totalBox);
		dateBox = (TextView) findViewById(R.id.dateBox);
		paymentitemListView = (ListView) findViewById(R.id.paymentitemList);
		customerBox = (TextView) findViewById(R.id.customerBox);
		status = (TextView) findViewById(R.id.status);
        invoice_number = (TextView) findViewById(R.id.invoice_number);
		customer_address = (TextView) findViewById(R.id.customer_address);
		customer_phone = (TextView) findViewById(R.id.customer_phone);
		payment_subtotal = (TextView) findViewById(R.id.payment_subtotal);
		payment_discount = (TextView) findViewById(R.id.payment_discount);
		payment_grand_total = (TextView) findViewById(R.id.payment_grand_total);
		payment_total_received = (TextView) findViewById(R.id.payment_total_received);
		payment_debt_container = (LinearLayout) findViewById(R.id.payment_debt_container);
		payment_debt = (TextView) findViewById(R.id.payment_debt);
		spacer_debt = (View) findViewById(R.id.spacer_debt);
		created_by = (TextView) findViewById(R.id.created_by);
		complete_button_container = (LinearLayout) findViewById(R.id.complete_button_container);

		lineitemListRecycle = (RecyclerView) findViewById(R.id.lineitemListRecycle);
		lineitemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		lineitemListRecycle.setHasFixedSize(true);
		lineitemListRecycle.setNestedScrollingEnabled(false);

		// for shipping detail
		shipping_method = (TextView) findViewById(R.id.shipping_method);
		shipping_date = (TextView) findViewById(R.id.shipping_date);
		shipping_warehouse = (TextView) findViewById(R.id.shipping_warehouse);
		label_shipping_warehouse = (TextView) findViewById(R.id.label_shipping_warehouse);
		shipping_recipient_name = (TextView) findViewById(R.id.shipping_recipient_name);
		shipping_recipient_phone = (TextView) findViewById(R.id.shipping_recipient_phone);
		recipient_name_container = (LinearLayout) findViewById(R.id.recipient_name_container);

		try {
			paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
			paymentList = paymentCatalog.getPaymentBySaleId(saleId);
			shippingCatalog = ShippingService.getInstance().getShippingCatalog();
			shipping = shippingCatalog.getShippingBySaleId(saleId);
			ship_methods = AppController.getPaymentMethods();
			getDetailFromServer();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		customerBox.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_name), customerBox.getText().toString());
				return false;
			}
		});

		customer_address.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_address), customer_address.getText().toString());
				return false;
			}
		});

		customer_phone.setOnLongClickListener(new View.OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_phone), customer_phone.getText().toString());
				return false;
			}
		});
	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<LineItem> list) {
		lineitemList = new ArrayList<Map<String, String>>();
		for(LineItem line : list) {
			lineitemList.add(line.toMap());
		}

		AdapterListOrder sAdap = new AdapterListOrder(SaleDetailActivity.this, list, register, totalBox);
		lineitemListRecycle.setAdapter(sAdap);

		// building payment information
		List<Map<String, String>> pyitemList = new ArrayList<Map<String, String>>();
		if (paymentList != null) {
			for (Payment payment : paymentList) {
				pyitemList.add(payment.toMap());
			}
		}

		SimpleAdapter pAdap = new SimpleAdapter(SaleDetailActivity.this, pyitemList,
				R.layout.listview_payment, new String[]{"formated_payment_channel","formated_amount"}, new int[] {R.id.title, R.id.price});
		paymentitemListView.setAdapter(pAdap);

		// building shipping information
		if (!shipping.equals(null)) {
			if (shipping.toMap().get("configs") != null) {
				try {
					JSONObject jsonObject = new JSONObject(shipping.toMap().get("configs"));
					if (jsonObject.has("recipient_name")) {
						shipping.setName(jsonObject.getString("recipient_name"));
						shipping.setPhone(jsonObject.getString("recipient_phone"));
					}
				} catch (Exception e){e.printStackTrace();}
			}
			Log.e(getClass().getSimpleName(), "shipping : "+ shipping.toMap());
			shipping_method.setText(ship_methods[shipping.getMethod()]);
			if (shipping.getDate().equals(null) || shipping.getDate().length() == 0) {
				DateFormat df = new SimpleDateFormat("dd MMM yyyy HH:mm");
				if (shipping.getMethod() == 0) {
					df = new SimpleDateFormat("dd MMM yyyy");
				}
				String date = df.format(Calendar.getInstance().getTime());
				shipping.setDate(date);
			}
			shipping_date.setText(shipping.getDate());
			shipping_warehouse.setText(shipping.getWarehouseName());
			if (shipping.getAddress() != null && shipping.getMethod() > 1) {
				shipping_warehouse.setText(shipping.getAddress());
				label_shipping_warehouse.setText(getResources().getString(R.string.label_shipping_address));

				shipping_date.setText(shipping.getPickupDate());
				if (shipping.getAddress().length() >= 25) {
					label_shipping_warehouse.setText(getResources().getString(R.string.address));
				}
			}

			if (shipping.getName() != null && shipping.getMethod() > 1) {
				recipient_name_container.setVisibility(View.VISIBLE);
				shipping_recipient_name.setText(shipping.getName());
				shipping_recipient_phone.setText(shipping.getPhone());
			}
		}

		payment_subtotal.setText(CurrencyController.getInstance().moneyFormat(sale.getTotal()) + "");
		payment_discount.setText(CurrencyController.getInstance().moneyFormat(sale.getDiscount()) + "");
		try {
			Double tot_order = sale.getTotal() - sale.getDiscount();
			payment_grand_total.setText(CurrencyController.getInstance().moneyFormat(tot_order) + "");

			List<Payment> the_payments = paymentCatalog.getPaymentBySaleId(sale.getId());
			Double getTotalPaymentBySaleId = 0.0;
			if (the_payments != null) {
				getTotalPaymentBySaleId = paymentCatalog.getTotalPaymentBySaleId(saleId);
			}
			payment_total_received.setText(CurrencyController.getInstance().moneyFormat(getTotalPaymentBySaleId) + "");

			if (getTotalPaymentBySaleId < tot_order) {
				Double debt = tot_order - getTotalPaymentBySaleId;
				payment_debt.setText(CurrencyController.getInstance().moneyFormat(debt) + "");
				payment_debt_container.setVisibility(View.VISIBLE);
				spacer_debt.setVisibility(View.VISIBLE);
			}

		} catch (Exception e) {e.printStackTrace();}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case android.R.id.home:
				finish();
				return true;
			case R.id.action_edit:
				new AlertDialog.Builder(SaleDetailActivity.this)
						.setTitle(getResources().getString(R.string.title_update_sale))
						.setMessage(getResources().getString(R.string.confirm_edit))
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog, int whichButton) {
								Intent updateAct = new Intent(SaleDetailActivity.this, MainActivity.class);
								updateAct.putExtra("saleId", sale.getId());
								startActivity(updateAct);
							}})
						.setNegativeButton(android.R.string.no, null).show();

				return true;
			case R.id.action_remove:
				removeInvoice(getCurrentFocus());
				return true;
			case R.id.action_print:
				printInvoice(getCurrentFocus());
				return true;
			case R.id.action_push:
				pushInvoice(getCurrentFocus());
				return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Update UI.
	 */
	public void update() {
		totalBox.setText(CurrencyController.getInstance().moneyFormat(sale.getTotal()) + "");
		dateBox.setText(DateTimeStrategy.parseDate(sale.getEndTime(), "dd/MM/yy HH:s") + "");
		customerBox.setText(customer.getName());
		status.setText(sale.getStatus());
		Map<String, String> salemap = sale.toMap();
		invoice_number.setText(salemap.get("server_invoice_number"));

		if (warehouse_data != null) {
			new android.os.Handler().postDelayed(
				new Runnable() {
					public void run() {
						showList(sale.getAllLineItem());
					}
				},
				2000);
		} else {
			showList(sale.getAllLineItem());
		}

		customer_address.setText(customer.getAddress());
		customer_phone.setText(customer.getPhone());
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		update();
	}

	public void printInvoice(View v) {
		Intent newActivity = new Intent(SaleDetailActivity.this,
				PrintPreviewActivity.class);
		newActivity.putExtra("saleId", saleId);
		startActivity(newActivity);
	}

	public void removeInvoice(View v) {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(
				SaleDetailActivity.this);
		quitDialog.setTitle(getResources().getString(R.string.dialog_remove_invoice));
		quitDialog.setPositiveButton(getResources().getString(R.string.remove), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					saleLedger.removeSale(sale);
				} catch (Exception e) {
					e.printStackTrace();
				}

				Intent newActivity = new Intent(SaleDetailActivity.this,
						MainActivity.class);
				startActivity(newActivity);
			}
		});

		quitDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		quitDialog.show();
	}

    /**
     * Params format for server (converted to json) :
	 * {"api-key":"ac43724f16e9241d990427ab7c8f4228",
	 * "payment":[{"type":"cash","change_due":"0","amount_tendered":"380000.0"}],
	 * "items_belanja":[{"barcode":"12","name":"Daging durian","unit_price":"100000.0","qty":"2","id":"2","base_price":"100000.0"},{"barcode":"1","name":"Durian Kupas","unit_price":"90000.0","qty":"1","id":"4","base_price":"90000.0"},{"barcode":"3","name":"Pancake Durian","unit_price":"90000.0","qty":"1","id":"3","base_price":"90000.0"}],
	 * "customer":{"name":"Farid Efendi","email":"-"},
	 * "admin_id":"1"}
     * @param v
     */
	public void pushInvoice(View v) {
		Map<String, Object> mObj = new HashMap<String, Object>();
		ArrayList arrItems = new ArrayList();
		for (int i = 0; i < lineitemList.size(); i++){
			Map<String, String> mItem = new HashMap<String, String>();
			try {
				// use map
				mItem.put("name", lineitemList.get(i).get("name"));
				mItem.put("qty", lineitemList.get(i).get("quantity"));
				mItem.put("unit_price", lineitemList.get(i).get("unit_price"));
				mItem.put("base_price", lineitemList.get(i).get("base_price"));
				mItem.put("id", lineitemList.get(i).get("id"));
				mItem.put("barcode", lineitemList.get(i).get("barcode"));
				arrItems.add(mItem);
			} catch (Exception e) {}
		}

        Map<String, String> arrCust = new HashMap<String, String>();

		ArrayList arrPaymentList = new ArrayList();
		Map<String, String> arrPayment = new HashMap<String, String>();
		try {
			Customer cust = saleLedger.getCustomerBySaleId(saleId);
			mObj.put("items_belanja", arrItems);

            arrCust.put("email", cust.getEmail());
            arrCust.put("name", cust.getName());
            arrCust.put("phone", cust.getPhone());
            mObj.put("customer", arrCust);

            if (paymentList.size() > 0) {
            	for (Payment py : paymentList) {
					Map<String, String> arrPayment2 = new HashMap<String, String>();
					arrPayment2.put("type", py.getPaymentChannel());
					arrPayment2.put("amount_tendered", ""+ py.getAmount());
					arrPayment2.put("change_due", "0");
					arrPaymentList.add(arrPayment2);
				}
			} else {
				arrPayment.put("type", "cash");
				arrPayment.put("amount_tendered", ""+ sale.getTotal());
				arrPayment.put("change_due", "0");
				arrPaymentList.add(arrPayment);
			}
			mObj.put("payment", arrPaymentList);
            // set warehouse_id if any
			Params whParam = paramCatalog.getParamByName("warehouse_id");
			if (whParam != null) {
				mObj.put("warehouse_id", whParam.getValue());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		Boolean pushed = false;
		try {
			int server_invoice_id = saleLedger.getServerInvoiceId(saleId);
			if (server_invoice_id <= 0) {
				_execute(mObj);
			} else {
                Toast.makeText(getApplicationContext(),
                        "Data telah tercatat di server dengan id "+ server_invoice_id, Toast.LENGTH_LONG).show();
            }
		} catch (Exception e) {

		}
	}

	private void _execute(Map mObj) {
		String _url = Server.URL + "transaction/create?api-key=" + Server.API_KEY;
		String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
		_url += "&"+ qry;

		Map<String, String> params = new HashMap<String, String>();
		String admin_id = sharedpreferences.getString(TAG_ID, null);
		params.put("admin_id", admin_id);

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
							int server_invoice_id = jObj.getInt(TAG_ID);
							String server_invoice_number = jObj.getString("invoice_number");
							// Check for error node in json
							if (success == 1) {
								saleLedger.setServerInvoiceId(sale, server_invoice_id, server_invoice_number);
							}
							Toast.makeText(getApplicationContext(),
									jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
						} catch (JSONException e) {
							e.printStackTrace();
						}

						hideDialog();
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

	public void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
		if (show_dialog) {
			pDialog = new ProgressDialog(this);
			pDialog.setCancelable(false);
			pDialog.setMessage("Request data ...");
			showDialog();
		}

		if (method == Request.Method.GET) {
			String qry = URLBuilder.httpBuildQuery(params, "UTF-8");
			url += "&" + qry;
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

	private void getWarehouseList() {
		List<Warehouses> whs = warehouseCatalog.getAllWarehouses();
		if (whs != null) {
			for (Warehouses wh : whs) {
				warehouse_names.put(wh.getWarehouseId(), wh.getTitle());
			}
		} else {
			Map<String, String> params = new HashMap<String, String>();

			String url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
			_string_request2(
				Request.Method.GET,
				url, params, false,
				new VolleyCallback() {
					@Override
					public void onSuccess(String result) {
						try {
							JSONObject jObj = new JSONObject(result);
							success = jObj.getInt(TAG_SUCCESS);
							// Check for error node in json
							if (success == 1) {
								warehouse_data = jObj.getJSONArray("data");
								for(int n = 0; n < warehouse_data.length(); n++)
								{
									JSONObject data_n = warehouse_data.getJSONObject(n);
									warehouse_names.put(data_n.getInt("id"), data_n.getString("title"));
								}
							}
							Log.e(getClass().getSimpleName(), "warehouse_names2 : "+ warehouse_names.toString());
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
		}
	}

	private void _string_request2(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
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

		strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
				DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

		try {
			AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void getDetailFromServer() {
		Map<String, String> params = new HashMap<String, String>();

		String admin_id = sharedpreferences.getString(TAG_ID, null);
		params.put("admin_id", admin_id);
		params.put("invoice_id", sale.getServerInvoiceId()+"");

		String url = Server.URL + "transaction/detail?api-key=" + Server.API_KEY;
		_string_request2(
				Request.Method.GET,
				url, params, false,
				new VolleyCallback() {
					@Override
					public void onSuccess(String result) {
						try {
							JSONObject jObj = new JSONObject(result);
							success = jObj.getInt(TAG_SUCCESS);
							Log.e(getClass().getSimpleName(), "jObj : "+ jObj.toString());
							// Check for error node in json
							if (success == 1) {
								server_invoice_data = jObj.getJSONObject("data");
								created_by.setText(server_invoice_data.getString("created_by_name"));
								if (server_invoice_data.has("shipping")) {
									JSONArray arr_shipping = server_invoice_data.getJSONArray("shipping");
									JSONObject obj_shipping = arr_shipping.getJSONObject(0);
									shipping.setWarehouseId(obj_shipping.getInt("warehouse_id"));
									shipping.setWarehouseName(obj_shipping.getString("warehouse_name"));

									shipping_warehouse.setText(shipping.getWarehouseName());
									if (shipping.getAddress() != null && shipping.getMethod() > 1) {
										shipping_warehouse.setText(shipping.getAddress());
										label_shipping_warehouse.setText(getResources().getString(R.string.label_shipping_address));

										shipping_date.setText(shipping.getPickupDate());
										if (shipping.getAddress().length() >= 25) {
											label_shipping_warehouse.setText(getResources().getString(R.string.address));
										}
									}

									if (shipping.getName() != null && shipping.getMethod() > 1) {
										recipient_name_container.setVisibility(View.VISIBLE);
										shipping_recipient_name.setText(shipping.getName());
										shipping_recipient_phone.setText(shipping.getPhone());
									}

									if (server_invoice_data.getInt("status") == 0) {
										complete_button_container.setVisibility(View.VISIBLE);
									}
								}
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void copyToClipBoard(String label, String text) {
		ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
		ClipData clip = ClipData.newPlainText(label, text);
		clipboard.setPrimaryClip(clip);

		Toast.makeText(getApplicationContext(),
				"Copying "+ label + " to clipboard.", Toast.LENGTH_LONG).show();
	}

	public void markAsComplete(View v) {
		AlertDialog.Builder quitDialog = new AlertDialog.Builder(
				SaleDetailActivity.this);
		quitDialog.setTitle(getResources().getString(R.string.dialog_mark_as_complete));
		quitDialog.setPositiveButton(getResources().getString(R.string.action_submit), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		quitDialog.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});
		quitDialog.show();
	}
}
