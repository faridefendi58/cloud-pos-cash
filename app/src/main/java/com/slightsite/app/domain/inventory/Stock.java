package com.slightsite.app.domain.inventory;

import android.content.ContentValues;

import java.util.List;

import com.slightsite.app.techicalservices.inventory.InventoryDao;

/**
 * Import log of ProductLot come in to store.
 *
 *
 */
public class Stock {

	private InventoryDao inventoryDao;

	/**
	 * Constructs Data Access Object of inventory in ProductCatalog.
	 * @param inventoryDao DAO of inventory.
	 */
	public Stock(InventoryDao inventoryDao) {
		this.inventoryDao = inventoryDao;
	}
	
	/**
	 * Constructs ProductLot and adds ProductLot to inventory.
	 * @param dateAdded date added of ProductLot.
	 * @param quantity quantity of ProductLot.
	 * @param product product of ProductLot.
	 * @param cost cost of ProductLot.
	 * @return
	 */
	public boolean addProductLot(String dateAdded, int quantity, Product product, double cost) {
		ProductLot productLot = new ProductLot(ProductLot. UNDEFINED_ID, dateAdded, quantity, product, cost);
		int id = inventoryDao.addProductLot(productLot);
		return id != -1;
	}

	/**
	 * Returns list of ProductLot in inventory finds by id. 
	 * @param id id of ProductLot.
	 * @return list of ProductLot in inventory finds by id.
	 */
	public List<ProductLot> getProductLotByProductId(int id) {
		return inventoryDao.getProductLotByProductId(id);
	}

	/**
	 * Returns list of ProductDiscount in inventory finds by id.
	 * @param id id of ProductDiscount.
	 * @return list of ProductDiscount in inventory finds by id.
	 */
	public List<ProductDiscount> getProductDiscountByProductId(int id) {
		return inventoryDao.getProductDiscountByProductId(id);
	}

	/**
	 * Returns all ProductLots in inventory.
	 * @return all ProductLots in inventory.
	 */
	public List<ProductLot> getAllProductLot() {
		return inventoryDao.getAllProductLot();
	}

	public List<ProductDiscount> getAllProductDiscount() {
		return inventoryDao.getAllProductDiscount();
	}

	/**
	 * Returns Stock in inventory finds by id.
	 * @param id id of Stock.
	 * @return Stock in inventory finds by id.
	 */
	public int getStockSumById(int id) {
		return inventoryDao.getStockSumById(id);
	}

	/**
	 * Updates quantity of product.
	 * @param productId id of product.
	 * @param quantity quantity of product.
	 */
	public void updateStockSum(int productId, int quantity) {
		inventoryDao.updateStockSum(productId,quantity);
		
	}

	/**
	 * Clear Stock.
	 */
	public void clearStock() {
		inventoryDao.clearStock();
		
	}

	/**
	 * Constructs ProductLot and adds ProductDiscount to inventory.
	 * @param dateAdded date added of ProductDiscount.
	 * @param quantity quantity of ProductDiscount.
	 * @param quantity_max quantity of ProductDiscount.
	 * @param product product of ProductDiscount.
	 * @param cost cost of ProductDiscount.
	 * @return
	 */
	public boolean addProductDiscount(String dateAdded, int quantity, int quantity_max, Product product, double cost) {
		ProductDiscount productDiscount = new ProductDiscount(ProductDiscount. UNDEFINED_ID, dateAdded, quantity, quantity_max, product, cost);
		int id = inventoryDao.addProductDiscount(productDiscount);
		return id != -1;
	}

	public void updateProductDiscount(int id, int quantity, int quantity_max, double cost) {
		inventoryDao.updateProductDiscount(id, quantity, quantity_max, cost);
	}

	public void deleteProductDiscount(int id) {
		inventoryDao.deleteProductDiscount(id);
	}

	public ContentValues getDiscountDataByQuantity(int productId, int quantity) {
		ContentValues discount_data = inventoryDao.getDiscountDataByQuantity(productId, quantity);
		return discount_data;
	}
}
