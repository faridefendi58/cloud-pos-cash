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
import com.slightsite.app.domain.sale.PaymentItem;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterListPayment extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PaymentItem> items = new ArrayList<>();
    private HashMap< String, String> payment_types;

    private Context ctx;
    private AdapterListPayment.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, PaymentItem obj, int position);
    }

    public void setOnItemClickListener(final AdapterListPayment.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListPayment(Context context, List<PaymentItem> items, HashMap<String,String> payment_types) {
        this.items = items;
        ctx = context;
        this.payment_types = payment_types;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_payment, parent, false);
        vh = new AdapterListPayment.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPayment.OriginalViewHolder) {
            final AdapterListPayment.OriginalViewHolder view = (AdapterListPayment.OriginalViewHolder) holder;
            AdapterListPayment.OriginalViewHolder vwh = (AdapterListPayment.OriginalViewHolder) holder;

            final PaymentItem p = items.get(position);
            view.title.setText(payment_types.get(p.getTitle()));
            view.price.setText(CurrencyController.getInstance().moneyFormat(p.getNominal())+ "");
            if (p.getNominal() <= 0) {
                view.lyt_parent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}