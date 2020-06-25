package com.slightsite.app.ui.customer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.sale.FeeOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListCustomerOrder extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<FeeOn> items = new ArrayList<>();

    private Context ctx;

    private AdapterListCustomerOrder.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, FeeOn obj, int position);
    }

    public void setOnItemClickListener(final AdapterListCustomerOrder.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListCustomerOrder(Context context, List<FeeOn> items) {
        this.ctx = context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView invoice_number;
        public TextView date;
        public TextView total_amount;
        public TextView total_revenue;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            invoice_number = (TextView) v.findViewById(R.id.invoice_number);
            date = (TextView) v.findViewById(R.id.date);
            total_amount = (TextView) v.findViewById(R.id.total_amount);
            total_revenue = (TextView) v.findViewById(R.id.total_revenue);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_customer_order, parent, false);
        vh = new AdapterListCustomerOrder.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListCustomerOrder.OriginalViewHolder) {
            final AdapterListCustomerOrder.OriginalViewHolder view = (AdapterListCustomerOrder.OriginalViewHolder) holder;
            AdapterListCustomerOrder.OriginalViewHolder vwh = (AdapterListCustomerOrder.OriginalViewHolder) holder;

            try {
                FeeOn fee = items.get(position);
                Map<String, String> fee_to_map = fee.toMap();
                view.invoice_number.setText(fee_to_map.get("invoice_number"));
                view.date.setText(fee_to_map.get("date"));
                //view.total_amount.setText("("+fee_to_map.get("total_amount")+" items)");
                view.total_revenue.setText(fee_to_map.get("total_net_revenue"));
            } catch (Exception e){}

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
