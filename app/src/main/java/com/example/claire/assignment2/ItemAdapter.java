package com.example.claire.assignment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by claire on 28/9/17.
 * The array adapter for Item-type lists.
 */

public class ItemAdapter extends ArrayAdapter<Item> {

    private int resourceId;
    private ImageView imageView;
    private TextView titleTextView;
    private TextView daysTextView;

    public ItemAdapter(Context context, int resourceId, List<Item> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        Item item = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        imageView = (ImageView) view.findViewById(R.id.itemPhotoIM);
        titleTextView = (TextView) view.findViewById(R.id.itemNameTV);
        daysTextView = (TextView) view.findViewById(R.id.itemLeftDayTV);

        imageView.setImageBitmap(String2Bitmap(item.getImage()));
        titleTextView.setText(item.getTitle());
        daysTextView.setText(item.getRemainingDays());

        return view;
    }

    public static Bitmap String2Bitmap(String st) {
        Bitmap bitmap = null;
        try {
            byte[] bitmapArray;
            bitmapArray = Base64.decode(st, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(bitmapArray, 0, bitmapArray.length);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }
}
