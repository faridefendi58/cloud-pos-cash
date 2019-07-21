package com.slightsite.app.techicalservices.sale;

import java.util.Calendar;
import java.util.List;

import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.Sale;

/**
 * DAO for Sale process.
 *
 *
 */
public interface SaleDao {

	/**
	 * Initiates a new Sale.
	 * @param startTime time that Sale initiated.
	 * @return Sale that initiated
	 */
	Sale initiateSale(String startTime);

	/**
	 * End Sale
	 * @param sale Sale to be ended.
	 * @param endTime time that Sale ended.
	 */
	void endSale(Sale sale, String endTime);

	/**
	 * Add LineItem to Sale.
	 * @param saleId ID of the Sale to add LineItem.
	 * @param lineItem LineItem to be added.
	 * @return ID of LineItem that just added.
	 */
	int addLineItem(int saleId, LineItem lineItem);
	
	/**
	 * Returns all sale in the records.
	 * @return all sale in the records.
	 */
	List<Sale> getAllSale();

	/**
	 * Returns the Sale with specific ID.
	 * @param id ID of specific Sale.
	 * @return the Sale with specific ID.
	 */
	Sale getSaleById(int id);

	/**
	 * Clear all records in SaleLedger.	
	 */
	void clearSaleLedger();

	/**
	 * Returns list of LineItem that belong to Sale with specific Sale ID.
	 * @param saleId ID of sale.
	 * @return list of LineItem that belong to Sale with specific Sale ID.
	 */
	List<LineItem> getLineItem(int saleId);

	/**
	 * Updates the data of specific LineItem.
	 * @param saleId ID of Sale that this LineItem belong to.
	 * @param lineItem to be updated.
	 */
	void updateLineItem(int saleId, LineItem lineItem);

	/**
	 * Returns list of Sale with scope of time. 
	 * @param start start bound of scope.
	 * @param end end bound of scope.
	 * @return list of Sale with scope of time.
	 */
	List<Sale> getAllSaleDuring(Calendar start, Calendar end);

	/**
	 * Cancel the Sale.
	 * @param sale Sale to be cancel.
	 * @param endTime time that cancelled.
	 */
	void cancelSale(Sale sale,String endTime);

	/**
	 * Removes LineItem.
	 * @param id of LineItem to be removed.
	 */
	void removeLineItem(int id);

	void setCustomerSale(Sale sale, Customer customer);

	Customer getCustomerBySaleId(int id);

	void removeCustomerSale(Sale sale);

	List<Sale> getAllSaleByCustomerId(int id);

	void removeSale(Sale sale);

	int getTotalIncome(String time_frame);

	int getTotalTransaction(String time_frame);

	void setPushedSale(Sale sale, int server_invoice_id, String server_invoice_number);

	int getServerInvoiceId(int sale_id);

	void setDiscount(Sale sale);

	int getDiscount(int sale_id);
}
