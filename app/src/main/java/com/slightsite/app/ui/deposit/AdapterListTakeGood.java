package com.slightsite.app.ui.deposit;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.DateTimeStrategy;
import com.slightsite.app.techicalservices.Tools;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListTakeGood extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<JSONObject> items = new ArrayList<JSONObject>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Integer view_version = 1;

    public interface OnItemClickListener {
        void onItemClick(View view, JSONObject obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListTakeGood(Context context, List<JSONObject> items) {
        this.items = items;
        ctx = context;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView created_at;
        public TextView admin_name;
        public TextView history_title;
        public View lyt_parent;
        public View line_separator;
        public RecyclerView takeGoodItemRecycle;

        public OriginalViewHolder(View v) {
            super(v);
            created_at = (TextView) v.findViewById(R.id.created_at);
            admin_name = (TextView) v.findViewById(R.id.admin_name);
            history_title = (TextView) v.findViewById(R.id.history_title);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
            takeGoodItemRecycle = (RecyclerView) v.findViewById(R.id.takeGoodItemRecycle);
            takeGoodItemRecycle.setLayoutManager(new LinearLayoutManager(ctx));
            takeGoodItemRecycle.setHasFixedSize(true);
            takeGoodItemRecycle.setNestedScrollingEnabled(false);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_take_good_history, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            OriginalViewHolder vwh = (OriginalViewHolder) holder;

            final JSONObject item = items.get(position);
            try {
                String _picked_at = "";
                if (item.has("created_at")) {
                    String dtStart = item.getString("created_at");
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    try {
                        Date date = format.parse(dtStart);
                        long _created_at = date.getTime();
                        _picked_at = Tools.getFormattedDateTimeShort(_created_at);
                        view.created_at.setText(Tools.getFormattedDateOnly(_created_at));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                view.admin_name.setText(item.getString("admin_name"));
                if (view_version == 2) {
                    if (_picked_at.length() > 0) {
                        view.history_title.setText("Pengambilan ("+ _picked_at +")");
                        view.created_at.setVisibility(View.GONE);
                        view.admin_name.setVisibility(View.GONE);
                        view.history_title.setVisibility(View.VISIBLE);
                    }
                }
                if (item.has("items")) {
                    List<Map<String,String>> lineitemList = new ArrayList<Map<String, String>>();
                    JSONArray _items = item.getJSONArray("items");
                    for (int n = 0; n < _items.length(); n++) {
                        JSONObject itObj = _items.getJSONObject(n);
                        Map<String,String> _map = new HashMap<String, String>();
                        _map.put("title", itObj.getString("title"));
                        _map.put("quantity", "-"+ itObj.getString("quantity"));
                        lineitemList.add(_map);
                    }
                    Log.e(getClass().getSimpleName(), "lineitemList : "+ lineitemList.toString());
                    if (lineitemList.size() > 0) {
                        AdapterListSimple sAdap = new AdapterListSimple(ctx, lineitemList);
                        view.takeGoodItemRecycle.setAdapter(sAdap);
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

    public void setViewVersion(int _view_version) {
        this.view_version = _view_version;
    }
}
