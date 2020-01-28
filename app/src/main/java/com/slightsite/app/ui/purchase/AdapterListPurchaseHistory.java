package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.purchase.PurchaseItem;

import java.util.List;

public class AdapterListPurchaseHistory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PurchaseItem> items;
    private OnItemClickListener mOnItemClickListener;

    public AdapterListPurchaseHistory(Context _context, List<PurchaseItem> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView issue_number;
        public TextView created_at;
        public TextView status;
        public TextView notes;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            issue_number = (TextView) v.findViewById(R.id.issue_number);
            created_at = (TextView) v.findViewById(R.id.created_at);
            status = (TextView) v.findViewById(R.id.status);
            notes = (TextView) v.findViewById(R.id.notes);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_purchase_history, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPurchaseHistory.OriginalViewHolder) {
            final AdapterListPurchaseHistory.OriginalViewHolder view = (AdapterListPurchaseHistory.OriginalViewHolder) holder;
            AdapterListPurchaseHistory.OriginalViewHolder vwh = (AdapterListPurchaseHistory.OriginalViewHolder) holder;

            PurchaseItem pi = items.get(position);
            view.issue_number.setText(pi.getIssueNumber());
            view.created_at.setText(pi.getCreatedAt());
            view.status.setText(pi.getStatus());
            view.notes.setText(pi.getNotes());

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            int last_item_position = getItemCount() - 1;
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, PurchaseItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}