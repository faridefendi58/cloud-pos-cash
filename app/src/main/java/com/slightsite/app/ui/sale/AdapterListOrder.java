package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;

import java.util.ArrayList;
import java.util.List;

public class AdapterListOrder extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private TextView cart_total;

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListOrder(Context context, List<LineItem> items, Register register, TextView cart_total) {
        this.items = items;
        ctx = context;
        this.register = register;
        this.cart_total = cart_total;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView price;
        public TextView price_subtotal;
        public TextView quantity;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
            //price_subtotal = (TextView) v.findViewById(R.id.price_subtotal);
            quantity = (TextView) v.findViewById(R.id.quantity);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_order, parent, false);
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
            int qty = 1;
            double prc = 0.0;
            double sub_total = 0.0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
            } catch (Exception e) {
                Log.e("Adapter List Cart", e.getMessage());
            }
            view.quantity.setText(""+ qty +" x "+ CurrencyController.getInstance().moneyFormat(prc));
            sub_total = prc * qty;
            view.price.setText(CurrencyController.getInstance().moneyFormat(sub_total));

            view.image.setImageResource(R.drawable.ic_no_image);
            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

