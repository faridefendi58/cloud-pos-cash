package com.slightsite.app.ui.sale;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.techicalservices.DownloadImageTask2;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AdapterListReceipt extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<JSONObject> items = new ArrayList<>();

    private AdapterListReceipt.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, JSONObject obj, int position);
    }

    public void setOnItemClickListener(final AdapterListReceipt.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListReceipt(List<JSONObject> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public ImageView img_receipt;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            img_receipt = (ImageView) v.findViewById(R.id.img_receipt);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_transfer_receipt, parent, false);
        vh = new AdapterListReceipt.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListReceipt.OriginalViewHolder) {
            final AdapterListReceipt.OriginalViewHolder view = (AdapterListReceipt.OriginalViewHolder) holder;
            AdapterListReceipt.OriginalViewHolder vwh = (AdapterListReceipt.OriginalViewHolder) holder;

            final JSONObject p = items.get(position);
            try {
                String title = p.getString("title");
                if (!title.isEmpty() && title.contains("_")) {
                    String[] separated = title.split("_");
                    title = separated[0];
                }
                view.title.setText(title.toUpperCase());
                if (p.has("image_url")) {
                    DownloadImageTask2 downloadImageTask = new DownloadImageTask2(view.img_receipt);
                    downloadImageTask.execute(p.getString("image_url"));
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static Bitmap getBitmapFromURL(String src) {
        try {
            InputStream in = new java.net.URL(src).openStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(in);
            return myBitmap;
        } catch (IOException e) {
            // Log exception
            e.printStackTrace();
            return null;
        }
    }
}
