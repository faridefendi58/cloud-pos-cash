package com.slightsite.app.domain.sale;

import android.util.Log;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Checkout implements Serializable {
    private HashMap< String, String> transfer_bank;
    private HashMap< String, String> edc;
    private Customer customer;
    private String cash_receive = "0";
    private String card_number = "0";
    private Boolean use_transfer_bank = false;
    private Boolean use_edc = false;
    private List<PaymentItem> payment_items;
    private Shipping shipping;
    private int discount;

    public HashMap< String, String> getTransferBank() {
        if (transfer_bank == null)
            transfer_bank = new HashMap< String, String>();;
        return transfer_bank;
    }

    public void setTransferBank(HashMap< String, String> transfer_bank) {
        Log.e(getClass().getSimpleName(), "transfer_bank : "+ transfer_bank.toString());
        this.transfer_bank = transfer_bank;
    }

    public void setEdc(HashMap< String, String> edc_params) {
        Log.e(getClass().getSimpleName(), "edc : "+ edc.toString());
        this.edc = edc_params;
    }

    public HashMap< String, String> getEdc() {
        if (edc == null)
            edc = new HashMap< String, String>();;
        return edc;
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
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
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

    public void setUseEdc(Boolean useedc) {
        this.use_edc = useedc;
    }

    public Boolean getUseEdc() {
        return use_edc;
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
        for (String key : edc.keySet()) {
            PaymentItem pi = new PaymentItem(key, Double.parseDouble(edc.get(key)));
            payment_items.add(pi);
        }
        return payment_items;
    }

    public HashMap<String,String> getPaymentTypes() {
        HashMap<String,String> types = new HashMap<String, String>();
        types.put("cash_receive", "Cash Payment");
        types.put("nominal_mandiri", "Transfer Bank Mandiri");
        types.put("nominal_bca", "Transfer Bank BCA");
        types.put("nominal_bri", "Transfer Bank BRI");
        types.put("nominal_edc", "EDC Payment");

        return types;
    }

    public Double getTotalPaymentReceived() {
        Double payment_received = 0.00;
        if (Integer.parseInt(cash_receive) > 0) {
            payment_received = payment_received + Double.parseDouble(cash_receive);
        }

        for (String key : transfer_bank.keySet()) {
            payment_received = payment_received + Double.parseDouble(transfer_bank.get(key));
        }

        for (String key : edc.keySet()) {
            payment_received = payment_received + Double.parseDouble(edc.get(key));
        }

        return payment_received;
    }

    public void setCardNumber(String numb) {
        this.card_number = numb;
    }

    public String getCardNumber() {
        return card_number;
    }

    public void setShipping(Shipping shipping) {
        this.shipping = shipping;
    }

    public Shipping getShipping() {
        if (shipping == null)
            shipping = new Shipping(1, 0, "", "", 0);
        return shipping;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    public int getDiscount() {
        return discount;
    }
}
