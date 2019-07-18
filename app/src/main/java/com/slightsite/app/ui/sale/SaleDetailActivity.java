package com.slightsite.app.ui.sale;

import java.util.ArrayList;
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
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
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
	private ListView lineitemListView;
	private ListView paymentitemListView;
	private ListView shippingitemListView;
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

	private PaymentCatalog paymentCatalog;
	private List<Payment> paymentList;
	private ParamCatalog paramCatalog;
	private ShippingCatalog shippingCatalog;
	private Shipping shipping;
	private WarehouseCatalog warehouseCatalog;

	ProgressDialog pDialog;
	int success;

	private final HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
	private JSONArray warehouse_data;

	private static final String TAG = SaleDetailActivity.class.getSimpleName();
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	private SharedPreferences sharedpreferences;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			saleLedger = SaleLedger.getInstance();
			paramCatalog = ParamService.getInstance().getParamCatalog();
			warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
			getWarehouseList();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		saleId = Integer.parseInt(getIntent().getStringExtra("id"));
		sale = saleLedger.getSaleById(saleId);
		customer = saleLedger.getCustomerBySaleId(saleId);
		Log.e(getClass().getSimpleName(), "customer : "+ customer.toMap().toString());
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
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
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
		lineitemListView = (ListView) findViewById(R.id.lineitemList);
		paymentitemListView = (ListView) findViewById(R.id.paymentitemList);
		shippingitemListView = (ListView) findViewById(R.id.shippingitemList);
		customerBox = (TextView) findViewById(R.id.customerBox);
		status = (TextView) findViewById(R.id.status);
        invoice_number = (TextView) findViewById(R.id.invoice_number);
		customer_address = (TextView) findViewById(R.id.customer_address);
		customer_phone = (TextView) findViewById(R.id.customer_phone);

		try {
			paymentCatalog = PaymentService.getInstance().getPaymentCatalog();
			paymentList = paymentCatalog.getPaymentBySaleId(saleId);
			shippingCatalog = ShippingService.getInstance().getShippingCatalog();
			shipping = shippingCatalog.getShippingBySaleId(saleId);

			//List<Shipping> list_shipping = shippingCatalog.getAllShipping();
			//Log.e(getClass().getSimpleName(), "list_shipping : "+ list_shipping.toString());

		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
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

		SimpleAdapter sAdap = new SimpleAdapter(SaleDetailActivity.this, lineitemList,
				R.layout.listview_lineitem, new String[]{"name","quantity","price"}, new int[] {R.id.name,R.id.quantity,R.id.price});
		lineitemListView.setAdapter(sAdap);

		// building payment information
		List<Map<String, String>> pyitemList = new ArrayList<Map<String, String>>();
		for (Payment payment : paymentList) {
			pyitemList.add(payment.toMap());
		}

		SimpleAdapter pAdap = new SimpleAdapter(SaleDetailActivity.this, pyitemList,
				R.layout.listview_payment, new String[]{"formated_payment_channel","formated_amount"}, new int[] {R.id.title, R.id.price});
		paymentitemListView.setAdapter(pAdap);

		// building shipping information
		List<Map<String, String>> shippingitemList = new ArrayList<Map<String, String>>();
		Map<String, String> ship_map = new HashMap<String, String>();
		String note = shipping.toMap().get("method_name");

		if (shipping.getMethod() == 0) {
			note += " pada tanggal "+ shipping.getPickupDate();
		} else {
			note += " pada tanggal "+ shipping.getPickupDate() + " di Warehouse "+ warehouse_names.get(shipping.getWarehouseId());
		}
		ship_map.put("note", note);
		shippingitemList.add(ship_map);

		SimpleAdapter spAdap = new SimpleAdapter(SaleDetailActivity.this, shippingitemList,
				R.layout.listview_payment, new String[]{"note"}, new int[] {R.id.title});
		shippingitemListView.setAdapter(spAdap);
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
		invoice_number.setText(salemap.get("invoiceNumber"));

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
				PrinterActivity.class);
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
}
