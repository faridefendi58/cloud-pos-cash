package com.slightsite.app.domain.sale;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class ItemCounter implements Serializable {

    private String name;
    private int counter;

    public ItemCounter(String name, int counter) {
        this.name = name;
        this.counter = counter;
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", name);
        map.put("counter", ""+ counter);

        return map;
    }
}
