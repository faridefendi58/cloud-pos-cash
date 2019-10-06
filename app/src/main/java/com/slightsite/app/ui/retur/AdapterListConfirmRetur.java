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
import com.slightsite.app.domain.inventory.Product;
import com.slightsite.app.domain.sale.Register;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListConfirmRetur extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<LineItem> items = new ArrayList<>();

    private Context ctx;
    private OnItemClickListener mOnItemClickListener;
    private Register register;
    private Map<String, Integer> qty_stacks = new HashMap<String, Integer>();
    private Map<Integer, Integer> new_qty_stacks = new HashMap<Integer, Integer>();
    private View sheetView;
    private Double tot_refund;

    public interface OnItemClickListener {
        void onItemClick(View view, LineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    public AdapterListConfirmRetur(Context context, List<LineItem> items, Register register) {
        this.items = items;
        ctx = context;
        this.register = register;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public ImageView image;
        public TextView title;
        public TextView quantity;
        public TextView price;
        public View lyt_parent;
        public View line_separator;
        public Button addReturStockButton;
        public LinearLayout add_to_stock_container;
        public LinearLayout add_qty_container;
        public TextView substract_qty;
        public TextView add_qty;

        public OriginalViewHolder(View v) {
            super(v);
            image = (ImageView) v.findViewById(R.id.image);
            title = (TextView) v.findViewById(R.id.title);
            quantity = (TextView) v.findViewById(R.id.quantity);
            price = (TextView) v.findViewById(R.id.price);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
            addReturStockButton = (Button) v.findViewById(R.id.addReturStockButton);
            add_to_stock_container = (LinearLayout) v.findViewById(R.id.add_to_stock_container);
            add_qty_container = (LinearLayout) v.findViewById(R.id.add_qty_container);
            substract_qty = (TextView) v.findViewById(R.id.substract_qty);
            add_qty = (TextView) v.findViewById(R.id.add_qty);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_retur_confirm, parent, false);
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
            String price_info = p.getQuantity() +" x "+ CurrencyController.getInstance().moneyFormat(p.getPriceAtSale());
            view.price.setText(price_info);

            try {
                qty_stacks.put(p.getProduct().getName(), p.getQuantity());
                new_qty_stacks.put(p.getProduct().getId(), 0);
            } catch (Exception e) {
                Log.e(getClass().getSimpleName(), e.getMessage());
            }

            view.addReturStockButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((ReturActivity) ctx).updateReturStockStacks(p.getProduct().getId(), 1);
                    view.quantity.setText("1");
                    view.add_qty_container.setVisibility(View.VISIBLE);
                    view.add_to_stock_container.setVisibility(View.GONE);
                    new_qty_stacks.put(p.getProduct().getId(), 1);
                    onAfterChangeQty();
                }
            });

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
                            ((ReturActivity) ctx).updateReturStockStacks(p.getProduct().getId(), current_qty);
                            if (current_qty == 0) {
                                view.add_to_stock_container.setVisibility(View.VISIBLE);
                                view.add_qty_container.setVisibility(View.GONE);
                            }
                            new_qty_stacks.put(p.getProduct().getId(), current_qty);
                            onAfterChangeQty();
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
                            ((ReturActivity) ctx).updateReturStockStacks(p.getProduct().getId(), current_qty);
                        }

                        new_qty_stacks.put(p.getProduct().getId(), current_qty);
                        onAfterChangeQty();
                    } catch (Exception e){e.printStackTrace();}
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

    public void setTotalRefund(Double _tot_refund) {
        this.tot_refund = _tot_refund;
    }

    private void onAfterChangeQty() {
        if (sheetView != null && tot_refund > 0) {
            try {
                Double no_refund = 0.0;
                for(LineItem line : items) {
                    no_refund = no_refund + (line.getPriceAtSale() * new_qty_stacks.get(line.getProduct().getId()));
                }

                Double new_refund = tot_refund - no_refund;
                ((TextView) sheetView.findViewById(R.id.debt_must_pay)).setText(CurrencyController.getInstance().moneyFormat(new_refund));
                ((ReturActivity) ctx).setRefundMustPay(new_refund);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
