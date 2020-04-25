package com.slightsite.app.ui.profile;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActionBar;

import android.app.ProgressDialog;
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
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.DashboardActivity;
import com.slightsite.app.ui.LoginActivity;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

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
    private TextView tv_role;

    ProgressDialog pDialog;
    String tag_json_obj = "json_obj_req";
    int success;
    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    @SuppressLint("NewApi")
    private void initiateActionBar() {
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
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
        tv_role = (TextView) findViewById(R.id.tv_role);

        sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);
        adminData = ProfileController.getInstance().getDataByEmail(sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
        if (adminData != null) {
            input_name.setText(adminData.getAsString(LoginActivity.TAG_NAME));
            input_email.setText(adminData.getAsString(LoginActivity.TAG_EMAIL));
            input_phone.setText(adminData.getAsString(LoginActivity.TAG_PHONE));
            HashMap<Integer, String> role_names = Tools.getRoleNames();
            int server_group_id = adminData.getAsInteger("server_group_id");
            if (role_names.containsKey(server_group_id)) {
                tv_role.setText(role_names.get(server_group_id));
            }
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
            content.put("server_admin_id", adminData.getAsInteger("server_admin_id"));
            content.put("email", email);
            content.put("name", name);
            content.put("phone", phone);

            try {
                editor = sharedpreferences.edit();
                doUpdateOnServer(content);
            } catch (Exception e){e.printStackTrace();}
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
            content.put("server_admin_id", adminData.getAsInteger("server_admin_id"));
            content.put("password", new_password);
            content.put("old_password", old_password);
            content.put("new_password", new_password);

            try {
                doChangePasswordOnServer(content);
            } catch (Exception e){e.printStackTrace();}
        }
    }

    private void doUpdateOnServer(final ContentValues content) {
        Log.e(getClass().getSimpleName(), "content values : "+ content.toString());

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Processing update ...");
        showDialog();

        StringRequest strReq = new StringRequest(
                Request.Method.POST,
                Server.URL + "user/update?api-key=" + Server.API_KEY,
                new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        content.remove("server_admin_id");
                        boolean updated = ProfileController.getInstance().update(content);
                        if (updated) {
                            //adminData = ProfileController.getInstance().getDataByEmail(content.getAsString("email"));

                            editor.putString(LoginActivity.TAG_NAME, content.getAsString("name"));
                            editor.putString(LoginActivity.TAG_EMAIL, content.getAsString("email"));
                            editor.putString(LoginActivity.TAG_PHONE, content.getAsString("phone"));
                            editor.commit();

                            /*Toast.makeText(ProfileActivity.this,
                                    getResources().getString(R.string.message_success_update),
                                    Toast.LENGTH_SHORT).show();*/
                        }

                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                        finish();
                        startActivity(getIntent());
                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) { e.printStackTrace(); }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("admin_id", content.getAsString("server_admin_id"));
                params.put("name", content.getAsString("name"));
                params.put("email", content.getAsString("email"));
                params.put("phone", content.getAsString("phone"));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void doChangePasswordOnServer(final ContentValues content) {
        Log.e(getClass().getSimpleName(), "content values : "+ content.toString());

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Processing update password ...");
        showDialog();

        StringRequest strReq = new StringRequest(
                Request.Method.POST,
                Server.URL + "user/change-password?api-key=" + Server.API_KEY,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            // Check for error node in json
                            if (success == 1) {
                                String new_password = content.getAsString("new_password");
                                content.remove("server_admin_id");
                                content.remove("old_password");
                                content.remove("new_password");

                                boolean updated = ProfileController.getInstance().update(content);
                                if (updated) {
                                    adminData = ProfileController.getInstance().getDataByEmail(
                                            sharedpreferences.getString(LoginActivity.TAG_EMAIL, null));
                                    editor.putString(LoginActivity.TAG_PASSWORD, new_password);
                                    editor.commit();
                                }

                                Toast.makeText(ProfileActivity.this,
                                        getResources().getString(R.string.message_success_change_password),
                                        Toast.LENGTH_SHORT).show();

                                finish();
                                startActivity(getIntent());
                            } else {
                                Toast.makeText(getApplicationContext(),
                                        jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) { e.printStackTrace(); }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("admin_id", content.getAsString("server_admin_id"));
                params.put("old_password", content.getAsString("old_password"));
                params.put("new_password", content.getAsString("new_password"));

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (pDialog != null && !pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog != null && pDialog.isShowing())
            pDialog.dismiss();
    }
}
