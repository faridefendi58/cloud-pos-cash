package com.slightsite.app.domain.sale;

import android.util.Log;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Checkout implements Serializable {
    private HashMap< String, String> transfer_bank;
    private Customer customer;
    private String cash_receive = "0";
    private Boolean use_transfer_bank = false;
    private Boolean use_edc = false;
    private List<PaymentItem> payment_items;

    public HashMap< String, String> getTransferBank() {
        if (transfer_bank == null)
            transfer_bank = new HashMap< String, String>();;
        return transfer_bank;
    }

    public void setTransferBank(HashMap< String, String> transfer_bank) {
        Log.e(getClass().getSimpleName(), "transfer_bank : "+ transfer_bank.toString());
        this.transfer_bank = transfer_bank;
    }

    public Customer getCustomer() {
        if (customer == null)
            customer = new Customer("Walk In", "email@email.com", "0000", "na", 1);
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setCashReceive(String nominal) {
        this.cash_receive = nominal;
    }

    public String getCashReceive() {
        return cash_receive;
    }

    public boolean hasTransferBank() {
        if (this.getTransferBank().size() == 0)
            return false;
        return true;
    }

    public boolean hasCustomer() {
        if (this.customer == null)
            return false;
        return true;
    }

    public void setUseTransferBank(Boolean use_bank) {
        this.use_transfer_bank = use_bank;
    }

    public Boolean getUseTransferBank() {
        return use_transfer_bank;
    }

    public void setUseEdc(Boolean use_bank) {
        this.use_transfer_bank = use_bank;
    }

    public Boolean getUseEdc() {
        return use_transfer_bank;
    }

    public List<PaymentItem> getPaymentItems() {
        payment_items =  new ArrayList<PaymentItem>();
        if (Integer.parseInt(cash_receive) > 0) {
            PaymentItem cash = new PaymentItem("cash_receive", Double.parseDouble(cash_receive));
            payment_items.add(cash);
        }
        for (String key : transfer_bank.keySet()) {
            PaymentItem pi = new PaymentItem(key, Double.parseDouble(transfer_bank.get(key)));
            payment_items.add(pi);
        }
        return payment_items;
    }

    public HashMap<String,String> getPaymentTypes() {
        HashMap<String,String> types = new HashMap<String, String>();
        types.put("cash_receive", "Cash Payment");
        types.put("nominal_mandiri", "Transfer Bank Mandiri");
        types.put("nominal_bca", "Transfer Bank BCA");

        return types;
    }
}
