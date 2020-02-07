package com.slightsite.app.ui.purchase;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.domain.inventory.Product;

@SuppressLint("ValidFragment")
public class EditPurchaseDialog extends DialogFragment {

    private PurchaseFragment purchaseFragment;
    private Product product;

    private EditText quantityBox;
    private Button confirmButton;
    private Button cancelButton;
    private TextView dialog_title;
    private String qty;

    public EditPurchaseDialog(PurchaseFragment purchaseFragment, Product product) {
        super();
        this.purchaseFragment = purchaseFragment;
        this.product = product;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        View v = inflater.inflate(R.layout.dialog_change_quantity, container, false);

        quantityBox = (EditText) v.findViewById(R.id.quantityBox);
        confirmButton = (Button) v.findViewById(R.id.confirmButton);
        cancelButton = (Button) v.findViewById(R.id.cancelButton);
        dialog_title = (TextView) v.findViewById(R.id.dialog_title);

        dialog_title.setText(getArguments().getString("title"));
        qty = getArguments().getString("quantity");
        quantityBox.setText(qty+"");
        quantityBox.setSelectAllOnFocus(true);
        quantityBox.setFocusable(true);

        cancelButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View arg0) {
                end();
            }
        });

        confirmButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                end();
                try {
                    int _qty = Integer.parseInt(quantityBox.getText().toString());
                    purchaseFragment.addSubstractTheCart(product, _qty);
                } catch (Exception e){e.printStackTrace();}
                return;
            }

        });
        return v;
    }

    private void end(){
        purchaseFragment.update();
        this.dismiss();
    }
}
