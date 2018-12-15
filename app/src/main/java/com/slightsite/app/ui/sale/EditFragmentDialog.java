package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.component.UpdatableFragment;

/**
 * A dialog for edit a LineItem of sale,
 * overriding price or set the quantity.
 *
 */
@SuppressLint("ValidFragment")
public class EditFragmentDialog extends DialogFragment {
	private Register register;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;
	private EditText quantityBox;
	private EditText priceBox;
	private Button comfirmButton;
	private String saleId;
	private String position;
	private LineItem lineItem;
	private Button removeButton;
	
	/**
	 * Construct a new  EditFragmentDialog.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public EditFragmentDialog(UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.dialog_saleedit, container, false);
		try {
			register = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}
		
		quantityBox = (EditText) v.findViewById(R.id.quantityBox);
		priceBox = (EditText) v.findViewById(R.id.priceBox);
		comfirmButton = (Button) v.findViewById(R.id.confirmButton);
		removeButton = (Button) v.findViewById(R.id.removeButton);
		
		saleId = getArguments().getString("sale_id");
		position = getArguments().getString("position");

		lineItem = register.getCurrentSale().getLineItemAt(Integer.parseInt(position));
		quantityBox.setText(lineItem.getQuantity()+"");
		priceBox.setText(lineItem.getProduct().getUnitPrice()+"");

		removeButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View arg0) {
				Log.d("remove", "id=" + lineItem.getId());
				register.removeItem(lineItem);
				end();
				//show success message
				Toast toast = Toast.makeText(
						getActivity().getApplicationContext(),
						getResources().getString(R.string.message_success_delete),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 8, 8);
				toast.show();
			}
		});

		comfirmButton.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View view) {
				Double grosir_price = lineItem.getProduct().getUnitPriceByQuantity(lineItem.getProduct().getId(), Integer.parseInt(quantityBox.getText().toString()));
				if (grosir_price > 0) {
					priceBox.setText(lineItem.getProduct().getUnitPrice()+"");
					register.updateItem(
							Integer.parseInt(saleId),
							lineItem,
							Integer.parseInt(quantityBox.getText().toString()),
							grosir_price
					);
				} else {
					register.updateItem(
							Integer.parseInt(saleId),
							lineItem,
							Integer.parseInt(quantityBox.getText().toString()),
							Double.parseDouble(priceBox.getText().toString())
					);
				}
				
				end();
				//show success message
				Toast toast = Toast.makeText(
						getActivity().getApplicationContext(),
						getResources().getString(R.string.message_success_update),
						Toast.LENGTH_SHORT);
				toast.setGravity(Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL, 8, 8);
				toast.show();
			}
			
		});
		return v;
	}
	
	/**
	 * End.
	 */
	private void end(){
		saleFragment.update();
		try {
			reportFragment.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.dismiss();
	}
	
	
}
