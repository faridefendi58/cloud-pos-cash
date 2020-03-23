package com.slightsite.app.ui.notification;

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

public class AdapterListNotification extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<JSONObject> items;
    private OnItemClickListener mOnItemClickListener;

    public AdapterListNotification(Context _context, List<JSONObject> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView created_at;
        public TextView description;
        public LinearLayout lyt_parent;
        public LinearLayout lyt_content;

        public OriginalViewHolder(View v) {
            super(v);
            created_at = (TextView) v.findViewById(R.id.created_at);
            description = (TextView) v.findViewById(R.id.description);
            lyt_parent = (LinearLayout) v.findViewById(R.id.lyt_parent);
            lyt_content = (LinearLayout) v.findViewById(R.id.lyt_content);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_notification, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListNotification.OriginalViewHolder) {
            final AdapterListNotification.OriginalViewHolder view = (AdapterListNotification.OriginalViewHolder) holder;
            AdapterListNotification.OriginalViewHolder vwh = (AdapterListNotification.OriginalViewHolder) holder;

            try {
                JSONObject jsonObject = items.get(position);
                view.created_at.setText(jsonObject.getString("created_at"));
                view.description.setText(jsonObject.getString("message"));
                if (jsonObject.has("status")) {
                    if (jsonObject.getString("status").equals("unread")) {
                        view.lyt_parent.setBackgroundColor(context.getResources().getColor(R.color.green_100));
                    } else {
                        view.lyt_parent.setBackgroundColor(context.getResources().getColor(R.color.grey_300));
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

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }
}