package com.slightsite.app.domain.shipping;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.slightsite.app.R;
import com.slightsite.app.ui.sale.CheckoutActivity;

public class CargoListDialog extends Dialog implements View.OnClickListener {

    private ListView list;
    private EditText filterText = null;
    ArrayAdapter<String> adapter = null;
    private static final String TAG = "CargoList";

    public CargoListDialog(final Context context, final View editText, String[] cityList) {
        super(context);

        setContentView(R.layout.listview_cargo);
        this.setTitle("Select Location");
        filterText = (EditText) findViewById(R.id.EditBox);
        filterText.addTextChangedListener(filterTextWatcher);
        list = (ListView) findViewById(R.id.List);
        adapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, cityList);
        list.setAdapter(adapter);
        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> a, View v, int position, long id) {
                //Log.d(TAG, "Selected Item is = "+list.getItemAtPosition(position));
                ((EditText) editText).setText(list.getItemAtPosition(position).toString());
                try {
                    ((CheckoutActivity)context).setCargoLocation(list.getItemAtPosition(position).toString());
                    onBackPressed();
                } catch (Exception e){e.printStackTrace();}
            }
        });
    }
    @Override
    public void onClick(View v) {

    }
    private TextWatcher filterTextWatcher = new TextWatcher() {

        public void afterTextChanged(Editable s) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count,
                                      int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before,
                                  int count) {
            adapter.getFilter().filter(s);
        }
    };
    @Override
    public void onStop(){
        filterText.removeTextChangedListener(filterTextWatcher);
    }
}