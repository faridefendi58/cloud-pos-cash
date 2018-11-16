package com.slightsite.app.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v4.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.slightsite.app.R;
import com.slightsite.app.ui.dashboard.DashboardFragment;
import com.slightsite.app.ui.dashboard.ReportFragment;
import com.slightsite.app.ui.params.ParamsActivity;
import com.slightsite.app.ui.profile.ProfileActivity;

public class DashboardActivity extends AppCompatActivity
        implements BottomNavigationView.OnNavigationItemSelectedListener {

    private android.support.v7.app.ActionBar actionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //loading the default fragment
        loadFragment(new DashboardFragment());

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(this);

        actionBar = getSupportActionBar();
        actionBar.setTitle(getResources().getString(R.string.title_home));
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(getResources().getDrawable(R.drawable.ic_launcher));
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        Fragment fragment = null;

        switch (item.getItemId()) {
            case R.id.navigation_home:
                fragment = new DashboardFragment();
                actionBar.setTitle(getResources().getString(R.string.title_home));
                break;

            case R.id.navigation_transaction:
                Intent intent = new Intent(DashboardActivity.this, MainActivity.class);
                finish();
                startActivity(intent);
                break;

            case R.id.navigation_reports:
                fragment = new ReportFragment();
                actionBar.setTitle(getResources().getString(R.string.title_reports));
                break;
        }

        return loadFragment(fragment);
    }

    private boolean loadFragment(Fragment fragment) {
        //switching fragment
        if (fragment != null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
            return true;
        }
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        MenuItem item = menu.findItem(R.id.navigation_home);
        item.setVisible(false);

        MenuItem language = menu.findItem(R.id.language);
        language.setVisible(false);

        MenuItem currency = menu.findItem(R.id.currency);
        currency.setVisible(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.params:
                Intent newActivity = new Intent(DashboardActivity.this,
                        ParamsActivity.class);
                startActivity(newActivity);
                return true;
            case R.id.navigation_home:
                intent = new Intent(DashboardActivity.this, DashboardActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.logout:
                SharedPreferences sharedpreferences = getSharedPreferences(LoginActivity.my_shared_preferences, Context.MODE_PRIVATE);

                SharedPreferences.Editor editor = sharedpreferences.edit();
                editor.putBoolean(LoginActivity.session_status, false);
                editor.putString(LoginActivity.TAG_ID, null);
                editor.putString(LoginActivity.TAG_EMAIL, null);
                editor.commit();

                intent = new Intent(DashboardActivity.this, LoginActivity.class);
                finish();
                startActivity(intent);
                return true;
            case R.id.profile:
                intent = new Intent(DashboardActivity.this, ProfileActivity.class);
                finish();
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
