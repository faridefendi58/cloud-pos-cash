package com.slightsite.app.domain.sale;

import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class FeeOn implements Serializable {
    private String date;
    private String invoice_number;
    private Double total_fee;
    private Double total_revenue;

    public static final int UNDEFINED = -1;

    public FeeOn(String date, String invoice_number, Double total_fee, Double total_revenue) {
        this.date = date;
        this.invoice_number = invoice_number;
        this.total_fee = total_fee;
        this.total_revenue = total_revenue;
    }

    /**
     * Returns the description of this LineItem in Map format.
     * @return the description of this LineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("date", DateTimeStrategy.parseDate(date, "dd MMM, E"));
        map.put("invoice_number", invoice_number);
        map.put("total_fee", CurrencyController.getInstance().moneyFormat(total_fee));
        map.put("total_revenue", CurrencyController.getInstance().moneyFormat(total_revenue));

        return map;

    }

    public void setDate(String date) {
        this.date = date;
    }
    public String getDate() {
        return date;
    }
}

