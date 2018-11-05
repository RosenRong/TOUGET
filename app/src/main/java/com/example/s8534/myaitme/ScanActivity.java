package com.example.s8534.myaitme;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Window;
import android.view.WindowManager;


public class ScanActivity extends AppCompatActivity{

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //调用初始化的手机状态栏的颜色2为初始化颜色1为订单栏颜色
        backcolors(2);
        //加载activity_main 布局
        setContentView(R.layout.scan_layout);

        //去掉默认的标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null)actionBar.hide();

    }

    //根据i返回过来的值设置android的状态栏颜色
    //i==1 就是订单的颜色
    //i等于其他的值的时候就标是恢复默认的颜色
    public void backcolors(int i){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SystemBarTintManager tin=new SystemBarTintManager(ScanActivity.this);
            setTranslucentStatus(true);
            tin.setStatusBarTintEnabled(true);
            if(i==1)
                tin.setStatusBarTintResource(R.color.dingdan);//通知栏所需颜色
            else
                tin.setStatusBarTintResource(R.color.bgcolor);//通知栏所需颜色
        }
    }


    @TargetApi(19)//更改android状态栏的颜色的具体调用方法
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }

}






