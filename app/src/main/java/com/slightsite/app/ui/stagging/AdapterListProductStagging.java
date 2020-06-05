package com.slightsite.app.ui.stagging;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListProductStagging extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private Map<Integer, Bitmap> image_stacks = new HashMap<Integer, Bitmap>();
    private Map<String, Integer> qty_stacks = new HashMap<String, Integer>();

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListProductStagging(Context context, List<LineItem> items, Register register) {
        this.items = items;
        ctx = context;
        this.register = register;
        this.image_stacks = register.getImageStacks();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView quantity;
        public TextView price;
        public TextView add_qty;
        public View lyt_parent;
        public View line_separator;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            quantity = (TextView) v.findViewById(R.id.quantity);
            price = (TextView) v.findViewById(R.id.price);
            add_qty = (TextView) v.findViewById(R.id.add_qty);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_stagging_items, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            OriginalViewHolder vwh = (OriginalViewHolder) holder;

            final LineItem p = items.get(position);
            view.title.setText(p.getProduct().getName());
            double prc = 0.0;
            int qty = 0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
                qty_stacks.put(p.getProduct().getName(), qty);
            } catch (Exception e) {
            }
            view.price.setText(""+ qty +" x "+ CurrencyController.getInstance().moneyFormat(prc));
            view.quantity.setText("0");

            view.image.setImageResource(R.drawable.ic_no_image);
            try {
                if (image_stacks.size() > 0 && image_stacks.get(p.getProduct().getId()) != null) {
                    view.image.setImageBitmap(image_stacks.get(p.getProduct().getId()));
                } else {
                    if (p.getProduct().getImageBitmap() != null) {
                        view.image.setImageBitmap(p.getProduct().getImageBitmap());
                    }
                }
            } catch (Exception e){e.printStackTrace();}

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            int last_item_position = getItemCount() - 1;
            if (position == last_item_position) {
                view.line_separator.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

