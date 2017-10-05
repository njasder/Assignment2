package com.example.claire.assignment2;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by claire on 28/9/17.
 * The array adapter for sub-type lists.
 */

public class SubItemAdapter extends ArrayAdapter<SubItem> {

    private int resourceId;
    private ImageView imageView;
    private TextView textView;
    private Resources res;

    public SubItemAdapter(Context context, int resourceId, List<SubItem> objects, Resources res) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
        this.res = res;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        SubItem subItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        imageView = (ImageView) view.findViewById(R.id.itemIV);
        textView = (TextView) view.findViewById(R.id.titleTV);

        //transform resources into bitmap
        Bitmap bmp= BitmapFactory.decodeResource(res, subItem.getImageFile());

        //set imageView with compressed images
        imageView.setImageBitmap(decodeSampledBitmapFromResource(res, subItem.getImageFile(), 50, 50));
        textView.setText(subItem.getSubType());

        return view;
    }

    private Bitmap decodeSampledBitmapFromResource(Resources res , int resId, int targetWidth, int tartgetHegiht){
// First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        /**
         * If set to true, the decoder will return null (no bitmap), but
         * the out... fields will still be set, allowing the caller to query
         * the bitmap without having to allocate the memory for its pixels.
         */
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeResource(res, resId, options);
        Log.d("BitmapFactory",bitmap+"");

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, targetWidth, tartgetHegiht);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;

        Bitmap bitmap2 = BitmapFactory.decodeResource(res, resId, options);
        Log.d("BitmapFactory",bitmap2+"");
        Log.d("BitmapFactory","bitmap2 height ="+bitmap2.getHeight()+"  width=="+bitmap2.getWidth());
        return  bitmap2;
    }

    private int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        String imageType = options.outMimeType;

        Log.d("BitmapFactory","Raw height ="+height+"  width=="+width);
        Log.d("BitmapFactory","options.outMimeType ="+imageType);
        /**
         * If set to a value > 1, requests the decoder to subsample the original
         * image, returning a smaller image to save memory. The sample size is
         * the number of pixels in either dimension that correspond to a single
         * pixel in the decoded bitmap. For example, inSampleSize == 4 returns
         * an image that is 1/4 the width/height of the original, and 1/16 the
         * number of pixels. Any value <= 1 is treated the same as 1. Note: the
         * decoder will try to fulfill this request, but the resulting bitmap
         * may have different dimensions that precisely what has been requested.
         * Also, powers of 2 are often faster/easier for the decoder to honor.
         */
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.d("BitmapFactory","inSampleSize ="+inSampleSize);
        return inSampleSize;
    }
}
