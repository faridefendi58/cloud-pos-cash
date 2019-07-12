package com.slightsite.app.techicalservices;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.ImageView;

import com.slightsite.app.ui.MainActivity;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;
    Bitmap mIcon11 = null;
    MainActivity activity;
    int product_id = 10;

    public DownloadImageTask(ImageView bmImage) {
        this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
        String urldisplay = urls[0];
        try {
            InputStream in = new java.net.URL(urldisplay).openStream();
            mIcon11 = BitmapFactory.decodeStream(in);
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
            e.printStackTrace();
        }
        return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
        bmImage.setImageBitmap(result);
        try {
            if (result != null) {
                Log.e(getClass().getSimpleName(), "Product id : "+ product_id);
                if (activity != null) {
                    activity.setImageStacks(product_id, result);
                }else {
                    Log.e(getClass().getSimpleName(), "activitine null bro");
                }
            } else {
                Log.e(getClass().getSimpleName(), "File belum ada");
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public Bitmap getBitmapFile() {
        return mIcon11;
    }

    public void setActivity(MainActivity act) {
        this.activity = act;
    }

    public void setProductId(int productId) {
        this.product_id = productId;
    }
}
