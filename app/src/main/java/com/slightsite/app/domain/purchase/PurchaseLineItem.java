package com.slightsite.app.domain.purchase;

import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.Product;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class PurchaseLineItem implements Serializable {
    private final Product product;
    private int quantity;
    private int id;
    private double unitPriceAtSale;
    private double unitGrosirPrice;
    public static boolean multi_level_price = true;

    /**
     * Static value for UNDEFINED ID.
     */
    public static final int UNDEFINED = -1;

    /**
     * Constructs a new PurchaseLineItem.
     * @param product product of this PurchaseLineItem.
     * @param quantity product quantity of this PurchaseLineItem.
     */
    public PurchaseLineItem(Product product, int quantity, int quantity_total) {
        this(UNDEFINED, product, quantity, product.getUnitPrice(), quantity_total);
    }

    /**
     * Constructs a new PurchaseLineItem.
     * @param id ID of this PurchaseLineItem, This value should be assigned from database.
     * @param product product of this PurchaseLineItem.
     * @param quantity product quantity of this PurchaseLineItem.
     * @param unitPriceAtSale unit price at sale time. default is price from ProductCatalog.
     */
    public PurchaseLineItem(int id, Product product, int quantity,
                    double unitPriceAtSale, int quantity_total) {
        if (id > 0) {
            this.id = id;
        } else {
            this.id = product.getId();
        }
        product.setImageBitmap(null);
        this.product = product;
        this.quantity = quantity;
        this.unitPriceAtSale = unitPriceAtSale;

        try {
            if (quantity_total >= 5 && multi_level_price) {
                unitGrosirPrice = product.getUnitPriceByQuantity(product.getId(), quantity_total);
                if (unitGrosirPrice > 0) {
                    this.unitPriceAtSale = unitGrosirPrice;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns product in this PurchaseLineItem.
     * @return product in this PurchaseLineItem.
     */
    public Product getProduct() {
        return product;
    }

    /**
     * Return quantity of product in this PurchaseLineItem.
     * @return quantity of product in this PurchaseLineItem.
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * Sets quantity of product in this PurchaseLineItem.
     * @param quantity quantity of product in this PurchaseLineItem.
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * Adds quantity of product in this PurchaseLineItem.
     * @param amount amount for add in quantity.
     */
    public void addQuantity(int amount) {
        this.quantity += amount;
    }

    /**
     * Returns total price of this PurchaseLineItem.
     * @return total price of this PurchaseLineItem.
     */
    public double getTotalPriceAtSale() {
        return unitPriceAtSale * quantity;
    }

    /**
     * Returns the description of this PurchaseLineItem in Map format.
     * @return the description of this PurchaseLineItem in Map format.
     */
    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put("name", product.getName());
        map.put("quantity", quantity + "");
        map.put("price", CurrencyController.getInstance().moneyFormat(getPriceAtSale()) + "");
        map.put("unit_price", getPriceAtSale() + "");
        map.put("base_price", product.getUnitPrice() + "");
        map.put("id", product.getId() + "");
        map.put("barcode", product.getBarcode() + "");
        return map;

    }

    /**
     * Returns id of this PurchaseLineItem.
     * @return id of this PurchaseLineItem.
     */
    public int getId() {
        return id;
    }

    /**
     * Sets id of this PurchaseLineItem.
     * @param id of this PurchaseLineItem.
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Sets price product of this PurchaseLineItem.
     * @param unitPriceAtSale price product of this PurchaseLineItem.
     */
    public void setUnitPriceAtSale(double unitPriceAtSale) {
        this.unitPriceAtSale = unitPriceAtSale;
    }

    /**
     * Returns price product of this PurchaseLineItem.
     * @return unitPriceAtSale price product of this PurchaseLineItem.
     */
    public Double getPriceAtSale() {
        return unitPriceAtSale;
    }

    /**
     * Determines whether two objects are equal or not.
     * @return true if Object is a PurchaseLineItem with same ID ; otherwise false.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null)
            return false;
        if (!(object instanceof PurchaseLineItem))
            return false;
        PurchaseLineItem lineItem = (PurchaseLineItem) object;
        return lineItem.getId() == this.getId();
    }
}
