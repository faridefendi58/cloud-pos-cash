package com.slightsite.app.domain.sale;

import android.util.Log;

import java.awt.font.TextAttribute;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.Stock;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.payment.PaymentDao;
import com.slightsite.app.techicalservices.sale.SaleDao;
import com.slightsite.app.techicalservices.shipping.ShippingDao;

/**
 * Handles all Sale processes.
 *
 *
 */
public class Register {
	private static Register instance = null;
	private static SaleDao saleDao = null;
	private static Stock stock = null;
	private static Customer customer = null;
	private static PaymentDao paymentDao = null;
	private static ShippingDao shippingDao = null;
	private static List<PaymentItem> payment_items = null;
	private static Shipping shipping = null;

	private Sale currentSale;
	
	private Register() throws NoDaoSetException {
		if (!isDaoSet()) {
			throw new NoDaoSetException();
		}
		stock = Inventory.getInstance().getStock();
		
	}
	
	/**
	 * Determines whether the DAO already set or not.
	 * @return true if the DAO already set; otherwise false.
	 */
	public static boolean isDaoSet() {
		return saleDao != null;
	}
	
	public static Register getInstance() throws NoDaoSetException {
		if (instance == null) instance = new Register();
		return instance;
	}

	/**
	 * Injects its sale DAO
	 * @param dao DAO of sale
	 */
	public static void setSaleDao(SaleDao dao) {
		saleDao = dao;	
	}
	
	/**
	 * Initiates a new Sale.
	 * @param startTime time that sale created.
	 * @return Sale that created.
	 */
	public Sale initiateSale(String startTime) {
		if (currentSale != null) {
			return currentSale;
		}
		currentSale = saleDao.initiateSale(startTime);
		return currentSale;
	}
	
	/**
	 * Add Product to Sale.
	 * @param product product to be added.
	 * @param quantity quantity of product that added.
	 * @return LineItem of Sale that just added.
	 */
	public LineItem addItem(Product product, int quantity) {
		if (currentSale == null)
			initiateSale(DateTimeStrategy.getCurrentTime());
		
		LineItem lineItem = currentSale.addLineItem(product, quantity);
		
		if (lineItem.getId() == LineItem.UNDEFINED) {
			int lineId = saleDao.addLineItem(currentSale.getId(), lineItem);
			lineItem.setId(lineId);
		} else {
			saleDao.updateLineItem(currentSale.getId(), lineItem);
		}
		
		return lineItem;
	}
	
	/**
	 * Returns total price of Sale.
	 * @return total price of Sale.
	 */
	public double getTotal() {
		if (currentSale == null) return 0;
		return currentSale.getTotal();
	}

	/**
	 * End the Sale.
	 * @param endTime time that Sale ended.
	 */
	public void endSale(String endTime) {
		if (currentSale != null) {
			saleDao.endSale(currentSale, endTime);
			for(LineItem line : currentSale.getAllLineItem()){
				Log.e(getClass().getSimpleName(), "end sale -> "+ line.getProduct().getName());
				stock.updateStockSum(line.getProduct().getId(), line.getQuantity());
			}

			try {
				List<Payment> check_payment = paymentDao.getPaymentBySaleId(currentSale.getId());
				if (check_payment != null) {
					for(Payment pymnt : check_payment){
						// remove the payment data on update
						paymentDao.removePayment(pymnt.getId());
					}
				}
			} catch (Exception e) {e.printStackTrace();}
			// saving the payment method
			for (PaymentItem pi : payment_items) {
				try {
					Payment pym = new Payment(currentSale.getId(), pi.getTitle(), pi.getNominal());
					paymentDao.addPayment(pym);
				} catch (Exception e){
					e.printStackTrace();
				}
			}

			try {
				Shipping check_shp = shippingDao.getShippingBySaleId(currentSale.getId());
				if (check_shp != null) {
					shippingDao.removeShipping(check_shp.getId());
				}
				Shipping shp = getShipping();
				shp.setDateAdded(endTime);
				shp.setSaleId(currentSale.getId());
				shippingDao.addShipping(shp);
			} catch (Exception e){
				e.printStackTrace();
			}
			Log.e(getClass().getSimpleName(), "Payment Data on end payment : "+ payment_items.toString());
			Log.e(getClass().getSimpleName(), "Shipping Data on end payment : "+ shipping.toMap().toString());
			currentSale = null;
		}
	}
	
