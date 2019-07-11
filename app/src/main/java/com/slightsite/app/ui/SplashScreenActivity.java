package com.slightsite.app.ui;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.LanguageController;
import com.slightsite.app.domain.ParamsController;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.domain.customer.CustomerService;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.payment.PaymentService;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.SaleLedger;
import com.slightsite.app.domain.shipping.ShippingService;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.techicalservices.AndroidDatabase;
import com.slightsite.app.techicalservices.Database;
import com.slightsite.app.techicalservices.DatabaseExecutor;
import com.slightsite.app.techicalservices.customer.CustomerDao;
import com.slightsite.app.techicalservices.customer.CustomerDaoAndroid;
import com.slightsite.app.techicalservices.inventory.InventoryDao;
import com.slightsite.app.techicalservices.inventory.InventoryDaoAndroid;
import com.slightsite.app.techicalservices.params.ParamDao;
import com.slightsite.app.techicalservices.params.ParamDaoAndroid;
import com.slightsite.app.techicalservices.payment.PaymentDao;
import com.slightsite.app.techicalservices.payment.PaymentDaoAndroid;
import com.slightsite.app.techicalservices.sale.SaleDao;
import com.slightsite.app.techicalservices.sale.SaleDaoAndroid;
import com.slightsite.app.techicalservices.shipping.ShippingDao;
import com.slightsite.app.techicalservices.shipping.ShippingDaoAndroid;
import com.slightsite.app.techicalservices.warehouse.AdminInWarehouseDao;
import com.slightsite.app.techicalservices.warehouse.AdminInWarehouseDaoAndroid;
import com.slightsite.app.techicalservices.warehouse.WarehouseDao;
import com.slightsite.app.techicalservices.warehouse.WarehouseDaoAndroid;

/**
 * This is the first activity page, core-app and database created here.
 * Dependency injection happens here.
 *
 * 
 */
public class SplashScreenActivity extends Activity {

	public static final String POS_VERSION = "UcokPOS 2.0";
	private static final long SPLASH_TIMEOUT = 2000;
	private Button goButton;
	private boolean gone;
	
	/**
	 * Loads database and DAO.
	 */
	private void initiateCoreApp() {
		Database database = new AndroidDatabase(this);
		InventoryDao inventoryDao = new InventoryDaoAndroid(database);
		SaleDao saleDao = new SaleDaoAndroid(database);
		CustomerDao customerDao = new CustomerDaoAndroid(database);
		ParamDao paramDao = new ParamDaoAndroid(database);
		PaymentDao paymentDao = new PaymentDaoAndroid(database);
		ShippingDao shippingDao = new ShippingDaoAndroid(database) {};
		WarehouseDao warehouseDao = new WarehouseDaoAndroid(database) {};
		AdminInWarehouseDao adminInWarehouseDao = new AdminInWarehouseDaoAndroid(database) {};

		DatabaseExecutor.setDatabase(database);
		LanguageController.setDatabase(database);
		CurrencyController.setDatabase(database);
		ParamsController.setDatabase(database);
		ProfileController.setDatabase(database);

		Inventory.setInventoryDao(inventoryDao);
		Register.setSaleDao(saleDao);
		SaleLedger.setSaleDao(saleDao);
		CustomerService.setCustomerDao(customerDao);
		ParamService.setParamDao(paramDao);
		PaymentService.setPaymentDao(paymentDao);
		Register.setPaymentDao(paymentDao);
		ShippingService.setShippingDao(shippingDao);
		Register.setShippingDao(shippingDao);
		WarehouseService.setWarehouseDao(warehouseDao);
		AdminInWarehouseService.setAdminInWarehouseDao(adminInWarehouseDao);

		DateTimeStrategy.setLocale("id", "ID");
		setLanguage(LanguageController.getInstance().getLanguage());
		CurrencyController.setCurrency("idr");
	}
	
	/**
	 * Set language.
	 * @param localeString
	 */
	private void setLanguage(String localeString) {
		Locale locale = new Locale(localeString);
		Locale.setDefault(locale);
		Configuration config = new Configuration();
		config.locale = locale;
		getBaseContext().getResources().updateConfiguration(config,
				getBaseContext().getResources().getDisplayMetrics());
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		initiateUI(savedInstanceState);
		initiateCoreApp();
	}
	
	/**
	 * Go.
	 */
	private void go() {
		gone = true;
		Intent newActivity = new Intent(SplashScreenActivity.this,
				LoginActivity.class);
		finish();
		startActivity(newActivity);
		//SplashScreenActivity.this.finish();
	}

	private ProgressBar progressBar;
	private int progressStatus = 0;
	private Handler handler = new Handler();

	/**
	 * Initiate this UI.
	 * @param savedInstanceState
	 */
	private void initiateUI(Bundle savedInstanceState) {
		setContentView(R.layout.layout_splashscreen);
		goButton = (Button) findViewById(R.id.goButton);
		goButton.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				go();
			}

		});

		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		// Start long running operation in a background thread
		new Thread(new Runnable() {
			public void run() {
				while (progressStatus < 100) {
					progressStatus += 5;
					// Update the progress bar and display the
					//current value in the text view
					handler.post(new Runnable() {
						public void run() {
							progressBar.setProgress(progressStatus);
						}
					});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				if (!gone) go();
			}
		}, SPLASH_TIMEOUT);
	}
}