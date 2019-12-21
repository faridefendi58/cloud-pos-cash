package com.slightsite.app.ui.fee;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.sale.Fee;
import com.slightsite.app.domain.sale.FeeOn;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListFeeOn extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<FeeOn> items = new ArrayList<>();

    private Context ctx;

    private AdapterListFeeOn.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, FeeOn obj, int position);
    }

    public void setOnItemClickListener(final AdapterListFeeOn.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListFeeOn(Context context, List<FeeOn> items) {
        this.ctx = context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView date;
        public TextView total_invoice;
        public TextView total_fee;
        public TextView total_revenue;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            date = (TextView) v.findViewById(R.id.date);
            total_invoice = (TextView) v.findViewById(R.id.total_invoice);
            total_fee = (TextView) v.findViewById(R.id.total_fee);
            total_revenue = (TextView) v.findViewById(R.id.total_revenue);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_fee, parent, false);
        vh = new AdapterListFeeOn.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListFeeOn.OriginalViewHolder) {
            final AdapterListFeeOn.OriginalViewHolder view = (AdapterListFeeOn.OriginalViewHolder) holder;
            AdapterListFeeOn.OriginalViewHolder vwh = (AdapterListFeeOn.OriginalViewHolder) holder;

            try {
                FeeOn fee = items.get(position);
                Map<String, String> fee_to_map = fee.toMap();
                view.date.setText(fee_to_map.get("invoice_number"));
                view.total_invoice.setText(fee_to_map.get("date"));
                view.total_fee.setText(fee_to_map.get("total_fee"));
                //view.total_revenue.setText(fee_to_map.get("total_revenue"));
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
