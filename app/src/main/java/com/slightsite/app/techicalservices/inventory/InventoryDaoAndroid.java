package com.slightsite.app.techicalservices.inventory;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.util.Log;

import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductDiscount;
import com.slightsite.app.domain.inventory.ProductLot;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;
import com.slightsite.app.ui.inventory.ProductDetailActivity;

/**
 * DAO used by android for Inventory.
 *
 *
 */
public class InventoryDaoAndroid implements InventoryDao {

	private Database database;
	
	/**
	 * Constructs InventoryDaoAndroid.
	 * @param database database for use in InventoryDaoAndroid.
	 */
	public InventoryDaoAndroid(Database database) {
		this.database = database;
	}

	@Override
	public int addProduct(Product product) {
		ContentValues content = new ContentValues();
        content.put("name", product.getName());
        content.put("barcode", product.getBarcode());
        content.put("unit_price", product.getUnitPrice());
        content.put("status", "ACTIVE");
        
        int id = database.insert(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
        
        
    	ContentValues content2 = new ContentValues();
        content2.put("_id", id);
        content2.put("quantity", 0);
        database.insert(DatabaseContents.TABLE_STOCK_SUM.toString(), content2);
        
        return id;
	}
	
	/**
	 * Converts list of object to list of product.
	 * @param objectList list of object.
	 * @return list of product.
	 */
	private List<Product> toProductList(List<Object> objectList) {
		List<Product> list = new ArrayList<Product>();
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
                list.add(new Product(
                		content.getAsInteger("_id"),
                        content.getAsString("name"),
                        content.getAsString("barcode"),
                        content.getAsDouble("unit_price"))
                );
        }
        return list;
	}

	@Override
	public List<Product> getAllProduct() {
        return getAllProduct(" WHERE status = 'ACTIVE'");
	}
	
