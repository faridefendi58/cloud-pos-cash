package com.slightsite.app.ui.printer;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.AlignmentSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.TextView;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

public class PrintPreviewFragment extends Fragment {
    private Context context;
    private GridListAdapter adapter;
    private WebView print_webview;

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
        TextView print_preview = (TextView) view.findViewById(R.id.print_preview);
        WebView print_webview = (WebView) view.findViewById(R.id.print_webview);

        String formated_receipt = ((PrinterActivity)getActivity()).getFormatedReceiptHtml();

        print_webview.loadDataWithBaseURL(null, "<html><body>"+ formated_receipt +"</body></html>", "text/html", "utf-8", null);

        //Spanned result = Html.fromHtml(formated_receipt);
        //print_preview.setText(result);
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
