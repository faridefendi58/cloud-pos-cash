package com.slightsite.app.ui.fee;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.techicalservices.Tools;

public class FeeOnDateActivity extends AppCompatActivity {

    private String date_fee = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fee_on_date);

        Intent intent = getIntent();
        if (intent.hasExtra("date")) {
            date_fee = intent.getExtras().get("date").toString();
        }

        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.grey_60), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.invoice_detail));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Tools.setSystemBarColor(this, R.color.greenUcok);
        Tools.setSystemBarLight(this);

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        if (date_fee.length() > 0) {
            toolbar_title.setText("Detail Fee On "+ DateTimeStrategy.parseDate(date_fee, "dd MMM yyyy"));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
