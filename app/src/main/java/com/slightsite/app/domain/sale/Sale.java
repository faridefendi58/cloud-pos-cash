package com.slightsite.app.domain.sale;

import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.LanguageController;
import com.slightsite.app.domain.ParamsController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;

/**
 * Sale represents sale operation.
 *
 *
 */
public class Sale implements Serializable {
	
	private final int id;
	private String startTime;
	private String endTime;
	private String status;
	private List<LineItem> items;
	private Integer customer_id;
	private String server_invoice_number;
	private int discount = 0;
	private int server_invoice_id;
	private int created_by;
	private String created_by_name;
	private int paid_by;
	private String paid_by_name;
	private int refunded_by;
	private String refunded_by_name;
	private String delivered_plan_at;
	private String delivered_at;

	public Sale(int id, String startTime) {
		this(id, startTime, startTime, "", new ArrayList<LineItem>(), 0);
	}
	
	/**
	 * Constructs a new Sale.
	 * @param id ID of this Sale.
	 * @param startTime start time of this Sale.
	 * @param endTime end time of this Sale.
	 * @param status status of this Sale.
	 * @param items list of LineItem in this Sale.
	 */
	public Sale(int id, String startTime, String endTime, String status, List<LineItem> items, int customer_id) {
		this.id = id;
		this.startTime = startTime;
		this.status = status;
		this.endTime = endTime;
		this.items = items;
		this.customer_id = customer_id;
	}
	
	/**
	 * Returns list of LineItem in this Sale.
	 * @return list of LineItem in this Sale.
	 */
	public List<LineItem> getAllLineItem(){
		return items;
	}

	public void setAllLineItem(List<LineItem> _items) {
		this.items = _items;
	}
	
	/**
	 * Add Product to Sale.
	 * @param product product to be added.
	 * @param quantity quantity of product that added.
	 * @return LineItem of Sale that just added.
	 */
	public LineItem addLineItem(Product product, int quantity) {

		for (LineItem lineItem : items) {
			if (lineItem.getProduct().getId() == product.getId()) {
				lineItem.addQuantity(quantity);
				return lineItem;
			}
		}

		int quantity_total = getOrders();
		LineItem lineItem = new LineItem(product, quantity, quantity_total);
		items.add(lineItem);
		return lineItem;
	}
	
	public int size() {
		return items.size();
	}
	
	/**
	 * Returns a LineItem with specific index.
	 * @param index of specific LineItem.
	 * @return a LineItem with specific index.
	 */
	public LineItem getLineItemAt(int index) {
		if (index >= 0 && index < items.size())
			return items.get(index);
		return null;
	}

	/**
	 * Returns the total price of this Sale.
	 * @return the total price of this Sale.
	 */
	public double getTotal() {
		double amount = 0;
		for(LineItem lineItem : items) {
			amount += lineItem.getTotalPriceAtSale();
		}
		return amount;
	}

	public int getId() {
		return id;
	}

	public String getStartTime() {
		return startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String _status) {
		this.status = _status;
	}

	/**
	 * Returns the total quantity of this Sale.
	 * @return the total quantity of this Sale.
	 */
	public int getOrders() {
		int orderCount = 0;
		for (LineItem lineItem : items) {
			orderCount += lineItem.getQuantity();
		}
		return orderCount;
	}

	/**
	 * Returns the description of this Sale in Map format. 
	 * @return the description of this Sale in Map format.
	 */
	public Map<String, String> toMap() {	
		Map<String, String> map = new HashMap<String, String>();
		map.put("id",id + "");
		map.put("startTime", DateTimeStrategy.parseDate(startTime, "dd/MM/yy HH:ss"));
		map.put("endTime", DateTimeStrategy.parseDate(endTime, "dd/MM/yy HH:ss"));
		map.put("status", getStatus());
		map.put("total", CurrencyController.getInstance().moneyFormat(getTotal()) + "");
		map.put("orders", getOrders() + "");
		map.put("customer_id", customer_id +"");
		String invoice_number = DateTimeStrategy.parseDate(endTime, "yyMMdd");
		String admin_id = ParamsController.getInstance().getParam("admin_id");
		if (admin_id != null) {
			invoice_number = invoice_number+"/"+admin_id+"/"+id;
		} else {
			invoice_number = invoice_number+"/0/"+id;
		}

		map.put("invoiceNumber", invoice_number);
		map.put("server_invoice_number", server_invoice_number);
		
		return map;
	}

	/**
	 * Removes LineItem from Sale.
	 * @param lineItem lineItem to be removed.
	 */
	public void removeItem(LineItem lineItem) {
		items.remove(lineItem);
		//rebuild the line item
		if (lineItem.multi_level_price) { // jika harga bertingkat aktif
			int tot_sale_qty = getOrders();
			for (LineItem lineItem2 : items) {
				// geting unit price of the all total quantity
				double thePriceAtSale = lineItem2.getProduct().getUnitPriceByQuantity(lineItem2.getProduct().getId(), tot_sale_qty);
				if (thePriceAtSale > 0) {
                    lineItem2.setUnitPriceAtSale(thePriceAtSale);
				}
			}
		}
	}

	public int getCustomerId() {
		return this.customer_id;
	}
	public void setCustomerId(int id) {this.customer_id = id;}

	public LineItem getLineItemByProductId(int id) {
		if (id >= 0) {
			for (LineItem lineItem : items) {
				if (lineItem.getProduct().getId() == id) {
					return lineItem;
				}
			}
		}
		return null;
	}

	public void setServerInvoiceNumber(String _server_invoice_number) {
		this.server_invoice_number = _server_invoice_number;
	}

	public String getServerInvoiceNumber() {
		return server_invoice_number;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public int getDiscount() {
		return discount;
	}

	public void setServerInvoiceId(int _server_invoice_id) {
		this.server_invoice_id = _server_invoice_id;
	}

	public int getServerInvoiceId() {
		return server_invoice_id;
	}

	public void setCreatedBy(int created_by) {
		this.created_by = created_by;
	}

	public int getCreatedBy() {
		return created_by;
	}

	public void setPaidBy(int paid_by) {
		this.paid_by = paid_by;
	}

	public int getPaidBy() {
		return paid_by;
	}

	public void setRefundedBy(int refunded_by) {
		this.refunded_by = refunded_by;
	}

	public int getRefundedBy() {
		return refunded_by;
	}

	public void setCreatedByName(String created_by_name) {
		this.created_by_name = created_by_name;
	}

	public String getCreatedByName() {
		return created_by_name;
	}

	public void setPaidByName(String paid_by_name) {
		this.paid_by_name = paid_by_name;
	}

	public String getPaidByName() {
		return paid_by_name;
	}

	public void setRefundedByName(String refunded_by_name) {
		this.refunded_by_name = refunded_by_name;
	}

	public String getRefundedByName() {
		return refunded_by_name;
	}

	public void setDeliveredPlanAt(String delivered_plan_at) {
		this.delivered_plan_at = delivered_plan_at;
	}

	public String getDeliveredPlanAt() {
		if (delivered_plan_at != null) {
			return DateTimeStrategy.parseDate(delivered_plan_at, "dd MMM yyyy HH:ss");
		}

		return delivered_plan_at;
	}

	public void setDeliveredAt(String delivered_at) {
		this.delivered_at = delivered_at;
	}

	public String getDeliveredAt() {
		return delivered_at;
	}
}