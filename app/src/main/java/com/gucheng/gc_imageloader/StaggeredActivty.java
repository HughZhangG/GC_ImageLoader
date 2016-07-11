package com.gucheng.gc_imageloader;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;
import android.widget.Toast;

import com.gucheng.gc_imageloader.adapter.SimpleAdapter;
import com.gucheng.gc_imageloader.adapter.StaggeredAdapter;

import java.util.ArrayList;
import java.util.List;

public class StaggeredActivty extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private SimpleAdapter mAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staggered_activty);

        initData();
        initView();
    }
    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recycler_view);

        mAdapter = new StaggeredAdapter(this,mDatas);

        mRecyclerView.setAdapter(mAdapter);

        /**
         * 设置布局管理器
         */
        mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(3,StaggeredGridLayoutManager.VERTICAL));
        /**
         * 设置动画
         */
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        /**
         * 设置分割线
         */
//        mRecyclerView.addItemDecoration(new ItemDecoration);

        mAdapter.setOnItemClickListener(new SimpleAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Toast.makeText(StaggeredActivty.this,"ItemClick: "+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(StaggeredActivty.this,"ItemLongClick: "+position,Toast.LENGTH_SHORT).show();
                mAdapter.deleteItem(position);
            }
        });
    }

    private void initData() {

        mDatas = new ArrayList<String>();
        for (int i = 'A' ; i <= 'z' ; i++){
            mDatas.add(""+(char)i);
        }

    }
}
