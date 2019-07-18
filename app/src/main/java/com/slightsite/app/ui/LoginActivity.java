package com.slightsite.app.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;

import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.design.widget.BottomSheetDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.LanguageController;
import com.slightsite.app.domain.ProfileController;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.domain.warehouse.AdminInWarehouseCatalog;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText passwordRepeat;
    private EditText nameBox;
    private EditText phoneBox;
    private Button mEmailSignInButton;
    private Button signup_button;
    private Button register_button;
    private Button signin_button;
    private View mProgressView;
    private View mLoginFormView;
    private LinearLayout role_container;
    private RadioGroup role_group;
    private Spinner available_warehouse;
    private LinearLayout wh_container;
    private LinearLayout pre_form;
    private LinearLayout header_container;
    private LinearLayout custom_title_bar;
    private ImageView current_lang_flag;
    private LinearLayout passwordRepeatContainer;
    private LinearLayout nameBoxContainer;
    private LinearLayout phoneBoxContainer;

    public final static String TAG_ID = "id";
    public final static String TAG_EMAIL = "email";
    public final static String TAG_NAME = "name";
    public final static String TAG_PHONE = "phone";
    public final static String TAG_PASSWORD = "password";
    public final static String TAG_USERNAME = "username";

    SharedPreferences sharedpreferences;
    Boolean session = false;
    String id, email, name;
    public static final String my_shared_preferences = "my_shared_preferences";
    public static final String session_status = "session_status";

    ProgressDialog pDialog;
    String tag_json_obj = "json_obj_req";

    private String url = Server.URL + "user/login?api-key=" + Server.API_KEY;
    private String url_register = Server.URL + "user/register?api-key=" + Server.API_KEY;

    private static final String TAG = LoginActivity.class.getSimpleName();

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";

    int success;
    ConnectivityManager conMgr;

    private ParamCatalog paramCatalog;
    private WarehouseCatalog warehouseCatalog;
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private Boolean is_cashier = true;
    private Boolean success_register = false;

    private final HashMap<Integer, String> warehouse_names = new HashMap<Integer, String>();
    private JSONArray warehouse_data;
    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private int warehouse_id = 0;
    private int register_admin_id = 0;
    private String current_lang;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            if (warehouse_names.size() == 0) {
                getWarehouseList();
            }
            adminInWarehouseCatalog = AdminInWarehouseService.getInstance().getAdminInWarehouseCatalog();
            current_lang = LanguageController.getInstance().getLanguage();
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        checkSession();

        conMgr = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        {
            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection",
                        Toast.LENGTH_LONG).show();
            }
        }

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        //populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
        nameBox = findViewById(R.id.nameBox);
        phoneBox = findViewById(R.id.phoneBox);

        signup_button = (Button) findViewById(R.id.signup_button);
        signup_button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegister();
            }
        });
        register_button = (Button) findViewById(R.id.register_button);
        signin_button = (Button) findViewById(R.id.signin_button);
        passwordRepeat = (EditText) findViewById(R.id.passwordRepeat);

        role_container = (LinearLayout) findViewById(R.id.role_container);
        role_group = (RadioGroup) findViewById(R.id.role_group);
        available_warehouse = (Spinner) findViewById(R.id.available_warehouse);
        wh_container = (LinearLayout) findViewById(R.id.wh_container);
        pre_form = (LinearLayout) findViewById(R.id.pre_form);
        header_container = (LinearLayout) findViewById(R.id.header_container);
        custom_title_bar = (LinearLayout) findViewById(R.id.custom_title_bar);
        current_lang_flag = (ImageView) findViewById(R.id.current_lang_flag);

        passwordRepeatContainer = (LinearLayout) findViewById(R.id.passwordRepeatContainer);
        nameBoxContainer = (LinearLayout) findViewById(R.id.nameBoxContainer);
        phoneBoxContainer = (LinearLayout) findViewById(R.id.phoneBoxContainer);

        if (warehouse_items.size() > 0) {
            ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                    getApplicationContext(),
                    R.layout.spinner_item, warehouse_items);
            whAdapter.notifyDataSetChanged();
            available_warehouse.setAdapter(whAdapter);
        }

        if (current_lang.equals("id")) {
            current_lang_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_flag_indonesia));
        } else {
            current_lang_flag.setImageDrawable(getResources().getDrawable(R.drawable.ic_iconfinder_flag_uk));
        }
    }

    private void populateAutoComplete() {
        /*if (!mayRequestContacts()) {
            return;
        }*/

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            // TODO: alert the user with a Snackbar/AlertDialog giving them the permission rationale
            // To use the Snackbar from the design support library, ensure that the activity extends
            // AppCompatActivity and uses the Theme.AppCompat theme.
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        /*if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }*/
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    public static boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 2;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    private void getWarehouseList() {
        warehouse_items.clear();

        List<Warehouses> whs = warehouseCatalog.getAllWarehouses();
        if (whs != null && whs.size() > 0) {
            Log.e(getClass().getSimpleName(), "whs : "+ whs.size());
            for (Warehouses wh : whs) {
                warehouse_names.put(wh.getWarehouseId(), wh.getTitle());
                warehouse_items.add(wh.getTitle());
            }
        } else {
            Log.e(getClass().getSimpleName(), "Tidak ada data");
            Map<String, String> params = new HashMap<String, String>();

            String url = Server.URL + "warehouse/list?api-key=" + Server.API_KEY;
            _string_request(
                    Request.Method.GET,
                    url, params, false,
                    new VolleyCallback() {
                        @Override
                        public void onSuccess(String result) {
                            try {
                                JSONObject jObj = new JSONObject(result);
                                success = jObj.getInt(TAG_SUCCESS);
                                // Check for error node in json
                                if (success == 1) {
                                    warehouse_data = jObj.getJSONArray("data");
                                    for(int n = 0; n < warehouse_data.length(); n++)
                                    {
                                        JSONObject data_n = warehouse_data.getJSONObject(n);
                                        warehouse_names.put(data_n.getInt("id"), data_n.getString("title"));
                                        warehouse_items.add(data_n.getString("title"));
                                        warehouse_ids.put(data_n.getString("title"), data_n.getString("id"));

                                        // updating or inserting the wh data on local
                                        Warehouses whs = warehouseCatalog.getWarehouseByWarehouseId(data_n.getInt("id"));
                                        if (whs == null) {
                                            warehouseCatalog.addWarehouse(
                                                    data_n.getInt("id"),
                                                    data_n.getString("title"),
                                                    data_n.getString("address"),
                                                    data_n.getString("phone"),
                                                    data_n.getInt("active")
                                            );
                                        } else {
                                            whs.setTitle(data_n.getString("title"));
                                            whs.setAddress(data_n.getString("address"));
                                            whs.setPhone(data_n.getString("phone"));
                                            whs.setStatus(data_n.getInt("active"));
                                            warehouseCatalog.editWarehouse(whs);
                                        }
                                    }

                                    ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                                            getApplicationContext(),
                                            R.layout.spinner_item, warehouse_items);
                                    whAdapter.notifyDataSetChanged();
                                    available_warehouse.setAdapter(whAdapter);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });
        }
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {
        if (show_dialog) {
            pDialog = new ProgressDialog(this);
            pDialog.setCancelable(false);
            pDialog.setMessage("Request data ...");
            showDialog();
        }

        if (method == Request.Method.GET) { //get method doesnt support getParams
            Iterator<Map.Entry<String, String>> iterator = params.entrySet().iterator();
            while(iterator.hasNext())
            {
                Map.Entry<String, String> pair = iterator.next();
                String pair_value = pair.getValue();
                if (pair_value.contains(" "))
                    pair_value = pair.getValue().replace(" ", "%20");
                url += "&" + pair.getKey() + "=" + pair_value;
            }
        }

        StringRequest strReq = new StringRequest(method, url, new Response.Listener < String > () {

            @Override
            public void onResponse(String Response) {
                callback.onSuccess(Response);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                if (show_dialog) {
                    hideDialog();
                }
            }
        })
        {
            // set headers
            @Override
            protected Map<String, String> getParams() {
                return params;
            }
        };

        strReq.setRetryPolicy(new DefaultRetryPolicy(20 * 1000, 0,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        try {
            AppController.getInstance().addToRequestQueue(strReq, "json_obj_req");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    /**
     * Shows the soft keyboard
     */
    public void showSoftKeyboard(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        view.requestFocus();
        inputMethodManager.showSoftInput(view, 0);
    }
    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            ContentValues admin = ProfileController.getInstance().getDataByEmail(mEmail);
            if (admin != null) {
                if (mPassword.equals(admin.getAsString("password"))) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putBoolean(session_status, true);
                    editor.putString(TAG_ID, admin.getAsString("_id"));
                    editor.putString(TAG_EMAIL, admin.getAsString("email"));
                    editor.putString(TAG_NAME, admin.getAsString("name"));
                    editor.commit();
                } else {
                    return false;
                }

                return true;
            } else {
                // check on server
                if (mEmail.trim().length() > 0 && mPassword.trim().length() > 0) {
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {
                        try {
                            checkLogin(mEmail, mPassword);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        return false;
                    }

                    return true;
                }
            }

            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                // mengecek kolom yang kosong
                if (mEmail.trim().length() > 0 && mPassword.trim().length() > 0) {
                    if (conMgr.getActiveNetworkInfo() != null
                            && conMgr.getActiveNetworkInfo().isAvailable()
                            && conMgr.getActiveNetworkInfo().isConnected()) {
                        checkLogin(mEmail, mPassword);
                    } else {
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(), "Kolom tidak boleh kosong", Toast.LENGTH_LONG).show();
                }

                /*Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(TAG_ID, id);
                intent.putExtra(TAG_EMAIL, email);
                finish();
                startActivity(intent);*/
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }

    private void checkSession() {
        // Cek session login jika TRUE maka langsung buka MainActivity
        sharedpreferences = getSharedPreferences(my_shared_preferences, Context.MODE_PRIVATE);
        session = sharedpreferences.getBoolean(session_status, false);
        id = sharedpreferences.getString(TAG_ID, null);
        email = sharedpreferences.getString(TAG_EMAIL, null);
        name = sharedpreferences.getString(TAG_NAME, null);

        if (session) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            intent.putExtra(TAG_ID, id);
            intent.putExtra(TAG_EMAIL, email);
            intent.putExtra(TAG_NAME, name);
            finish();
            startActivity(intent);
        }
    }

    public void registerRequest(View view) {
        header_container.setVisibility(View.GONE);
        custom_title_bar.setVisibility(View.VISIBLE);
        pre_form.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);
        nameBox.setVisibility(View.VISIBLE);
        phoneBox.setVisibility(View.VISIBLE);
        mEmailSignInButton.setVisibility(View.GONE);
        signup_button.setVisibility(View.VISIBLE);
        register_button.setVisibility(View.GONE);
        signin_button.setVisibility(View.VISIBLE);
        passwordRepeat.setVisibility(View.VISIBLE);
        role_container.setVisibility(View.VISIBLE);
        wh_container.setVisibility(View.VISIBLE);
        passwordRepeatContainer.setVisibility(View.VISIBLE);
        nameBoxContainer.setVisibility(View.VISIBLE);
        phoneBoxContainer.setVisibility(View.VISIBLE);
        hideSoftKeyboard();
    }

    public void signinRequest(View view) {
        header_container.setVisibility(View.GONE);
        custom_title_bar.setVisibility(View.VISIBLE);
        pre_form.setVisibility(View.GONE);
        mLoginFormView.setVisibility(View.VISIBLE);
        nameBox.setVisibility(View.GONE);
        phoneBox.setVisibility(View.GONE);
        mEmailSignInButton.setVisibility(View.VISIBLE);
        signup_button.setVisibility(View.GONE);
        register_button.setVisibility(View.VISIBLE);
        signin_button.setVisibility(View.GONE);
        passwordRepeat.setVisibility(View.GONE);
        role_container.setVisibility(View.GONE);
        wh_container.setVisibility(View.GONE);
        passwordRepeatContainer.setVisibility(View.GONE);
        nameBoxContainer.setVisibility(View.GONE);
        phoneBoxContainer.setVisibility(View.GONE);
    }

    public void backRequest(View view) {
        header_container.setVisibility(View.VISIBLE);
        custom_title_bar.setVisibility(View.GONE);
        pre_form.setVisibility(View.VISIBLE);
        mLoginFormView.setVisibility(View.GONE);
    }

    private void attemptRegister() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String password_repeat = passwordRepeat.getText().toString();
        String full_name = nameBox.getText().toString();
        String phone = phoneBox.getText().toString();
        int role_id = role_group.getCheckedRadioButtonId();
        if (role_id == R.id.role_cashier) {
            is_cashier = true;
        } else {
            is_cashier = false;
        }
        String warehouse_name = available_warehouse.getSelectedItem().toString();
        if (warehouse_ids.containsKey(warehouse_name)) {
            warehouse_id = Integer.parseInt(warehouse_ids.get(warehouse_name));
        }

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        if (!TextUtils.isEmpty(password_repeat) && !isPasswordValid(password_repeat)) {
            passwordRepeat.setError(getString(R.string.error_invalid_password));
            focusView = passwordRepeat;
            cancel = true;
        }

        if (!password_repeat.equals(password)) {
            passwordRepeat.setError(getString(R.string.error_invalid_password_repeat));
            focusView = passwordRepeat;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        } else {
            ContentValues adminData = ProfileController.getInstance().getDataByEmail(email);
            if (adminData != null) {
                mEmailView.setError(getString(R.string.error_unavailable_email));
                focusView = mEmailView;
                cancel = true;
            }
        }

        if (TextUtils.isEmpty(full_name)) {
            nameBox.setError(getString(R.string.error_field_required));
            focusView = nameBox;
            cancel = true;
        }

        if (TextUtils.isEmpty(phone)) {
            phoneBox.setError(getString(R.string.error_field_required));
            focusView = phoneBox;
            cancel = true;
        }

        if (cancel) {
            focusView.requestFocus();
        } else {
            showProgress(true);

            ContentValues content = new ContentValues();
            content.put("username", email);
            content.put("email", email);
            content.put("name", full_name);
            content.put("password", password);
            content.put("password_repeat", password_repeat);
            content.put("phone", phone);
            int group_id = 5;
            if (!is_cashier) {
                group_id = 4;
            }
            content.put("group_id", ""+group_id);
            content.put("date_added", DateTimeStrategy.getCurrentTime());

            if (conMgr.getActiveNetworkInfo() != null
                    && conMgr.getActiveNetworkInfo().isAvailable()
                    && conMgr.getActiveNetworkInfo().isConnected()) {
                if (password.equals(password_repeat)) {
                    try {
                        checkRegister(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Konfirmasi password harus sama dengan password.", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            }

            /*
                int id = ProfileController.getInstance().register(content);
                if (id > 0) {
                    addRoleParams();

                    mAuthTask = new UserLoginTask(email, password);
                    mAuthTask.execute((Void) null);
                }*/
        }
    }

    private void doRegisterOnLocal(ContentValues content) {
        content.remove("password_repeat");
        content.remove("username");
        content.remove("group_id");

        int id = ProfileController.getInstance().register(content);
        if (id > 0) {
            register_admin_id = id;
            addRoleParams();

            mAuthTask = new UserLoginTask(content.getAsString("email"), content.getAsString("password"));
            mAuthTask.execute((Void) null);
        }
    }

    private void checkLogin(final String username, final String password) {
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);
                    Log.e(getClass().getSimpleName(), "jObj : "+ jObj.toString());

                    // Check for error node in json
                    if (success == 1) {
                        String username = jObj.getString(TAG_USERNAME);
                        String id = jObj.getString(TAG_ID);
                        String name = jObj.getString(TAG_NAME);
                        String email = jObj.getString(TAG_EMAIL);
                        String phone = jObj.getString(TAG_PHONE);
                        int group_id = jObj.getInt("group_id");

                        // update the params first
                        Params admin_id = paramCatalog.getParamByName("admin_id");
                        if (admin_id instanceof Params) {
                            if (admin_id.getValue() != id) {
                                admin_id.setValue(id);
                                Boolean save_admin_id = paramCatalog.editParam(admin_id);
                            }
                        } else {
                            Boolean save_admin_id = paramCatalog.addParam(
                                    "admin_id",
                                    id,
                                    "text",
                                    "Admin id on server"
                            );
                        }

                        // insert the admin data if empty
                        ContentValues content = new ContentValues();
                        content.put("email", email);
                        content.put("name", name);
                        content.put("password", password);
                        content.put("phone", phone);
                        content.put("server_admin_id", id);
                        content.put("server_group_id", group_id);
                        content.put("date_added", DateTimeStrategy.getCurrentTime());
                        int new_id = ProfileController.getInstance().register(content);
                        if (new_id > 0) {
                            if (group_id != 5) {
                                is_cashier = true;
                            }
                            addRoleParams();
                        }

                        // get the roles form server if any
                        try {
                            if (jObj.get("roles") != null || jObj.get("roles") != "false" || jObj.getBoolean("roles") == false) {
                                JSONObject roles = jObj.getJSONObject("roles");
                                if (roles.length() > 0) {
                                    Log.e(getClass().getSimpleName(), "roles : " + roles.toString());
                                    Iterator<String> iter = roles.keys();
                                    while (iter.hasNext()) {
                                        String key = iter.next();
                                        try {
                                            AdminInWarehouse aiw = adminInWarehouseCatalog.getDataByAdminAndWH(Integer.parseInt(id), Integer.parseInt(key));
                                            if (aiw == null) {
                                                Boolean save = adminInWarehouseCatalog.addAdminInWarehouse(Integer.parseInt(id), Integer.parseInt(key), 1);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                        Toast.makeText(getApplicationContext(), jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                        // menyimpan login ke session
                        SharedPreferences.Editor editor = sharedpreferences.edit();
                        editor.putBoolean(session_status, true);
                        editor.putString(TAG_ID, id);
                        editor.putString(TAG_USERNAME, username);
                        editor.putString(TAG_NAME, name);
                        editor.putString(TAG_EMAIL, email);
                        editor.putString(TAG_PHONE, phone);
                        editor.commit();

                        // Memanggil main activity
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra(TAG_ID, id);
                        intent.putExtra(TAG_USERNAME, username);
                        intent.putExtra(TAG_NAME, name);
                        intent.putExtra(TAG_EMAIL, email);
                        intent.putExtra(TAG_PHONE, phone);
                        finish();
                        startActivity(intent);
                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void checkRegister(final ContentValues content) {
        Log.e(getClass().getSimpleName(), "content values : "+ content.toString());

        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        pDialog.setMessage("Register ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST, url_register, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.e(TAG, "Register Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    success = jObj.getInt(TAG_SUCCESS);

                    // Check for error node in json
                    if (success == 1) {
                        Log.e("Successfully Register!", jObj.toString());
                        success_register = true;
                        content.put("server_admin_id", jObj.getString(TAG_ID));
                        content.put("server_group_id", jObj.getString("group_id"));
                        doRegisterOnLocal(content);

                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getApplicationContext(),
                                jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Signup Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();

                hideDialog();

            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to register url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", content.getAsString("username"));
                params.put("password", content.getAsString("password"));
                params.put("confirm_password", content.getAsString("password_repeat"));
                params.put("name", content.getAsString("name"));
                params.put("email", content.getAsString("email"));
                params.put("status", "1");
                params.put("group_id", content.getAsString("group_id"));
                Log.e(getClass().getSimpleName(), "params to be submited : "+ params.toString());

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_json_obj);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void addRoleParams() {
        String role = "cashier";
        if (!is_cashier) {
            role = "cs";
        }
        Params prole = paramCatalog.getParamByName("role");
        if (prole instanceof Params) {
            if (prole.getValue() != role) {
                prole.setValue(role);
                Boolean save = paramCatalog.editParam(prole);
            }
        } else {
            Boolean save = paramCatalog.addParam(
                    "role",
                    role,
                    "text",
                    "Admin role for apps"
            );
        }

        if (warehouse_id > 0) {
            try {
                Params pWarehouseId = paramCatalog.getParamByName("warehouse_id");
                if (pWarehouseId instanceof Params) {
                    pWarehouseId.setValue(warehouse_id + "");
                    Boolean saveParam = paramCatalog.editParam(pWarehouseId);
                } else {
                    Boolean saveParam = paramCatalog.addParam("warehouse_id", warehouse_id + "", "text", warehouse_names.get(warehouse_id));
                }

                if (register_admin_id > 0) {
                    AdminInWarehouse aiw = adminInWarehouseCatalog.getDataByAdminAndWH(register_admin_id, warehouse_id);
                    if (aiw == null) {
                        Boolean save = adminInWarehouseCatalog.addAdminInWarehouse(register_admin_id, warehouse_id, 1);
                    }
                }
            } catch (Exception e){e.printStackTrace();}
        }
    }

    private BottomSheetDialog bottomSheetDialog;

    public void changeLanguageRequest(View view) {
        bottomSheetDialog = new BottomSheetDialog(LoginActivity.this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_language, null);
        bottomSheetDialog.setContentView(sheetView);

        bottomSheetDialog.show();

        triggerBottomDialogButton(sheetView);
    }

    private void triggerBottomDialogButton(View view) {
        LinearLayout lang_id_container = (LinearLayout) view.findViewById(R.id.lang_id_container);
        LinearLayout lang_en_container = (LinearLayout) view.findViewById(R.id.lang_en_container);
        ImageButton btn_close_sheet = (ImageButton) view.findViewById(R.id.btn_close_sheet);

        final ImageView lang_id_checked = (ImageView) view.findViewById(R.id.lang_id_checked);
        final ImageView lang_en_checked = (ImageView) view.findViewById(R.id.lang_en_checked);

        if (current_lang.equals("id")) {
            lang_id_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_green_24dp));
        } else if (current_lang.equals("en")) {
            lang_en_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_green_24dp));
        }

        lang_id_container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lang_id_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_green_24dp));
                lang_en_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_grey_24dp));
                try {
                    setLanguage("id");
                } catch (Exception e){e.printStackTrace();}
            }
        });

        lang_en_container.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                lang_id_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_grey_24dp));
                lang_en_checked.setImageDrawable(getDrawable(R.drawable.ic_check_circle_green_24dp));
                try {
                    setLanguage("en");
                } catch (Exception e){e.printStackTrace();}
            }
        });

        btn_close_sheet.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetDialog.dismiss();
            }
        });
    }

    private void setLanguage(String localeString) {
        Locale locale = new Locale(localeString);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        LanguageController.getInstance().setLanguage(localeString);

        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        bottomSheetDialog.dismiss();

        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }
}

