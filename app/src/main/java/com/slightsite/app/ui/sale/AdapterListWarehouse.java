package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.warehouse.Warehouses;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListWarehouse extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Warehouses> items = new ArrayList<>();

    private Context ctx;
    private Integer warehouse_id;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Warehouses _item, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListWarehouse(Context context, List<Warehouses> items) {
        this.ctx = context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public LinearLayout lyt_parent;
        public ImageView lang_id_checked;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
            lang_id_checked = (ImageView) v.findViewById(R.id.lang_id_checked);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_warehouse, parent, false);
        vh = new AdapterListWarehouse.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListWarehouse.OriginalViewHolder) {
            final AdapterListWarehouse.OriginalViewHolder view = (AdapterListWarehouse.OriginalViewHolder) holder;
            AdapterListWarehouse.OriginalViewHolder vwh = (AdapterListWarehouse.OriginalViewHolder) holder;

            try {
                Warehouses wh = items.get(position);
                Map<String, String> wh_to_map = wh.toMap();
                view.title.setText(wh_to_map.get("title"));
                if (wh_to_map.get("warehouse_id").equals(warehouse_id+"")) {
                    view.lang_id_checked.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_circle_green_24dp));
                }
            } catch (Exception e){}

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(_view, items.get(position), position);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setSelectedWarehouseId(int warehouse_id) {
        this.warehouse_id = warehouse_id;
    }
}