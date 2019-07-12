package com.slightsite.app.domain.inventory;

import java.util.HashMap;
import java.util.Map;

import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;

/**
 * Product or item represents the real product in store.
 *
 *
 */
public class Product {

	private int id;
	private String name;
	private String barcode;
	private double unitPrice;
	private int priority = 0;
	private String image;
	
	/**
	 * Static value for UNDEFINED ID.
	 */
	public static final int UNDEFINED_ID = -1;

	/**
	 * Constructs a new Product.
	 * @param id ID of the product, This value should be assigned from database.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 */
	public Product(int id, String name, String barcode, double salePrice) {
		this.id = id;
		this.name = name;
		this.barcode = barcode;
		this.unitPrice = salePrice;
	}
	
	/**
	 * Constructs a new Product.
	 * @param name name of this product.
	 * @param barcode barcode (any standard format) of this product.
	 * @param salePrice price for using when doing sale.
	 */
	public Product(String name, String barcode, double salePrice) {
		this(UNDEFINED_ID, name, barcode, salePrice);
	}

	/**
	 * Returns name of this product.
	 * @return name of this product.
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Sets name of this product.
	 * @param name name of this product.
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Sets barcode of this product.
	 * @param barcode barcode of this product.
	 */
	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	/**
	 * Sets price of this product.
	 * @param unitPrice price of this product.
	 */
	public void setUnitPrice(double unitPrice) {
		this.unitPrice = unitPrice;
	}

	/**
	 * Returns id of this product.
	 * @return id of this product.
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * Returns barcode of this product.
	 * @return barcode of this product.
	 */
	public String getBarcode() {
		return barcode;
	}
	
	/**
	 * Returns price of this product.
	 * @return price of this product.
	 */
	public double getUnitPrice() {
		return unitPrice;
	}

	/**
	 * Returns the description of this Product in Map format. 
	 * @return the description of this Product in Map format.
	 */
	public Map<String, String> toMap() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("id", id + "");
		map.put("name", name);
		map.put("barcode", barcode);
		map.put("unitPrice", unitPrice + "");
		int stock = getStock(id);
		map.put("stock", stock + "");
		if (stock > 0) {
			map.put("availability", stock + " in stock");
		} else {
			map.put("availability", "Out of stock");
		}
		map.put("priority", priority +"");
		if (image != null) {
			map.put("image", Server.BASE_API_URL +""+ image);
		} else {
			map.put("image", null);
		}
		return map;
		
	}

	public double getUnitPriceByQuantity(int id, int quantity) {
		Double stock = 0.0;
		try {
			stock = Inventory.getInstance().getUnitPriceByQuantity(id, quantity);
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		return stock;
	}

	public int getStock(int id) {
		int stock = 0;
		try {
			stock = Inventory.getInstance().getStock().getStockSumById(id);
		} catch (NoDaoSetException e) {
			e.printStackTrace();
		}

		return stock;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public int getPriority() {
		return priority;
	}

	public String getImage() {
		return image;
	}
}
