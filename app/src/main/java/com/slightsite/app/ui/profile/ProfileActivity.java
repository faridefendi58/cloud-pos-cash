package com.slightsite.app.ui.profile;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import android.view.View;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.ui.DashboardActivity;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;

public class ProfileActivity extends Activity {

    private SharedPreferences sharedpreferences;
    private SharedPreferences.Editor editor;
    private ContentValues adminData;

    private TabHost mTabHost;
    private EditText input_name;
    private EditText input_email;
    private EditText input_phone;
    private EditText input_old_password;
    private EditText input_new_password;
    private EditText input_new_password_confirm;

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#1ABC9C")));
            actionBar.setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mTabHost = (TabHost) findViewById(android.R.id.tabhost);
        mTabHost.setup();
        mTabHost.addTab(mTabHost.newTabSpec("tab_test1").setIndicator(getResources().getString(R.string.tab_profile_data))
                .setContent(R.id.tab1));
        mTabHost.addTab(mTabHost.newTabSpec("tab_test2").setIndicator(getResources().getString(R.string.tab_profile_password))
                .setContent(R.id.tab2));
        mTabHost.setCurrentTab(0);

        initUi();
        initiateActionBar();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_profile, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                startActivity(intent);
                return true;
            case R.id.navigation_home:
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initUi() {
        input_name = (EditText) findViewById(R.id.input_name);
        input_email = (EditText) findViewById(R.id.input_email);
        input_phone = (EditText) findViewById(R.id.input_phone);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
        if (adminData != null) {
            input_name.setText(adminData.getAsString(LoginActivity.TAG_NAME));
            input_email.setText(adminData.getAsString(LoginActivity.TAG_EMAIL));
            input_phone.setText(adminData.getAsString(LoginActivity.TAG_PHONE));
        } else {
            input_name.setText(sharedpreferences.getString(LoginActivity.TAG_NAME, null));
            input_email.setText(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
            input_phone.setText(sharedpreferences.getString(LoginActivity.TAG_PHONE, null));
        }

        input_old_password = (EditText) findViewById(R.id.input_old_password);
        input_new_password = (EditText) findViewById(R.id.input_new_password);
        input_new_password_confirm = (EditText) findViewById(R.id.input_new_password_confirm);
    }

    public void updateProfile(View view) {
        boolean cancel = false;
        View focusView = null;

        String email = input_email.getText().toString();
        String name = input_name.getText().toString();
        String phone = input_phone.getText().toString();
        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            input_email.setError(getString(R.string.error_field_required));
            focusView = input_email;
            cancel = true;
        } else if (!LoginActivity.isEmailValid(email)) {
            input_email.setError(getString(R.string.error_invalid_email));
            focusView = input_email;
            cancel = true;
        } else {
            ContentValues adminData = ProfileController.getInstance().getDataByEmail(email);
            if (adminData != null
                    && !email.equals(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null))) {
                input_email.setError(getString(R.string.error_unavailable_email));
                focusView = input_email;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(name)) {
            input_name.setError(getString(R.string.error_field_required));
            focusView = input_name;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            input_phone.setError(getString(R.string.error_field_required));
            focusView = input_phone;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            ContentValues content = new ContentValues();
            content.put("_id", adminData.getAsInteger("_id"));
            content.put("email", email);
            content.put("name", name);
            content.put("phone", phone);

            boolean updated = ProfileController.getInstance().update(content);
            if (updated) {
                adminData = ProfileController.getInstance().getDataByEmail(email);

                editor.putString(LoginActivity.TAG_NAME, name);
                editor.putString(LoginActivity.TAG_EMAIL, email);
                editor.putString(LoginActivity.TAG_PHONE, phone);
                editor.commit();

                Toast.makeText(ProfileActivity.this,
                        getResources().getString(R.string.message_success_update),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void updatePassword(View view) {
        boolean cancel = false;
        View focusView = null;
        editor = sharedpreferences.edit();

        String old_password = input_old_password.getText().toString();
        String new_password = input_new_password.getText().toString();
        String new_password_confirm = input_new_password_confirm.getText().toString();
        // Check for a valid email address.
        if (TextUtils.isEmpty(old_password)) {
            input_old_password.setError(getString(R.string.error_field_required));
            focusView = input_old_password;
            cancel = true;
        } else {
            if (adminData != null
                    && !old_password.equals(adminData.getAsString(LoginActivity.TAG_PASSWORD))) {
                input_old_password.setError(getString(R.string.error_old_password));
                focusView = input_old_password;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(new_password)) {
            input_new_password.setError(getString(R.string.error_field_required));
            focusView = input_new_password;
            cancel = true;
        }

        if (TextUtils.isEmpty(new_password_confirm)) {
            input_new_password_confirm.setError(getString(R.string.error_field_required));
            focusView = input_new_password_confirm;
            cancel = true;
        }

        if (!new_password.equals(new_password_confirm)) {
            input_new_password_confirm.setError(getString(R.string.error_invalid_password_repeat));
            focusView = input_new_password_confirm;
            cancel = true;
        }

        /*if (LoginActivity.isPasswordValid(new_password)) {
            input_new_password.setError(getString(R.string.error_invalid_password));
            focusView = input_new_password;
            cancel = true;
        }*/

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            ContentValues content = new ContentValues();
            content.put("_id", adminData.getAsInteger("_id"));
            content.put("password", new_password);

            boolean updated = ProfileController.getInstance().update(content);
            if (updated) {
                adminData = ProfileController.getInstance().getDataByEmail(
                        sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
                editor.putString(LoginActivity.TAG_PASSWORD, new_password);
                editor.commit();

                Toast.makeText(ProfileActivity.this,
                        getResources().getString(R.string.message_success_change_password),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
