package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.ui.MainActivity;
import com.slightsite.app.ui.component.UpdatableFragment;
import com.slightsite.app.ui.inventory.InventoryFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListProduct extends BaseAdapter{

    private Context context;
    private List<Product> items;
    private int resource;
    private MainActivity activity;
    private InventoryFragment fragment;
    private Map<Integer, Integer> stacks = new HashMap<Integer, Integer>();

    private static LayoutInflater inflater = null;

    public AdapterListProduct(MainActivity activity, List<Product> items, int resource, InventoryFragment fragment) {
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
        TextView quantity;
        ImageView product_image;
        View optionView;
        LinearLayout add_qty_container;
        View rowView;
        Button addCartButton;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub
        final Holder holder = new Holder();
        View rowView;

        rowView = inflater.inflate(this.resource, null);
        holder.name = (TextView) rowView.findViewById(R.id.name);
        holder.stock_counter = (TextView) rowView.findViewById(R.id.stock_counter);
        holder.quantity = (TextView) rowView.findViewById(R.id.quantity);
        holder.product_image = (ImageView) rowView.findViewById(R.id.product_image);
        holder.optionView = (View) rowView.findViewById(R.id.optionView);
        holder.add_qty_container = (LinearLayout) rowView.findViewById(R.id.add_qty_container);
        holder.rowView = rowView;
        holder.addCartButton = (Button) rowView.findViewById(R.id.addCartButton);

        final Product p = items.get(position);
        Map<String, String> pmap = p.toMap();
        holder.name.setText(p.getName());
        holder.stock_counter.setText(pmap.get("availability"));

        stacks = fragment.getStacks();
        if (stacks.containsKey(p.getId())) {
            holder.optionView.setVisibility(View.GONE);
            holder.quantity.setText(""+ stacks.get(p.getId()));
            holder.add_qty_container.setVisibility(View.VISIBLE);
            holder.addCartButton.setVisibility(View.GONE);
        } else {
            holder.optionView.setVisibility(View.GONE);
            holder.quantity.setText("0");
            holder.add_qty_container.setVisibility(View.GONE);
            holder.addCartButton.setVisibility(View.VISIBLE);
        }

        rowView.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                holder.addCartButton.setVisibility(View.GONE);
                if (!stacks.containsKey(p.getId())) {
                    holder.add_qty_container.setVisibility(View.VISIBLE);
                    holder.optionView.setVisibility(View.GONE);

                    int tot_qty = Integer.parseInt(holder.quantity.getText().toString()) + 1;
                    if (tot_qty <= 0) {
                        tot_qty = 1;
                    }
                    holder.quantity.setText(""+ tot_qty);

                    try {
                        fragment.addToCart(p);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                } else {
                    //holder.add_qty_container.setVisibility(View.VISIBLE);
                    //holder.addCartButton.setVisibility(View.VISIBLE);
                    holder.optionView.setVisibility(View.GONE);
                    int tot_qty = stacks.get(p.getId());
                    holder.quantity.setText(""+ tot_qty);
                }
                fragment.triggerAddSubstractButton(holder.rowView, p.getId(), position);
            }
        });

        holder.optionView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity instanceof MainActivity){
                    activity.optionOnClickHandler2(p.getId());
                }
            }
        });

        holder.product_image.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity instanceof MainActivity){
                    activity.optionOnClickHandler2(p.getId());
                }
            }
        });

        holder.addCartButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!stacks.containsKey(p.getId())) {
                    holder.add_qty_container.setVisibility(View.VISIBLE);
                    holder.addCartButton.setVisibility(View.GONE);

                    int tot_qty = Integer.parseInt(holder.quantity.getText().toString()) + 1;
                    if (tot_qty <= 0) {
                        tot_qty = 1;
                    }
                    holder.quantity.setText(""+ tot_qty);

                    try {
                        fragment.addToCart(p);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                } else {
                    holder.addCartButton.setVisibility(View.VISIBLE);
                    holder.optionView.setVisibility(View.GONE);
                    int tot_qty = stacks.get(p.getId()) + 1;
                    holder.quantity.setText(""+ tot_qty);
                }
                fragment.triggerAddSubstractButton(holder.rowView, p.getId(), position);
            }
        });

        return rowView;
    }

}