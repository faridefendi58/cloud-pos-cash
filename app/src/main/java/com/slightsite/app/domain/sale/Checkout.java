package com.slightsite.app.domain.sale;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.slightsite.app.domain.customer.Customer;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String wallet_tokopedia = "0";

    public Checkout() {}

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
        } else {
            // ini hanya default value
            if (Integer.parseInt(wallet_tokopedia) == 0) {
                PaymentItem cash = new PaymentItem("cash_receive", 0.0);
                payment_items.add(cash);
            }
        }
        if (transfer_bank != null) {
            for (String key : transfer_bank.keySet()) {
                if (transfer_bank.get(key) != null && transfer_bank.get(key).length() > 0) {
                    PaymentItem pi = new PaymentItem(key, Double.parseDouble(transfer_bank.get(key)));
                    payment_items.add(pi);
                }
            }
        }
        if (edc != null) {
            for (String key : edc.keySet()) {
                if (edc.get(key) != null && edc.get(key).length() > 0) {
                    PaymentItem pi = new PaymentItem(key, Double.parseDouble(edc.get(key)));
                    payment_items.add(pi);
                }
            }
        }
        if (Integer.parseInt(wallet_tokopedia) > 0) {
            if (wallet_tokopedia != null && wallet_tokopedia.length() > 0) {
                PaymentItem _wallet = new PaymentItem("wallet_tokopedia", Double.parseDouble(wallet_tokopedia));
                payment_items.add(_wallet);
            }
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
        types.put("wallet_tokopedia", "Wallet Tokopedia");

        return types;
    }

    public Double getTotalPaymentReceived() {
        Double payment_received = 0.00;
        if (Integer.parseInt(cash_receive) > 0) {
            payment_received = payment_received + Double.parseDouble(cash_receive);
        }

        if (transfer_bank != null) {
            for (String key : transfer_bank.keySet()) {
                if (transfer_bank.get(key) != null && transfer_bank.get(key).length() > 0) {
                    payment_received = payment_received + Double.parseDouble(transfer_bank.get(key));
                }
            }
        }

        if (edc != null) {
            for (String key : edc.keySet()) {
                if (edc.get(key) != null && edc.get(key).length() > 0) {
                    payment_received = payment_received + Double.parseDouble(edc.get(key));
                }
            }
        }

        if (wallet_tokopedia != null && wallet_tokopedia != "0") {
            payment_received = Double.parseDouble(wallet_tokopedia);
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

    public void setWalletTokopedia(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.wallet_tokopedia = nominal;
        if (!cash_receive.equals("0")) {
            cash_receive = "0";
        }
    }

    public String getWalletTokopedia() {
        return wallet_tokopedia;
    }
}
