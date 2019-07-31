package com.slightsite.app.ui.sale;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.inventory.LineItem;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.techicalservices.DownloadImageTask;
import com.slightsite.app.techicalservices.Server;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.MainActivity;

import java.util.ArrayList;
import java.util.List;

public class AdapterListCart extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private TextView cart_total;

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListCart(Context context, List<LineItem> items, Register register, TextView cart_total) {
        this.items = items;
        ctx = context;
        this.register = register;
        this.cart_total = cart_total;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView price;
        public TextView price_subtotal;
        public TextView quantity;
        public View lyt_parent;
        public TextView add_qty;
        public TextView substract_qty;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            price = (TextView) v.findViewById(R.id.price);
            //price_subtotal = (TextView) v.findViewById(R.id.price_subtotal);
            quantity = (TextView) v.findViewById(R.id.quantity);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            add_qty = (TextView) v.findViewById(R.id.add_qty);
            substract_qty = (TextView) v.findViewById(R.id.substract_qty);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_cart, parent, false);
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
            int qty = 1;
            double prc = 0.0;
            double sub_total = 0.0;
            Double grosir_price = 0.0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
            } catch (Exception e) {e.printStackTrace();}

            view.quantity.setText(""+qty);

            //sub_total = prc * qty;
            view.price.setText("@ " + CurrencyController.getInstance().moneyFormat(prc));

            if (p.getProduct().getImage() != null) {
                Bitmap drawable = ((MainActivity)ctx).getImageStack(p.getId());
                if (drawable != null) {
                    view.image.setImageBitmap(drawable);
                } else {
                    /*new DownloadImageTask(view.image)
                            .execute(Server.BASE_API_URL +""+ p.getProduct().getImage());*/
                    DownloadImageTask downloadImageTask = new DownloadImageTask(view.image);
                    downloadImageTask.setActivity((MainActivity)ctx);
                    downloadImageTask.setProductId(p.getId());
                    downloadImageTask.execute(Server.BASE_API_URL + "" + p.getProduct().getImage());
                }
            } else {
                view.image.setImageResource(R.drawable.ic_no_image);
            }

            view.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (mOnItemClickListener != null) {
                        mOnItemClickListener.onItemClick(view, items.get(position), position);
                    }
                }
            });

            view.add_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int current_qty = 0;
                    try {
                        current_qty = Integer.parseInt(view.quantity.getText().toString());
                    } catch (Exception e) {
                        Log.e(this.getClass().getSimpleName(), e.getMessage());
                    }
                    current_qty = current_qty + 1;
                    view.quantity.setText(""+ current_qty);
                    try {
                        updateQty(p, current_qty, p.getPriceAtSale(), view);
                    } catch (Exception e) {
                        Log.e(getClass().getSimpleName(), e.getMessage());
                    }
                }
            });

            view.substract_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int current_qty = 1;
                    try {
                        current_qty = Integer.parseInt(view.quantity.getText().toString());
                    } catch (Exception e) {
                        Log.e(this.getClass().getSimpleName(), e.getMessage());
                    }
                    if (current_qty > 1) {
                        current_qty = current_qty - 1;
                        view.quantity.setText(""+ current_qty);
                        try {
                            updateQty(p, current_qty, p.getPriceAtSale(), view);
                        } catch (Exception e) {
                            Log.e(getClass().getSimpleName(), e.getMessage());
                        }
                    } else {
                        try {
                            register.removeItem(p);
                            ((MainActivity)ctx).updateInventoryFragment();
                            ((MainActivity)ctx).updateSaleFragment();
                        } catch (Exception e) {
                            Log.e(getClass().getSimpleName(), e.getMessage());
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

    private void updateQty(LineItem lineItem, int qty, Double price, OriginalViewHolder view) {
        lineItem.setQuantity(qty);
        Double grosir_price = lineItem.getProduct().getUnitPriceByQuantity(lineItem.getProduct().getId(), qty);

        int saleId = register.getCurrentSale().getId();
        if (grosir_price > 0) {
            register.updateItem(
                    saleId,
                    lineItem,
                    qty,
                    grosir_price
            );

            double sub_total = grosir_price * qty;
            view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(grosir_price));
            //view.price_subtotal.setText(CurrencyController.getInstance().moneyFormat(sub_total));
        } else {
            register.updateItem(
                    saleId,
                    lineItem,
                    qty,
                    price
            );

            double sub_total = price * qty;
            view.price.setText("@ "+ CurrencyController.getInstance().moneyFormat(price));
            //view.price_subtotal.setText(CurrencyController.getInstance().moneyFormat(sub_total));
        }

        cart_total.setText(CurrencyController.getInstance().moneyFormat(register.getTotal()));
        ((MainActivity)ctx).updateInventoryFragment();
        ((MainActivity)ctx).updateSaleFragment();
    }
}

