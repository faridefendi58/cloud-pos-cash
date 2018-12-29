package com.slightsite.app.ui.sale;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.ui.component.UpdatableFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

/**
 * A dialog for input a money for sale.
 *
 */
@SuppressLint("ValidFragment")
public class PaymentFragmentDialog extends DialogFragment {
	
	private TextView totalPrice;
	private EditText input;
	private EditText input_trf;
	private Button clearButton;
	private Button confirmButton;
	private String strtext;
	private UpdatableFragment saleFragment;
	private UpdatableFragment reportFragment;

	private RadioButton radio_ninjas;
	private RadioButton radio_transfer;

	/**
	 * Construct a new PaymentFragmentDialog.
	 * @param saleFragment
	 * @param reportFragment
	 */
	public PaymentFragmentDialog(UpdatableFragment saleFragment, UpdatableFragment reportFragment) {
		super();
		this.saleFragment = saleFragment;
		this.reportFragment = reportFragment;
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		View v = inflater.inflate(R.layout.dialog_payment, container,false);
		strtext = getArguments().getString("edttext");
		String currency = CurrencyController.getInstance().getCurrency();
		if (currency.equals("idr")) {
			strtext = strtext.replaceAll("\\.", "");
		}

		input = (EditText) v.findViewById(R.id.dialog_saleInput);
		input_trf = (EditText) v.findViewById(R.id.dialog_transferInput);
		totalPrice = (TextView) v.findViewById(R.id.payment_total);
		radio_ninjas = (RadioButton) v.findViewById(R.id.radio_ninjas);
		radio_transfer = (RadioButton) v.findViewById(R.id.radio_transfer);

		totalPrice.setText(CurrencyController.getInstance().moneyFormat(Double.parseDouble(strtext)));
		input_trf.setText(strtext);

		clearButton = (Button) v.findViewById(R.id.clearButton);
		clearButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				end();
			}
		});
		
		confirmButton = (Button) v.findViewById(R.id.confirmButton);
		confirmButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				
				String inputString = input.getText().toString();
				String inputTrfString = input_trf.getText().toString();

				if (inputString.equals("") && radio_ninjas.isChecked()) {
					Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.please_input_all), Toast.LENGTH_SHORT).show();
					return;
				}
				double a = Double.parseDouble(strtext);
				double b = a;
				if (radio_ninjas.isChecked())
					b = Double.parseDouble(inputString);
				if (b < a) {
					Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.need_money) + " " + (b - a), Toast.LENGTH_SHORT).show();
				} else {
					Bundle bundle = new Bundle();
					bundle.putString("edttext", b - a + "");
					EndPaymentFragmentDialog newFragment = new EndPaymentFragmentDialog(
							saleFragment, reportFragment);
					newFragment.setArguments(bundle);
					newFragment.show(getFragmentManager(), "");
					end();
				}

			}
		});

		radio_ninjas.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				input.setVisibility(View.VISIBLE);
				input_trf.setVisibility(View.GONE);
				radio_transfer.setChecked(false);
			}
		});

		radio_transfer.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				input.setVisibility(View.GONE);
				input_trf.setVisibility(View.VISIBLE);
				radio_ninjas.setChecked(false);
			}
		});

		return v;
	}

	/**
	 * End.
	 */
	private void end() {
		this.dismiss();
		
	}
	

}
