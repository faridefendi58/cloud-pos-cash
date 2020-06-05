package com.slightsite.app.ui.stagging;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;

import org.json.JSONObject;

import java.util.List;

public class AdapterListStagging extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<JSONObject> items;
    private AdapterListStagging.OnItemClickListener mOnItemClickListener;

    public AdapterListStagging(Context _context, List<JSONObject> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView order_key;
        public TextView pickup_date;
        public TextView status;
        public TextView customer_name;
        public TextView customer_phone;
        public TextView customer_address;
        public TextView shipping_method;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_content;

        public OriginalViewHolder(View v) {
            super(v);
            order_key = (TextView) v.findViewById(R.id.order_key);
            pickup_date = (TextView) v.findViewById(R.id.pickup_date);
            status = (TextView) v.findViewById(R.id.status);
            customer_name = (TextView) v.findViewById(R.id.customer_name);
            customer_phone = (TextView) v.findViewById(R.id.customer_phone);
            customer_address = (TextView) v.findViewById(R.id.customer_address);
            shipping_method = (TextView) v.findViewById(R.id.shipping_method);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
            lyt_content = (LinearLayout) v.findViewById(R.id.lyt_content);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_stagging, parent, false);
        vh = new AdapterListStagging.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListStagging.OriginalViewHolder) {
            final AdapterListStagging.OriginalViewHolder view = (AdapterListStagging.OriginalViewHolder) holder;
            AdapterListStagging.OriginalViewHolder vwh = (AdapterListStagging.OriginalViewHolder) holder;

            try {
                JSONObject jsonObject = items.get(position);
                if (jsonObject.has("invoice_number") && !jsonObject.isNull("invoice_number")) {
                    view.order_key.setText(jsonObject.getString("invoice_number"));
                } else {
                    view.order_key.setText(jsonObject.getString("order_key"));
                }
                view.pickup_date.setText(jsonObject.getString("created_at"));
                if (jsonObject.has("status")) {
                    if (jsonObject.getInt("status") == 0) {
                        view.status.setText("Pending");
                    } else if (jsonObject.getInt("status") == 1) {
                        view.status.setText("Approved");
                    }
                }
                view.customer_name.setText(jsonObject.getString("name"));
                view.customer_phone.setText(jsonObject.getString("phone"));
                view.customer_address.setText(jsonObject.getString("address"));
                if (jsonObject.has("shipping_method")) {
                    if (jsonObject.getString("shipping_method").equals("ambil_nanti")) {
                        view.shipping_method.setText("Ambil Nanti");
                    } else if (jsonObject.getString("shipping_method").equals("gosend")) {
                        view.shipping_method.setText("GoSend");
                    }
                }
            } catch (Exception e){e.printStackTrace();}

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
        void onItemClick(View view, JSONObject obj, int position);
    }

    public void setOnItemClickListener(final AdapterListStagging.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}
