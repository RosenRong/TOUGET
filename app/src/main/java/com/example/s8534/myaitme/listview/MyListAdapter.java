package com.example.s8534.myaitme.listview;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.s8534.myaitme.R;

public class MyListAdapter extends BaseAdapter {

    private Context mContext;
    private LayoutInflater mLayoutInflater;
    public MyListAdapter(Context context){
        this.mContext = context;
        mLayoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return 10;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    static class ViewHolder{
        public ImageView imageView;
        public TextView tvTilet,tvContent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if(convertView == null){
            convertView = mLayoutInflater.inflate(R.layout.layout_list_item,null);
            holder = new ViewHolder();
            holder.imageView = convertView.findViewById(R.id.iv_1);
            holder.tvTilet = convertView.findViewById(R.id.iv_title);
            holder.tvContent = convertView.findViewById(R.id.iv_content);
            convertView.setTag(holder);
        }else {
            holder =(ViewHolder) convertView.getTag();
        }
        //给控件赋值
        holder.tvTilet.setText("标题名字");
        holder.tvContent.setText("文章内容详细");
        Glide.with(mContext).load("http://spk18384229726.gz01.bdysite.com/img/story-bg-5.jpg").into(holder.imageView);
        return convertView;
    }
}
