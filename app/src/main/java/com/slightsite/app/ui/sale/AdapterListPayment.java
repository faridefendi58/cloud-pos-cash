package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.sale.PaymentItem;

import java.util.ArrayList;
import java.util.List;

public class AdapterListPayment extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PaymentItem> items = new ArrayList<>();

    private Context ctx;
    private AdapterListPayment.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, PaymentItem obj, int position);
    }

    public void setOnItemClickListener(final AdapterListPayment.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListPayment(Context context, List<PaymentItem> items) {
        this.items = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView price;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_cart, parent, false);
        vh = new AdapterListPayment.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListCart.OriginalViewHolder) {
            final AdapterListPayment.OriginalViewHolder view = (AdapterListPayment.OriginalViewHolder) holder;
            AdapterListPayment.OriginalViewHolder vwh = (AdapterListPayment.OriginalViewHolder) holder;

            final PaymentItem p = items.get(position);
            view.title.setText(p.getTitle());
            view.price.setText(p.getNominal()+"");
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}