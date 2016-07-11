package com.gucheng.gc_imageloader.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.gucheng.gc_imageloader.R;

import java.util.List;

/**
 * Created by gc on 2016/7/10.
 */
public class SimpleAdapter extends RecyclerView.Adapter<SimpleAdapter.VH> {

    protected List<String> mDatas;
    protected Context mContext;
    private LayoutInflater mInflater;

    private OnItemClickListener mOnItemClickListener;

    public interface OnItemClickListener{
        void onItemClick(View view , int position);
        void onItemLongClick(View view , int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        this.mOnItemClickListener = listener;
    }

    public SimpleAdapter(Context context,List<String> datas) {
        this.mDatas = datas;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {

        View inflate = mInflater.inflate(R.layout.item_simple_adapter, parent, false);

        VH holder = new VH(inflate);


        return holder;
    }

    @Override
    public void onBindViewHolder(final VH holder, int position) {

        holder.textView.setText(mDatas.get(position));

        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemClick(v,layoutPosition);
                }
            });

            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    int layoutPosition = holder.getLayoutPosition();
                    mOnItemClickListener.onItemLongClick(v,layoutPosition);
                    return true;
                }
            });

        }
    }

    @Override
    public int getItemCount() {
        return mDatas.size();
    }

    /**
     * add an item by position
     * @param pos
     */
    public void addItem(int pos){
        mDatas.add(pos,"Insert One");
//        notifyDataSetChanged();//没有动画效果
        notifyItemInserted(pos);
    }


    /**
     * delete the item by position
     * @param pos
     */
    public void deleteItem(int pos){
        mDatas.remove(pos);
        notifyItemRemoved(pos);
    }

    class VH extends RecyclerView.ViewHolder{

        TextView textView;

        public VH(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.id_tv);
        }
    }
}
