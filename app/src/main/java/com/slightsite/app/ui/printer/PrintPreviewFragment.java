package com.slightsite.app.ui.printer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.slightsite.app.R;
import com.slightsite.app.ui.MainActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class PrintPreviewFragment extends Fragment {
    private Context context;
    private GridListAdapter adapter;
    private WebView print_webview;
    private View view;

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
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        this.view = view;
        loadPreview(view);
        onClickEvent(view);
    }

    private void loadPreview(View view) {
        TextView print_preview = (TextView) view.findViewById(R.id.print_preview);
        print_webview = (WebView) view.findViewById(R.id.print_webview);
        print_webview.setVerticalScrollBarEnabled(false);
        print_webview.setHorizontalScrollBarEnabled(false);
        print_webview.getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);

        try {
            ((PrinterActivity)getActivity()).buildDataFromServer(print_webview);
        } catch (Exception e){
            e.printStackTrace();
            // build from local data
            String formated_receipt = ((PrinterActivity)getActivity()).getFormatedReceiptHtml();
            print_webview.loadDataWithBaseURL(null, "<html><body>"+ formated_receipt +"</body></html>", "text/html", "utf-8", null);
        }

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

    private String invoiceNumber = "INV-000";
    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    private Bitmap screenShot(WebView view) {
        //view.enableSlowWholeDocumentDraw();
        //view.getSettings().setBuiltInZoomControls(true);
        //view.getSettings().setSupportZoom(true);

        Log.e(getTag(), "view.getContentHeight() : "+ view.getContentHeight());
        Log.e(getTag(), "view.getHeight() : "+ view.getHeight());
        Log.e(getTag(), "view.getScaleY() : "+ view.getScaleY());
        Log.e(getTag(), "view.getY() : "+ view.getY());
        Log.e(getTag(), "view.getVerticalScrollbarWidth() : "+ view.getVerticalScrollbarWidth());
        Log.e(getTag(), "View.MeasureSpec.getSize(view.getMeasuredWidth()) : "+ View.MeasureSpec.getSize(view.getMeasuredWidth()));
        Log.e(getTag(), "View.MeasureSpec.getSize(view.getMeasuredHeight()) : "+ View.MeasureSpec.getSize(view.getMeasuredHeight()));

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private static File saveBitmap(Bitmap bm, String fileName){
        final String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Screenshots";
        File dir = new File(path);
        if(!dir.exists())
            dir.mkdirs();
        File file = new File(dir, fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bm.compress(Bitmap.CompressFormat.PNG, 90, fOut);
            fOut.flush();
            fOut.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    public void shareInvoice() {
        Bitmap bm = screenShot(print_webview);
        File file = saveBitmap(bm, invoiceNumber+".png");

        Uri uri = Uri.fromFile(new File(file.getAbsolutePath()));
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, invoiceNumber);
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        shareIntent.setType("image/*");
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(Intent.createChooser(shareIntent, "share via"));
    }
}
