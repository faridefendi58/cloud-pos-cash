package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.PaymentItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterListPaymentSimple extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Payment> items = new ArrayList<>();

    private AdapterListPaymentSimple.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Payment obj, int position);
    }

    public void setOnItemClickListener(final AdapterListPaymentSimple.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListPaymentSimple(List<Payment> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView price;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_payment_simple, parent, false);
        vh = new AdapterListPaymentSimple.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPaymentSimple.OriginalViewHolder) {
            final AdapterListPaymentSimple.OriginalViewHolder view = (AdapterListPaymentSimple.OriginalViewHolder) holder;
            AdapterListPaymentSimple.OriginalViewHolder vwh = (AdapterListPaymentSimple.OriginalViewHolder) holder;

            final Payment p = items.get(position);
            Log.e(getClass().getSimpleName(), "payment on adapter : "+ p.toMap().toString());
            view.title.setText(p.toMap().get("formated_payment_channel"));
            view.price.setText(p.toMap().get("formated_amount"));
            if (p.getAmount() <= 0) {
                view.lyt_parent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}