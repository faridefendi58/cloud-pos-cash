package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.techicalservices.DownloadImageTask;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.ui.MainActivity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListProductPurchase extends BaseAdapter {

    private Context context;
    private List<Product> items;
    private int resource;
    private MainActivity activity;
    private PurchaseFragment fragment;
    private Map<Integer, Integer> stacks = new HashMap<Integer, Integer>();
    private Boolean is_inventory_issue = false;

    private static LayoutInflater inflater = null;

    public AdapterListProductPurchase(MainActivity activity, List<Product> items, int resource, PurchaseFragment fragment) {
        // TODO Auto-generated constructor stub
        this.activity = activity;
        this.context = activity.getBaseContext();
        this.items = items;
        this.resource = resource;
        this.fragment = fragment;
        inflater = ( LayoutInflater )context.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return this.items.size();
    }

    @Override
    public Object getItem(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    @Override
    public long getItemId(int position) {
        // TODO Auto-generated method stub
        return position;
    }

    public class Holder
    {
        TextView name;
        TextView stock_counter;
        EditText quantity;
        ImageView product_image;
        LinearLayout add_qty_container;
        View rowView;
        Button addCartButton;
        TextView remove_item;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(this.resource, null);
        holder.name = (TextView) rowView.findViewById(R.id.name);
        holder.stock_counter = (TextView) rowView.findViewById(R.id.stock_counter);
        holder.quantity = (EditText) rowView.findViewById(R.id.purchase_quantity);
        holder.product_image = (ImageView) rowView.findViewById(R.id.product_image);
        holder.add_qty_container = (LinearLayout) rowView.findViewById(R.id.add_qty_container);
        holder.rowView = rowView;
        holder.addCartButton = (Button) rowView.findViewById(R.id.addCartButton);
        holder.remove_item = (TextView) rowView.findViewById(R.id.remove_item);

        holder.quantity.setSelectAllOnFocus(true);

        final Product p = items.get(position);
        Map<String, String> pmap = p.toMap();
        holder.name.setText(p.getName());
        holder.stock_counter.setText(pmap.get("availability"));
        if (p.getImage() != null) {
            if (activity.getImageStack(p.getId()) instanceof Bitmap) {
                holder.product_image.setImageBitmap(activity.getImageStack(p.getId()));
            } else {
                if (p.getImageBitmap() == null) {
                    DownloadImageTask downloadImageTask = new DownloadImageTask(holder.product_image);
                    downloadImageTask.setActivity(activity);
                    downloadImageTask.setProductId(p.getId());
                    downloadImageTask.execute(Server.BASE_API_URL + "" + p.getImage());
                } else {
                    holder.product_image.setImageBitmap(p.getImageBitmap());
                }
            }
        }
        if (p.getIsAvoidStock() > 0) {
            holder.stock_counter.setText(context.getResources().getString(R.string.available));
        }

        if (is_inventory_issue) {
            holder.addCartButton.setText(context.getResources().getString(R.string.button_substract));
            holder.addCartButton.setBackgroundColor(context.getResources().getColor(R.color.red_400));
        }
        stacks = fragment.getStacks();
        if (stacks.containsKey(p.getId())) {
            holder.quantity.setText(""+ stacks.get(p.getId()));
            holder.add_qty_container.setVisibility(View.VISIBLE);
            holder.addCartButton.setVisibility(View.GONE);
        } else {
            holder.quantity.setText("0");
            holder.add_qty_container.setVisibility(View.GONE);
            holder.addCartButton.setVisibility(View.VISIBLE);
        }

        holder.addCartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stacks.containsKey(p.getId())) {
                    holder.add_qty_container.setVisibility(View.VISIBLE);
                    holder.addCartButton.setVisibility(View.GONE);

                    holder.quantity.setText("1");
                } else {
                    holder.addCartButton.setVisibility(View.VISIBLE);
                    int tot_qty = stacks.get(p.getId()) + 1;
                    holder.quantity.setText(""+ tot_qty);
                }
            }
        });

        holder.remove_item.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    fragment.addSubstractTheCart(p, 0);
                    holder.add_qty_container.setVisibility(View.GONE);
                    holder.addCartButton.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.quantity.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            private String current_val;

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                try {
                    if (charSequence.length() > 0 && !charSequence.toString().equals(current_val)) {
                        current_val = charSequence.toString();
                        holder.quantity.removeTextChangedListener(this);
                        int _qty = Integer.parseInt(charSequence.toString());
                        if (_qty <= 0) {
                            holder.add_qty_container.setVisibility(View.GONE);
                            holder.addCartButton.setVisibility(View.VISIBLE);
                        }
                        holder.quantity.setText(_qty+"");
                        Log.e("CUK", "_qty onTextChanged : "+ _qty);
                        //holder.quantity.setSelection(charSequence.length());
                        holder.quantity.addTextChangedListener(this);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().length() > 0) {
                    int _qty = Integer.parseInt(editable.toString());
                    Log.e("CUK", "_qty afterTextChanged : "+ _qty);
                    fragment.addSubstractTheCart(p, _qty);
                }
            }
        });

        return rowView;
    }

    public void setIsInventoryIssue(Boolean is_inventory_issue) {
        this.is_inventory_issue = is_inventory_issue;
    }
}
