package com.slightsite.app.techicalservices;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.ui.sale.ShippingFragment;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class AutoCompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private ArrayList<String> data;
    private String server = Server.URL +"customer/list?api-key="+ Server.API_KEY +"&limit=10";

    private ShippingFragment shippingFragment;
    private HashMap<String, Customer> customers = new HashMap<String, Customer>();
    private String delimeter = "name";

    public AutoCompleteAdapter(@NonNull Context context, @LayoutRes int resource) {
        super(context, resource);
        this.data = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return data.size();
    }

    @Nullable
    @Override
    public String getItem(int position) {
        return data.get(position);
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null && constraint.length() >= 2) {
                    HttpURLConnection conn = null;
                    InputStream input = null;
                    try {
                        URL url = new URL(server + "&"+ delimeter + "=" + constraint.toString());
                        conn = (HttpURLConnection) url.openConnection();
                        input = conn.getInputStream();
                        InputStreamReader reader = new InputStreamReader(input, "UTF-8");
                        BufferedReader buffer = new BufferedReader(reader, 8192);
                        StringBuilder builder = new StringBuilder();
                        String line;
                        while ((line = buffer.readLine()) != null) {
                            builder.append(line);
                        }
                        JSONObject jsonObject = new JSONObject(builder.toString());
                        ArrayList<String> suggestions = new ArrayList<>();
                        int success = jsonObject.getInt("success");
                        if (success > 0) {
                            JSONArray terms = new JSONArray(jsonObject.getString("data"));
                            for (int ind = 0; ind < terms.length(); ind++) {
                                JSONObject data_n = terms.getJSONObject(ind);
                                String term = data_n.getString("name") + " - " + data_n.getString("telephone");
                                if (delimeter == "telephone") {
                                    term = data_n.getString("telephone");
                                }
                                suggestions.add(term);
                                Customer customer = new Customer(
                                        "-", "-", "-", "-", 0
                                );

                                customer.setName(data_n.getString("name"));
                                customer.setEmail(data_n.getString("email"));
                                customer.setPhone(data_n.getString("telephone"));
                                if (data_n.getString("address") != null && data_n.getString("address") != "null" && !data_n.getString("address").equals(null)) {
                                    customer.setAddress(data_n.getString("address"));
                                } else {
                                    customer.setAddress("-");
                                }

                                customer.setStatus(data_n.getInt("status"));
                                customer.setServerCustomerId(data_n.getInt("id"));

                                customers.put(term, customer);
                            }

                            shippingFragment.setCustomerHashMap(customers);
                        }

                        results.values = suggestions;
                        results.count = suggestions.size();
                        data = suggestions;
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    } finally {
                        if (input != null) {
                            try {
                                input.close();
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                        if (conn != null) conn.disconnect();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else notifyDataSetInvalidated();
            }
        };
    }

    public void setShippingFragment(ShippingFragment fragment) {
        this.shippingFragment = fragment;
    }

    public void setDelimeter(String delimeter) {
        this.delimeter = delimeter;
    }
}
