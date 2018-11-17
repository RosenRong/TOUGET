package com.example.s8534.myaitme.listview;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import com.example.s8534.myaitme.R;

public class ListViewActivity extends AppCompatActivity {
    private ListView mLv1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view);
        //去掉默认的标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null)actionBar.hide();
        mLv1 = findViewById(R.id.lv_1);
        mLv1.setAdapter(new MyListAdapter(ListViewActivity.this));
    }
}
