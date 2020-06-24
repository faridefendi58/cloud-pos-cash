package com.slightsite.app.ui.customer;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.techicalservices.Tools;

import org.json.JSONObject;

import java.util.List;

public class AdapterListCustomer extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<JSONObject> items;
    private AdapterListCustomer.OnItemClickListener mOnItemClickListener;

    public AdapterListCustomer(Context _context, List<JSONObject> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView name;
        public TextView phone;
        public TextView email;
        public TextView address;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_content;

        public OriginalViewHolder(View v) {
            super(v);
            name = (TextView) v.findViewById(R.id.name);
            phone = (TextView) v.findViewById(R.id.phone);
            email = (TextView) v.findViewById(R.id.email);
            address = (TextView) v.findViewById(R.id.address);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
            lyt_content = (LinearLayout) v.findViewById(R.id.lyt_content);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_customer, parent, false);
        vh = new AdapterListCustomer.OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListCustomer.OriginalViewHolder) {
            final AdapterListCustomer.OriginalViewHolder view = (AdapterListCustomer.OriginalViewHolder) holder;
            AdapterListCustomer.OriginalViewHolder vwh = (AdapterListCustomer.OriginalViewHolder) holder;

            try {
                JSONObject jsonObject = items.get(position);
                if (jsonObject.has("name")) {
                    view.name.setText(Tools.capitalize(jsonObject.getString("name")));
                }
                if (jsonObject.has("telephone")) {
                    view.phone.setText(jsonObject.getString("telephone"));
                }
                if (jsonObject.has("email")) {
                    view.email.setText(jsonObject.getString("email"));
                }
                if (jsonObject.has("address")) {
                    view.address.setText(jsonObject.getString("address"));
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

    public void setOnItemClickListener(final AdapterListCustomer.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}

