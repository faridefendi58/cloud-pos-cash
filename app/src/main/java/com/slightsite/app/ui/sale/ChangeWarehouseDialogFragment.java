package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.domain.warehouse.AdminInWarehouseCatalog;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@SuppressLint("ValidFragment")
public class ChangeWarehouseDialogFragment extends DialogFragment {

    private WarehouseCatalog warehouseCatalog;
    private ParamCatalog paramCatalog;
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private Button confirmButton;
    private Button clearButton;
    private UpdatableFragment fragment;
    private Resources res;
    private Spinner available_warehouse;
    private ArrayList<String> warehouse_items = new ArrayList<String>();
    private ArrayList<Integer> allowed_warehouses = new ArrayList<Integer>();
    private HashMap<String, String> warehouse_ids = new HashMap<String, String>();
    private List<Warehouses> warehousesList;
    private int selected_wh = 0;
    private String selected_wh_name;
    private String wh_id;
    private String admin_id;

    /**
     * Construct a new AddProductDialogFragment
     * @param fragment
     */
    public ChangeWarehouseDialogFragment(UpdatableFragment fragment) {

        super();
        this.fragment = fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        try {
            paramCatalog = ParamService.getInstance().getParamCatalog();
            wh_id = paramCatalog.getParamByName("warehouse_id").getValue();
            warehouseCatalog = WarehouseService.getInstance().getWarehouseCatalog();
            adminInWarehouseCatalog = AdminInWarehouseService.getInstance().getAdminInWarehouseCatalog();
            admin_id = paramCatalog.getParamByName("admin_id").getValue();
            if (admin_id != null) {
                List<AdminInWarehouse> adminInWarehouses = adminInWarehouseCatalog.getDataByAdminId(Integer.parseInt(admin_id));
                if (adminInWarehouses != null) {
                    Log.e(getTag(), "adminInWarehouses : " + adminInWarehouses.toArray().toString());
                } else {
                    Log.e(getTag(), "no admin data in any wh");
                }
                if (adminInWarehouses != null && adminInWarehouses.size() > 0) {
                    for (AdminInWarehouse aiw : adminInWarehouses) {
                        allowed_warehouses.add(aiw.getWarehouseId());
                    }
                }
            }
            Log.e(getTag(), "admin_id : "+ admin_id);
            Log.e(getTag(), "allowed_warehouses : "+ allowed_warehouses.toString());

            warehousesList = warehouseCatalog.getAllWarehouses();
            if (allowed_warehouses.size() > 0) {
                for (int n = 0; n < warehousesList.size(); n++) {
                    Warehouses wh = warehousesList.get(n);
                    if (allowed_warehouses.contains(wh.getWarehouseId())) {
                        warehouse_items.add(wh.getTitle());
                        warehouse_ids.put(wh.getTitle(), wh.getWarehouseId() + "");
                        if (wh.getWarehouseId() == Integer.parseInt(wh_id)) {
                            selected_wh_name = wh.getTitle();
                        }
                    }
                }
                if (selected_wh_name != null) {
                    selected_wh = warehouse_items.indexOf(selected_wh_name);
                }
            } else {
                for (int n = 0; n < warehousesList.size(); n++) {
                    Warehouses wh = warehousesList.get(n);
                    warehouse_items.add(wh.getTitle());
                    warehouse_ids.put(wh.getTitle(), wh.getWarehouseId() +"");
                    if (wh.getWarehouseId() == Integer.parseInt(wh_id)) {
                        selected_wh = n;
                        selected_wh_name = wh.getTitle();
                    }
                }
            }

            Log.e(getTag(), "warehouse_items : "+ warehouse_items.toString());
            Log.e(getTag(), "warehouse_ids : "+ warehouse_ids.toString());
            Log.e(getTag(), "selected_wh : "+ selected_wh);
            Log.e(getTag(), "selected_wh_name : "+ selected_wh_name);
        } catch (NoDaoSetException e) {
            e.printStackTrace();
        }

        View v = inflater.inflate(R.layout.layout_change_warehouse, container,
                false);

        res = getResources();

        available_warehouse = (Spinner) v.findViewById(R.id.available_warehouse);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        clearButton = (Button) v.findViewById(R.id.clearButton);

        getDialog().getWindow().setTitle(res.getString(R.string.title_change_warehouse));

        initUI();
        return v;
    }

    /**
     * Construct a new 
     */
    private void initUI() {

        ArrayAdapter<String> whAdapter = new ArrayAdapter<String>(
                getActivity().getBaseContext(),
                R.layout.spinner_item, warehouse_items);
        whAdapter.notifyDataSetChanged();
        available_warehouse.setAdapter(whAdapter);
        available_warehouse.setSelection(selected_wh);

        confirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (available_warehouse.getSelectedItem() == selected_wh_name) {
                    Toast.makeText(getActivity().getBaseContext(), selected_wh_name +" is your current data. Please choose the other one!",
                            Toast.LENGTH_LONG).show();
                } else {
                    // starting to do updating the data
                    Toast.makeText(getActivity().getBaseContext(), "Your data is successfully updated to "+ available_warehouse.getSelectedItem(),
                            Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
            }
        });

        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ChangeWarehouseDialogFragment.this.dismiss();
            }
        });
    }
}
