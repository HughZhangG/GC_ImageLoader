package com.gucheng.gc_imageloader;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.gucheng.gc_imageloader.adapter.SimpleAdapter;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<String> mDatas;
    private SimpleAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();

        initView();

    }

    private void initView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.id_recycler_view);

        mAdapter = new SimpleAdapter(this,mDatas);

        mRecyclerView.setAdapter(mAdapter);

        /**
         * 设置布局管理器
         */
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
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
                Toast.makeText(MainActivity.this,"ItemClick: "+position,Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemLongClick(View view, int position) {
                Toast.makeText(MainActivity.this,"ItemLongClick: "+position,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initData() {

        mDatas = new ArrayList<String>();
        for (int i = 'A' ; i <= 'z' ; i++){
            mDatas.add(""+(char)i);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main,menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int itemId = item.getItemId();

        switch (itemId){
            case R.id.action_add:
                mAdapter.addItem(1);
                break;
            case R.id.action_delete:
                mAdapter.deleteItem(1);
                break;
            case R.id.action_list_view:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false));
                break;
            case R.id.action_horizontal_list_view:
                mRecyclerView.setLayoutManager(new LinearLayoutManager(this,LinearLayoutManager.HORIZONTAL,false));
                break;
            case R.id.action_grid_view:
                mRecyclerView.setLayoutManager(new GridLayoutManager(this, 3,GridLayoutManager.VERTICAL,false));
                break;
            case R.id.action_horizontal_grid:
                mRecyclerView.setLayoutManager(new StaggeredGridLayoutManager(5,StaggeredGridLayoutManager.HORIZONTAL));
                break;
            case R.id.action_staggered:
                Intent intent = new Intent(this,StaggeredActivty.class);
                startActivity(intent);
                break;

        }

        return super.onOptionsItemSelected(item);
    }
}
