package com.slightsite.app.domain.inventory;

import java.util.HashMap;
import java.util.Map;

import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.techicalservices.inventory.InventoryDao;

/**
 * Lot or bunch of product that import to inventory.
 *
 * @author Farid Efendi
 *
 */
public class ProductDiscount {

    private int id;
    private String dateAdded;
    private int quantity;
    private int quantity_max;
    private Product product;
    private double unitCost;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED_ID = -1;

    /**
     * Constructs a new ProductDiscount.
     * @param id ID of the ProductDiscount, This value should be assigned from database.
     * @param dateAdded date and time of adding this lot.
     * @param quantity quantity of product.
     * @param product a product of this lot.
     * @param unitCost cost (of buy) of each unit in this lot.
     */
    public ProductDiscount(int id, String dateAdded, int quantity, int quantity_max, Product product, double unitCost) {
        this.id = id;
        this.dateAdded = dateAdded;
        this.quantity = quantity;
        this.quantity_max = quantity_max;
        this.product = product;
        this.unitCost = unitCost;
    }

    /**
     * Returns date added of this ProductDiscount.
     * @return date added of this ProductDiscount.
     */
    public String getDateAdded() {
        return dateAdded;
    }

    /**
     * Returns quantity of this ProductDiscount.
     * @return quantity of this ProductDiscount.
     */
    public int getQuantity() {
        return quantity;
    }
    
    public int getQuantityMax() {
        return quantity_max;
    }

    /**
     * Returns cost of this ProductDiscount.
     * @return cost of this ProductDiscount.
     */
    public double unitCost() {
        return unitCost;
    }

    /**
     * Returns id of this ProductDiscount.
     * @return id of this ProductDiscount.
     */
    public int getId() {
        return id;
    }

    /**
     * Returns product in this ProductDiscount.
     * @return product in this ProductDiscount.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Returns the description of this ProductDiscount in Map format.
     * @return the description of this ProductDiscount in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("id", id + "");
        map.put("dateAdded", DateTimeStrategy.format(dateAdded));
        map.put("quantity", quantity + "");
        map.put("quantity_max", quantity_max + "");
        map.put("productName", product.getName());
        map.put("cost", unitCost + "");
        return map;
    }
}
