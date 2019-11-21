package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.slightsite.app.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.HashMap;
import java.util.Map;

public class AdapterListBank extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private JSONArray items = new JSONArray();
    private Context context;
    private Map<String, Integer> icons = new HashMap<String, Integer>();

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, String obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListBank(Context ctx, JSONArray items) {
        this.context = ctx;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageButton bank_icon;
        public TextView price;
        public View lyt_parent2;
        public RecyclerView bank_transfer_icons;

        public OriginalViewHolder(View v) {
            super(v);
            bank_icon = (ImageButton) v.findViewById(R.id.bank_icon);
            bank_transfer_icons = (RecyclerView) v.findViewById(R.id.bank_transfer_icons);
            lyt_parent2 = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_bank, parent, false);
        vh = new AdapterListBank.OriginalViewHolder(v);

        icons.put("nominal_mandiri", R.drawable.ic_mandiri);
        icons.put("nominal_bca", R.drawable.ic_bca);
        icons.put("nominal_bri", R.drawable.ic_bri);
        icons.put("wallet_gofood", R.drawable.ic_gofood_gojek);
        icons.put("wallet_grabfood", R.drawable.ic_grabfood);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;

            try {
                String pay_channel = items.getString(position);
                view.bank_icon.setImageDrawable((context).getDrawable(icons.get(pay_channel)));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            view.bank_icon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        try {
                            mOnItemClickListener.onItemClick(view, items.getString(position), position);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.length();
    }
}
