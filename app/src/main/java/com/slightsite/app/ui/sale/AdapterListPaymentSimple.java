package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.payment.Payment;
import com.slightsite.app.domain.sale.PaymentItem;
import com.slightsite.app.techicalservices.DownloadImageTask2;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.ZoomableImageView;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AdapterListPaymentSimple extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Payment> items = new ArrayList<>();

    private AdapterListPaymentSimple.OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener {
        void onItemClick(View view, Payment obj, int position);
    }

    public void setOnItemClickListener(final AdapterListPaymentSimple.OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListPaymentSimple(List<Payment> items) {
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public TextView price;
        public ZoomableImageView img_receipt;
        public View lyt_parent;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
            img_receipt = (ZoomableImageView) v.findViewById(R.id.img_receipt);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_payment_simple, parent, false);
        vh = new AdapterListPaymentSimple.OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPaymentSimple.OriginalViewHolder) {
            final AdapterListPaymentSimple.OriginalViewHolder view = (AdapterListPaymentSimple.OriginalViewHolder) holder;
            AdapterListPaymentSimple.OriginalViewHolder vwh = (AdapterListPaymentSimple.OriginalViewHolder) holder;

            final Payment p = items.get(position);
            Log.e(getClass().getSimpleName(), "payment on adapter : "+ p.toMap().toString());
            view.title.setText(p.toMap().get("formated_payment_channel"));
            view.price.setText(p.toMap().get("formated_amount"));
            if (p.toMap().containsKey("transfer_receipt")) {
                try {
                    if (p.toMap().get("transfer_receipt").contains(Server.BASE_API_URL)) {
                        DownloadImageTask2 downloadImageTask = new DownloadImageTask2(view.img_receipt);
                        downloadImageTask.execute(p.toMap().get("transfer_receipt"));
                        view.img_receipt.setVisibility(View.VISIBLE);
                    } else {
                        JSONObject json = new JSONObject(p.toMap().get("transfer_receipt"));
                        String chn = p.toMap().get("payment_channel");

                        if (!chn.isEmpty() && chn.contains("_")) {
                            String[] separated = chn.split("_");
                            if (separated.length > 1) {
                                chn = separated[1];
                            }
                        }

                        String receipt_url = null;
                        if (json.has(chn)) {
                            receipt_url = Server.BASE_API_URL + "" + json.getString(chn);
                        }
                        if (!receipt_url.isEmpty() && receipt_url != null) {
                            DownloadImageTask2 downloadImageTask = new DownloadImageTask2(view.img_receipt);
                            downloadImageTask.execute(receipt_url);
                            view.img_receipt.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            if (p.getAmount() <= 0) {
                view.lyt_parent.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}