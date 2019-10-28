package com.slightsite.app.ui.retur;

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
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.sale.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListProductChange extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Product> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private Map<Integer, Bitmap> image_stacks = new HashMap<Integer, Bitmap>();
    private Map<String, Integer> qty_stacks = new HashMap<String, Integer>();

    private View sheetView;

    public interface OnItemClickListener {
        void onItemClick(View view, Product obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListProductChange(Context context, List<Product> items, Register register) {
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
        public Button addReturButton;
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
            addReturButton = (Button) v.findViewById(R.id.addReturButton);
            add_to_queue_container = (LinearLayout) v.findViewById(R.id.add_to_queue_container);
            add_qty_container = (LinearLayout) v.findViewById(R.id.add_qty_container);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_retur, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof OriginalViewHolder) {
            final OriginalViewHolder view = (OriginalViewHolder) holder;
            OriginalViewHolder vwh = (OriginalViewHolder) holder;

            final Product p = items.get(position);
            view.title.setText(p.getName());
            double prc = 0.0;
            int qty = 0;
            try {
                prc = p.getUnitPrice();
                qty_stacks.put(p.getName(), qty);
            } catch (Exception e) {
                Log.e("Adapter List Cart", e.getMessage());
            }
            view.price.setText("@"+ CurrencyController.getInstance().moneyFormat(prc));
            view.quantity.setText("0");

            view.image.setImageResource(R.drawable.ic_no_image);
            try {
                if (image_stacks.size() > 0 && image_stacks.get(p.getId()) != null) {
                    view.image.setImageBitmap(image_stacks.get(p.getId()));
                } else {
                    if (p.getImageBitmap() != null) {
                        view.image.setImageBitmap(p.getImageBitmap());
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
                            ((ReturActivity) ctx).updateProductReturStacks(p.getId(), current_qty);
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
                            ((ReturActivity) ctx).updateProductReturStacks(p.getId(), current_qty);
                        }
                    } catch (Exception e){e.printStackTrace();}
                }
            });

            view.addReturButton.setText(((ReturActivity) ctx).getResources().getString(R.string.button_add_to_cart));
            view.addReturButton.setBackgroundColor(((ReturActivity) ctx).getResources().getColor(R.color.greenUcok));
            view.addReturButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ReturActivity) ctx).updateProductReturStacks(p.getId(), 1);
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

    public void setSheetView(View _sheetView) {
        this.sheetView = _sheetView;
    }
}
