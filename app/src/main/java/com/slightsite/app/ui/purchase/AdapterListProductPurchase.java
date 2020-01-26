package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
        TextView quantity;
        ImageView product_image;
        View optionView;
        LinearLayout add_qty_container;
        View rowView;
        Button addCartButton;
        TextView substract_qty;
        TextView add_qty;
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
        holder.substract_qty = (TextView) rowView.findViewById(R.id.substract_qty);
        holder.add_qty = (TextView) rowView.findViewById(R.id.add_qty);

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

        rowView.setOnClickListener(new View.OnClickListener() {

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
                    } catch (Exception e) {e.printStackTrace();}
                } else {
                    holder.optionView.setVisibility(View.GONE);
                    int tot_qty = stacks.get(p.getId());
                    holder.quantity.setText(""+ tot_qty);
                }
            }
        });

        holder.optionView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity instanceof MainActivity){
                    activity.optionOnClickHandler2(p.getId());
                }
            }
        });

        holder.product_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity instanceof MainActivity){
                    activity.optionOnClickHandler2(p.getId());
                }
            }
        });

        holder.addCartButton.setOnClickListener(new View.OnClickListener() {
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
                    } catch (Exception e) {e.printStackTrace();}
                } else {
                    holder.addCartButton.setVisibility(View.VISIBLE);
                    holder.optionView.setVisibility(View.GONE);
                    int tot_qty = stacks.get(p.getId()) + 1;
                    holder.quantity.setText(""+ tot_qty);
                }
            }
        });

        holder.add_qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int the_qty = Integer.parseInt(holder.quantity.getText().toString()) + 1;
                holder.quantity.setText(""+ the_qty);
                try {
                    if (the_qty == 1) {
                        fragment.addToCart(p);
                    } else {
                        fragment.addSubstractTheCart(p, the_qty);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        holder.substract_qty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int current_qty = Integer.parseInt(holder.quantity.getText().toString());
                int the_qty = 0;
                if (current_qty > 1) {
                    the_qty = current_qty - 1;
                }
                holder.quantity.setText(""+ the_qty);
                if (the_qty == 0) {
                    holder.addCartButton.setVisibility(View.VISIBLE);
                    //holder.add_qty_container.setVisibility(View.GONE);
                }
                try {
                    fragment.addSubstractTheCart(p, the_qty);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        return rowView;
    }
}
