package com.slightsite.app.ui.sale;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.otaliastudios.autocomplete.RecyclerViewPresenter;
import com.slightsite.app.R;
import com.slightsite.app.domain.customer.Customer;

import java.util.ArrayList;
import java.util.List;

public class PhonePresenter extends RecyclerViewPresenter<Customer> {

    protected Adapter adapter;
    protected List<Customer> customers;

    public PhonePresenter(Context context, List<Customer> customers) {
        super(context);
        this.customers = customers;
    }

    @Override
    protected PopupDimensions getPopupDimensions() {
        PopupDimensions dims = new PopupDimensions();
        dims.width = 600;
        dims.height = ViewGroup.LayoutParams.WRAP_CONTENT;
        return dims;
    }

    @Override
    protected RecyclerView.Adapter instantiateAdapter() {
        adapter = new Adapter();
        return adapter;
    }

    @Override
    protected void onQuery(@Nullable CharSequence query) {
        List<Customer> all = customers; //User.USERS;
        if (TextUtils.isEmpty(query)) {
            adapter.setData(all);
        } else {
            query = query.toString().toLowerCase();
            List<Customer> list = new ArrayList<>();
            for (Customer u : all) {
                if (u.getPhone().toLowerCase().contains(query)) {
                    list.add(u);
                }
            }
            adapter.setData(list);
        }
        adapter.notifyDataSetChanged();
    }

    class Adapter extends RecyclerView.Adapter<Adapter.Holder> {

        private List<Customer> data;

        public class Holder extends RecyclerView.ViewHolder {
            private View root;
            private TextView fullname;
            private TextView phone;
            public Holder(View itemView) {
                super(itemView);
                root = itemView;
                phone = ((TextView) itemView.findViewById(R.id.phone));
            }
        }

        public void setData(List<Customer> data) {
            this.data = data;
        }

        @Override
        public int getItemCount() {
            return (isEmpty()) ? 1 : data.size();
        }

        @Override
        public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new Holder(LayoutInflater.from(getContext()).inflate(R.layout.phone_autocomplete, parent, false));
        }

        private boolean isEmpty() {
            return data == null || data.isEmpty();
        }

        @Override
        public void onBindViewHolder(Holder holder, int position) {
            if (isEmpty()) {
                holder.phone.setText("Sorry!");
                holder.root.setOnClickListener(null);
                return;
            }
            final Customer user = data.get(position);
            holder.phone.setText(user.getPhone());
            holder.root.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dispatchClick(user);
                }
            });
        }
    }
}
