package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.printer.PrinterActivity;

/**
 * A dialog shows the total change and confirmation for Sale.
 *
 */
@SuppressLint("ValidFragment")
public class EndPaymentFragmentDialog extends DialogFragment  {

	private Button doneButton;
	private Button printButton;
	private TextView chg;
	private Register regis;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;
	
	/**
	 * End this UI.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public EndPaymentFragmentDialog(UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		try {
			regis = Register.getInstance();
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.dialog_paymentsuccession, container,false);
		String strtext=getArguments().getString("edttext");
		chg = (TextView) v.findViewById(R.id.changeTxt);
		chg.setText(CurrencyController.getInstance().moneyFormat(Double.parseDouble(strtext)));
		doneButton = (Button) v.findViewById(R.id.doneButton);
		doneButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				end();
			}
		});
		printButton = (Button) v.findViewById(R.id.printButton);
		printButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				print();
			}
		});
		
		return v;
	}
	
	/**
	 * End
	 */
	private void end(){
		regis.endSale(DateTimeStrategy.getCurrentTime());
		saleFragment.update();
		try {
			reportFragment.update();
		} catch (Exception e) {
			e.printStackTrace();
		}

		this.dismiss();
	}

	private void print(){
		end();
		Intent newActivity = new Intent(getActivity(), PrinterActivity.class);
		//newActivity.putExtra("id", productId);
		getActivity().startActivity(newActivity);
	}
}
