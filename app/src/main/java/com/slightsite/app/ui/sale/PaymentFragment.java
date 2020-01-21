package com.slightsite.app.ui.sale;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.util.ArrayMap;
import android.support.v7.widget.SwitchCompat;
import android.text.Editable;
import android.text.TextUtils;
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
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.sale.Checkout;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Shipping;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Tools;

import org.w3c.dom.Text;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PaymentFragment extends Fragment {

    private LinearLayout bank_edc_container;
    private LinearLayout transfer_bank_container;
    private LinearLayout edc_container;
    private LinearLayout normal_billing_info_container;
    private LinearLayout gofood_billing_info_container;
    private LinearLayout payment_gograbfood_container;
    private LinearLayout cash_container;
    private LinearLayout switch_cash_container;
    private SwitchCompat switch_tranfer;
    private SwitchCompat switch_edc;
    private SwitchCompat switch_cash;

    private View root;
    private EditText cash_receive;
    private EditText ed_nominal_mandiri;
    private EditText ed_nominal_bca;
    private EditText ed_nominal_bri;
    private EditText edc_card_type;
    private EditText edc_card_number;
    private EditText edc_nominal;
    private EditText gograbfood_total;
    private TextView label_gograbfood_total;
    private EditText gograbfood_discount;
    private TextView label_gograb_discount;
    private TextView gograbfood_grand_total;
    private TextView label_gograb_grand_total;
    private EditText nominal_gograbfood;
    private TextView total_order;
    private EditText total_discount;
    private TextView grand_total;
    private TextView billing_info_message;
    private TextView label_gograb_pay;
    private ImageButton bt_toggle_mandiri;
    private ImageButton bt_toggle_bca;
    private ImageButton bt_toggle_bri;

    private Register register;
    private ParamCatalog paramCatalog;

    private Checkout c_data;
    private HashMap< String, String> banks = new HashMap< String, String>();
    private HashMap< String, String> edcs = new HashMap< String, String>();

    private ArrayList<String> warehouse_items = new ArrayList<String>();

    public PaymentFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        try {
            register = Register.getInstance();
            paramCatalog = ParamService.getInstance().getParamCatalog();
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
        bank_edc_container = (LinearLayout) root.findViewById(R.id.bank_edc_container);
        transfer_bank_container = (LinearLayout) root.findViewById(R.id.transfer_bank_container);
        edc_container = (LinearLayout) root.findViewById(R.id.edc_container);
        normal_billing_info_container = (LinearLayout) root.findViewById(R.id.normal_billing_info_container);
        gofood_billing_info_container = (LinearLayout) root.findViewById(R.id.gofood_billing_info_container);
        payment_gograbfood_container = (LinearLayout) root.findViewById(R.id.payment_gograbfood_container);
        cash_container = (LinearLayout) root.findViewById(R.id.cash_container);
        switch_cash_container = (LinearLayout) root.findViewById(R.id.switch_cash_container);
        switch_tranfer = (SwitchCompat) root.findViewById(R.id.switch_tranfer);
        switch_edc = (SwitchCompat) root.findViewById(R.id.switch_edc);
        switch_cash = (SwitchCompat) root.findViewById(R.id.switch_cash);
        cash_receive = (EditText) root.findViewById(R.id.cash_receive);
        ed_nominal_mandiri = (EditText) root.findViewById(R.id.nominal_mandiri);
        ed_nominal_bca = (EditText) root.findViewById(R.id.nominal_bca);
        ed_nominal_bri = (EditText) root.findViewById(R.id.nominal_bri);
        edc_card_type = (EditText) root.findViewById(R.id.edc_card_type);
        edc_card_number  = (EditText) root.findViewById(R.id.edc_card_number);
        edc_nominal  = (EditText) root.findViewById(R.id.edc_nominal);
        gograbfood_total  = (EditText) root.findViewById(R.id.gograbfood_total);
        label_gograbfood_total  = (TextView) root.findViewById(R.id.label_gograbfood_total);
        gograbfood_discount  = (EditText) root.findViewById(R.id.gograbfood_discount);
        label_gograb_discount  = (TextView) root.findViewById(R.id.label_gograb_discount);
        gograbfood_grand_total  = (TextView) root.findViewById(R.id.gograbfood_grand_total);
        label_gograb_grand_total  = (TextView) root.findViewById(R.id.label_gograb_grand_total);
        nominal_gograbfood  = (EditText) root.findViewById(R.id.nominal_gograbfood);
        total_order  = (TextView) root.findViewById(R.id.total_order);
        total_discount  = (EditText) root.findViewById(R.id.total_discount);
        grand_total  = (TextView) root.findViewById(R.id.grand_total);
        billing_info_message  = (TextView) root.findViewById(R.id.billing_info_message);
        label_gograb_pay  = (TextView) root.findViewById(R.id.label_gograb_pay);
        bt_toggle_mandiri = (ImageButton) root.findViewById(R.id.bt_toggle_mandiri);
        bt_toggle_bca = (ImageButton) root.findViewById(R.id.bt_toggle_bca);
        bt_toggle_bri = (ImageButton) root.findViewById(R.id.bt_toggle_bri);
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

        if (c_data.getShipping().getWarehouseId() == 0) {
            Shipping shp = c_data.getShipping();
            CheckoutActivity checkoutActivity = ((CheckoutActivity)getActivity());
            shp.setWarehouseId(checkoutActivity.getCurrentWarehouseId());
            shp.setWarehouseName(checkoutActivity.getCurrentWarehouseName());
            checkoutActivity.setShipping(shp, c_data);
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
        setTextChangeListener(ed_nominal_bri, "nominal_bri");
        setTextChangeListener(edc_card_number, "card_number");
        setTextChangeListener(edc_nominal, "nominal_edc");
        setTextChangeListener(gograbfood_total, "gograbfood_total");
        setTextChangeListener(gograbfood_discount, "gograbfood_discount");
        setTextChangeListener(nominal_gograbfood, "nominal_gograbfood");

        String tot_order = CurrencyController.getInstance().moneyFormat(register.getTotal());
        total_order.setText(tot_order);
        grand_total.setText(tot_order);

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

        bt_toggle_bri.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LinearLayout lyt_expand_text = (LinearLayout) v.getRootView().findViewById(R.id.lyt_expand_bri);
                if (lyt_expand_text.getVisibility() == View.VISIBLE) {
                    lyt_expand_text.setVisibility(View.GONE);
                    bt_toggle_bri.setImageDrawable(getResources().getDrawable(R.drawable.ic_add_black));
                } else {
                    lyt_expand_text.setVisibility(View.VISIBLE);
                    bt_toggle_bri.setImageDrawable(getResources().getDrawable(R.drawable.ic_remove_black));
                }
            }
        });

        total_discount.addTextChangedListener(new TextWatcher(){
            private String current_discount_val;
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {
                    try {
                        String cleanString = s.toString().replaceAll("[.]", "");

                        int discount_val = Integer.parseInt(cleanString);
                        String tot_order = CurrencyController.getInstance().moneyFormat(register.getTotal());
                        tot_order = tot_order.replace(".", "");
                        int grand_total_now = Integer.parseInt(tot_order);
                        int grand_total_current = 0;
                        if (discount_val < grand_total_now) {
                            grand_total_current = grand_total_now - discount_val;
                        }

                        // save the discount value
                        current_discount_val = discount_val+"";
                        c_data.setDiscount(discount_val);
                        //s.setText(CurrencyController.getInstance().moneyFormat(discount_val));
                        grand_total.setText(CurrencyController.getInstance().moneyFormat(Double.parseDouble(grand_total_current+"")));
                    } catch (Exception e){e.printStackTrace();}
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void onTextChanged(CharSequence s, int start, int before, int count){
                if(!s.toString().equals(current_discount_val)){
                    total_discount.removeTextChangedListener(this);

                    String cleanString = s.toString().replaceAll("[.]", "");

                    double parsed = Double.parseDouble(cleanString);
                    String formatted = CurrencyController.getInstance().moneyFormat(parsed);

                    current_discount_val = formatted;
                    total_discount.setText(formatted);
                    total_discount.setSelection(formatted.length());
                    total_discount.addTextChangedListener(this);
                }
            }
        });

        switch_cash.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    cash_container.setVisibility(View.VISIBLE);
                } else {
                    cash_container.setVisibility(View.GONE);
                }
                ((CheckoutActivity) getActivity()).hideKeyboard(getActivity());
                c_data.setUseCash(isChecked);
            }
        });
    }

    private void setTextChangeListener(final EditText etv, final String setType) {
        etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private String current_val;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current_val)){
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (cleanString.length() >= 3) {
                        etv.removeTextChangedListener(this);
                        String formatted = "";
                        if (setType.equals("card_number")) {
                            formatted = Tools.cardFormat(cleanString);
                            current_val = formatted;
                        } else {
                            double parsed = Double.parseDouble(cleanString);
                            formatted = CurrencyController.getInstance().moneyFormat(parsed);

                            current_val = formatted;
                        }
                        etv.setText(formatted);
                        etv.setSelection(formatted.length());
                        etv.addTextChangedListener(this);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (setType == "cashReceive") {
                        if (cleanString.length() > 0) {
                            c_data.setCashReceive(cleanString);
                        } else {
                            if (s.toString().length() == 0) {
                                c_data.removeCashReceive();
                            }
                        }
                    } else if (setType == "nominal_mandiri") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, cleanString);
                        c_data.setTransferBank(banks);
                    } else if (setType == "nominal_bca") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, cleanString);
                        c_data.setTransferBank(banks);
                    } else if (setType == "nominal_bri") {
                        if (banks.containsKey(setType)) {
                            banks.remove(setType);
                        }
                        banks.put(setType, cleanString);
                        c_data.setTransferBank(banks);
                    } else if (setType == "card_number") {
                        c_data.setCardNumber(s.toString());
                    } else if (setType == "nominal_edc") {
                        if (edcs.containsKey(setType)) {
                            edcs.remove(setType);
                        }
                        edcs.put(setType, cleanString);
                        c_data.setEdc(edcs);
                    } else if (setType == "nominal_gograbfood") {
                        if (cleanString.length() > 0) {
                            if (c_data.getUseGoFood()) {
                                c_data.setWalletGoFood(cleanString);
                            } else if (c_data.getUseGrabFood()) {
                                c_data.setWalletGrabFood(cleanString);
                            }
                        }
                    } else if (setType == "gograbfood_total") {
                        if (cleanString.length() > 0) {
                            if (c_data.getUseGoFood()) {
                                c_data.setTotalGoFoodInvoice(cleanString);
                            } else if (c_data.getUseGrabFood()) {
                                c_data.setTotalGrabFoodInvoice(cleanString);
                            }
                            try {
                                if (cleanString.length()>0 && !cleanString.equals("0")) {
                                    String gt_formatted = CurrencyController.getInstance().moneyFormat(Double.parseDouble(cleanString));
                                    gograbfood_grand_total.setText(gt_formatted);
                                    nominal_gograbfood.setText(gt_formatted);
                                }
                            } catch (Exception e){}
                        }
                    } else if (setType == "gograbfood_discount") {
                        if (cleanString.length() > 0) {
                            String ggt = "0";
                            if (c_data.getUseGoFood()) {
                                c_data.setGoFoodDiscount(cleanString);
                                ggt = c_data.getTotalGoFoodInvoice();
                            } else if (c_data.getUseGrabFood()) {
                                c_data.setGrabFoodDiscount(cleanString);
                                ggt = c_data.getTotalGrabFoodInvoice();
                            }

                            if (ggt.length() > 0 && !ggt.equals("0")) {
                                try {
                                    c_data.setDiscount(Integer.parseInt(cleanString));
                                    Double gt = Double.parseDouble(ggt) - Double.parseDouble(cleanString);
                                    String gt_formatted = CurrencyController.getInstance().moneyFormat(gt);
                                    gograbfood_grand_total.setText(gt_formatted);
                                    nominal_gograbfood.setText(gt_formatted);
                                } catch (Exception e){}
                            }
                        }
                    }

                    if (setType == "card_number") {
                        current_val = s.toString();
                    } else {
                        if (cleanString.length() >= 3) {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = CurrencyController.getInstance().moneyFormat(parsed);
                            current_val = formatted;
                        } else {
                            current_val = s.toString();
                        }
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
                if (!c_data.getCashReceive().equals("0")) {
                    cash_receive.setText(c_data.getCashReceive());
                }
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

                        LinearLayout lyt_expand_mandiri = (LinearLayout) root.findViewById(R.id.lyt_expand_mandiri);
                        lyt_expand_mandiri.setVisibility(View.VISIBLE);
                    }
                    if (banks.containsKey("nominal_bca")) {
                        ed_nominal_bca.setText(c_data.getTransferBank().get("nominal_bca"));
                        if (c_data.getTransferBank().get("nominal_bca").contains(".")) {
                            ed_nominal_bca.setText(c_data.getTransferBank().get("nominal_bca").split("\\.")[0]);
                        }

                        LinearLayout lyt_expand_bca = (LinearLayout) root.findViewById(R.id.lyt_expand_bca);
                        lyt_expand_bca.setVisibility(View.VISIBLE);
                    }
                    if (banks.containsKey("nominal_bri")) {
                        ed_nominal_bri.setText(c_data.getTransferBank().get("nominal_bri"));
                        if (c_data.getTransferBank().get("nominal_bri").contains(".")) {
                            ed_nominal_bri.setText(c_data.getTransferBank().get("nominal_bri").split("\\.")[0]);
                        }
                        LinearLayout lyt_expand_bri = (LinearLayout) root.findViewById(R.id.lyt_expand_bri);
                        lyt_expand_bri.setVisibility(View.VISIBLE);
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
                        if (c_data.getEdc().get("nominal_edc").contains(".")) {
                            edc_nominal.setText(c_data.getEdc().get("nominal_edc").split("\\.")[0]);
                        }
                    }
                }
            }

            if (c_data.getDiscount() > 0) {
                total_discount.setText(c_data.getDiscount()+"");
                String tot_order = CurrencyController.getInstance().moneyFormat(register.getTotal());
                tot_order = tot_order.replace(".", "");
                int grand_total_now = Integer.parseInt(tot_order);
                int grand_total_current = 0;
                if (c_data.getDiscount() < grand_total_now) {
                    grand_total_current = grand_total_now - c_data.getDiscount();
                }
                grand_total.setText(CurrencyController.getInstance().moneyFormat(Double.parseDouble(grand_total_current+"")));
            }

            if (c_data.getUseGoFood() || c_data.getUseGrabFood()) {
                if (c_data.getUseGoFood()) {
                    billing_info_message.setText(getResources().getString(R.string.message_gofood_billing_info));
                    label_gograb_pay.setText(getResources().getString(R.string.label_gopay));
                }
                if (c_data.getUseGrabFood()) {
                    billing_info_message.setText(getResources().getString(R.string.message_grabfood_billing_info));
                    label_gograb_pay.setText(getResources().getString(R.string.label_grabpay));
                }
                normal_billing_info_container.setVisibility(View.GONE);
                gofood_billing_info_container.setVisibility(View.VISIBLE);
                payment_gograbfood_container.setVisibility(View.VISIBLE);
                String gograbfood = "0";
                String total_gograbfood = "0";
                if (c_data.getUseGoFood()) {
                    gograbfood = c_data.getWalletGoFood();
                    total_gograbfood = c_data.getTotalGoFoodInvoice();
                    label_gograb_grand_total.setText(getResources().getString(R.string.total)+ " GoFood");
                    label_gograbfood_total.setText(getResources().getString(R.string.label_sub_total)+ " GoFood");
                } else if (c_data.getUseGrabFood()) {
                    gograbfood = c_data.getWalletGrabFood();
                    total_gograbfood = c_data.getTotalGrabFoodInvoice();
                    label_gograb_grand_total.setText(getResources().getString(R.string.total)+ " GrabFood");
                    label_gograbfood_total.setText(getResources().getString(R.string.label_sub_total)+ " GrabFood");
                }

                //nominal_gograbfood.setHint(gograbfood);
                if (!total_gograbfood.equals("0")) {
                    gograbfood_total.setText(total_gograbfood);
                }

                if (c_data.getDiscount() > 0) {
                    String _disc = CurrencyController.getInstance().moneyFormat(c_data.getDiscount());
                    gograbfood_discount.setText(_disc);
                }

                // hide bank and edc payment
                bank_edc_container.setVisibility(View.GONE);
                label_gograb_discount.setText(getResources().getString(R.string.label_merchant_discount));
                // hide cash payment and add switch button
                switch_cash_container.setVisibility(View.VISIBLE);
                cash_container.setVisibility(View.GONE);
                Log.e("CUK", "c_data.getCashReceive() : "+ c_data.getCashReceive());
                if (c_data.getUseCash()) {
                    cash_container.setVisibility(View.VISIBLE);
                    switch_cash.setChecked(true);
                }
            } else {
                bank_edc_container.setVisibility(View.VISIBLE);
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
