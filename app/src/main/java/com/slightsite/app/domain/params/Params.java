package com.slightsite.app.domain.params;

import java.util.HashMap;
import java.util.Map;

import com.slightsite.app.techicalservices.NoDaoSetException;

/**
 * @author Params
 */
public class Params {

    private int id;
    private String name;
    private String value;
    private String type;
    private String description;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;


    public Params(int id, String name, String value, String type, String description) {
        this.id = id;
        this.name = name;
        this.value = value;
        this.type = type;
        this.description = description;
    }

    public Params(String name, String value, String type, String description) {
        this(UNDEFINED_ID, name, value, type, description);
    }

    /**
     * Sets name of this params.
     * @param name name of this params.
     */
    public void setName(String name) {
        this.name = name;
    }


    public void setValue(String value) {
        this.value = value;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns id of this params.
     * @return id of this params.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns name of this params.
     * @return name of this params.
     */
    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Returns the description of this Customer in Map format.
     * @return the description of this Customer in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("name", name);
        map.put("value", value);
        map.put("type", type);
        map.put("description", description);

        return map;

    }
}


