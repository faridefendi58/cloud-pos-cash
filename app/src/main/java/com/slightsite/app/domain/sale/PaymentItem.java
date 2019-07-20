package com.slightsite.app.domain.sale;

import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.Product;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentItem implements Serializable {
    private String title;
    private double nominal;

    public static final int UNDEFINED = -1;

    public PaymentItem(String title, double nominal) {
        this(UNDEFINED, title, nominal);
    }

    public PaymentItem(int id, String title, double nominal) {
        this.title = title;
        this.nominal = nominal;
    }

    public String getTitle() {
        return title;
    }

    public Double getNominal() {
        return nominal;
    }

    /**
     * Returns the description of this LineItem in Map format.
     * @return the description of this LineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("title", title);
        map.put("nominal", CurrencyController.getInstance().moneyFormat(nominal) + "");

        return map;

    }


    public void setTitle(String title) {
        this.title = title;
    }

    public void setNominal(Double nominal) {
        this.nominal = nominal;
    }
}