	/**
	 * Returns the current Sale of this Register.
	 * @return the current Sale of this Register.
	 */
	public Sale getCurrentSale() {
		if (currentSale == null)
			initiateSale(DateTimeStrategy.getCurrentTime());
		return currentSale;
	}

	/**
	 * Sets the current Sale of this Register.
	 * @param id of Sale to retrieve.
	 * @return true if success to load Sale from ID; otherwise false.
	 */
	public boolean setCurrentSale(int id) {
		currentSale = saleDao.getSaleById(id);
		return false;
	}

	/**
	 * Determines that if there is a Sale handling or not.
	 * @return true if there is a current Sale; otherwise false.
	 */
	public boolean hasSale(){
		if(currentSale == null)return false;
		return true;
	}
	
	/**
	 * Cancels the current Sale.
	 */
	public void cancleSale() {
		if (currentSale != null){
			saleDao.cancelSale(currentSale,DateTimeStrategy.getCurrentTime());
			currentSale = null;
		}
	}

	/**
	 * Edit the specific LineItem 
	 * @param saleId ID of LineItem to be edited. 
	 * @param lineItem LineItem to be edited.
	 * @param quantity a new quantity to set.
	 * @param priceAtSale a new priceAtSale to set.
	 */
	public void updateItem(int saleId, LineItem lineItem, int quantity, double priceAtSale) {
		int tot_sale_qty = currentSale.getOrders() + quantity - lineItem.getQuantity();

		if (lineItem.multi_level_price) { // jika harga bertingkat aktif
			// geting unit price of the all total quantity
			double thePriceAtSale = lineItem.getProduct().getUnitPriceByQuantity(lineItem.getProduct().getId(), tot_sale_qty);
			if (thePriceAtSale > 0) {
				priceAtSale = thePriceAtSale;
			}
			//also update the other item
			for(LineItem line : currentSale.getAllLineItem()){
				if (line.getId() != lineItem.getId()) {
					double thePriceAtSale2 = line.getProduct().getUnitPriceByQuantity(line.getProduct().getId(), tot_sale_qty);
					if (thePriceAtSale2 > 0) {
						line.setUnitPriceAtSale(thePriceAtSale2);
					}
				}
			}
		}

		lineItem.setUnitPriceAtSale(priceAtSale);

		lineItem.setQuantity(quantity);
		saleDao.updateLineItem(saleId, lineItem);
	}

	/**
	 * Removes LineItem from the current Sale.
	 * @param lineItem lineItem to be removed.
	 */
	public void removeItem(LineItem lineItem) {
		saleDao.removeLineItem(lineItem.getId());
		currentSale.removeItem(lineItem);
		if (currentSale.getAllLineItem().isEmpty()) {
			cancleSale();
		}
	}

	public void setCustomer(Customer cst) {
		if (currentSale != null) {
			saleDao.setCustomerSale(currentSale, cst);
			customer = cst;
		} else {
			Log.e("Register", "currentSale : ga ada.");
		}
	}

	public Customer getCustomer() {

		return customer;
	}

	public void removeCustomer() {
		if (currentSale != null) {
			saleDao.removeCustomerSale(currentSale);
		}
	}

	public void setPaymentItems(List<PaymentItem> payment_items) {
		if (currentSale != null) {
			this.payment_items = payment_items;
		}
	}

	public List<PaymentItem> getPaymentItems() {
		return payment_items;
	}

	public static void setPaymentDao(PaymentDao dao) {
		paymentDao = dao;
	}

	public static void setShippingDao(ShippingDao dao) {
		shippingDao = dao;
	}

	public void setShipping(Shipping _shipping) {
		if (currentSale != null) {
			this.shipping = _shipping;
		}
	}

	public Shipping getShipping() {
		return shipping;
	}
}