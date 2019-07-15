package com.slightsite.app.ui.sale;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentValues;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.slightsite.app.R;
import com.slightsite.app.domain.AppController;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.domain.inventory.Inventory;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.inventory.ProductCatalog;
import com.slightsite.app.domain.inventory.Stock;
import com.slightsite.app.domain.params.ParamCatalog;
import com.slightsite.app.domain.params.ParamService;
import com.slightsite.app.domain.params.Params;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.warehouse.AdminInWarehouse;
import com.slightsite.app.domain.warehouse.AdminInWarehouseCatalog;
import com.slightsite.app.domain.warehouse.AdminInWarehouseService;
import com.slightsite.app.domain.warehouse.WarehouseCatalog;
import com.slightsite.app.domain.warehouse.WarehouseService;
import com.slightsite.app.domain.warehouse.Warehouses;
import com.slightsite.app.techicalservices.NoDaoSetException;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@SuppressLint("ValidFragment")
public class ChangeWarehouseDialogFragment extends DialogFragment {

    private WarehouseCatalog warehouseCatalog;
    private ParamCatalog paramCatalog;
    private AdminInWarehouseCatalog adminInWarehouseCatalog;
    private ProductCatalog productCatalog;
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

    private Register register;
    private Stock stock;

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
            register = Register.getInstance();
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
                        if (aiw.getWarehouseId() > 0) {
                            allowed_warehouses.add(aiw.getWarehouseId());
                        }
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

