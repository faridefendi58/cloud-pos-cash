package com.slightsite.app.techicalservices.sale;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.QuickLoadSale;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseContents;


/**
 * DAO used by android for Sale process.
 *
 *
 */
public class SaleDaoAndroid implements SaleDao {

	Database database;
	public SaleDaoAndroid(Database database) {
		this.database = database;
	}

	@Override
	public Sale initiateSale(String startTime) {
		ContentValues content = new ContentValues();
        content.put("start_time", startTime.toString());
        content.put("status", "ON PROCESS");
        content.put("payment", "n/a");
        content.put("total", "0.0");
        content.put("orders", "0");
        content.put("end_time", startTime.toString());
        
        int id = database.insert(DatabaseContents.TABLE_SALE.toString(), content);
		return new Sale(id,startTime);
	}

	@Override
	public void endSale(Sale sale, String endTime) {
		ContentValues content = new ContentValues();
        content.put("_id", sale.getId());
        content.put("status", "ENDED");
        content.put("payment", "n/a");
        content.put("total", sale.getTotal());
        if (sale.getDiscount() > 0) {
			content.put("discount", sale.getDiscount());
		}
        content.put("orders", sale.getOrders());
        content.put("start_time", sale.getStartTime());
        content.put("end_time", endTime);
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}
	
	@Override
	public int addLineItem(int saleId, LineItem lineItem) {
		ContentValues content = new ContentValues();
        content.put("sale_id", saleId);
        content.put("product_id", lineItem.getProduct().getId());
        content.put("quantity", lineItem.getQuantity());
        content.put("unit_price", lineItem.getPriceAtSale());
        int id = database.insert(DatabaseContents.TABLE_SALE_LINEITEM.toString(), content);
        return id;
	}

	@Override
	public void updateLineItem(int saleId, LineItem lineItem) {
		ContentValues content = new ContentValues();		
		content.put("_id", lineItem.getId());
		content.put("sale_id", saleId);
		content.put("product_id", lineItem.getProduct().getId());
		content.put("quantity", lineItem.getQuantity());
		content.put("unit_price", lineItem.getPriceAtSale());
		database.update(DatabaseContents.TABLE_SALE_LINEITEM.toString(), content);
	}

	@Override
	public List<Sale> getAllSale() {
		return getAllSale(" WHERE status = 'ENDED'");
	}
	
	@Override
	public List<Sale> getAllSaleDuring(Calendar start, Calendar end) {
		String startBound = DateTimeStrategy.getSQLDateFormat(start);
		String endBound = DateTimeStrategy.getSQLDateFormat(end);
		List<Sale> list = getAllSale(" WHERE end_time BETWEEN '" + startBound + " 00:00:00' AND '" + endBound + " 23:59:59' AND status IN ('ENDED', 'PUSHED')");
		return list;
	}
	
