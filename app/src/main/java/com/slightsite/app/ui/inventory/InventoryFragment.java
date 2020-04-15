package com.slightsite.app.ui.inventory;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.github.clans.fab.FloatingActionButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentIntegratorSupportV4;
import com.google.zxing.integration.android.IntentResult;
import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.DatabaseExecutor;
import com.slightsite.app.techicalservices.Demo;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.ButtonAdapter;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.sale.AdapterListProduct;
import com.slightsite.app.ui.sale.ChangeWarehouseDialogFragment;

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
	private FloatingActionButton syncProductButton;
	private TextView cart_total;
	private LinearLayout bottom_cart_container;
	private MaterialRippleLayout lyt_next;
	private LinearLayout no_product_container;
	private EditText wh_options;

	private ViewPager viewPager;
	private Register register;
	private MainActivity main;

	private UpdatableFragment saleFragment;
	private Resources res;

	private Map<Integer, Integer> stacks = new HashMap<Integer, Integer>();

	private View fragment_view;
	private List<Warehouses> warehousesList;
	private Map<Integer, String> allowed_warehouses = new HashMap<Integer, String>();
	private Menu menu;
	private SwipeRefreshLayout swipeRefresh;

	/**
	 * Construct a new InventoryFragment.
	 */
	/*public InventoryFragment(UpdatableFragment saleFragment) {
		super();
		this.saleFragment = saleFragment;
	}*/

	public InventoryFragment() {
		super();
	}

	public void setSaleFragment(UpdatableFragment saleFragment) {
		this.saleFragment = saleFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		try {
			productCatalog = Inventory.getInstance().getProductCatalog();
			register = Register.getInstance();
			warehousesList = ((MainActivity)getActivity()).getWarehouseList();
			allowed_warehouses = ((MainActivity)getActivity()).getAllowedWarehouseList();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.layout_inventory, container, false);
		setHasOptionsMenu(true);

		fragment_view = view;

		res = getResources();
		inventoryListView = (GridView) view.findViewById(R.id.productListView);
		addProductButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.addProductButton);
		scanButton = (Button) view.findViewById(R.id.scanButton);
		searchBox = (EditText) view.findViewById(R.id.searchBox);
		syncProductButton = (FloatingActionButton) view.findViewById(R.id.syncProductButton);
		cart_total = (TextView) view.findViewById(R.id.cart_total);
		bottom_cart_container = (LinearLayout) view.findViewById(R.id.bottom_cart_container);
		lyt_next = (MaterialRippleLayout) view.findViewById(R.id.lyt_next);
		no_product_container = (LinearLayout) view.findViewById(R.id.no_product_container);
		wh_options = (EditText) view.findViewById(R.id.wh_options);
		swipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefresh);

		main = (MainActivity) getActivity();
		viewPager = main.getViewPager();

		initUI();
		updateCart();

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
			public void onItemClick(AdapterView<?> myAdapter, final View myView, int position, long mylng) {
				LinearLayout add_qty_container = (LinearLayout) myView.findViewById(R.id.add_qty_container);

				int id = Integer.parseInt(inventoryList.get(position).get("id").toString());
				if (!add_qty_container.isShown()) {

					register.addItem(productCatalog.getProductById(id), 1);
					saleFragment.update();
					viewPager.setCurrentItem(0);
					updateCart();

					add_qty_container.setVisibility(View.VISIBLE);
					myView.findViewById(R.id.optionView).setVisibility(View.GONE);
					TextView quantity = myView.findViewById(R.id.quantity);
					quantity.setText(""+ 1);
				}
			}     
		});

		scanButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				IntentIntegratorSupportV4 scanIntegrator = new IntentIntegratorSupportV4(InventoryFragment.this);
				scanIntegrator.initiateScan();
			}
		});

		syncProductButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent newActivity = new Intent(getActivity(),
						ProductServerActivity.class);
				startActivity(newActivity);
			}
		});

		lyt_next.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				viewPager.setCurrentItem(1);
			}
		});

		wh_options.setText(((MainActivity)getActivity()).getWarehouseName());
		wh_options.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if ((allowed_warehouses != null) && allowed_warehouses.size() > 0) {
					showOutletOptionsDialog(v);
				} else {
					Toast.makeText(getActivity().getBaseContext(), res.getString(R.string.message_no_product),
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
				update();
				swipeRefresh.setRefreshing(false);
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

		if (inventoryList.size() == 0) {
			no_product_container.setVisibility(View.VISIBLE);
		} else {
			no_product_container.setVisibility(View.GONE);
		}

		// clearing the stack on update
		this.stacks = new HashMap<Integer, Integer>();

		AdapterListProduct pAdap = new AdapterListProduct(main, list, R.layout.listview_inventory, InventoryFragment.this);
		pAdap.notifyDataSetChanged();
		inventoryListView.setAdapter(pAdap);
	}

	/**
	 * Search.
	 */
	private void search() {
		if (searchBox == null) {
			return;
		}

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
		updateCart();
	}

	@Override
	public void onResume() {
		super.onResume();
		update();
	}

	private Boolean show_animation = true;

	public void updateCart() {
		Double tot_cart = register.getTotal();
		if (tot_cart > 0) {
			// show hide clear menu
			try {
				if (menu != null) {
					menu.findItem(R.id.nav_delete).setVisible(true);
				}
			} catch (Exception e){e.printStackTrace();}

			Integer tot_item_cart = register.getCurrentSale().getAllLineItem().size();
			String cart_total_txt = getResources().getString(R.string.label_sub_total)+" " +
					"" + CurrencyController.getInstance().moneyFormat(tot_cart) +" " +
					getResources().getString(R.string.label_of) +" "+ tot_item_cart + " " +
					getResources().getString(R.string.label_items);
			cart_total.setText(cart_total_txt);

			bottom_cart_container.getLayoutParams().height = LinearLayout.LayoutParams.WRAP_CONTENT;
			bottom_cart_container.requestLayout();

			bottom_cart_container.setVisibility(View.VISIBLE);

			if (show_animation) {
				TranslateAnimation animate = new TranslateAnimation(
						0,                 // fromXDelta
						0,                 // toXDelta
						bottom_cart_container.getHeight() + 100,  // fromYDelta
						0);                // toYDelta
				animate.setDuration(500);
				animate.setFillAfter(true);
				bottom_cart_container.startAnimation(animate);

				show_animation = false;
			}
		} else {
			// show hide clear menu
			try {
				if (menu != null && menu.findItem(R.id.nav_delete) != null) {
					menu.findItem(R.id.nav_delete).setVisible(false);
				}
			} catch (Exception e){e.printStackTrace();}

			TranslateAnimation animate = new TranslateAnimation(
					0,                 // fromXDelta
					0,                 // toXDelta
					0,  // fromYDelta
					bottom_cart_container.getHeight() + 100);                // toYDelta
			animate.setDuration(500);
			animate.setFillAfter(true);
			bottom_cart_container.startAnimation(animate);

			new android.os.Handler().postDelayed(
					new Runnable() {
						public void run() {
							bottom_cart_container.getLayoutParams().height = 0;
							bottom_cart_container.requestLayout();
							bottom_cart_container.setVisibility(View.INVISIBLE);
						}
					},
					600);

			cart_total.setText("Empty cart");

			show_animation = true;
		}
	}

	public void addToCart(Product p) {
		register.addItem(p, 1);
		LineItem lineItem = register.getCurrentSale().getLineItemByProductId(p.getId());
		if (!lineItem.getProduct().equals(null)) {
			Integer tot_qty_in_cart = register.getCurrentSale().getOrders();
			Double grosir_price = lineItem.getProduct().getUnitPriceByQuantity(p.getId(), tot_qty_in_cart);
			if (grosir_price > 0) {
				register.updateItem(
						register.getCurrentSale().getId(),
						lineItem,
						1,
						grosir_price
				);
			}
		}
		stacks.put(p.getId(), 1);
		saleFragment.update();
		viewPager.setCurrentItem(0);
		updateCart();
	}

	public void addSubstractTheCart(Product p, int quantity) {
		if (quantity == 0) {
			// substract the cart
			try {
				LineItem lineItem = register.getCurrentSale().getLineItemByProductId(p.getId());
				if (lineItem != null && lineItem.getId() >= 0) {
					register.removeItem(lineItem);
					stacks.remove(p.getId());
				}
			} catch (Exception e) { e.printStackTrace();}
		} else {
			LineItem lineItem = register.getCurrentSale().getLineItemByProductId(p.getId());
			if (!lineItem.getProduct().equals(null)) {
				Integer tot_qty_in_cart = register.getCurrentSale().getOrders();
				Double grosir_price = lineItem.getProduct().getUnitPriceByQuantity(p.getId(), tot_qty_in_cart);
				if (grosir_price > 0) {
					register.updateItem(
							register.getCurrentSale().getId(),
							lineItem,
							quantity,
							grosir_price
					);
				} else {
					register.updateItem(
							register.getCurrentSale().getId(),
							lineItem,
							quantity,
							p.getUnitPrice()
					);
				}
				stacks.put(p.getId(), quantity);
			}
		}

		saleFragment.update();
		viewPager.setCurrentItem(0);
		updateCart();
	}

	public Map<Integer, Integer> getStacks() {
		for(LineItem line : register.getCurrentSale().getAllLineItem()){
			updateStack(line.getProduct().getId(), line.getQuantity());
		}

		return stacks;
	}

	public void updateStack(int product_id, int quantity) {
		if (quantity > 0) {
			stacks.put(product_id, quantity);
		} else {
			stacks.remove(product_id);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_delete, menu);
		this.menu = menu;
		if (stacks.size() == 0) {
			menu.findItem(R.id.nav_delete).setVisible(false);
		}
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.nav_delete :
				showConfirmClearDialog();
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	/**
	 * Show confirm or clear dialog.
	 */
	private void showConfirmClearDialog() {
		LayoutInflater inflater2 = this.getLayoutInflater();

		View titleView = inflater2.inflate(R.layout.dialog_custom_title, null);
		((TextView) titleView.findViewById(R.id.dialog_title)).setText(res.getString(R.string.title_clear_sale));
		if (register.hasSale() && register.getCurrentSale().size() > 0) {
			((TextView) titleView.findViewById(R.id.dialog_content)).setText(res.getString(R.string.dialog_clear_sale));
		} else {
			((TextView) titleView.findViewById(R.id.dialog_content)).setText(res.getString(R.string.message_clear_empty_sale));
		}

		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setCustomTitle(titleView);
		if (register.hasSale() && register.getCurrentSale().size() > 0) {
			dialog.setPositiveButton(res.getString(R.string.clear), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (!register.getCurrentSale().getStatus().equals("ENDED")) {
						register.cancleSale();
					} else {
						register.setCurrentSale(0);
					}
					update();
					try {
						((MainActivity) getActivity()).updateSaleFragment();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});

			dialog.setNegativeButton(res.getString(R.string.no), new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
		} else {
			dialog.setNegativeButton(res.getString(R.string.button_close), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

				}
			});
		}

		dialog.show();
	}

	public void changeWHPopup() {
		ChangeWarehouseDialogFragment newFragment = new ChangeWarehouseDialogFragment(InventoryFragment.this);
		newFragment.show(getFragmentManager(), "");
	}

	private String[] warehouses = new String[]{};
	private HashMap<String, String> warehouse_ids = new HashMap<String, String>();

	public void showOutletOptionsDialog(final View v) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
		int selected_wh = -1;
		String current_warehouse_name = ((MainActivity)getActivity()).getCurrentWarehouseName();
		Log.e(getTag(), "showOutletOptionsDialog current_warehouse_name : "+ current_warehouse_name);
		Log.e(getTag(), "showOutletOptionsDialog allowed_warehouses : "+ allowed_warehouses.toString());
		try {
			if (allowed_warehouses.size() > 0) {
				ArrayList<String> stringArrayList = new ArrayList<String>();
				for (Map.Entry<Integer, String> entry : allowed_warehouses.entrySet()) {
					stringArrayList.add(entry.getValue());
					warehouse_ids.put(entry.getValue(), entry.getKey() + "");
				}
				warehouses = stringArrayList.toArray(new String[stringArrayList.size()]);
			} else {
				if (warehousesList.size() > 0) {
					ArrayList<String> stringArrayList = new ArrayList<String>();
					for (Warehouses wh : warehousesList) {
						stringArrayList.add(wh.getTitle());
						warehouse_ids.put(wh.getTitle(), wh.getId() + "");
					}
					warehouses = stringArrayList.toArray(new String[stringArrayList.size()]);
				}
			}

			if (warehouses.length > 0) {
				selected_wh = Arrays.asList(warehouses).indexOf(current_warehouse_name);
			}

			builder.setSingleChoiceItems(warehouses, selected_wh, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dialogInterface.dismiss();
					String current_wh = ((MainActivity)getActivity()).getCurrentWarehouseName();
					if (warehouses[i].equals(current_wh)) {
						Toast.makeText(getContext(), "Please choose another branch!",
								Toast.LENGTH_LONG).show();
					} else {
						((EditText) v).setText(warehouses[i]);
						if (register.hasSale()) {
							try {
								if (!register.getCurrentSale().getStatus().equals("ENDED")) {
									register.cancleSale();
								} else {
									register.setCurrentSale(0);
								}
								update();
								((MainActivity) getActivity()).updateSaleFragment();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}

						// starting to do updating the data
						String wh_choosen = warehouses[i];
						String wh_choosen_id = warehouse_ids.get(wh_choosen);
						try {
							((MainActivity)getActivity()).changeOutletExecution(wh_choosen_id, wh_choosen, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}


				}
			});
			builder.show();
		} catch (Exception e){e.printStackTrace();}
	}
}