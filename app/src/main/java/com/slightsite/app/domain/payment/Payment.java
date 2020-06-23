package com.slightsite.app.domain.payment;

import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.ui.sale.CheckoutActivity;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Payment implements Serializable {
    private int id;
    private int sale_id;
    private String payment_channel;
    private Double amount;
    private String transfer_receipt;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;


    public Payment(int id, Integer sale_id, String payment_channel, Double amount) {
        this.id = id;
        this.sale_id = sale_id;
        this.payment_channel = payment_channel;
        this.amount = amount;
    }

    public Payment(Integer sale_id, String payment_channel, Double amount) {
        this(UNDEFINED_ID, sale_id, payment_channel, amount);
    }

    public void setSaleId(Integer sale_id) {
        this.sale_id = sale_id;
    }


    public void setPaymentChannel(String payment_channel) {
        this.payment_channel = payment_channel;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    /**
     * Returns id of this params.
     * @return id of this params.
     */
    public int getId() {
        return id;
    }

    public Integer getSaleId() {
        return sale_id;
    }

    public String getPaymentChannel() {
        return payment_channel;
    }

    public Double getAmount() {
        return amount;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("sale_id", sale_id + "");
        map.put("payment_channel", payment_channel);
        map.put("amount", amount + "");
        String formated_amount = CurrencyController.getInstance().moneyFormat(amount);
        map.put("formated_amount", formated_amount);
        String formated_payment_channel = payment_channel;
        if (payment_channel != null) {
            if (payment_channel.contains("refund_")) {
                String[] split = payment_channel.split("refund_");
                if (split.length > 0) {
                    formated_payment_channel = "Refund "+ getPaymentTypes().get(split[1]);
                    map.put("formated_amount", "- "+ formated_amount);
                }
            } else if (payment_channel == "change_due") {
                formated_payment_channel = "Uang Kembali (Tunai)";
                map.put("formated_amount", "- "+ formated_amount);
            } else {
                formated_payment_channel = getPaymentTypes().get(payment_channel);
            }
        }
        map.put("formated_payment_channel", formated_payment_channel);
        if (transfer_receipt != null) {
            map.put("transfer_receipt", transfer_receipt);
        }

        return map;
    }

    public HashMap<String,String> getPaymentTypes() {
        HashMap<String,String> types = new HashMap<String, String>();
        types.put("cash_receive", AppController.getInstance().getString(R.string.payment_cash));
        types.put("nominal_mandiri", AppController.getInstance().getString(R.string.payment_mandiri));
        types.put("nominal_bca", AppController.getInstance().getString(R.string.payment_bca));
        types.put("nominal_bri", AppController.getInstance().getString(R.string.payment_bri));
        types.put("nominal_edc", AppController.getInstance().getString(R.string.payment_edc));
        types.put("wallet_tokopedia", AppController.getInstance().getString(R.string.payment_wallet_tokopedia));
        types.put("wallet_gofood", AppController.getInstance().getString(R.string.payment_wallet_gofood));
        types.put("wallet_grabfood", AppController.getInstance().getString(R.string.payment_wallet_grab_food));

        return types;
    }

    public void setTransferReceipt(String _receipt) {
        this.transfer_receipt = _receipt;
    }
}
