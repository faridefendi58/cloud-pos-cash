package com.slightsite.app.techicalservices;

/**
 * Enum for name of tables in database.
 *
 *
 */
public enum DatabaseContents {
	
	DATABASE("com.slightsite.posdb3"),
	TABLE_PRODUCT_CATALOG("product_catalog"),
	TABLE_STOCK("stock"),
	TABLE_PRODUCT_DISCOUNT("product_discount"),
	TABLE_SALE("sale"),
	TABLE_SALE_LINEITEM("sale_lineitem"),
	TABLE_STOCK_SUM("stock_sum"),
	TABLE_CUSTOMER("customer"),
	LANGUAGE("language"),
	CURRENCY("currency"),
	TABLE_PARAMS("params"),
	TABLE_ADMIN("admin"),
	TABLE_SALE_PAYMENT("sale_payment"),
	TABLE_SALE_SHIPPING("sale_shipping"),
	TABLE_WAREHOUSES("warehouses"),
	TABLE_ADMIN_IN_WAREHOUSE("admin_in_warehouse");

	private String name;
	
	/**
	 * Constructs DatabaseContents.
	 * @param name name of this content for using in database.
	 */
	private DatabaseContents(String name) {
		this.name = name;
	}
	
	@Override 
	public String toString() {
		return name;
	}
}
