package com.slightsite.app.techicalservices;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.InputStream;

public class DownloadImageTask2 extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    Bitmap _bitmap = null;

    public DownloadImageTask2(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            _bitmap = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return _bitmap;
    }

    protected void onPostExecute(Bitmap result) {
        try {
            if (result != null) {
                bmImage.setImageBitmap(result);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFile() {
        return _bitmap;
    }
}
