package com.slightsite.app.ui.sale;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.LanguageController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.inventory.InventoryFragment;

/**
 * UI for Sale operation.
 *
 */
@SuppressLint("ValidFragment")
public class SaleFragment extends UpdatableFragment {
    
	private Register register;
	private ArrayList<Map<String, String>> saleList;
	private RecyclerView saleListView;
	private com.github.clans.fab.FloatingActionButton clearButton;
	private TextView totalPrice;
	private com.github.clans.fab.FloatingActionButton endButton;
	private UpdatableFragment reportFragment;
	private Resources res;
	private TextView customer_name_box;
	private com.github.clans.fab.FloatingActionMenu fButtonMenu;

	private AdapterListCart mAdapter;
	private LinearLayout total_container;
	private ViewPager viewPager;
	private MainActivity main;

	/**
	 * Construct a new SaleFragment.
	 * @param
	 */
	public SaleFragment(UpdatableFragment reportFragment) {
		super();
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		
		try {
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		View view = inflater.inflate(R.layout.layout_cart, container, false);
		setHasOptionsMenu(true);
		
		res = getResources();
		saleListView = (RecyclerView) view.findViewById(R.id.sale_List);
		saleListView.setLayoutManager(new LinearLayoutManager(getContext()));
		saleListView.setHasFixedSize(true);
		saleListView.setNestedScrollingEnabled(false);
		totalPrice = (TextView) view.findViewById(R.id.totalPrice);
		total_container = (LinearLayout) view.findViewById(R.id.total_container);

		/*clearButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.clearButton);
		endButton = (com.github.clans.fab.FloatingActionButton) view.findViewById(R.id.endButton);
		customer_name_box = (TextView) view.findViewById(R.id.customer_name_box);

		fButtonMenu = (com.github.clans.fab.FloatingActionMenu) view.findViewById(R.id.menu);*/

		main = (MainActivity) getActivity();
		viewPager = main.getViewPager();

		initUI();
		return view;
	}

	/**
	 * Initiate this UI.
	 */
	private void initUI() {
		/*saleListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				showEditPopup(arg1,arg2);
			}
		});*/

		/*clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				ViewPager viewPager = ((MainActivity) getActivity()).getViewPager();
				viewPager.setCurrentItem(1);
			}
		});

		endButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(register.hasSale()){
					fButtonMenu.close(true);
					showPopup(v);
				} else {
					Toast.makeText(getActivity().getBaseContext() , res.getString(R.string.hint_empty_sale), Toast.LENGTH_SHORT).show();
				}
			}
		});
		
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				fButtonMenu.close(true);
				if (!register.hasSale() || register.getCurrentSale().getAllLineItem().isEmpty()) {
					Toast.makeText(getActivity().getBaseContext() , res.getString(R.string.hint_empty_sale), Toast.LENGTH_SHORT).show();
				} else {
					showConfirmClearDialog();
				}
			} 
		});*/
	}
	
	/**
	 * Show list
	 * @param list
	 */
	private void showList(List<LineItem> list) {
		saleList = new ArrayList<Map<String, String>>();
		for(LineItem line : list) {
			saleList.add(line.toMap());
		}

		/*SimpleAdapter sAdap;
		sAdap = new SimpleAdapter(getActivity().getBaseContext(), saleList,
				R.layout.listview_cart, new String[]{"name","quantity","price"}, new int[] {R.id.name,R.id.quantity,R.id.price});
		saleListView.setAdapter(sAdap);*/
		//set data and list adapter
		mAdapter = new AdapterListCart(getContext(), list, register, totalPrice);
		saleListView.setAdapter(mAdapter);

		mAdapter.setOnItemClickListener(new AdapterListCart.OnItemClickListener() {
			@Override
			public void onItemClick(View view, LineItem obj, int position) {
				showEditPopup(view, position);
			}
		});

		if (register.getTotal() > 0) {
			total_container.setVisibility(View.VISIBLE);
		}
	}

	/**
	 * Try parsing String to double.
	 * @param value
	 * @return true if can parse to double.
	 */
	public boolean tryParseDouble(String value)  
	{  
		try  {  
			Double.parseDouble(value);  
			return true;  
		} catch(NumberFormatException e) {  
			return false;  
		}  
	}
	
	/**
	 * Show edit popup.
	 * @param anchorView
	 * @param position
	 */
	public void showEditPopup(View anchorView,int position){
		Bundle bundle = new Bundle();
		bundle.putString("position",position+"");
		bundle.putString("sale_id",register.getCurrentSale().getId()+"");
		bundle.putString("product_id",register.getCurrentSale().getLineItemAt(position).getProduct().getId()+"");
		
		EditFragmentDialog newFragment = new EditFragmentDialog(SaleFragment.this, reportFragment);
		newFragment.setArguments(bundle);
		newFragment.show(getFragmentManager(), "");
		
	}

	/**
	 * Show popup
	 * @param anchorView
	 */
	public void showPopup(View anchorView) {
		Bundle bundle = new Bundle();
		bundle.putString("edttext", totalPrice.getText().toString());
		PaymentFragmentDialog newFragment = new PaymentFragmentDialog(SaleFragment.this, reportFragment);
		newFragment.setArguments(bundle);
		newFragment.show(getFragmentManager(), "");
	}

	@Override
	public void update() {
		if(register.hasSale()){
			showList(register.getCurrentSale().getAllLineItem());

			totalPrice.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()) + "");
		}
		else{
			showList(new ArrayList<LineItem>());
			totalPrice.setText("0.00");
			total_container.setVisibility(View.GONE);
			viewPager.setCurrentItem(0);
			try {
				LinearLayout bottom_cart_container = ( LinearLayout) viewPager.findViewById(R.id.bottom_cart_container);
				bottom_cart_container.setVisibility(View.GONE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		update();
	}
	
	/**
	 * Show confirm or clear dialog.
	 */
	private void showConfirmClearDialog() {
		AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
		dialog.setTitle(res.getString(R.string.dialog_clear_sale));
		dialog.setPositiveButton(res.getString(R.string.no), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

			}
		});

		dialog.setNegativeButton(res.getString(R.string.clear), new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				register.cancleSale();
				update();
			}
		});

		dialog.show();
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.menu_delete, menu);
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
}
