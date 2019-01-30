package com.slightsite.app.domain.payment;

import java.util.HashMap;
import java.util.Map;

public class Payment {
    private int id;
    private int sale_id;
    private String payment_channel;
    private Double amount;

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

        return map;
    }
}
