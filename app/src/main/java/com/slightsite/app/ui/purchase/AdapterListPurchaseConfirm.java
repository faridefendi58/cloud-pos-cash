package com.slightsite.app.ui.purchase;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.purchase.PurchaseLineItem;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.MainActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdapterListPurchaseConfirm extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<PurchaseLineItem> items;
    private OnItemClickListener mOnItemClickListener;
    private Boolean is_detail = false;
    private Boolean is_editable = true;
    private Boolean show_price = false;
    private Boolean show_price_text = false;
    private JSONObject supplier_configs = new JSONObject();

    public AdapterListPurchaseConfirm(Context _context, List<PurchaseLineItem> items) {
        this.context = _context;
        this.items = items;
    }

    public class OriginalViewHolder extends RecyclerView.ViewHolder {
        public TextView title;
        public EditText price;
        public EditText quantity;
        public TextView quantity_txt;
        public TextView unit;
        public View lyt_parent;
        public View line_separator;
        public LinearLayout price_container;
        public TextView price_text;

        public OriginalViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            price = (EditText) v.findViewById(R.id.price);
            quantity = (EditText) v.findViewById(R.id.quantity);
            quantity_txt = (TextView) v.findViewById(R.id.quantity_txt);
            unit = (TextView) v.findViewById(R.id.unit);
            lyt_parent = (View) v.findViewById(R.id.lyt_parent);
            line_separator = (View) v.findViewById(R.id.line_separator);
            price_container = (LinearLayout) v.findViewById(R.id.price_container);
            price_text = (TextView) v.findViewById(R.id.price_text);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.listview_purchase_confirm, parent, false);
        vh = new OriginalViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof AdapterListPurchaseConfirm.OriginalViewHolder) {
            final AdapterListPurchaseConfirm.OriginalViewHolder view = (AdapterListPurchaseConfirm.OriginalViewHolder) holder;
            AdapterListPurchaseConfirm.OriginalViewHolder vwh = (AdapterListPurchaseConfirm.OriginalViewHolder) holder;

            PurchaseLineItem pl = items.get(position);
            view.title.setText(pl.getProduct().getName());
            int qty = 1;
            double prc = 0.0;
            double sub_total = 0.0;
            try {
                qty = pl.getQuantity();
                prc = pl.getProduct().getUnitPrice();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (is_editable) {
                view.quantity.setSelectAllOnFocus(true);
                view.quantity.setText(qty + "");
                view.price.setSelectAllOnFocus(true);
            } else {
                view.quantity.setVisibility(View.GONE);
                view.quantity_txt.setText(qty +"");
                view.quantity_txt.setVisibility(View.VISIBLE);
            }

            if ((supplier_configs != null) && supplier_configs.length() > 0 && supplier_configs.has("products")) {
                try {
                    if (supplier_configs.has("use_default_price")) {
                        this.show_price_text = false;
                        this.show_price = false;
                    }

                    JSONObject supplier_products = supplier_configs.getJSONObject("products");
                    if (supplier_products.has(pl.getProduct().getBarcode())) {
                        JSONObject _product_data = supplier_products.getJSONObject(pl.getProduct().getBarcode());
                        if (_product_data.has("price") && (_product_data.getString("price") != null)) {
                            Double _def_price = _product_data.getDouble("price");
                            view.price.setText(CurrencyController.getInstance().moneyFormat(_def_price));
                            if (show_price_text) {
                                view.price_text.setVisibility(View.VISIBLE);
                                view.price_text.setText(CurrencyController.getInstance().moneyFormat(_def_price));
                            }
                            if (!show_price) {
                                view.price.setVisibility(View.GONE);
                            }
                            Integer _int_price = _product_data.getInt("price");
                            ((PurchaseOrderActivity) context).updatePurchaseData(position, "price", _int_price+"");
                        }
                    }
                } catch (JSONException e) {e.printStackTrace();}
            }
            //view.price.setText(CurrencyController.getInstance().moneyFormat(prc));
            if (show_price) {
                view.price_container.setVisibility(View.VISIBLE);
            }

            if (show_price_text) {
                view.price_text.setVisibility(View.VISIBLE);
                view.price_text.setText(CurrencyController.getInstance().moneyFormat(prc));
            }

            view.unit.setText(pl.getProduct().getUnit());

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

            if (is_editable) {
                setTextChangeListener(view.price, "price", position);
                setTextChangeListener(view.quantity, "quantity", position);
            }
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public interface OnItemClickListener {
        void onItemClick(View view, PurchaseLineItem obj, int position);
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener) {
        this.mOnItemClickListener = mItemClickListener;
    }

    private void setTextChangeListener(final EditText etv, final String setType, final int position) {
        etv.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            private String current_val;

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(!s.toString().equals(current_val)){
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (cleanString.length() >= 3) {
                        etv.removeTextChangedListener(this);
                        String formatted = "";
                        if (setType.equals("quantity")) {
                            current_val = Integer.parseInt(cleanString)+"";
                        } else {
                            double parsed = Double.parseDouble(cleanString);
                            formatted = CurrencyController.getInstance().moneyFormat(parsed);

                            current_val = formatted;
                        }
                        etv.setText(current_val);
                        etv.setSelection(current_val.length());
                        etv.addTextChangedListener(this);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                try {
                    String cleanString = s.toString().replaceAll("[.]", "");
                    if (setType == "price") {
                        if (cleanString.length() >= 3) {
                            double parsed = Double.parseDouble(cleanString);
                            String formatted = CurrencyController.getInstance().moneyFormat(parsed);
                            current_val = formatted;
                        } else {
                            current_val = s.toString();
                        }
                        if (cleanString.length() > 0) {
                            if (!is_detail) {
                                ((PurchaseOrderActivity) context).updatePurchaseData(position, "price", cleanString);
                            } else {
                                ((PurchaseDetailActivity) context).updatePurchaseData(position, "price", cleanString);
                            }
                        }
                    } else if (setType == "quantity") {
                        current_val = s.toString();
                        if (current_val.length() > 0) {
                            if (!is_detail) {
                                ((PurchaseOrderActivity) context).updatePurchaseData(position, "quantity", current_val);
                            } else {
                                ((PurchaseDetailActivity) context).updatePurchaseData(position, "quantity", current_val);
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void setIsDetail() {
        this.is_detail = true;
    }

    public void setIsEditable(Boolean _is_editable) {
        this.is_editable = _is_editable;
    }

    public void showPrice() {
        this.show_price = true;
    }

    public void showPriceText() {
        this.show_price_text = true;
    }

    public void setSupplierConfigs(JSONObject _supplierConfig) {
        this.supplier_configs = _supplierConfig;
    }
}
