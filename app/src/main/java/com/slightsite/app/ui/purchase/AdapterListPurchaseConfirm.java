package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.purchase.PurchaseLineItem;

import java.util.List;

public class AdapterListPurchaseConfirm extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PurchaseLineItem> items;
    private OnItemClickListener mOnItemClickListener;

    public AdapterListPurchaseConfirm(Context _context, List<PurchaseLineItem> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public EditText price;
        public EditText quantity;
        public View lyt_parent;
        public View line_separator;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            price = (EditText) v.findViewById(R.id.price);
            quantity = (EditText) v.findViewById(R.id.quantity);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_purchase_confirm, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPurchaseConfirm.OriginalViewHolder) {
            final AdapterListPurchaseConfirm.OriginalViewHolder view = (AdapterListPurchaseConfirm.OriginalViewHolder) holder;
            AdapterListPurchaseConfirm.OriginalViewHolder vwh = (AdapterListPurchaseConfirm.OriginalViewHolder) holder;

            final PurchaseLineItem p = items.get(position);
            view.title.setText(p.getProduct().getName());
            int qty = 1;
            double prc = 0.0;
            double sub_total = 0.0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
            } catch (Exception e) {
                e.printStackTrace();
            }
            view.quantity.setText(qty+"");
            sub_total = prc * qty;
            view.price.setText(CurrencyController.getInstance().moneyFormat(sub_total));

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

    public interface OnItemClickListener {
        void onItemClick(View view, PurchaseLineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}
