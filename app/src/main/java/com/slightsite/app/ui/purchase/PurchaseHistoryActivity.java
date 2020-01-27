package com.slightsite.app.ui.purchase;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

public class PurchaseHistoryActivity extends AppCompatActivity {

    private String issue_number;
    private Integer issue_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_purchase_order_history);

        Intent intent = getIntent();
        if (intent.hasExtra("issue_number") && intent.hasExtra("issue_id")) {
            issue_number = intent.getStringExtra("issue_number");
            issue_id = Integer.parseInt(intent.getStringExtra("issue_id"));
        }

        initToolbar();
    }

    private void initToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setNavigationIcon(R.drawable.ic_close);
        toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.colorWhite), PorterDuff.Mode.SRC_ATOP);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(getResources().getString(R.string.title_stock_purchase));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#019e47")));
        getSupportActionBar().setStackedBackgroundDrawable(new ColorDrawable(Color.parseColor("#e2e3e5")));

        TextView toolbar_title = (TextView) findViewById(R.id.toolbar_title);
        toolbar_title.setText(getResources().getString(R.string.title_stock_in_out_history));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            Intent intent = new Intent(PurchaseHistoryActivity.this, MainActivity.class);
                intent.putExtra("refreshStock", "1");
                finish();
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
