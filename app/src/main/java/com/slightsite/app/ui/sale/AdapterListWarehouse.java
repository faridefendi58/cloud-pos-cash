package com.slightsite.app.ui.sale;

import android.content.Context;
import android.graphics.Typeface;
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
import com.slightsite.app.techicalservices.Tools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListWarehouse extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Warehouses> items = new ArrayList<>();

    private Context ctx;
    private Integer warehouse_id;
    private Integer has_group_name = 0;

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
                if (wh.getId() <= 0) {
                    view.lang_id_checked.setVisibility(View.GONE);
                    Map<String, String> warehouseTypeList = Tools.getWarehouseTypeList();
                    if (warehouseTypeList.containsKey(wh_to_map.get("title"))) {
                        view.title.setText(warehouseTypeList.get(wh_to_map.get("title")));
                    }
                    view.title.setAllCaps(true);
                    view.title.setTypeface(null, Typeface.BOLD);
                    has_group_name = has_group_name + 1;
                }
                if (has_group_name > 0) {
                    if (wh.getId() > 0) {
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                        params.setMargins(20,0,0,0);
                        view.title.setLayoutParams(params);
                    }
                }
                if (wh_to_map.get("warehouse_id").equals(warehouse_id+"")) {
                    view.lang_id_checked.setImageDrawable(ctx.getDrawable(R.drawable.ic_check_circle_green_24dp));
                }
            } catch (Exception e){}

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View _view) {
                    if (mOnItemClickListener != null) {
                        Warehouses wh = items.get(position);
                        if (wh.getId() > 0) {
                            mOnItemClickListener.onItemClick(_view, items.get(position), position);
                        }
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