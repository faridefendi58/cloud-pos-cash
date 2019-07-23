package com.slightsite.app.ui.params;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import com.slightsite.app.R;
import com.slightsite.app.domain.ParamsController;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.techicalservices.NoDaoSetException;

public class ParamsActivity extends Activity {
    private ParamCatalog paramCatalog;

    private EditText appStoreNameBox;
    private EditText appStoreAddressBox;
    private EditText appStoreAddressBox2;
    private EditText appStorePhoneBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        initiateUI();

        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            //ParamsController.getInstance().buildDatabase();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }
    }

    private void initiateUI() {
        setContentView(R.layout.layout_params);
        initiateActionBar();

        appStoreNameBox = (EditText) findViewById(R.id.appStoreNameBox);
        appStoreAddressBox = (EditText) findViewById(R.id.appStoreAddressBox);
        appStoreAddressBox2 = (EditText) findViewById(R.id.appStoreAddressBox2);
        appStorePhoneBox = (EditText) findViewById(R.id.appStorePhoneBox);

        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            update();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        initiateSubmision();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(getResources().getString(R.string.params));
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initiateSubmision() {
        Button confirmButton = (Button) findViewById(R.id.confirmButton);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    saveParams();
                    update();
                        Toast.makeText(getBaseContext(), getResources().getString(R.string.success),
                                Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getBaseContext(), getResources().getString(R.string.fail),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void saveParams() {
        if (appStoreNameBox.getText().toString().length() > 0) {
            Params pStoreName = paramCatalog.getParamByName("store_name");
            //Log.e(ParamsActivity.class.getSimpleName(), "pStoreName : "+ pStoreName.getValue());
            if (pStoreName instanceof Params) {
                pStoreName.setValue(appStoreNameBox.getText().toString());
                Boolean storeName = paramCatalog.editParam(pStoreName);
            } else {
                Boolean storeName = paramCatalog.addParam(
                        "store_name",
                        appStoreNameBox.getText().toString(),
                        "text",
                        "The name of store"
                );
            }
        }

        if (appStoreAddressBox.getText().toString().length() > 0) {
            Params pStoreAddress = paramCatalog.getParamByName("store_address");
            if (pStoreAddress instanceof Params) {
                pStoreAddress.setValue(appStoreAddressBox.getText().toString());
                Boolean storeAddress = paramCatalog.editParam(pStoreAddress);
            } else {
                Boolean storeAddress = paramCatalog.addParam(
                        "store_address",
                        appStoreAddressBox.getText().toString(),
                        "text",
                        "The address of store"
                );
            }
        }

        if (appStoreAddressBox2.getText().toString().length() > 0) {
            Params pStoreAddress2 = paramCatalog.getParamByName("store_address2");
            if (pStoreAddress2 instanceof Params) {
                pStoreAddress2.setValue(appStoreAddressBox2.getText().toString());
                Boolean storeAddress2 = paramCatalog.editParam(pStoreAddress2);
            } else {
                Boolean storeAddress2 = paramCatalog.addParam(
                        "store_address2",
                        appStoreAddressBox2.getText().toString(),
                        "text",
                        "The address of store 2"
                );
            }
        }

        if (appStorePhoneBox.getText().toString().length() > 0) {
            Params pStorePhone = paramCatalog.getParamByName("store_phone");
            if (pStorePhone instanceof Params) {
                pStorePhone.setValue(appStorePhoneBox.getText().toString());
                Boolean storePhone = paramCatalog.editParam(pStorePhone);
            } else {
                Boolean storePhone = paramCatalog.addParam(
                        "store_phone",
                        appStorePhoneBox.getText().toString(),
                        "text",
                        "The phone of store"
                );
            }
        }
    }

    private void update() {
        if (paramCatalog.getParamByName("store_name") != null)
            appStoreNameBox.setText(paramCatalog.getParamByName("store_name").getValue());

        if (paramCatalog.getParamByName("store_address") != null)
            appStoreAddressBox.setText(paramCatalog.getParamByName("store_address").getValue());

        if (paramCatalog.getParamByName("store_address2") != null)
            appStoreAddressBox2.setText(paramCatalog.getParamByName("store_address2").getValue());

        if (paramCatalog.getParamByName("store_phone") != null)
            appStorePhoneBox.setText(paramCatalog.getParamByName("store_phone").getValue());
    }
}
