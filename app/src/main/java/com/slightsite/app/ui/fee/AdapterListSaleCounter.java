package com.slightsite.app.ui.fee;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.sale.ItemCounter;

import java.util.ArrayList;
import java.util.List;

public class AdapterListSaleCounter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<ItemCounter> items = new ArrayList<>();

    private AdapterListSaleCounter.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, ItemCounter obj, int position);
    }

    public void setOnItemClickListener(final AdapterListSaleCounter.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListSaleCounter(List<ItemCounter> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView counter;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            counter = (TextView) v.findViewById(R.id.counter);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_sale_counter, parent, false);
        vh = new AdapterListSaleCounter.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListSaleCounter.OriginalViewHolder) {
            final AdapterListSaleCounter.OriginalViewHolder view = (AdapterListSaleCounter.OriginalViewHolder) holder;
            AdapterListSaleCounter.OriginalViewHolder vwh = (AdapterListSaleCounter.OriginalViewHolder) holder;

            final ItemCounter p = items.get(position);
            view.title.setText(p.toMap().get("name"));
            view.counter.setText(p.toMap().get("counter"));
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}

