package com.slightsite.app.domain.sale;

import android.graphics.Bitmap;
import android.location.Location;
import android.util.Log;

import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
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
    private Boolean use_cash = false;
    private List<PaymentItem> payment_items;
    private Shipping shipping;
    private int discount = 0;
    private int ongkir = 0;
    private Boolean ongkir_cash_to_driver = false;
    private String wallet_tokopedia = "0";
    private String wallet_gofood = "0";
    private String wallet_grabfood = "0";
    private Boolean use_gofood = false;
    private Boolean use_grabfood = false;
    private String total_gofood_invoice = "0";
    private String total_grabfood_invoice = "0";
    private String gofood_discount = "0";
    private String grabfood_discount = "0";
    private Double change_due = 0.0;

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

    public void removeCashReceive() {
        this.cash_receive = "0";
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
        if (cash_receive.length() > 0 && Integer.parseInt(cash_receive) > 0) {
            PaymentItem cash = new PaymentItem("cash_receive", Double.parseDouble(cash_receive));
            payment_items.add(cash);
        } else {
            // ini hanya default value, tp ini bermasalah jika nilai cash_receive diubah jadi 0 dr form
            /*if (Integer.parseInt(wallet_tokopedia) == 0) {
                PaymentItem cash = new PaymentItem("cash_receive", 0.0);
                payment_items.add(cash);
            }*/
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

        if (Integer.parseInt(wallet_gofood) > 0) {
            if (wallet_gofood != null && wallet_gofood.length() > 0) {
                PaymentItem _wallet = new PaymentItem("wallet_gofood", Double.parseDouble(wallet_gofood));
                payment_items.add(_wallet);
            }
        }

        if (Integer.parseInt(wallet_grabfood) > 0) {
            if (wallet_grabfood != null && wallet_grabfood.length() > 0) {
                PaymentItem _wallet = new PaymentItem("wallet_grabfood", Double.parseDouble(wallet_grabfood));
                payment_items.add(_wallet);
            }
        }

        // last check if null payment_items
        if (payment_items.size() == 0) {
            PaymentItem cash = new PaymentItem("cash_receive", 0.0);
            payment_items.add(cash);
        }

        return payment_items;
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

    public Double getTotalPaymentReceived() {
        Double payment_received = 0.00;
        if (cash_receive.length() > 0 && Integer.parseInt(cash_receive) > 0) {
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

        if (wallet_gofood != null && wallet_gofood != "0") {
            payment_received = payment_received + Double.parseDouble(wallet_gofood);
        }

        if (wallet_grabfood != null && wallet_grabfood != "0") {
            payment_received = payment_received + Double.parseDouble(wallet_grabfood);
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

    public void setOngkir(int ongkir) {
        this.ongkir = ongkir;
    }

    public int getOngkir() {
        return ongkir;
    }

    public void setOngkirCashToDriver(Boolean cash_to_driver) {
        this.ongkir_cash_to_driver = cash_to_driver;
    }

    public Boolean getOngkirCashToDriver() {
        return ongkir_cash_to_driver;
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

    public void setWalletGoFood(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.wallet_gofood = nominal;
        if (nominal != "0") {
            this.use_gofood = true;
            this.use_grabfood = false;
        }
        if (!cash_receive.equals("0")) {
            cash_receive = "0";
        }
    }

    public String getWalletGoFood() {
        return wallet_gofood;
    }

    public void setWalletGrabFood(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.wallet_grabfood = nominal;

        if (nominal != "0") {
            this.use_grabfood = true;
            this.use_gofood = false;
        }
        if (!cash_receive.equals("0")) {
            cash_receive = "0";
        }
    }

    public String getWalletGrabFood() {
        return wallet_grabfood;
    }

    public Boolean getUseGoFood() {
        return use_gofood;
    }

    public void setUseGoFood(Boolean _use) {
        this.use_gofood = _use;
    }

    public Boolean getUseGrabFood() {
        return use_grabfood;
    }

    public void setUseGrabFood(Boolean _use) {
        this.use_grabfood = _use;
    }

    public void setTotalGoFoodInvoice(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.total_gofood_invoice = nominal;
    }

    public String getTotalGoFoodInvoice() {
        return total_gofood_invoice;
    }

    public void setTotalGrabFoodInvoice(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.total_grabfood_invoice = nominal;
    }

    public String getTotalGrabFoodInvoice() {
        return total_grabfood_invoice;
    }

    public void setGoFoodDiscount(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.gofood_discount = nominal;
    }

    public String getGofoodDiscount() {
        return gofood_discount;
    }

    public void setGrabFoodDiscount(String nominal) {
        if (nominal.contains(".")) {
            nominal = nominal.split("\\.")[0];
        }
        this.grabfood_discount = nominal;
    }

    public String getGrabfoodDiscount() {
        return grabfood_discount;
    }

    public void setChangeDue(Double change_due) {
        this.change_due = change_due;
    }

    public Double getChangeDue() {
        return change_due;
    }

    public void setUseCash(Boolean usecash) {
        this.use_cash = usecash;
    }

    public Boolean getUseCash() {
        return use_cash;
    }
}
