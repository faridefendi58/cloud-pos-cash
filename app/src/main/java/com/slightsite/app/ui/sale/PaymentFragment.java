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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.balysv.materialripple.MaterialRippleLayout;
import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.NoDaoSetException;

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
    private EditText ed_nominal_bni;
    private EditText edc_card_type;
    private EditText edc_card_number;
    private EditText edc_nominal;
    private TextView total_order;
    private ImageButton bt_toggle_mandiri;
    private ImageButton bt_toggle_bca;
    private ImageButton bt_toggle_bni;

    private Register register;

    private Checkout c_data;
    private HashMap< String, String> banks = new HashMap< String, String>();
    private HashMap< String, String> edcs = new HashMap< String, String>();

    public PaymentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
        root = inflater.inflate(R.layout.fragment_payment, container, false);

        initView();
        initAction();
        initDefaultValue();
        if (!c_data.getTransferBank().isEmpty()) {
            Log.e(getTag(), "transfer bank data on default value :"+ c_data.getTransferBank().toString());
        }
        Log.e(getTag(), "shipping data on payment frag : "+ c_data.getShipping().toMap().toString());

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
        ed_nominal_bni = (EditText) root.findViewById(R.id.nominal_bni);
        edc_card_type = (EditText) root.findViewById(R.id.edc_card_type);
        edc_card_number  = (EditText) root.findViewById(R.id.edc_card_number);
        edc_nominal  = (EditText) root.findViewById(R.id.edc_nominal);
        total_order  = (TextView) root.findViewById(R.id.total_order);
        bt_toggle_mandiri = (ImageButton) root.findViewById(R.id.bt_toggle_mandiri);
        bt_toggle_bca = (ImageButton) root.findViewById(R.id.bt_toggle_bca);
        bt_toggle_bni = (ImageButton) root.findViewById(R.id.bt_toggle_bni);
    }

    private void initAction() {
        c_data = ((CheckoutActivity) getActivity()).getCheckoutData();
        if (!c_data.getTransferBank().isEmpty()) {
            banks = c_data.getTransferBank();
            Log.e(getTag(), "transfer bank data on init :"+ banks.toString());
        }

        if (!c_data.getEdc().isEmpty()) {
            edcs = c_data.getEdc();
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
        setTextChangeListener(ed_nominal_bni, "nominal_bni");
        setTextChangeListener(edc_card_number, "card_number");
        setTextChangeListener(edc_nominal, "nominal_edc");

        total_order.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()));

        bt_toggle_mandiri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout lyt_expand_text = (LinearLayout) v.getRootView().findViewById(R.id.lyt_expand_mandiri);
                if (lyt_expand_text.getVisibility() == View.VISIBLE) {
                    lyt_expand_text.setVisibility(View.GONE);
                    bt_toggle_mandiri.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black));
                } else {
                    lyt_expand_text.setVisibility(View.VISIBLE);
                    bt_toggle_mandiri.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_black));
                }
            }
        });

        bt_toggle_bca.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout lyt_expand_text = (LinearLayout) v.getRootView().findViewById(R.id.lyt_expand_bca);
                if (lyt_expand_text.getVisibility() == View.VISIBLE) {
                    lyt_expand_text.setVisibility(View.GONE);
                    bt_toggle_bca.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black));
                } else {
                    lyt_expand_text.setVisibility(View.VISIBLE);
                    bt_toggle_bca.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_black));
                }
            }
        });

        bt_toggle_bni.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout lyt_expand_text = (LinearLayout) v.getRootView().findViewById(R.id.lyt_expand_bni);
                if (lyt_expand_text.getVisibility() == View.VISIBLE) {
                    lyt_expand_text.setVisibility(View.GONE);
                    bt_toggle_bni.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black));
                } else {
                    lyt_expand_text.setVisibility(View.VISIBLE);
                    bt_toggle_bni.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_black));
                }
            }
        });
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
                        Log.e(getTag(), "Banks : " + banks.toString());
                        c_data.setTransferBank(banks);
                    } else if (setType == "nominal_bni") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, s.toString());
                        c_data.setTransferBank(banks);
                    } else if (setType == "card_number") {
                        c_data.setCardNumber(s.toString());
                    } else if (setType == "nominal_edc") {
                        if (edcs.containsKey(setType)) {
                            edcs.remove(setType);
                        }
                        edcs.put(setType, s.toString());
                        c_data.setEdc(edcs);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initDefaultValue() {
        try {
            if (c_data.getCashReceive().length() > 0) {
                if (c_data.getCashReceive().contains(".")) {
                    c_data.setCashReceive(c_data.getCashReceive().split("\\.")[0]);
                }
                cash_receive.setText(c_data.getCashReceive());
            }

            if (c_data.getUseTransferBank()) {
                switch_tranfer.setChecked(true);
                transfer_bank_container.setVisibility(View.VISIBLE);
                if (!c_data.getTransferBank().isEmpty()) {
                    if (banks.containsKey("nominal_mandiri")) {
                        ed_nominal_mandiri.setText(c_data.getTransferBank().get("nominal_mandiri"));
                        if (c_data.getTransferBank().get("nominal_mandiri").contains(".")) {
                            ed_nominal_mandiri.setText(c_data.getTransferBank().get("nominal_mandiri").split("\\.")[0]);
                        }

                        LinearLayout lyt_expand_bca = (LinearLayout) root.findViewById(R.id.lyt_expand_bca);
                        lyt_expand_bca.setVisibility(View.VISIBLE);
                    }
                    if (banks.containsKey("nominal_bca")) {
                        ed_nominal_bca.setText(c_data.getTransferBank().get("nominal_bca"));
                        if (c_data.getTransferBank().get("nominal_bca").contains(".")) {
                            ed_nominal_bca.setText(c_data.getTransferBank().get("nominal_bca").split("\\.")[0]);
                        }
                        LinearLayout lyt_expand_mandiri = (LinearLayout) root.findViewById(R.id.lyt_expand_mandiri);
                        lyt_expand_mandiri.setVisibility(View.VISIBLE);
                    }
                    if (banks.containsKey("nominal_bni")) {
                        ed_nominal_bni.setText(c_data.getTransferBank().get("nominal_bni"));
                        if (c_data.getTransferBank().get("nominal_bni").contains(".")) {
                            ed_nominal_bni.setText(c_data.getTransferBank().get("nominal_bni").split("\\.")[0]);
                        }
                        LinearLayout lyt_expand_bni = (LinearLayout) root.findViewById(R.id.lyt_expand_bni);
                        lyt_expand_bni.setVisibility(View.VISIBLE);
                    }
                }
            }

            if (c_data.getUseEdc()) {
                switch_edc.setChecked(true);
                edc_container.setVisibility(View.VISIBLE);
                if (!c_data.getEdc().isEmpty()) {
                    if (c_data.getCardNumber().length() > 1) {
                        edc_card_number.setText(c_data.getCardNumber());
                    }
                    if (edcs.containsKey("nominal_edc")) {
                        edc_nominal.setText(c_data.getEdc().get("nominal_edc"));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean toggleArrow(View view) {
        if (view.getRotation() == 0) {
            view.animate().setDuration(200).rotation(180);
            return true;
        } else {
            view.animate().setDuration(200).rotation(0);
            return false;
        }
    }
}
