package com.example.studio1;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import java.util.ArrayList;

public class MyImageAdapter extends BaseAdapter{
    private Context context;
    ArrayList<Bitmap> imgs;

    public MyImageAdapter(Context mainActivitycontext, ArrayList<Bitmap> imgs) {
        context=mainActivitycontext;
        this.imgs = imgs;
    }
    public int getCount() {
        return imgs.size();
    }
    public Bitmap getItem(int position){
        return imgs.get(position);
    }

    public long getItemId(int position){
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if(convertView==null) {
            imageView=new ImageView(context);
            int gridsize=context.getResources().getDimensionPixelSize(R.dimen.gridview_size);
            imageView.setLayoutParams(new GridView.LayoutParams(gridsize,gridsize));
            imageView.setScaleType(ImageView.ScaleType.FIT_XY);
            imageView.setPadding(5,5,5,5);
        }
        else{
            imageView=(ImageView) convertView;
        }
        //imageView.setImageResource(smallImages[position]);
        imageView.setImageBitmap(imgs.get(position));
        imageView.setId(position);

        return imageView;
    }
}