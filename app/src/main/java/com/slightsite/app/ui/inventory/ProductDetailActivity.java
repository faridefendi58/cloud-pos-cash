package com.slightsite.app.ui.inventory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

import com.slightsite.app.R;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.inventory.ProductDiscount;
import com.slightsite.app.domain.inventory.ProductLot;
import com.slightsite.app.domain.inventory.Stock;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.sale.SaleFragment;

/**
 * UI for shows the datails of each Product.
 *
 */
@SuppressLint("NewApi")
public class ProductDetailActivity extends Activity {

	private ProductCatalog productCatalog;
	private ProductDiscount productDiscount;
	private Stock stock;
	private Product product;
	private List<Map<String, String>> stockList;
	private List<Map<String, String>> discountList;
	private EditText nameBox;
	private EditText barcodeBox;
	private TextView stockSumBox;
	private EditText priceBox;
	private Button addProductLotButton;
	private Button addProductDiscountButton;
	private Button submitEditButton;
	private Button cancelEditButton;
	private Button openEditButton;
	private TabHost mTabHost;
	private ListView stockListView;
	private ListView discountListView;
	private String id;
	private String[] remember;
	private AlertDialog.Builder popDialog;
	private LayoutInflater inflater ;
	private Resources res;
	private EditText costBox;
	private EditText quantityBox;
	private EditText quantityMaxBox;
	private Button confirmButton;
	private Button clearButton;
	private View Viewlayout;
	private AlertDialog alert;
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.detail_menu, menu);
	    return true;
	  } 
	
	@SuppressLint("NewApi")
	private void initiateActionBar() {
		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			ActionBar actionBar = getActionBar();
			actionBar.setDisplayHomeAsUpEnabled(true);
			//actionBar.setTitle(res.getString(R.string.product_detail));
			actionBar.setTitle(product.getName());
			actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
		}
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		res = getResources();
		
		try {
			stock = Inventory.getInstance().getStock();
			productCatalog = Inventory.getInstance().getProductCatalog();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		id = getIntent().getStringExtra("id");
		product = productCatalog.getProductById(Integer.parseInt(id));

		initiateActionBar();

		initUI(savedInstanceState);
		remember = new String[3];
		nameBox.setText(product.getName());
		priceBox.setText(product.getUnitPrice() + "");
		barcodeBox.setText(product.getBarcode());

	}

	/**
	 * Initiate this UI.
	 * @param savedInstanceState
	 */
	private void initUI(Bundle savedInstanceState) {
		setContentView(R.layout.layout_productdetail_main);
		stockListView = (ListView) findViewById(R.id.stockListView);
		discountListView = (ListView) findViewById(R.id.discountListView);
		nameBox = (EditText) findViewById(R.id.nameBox);
		priceBox = (EditText) findViewById(R.id.priceBox);
		barcodeBox = (EditText) findViewById(R.id.barcodeBox);
		stockSumBox = (TextView) findViewById(R.id.stockSumBox);
		submitEditButton = (Button) findViewById(R.id.submitEditButton);
		submitEditButton.setVisibility(View.INVISIBLE);
		cancelEditButton = (Button) findViewById(R.id.cancelEditButton);
		cancelEditButton.setVisibility(View.INVISIBLE);
		openEditButton = (Button) findViewById(R.id.openEditButton);
		openEditButton.setVisibility(View.VISIBLE);
		addProductLotButton = (Button) findViewById(R.id.addProductLotButton);
		addProductDiscountButton = (Button) findViewById(R.id.addProductDiscountButton);
		mTabHost = (TabHost) findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(res.getString(R.string.product_detail))
				.setContent(R.id.tab1));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator(res.getString(R.string.stock))
				.setContent(R.id.tab2));
		mTabHost.addTab(mTabHost.newTabSpec("tab_test3").setIndicator(res.getString(R.string.product_discount))
				.setContent(R.id.tab3));
		mTabHost.setCurrentTab(0);
		popDialog = new AlertDialog.Builder(this);
		inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
		addProductLotButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showAddProductLot();
			}
		});

		openEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				edit();
			}
		});

		submitEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				submitEdit();
			}
		});
		
		cancelEditButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancelEdit();
			}
		});

		addProductDiscountButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showAddProductDiscount();
			}
		});
	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<ProductLot> list) {

		stockList = new ArrayList<Map<String, String>>();
		for (ProductLot productLot : list) {
			stockList.add(productLot.toMap());
		}

		SimpleAdapter sAdap = new SimpleAdapter(ProductDetailActivity.this, stockList,
				R.layout.listview_stock, new String[] { "dateAdded",
				"cost", "quantity" }, new int[] {
				R.id.dateAdded, R.id.cost, R.id.quantity, });
		stockListView.setAdapter(sAdap);
	}

	/**
	 * Show list discount.
	 * @param list
	 */
	private void showListDiscount(List<ProductDiscount> list) {

		discountList = new ArrayList<Map<String, String>>();
		for (ProductDiscount productDiscount : list) {
			discountList.add(productDiscount.toMap());
		}

		SimpleAdapter sAdap = new SimpleAdapter(ProductDetailActivity.this, discountList,
				R.layout.listview_discount, new String[] {
				"quantity", "quantity_max", "cost" },
				new int[] {
						R.id.quantity, R.id.quantity_max, R.id.cost });
		discountListView.setAdapter(sAdap);

		triggerDiscountList();
	}

	@Override
	protected void onResume() {
		super.onResume();
		int productId = Integer.parseInt(id);
		stockSumBox.setText(stock.getStockSumById(productId)+"");
		showList(stock.getProductLotByProductId(productId));
		showListDiscount(stock.getProductDiscountByProductId(productId));
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			this.finish();
			return true;
		case R.id.action_edit:
			edit();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * Submit editing.
	 */
	private void submitEdit() {
		nameBox.setFocusable(false);
		nameBox.setFocusableInTouchMode(false);
		nameBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		priceBox.setFocusable(false);
		priceBox.setFocusableInTouchMode(false);
		priceBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		barcodeBox.setFocusable(false);
		barcodeBox.setFocusableInTouchMode(false);
		barcodeBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		product.setName(nameBox.getText().toString());
		if(priceBox.getText().toString().equals(""))
			priceBox.setText("0.0");
		product.setUnitPrice(Double.parseDouble(priceBox.getText().toString()));
		product.setBarcode(barcodeBox.getText().toString());
		productCatalog.editProduct(product);
		submitEditButton.setVisibility(View.INVISIBLE);
		cancelEditButton.setVisibility(View.INVISIBLE);
		openEditButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Cancel editing.
	 */
	private void cancelEdit() {
		nameBox.setFocusable(false);
		nameBox.setFocusableInTouchMode(false);
		nameBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		priceBox.setFocusable(false);
		priceBox.setFocusableInTouchMode(false);
		priceBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		barcodeBox.setFocusable(false);
		barcodeBox.setFocusableInTouchMode(false);
		barcodeBox.setBackgroundColor(Color.parseColor("#87CEEB"));
		submitEditButton.setVisibility(View.INVISIBLE);
		cancelEditButton.setVisibility(View.INVISIBLE);
		nameBox.setText(remember[0]);
		priceBox.setText(remember[1]);
		barcodeBox.setText(remember[2]);
		openEditButton.setVisibility(View.VISIBLE);
	}
	
	/**
	 * Edit
	 */
	private void edit() {
		nameBox.setFocusable(true);
		nameBox.setFocusableInTouchMode(true);
		nameBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		priceBox.setFocusable(true);
		priceBox.setFocusableInTouchMode(true);
		priceBox.setBackgroundColor(Color.parseColor("#FFBB33"));
		barcodeBox.setFocusable(true);
		barcodeBox.setFocusableInTouchMode(true);
		barcodeBox.setBackgroundColor(Color.parseColor("#FFBB33"));	
		remember[0] = nameBox.getText().toString();
		remember[1] = priceBox.getText().toString();
		remember[2] = barcodeBox.getText().toString();
		submitEditButton.setVisibility(View.VISIBLE);
		cancelEditButton.setVisibility(View.VISIBLE);
		openEditButton.setVisibility(View.INVISIBLE);
	}
	
	/**
	 * Show adding product lot.
	 */
	private void showAddProductLot(){
		Viewlayout = inflater.inflate(R.layout.layout_addproductlot,
				(ViewGroup) findViewById(R.id.addProdutlot_dialog));
		popDialog.setView(Viewlayout);
		
		costBox = (EditText) Viewlayout.findViewById(R.id.costBox);
		quantityBox = (EditText) Viewlayout.findViewById(R.id.quantityBox);
		confirmButton = (Button) Viewlayout.findViewById(R.id.confirmButton);
		clearButton = (Button) Viewlayout.findViewById(R.id.clearButton); 
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (quantityBox.getText().toString().equals("") || costBox.getText().toString().equals("")) {
					Toast.makeText(ProductDetailActivity.this,
							res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
							.show();
				} else {
					boolean success = stock.addProductLot(
							DateTimeStrategy.getCurrentTime(), 
							Integer.parseInt(quantityBox.getText().toString()), 
							product, 
							Double.parseDouble(costBox.getText().toString()));

					if (success) {
						Toast.makeText(ProductDetailActivity.this, res.getString(R.string.success), Toast.LENGTH_SHORT).show();
						costBox.setText("");
						quantityBox.setText("");
						onResume();
						alert.dismiss();
						
						
					} else {
						Toast.makeText(ProductDetailActivity.this, res.getString(R.string.fail) ,Toast.LENGTH_SHORT).show();
					}
				}
				
			}
		});
		clearButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(quantityBox.getText().toString().equals("") && costBox.getText().toString().equals("")){
					alert.dismiss();
					onResume();
				}
				else{
					costBox.setText("");
					quantityBox.setText("");
				}	
			}
		});
		
		alert = popDialog.create();
		alert.show();
	}

	private void showAddProductDiscount(){
		Viewlayout = inflater.inflate(R.layout.layout_addproductdiscount,
				(ViewGroup) findViewById(R.id.addProductDiscount_dialog));
		popDialog.setView(Viewlayout);

		costBox = (EditText) Viewlayout.findViewById(R.id.costBox);
		quantityBox = (EditText) Viewlayout.findViewById(R.id.quantityBox);
		quantityMaxBox = (EditText) Viewlayout.findViewById(R.id.quantityMaxBox);
		confirmButton = (Button) Viewlayout.findViewById(R.id.confirmButton);
		clearButton = (Button) Viewlayout.findViewById(R.id.clearButton);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (quantityBox.getText().toString().equals("") || costBox.getText().toString().equals("")) {
					Toast.makeText(ProductDetailActivity.this,
							res.getString(R.string.please_input_all), Toast.LENGTH_SHORT)
							.show();
				} else {
					boolean success = stock.addProductDiscount(
							DateTimeStrategy.getCurrentTime(),
							Integer.parseInt(quantityBox.getText().toString()),
							Integer.parseInt(quantityMaxBox.getText().toString()),
							product,
							Double.parseDouble(costBox.getText().toString()));

					if (success) {
						Toast.makeText(ProductDetailActivity.this, res.getString(R.string.success), Toast.LENGTH_SHORT).show();
						costBox.setText("");
						quantityBox.setText("");
						onResume();
						alert.dismiss();


					} else {
						Toast.makeText(ProductDetailActivity.this, res.getString(R.string.fail) ,Toast.LENGTH_SHORT).show();
					}
				}

			}
		});
		clearButton.setOnClickListener(new View.OnClickListener(){

			@Override
			public void onClick(View v) {
				if(quantityBox.getText().toString().equals("") && costBox.getText().toString().equals("")){
					alert.dismiss();
					onResume();
				}
				else{
					costBox.setText("");
					quantityBox.setText("");
				}
			}
		});

		alert = popDialog.create();
		alert.show();
	}

	private void triggerDiscountList() {
		discountListView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showEditPopup(arg1,arg2);
			}
		});
	}

	private void showEditPopup(View anchorView, int position)
	{
		Log.e("Edit discount", "discount list : "+ discountList.get(position).toString());
		AlertDialog.Builder builder = new AlertDialog.Builder(ProductDetailActivity.this);
		View mView = getLayoutInflater().inflate(R.layout.dialog_discountedit, null);

		EditText quantityBox = (EditText) mView.findViewById(R.id.quantityBox);
		quantityBox.setText(discountList.get(position).get("quantity"));

		EditText quantityMaxBox = (EditText) mView.findViewById(R.id.quantityMaxBox);
		quantityMaxBox.setText(discountList.get(position).get("quantity_max"));

		EditText priceBox = (EditText) mView.findViewById(R.id.priceBox);
		priceBox.setText(discountList.get(position).get("cost"));

		TextView discount_id = (TextView) mView.findViewById(R.id.discount_id);
		discount_id.setText(discountList.get(position).get("id"));

		builder.setView(mView);
		AlertDialog dialog = builder.create();

		triggerSubmitUpdateDiscount(mView, dialog);

		dialog.show();
	}

	private void triggerSubmitUpdateDiscount(View view, final AlertDialog dialog) {
		final EditText quantityBox = (EditText) view.findViewById(R.id.quantityBox);
		final EditText quantityMaxBox = (EditText) view.findViewById(R.id.quantityMaxBox);
		final EditText priceBox = (EditText) view.findViewById(R.id.priceBox);
		final TextView discount_id = (TextView) view.findViewById(R.id.discount_id);

		Button confirmButton = (Button) view.findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stock.updateProductDiscount(
						Integer.parseInt(discount_id.getText().toString()),
						Integer.parseInt(quantityBox.getText().toString()),
						Integer.parseInt(quantityMaxBox.getText().toString()),
						Double.parseDouble(priceBox.getText().toString())
				);

				onResume();
				dialog.hide();
			}
		});

		Button removeButton = (Button) view.findViewById(R.id.removeButton);
		removeButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				stock.deleteProductDiscount(Integer.parseInt(discount_id.getText().toString()));

				onResume();
				dialog.hide();
			}
		});
	}
}