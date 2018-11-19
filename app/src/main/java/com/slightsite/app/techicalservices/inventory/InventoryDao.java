package com.slightsite.app.techicalservices.inventory;

import android.content.ContentValues;

import java.util.List;

import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductDiscount;
import com.slightsite.app.domain.inventory.ProductLot;

/**
 * DAO for Inventory.
 *
 *
 */
public interface InventoryDao {

	/**
	 * Adds product to inventory.
	 * @param product the product to be added.
	 * @return id of this product that assigned from database.
	 */
	int addProduct(Product product);
	
	/**
	 * Adds ProductLot to inventory.
	 * @param productLot the ProductLot to be added.
	 * @return id of this ProductLot that assigned from database.
	 */
	int addProductLot(ProductLot productLot);

	/**
	 * Adds ProductDiscount to inventory.
	 * @param productDiscount the ProductDiscount to be added.
	 * @return id of this ProductDiscount that assigned from database.
	 */
	int addProductDiscount(ProductDiscount productDiscount);

	void updateProductDiscount(int id, int quantity, int quantity_max, double cost);

	void deleteProductDiscount(int id);

	/**
	 * Edits product.
	 * @param product the product to be edited.
	 * @return true if product edits success ; otherwise false.
	 */
	boolean editProduct(Product product);

	/**
	 * Returns product from inventory finds by id.
	 * @param id id of product.
	 * @return product
	 */
	Product getProductById(int id);
	
	/**
	 * Returns product from inventory finds by barcode.
	 * @param barcode barcode of product.
	 * @return product
	 */
	Product getProductByBarcode(String barcode);
	
	/**
	 * Returns list of all products in inventory.
	 * @return list of all products in inventory.
	 */
	List<Product> getAllProduct();
	
	/**
	 * Returns list of product in inventory finds by name.
	 * @param name name of product.
	 * @return list of product in inventory finds by name.
	 */
	List<Product> getProductByName(String name);
	
	/**
	 * Search product from string in inventory.
	 * @param search string for searching.
	 * @return list of product.
	 */
	List<Product> searchProduct(String search);
	
	/**
	 * Returns list of all products in inventory.
	 * @return list of all products in inventory.
	 */
	List<ProductLot> getAllProductLot();

	List<ProductDiscount> getAllProductDiscount();

	/**
	 * Returns list of product in inventory finds by id. 
	 * @param id id of product.
	 * @return list of product in inventory finds by id.
	 */
	List<ProductLot> getProductLotById(int id);

	List<ProductDiscount> getProductDiscountById(int id);

	/**
	 * Returns list of ProductLot in inventory finds by id. 
	 * @param id id of ProductLot.
	 * @return list of ProductLot in inventory finds by id.
	 */
	List<ProductLot> getProductLotByProductId(int id);

	/**
	 *
	 * @param id
	 * @return
	 */
	List<ProductDiscount> getProductDiscountByProductId(int id);

	/**
	 * Returns Stock in inventory finds by id.
	 * @param id id of Stock.
	 * @return Stock in inventory finds by id.
	 */
	int getStockSumById(int id);
	
	/**
	 * Updates quantity of product.
	 * @param productId id of product.
	 * @param quantity quantity of product.
	 */
	void updateStockSum(int productId, double quantity);
	
	/**
	 * Clears ProductCatalog.
	 */
	void clearProductCatalog();
	
	/**
	 * Clear Stock.
	 */
	void clearStock();
	
	/**
	 * Hidden product from inventory.
	 * @param product The product to be hidden.
	 */
	void suspendProduct(Product product);

	double getUnitPriceByQuantity(int productId, int quantity);

	ContentValues getDiscountDataByQuantity(int productId, int quantity);
}
