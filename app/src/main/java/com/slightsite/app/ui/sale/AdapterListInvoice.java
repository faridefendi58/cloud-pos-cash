package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.LineItem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListInvoice extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Map<String, String>> items = new ArrayList<Map<String, String>>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Map<String, String> _item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListInvoice(Context context, List<Map<String, String>> _items) {
        this.items = _items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView invoice_number;
        public TextView status;
        public TextView customer_data;
        public LinearLayout lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            invoice_number = (TextView) v.findViewById(R.id.invoice_number);
            status = (TextView) v.findViewById(R.id.status);
            customer_data = (TextView) v.findViewById(R.id.customer_data);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_report_v2, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            view.invoice_number.setText(items.get(position).get("server_invoice_number"));
            view.status.setText(items.get(position).get("status"));
            if (Integer.parseInt(items.get(position).get("is_paid")) <= 0) {
                view.status.setTextColor(view.itemView.getContext().getResources().getColor(R.color.red_800));
            }
            view.customer_data.setText(items.get(position).get("customer_data"));

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
