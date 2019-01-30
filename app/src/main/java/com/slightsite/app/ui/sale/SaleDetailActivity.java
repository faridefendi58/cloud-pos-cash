package com.slightsite.app.ui.sale;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.printer.PrinterActivity;

/**
 * UI for showing the detail of Sale in the record.
 *
 */
public class SaleDetailActivity extends Activity{
	
	private TextView totalBox;
	private TextView dateBox;
	private ListView lineitemListView;
	private List<Map<String, String>> lineitemList;
	private Sale sale;
	private int saleId;
	private SaleLedger saleLedger;
	private TextView customerBox;
	private Customer customer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		try {
			saleLedger = SaleLedger.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		saleId = Integer.parseInt(getIntent().getStringExtra("id"));
		sale = saleLedger.getSaleById(saleId);
		customer = saleLedger.getCustomerBySaleId(saleId);

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
		customerBox = (TextView) findViewById(R.id.customerBox);
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
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			Intent newActivity = new Intent(SaleDetailActivity.this, MainActivity.class);
			finish();
			startActivity(newActivity);
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
		showList(sale.getAllLineItem());
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
		Log.e("Sale detail act", "send saleId : "+ saleId);
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
}
