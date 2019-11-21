package com.slightsite.app.ui.retur;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListReturReport extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private Map<String, Integer> qty_stacks = new HashMap<String, Integer>();
    private Map<Integer, Double> subtotal_stacks = new HashMap<Integer, Double>();
    private Double tot_refund;
    private String type = "retur"; //retur, refund

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListReturReport(Context context, List<LineItem> items, Register register) {
        this.items = items;
        ctx = context;
        this.register = register;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView sub_total;
        public TextView price;
        public View lyt_parent;
        public View line_separator;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            sub_total = (TextView) v.findViewById(R.id.sub_total);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_retur_report, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            OriginalViewHolder vwh = (OriginalViewHolder) holder;

            final LineItem p = items.get(position);
            view.title.setText(p.getProduct().getName());
            if (type.equals("refund")) {
                String price_info = p.getQuantity() + " x " + CurrencyController.getInstance().moneyFormat(p.getPriceAtSale());
                view.price.setText(price_info);
            } else if (type.equals("change_item")) {
                String price_info = p.getQuantity() + " x -" + CurrencyController.getInstance().moneyFormat(p.getPriceAtSale());
                view.price.setText(price_info);
            } else {
                view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(p.getPriceAtSale()));
                view.sub_total.setText(""+ p.getQuantity()+" pcs");
            }
            try {
                qty_stacks.put(p.getProduct().getName(), p.getQuantity());
                Double sub_total = p.getPriceAtSale() * p.getQuantity();
                subtotal_stacks.put(p.getProduct().getId(), sub_total);
                if (type.equals("refund")) {
                    view.sub_total.setText(CurrencyController.getInstance().moneyFormat(sub_total));
                } else if (type.equals("change_item")) {
                    view.sub_total.setText("-"+ CurrencyController.getInstance().moneyFormat(sub_total));
                }
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void setType(String _type) {
        this.type = _type;
    }
}
