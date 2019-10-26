package com.slightsite.app.domain.sale;

import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class Fee implements Serializable {
    private String date;
    private int total_invoice;
    private Double total_fee;

    public static final int UNDEFINED = -1;

    public Fee(String date, int total_invoice, Double total_fee) {
        this.date = date;
        this.total_invoice = total_invoice;
        this.total_fee = total_fee;
    }

    /**
     * Returns the description of this LineItem in Map format.
     * @return the description of this LineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("date", DateTimeStrategy.parseDate(date, "dd MMM, E"));
        map.put("total_invoice", total_invoice+"");
        map.put("total_fee", CurrencyController.getInstance().moneyFormat(total_fee));

        return map;

    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }
}
