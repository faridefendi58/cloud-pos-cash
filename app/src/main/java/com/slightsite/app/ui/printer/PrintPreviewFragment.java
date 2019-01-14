package com.slightsite.app.ui.printer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

public class PrintPreviewFragment extends Fragment {
    private Context context;
    private GridListAdapter adapter;

    public PrintPreviewFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.print_preview_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadPreview(view);
        onClickEvent(view);
    }

    private void loadPreview(View view) {
        TextView listView = (TextView) view.findViewById(R.id.print_preview);

        String formated_receipt = ((PrinterActivity)getActivity()).getFormatedReceipt();
        listView.setText(formated_receipt);
    }

    private void onClickEvent(View view) {
        view.findViewById(R.id.print_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((PrinterActivity)getActivity()).setBluetoothDeviceName(adapter.getSelectedItem());
                String txt = ((PrinterActivity)getActivity()).getFormatedReceipt();
                ((PrinterActivity)getActivity()).IntentPrint(txt);
            }
        });
        view.findViewById(R.id.home_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                getActivity().startActivity(intent);
            }
        });
    }
}
