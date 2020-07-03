package com.slightsite.app.ui.sale;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
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
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.payment.PaymentCatalog;
import com.slightsite.app.domain.payment.PaymentService;
import com.slightsite.app.domain.retur.Retur;
import com.slightsite.app.domain.sale.PaymentItem;
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
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.techicalservices.URLBuilder;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.deposit.DepositActivity;
import com.slightsite.app.ui.inventory.ProductServerActivity;
import com.slightsite.app.ui.printer.PrintPreviewActivity;
import com.slightsite.app.ui.printer.PrinterActivity;
import com.slightsite.app.ui.retur.AdapterListConfirmRetur;
import com.slightsite.app.ui.retur.AdapterListReturReport;
import com.slightsite.app.ui.retur.PrintReturActivity;
import com.slightsite.app.ui.retur.ReturActivity;

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
	private RecyclerView paymentitemListView;
	private RecyclerView receiptitemListView;
	private List<Map<String, String>> lineitemList;
	private Sale sale;
	private int saleId;
	private SaleLedger saleLedger;
	private TextView customerBox;
	private Customer customer;
	private TextView status;
	private TextView invoice_number;
	private TextView customer_address;
	private LinearLayout customer_address_container;
	private TextView customer_phone;
	private LinearLayout customer_phone_container;
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
	private TextView finished_by;
	private TextView dateFinished;
	private LinearLayout complete_button_container;
	private LinearLayout finish_button_container;
	private LinearLayout retur_button_container;
	private LinearLayout retur_information;
	private LinearLayout finished_date_container;
	private LinearLayout main_discount_container;
	private LinearLayout gograbfood_discount_container;
	private LinearLayout main_total_price_container;
	private LinearLayout receipt_information;
	private LinearLayout shipping_information_container;
	private LinearLayout take_good_button_container;
	private TextView gograbfood_discount_label;
	private TextView gograbfood_total_price;
	private TextView gograbfood_discount;

	private PaymentCatalog paymentCatalog;
	private List<Payment> paymentList;
	private List<LineItem> lineItems;
	private ParamCatalog paramCatalog;
	private ShippingCatalog shippingCatalog;
	private Shipping shipping;
	private String[] ship_methods;
	private WarehouseCatalog warehouseCatalog;
	private BottomSheetDialog bottomSheetDialog;
	private Double tot_debt = 0.0;
	private ProductCatalog productCatalog;

	ProgressDialog pDialog;
	int success;

	private final HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
	private JSONArray warehouse_data;
	private JSONObject server_invoice_data;
	private JSONObject obj_retur = new JSONObject();

	private static final String TAG = SaleDetailActivity.class.getSimpleName();
	private static final String TAG_SUCCESS = "success";
	private static final String TAG_MESSAGE = "message";

	private SharedPreferences sharedpreferences;
	private Register register;
	private Boolean is_local_data = false;

	private Sale sale_intent;
	private Customer customer_intent;
	private Shipping shipping_intent;
	private String payment_intent;
	private String line_items_intent;

	private HashMap<String, Bitmap> payment_receipts = new HashMap<String, Bitmap>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			saleLedger = SaleLedger.getInstance();
			paramCatalog = ParamService.getInstance().getParamCatalog();
			warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
			register = Register.getInstance();
			productCatalog = Inventory.getInstance().getProductCatalog();

			getWarehouseList();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		if (getIntent().hasExtra("sale_intent")) { // has sale data from server
			sale = (Sale) getIntent().getSerializableExtra("sale_intent");
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
		} else { // sale data from database
			saleId = Integer.parseInt(getIntent().getStringExtra("id"));
			sale = saleLedger.getSaleById(saleId);
			customer = saleLedger.getCustomerBySaleId(saleId);
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
		paymentitemListView = (RecyclerView) findViewById(R.id.paymentitemList);
		paymentitemListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		paymentitemListView.setHasFixedSize(true);
		paymentitemListView.setNestedScrollingEnabled(false);

		receiptitemListView = (RecyclerView) findViewById(R.id.receiptitemList);
		receiptitemListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		receiptitemListView.setHasFixedSize(true);
		receiptitemListView.setNestedScrollingEnabled(false);

		customerBox = (TextView) findViewById(R.id.customerBox);
		status = (TextView) findViewById(R.id.status);
        invoice_number = (TextView) findViewById(R.id.invoice_number);
		customer_address = (TextView) findViewById(R.id.customer_address);
		customer_address_container = (LinearLayout) findViewById(R.id.customer_address_container);
		customer_phone = (TextView) findViewById(R.id.customer_phone);
		customer_phone_container = (LinearLayout) findViewById(R.id.customer_phone_container);
		payment_subtotal = (TextView) findViewById(R.id.payment_subtotal);
		payment_discount = (TextView) findViewById(R.id.payment_discount);
		payment_grand_total = (TextView) findViewById(R.id.payment_grand_total);
		payment_total_received = (TextView) findViewById(R.id.payment_total_received);
		payment_debt_container = (LinearLayout) findViewById(R.id.payment_debt_container);
		payment_debt = (TextView) findViewById(R.id.payment_debt);
		spacer_debt = (View) findViewById(R.id.spacer_debt);
		created_by = (TextView) findViewById(R.id.created_by);
		finished_by = (TextView) findViewById(R.id.finished_by);
		dateFinished = (TextView) findViewById(R.id.dateFinished);
		complete_button_container = (LinearLayout) findViewById(R.id.complete_button_container);
		finish_button_container = (LinearLayout) findViewById(R.id.finish_button_container);
		retur_button_container = (LinearLayout) findViewById(R.id.retur_button_container);
		retur_information = (LinearLayout) findViewById(R.id.retur_information);
		finished_date_container = (LinearLayout) findViewById(R.id.finished_date_container);
		main_discount_container = (LinearLayout) findViewById(R.id.main_discount_container);
		gograbfood_discount_container = (LinearLayout) findViewById(R.id.gograbfood_discount_container);
		main_total_price_container = (LinearLayout) findViewById(R.id.main_total_price_container);
		receipt_information = (LinearLayout) findViewById(R.id.receipt_information);
		shipping_information_container = (LinearLayout) findViewById(R.id.shipping_information_container);
        take_good_button_container = (LinearLayout) findViewById(R.id.take_good_button_container);

		gograbfood_discount_label = (TextView) findViewById(R.id.gograbfood_discount_label);
		gograbfood_total_price = (TextView) findViewById(R.id.gograbfood_total_price);
		gograbfood_discount = (TextView) findViewById(R.id.gograbfood_discount);

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
			if (getIntent().hasExtra("payment_intent")) {
				payment_intent = getIntent().getStringExtra("payment_intent");
				JSONArray arrPayment = null;
				try {
					arrPayment = new JSONArray(getIntent().getStringExtra("payment_intent"));
					Log.e(getClass().getSimpleName(), "arrPayment from payment_intent : "+ arrPayment.toString());
				} catch (JSONException e) {
					e.printStackTrace();
				}
				if (arrPayment != null) {
					paymentList = new ArrayList<Payment>();
					for (int m = 0; m < arrPayment.length(); m++) {
						JSONObject pay_method = null;
						try {
							pay_method = arrPayment.getJSONObject(m);
							Payment payment = new Payment(
									-1,
									sale.getId(),
									pay_method.getString("type"),
									pay_method.getDouble("amount_tendered")
							);
							paymentList.add(payment);
						} catch (JSONException e) {
							e.printStackTrace();
						}
					}
				}
			} else {
				paymentList = paymentCatalog.getPaymentBySaleId(saleId);
			}
			shippingCatalog = ShippingService.getInstance().getShippingCatalog();
			if (getIntent().hasExtra("shipping_intent")) {
				shipping = (Shipping) getIntent().getSerializableExtra("shipping_intent");
				shipping_intent = shipping;
			} else {
				shipping = shippingCatalog.getShippingBySaleId(saleId);
			}
			ship_methods = AppController.getPaymentMethods();
			getDetailFromServer();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		customerBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_name), customerBox.getText().toString());
			}
		});

		customer_address.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_address), customer_address.getText().toString());
			}
		});

		customer_phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_customer_phone), customer_phone.getText().toString());
			}
		});

		shipping_recipient_name.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_shipping_name), shipping_recipient_name.getText().toString());
			}
		});

		shipping_recipient_phone.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.label_shipping_phone), shipping_recipient_phone.getText().toString());
			}
		});

		shipping_warehouse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				copyToClipBoard(getResources().getString(R.string.address), shipping_warehouse.getText().toString());
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

		/*SimpleAdapter pAdap = new SimpleAdapter(SaleDetailActivity.this, pyitemList,
				R.layout.listview_payment, new String[]{"formated_payment_channel","formated_amount"}, new int[] {R.id.title, R.id.price});*/
		AdapterListPaymentSimple pAdap = new AdapterListPaymentSimple(paymentList);
		paymentitemListView.setAdapter(pAdap);

		// building shipping information
		if (shipping != null && !shipping.equals(null)) {
			if (shipping.toMap().get("configs") != null) {
				try {
					JSONObject jsonObject = new JSONObject(shipping.toMap().get("configs"));
					if (jsonObject.has("recipient_name")) {
						shipping.setName(jsonObject.getString("recipient_name"));
						shipping.setPhone(jsonObject.getString("recipient_phone"));
					}
				} catch (Exception e){e.printStackTrace();}
			}
			if (shipping.getMethod() == 4 || shipping.getMethod() == 5) {
				if (shipping.getName() == null || shipping.getName().equals("null")) {
					shipping.setName(customer.getName());
				}
				if (shipping.getPhone() == null || shipping.getPhone().equals("null")) {
					shipping.setPhone(customer.getPhone());
				}
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
			if (shipping.getPickupDate() != null) {
				shipping_date.setText(shipping.getPickupDate());
			}
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

			if (shipping.getMethod() == 6) { //6 is nitip barang
				shipping_information_container.setVisibility(View.GONE);
			}
		}

		payment_subtotal.setText(CurrencyController.getInstance().moneyFormat(sale.getTotal()) + "");
		payment_discount.setText(CurrencyController.getInstance().moneyFormat(sale.getDiscount()) + "");

		try {
			Double tot_order = sale.getTotal() - sale.getDiscount();
			payment_grand_total.setText(CurrencyController.getInstance().moneyFormat(tot_order) + "");

			List<Payment> the_payments = null;
			if (is_local_data) {
				the_payments = paymentCatalog.getPaymentBySaleId(sale.getId());
			} else {
				the_payments = paymentList;
			}
			Double getTotalPaymentBySaleId = 0.0;
			if (the_payments != null) {
				if (is_local_data) {
					getTotalPaymentBySaleId = paymentCatalog.getTotalPaymentBySaleId(saleId);
				} else {
					for (Payment payment : the_payments) {
						getTotalPaymentBySaleId = getTotalPaymentBySaleId + payment.getAmount();
					}
				}
			}
			payment_total_received.setText(CurrencyController.getInstance().moneyFormat(getTotalPaymentBySaleId) + "");

			if (getTotalPaymentBySaleId < tot_order) {
				Double debt = tot_order - getTotalPaymentBySaleId;

				tot_debt = debt;
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
				if (sale.getIsVerifiedPayment() <= 0 || sale.getStatus() != "FINISHED") { //make sure this is not complete order
					removeInvoice(getCurrentFocus());
				} else {
					Toast.makeText(getApplicationContext(),
							"Tidak diperbolehkan menghapus data transaksi yang sudah selesai.", Toast.LENGTH_LONG).show();
				}
				return true;
			case R.id.action_print:
				printInvoice(getCurrentFocus());
				return true;
			case R.id.action_push:
				pushInvoice(getCurrentFocus());
				return true;
			case R.id.action_retur:
				Toast.makeText(getBaseContext(),
						"Will be available soon!", Toast.LENGTH_SHORT)
						.show();
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
		dateBox.setText(DateTimeStrategy.parseDate(sale.getEndTime(), "dd MMM yyyy HH:s") + "");
		customerBox.setText(customer.getName());
		status.setText(sale.getStatus());
		Map<String, String> salemap = sale.toMap();
		invoice_number.setText(salemap.get("server_invoice_number"));

		if (warehouse_data != null) {
			new android.os.Handler().postDelayed(
				new Runnable() {
					public void run() {
						if (lineItems != null) {
							showList(lineItems);
						} else {
							showList(sale.getAllLineItem());
						}
					}
				},
				2000);
		} else {
			if (lineItems != null) {
				showList(lineItems);
			} else {
				showList(sale.getAllLineItem());
			}
		}

		customer_address.setText(customer.getAddress());
		customer_phone.setText(customer.getPhone());

		if (customer.getPhone().equals("-")) {
			customer_phone_container.setVisibility(View.GONE);
		}

		if (customer.getAddress().equals("-")) {
			customer_address_container.setVisibility(View.GONE);
		}
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
		newActivity.putExtra("shipping_method", shipping.getMethod());
		if (!is_local_data) {
			Sale new_sale = new Sale(saleId, sale.getEndTime());
			new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
			new_sale.setServerInvoiceId(sale.getServerInvoiceId());
			new_sale.setCustomerId(sale.getCustomerId());
			new_sale.setStatus(sale.getStatus());
			new_sale.setDiscount(sale.getDiscount());
			new_sale.setDeliveredAt(sale.getDeliveredAt());
			new_sale.setDeliveredByName(sale.getDeliveredByName());

			newActivity.putExtra("sale_intent", new_sale);
			newActivity.putExtra("customer_intent", customer_intent);
			newActivity.putExtra("shipping_intent", shipping_intent);
			newActivity.putExtra("payment_intent", payment_intent);
			newActivity.putExtra("line_items_intent", line_items_intent);
		}
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
					Map<String, Object> mObj = new HashMap<String, Object>();
					mObj.put("invoice_id", sale.getServerInvoiceId());
					_server_remove_inv(mObj);
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

	private int tot_inv_qty = 0;
	private JSONObject merchant_data = new JSONObject();

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
                                tot_inv_qty = server_invoice_data.getInt("total_quantity");
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

									// null value on pickup date when ship method 0
									if (shipping.getMethod() == 0) {
										try {
											if (server_invoice_data.getInt("delivered") == 1) {
												shipping_date.setText(DateTimeStrategy.parseDate(server_invoice_data.getString("delivered_at"), "dd MMM yyyy HH:mm") + "");
											}
										} catch (Exception e){}
									}

									// find the retur data
									try {
										obj_retur = server_invoice_data.getJSONObject("refund");
									} catch (JSONException e) {
										Log.e("JSON Parser", "Error parsing data " + e.toString());
									}
									Log.e(getClass().getSimpleName(), "obj_retur : "+ obj_retur.toString());

									if (server_invoice_data.getInt("status") == 0) {
                                        complete_button_container.setVisibility(View.VISIBLE);
									} else if (server_invoice_data.getInt("status") == 1) {
										if (server_invoice_data.getInt("delivered") == 1) {
											try {
												finished_date_container.setVisibility(View.VISIBLE);
												if (server_invoice_data.has("delivered_at") && !server_invoice_data.getString("delivered_at").equals("null")) {
													dateFinished.setText(DateTimeStrategy.parseDate(server_invoice_data.getString("delivered_at"), "dd MMM yyyy HH:mm"));
													sale.setDeliveredAt(server_invoice_data.getString("delivered_at"));
													sale.setDeliveredByName(server_invoice_data.getString("delivered_by_name"));
												}
												finished_by.setText(server_invoice_data.getString("delivered_by_name"));
											} catch (Exception e){}
											if (!obj_retur.has("id")) { // hanya yg belum pnh retur
												retur_button_container.setVisibility(View.VISIBLE);
												finish_button_container.setVisibility(View.GONE);
											} else {
												buildReturInformation();
											}
										} else {
                                            Log.e(TAG, "shipping.getMethod() : "+ shipping.getMethod());
                                            if (shipping.getMethod() == 6) {
                                                take_good_button_container.setVisibility(View.VISIBLE);
                                            } else {
                                                finish_button_container.setVisibility(View.VISIBLE);
                                            }
										}
									}
								}

								if (server_invoice_data.has("merchant")) {
									merchant_data = server_invoice_data.getJSONObject("merchant");
									if (shipping.getMethod() == 4 || shipping.getMethod() == 5) {
										try {
											main_discount_container.setVisibility(View.GONE);
											main_total_price_container.setVisibility(View.GONE);
											gograbfood_discount_container.setVisibility(View.VISIBLE);
											Double selisih = 0.0;
											if (merchant_data.has("total_invoice")) {
												Double tot_pr = merchant_data.getDouble("total_invoice");
												selisih = tot_pr - sale.getTotal();
												if (sale.getDiscount() > 0) {
													tot_pr = tot_pr - sale.getDiscount();
												}

												gograbfood_total_price.setText(CurrencyController.getInstance().moneyFormat(tot_pr));
												gograbfood_discount.setText(CurrencyController.getInstance().moneyFormat(selisih));
											}
											if (shipping.getMethod() == 4) {
												gograbfood_discount_label.setText(getResources().getString(R.string.label_gofood_discount));
												if (selisih > 0) {
													gograbfood_discount_label.setText(getResources().getString(R.string.label_gofood_fee));
												}
											} else if (shipping.getMethod() == 5) {
												gograbfood_discount_label.setText(getResources().getString(R.string.label_grabfood_discount));
												if (selisih > 0) {
													gograbfood_discount_label.setText(getResources().getString(R.string.label_grabfood_fee));
												}
											}
											if (sale.getDiscount() > 0) {
												main_discount_container.setVisibility(View.VISIBLE);
												payment_discount.setText("- "+ CurrencyController.getInstance().moneyFormat(sale.getDiscount()));
											} else {
												main_discount_container.setVisibility(View.GONE);
											}
											// no debt information
											payment_debt_container.setVisibility(View.GONE);
											spacer_debt.setVisibility(View.GONE);
										} catch (Exception e){e.printStackTrace();}
									}
								}

								if (server_invoice_data.has("transfer_receipt")) {
									JSONObject transfer_receipt = server_invoice_data.getJSONObject("transfer_receipt");

									List<JSONObject> rec_list = new ArrayList<JSONObject>();
									ArrayList<String> receipt_urls = new ArrayList<String>();
									Iterator<String> keys = transfer_receipt.keys();
									while(keys.hasNext()) {
										String key = keys.next();
										JSONObject xobj = new JSONObject();
										xobj.put("title", key);
										xobj.put("image_url", Server.BASE_API_URL +""+ transfer_receipt.getString(key));
										rec_list.add(xobj);
										receipt_urls.add(transfer_receipt.getString(key));
									}
									// wait check receipt on payment if any
									if (server_invoice_data.has("payment")) {
										JSONArray pym_list = server_invoice_data.getJSONArray("payment");
										for (int pli = 0; pli < pym_list.length(); pli++) {
											JSONObject pym_dt = pym_list.getJSONObject(pli);
											if (pym_dt != null && pym_dt.has("transfer_receipt")) {
												if (!receipt_urls.contains(pym_dt.getString("transfer_receipt"))) {
													receipt_urls.add(pym_dt.getString("transfer_receipt"));
													JSONObject xobj2 = new JSONObject();
													String _bank = pym_dt.getString("type").replace("nominal_", "");
													xobj2.put("title", _bank);
													xobj2.put("image_url", Server.BASE_API_URL +""+ pym_dt.getString("transfer_receipt"));
													rec_list.add(xobj2);
												}
											}
										}
									}
									if (rec_list.size() > 0) {
										AdapterListReceipt rAdap = new AdapterListReceipt(rec_list);
										receiptitemListView.setAdapter(rAdap);
										receipt_information.setVisibility(View.VISIBLE);
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

	private TextView transfer_bank_header;
	private LinearLayout transfer_bank_container;
	private Button finish_submit_button;
	private EditText cash_receive;
	private EditText nominal_bca;
	private EditText nominal_mandiri;
	private EditText nominal_bri;
	private List<PaymentItem> payment_items;
	private TextView edc_header;
	private LinearLayout edc_container;
	private EditText edc_card_type;
	private EditText edc_card_number;
	private EditText edc_nominal;

	private Button btn_receipt_mandiri;
	private Button btn_receipt_bca;
	private Button btn_receipt_bri;
	private Button btn_remove_receipt_mandiri;
	private Button btn_remove_receipt_bca;
	private Button btn_remove_receipt_bri;
	private ImageView img_receipt_mandiri;
	private ImageView img_receipt_bca;
	private ImageView img_receipt_bri;

	public void markAsComplete(View v) {
		bottomSheetDialog = new BottomSheetDialog(SaleDetailActivity.this);
		View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_mark_complete, null);
		bottomSheetDialog.setContentView(sheetView);
		((TextView) sheetView.findViewById(R.id.debt_must_pay)).setText(payment_debt.getText());
		transfer_bank_header = (TextView) sheetView.findViewById(R.id.transfer_bank_header);
		transfer_bank_container = (LinearLayout) sheetView.findViewById(R.id.transfer_bank_container);
		finish_submit_button = (Button) sheetView.findViewById(R.id.finish_submit_button);
		cash_receive = (EditText) sheetView.findViewById(R.id.cash_receive);
		nominal_bca = (EditText) sheetView.findViewById(R.id.nominal_bca);
		nominal_mandiri = (EditText) sheetView.findViewById(R.id.nominal_mandiri);
		nominal_bri = (EditText) sheetView.findViewById(R.id.nominal_bri);
		edc_header = (TextView) sheetView.findViewById(R.id.edc_header);
		edc_container = (LinearLayout) sheetView.findViewById(R.id.edc_container);
		edc_card_type = (EditText) sheetView.findViewById(R.id.edc_card_type);
		edc_card_number  = (EditText) sheetView.findViewById(R.id.edc_card_number);
		edc_nominal  = (EditText) sheetView.findViewById(R.id.edc_nominal);

		btn_receipt_mandiri = (Button) sheetView.findViewById(R.id.btn_receipt_mandiri);
		btn_receipt_bca = (Button) sheetView.findViewById(R.id.btn_receipt_bca);
		btn_receipt_bri = (Button) sheetView.findViewById(R.id.btn_receipt_bri);
		img_receipt_mandiri = (ImageView) sheetView.findViewById(R.id.img_receipt_mandiri);
		img_receipt_bca = (ImageView) sheetView.findViewById(R.id.img_receipt_bca);
		img_receipt_bri = (ImageView) sheetView.findViewById(R.id.img_receipt_bri);
		btn_remove_receipt_mandiri = (Button) sheetView.findViewById(R.id.btn_remove_receipt_mandiri);
		btn_remove_receipt_bca = (Button) sheetView.findViewById(R.id.btn_remove_receipt_bca);
		btn_remove_receipt_bri = (Button) sheetView.findViewById(R.id.btn_remove_receipt_bri);

		bottomSheetDialog.show();

		payment_items =  new ArrayList<PaymentItem>();
		triggerBottomDialogButton(sheetView);
	}

	private void triggerBottomDialogButton(View view) {
		setTextChangeListener(cash_receive, "cashReceive");
		setTextChangeListener(nominal_mandiri, "nominal_mandiri");
		setTextChangeListener(nominal_bca, "nominal_bca");
		setTextChangeListener(nominal_bri, "nominal_bri");
		setTextChangeListener(edc_card_number, "card_number");
		setTextChangeListener(edc_nominal, "nominal_edc");

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

		edc_header.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (edc_container.getVisibility() == View.GONE) {
					edc_container.setVisibility(View.VISIBLE);
					edc_header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_remove_black, 0);
				} else {
					edc_container.setVisibility(View.GONE);
					edc_header.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_add_black, 0);
				}
			}
		});

		btn_receipt_mandiri.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFileChooser("mandiri");
			}
		});

		btn_receipt_bca.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFileChooser("bca");
			}
		});

		btn_receipt_bri.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				showFileChooser("bri");
			}
		});

		btn_remove_receipt_mandiri.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				img_receipt_mandiri.setVisibility(View.GONE);
				btn_receipt_mandiri.setVisibility(View.VISIBLE);
				btn_remove_receipt_mandiri.setVisibility(View.GONE);
				removeReceiptBitmap("mandiri");
			}
		});

		btn_remove_receipt_bca.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				img_receipt_bca.setVisibility(View.GONE);
				btn_receipt_bca.setVisibility(View.VISIBLE);
				btn_remove_receipt_bca.setVisibility(View.GONE);
				removeReceiptBitmap("bca");
			}
		});

		btn_remove_receipt_bri.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				img_receipt_bri.setVisibility(View.GONE);
				btn_receipt_bri.setVisibility(View.VISIBLE);
				btn_remove_receipt_bri.setVisibility(View.GONE);
				removeReceiptBitmap("bri");
			}
		});

		try {
			Bitmap bt_mandiri = getReceiptBitmap("mandiri");
			if (bt_mandiri != null) {
				img_receipt_mandiri.setVisibility(View.VISIBLE);
				img_receipt_mandiri.setImageBitmap(bt_mandiri);
				btn_remove_receipt_mandiri.setVisibility(View.VISIBLE);
				btn_receipt_mandiri.setVisibility(View.GONE);
			}
			Bitmap bt_bca = getReceiptBitmap("bca");
			if (bt_bca != null) {
				img_receipt_bca.setVisibility(View.VISIBLE);
				img_receipt_bca.setImageBitmap(bt_bca);
				btn_remove_receipt_bca.setVisibility(View.VISIBLE);
				btn_receipt_bca.setVisibility(View.GONE);
			}
			Bitmap bt_bri = getReceiptBitmap("bri");
			if (bt_bri != null) {
				img_receipt_bri.setVisibility(View.VISIBLE);
				img_receipt_bri.setImageBitmap(bt_bri);
				btn_remove_receipt_bri.setVisibility(View.VISIBLE);
				btn_receipt_bri.setVisibility(View.GONE);
			}
		} catch (Exception e){e.printStackTrace();}

		finish_submit_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// get all total data
				String cash = cash_receive.getText().toString();
				String bca = nominal_bca.getText().toString();
				String mandiri = nominal_mandiri.getText().toString();
				String bri = nominal_bri.getText().toString();
				String edc = edc_nominal.getText().toString();

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
					PaymentItem pi_bri = new PaymentItem("nominal_bri", Double.parseDouble(bri));
					payment_items.add(pi_bri);
					tot_payment = tot_payment + Double.parseDouble(bri);
				}

				if (edc.length() > 0) {
					if (edc.contains(".")) {
						edc = edc.replaceAll("\\.", "");
					}
					PaymentItem pi_edc = new PaymentItem("nominal_edc", Double.parseDouble(edc));
					payment_items.add(pi_edc);
					tot_payment = tot_payment + Double.parseDouble(edc);
				}

				if (tot_payment < tot_debt) {
					Toast.makeText(getApplicationContext(),
							"Pembayaran masih kurang " + (tot_debt - tot_payment),
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

							if (is_local_data) {
								paymentCatalog.addPayment(saleId, pi.getTitle(), pi.getNominal());
							}
						}

						mObj.put("payment", arrPaymentList);
						_complete_inv(mObj);
					} catch (Exception e){
						e.printStackTrace();
					}
					/*Toast.makeText(getApplicationContext(),
							"Total submited payment : " + tot_payment,
							Toast.LENGTH_SHORT).show();*/
				}
			}
		});
	}

	/**
	 * Use to set complete to server
	 * @param mObj
	 */
	private void _complete_inv(final Map mObj) {
		Map<String, String> params = new HashMap<String, String>();
		if (payment_receipts.containsKey("mandiri")) {
			String base64 = encodeToBase64("mandiri");
			if (base64 != null) {
				params.put("receipt_mandiri", base64);
			}
		}
		if (payment_receipts.containsKey("bca")) {
			String base64 = encodeToBase64("bca");
			if (base64 != null) {
				params.put("receipt_bca", base64);
			}
		}
		if (payment_receipts.containsKey("bri")) {
			String base64 = encodeToBase64("bri");
			if (base64 != null) {
				params.put("receipt_bri", base64);
			}
		}
		String _url = Server.URL + "transaction/complete-payment?api-key=" + Server.API_KEY;
		String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
		_url += "&"+ qry;

		String admin_id = sharedpreferences.getString(TAG_ID, null);
		Params adminParam = paramCatalog.getParamByName("admin_id");
		if (adminParam != null) {
			admin_id = adminParam.getValue();
		}

		params.put("admin_id", admin_id);
		//Log.e(TAG, "params complete-payment : "+ params.toString());

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
								String server_invoice_number = jObj.getString("invoice_number");
								sale.setServerInvoiceNumber(server_invoice_number);
								if (is_local_data) {
									saleLedger.setFinished(sale);
								}

								bottomSheetDialog.dismiss();

								//Intent newActivity = new Intent(getApplicationContext(), MainActivity.class);
								Intent newActivity = new Intent(SaleDetailActivity.this,
										PrintPreviewActivity.class);
								newActivity.putExtra("saleId", saleId);
								newActivity.putExtra("shipping_method", shipping.getMethod());
								newActivity.putExtra("process_order", true);
								if (!is_local_data) {
									Sale new_sale = new Sale(saleId, sale.getEndTime());
									new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
									new_sale.setServerInvoiceId(sale.getServerInvoiceId());
									new_sale.setCustomerId(sale.getCustomerId());
									new_sale.setStatus(sale.getStatus());
									new_sale.setDiscount(sale.getDiscount());

									newActivity.putExtra("sale_intent", new_sale);
									newActivity.putExtra("customer_intent", customer_intent);
									newActivity.putExtra("shipping_intent", shipping_intent);

                                    JSONArray arrPayment = null;
                                    try {
                                        arrPayment = new JSONArray(payment_intent);
                                        if (arrPayment != null) {
                                            for (PaymentItem pi : payment_items) {
                                                arrPayment.put(pi.toMap());
                                            }
                                            payment_intent = arrPayment.toString();
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

									newActivity.putExtra("payment_intent", payment_intent);
									newActivity.putExtra("line_items_intent", line_items_intent);
								}
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

						String formatted = "";
						if (setType.equals("card_number")) {
							formatted = Tools.cardFormat(cleanString);
							current_val = formatted;
						} else {
							double parsed = Double.parseDouble(cleanString);
							formatted = CurrencyController.getInstance().moneyFormat(parsed);

							current_val = formatted;
						}
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

	public void finishAndPrint(View v) {
		try {
			Map<String, Object> mObj = new HashMap<String, Object>();
			mObj.put("invoice_id", sale.getServerInvoiceId());

			String _url = Server.URL + "transaction/complete?api-key=" + Server.API_KEY;
			String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
			_url += "&"+ qry;

			Map<String, String> params = new HashMap<String, String>();
			String admin_id = sharedpreferences.getString(TAG_ID, null);
			Params adminParam = paramCatalog.getParamByName("admin_id");
			if (adminParam != null) {
				admin_id = adminParam.getValue();
			}

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
								// Check for error node in json
								if (success == 1) {
									String server_invoice_number = jObj.getString("invoice_number");
									sale.setServerInvoiceNumber(server_invoice_number);
									if (is_local_data) {
										saleLedger.setFinished(sale);
									}

									Intent intent = new Intent(SaleDetailActivity.this, PrintPreviewActivity.class);
									intent.putExtra("saleId", saleId);
									finish();
									startActivity(intent);
								}
							} catch (JSONException e) {
								e.printStackTrace();
							}
						}
					});
		} catch (Exception e){e.printStackTrace();}
	}

	public void returInvoice(View v) {
		Intent intent = new Intent(SaleDetailActivity.this, ReturActivity.class);
		intent.putExtra("saleId", saleId+"");
		//if (!is_local_data) {
			Sale new_sale = new Sale(saleId, sale.getEndTime());
			new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
			new_sale.setServerInvoiceId(sale.getServerInvoiceId());
			new_sale.setCustomerId(sale.getCustomerId());
			new_sale.setStatus(sale.getStatus());
			new_sale.setDiscount(sale.getDiscount());

			intent.putExtra("sale_intent", new_sale);
			intent.putExtra("customer_intent", customer_intent);
			intent.putExtra("shipping_intent", shipping_intent);
			intent.putExtra("payment_intent", payment_intent);
			intent.putExtra("line_items_intent", line_items_intent);
		//}

		finish();
		startActivity(intent);
	}

	public void finishRequest(View v) {
		Intent newActivity = new Intent(SaleDetailActivity.this,
				PrintPreviewActivity.class);
		newActivity.putExtra("saleId", saleId);
		newActivity.putExtra("shipping_method", shipping.getMethod());
		newActivity.putExtra("process_order", true);
		if (!is_local_data) {
			Sale new_sale = new Sale(saleId, sale.getEndTime());
			new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
			new_sale.setServerInvoiceId(sale.getServerInvoiceId());
			new_sale.setCustomerId(sale.getCustomerId());
			new_sale.setStatus(sale.getStatus());
			new_sale.setDiscount(sale.getDiscount());

			newActivity.putExtra("sale_intent", new_sale);
			newActivity.putExtra("customer_intent", customer_intent);
			newActivity.putExtra("shipping_intent", shipping_intent);
			newActivity.putExtra("payment_intent", payment_intent);
			newActivity.putExtra("line_items_intent", line_items_intent);
		}
		startActivity(newActivity);
	}

	private RecyclerView returItemListRecycle;
	private RecyclerView refundItemListRecycle;
	private RecyclerView changeItemListRecycle;
	private List<LineItem> returitemList = new ArrayList<LineItem>();
	private List<LineItem> refunditemList = new ArrayList<LineItem>();
	private List<LineItem> changeitemList = new ArrayList<LineItem>();
	private JSONArray returJSONArray = new JSONArray();
	private JSONArray paymentJSONArray = new JSONArray();
	private JSONArray changeOtherItemsJSONArray = new JSONArray();
	private LinearLayout change_item_container;
	private LinearLayout refund_item_container;
	private LinearLayout change_other_item_container;
	private LinearLayout payment_detail_container;
	private RecyclerView returPaymentListView;
	private TextView retur_invoice_number;
	private TextView retur_proceed_by;
	private TextView refunded_at;
	// for print preview
    private ArrayList arrRefundList = new ArrayList();
    private ArrayList arrPaymentList = new ArrayList();
    private ArrayList arrChangeItemList = new ArrayList();
    private ArrayList<String> retur_reason = new ArrayList<String>();

	private void buildReturInformation() {
		returItemListRecycle = (RecyclerView) findViewById(R.id.returItemListRecycle);
		returItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		returItemListRecycle.setHasFixedSize(true);
		returItemListRecycle.setNestedScrollingEnabled(false);

		refundItemListRecycle = (RecyclerView) findViewById(R.id.refundItemListRecycle);
		refundItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		refundItemListRecycle.setHasFixedSize(true);
		refundItemListRecycle.setNestedScrollingEnabled(false);

		changeItemListRecycle = (RecyclerView) findViewById(R.id.changeItemListRecycle);
		changeItemListRecycle.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
		changeItemListRecycle.setHasFixedSize(true);
		changeItemListRecycle.setNestedScrollingEnabled(false);

		change_item_container = (LinearLayout) findViewById(R.id.change_item_container);
		refund_item_container = (LinearLayout) findViewById(R.id.refund_item_container);
		change_other_item_container = (LinearLayout) findViewById(R.id.change_other_item_container);
		payment_detail_container = (LinearLayout) findViewById(R.id.payment_detail_container);

		try {
			returJSONArray = obj_retur.getJSONArray("items");
			if (obj_retur.has("payments") && !obj_retur.getString("payments").equals("null")) {
				paymentJSONArray = obj_retur.getJSONArray("payments");
			}
			if (obj_retur.has("items_change")) {
				changeOtherItemsJSONArray = obj_retur.getJSONArray("items_change");
			}
			// build other information
			retur_invoice_number = (TextView) findViewById(R.id.retur_invoice_number);
			retur_invoice_number.setText(obj_retur.getString("invoice_number"));
			retur_proceed_by = (TextView) findViewById(R.id.retur_proceed_by);
			retur_proceed_by.setText(obj_retur.getString("refunded_by_name"));
			refunded_at = (TextView) findViewById(R.id.refunded_at);
			refunded_at.setText(DateTimeStrategy.parseDate(obj_retur.getString("refunded_at"), "dd MMM yyyy HH:s") + "");
			if (obj_retur.has("reasons")) {
                JSONArray reasonJSONArray = obj_retur.getJSONArray("reasons");
                if (reasonJSONArray.length() > 0) {
                    for (int i = 0; i < reasonJSONArray.length(); i++) {
                        try {
                            retur_reason.add(reasonJSONArray.getString(i));
                        } catch (Exception e){}
                    }
                }
            }
		} catch (Exception e){e.printStackTrace();}

		returitemList.clear();
		if(returJSONArray!=null && returJSONArray.length()>0){
			for (int i = 0; i < returJSONArray.length(); i++) {
				try {
					JSONObject obj_retur_item = returJSONArray.getJSONObject(i);
					Product product = productCatalog.getProductByBarcode(obj_retur_item.getString("id"));

					if (obj_retur_item.has("returned_qty") && obj_retur_item.getInt("returned_qty") > 0) {
						LineItem lineItem = new LineItem(product, obj_retur_item.getInt("returned_qty"), tot_inv_qty);
						returitemList.add(lineItem);
					}
					if (obj_retur_item.has("refunded_qty") && obj_retur_item.getInt("refunded_qty") > 0) {
						LineItem lineItem2 = new LineItem(product, obj_retur_item.getInt("refunded_qty"), tot_inv_qty);
						refunditemList.add(lineItem2);
					}
					// for print preview
                    Map<String, String> arrRefundList2 = new HashMap<String, String>();
                    arrRefundList2.put("title", product.getName());
                    arrRefundList2.put("product_id", product.getBarcode()+"");
                    arrRefundList2.put("quantity", obj_retur_item.getInt("refunded_qty")+"");
                    arrRefundList2.put("price", product.getUnitPriceByQuantity(product.getId(), tot_inv_qty)+"");

                    if (obj_retur_item.has("returned_qty") && obj_retur_item.getInt("returned_qty") > 0) {
                        arrRefundList2.put("change_item", obj_retur_item.getString("returned_qty"));
                    } else {
                        arrRefundList2.put("change_item", "0");
                    }

                    arrRefundList.add(arrRefundList2);
				} catch (JSONException e) {}
			}
			if (returitemList.size() <= 0) {
				change_item_container.setVisibility(View.GONE);
			}
			if (refunditemList.size() <= 0) {
				refund_item_container.setVisibility(View.GONE);
			}
		}

		// also build change other items
		changeitemList.clear();
		int inv_quantity_total = 0;
		Log.e(getClass().getSimpleName(), "changeOtherItemsJSONArray : " + changeOtherItemsJSONArray.toString());
		if(changeOtherItemsJSONArray!=null && changeOtherItemsJSONArray.length()>0){
			for (int i = 0; i < changeOtherItemsJSONArray.length(); i++) {
				try {
					JSONObject obj_change_item = changeOtherItemsJSONArray.getJSONObject(i);
                    inv_quantity_total = obj_change_item.getInt("quantity_total");
					Product product;
					if (obj_change_item.has("id")) {
						product = productCatalog.getProductByBarcode(obj_change_item.getString("id"));
					} else {
						product = productCatalog.getProductByName(obj_change_item.getString("name")).get(0);
					}
					if ((product != null) && obj_change_item.has("quantity") && obj_change_item.getInt("quantity") > 0) {
						LineItem lineItem = new LineItem(product, obj_change_item.getInt("quantity"), obj_change_item.getInt("quantity_total"));
						changeitemList.add(lineItem);

						// for print preview intent data
                        Map<String, String> arrChangeItemList2 = new HashMap<String, String>();
                        arrChangeItemList2.put("id", product.getId()+"");
                        arrChangeItemList2.put("product_id", product.getBarcode()+"");
                        arrChangeItemList2.put("title", product.getName());
                        arrChangeItemList2.put("quantity", lineItem.getQuantity()+"");
                        arrChangeItemList2.put("price", lineItem.getPriceAtSale()+"");
                        arrChangeItemList2.put("quantity_total", obj_change_item.getInt("quantity_total")+"");
                        arrChangeItemList.add(arrChangeItemList2);
					}
				} catch (JSONException e) {}
			}
			if (changeitemList.size() > 0) {
				change_other_item_container.setVisibility(View.VISIBLE);
			}
		}

		AdapterListReturReport sAdap = new AdapterListReturReport(SaleDetailActivity.this, returitemList, register);
		returItemListRecycle.setAdapter(sAdap);

		AdapterListReturReport rfAdap = new AdapterListReturReport(SaleDetailActivity.this, refunditemList, register);
		rfAdap.setType("refund");
		refundItemListRecycle.setAdapter(rfAdap);

		AdapterListReturReport ciAdap = new AdapterListReturReport(SaleDetailActivity.this, changeitemList, register);
		ciAdap.setType("change_item");
		changeItemListRecycle.setAdapter(ciAdap);

		// build the payment if any
		if (refunditemList.size() > 0) {
			returPaymentListView = (RecyclerView) findViewById(R.id.returPaymentList);
			returPaymentListView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
			returPaymentListView.setHasFixedSize(true);
			returPaymentListView.setNestedScrollingEnabled(false);

			List<Payment> pyitemList = new ArrayList<Payment>();
			if (paymentJSONArray != null && paymentJSONArray.length() > 0) {
				for (int i = 0; i < paymentJSONArray.length(); i++) {
					try {
						JSONObject obj_payment_item = paymentJSONArray.getJSONObject(i);
						Payment payment = new Payment(
								-1,
								sale.getId(),
								obj_payment_item.getString("type"),
								obj_payment_item.getDouble("amount")
						);

						pyitemList.add(payment);
                        Map<String, String> arrPayment2 = new HashMap<String, String>();
                        arrPayment2.put("type", obj_payment_item.getString("type"));
                        arrPayment2.put("amount_tendered", ""+ obj_payment_item.getDouble("amount"));
                        // for print preview
                        arrPaymentList.add(arrPayment2);
					} catch (Exception e){}
				}
			}

			AdapterListPaymentSimple pAdap = new AdapterListPaymentSimple(pyitemList);
			returPaymentListView.setAdapter(pAdap);
		}

		if (paymentJSONArray.length() <= 0) {
			payment_detail_container.setVisibility(View.GONE);
		}

		retur_information.setVisibility(View.VISIBLE);
	}

	public void printReturInvoice(View view) {
        if (obj_retur != null) {
            try {
                Retur retur = new Retur(obj_retur.getInt("id"));
                retur.setItems(arrRefundList);
                retur.setPayment(arrPaymentList);
                retur.setCustomer(customer);
                retur.setItemsChange(arrChangeItemList);
                if (retur_reason.size() > 0) {
                    retur.setItemsReason(retur_reason);
                }
                /*if (retur_notes.getText().toString().length() > 0) {
                    retur.setNotes(retur_notes.getText().toString());
                }*/

                Intent intent = new Intent(SaleDetailActivity.this,
                        PrintReturActivity.class);
                intent.putExtra("retur_intent", retur);
                intent.putExtra("just_print", true);
                finish();
                startActivity(intent);
            } catch (Exception e){e.printStackTrace();}
        }
    }

	private void _server_remove_inv(Map mObj) {
		String _url = Server.URL + "transaction/delete?api-key=" + Server.API_KEY;
		String qry = URLBuilder.httpBuildQuery(mObj, "UTF-8");
		_url += "&"+ qry;

		Map<String, String> params = new HashMap<String, String>();
		String admin_id = sharedpreferences.getString(TAG_ID, null);
		Params adminParam = paramCatalog.getParamByName("admin_id");
		if (adminParam != null) {
			admin_id = adminParam.getValue();
		}

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
							// Check for error node in json
							if (success == 1) {
								saleLedger.removeSale(sale);
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}

						hideDialog();
					}
				});
	}

	private void showFileChooser(String bank_name) {
		Intent intent = new Intent();
		intent.setType("image/*");
		intent.setAction(Intent.ACTION_GET_CONTENT);
		//startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
		Integer pick_image_request = 1;
		if (bank_name.equals("mandiri")) {
			pick_image_request = 1;
		} else if (bank_name.equals("bca")) {
			pick_image_request = 2;
		} else if (bank_name.equals("bri")) {
			pick_image_request = 3;
		}
		startActivityForResult(Intent.createChooser(intent, "Select Picture"), pick_image_request);
	}

	private Bitmap getReceiptBitmap(String bank_name) {
		Bitmap src = null;
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS/" + bank_name + ".jpg";
			File file = new File(bank_name);
			if(file.exists()) {
				src = BitmapFactory.decodeStream(openFileInput(bank_name));
			}
		} catch (FileNotFoundException e) {e.printStackTrace();}

		return src;
	}

	private void removeReceiptBitmap(String bank_name) {
		try {
			String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS/" + bank_name + ".jpg";
			File file = new File(bank_name);
			if(file.exists()) {
				deleteFile(path);
			}
		} catch (Exception e){e.printStackTrace();}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		final int reqCode = requestCode;
		if (resultCode == RESULT_OK && data != null && data.getData() != null) {
			Uri filePath = data.getData();
			try {
				Glide.with(getApplicationContext())
						.asBitmap()
						.load(filePath)
						.into(new SimpleTarget<Bitmap>() {
							@Override
							public void onResourceReady(Bitmap bitmap,
														Transition<? super Bitmap> transition) {
								int w = bitmap.getWidth();
								int h = bitmap.getHeight();
								Log.e(getClass().getSimpleName(), "width : "+ w + " Height : "+ h);
								float density = getResources().getDisplayMetrics().density;
								if (density > 2) {
									density = 2;
								}
								Log.e(getClass().getSimpleName(), "density : "+ density);
								int bounding = Math.round(250 * density);
								Log.e(getClass().getSimpleName(), "bounding : "+ bounding);
								float xScale = ((float) bounding) / w;
								float yScale = ((float) bounding) / h;
								Log.e(getClass().getSimpleName(), "xScale : "+ xScale + " yScale : "+ yScale);
								float scale = (xScale <= yScale) ? xScale : yScale;

								// Create a matrix for the scaling and add the scaling data
								Matrix matrix = new Matrix();
								matrix.postScale(scale, scale);

								// Create a new bitmap and convert it to a format understood by the ImageView
								Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
								int w2 = scaledBitmap.getWidth();
								int h2 = scaledBitmap.getHeight();
								Log.e(getClass().getSimpleName(), "scalled width : "+ w2 + " Scalled Height : "+ h2);
								if (reqCode == 1) {
									img_receipt_mandiri.setImageBitmap(scaledBitmap);
									img_receipt_mandiri.setVisibility(View.VISIBLE);
									btn_receipt_mandiri.setVisibility(View.GONE);
									btn_remove_receipt_mandiri.setVisibility(View.VISIBLE);
									File savedBitmap = saveReceiptBitmap(scaledBitmap, "mandiri.jpg");
									setReceiptBitmap("mandiri", scaledBitmap);
								} else if (reqCode == 2) {
									img_receipt_bca.setImageBitmap(scaledBitmap);
									img_receipt_bca.setVisibility(View.VISIBLE);
									btn_receipt_bca.setVisibility(View.GONE);
									btn_remove_receipt_bca.setVisibility(View.VISIBLE);

									File savedBitmap = saveReceiptBitmap(scaledBitmap, "bca.jpg");
									setReceiptBitmap("bca", scaledBitmap);
								} else if (reqCode == 3) {
									img_receipt_bri.setImageBitmap(scaledBitmap);
									img_receipt_bri.setVisibility(View.VISIBLE);
									btn_receipt_bri.setVisibility(View.GONE);
									btn_remove_receipt_bri.setVisibility(View.VISIBLE);

									File savedBitmap = saveReceiptBitmap(scaledBitmap, "bri.jpg");
									setReceiptBitmap("bri", scaledBitmap);
								}
							}
						});

			} catch (Exception e) {e.printStackTrace();}
		}
	}

	private void setReceiptBitmap(String bank_name, Bitmap bitmap) {
		payment_receipts.put(bank_name, bitmap);
	}

	private String encodeToBase64(final String file_name){
		try {
			String url = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS/" + file_name +".jpg";
			File file = new File(url);
			byte[] bFile = new byte[(int) file.length()];
			FileInputStream inputStream = new FileInputStream(url);
			inputStream.read(bFile);
			inputStream.close();
			return Base64.encodeToString(bFile, Base64.NO_WRAP);
		} catch (IOException e) { e.printStackTrace();}
		return null;
	}

	private static File saveReceiptBitmap(Bitmap bm, String fileName){
		final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/UcokPOS";
		File dir = new File(path);
		if(!dir.exists())
			dir.mkdirs();
		File file = new File(dir, fileName);
		try {
			FileOutputStream fOut = new FileOutputStream(file);
			bm.compress(Bitmap.CompressFormat.JPEG, 100, fOut);
			fOut.flush();
			fOut.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return file;
	}

    public void takeTheGood(View v) {
        Intent intent = new Intent(SaleDetailActivity.this, DepositActivity.class);
        intent.putExtra("saleId", saleId+"");

        Sale new_sale = new Sale(saleId, sale.getEndTime());
        new_sale.setServerInvoiceNumber(sale.getServerInvoiceNumber());
        new_sale.setServerInvoiceId(sale.getServerInvoiceId());
        new_sale.setCustomerId(sale.getCustomerId());
        new_sale.setStatus(sale.getStatus());
        new_sale.setDiscount(sale.getDiscount());

        intent.putExtra("sale_intent", new_sale);
        intent.putExtra("customer_intent", customer_intent);
        intent.putExtra("shipping_intent", shipping_intent);
        intent.putExtra("payment_intent", payment_intent);
        intent.putExtra("line_items_intent", line_items_intent);

        //finish();
        startActivity(intent);
    }
}
