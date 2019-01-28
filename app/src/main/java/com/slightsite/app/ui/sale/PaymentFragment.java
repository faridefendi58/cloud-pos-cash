package com.slightsite.app.ui.sale;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.slightsite.app.R;
import com.slightsite.app.domain.sale.Checkout;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.List;

public class PaymentFragment extends Fragment {

    private LinearLayout transfer_bank_container;
    private LinearLayout edc_container;
    private SwitchCompat switch_tranfer;
    private SwitchCompat switch_edc;
    private View root;
    private EditText cash_receive;
    private EditText ed_nominal_mandiri;
    private EditText ed_nominal_bca;
    private EditText edc_card_type;
    private EditText edc_card_number;

    private Checkout c_data;
    private HashMap< String, String> banks = new HashMap< String, String>();

    public PaymentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root = inflater.inflate(R.layout.fragment_payment, container, false);

        initView();
        initAction();
        initDefaultValue();
        if (!c_data.getTransferBank().isEmpty()) {
            Log.e(getTag(), "transfer bank data on default value :"+ c_data.getTransferBank().toString());
        }

        return root;
    }

    private void initView() {
        transfer_bank_container = (LinearLayout) root.findViewById(R.id.transfer_bank_container);
        edc_container = (LinearLayout) root.findViewById(R.id.edc_container);
        switch_tranfer = (SwitchCompat) root.findViewById(R.id.switch_tranfer);
        switch_edc = (SwitchCompat) root.findViewById(R.id.switch_edc);
        cash_receive = (EditText) root.findViewById(R.id.cash_receive);
        ed_nominal_mandiri = (EditText) root.findViewById(R.id.nominal_mandiri);
        ed_nominal_bca = (EditText) root.findViewById(R.id.nominal_bca);
        edc_card_type = (EditText) root.findViewById(R.id.edc_card_type);
        edc_card_number  = (EditText) root.findViewById(R.id.edc_card_number);
    }

    private void initAction() {
        c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        if (!c_data.getTransferBank().isEmpty()) {
            banks = c_data.getTransferBank();
            Log.e(getTag(), "transfer bank data on init :"+ banks.toString());
        }

        switch_tranfer.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    transfer_bank_container.setVisibility(View.VISIBLE);
                } else {
                    transfer_bank_container.setVisibility(View.GONE);
                }
                ((CheckoutActivity) getActivity()).hideKeyboard(getActivity());
                c_data.setUseTransferBank(isChecked);
            }
        });

        switch_edc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edc_container.setVisibility(View.VISIBLE);
                } else {
                    edc_container.setVisibility(View.GONE);
                }
                ((CheckoutActivity) getActivity()).hideKeyboard(getActivity());
                c_data.setUseEdc(isChecked);
            }
        });

        setTextChangeListener(cash_receive, "cashReceive");
        setTextChangeListener(ed_nominal_mandiri, "nominal_mandiri");
        setTextChangeListener(ed_nominal_bca, "nominal_bca");
    }

    private void setTextChangeListener(EditText etv, final String setType) {
        etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    if (setType == "cashReceive") {
                        c_data.setCashReceive(s.toString());
                    } else if (setType == "nominal_mandiri") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, s.toString());
                        c_data.setTransferBank(banks);
                    } else if (setType == "nominal_bca") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, s.toString());
                        Log.e(getTag(), "Banks : "+ banks.toString());
                        c_data.setTransferBank(banks);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initDefaultValue() {
        try {
            if (Integer.parseInt(c_data.getCashReceive()) > 0) {
                cash_receive.setText(c_data.getCashReceive());
            }

            if (c_data.getUseTransferBank()) {
                switch_tranfer.setChecked(true);
                transfer_bank_container.setVisibility(View.VISIBLE);
                if (!c_data.getTransferBank().isEmpty()) {
                    if (banks.containsKey("nominal_mandiri")) {
                        ed_nominal_mandiri.setText(c_data.getTransferBank().get("nominal_mandiri"));
                    }
                    if (banks.containsKey("nominal_bca")) {
                        ed_nominal_bca.setText(c_data.getTransferBank().get("nominal_bca"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
