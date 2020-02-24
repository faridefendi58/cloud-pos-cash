package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.warehouse.Warehouses;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class StockOptionsDialog extends DialogFragment {

    private Context context;

    private EditText master_option;
    private TextView dialog_title;
    private HashMap<String, List<Warehouses>> listData = new HashMap<String, List<Warehouses>>();
    private LinearLayout warehouse_option_container;
    private LinearLayout expedition_option_container;
    private LinearLayout production_option_container;
    private LinearLayout nontransaction_option_container;
    private LinearLayout supplier_option_container;

    private RadioGroup radioWarehouse;
    private RadioGroup radioExpedition;
    private RadioGroup radioProduction;
    private RadioGroup radioNonTransaction;
    private RadioGroup radioSupplier;

    private Boolean is_stock_in = true;
    private Boolean is_stock_out = false;

    private Button confirmButton;
    private int selected_warehouse_id = -1;

    public StockOptionsDialog(Context context, EditText editText) {
        super();
        this.context = context;
        this.master_option = editText;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.dialog_stock_options, container, false);

        dialog_title = (TextView) v.findViewById(R.id.dialog_title);
        dialog_title.setText(getArguments().getString("title"));
        confirmButton = (Button) v.findViewById(R.id.confirmButton);

        warehouse_option_container = (LinearLayout) v.findViewById(R.id.warehouse_option_container);
        expedition_option_container = (LinearLayout) v.findViewById(R.id.expedition_option_container);
        production_option_container = (LinearLayout) v.findViewById(R.id.production_option_container);
        nontransaction_option_container = (LinearLayout) v.findViewById(R.id.nontransaction_option_container);
        supplier_option_container = (LinearLayout) v.findViewById(R.id.supplier_option_container);

        radioWarehouse = (RadioGroup) v.findViewById(R.id.radioWarehouse);
        radioExpedition = (RadioGroup) v.findViewById(R.id.radioExpedition);
        radioProduction = (RadioGroup) v.findViewById(R.id.radioProduction);
        radioNonTransaction = (RadioGroup) v.findViewById(R.id.radioNonTransaction);
        radioSupplier = (RadioGroup) v.findViewById(R.id.radioSupplier);

        buildRadioButton(v);
        initAction();
        return v;
    }

    private void initAction(){
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                end();
            }
        });
    }

    private void end(){
        this.dismiss();
    }

    private HashMap<String, Integer> warehouse_ids = new HashMap<String, Integer>();
    private RadioGroup activeRadio;
    private void buildRadioButton(final View view) {
        if (listData.containsKey("warehouse")) {
            warehouse_option_container.setVisibility(View.VISIBLE);
            ArrayList<String> stringArrayList = new ArrayList<String>();
            Boolean is_wh_selected = false;
            for (Warehouses wh : listData.get("warehouse")) {
                stringArrayList.add(wh.getTitle());
                warehouse_ids.put(wh.getTitle(), wh.getWarehouseId());
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(wh.getTitle());
                radioButton.setId(wh.getWarehouseId());
                if (selected_warehouse_id == wh.getWarehouseId()) {
                    radioButton.setChecked(true);
                    is_wh_selected = true;
                }
                radioWarehouse.addView(radioButton);
            }
            if (is_wh_selected) {
                activeRadio = radioWarehouse;
            }
            radioWarehouse.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (activeRadio != null) {
                        if (!activeRadio.equals(radioWarehouse)) {
                            removeActiveRadio(view);
                        } else {
                            RadioButton radioBtn1 = (RadioButton) view.findViewById(selected_warehouse_id);
                            radioBtn1.setChecked(false);
                        }
                    }
                    int checkedRadioButtonId = radioWarehouse.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    activeRadio = radioWarehouse;
                    //Toast.makeText(context, radioBtn.getText(), Toast.LENGTH_SHORT).show();
                    try {
                        master_option.setText(radioBtn.getText());
                        ((PurchaseOrderActivity) context).setSelectedWH(warehouse_ids.get(radioBtn.getText().toString()), radioBtn.getText().toString());
                    } catch (Exception e){e.printStackTrace();}
                }
            });
        }
        if (listData.containsKey("expedition_truck")) {
            expedition_option_container.setVisibility(View.VISIBLE);
            ArrayList<String> stringArrayList = new ArrayList<String>();
            Boolean is_ex_selected = false;
            for (Warehouses wh : listData.get("expedition_truck")) {
                stringArrayList.add(wh.getTitle());
                warehouse_ids.put(wh.getTitle(), wh.getWarehouseId());
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(wh.getTitle());
                radioButton.setId(wh.getWarehouseId());
                if (selected_warehouse_id == wh.getWarehouseId()) {
                    radioButton.setChecked(true);
                    is_ex_selected = true;
                }
                radioExpedition.addView(radioButton);
            }
            if (is_ex_selected) {
                activeRadio = radioExpedition;
            }
            radioExpedition.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (activeRadio != null && !activeRadio.equals(radioExpedition)) {
                        if (!activeRadio.equals(radioExpedition)) {
                            removeActiveRadio(view);
                        } else {
                            if (selected_warehouse_id >= 0) {
                                RadioButton radioBtn1 = (RadioButton) view.findViewById(selected_warehouse_id);
                                radioBtn1.setChecked(false);
                            }
                        }
                    }
                    int checkedRadioButtonId = radioExpedition.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    activeRadio = radioExpedition;
                    //Toast.makeText(context, radioBtn.getText(), Toast.LENGTH_SHORT).show();
                    try {
                        master_option.setText(radioBtn.getText());
                        ((PurchaseOrderActivity) context).setSelectedWH(warehouse_ids.get(radioBtn.getText().toString()), radioBtn.getText().toString());
                    } catch (Exception e){e.printStackTrace();}
                }
            });
        }
        if (listData.containsKey("production")) {
            production_option_container.setVisibility(View.VISIBLE);
            ArrayList<String> stringArrayList = new ArrayList<String>();
            Boolean is_pro_selected = false;
            for (Warehouses wh : listData.get("production")) {
                stringArrayList.add(wh.getTitle());
                warehouse_ids.put(wh.getTitle(), wh.getWarehouseId());
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(wh.getTitle());
                radioButton.setId(wh.getWarehouseId());
                if (selected_warehouse_id == wh.getWarehouseId()) {
                    radioButton.setChecked(true);
                    is_pro_selected = true;
                }
                radioProduction.addView(radioButton);
            }
            if (is_pro_selected) {
                activeRadio = radioProduction;
            }
            radioProduction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (activeRadio != null) {
                        if (!activeRadio.equals(radioProduction)) {
                            removeActiveRadio(view);
                        } else {
                            if (selected_warehouse_id >= 0) {
                                RadioButton radioBtn1 = (RadioButton) view.findViewById(selected_warehouse_id);
                                radioBtn1.setChecked(false);
                            }
                        }
                    }
                    int checkedRadioButtonId = radioProduction.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    activeRadio = radioProduction;
                    //Toast.makeText(context, radioBtn.getText(), Toast.LENGTH_SHORT).show();
                    try {
                        master_option.setText(radioBtn.getText());
                        ((PurchaseOrderActivity) context).setSelectedWH(warehouse_ids.get(radioBtn.getText().toString()), radioBtn.getText().toString());
                    } catch (Exception e){e.printStackTrace();}
                }
            });
        }

        if (listData.containsKey("supplier")) {
            supplier_option_container.setVisibility(View.VISIBLE);
            ArrayList<String> stringArrayList = new ArrayList<String>();
            Boolean is_supl_selected = false;
            for (Warehouses wh : listData.get("supplier")) {
                stringArrayList.add(wh.getTitle());
                warehouse_ids.put(wh.getTitle(), wh.getWarehouseId());
                RadioButton radioButton = new RadioButton(context);
                radioButton.setText(wh.getTitle());
                radioButton.setId(wh.getWarehouseId());
                if (selected_warehouse_id == wh.getWarehouseId()) {
                    radioButton.setChecked(true);
                    is_supl_selected = true;
                }
                radioSupplier.addView(radioButton);
            }
            if (is_supl_selected) {
                activeRadio = radioSupplier;
            }
            radioSupplier.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (activeRadio != null) {
                        if (!activeRadio.equals(radioSupplier)) {
                            removeActiveRadio(view);
                        } else {
                            if (selected_warehouse_id >= 0) {
                                RadioButton radioBtn1 = (RadioButton) view.findViewById(selected_warehouse_id);
                                radioBtn1.setChecked(false);
                            }
                        }
                    }
                    int checkedRadioButtonId = radioSupplier.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    activeRadio = radioSupplier;
                    Toast.makeText(context, radioBtn.getText(), Toast.LENGTH_SHORT).show();
                    try {
                        master_option.setText(radioBtn.getText());
                        ((PurchaseOrderActivity) context).setSelectedWH(warehouse_ids.get(radioBtn.getText().toString()), radioBtn.getText().toString());
                    } catch (Exception e){e.printStackTrace();}
                }
            });
        }

        if (is_stock_out) {
            nontransaction_option_container.setVisibility(View.VISIBLE);
            Boolean is_non_selected = false;
            if (selected_warehouse_id == 0) {
                activeRadio = radioNonTransaction;
            }
            radioNonTransaction.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (activeRadio != null) {
                        if (!activeRadio.equals(radioNonTransaction)) {
                            removeActiveRadio(view);
                        } else {
                            if (selected_warehouse_id >= 0) {
                                RadioButton radioBtn1 = (RadioButton) view.findViewById(selected_warehouse_id);
                                if (radioBtn1 != null) {
                                    radioBtn1.setChecked(false);
                                }
                            }
                        }
                    }
                    int checkedRadioButtonId = radioNonTransaction.getCheckedRadioButtonId();
                    RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                    activeRadio = radioNonTransaction;
                    //Toast.makeText(context, radioBtn.getText(), Toast.LENGTH_SHORT).show();
                    try {
                        master_option.setText(radioBtn.getText());
                        ((PurchaseOrderActivity) context).setSelectedWH(0, radioBtn.getText().toString());
                    } catch (Exception e){e.printStackTrace();}
                }
            });
        }
    }

    public void setListData(HashMap<String, List<Warehouses>> listData) {
        this.listData = listData;
    }

    public void setIsStockIn() {
        this.is_stock_in = true;
        this.is_stock_out = false;
    }

    public void setIsStockOut() {
        this.is_stock_out = true;
        this.is_stock_in = false;
    }

    private void removeActiveRadio(View view) {
        if (activeRadio != null) {
            try {
                int checkedRadioButtonId = activeRadio.getCheckedRadioButtonId();
                RadioButton radioBtn = (RadioButton) view.findViewById(checkedRadioButtonId);
                radioBtn.setChecked(false);
            } catch (Exception e){e.printStackTrace();}
        }
    }

    public void setSelectedWarehouseId(int warehouseId) {
        this.selected_warehouse_id = warehouseId;
    }
}
