package com.slightsite.app.ui.sale;

import android.app.Dialog;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.CurrencyController;
import com.slightsite.app.domain.customer.Customer;
import com.slightsite.app.domain.sale.Register;
import com.slightsite.app.domain.sale.Sale;
import com.slightsite.app.techicalservices.Tools;
import com.slightsite.app.ui.MainActivity;

import java.util.Calendar;

public class PaymentSuccessFragment extends DialogFragment {

    private View root_view;
    private Sale sale;
    private Customer customer;
    private String status;
    private Register register;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        root_view = inflater.inflate(R.layout.dialog_payment_success, container, false);

        try {
            register = Register.getInstance();
            long currentTime = Calendar.getInstance().getTimeInMillis();
            ((TextView) root_view.findViewById(R.id.transaction_date)).setText(Tools.getFormattedDateOnly(currentTime));
            ((TextView) root_view.findViewById(R.id.transaction_time)).setText(Tools.getFormattedTimeEvent(currentTime));
            if (customer != null) {
                ((TextView) root_view.findViewById(R.id.customer_name)).setText(customer.getName());
                ((TextView) root_view.findViewById(R.id.customer_phone)).setText(customer.getPhone());
                ((TextView) root_view.findViewById(R.id.customer_address)).setText(customer.getAddress());
            }

            if (sale != null) {
                Double tot = sale.getTotal() - sale.getDiscount();
                String formated_total = CurrencyController.getInstance().moneyFormat(tot);
                ((TextView) root_view.findViewById(R.id.transaction_total_amount)).setText(formated_total);
                TextView transaction_status = (TextView) root_view.findViewById(R.id.transaction_status);
                transaction_status.setText(status);
                if (status == getResources().getString(R.string.message_paid)) {
                    transaction_status.setTextColor(getResources().getColor(R.color.greenUcok));
                }
            }
        } catch (Exception e){e.printStackTrace();}

        ((FloatingActionButton) root_view.findViewById(R.id.fab)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                register.setCurrentSale(0);
                Intent newActivity = new Intent(getContext(), MainActivity.class);
                dismiss();
                startActivity(newActivity);
            }
        });

        return root_view;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        return dialog;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public void setSale(Sale sale) {
        this.sale = sale;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}