	/**
	 * Returns list of all products in inventory.
	 * @param condition specific condition for getAllProduct.
	 * @return list of all products in inventory.
	 */
	private List<Product> getAllProduct(String condition) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG.toString() + condition + " ORDER BY _id";
        List<Product> list = toProductList(database.select(queryString));
        return list;
	}
	
	/**
	 * Returns product from inventory finds by specific reference. 
	 * @param reference reference value.
	 * @param value value for search.
	 * @return list of product.
	 */
	private List<Product> getProductBy(String reference, String value) {
        String condition = " WHERE " + reference + " = " + value + " ;";
        return getAllProduct(condition);
	}
	
	/**
	 * Returns product from inventory finds by similar name.
	 * @param reference reference value.
	 * @param value value for search.
	 * @return list of product.
	 */
	private List<Product> getSimilarProductBy(String reference, String value) {
        String condition = " WHERE " + reference + " LIKE '%" + value + "%' ;";
        return getAllProduct(condition);
	}

	@Override
	public Product getProductByBarcode(String barcode) {
		List<Product> list = getProductBy("barcode", barcode);
        if (list.isEmpty()) return null;
        return list.get(0);
	}

	@Override
	public Product getProductById(int id) {
		return getProductBy("_id", id+"").get(0);
	}

	@Override
	public boolean editProduct(Product product) {
		ContentValues content = new ContentValues();
		content.put("_id", product.getId());
		content.put("name", product.getName());
        content.put("barcode", product.getBarcode());
        content.put("status", "ACTIVE");
        content.put("unit_price", product.getUnitPrice());
		return database.update(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
	}
	
	
	@Override
	public int addProductLot(ProductLot productLot) {
		 ContentValues content = new ContentValues();
         content.put("date_added", productLot.getDateAdded());
         content.put("quantity",  productLot.getQuantity());
         content.put("product_id",  productLot.getProduct().getId());
         content.put("cost",  productLot.unitCost());
         int id = database.insert(DatabaseContents.TABLE_STOCK.toString(), content);
         
         int productId = productLot.getProduct().getId();
         ContentValues content2 = new ContentValues();
         content2.put("_id", productId);
         content2.put("quantity", getStockSumById(productId) + productLot.getQuantity());

         if (isEmptyData(productId)) {
         	database.insert(DatabaseContents.TABLE_STOCK_SUM.toString(), content2);
		 } else {
         	database.update(DatabaseContents.TABLE_STOCK_SUM.toString(), content2);
		 }
         
         return id;
	}


	@Override
	public List<Product> getProductByName(String name) {
		return getSimilarProductBy("name", name);
	}

	@Override
	public List<Product> searchProduct(String search) {
		String condition = " WHERE name LIKE '%" + search + "%' OR barcode LIKE '%" + search + "%' ;";
        return getAllProduct(condition);
	}
	
	/**
	 * Returns list of all ProductLot in inventory.
	 * @param condition specific condition for get ProductLot.
	 * @return list of all ProductLot in inventory.
	 */
	private List<ProductLot> getAllProductLot(String condition) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_STOCK.toString() + condition;
        List<ProductLot> list = toProductLotList(database.select(queryString));
        return list;
	}

	/**
	 * Converts list of object to list of ProductLot.
	 * @param objectList list of object.
	 * @return list of ProductLot.
	 */
	private List<ProductLot> toProductLotList(List<Object> objectList) {
		List<ProductLot> list = new ArrayList<ProductLot>();
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			int productId = content.getAsInteger("product_id");
			Product product = getProductById(productId);
					list.add( 
					new ProductLot(content.getAsInteger("_id"),
							content.getAsString("date_added"),
							content.getAsInteger("quantity"),
							product,
							content.getAsDouble("cost"))
					);
		}
		return list;
	}

	@Override
	public List<ProductLot> getProductLotByProductId(int id) {
		return getAllProductLot(" WHERE product_id = " + id);
	}
	
	@Override
	public List<ProductLot> getProductLotById(int id) {
		return getAllProductLot(" WHERE _id = " + id);
	}

	@Override
	public List<ProductLot> getAllProductLot() {
		return getAllProductLot("");
	}

	@Override
	public int getStockSumById(int id) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_STOCK_SUM + " WHERE _id = " + id;
		List<Object> objectList = (database.select(queryString));
		if (objectList.size() <= 0) {
			return 0;
		}
		ContentValues content = (ContentValues) objectList.get(0);
		int quantity = content.getAsInteger("quantity");
		Log.d("inventoryDaoAndroid", "stock sum of "+ id + " is " + quantity);
		return quantity;
	}

	@Override
	public void updateStockSum(int productId, double quantity) {
		 ContentValues content = new ContentValues();
         content.put("_id", productId);
         content.put("quantity", getStockSumById(productId) - quantity);
         database.update(DatabaseContents.TABLE_STOCK_SUM.toString(), content);   
	}

	@Override
	public void clearProductCatalog() {		
		database.execute("DELETE FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG);
	}

	@Override
	public void clearStock() {
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK);
		database.execute("DELETE FROM " + DatabaseContents.TABLE_STOCK_SUM);
	}

	@Override
	public void suspendProduct(Product product) {
		ContentValues content = new ContentValues();
		content.put("_id", product.getId());
		content.put("name", product.getName());
		content.put("barcode", product.getBarcode());
		content.put("status", "INACTIVE");
		content.put("unit_price", product.getUnitPrice());
		database.update(DatabaseContents.TABLE_PRODUCT_CATALOG.toString(), content);
	}

	@Override
	public int addProductDiscount(ProductDiscount productDiscount) {
		ContentValues content = new ContentValues();
		content.put("date_added", productDiscount.getDateAdded());
		content.put("quantity",  productDiscount.getQuantity());
		content.put("quantity_max",  productDiscount.getQuantityMax());
		content.put("product_id",  productDiscount.getProduct().getId());
		content.put("cost",  productDiscount.unitCost());

		//Log.e(ProductDetailActivity.class.getSimpleName(), "COntent data : "+ content.toString());
		int id = database.insert(DatabaseContents.TABLE_PRODUCT_DISCOUNT.toString(), content);

		return id;
	}

	/**
	 * Returns list of all ProductDiscount in inventory.
	 * @param condition specific condition for get ProductDiscount.
	 * @return list of all ProductDiscount in inventory.
	 */
	private List<ProductDiscount> getAllProductDiscount(String condition) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_PRODUCT_DISCOUNT.toString() + condition;
		List<ProductDiscount> list = toProductDiscountList(database.select(queryString));
		return list;
	}

	/**
	 * Converts list of object to list of ProductDiscount.
	 * @param objectList list of object.
	 * @return list of ProductDiscount.
	 */
	private List<ProductDiscount> toProductDiscountList(List<Object> objectList) {
		List<ProductDiscount> list = new ArrayList<ProductDiscount>();
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			int productId = content.getAsInteger("product_id");
			Product product = getProductById(productId);
			list.add(
					new ProductDiscount(content.getAsInteger("_id"),
							content.getAsString("date_added"),
							content.getAsInteger("quantity"),
							content.getAsInteger("quantity_max"),
							product,
							content.getAsDouble("cost"))
			);
		}
		return list;
	}

	@Override
	public List<ProductDiscount> getProductDiscountByProductId(int id) {
		return getAllProductDiscount(" WHERE product_id = " + id);
	}

	@Override
	public List<ProductDiscount> getProductDiscountById(int id) {
		return getAllProductDiscount(" WHERE _id = " + id);
	}

	@Override
	public List<ProductDiscount> getAllProductDiscount() {
		return getAllProductDiscount("");
	}

	@Override
	public void updateProductDiscount(int id, int quantity, int quantity_max, double cost) {
		ContentValues content = new ContentValues();
		content.put("_id", id);
		content.put("quantity", quantity);
		content.put("quantity_max", quantity_max);
		content.put("cost", cost);

		database.update(DatabaseContents.TABLE_PRODUCT_DISCOUNT.toString(), content);
	}

	@Override
	public void deleteProductDiscount(int id) {
		database.delete(DatabaseContents.TABLE_PRODUCT_DISCOUNT.toString(), id);
	}

	@Override
	public double getUnitPriceByQuantity(int productId, int quantity) {
		String queryString = "SELECT * FROM " +
				DatabaseContents.TABLE_PRODUCT_DISCOUNT +
				" WHERE product_id = " + productId + " AND "+ quantity +" BETWEEN quantity AND quantity_max;";

		List<Object> objectList = (database.select(queryString));
		//Log.e("DB", "Object list : "+ objectList.toString());
		//Log.e("DB", "productId : "+ productId);
		//Log.e("DB", "quantity : "+ quantity);

		Double cost = 0.0;
		if (objectList.size() > 0) {
			ContentValues content = (ContentValues) objectList.get(0);
			cost = content.getAsDouble("cost");
		}

		return cost;
	}

	@Override
	public ContentValues getDiscountDataByQuantity(int productId, int quantity) {
		String queryString = "SELECT * FROM " +
				DatabaseContents.TABLE_PRODUCT_DISCOUNT +
				" WHERE product_id = " + productId + " AND "+ quantity +" BETWEEN quantity AND quantity_max;";

		List<Object> objectList = (database.select(queryString));

		ContentValues content = null;
		if (objectList.size() > 0) {
			content = (ContentValues) objectList.get(0);
		}

		return content;
	}

    @Override
    public void clearProductDiscount() {
        database.execute("DELETE FROM " + DatabaseContents.TABLE_PRODUCT_DISCOUNT);
    }

	public Boolean isEmptyData(int id) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_STOCK_SUM + " WHERE _id = " + id;
		List<Object> objectList = (database.select(queryString));
		if (objectList.size() <= 0) {
			return true;
		}

		return false;
	}
}
