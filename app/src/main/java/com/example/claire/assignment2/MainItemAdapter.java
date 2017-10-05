package com.example.claire.assignment2;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by claire on 22/9/17.
 * The array adapter for MainItem-type lists.
 */

public class MainItemAdapter extends ArrayAdapter<MainItem>{

    private int resourceId;
    private ImageView imageView;
    private TextView textView;

    public MainItemAdapter(Context context, int resourceId, List<MainItem> objects) {
        super(context, resourceId, objects);
        this.resourceId = resourceId;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        MainItem mainItem = getItem(position);
        View view = LayoutInflater.from(getContext()).inflate(resourceId, null);

        imageView = (ImageView) view.findViewById(R.id.itemIV);
        textView = (TextView) view.findViewById(R.id.titleTV);
//        Bitmap bm = BitmapFactory.decodeFile("makeup.png");
//        Bitmap bm = BitmapFactory.decodeResource(getRe, R.drawable.makeup);
//        imageView.setImageBitmap(bm);
        String i = "make_up";
        imageView.setBackgroundResource(mainItem.getImageFile());
        textView.setText(mainItem.getType());
//        imageView.setImageDrawable(getResources().getDrawable(R.drawable.makeup)

        return view;
    }
}
