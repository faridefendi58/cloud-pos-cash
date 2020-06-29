package com.slightsite.app.ui.deposit;

import android.content.Context;
import android.graphics.Bitmap;
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

public class AdapterListProductTake extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private Map<Integer, Bitmap> image_stacks = new HashMap<Integer, Bitmap>();
    private Map<String, Integer> qty_stacks = new HashMap<String, Integer>();

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListProductTake(Context context, List<LineItem> items, Register register) {
        this.items = items;
        ctx = context;
        this.register = register;
        this.image_stacks = register.getImageStacks();
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView quantity;
        public TextView price;
        public TextView substract_qty;
        public TextView add_qty;
        public View lyt_parent;
        public View line_separator;
        public Button addTakeGoodButton;
        public LinearLayout add_to_queue_container;
        public LinearLayout add_qty_container;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            quantity = (TextView) v.findViewById(R.id.quantity);
            price = (TextView) v.findViewById(R.id.price);
            substract_qty = (TextView) v.findViewById(R.id.substract_qty);
            add_qty = (TextView) v.findViewById(R.id.add_qty);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
            addTakeGoodButton = (Button) v.findViewById(R.id.addTakeGoodButton);
            add_to_queue_container = (LinearLayout) v.findViewById(R.id.add_to_queue_container);
            add_qty_container = (LinearLayout) v.findViewById(R.id.add_qty_container);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_take_good, parent, false);
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
            double prc = 0.0;
            int qty = 0;
            try {
                qty = p.getQuantity();
                prc = p.getPriceAtSale();
                qty_stacks.put(p.getProduct().getName(), qty);
            } catch (Exception e) {
                Log.e("Adapter List Cart", e.getMessage());
            }
            view.price.setText(""+ qty +" x "+ CurrencyController.getInstance().moneyFormat(prc));
            view.quantity.setText("0");
            Log.e(getClass().getSimpleName(), "qty_stacks : "+ qty_stacks.toString());

            view.image.setImageResource(R.drawable.ic_no_image);
            try {
                if (image_stacks.size() > 0 && image_stacks.get(p.getProduct().getId()) != null) {
                    view.image.setImageBitmap(image_stacks.get(p.getProduct().getId()));
                    Log.e(getClass().getSimpleName(), "image_stacks : "+ image_stacks.toString());
                } else {
                    if (p.getProduct().getImageBitmap() != null) {
                        view.image.setImageBitmap(p.getProduct().getImageBitmap());
                    }
                    Log.e(getClass().getSimpleName(), "image_stacks tidak ada size : "+ image_stacks.size());
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

            view.substract_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int current_qty = Integer.parseInt(view.quantity.getText().toString());
                        if (current_qty > 0) {
                            current_qty = current_qty - 1;
                        }

                        view.quantity.setText(current_qty+"");
                        if (current_qty >= 0) {
                            int barcode = Integer.parseInt(p.getProduct().getBarcode());
                            ((DepositActivity) ctx).updateProductTakeStacks(barcode, current_qty);
                            if (current_qty == 0) {
                                view.add_to_queue_container.setVisibility(View.VISIBLE);
                                view.add_qty_container.setVisibility(View.GONE);
                            }
                        }
                    } catch (Exception e){e.printStackTrace();}
                }
            });

            view.add_qty.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        int current_qty = Integer.parseInt(view.quantity.getText().toString());
                        int max_qty = qty_stacks.get(view.title.getText().toString());
                        if (current_qty > 0) {
                            if (current_qty < max_qty) {
                                current_qty = current_qty + 1;
                            }
                        }

                        view.quantity.setText(current_qty+"");
                        if (current_qty > 0 && current_qty <= max_qty) {
                            int barcode = Integer.parseInt(p.getProduct().getBarcode());
                            ((DepositActivity) ctx).updateProductTakeStacks(barcode, current_qty);
                        }
                    } catch (Exception e){e.printStackTrace();}
                }
            });

            view.addTakeGoodButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int barcode = Integer.parseInt(p.getProduct().getBarcode());
                    ((DepositActivity) ctx).updateProductTakeStacks(barcode, 1);
                    view.quantity.setText("1");
                    view.add_qty_container.setVisibility(View.VISIBLE);
                    view.add_to_queue_container.setVisibility(View.GONE);
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }
}