            productCatalog = Inventory.getInstance().getProductCatalog();
            stock = Inventory.getInstance().getStock();
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
                    if (register.hasSale()) {
                        try {
                            if (!register.getCurrentSale().getStatus().equals("ENDED")) {
                                register.cancleSale();
                            } else {
                                register.setCurrentSale(0);
                            }
                            ((MainActivity) getActivity()).updateInventoryFragment();
                            ((MainActivity) getActivity()).updateSaleFragment();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // starting to do updating the data
                    String wh_choosen = available_warehouse.getSelectedItem().toString();
                    String wh_choosen_id = warehouse_ids.get(wh_choosen);
                    try {
                        insert_new_product(wh_choosen_id, wh_choosen);
                    } catch (Exception e){
                        e.printStackTrace();
                    }
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

    private void insert_new_product(final String warehouse_id, final String warehouse_name) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("simply", "1");
        params.put("with_discount", "1");
        params.put("warehouse_id", warehouse_id);
        params.put("with_stock", "1");

        final ArrayList<String> items = new ArrayList<String>();
        items.add("-");

        final List<Product> current_products = productCatalog.getAllProduct();

        String url = Server.URL + "product/list?api-key=" + Server.API_KEY;
        _string_request(Request.Method.GET, url, params, false,
                new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        try {
                            JSONObject jObj = new JSONObject(result);
                            int success = jObj.getInt("success");
                            // Check for error node in json
                            if (success == 1) {
                                JSONArray data = jObj.getJSONArray("data");

                                if (data.length() > 0) {
                                    try {
                                        // suspending the old product
                                        //productCatalog.suspendAllProduct();
                                        Log.e(getTag(), "product list after suspend :"+ productCatalog.getAllProduct().toArray().toString());
                                        for(Product pd : current_products) {
                                            productCatalog.suspendProduct(pd);
                                        }
                                    } catch (Exception e){e.printStackTrace();}
                                }

                                ArrayList<String> product_items = new ArrayList<String>();
                                HashMap< Integer, Integer> stocks = new HashMap< Integer, Integer>();
                                for(int n = 0; n < data.length(); n++)
                                {
                                    JSONObject data_n = data.getJSONObject(n);
                                    JSONObject config = data_n.getJSONObject("config");

                                    items.add(data_n.getString("title"));
                                    product_items.add(data_n.getString("id"));
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(data_n.getString("id"));
                                    } catch (Exception e) {}
                                    if (pd == null) {
                                        try {
                                            if (config.has("image")) {
                                                String image = config.getString("image");
                                                productCatalog.addProduct2(
                                                        data_n.getString("title"),
                                                        data_n.getString("id"),
                                                        Double.parseDouble(data_n.getString("price")),
                                                        data_n.getInt("priority"),
                                                        image);
                                            } else {
                                                productCatalog.addProduct(
                                                        data_n.getString("title"),
                                                        data_n.getString("id"),
                                                        Double.parseDouble(data_n.getString("price")));
                                            }
                                        } catch (Exception e) {
                                            Log.e(getTag(), e.getMessage());
                                        }
                                    } else {
                                        try {
                                            pd.setName(data_n.getString("title"));
                                            pd.setBarcode(data_n.getString("id"));
                                            pd.setUnitPrice(Double.parseDouble(data_n.getString("price")));
                                            //Log.e(getClass().getSimpleName(), "config.has(\"image\") :"+ config.has("image"));
                                            if (config.has("image")) {
                                                pd.setImage(config.getString("image"));
                                            }
                                            //Log.e(getClass().getSimpleName(), "Priority : "+ data_n.getString("priority"));
                                            pd.setPriority(data_n.getInt("priority"));
                                            pd.setStatus("ACTIVE");

                                            productCatalog.editProduct(pd);
                                        } catch (Exception e) {}
                                    }

                                    stocks.put(data_n.getInt("id"), data_n.getInt("stock"));
                                }
                                // clear the discount data
                                stock.clearProductDiscount();
                                Log.e(getTag(), "Clearing product discount");

                                JSONArray discounts = jObj.getJSONArray("discount");
                                for(int p = 0; p < data.length(); p++)
                                {
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(product_items.get(p));
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    JSONArray discount_items = discounts.getJSONArray(p);
                                    if (discount_items.length() > 0 && pd != null) {
                                        for(int q = 0; q < discount_items.length(); q++)
                                        {
                                            JSONObject data_q = discount_items.getJSONObject(q);
                                            try {
                                                ContentValues disc = stock.getDiscountDataByQuantity(Integer.parseInt(product_items.get(p)),data_q.getInt("quantity"));
                                                if (disc != null) {
                                                    stock.updateProductDiscount(
                                                            disc.getAsInteger("_id"),
                                                            Integer.parseInt(data_q.getString("quantity")),
                                                            Integer.parseInt(data_q.getString("quantity_max")),
                                                            Double.parseDouble(data_q.getString("price")));
                                                } else {
                                                    stock.addProductDiscount(
                                                            DateTimeStrategy.getCurrentTime(),
                                                            Integer.parseInt(data_q.getString("quantity")),
                                                            Integer.parseInt(data_q.getString("quantity_max")),
                                                            pd,
                                                            Double.parseDouble(data_q.getString("price")));
                                                }
                                            } catch (Exception e) {
                                                Log.e(getTag(), e.getMessage());
                                            }
                                        }
                                    }
                                }

                                // clear the discount data
                                stock.clearStock();
                                Log.e(getTag(), "Clearing all the stock");

                                // also insert the stock
                                for (Map.Entry<Integer, Integer> entry : stocks.entrySet()) {
                                    Product pd = null;
                                    try {
                                        pd = productCatalog.getProductByBarcode(entry.getKey().toString());
                                        List lot = stock.getProductLotByProductId(entry.getKey());
                                        if (lot.size() > 0) {
                                            stock.updateStockSum(entry.getKey(), entry.getValue());
                                        } else {
                                            stock.addProductLot(
                                                    DateTimeStrategy.getCurrentTime(),
                                                    entry.getValue(),
                                                    pd,
                                                    pd.getUnitPrice());
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }

                                try {
                                    Params pWarehouseId = paramCatalog.getParamByName("warehouse_id");
                                    if (pWarehouseId instanceof Params) {
                                        pWarehouseId.setValue(warehouse_id);
                                        Boolean saveParam = paramCatalog.editParam(pWarehouseId);
                                    } else {
                                        Boolean saveParam = paramCatalog.addParam("warehouse_id", warehouse_id, "text", warehouse_name);
                                    }
                                } catch (Exception e) { e.printStackTrace(); }

                                Toast.makeText(getActivity().getBaseContext(), "Your data is successfully updated to "+ available_warehouse.getSelectedItem(),
                                        Toast.LENGTH_LONG).show();

                                Intent intent = new Intent(getActivity(), MainActivity.class);
                                startActivity(intent);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    public interface VolleyCallback {
        void onSuccess(String result);
    }

    private void _string_request(int method, String url, final Map params, final Boolean show_dialog, final VolleyCallback callback) {

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
                Toast.makeText(getContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
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
            Log.e(getClass().getSimpleName(), e.getMessage());
        }
    }
}
