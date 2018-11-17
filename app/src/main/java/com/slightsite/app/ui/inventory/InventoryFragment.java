package com.slightsite.app.ui.inventory;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.slightsite.app.R;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.DatabaseExecutor;
import com.slightsite.app.techicalservices.Demo;
import com.slightsite.app.techicalservices.ExpandableHeightGridView;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.ButtonAdapter;
import com.slightsite.app.ui.component.UpdatableFragment;

/**
 * UI for Inventory, shows list of Product in the ProductCatalog.
 * Also use for a sale process of adding Product into sale.
 *
 *
 */
@SuppressLint("ValidFragment")
public class InventoryFragment extends UpdatableFragment {

	protected static final int SEARCH_LIMIT = 0;
	private GridView inventoryListView;
	private ProductCatalog productCatalog;
	private List<Map<String, String>> inventoryList;
	private com.github.clans.fab.FloatingActionButton addProductButton;
	private EditText searchBox;
	private Button scanButton;

	private ViewPager viewPager;
	private Register register;
	private MainActivity main;

	private UpdatableFragment saleFragment;
	private Resources res;

	/**
	 * Construct a new InventoryFragment.
	 * @param saleFragment
	 */
	public InventoryFragment(UpdatableFragment saleFragment) {
		super();
		this.saleFragment = saleFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.layout_inventory, container, false);

		res = getResources();
		inventoryListView = (GridView) view.findViewById(R.id.productListView);
		addProductButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.addProductButton);
		scanButton = (Button) view.findViewById(R.id.scanButton);
		searchBox = (EditText) view.findViewById(R.id.searchBox);

		main = (MainActivity) getActivity();
		viewPager = main.getViewPager();

		initUI();
		return view;
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI() {

		addProductButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				showPopup(v);
			}
		});

		searchBox.addTextChangedListener(new TextWatcher(){
			public void afterTextChanged(Editable s) {
				if (s.length() >= SEARCH_LIMIT) {
					search();
				}
			}
			public void beforeTextChanged(CharSequence s, int start, int count, int after){}
			public void onTextChanged(CharSequence s, int start, int before, int count){}
		});


		inventoryListView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> myAdapter, View myView, int position, long mylng) {
				int id = Integer.parseInt(inventoryList.get(position).get("id").toString());

				register.addItem(productCatalog.getProductById(id), 1);
				saleFragment.update();
				viewPager.setCurrentItem(1);

				Toast toast = Toast.makeText(
						getActivity().getApplicationContext(),
						productCatalog.getProductById(id).getName()+ ' ' +res.getString(R.string.message_success_added),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 8, 8);
				toast.show();
			}     
		});

		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(InventoryFragment.this);
				scanIntegrator.initiateScan();
			}
		});

	}

	/**
	 * Show list.
	 * @param list
	 */
	private void showList(List<Product> list) {

		inventoryList = new ArrayList<Map<String, String>>();
		for(Product product : list) {
			inventoryList.add(product.toMap());
		}

		ButtonAdapter sAdap = new ButtonAdapter(getActivity().getBaseContext(), inventoryList,
				R.layout.listview_inventory, new String[]{"name"}, new int[] {R.id.name}, R.id.optionView, "id");
		inventoryListView.setAdapter(sAdap);
	}

	/**
	 * Search.
	 */
	private void search() {
		String search = searchBox.getText().toString();

		if (search.equals("/demo")) {
			testAddProduct();
			searchBox.setText("");
		} else if (search.equals("/clear")) {
			DatabaseExecutor.getInstance().dropAllData();
			searchBox.setText("");
		}
		else if (search.equals("")) {
			showList(productCatalog.getAllProduct());
		} else {
			List<Product> result = productCatalog.searchProduct(search);
			showList(result);
			if (result.isEmpty()) {

			}
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent intent) {
		IntentResult scanningResult = IntentIntegrator.parseActivityResult(
				requestCode, resultCode, intent);

		if (scanningResult != null) {
			String scanContent = scanningResult.getContents();
			searchBox.setText(scanContent);
		} else {
			Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.fail),
					Toast.LENGTH_SHORT).show();
		}
	}

	/**
	 * Test adding product
	 */
	protected void testAddProduct() {
		Demo.testProduct(getActivity());
		Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.success),
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * Show popup.
	 * @param anchorView
	 */
	public void showPopup(View anchorView) {
		AddProductDialogFragment newFragment = new AddProductDialogFragment(InventoryFragment.this);
		newFragment.show(getFragmentManager(), "");
	}

	@Override
	public void update() {
		search();
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	public static void updateGridViewHeight(GridView myGridView, Integer add_height) {
		ListAdapter myListAdapter = myGridView.getAdapter();
		if (myListAdapter == null) {
			return;
		}
		// get listview height
		int totalHeight = 0;
		int adapterCount = myListAdapter.getCount();
		for (int size = 0; size < adapterCount; size++) {
			View listItem = myListAdapter.getView(size, null, myGridView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}
		// Change Height of ListView
		ViewGroup.LayoutParams params = myGridView.getLayoutParams();
		params.height = (totalHeight
				+ (myGridView.getVerticalSpacing() * (adapterCount))) + add_height;
		myGridView.setLayoutParams(params);
	}
}