	/**
	 * This method get all Sale *BUT* no LineItem will be loaded.
	 * @param condition
	 * @return
	 */
	public List<Sale> getAllSale(String condition) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE + condition;
        List<Object> objectList = database.select(queryString);
        List<Sale> list = new ArrayList<Sale>();
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
			QuickLoadSale the_sale = new QuickLoadSale(
					content.getAsInteger("_id"),
					content.getAsString("start_time"),
					content.getAsString("end_time"),
					content.getAsString("status"),
					content.getAsDouble("total"),
					content.getAsInteger("orders"),
					content.getAsInteger("customer_id")
			);
			the_sale.setServerInvoiceId(content.getAsInteger("server_invoice_id"));
			the_sale.setServerInvoiceNumber(content.getAsString("server_invoice_number"));
			the_sale.setDiscount(content.getAsInteger("discount"));
        	list.add(the_sale);
        }
        return list;
	}
	
	/**
	 * This load complete data of Sale.
	 * @param id Sale ID.
	 * @return Sale of specific ID.
	 */
	@Override
	public Sale getSaleById(int id) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE + " WHERE _id = " + id;
        List<Object> objectList = database.select(queryString);
        List<Sale> list = new ArrayList<Sale>();
        for (Object object: objectList) {
        	ContentValues content = (ContentValues) object;
        	Sale sale = new Sale(
					content.getAsInteger("_id"),
					content.getAsString("start_time"),
					content.getAsString("end_time"),
					content.getAsString("status"),
					getLineItem(content.getAsInteger("_id")),
					content.getAsInteger("customer_id"));
        	sale.setDiscount(content.getAsInteger("discount"));
        	sale.setServerInvoiceNumber(content.getAsString("server_invoice_number"));
        	sale.setServerInvoiceId(content.getAsInteger("server_invoice_id"));

        	list.add(sale);
        }
        return list.get(0);
	}

	@Override
	public List<LineItem> getLineItem(int saleId) {
		String queryString = "SELECT * FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE sale_id = " + saleId;
		List<Object> objectList = database.select(queryString);
		List<LineItem> list = new ArrayList<LineItem>();

		int tot_qty = 0; //getTotalQuantity(saleId);
		for (Object object1: objectList) {
			ContentValues content1 = (ContentValues) object1;
			tot_qty = tot_qty + content1.getAsInteger("quantity");
		}

		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			int productId = content.getAsInteger("product_id");
			String queryString2 = "SELECT * FROM " + DatabaseContents.TABLE_PRODUCT_CATALOG + " WHERE _id = " + productId;
			List<Object> objectList2 = database.select(queryString2);
			
			List<Product> productList = new ArrayList<Product>();
			for (Object object2: objectList2) {
				ContentValues content2 = (ContentValues) object2;
				Product prod = new Product(productId, content2.getAsString("name"), content2.getAsString("barcode"), content2.getAsDouble("unit_price"));
				prod.setImage(content2.getAsString("images"));
				if (content2.getAsByteArray("image_bitmap") != null) {
					Bitmap bitmap = BitmapFactory.decodeByteArray(content2.getAsByteArray("image_bitmap"), 0, content2.getAsByteArray("image_bitmap").length);
					prod.setImageBitmap(bitmap);
				}
				productList.add(prod);
			}

			list.add(new LineItem(content.getAsInteger("_id") , productList.get(0), content.getAsInteger("quantity"), content.getAsDouble("unit_price"), tot_qty));
		}
		return list;
	}

	@Override
	public void clearSaleLedger() {
		database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE);
		database.execute("DELETE FROM " + DatabaseContents.TABLE_SALE_LINEITEM);
	}

	@Override
	public void cancelSale(Sale sale,String endTime) {
		ContentValues content = new ContentValues();
        content.put("_id", sale.getId());
        content.put("status", "CANCELED");
        content.put("payment", "n/a");
        content.put("total", sale.getTotal());
        content.put("orders", sale.getOrders());
		content.put("customer_id", 0);
        content.put("start_time", sale.getStartTime());
        content.put("end_time", endTime);
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}

	@Override
	public void removeLineItem(int id) {
		database.delete(DatabaseContents.TABLE_SALE_LINEITEM.toString(), id);
	}

	@Override
	public void setCustomerSale(Sale sale, Customer customer) {
		ContentValues content = new ContentValues();
		content.put("_id", sale.getId());
		content.put("customer_id", customer.getId());
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}

	@Override
	public Customer getCustomerBySaleId(int id) {
		String queryString = "SELECT t._id, t.customer_id, c.name, c.email, c.phone, c.address, c.status, c.server_customer_id " +
				"FROM " + DatabaseContents.TABLE_SALE + " t " +
				"LEFT JOIN " + DatabaseContents.TABLE_CUSTOMER + " c ON c._id = t.customer_id " +
				"WHERE t._id = " + id;

		List<Object> objectList = database.select(queryString);
		List<Customer> list = new ArrayList<Customer>();
		if (objectList.size() > 0) {
			for (Object object: objectList) {
				ContentValues content = (ContentValues) object;
				Log.e("SaleDAO", "content : "+ content.toString());
				if (content != null) {
					Customer customer = new Customer(
							(content.get("name") != null)? content.getAsString("name") : "-",
							(content.get("email") != null)? content.getAsString("email") : "-",
							(content.get("phone") != null)? content.getAsString("phone") : "-",
							(content.get("address") != null)? content.getAsString("address") : "-",
							(content.get("status") != null)? content.getAsInteger("status") : 0
					);
					customer.setId(content.getAsInteger("_id"));
					customer.setServerCustomerId(content.getAsInteger("server_customer_id"));
					list.add(customer);
				} else {
					list.add(new Customer(
							"-", "-", "-", "-", 0
					));
				}
			}
		}

		return list.get(0);
	}

	@Override
	public void removeCustomerSale(Sale sale) {
		ContentValues content = new ContentValues();
		content.put("_id", sale.getId());
		content.put("customer_id", 0);
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}

	@Override
	public List<Sale> getAllSaleByCustomerId(int id) {
		return getAllSale(" WHERE status = 'ENDED' AND customer_id = "+ id);
	}

	@Override
	public void removeSale(Sale sale) {
		int sale_id = sale.getId();
		database.delete(DatabaseContents.TABLE_SALE.toString(), sale_id);
		String sql = "DELETE FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " " +
				"WHERE sale_id = " + sale_id;
		database.execute(sql);
	}

	@Override
	public int getTotalIncome(String time_frame) {
		String queryString = "SELECT end_time, SUM(total) AS sum_total, COUNT(_id) AS count " +
				"FROM " + DatabaseContents.TABLE_SALE +
				" WHERE status = 'ENDED'";
		if (time_frame.equals("today")) {
			queryString += " AND strftime('%Y-%m-%d',end_time) = date('now')";
		} else if (time_frame.equals("yesterday")) {
			queryString += " AND strftime('%Y-%m-%d',end_time) = date('now', '-1 day')";
		} else if (time_frame.equals("this_month")) {
			queryString += " AND end_time BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')";
		} else if (time_frame.equals("last_month")) {
			queryString += " AND strftime('%Y-%m', end_time) = strftime('%Y-%m', datetime('now', 'start of month', '-1 month'))";
		} else {
			queryString += " AND strftime('%Y-%m-%d',end_time) = strftime('%Y-%m-%d',date('now'))";
		}

		List<Object> objectList = database.select(queryString);
		int total = 0;
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			if (content != null
					&& content.containsKey("sum_total")
					&& content.getAsString("sum_total") != null
					&& !content.getAsString("sum_total").equals("1.395e+06")
					&& !content.getAsString("sum_total").contains("e+")) {
				total = content.getAsInteger("sum_total");
			}
			Log.e("tag", "content :"+ content.toString());
		}

		return total;
	}

	@Override
	public int getTotalTransaction(String time_frame) {
		String queryString = "SELECT COUNT(_id) AS count " +
				"FROM " + DatabaseContents.TABLE_SALE +
				" WHERE status = 'ENDED'";
		if (time_frame.equals("today")) {
			queryString += " AND strftime('%Y-%m-%d',end_time) = date('now')";
		} else if (time_frame.equals("yesterday")) {
			queryString += " AND strftime('%Y-%m-%d',end_time) = date('now', '-1 day')";
		} else if (time_frame.equals("this_month")) {
			queryString += " AND end_time BETWEEN datetime('now', 'start of month') AND datetime('now', 'localtime')";
		} else if (time_frame.equals("last_month")) {
			queryString += " AND strftime('%Y-%m', end_time) = strftime('%Y-%m', datetime('now', 'start of month', '-1 month'))";
		} else {
			queryString += " AND strftime('%Y-%m-%d',end_time) = strftime('%Y-%m-%d',date('now'))";
		}

		List<Object> objectList = database.select(queryString);
		int total = 0;
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			if (content != null
					&& content.containsKey("count")
					&& content.getAsString("count") != null) {
				total = content.getAsInteger("count");
			}
		}

		return total;
	}

	public int getTotalQuantity(int saleId) {
		String queryString = "SELECT SUM(quantity) AS total_qty FROM " + DatabaseContents.TABLE_SALE_LINEITEM + " WHERE sale_id = " + saleId;
		List<Object> objectList = database.select(queryString);
		int total = 0;
		Log.e("Sale dao android", "qry total qty : "+ objectList.toString());
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			if (content != null
					&& content.containsKey("total_qty")
					&& content.getAsString("total_qty") != null) {
				total = content.getAsInteger("total_qty");
			}
		}

		return total;
	}

	@Override
	public void setPushedSale(Sale sale, int server_invoice_id, String server_invoice_number) {
		ContentValues content = new ContentValues();
		content.put("_id", sale.getId());
		content.put("pushed", server_invoice_id);
		content.put("status", "PUSHED");
		if (server_invoice_number != null) {
			content.put("server_invoice_id", server_invoice_id);
			content.put("server_invoice_number", server_invoice_number);
		}
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}

	@Override
	public int getServerInvoiceId(int sale_id) {
		String queryString = "SELECT server_invoice_id FROM " + DatabaseContents.TABLE_SALE + " WHERE _id = " + sale_id;
		List<Object> objectList = database.select(queryString);
		int inv_id = 0;
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			if (content != null
					&& content.containsKey("server_invoice_id")
					&& content.getAsString("server_invoice_id") != null) {
				inv_id = content.getAsInteger("server_invoice_id");
			}
		}

		return inv_id;
	}

	@Override
	public void setDiscount(Sale sale) {
		ContentValues content = new ContentValues();
		content.put("_id", sale.getId());
		content.put("discount", sale.getDiscount());
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}

	@Override
	public int getDiscount(int sale_id) {
		String queryString = "SELECT discount FROM " + DatabaseContents.TABLE_SALE + " WHERE _id = " + sale_id;
		List<Object> objectList = database.select(queryString);
		int discount = 0;
		for (Object object: objectList) {
			ContentValues content = (ContentValues) object;
			if (content != null
					&& content.containsKey("discount")
					&& content.getAsString("discount") != null) {
				discount = content.getAsInteger("discount");
			}
		}

		return discount;
	}

	@Override
	public void setFinished(Sale sale) {
		ContentValues content = new ContentValues();
		content.put("_id", sale.getId());
		content.put("status", "FINISHED");
		database.update(DatabaseContents.TABLE_SALE.toString(), content);
	}
}
