package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.purchase.PurchaseItem;

import java.util.List;

public class AdapterListPurchaseHistory extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PurchaseItem> items;
    private OnItemClickListener mOnItemClickListener;
    private Boolean is_manager = false;

    public AdapterListPurchaseHistory(Context _context, List<PurchaseItem> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView issue_number;
        public ImageView is_need_confirmation;
        public TextView created_at;
        public TextView status;
        public TextView notes;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_content;

        public OriginalViewHolder(View v) {
            super(v);
            issue_number = (TextView) v.findViewById(R.id.issue_number);
            is_need_confirmation = (ImageView) v.findViewById(R.id.is_need_confirmation);
            created_at = (TextView) v.findViewById(R.id.created_at);
            status = (TextView) v.findViewById(R.id.status);
            notes = (TextView) v.findViewById(R.id.notes);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
            lyt_content = (LinearLayout) v.findViewById(R.id.lyt_content);
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
            view.issue_number.setText(pi.getTitle());
            view.created_at.setText(pi.getCreatedAt());
            //view.status.setText(pi.getStatus());
            view.status.setVisibility(View.GONE);
            view.notes.setText(pi.getNotes());

            if (pi.getType().equals("transfer_issue")) {
                view.issue_number.setTextColor(context.getResources().getColor(R.color.red_500));
            } else if (pi.getType().equals("transfer_receipt")) {
                view.issue_number.setTextColor(context.getResources().getColor(R.color.greenUcok));
            } else if (pi.getType().equals("inventory_issue")) {
                view.issue_number.setTextColor(context.getResources().getColor(R.color.red_500));
            } else if (pi.getType().equals("stock_in")) {
                if (pi.getStatus().equals("-1") || pi.getStatus().equals("-2")) {
                    view.issue_number.setTextColor(context.getResources().getColor(R.color.red_500));
                } else {
                    view.issue_number.setTextColor(context.getResources().getColor(R.color.greenUcok));
                }
            } else if (pi.getType().equals("stock_out")) {
                view.issue_number.setTextColor(context.getResources().getColor(R.color.yellowDarkUcok));
            }

            if (pi.getStatus().equals("0")) {
                view.is_need_confirmation.setVisibility(View.VISIBLE);
            } else if (pi.getStatus().equals("-1")) {
                if (is_manager) {
                    view.is_need_confirmation.setVisibility(View.VISIBLE);
                }
            }

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            view.lyt_content.setOnClickListener(new View.OnClickListener() {
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

    public void setIsManager() {
        this.is_manager = true;
    }
}