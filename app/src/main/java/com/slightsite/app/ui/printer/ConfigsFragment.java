package com.slightsite.app.ui.printer;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

import java.util.Map;

public class ConfigsFragment extends Fragment {
    private Context context;
    private GridListAdapter adapter;
    private EditText txt_char_length;
    private EditText txt_header;
    private EditText txt_footer;
    private Map<String, String > printerConfigs;

    public ConfigsFragment() {
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.print_config_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initConfigs(view);
    }

    private void initConfigs(View view) {
        txt_char_length = (EditText) view.findViewById(R.id.txt_char_length);
        txt_header = (EditText) view.findViewById(R.id.txt_header);
        txt_footer = (EditText) view.findViewById(R.id.txt_footer);

        printerConfigs = ((PrinterActivity)getActivity()).getPrinterConfigs();
        txt_char_length.setText(printerConfigs.get("char_length"));
        txt_header.setText(printerConfigs.get("header"));
        txt_footer.setText(printerConfigs.get("footer"));
    }
}
