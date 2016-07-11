package com.gucheng.gc_imageloader.adapter;

import android.content.Context;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gc on 2016/7/10.
 */
public class StaggeredAdapter extends SimpleAdapter {

    private List<Integer> mHeights;

    public StaggeredAdapter(Context context, List<String> datas) {
        super(context, datas);

        initHeights();
    }

    private void initHeights() {
        mHeights = new ArrayList<Integer>();
        for (int i = 0; i < mDatas.size(); i++){
            mHeights.add((int) (100 + Math.random() * 300));
        }
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        super.onBindViewHolder(holder, position);
        ViewGroup.LayoutParams layoutParams = holder.itemView.getLayoutParams();
        layoutParams.height = mHeights.get(position);
        holder.itemView.setLayoutParams(layoutParams);
    }
}
