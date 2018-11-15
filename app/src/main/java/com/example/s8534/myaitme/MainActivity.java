package com.example.s8534.myaitme;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


import com.gyf.barlibrary.ImmersionBar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
ImageView myhome,classify,order,homepage,top_news,me_news,top_sao;

WebView webfeilei,webdingdan;

NoScrollWebView tuijianweb;

RelativeLayout fenlei,mein;

ConstraintLayout sousuolan,homepages;

LinearLayout biglayouts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //调用初始化的手机状态栏的颜色2为初始化颜色1为订单栏颜色
        //backcolors(2);
        //加载activity_main 布局
        setContentView(R.layout.activity_main);
        ImmersionBar.with(this)
                .transparentNavigationBar()
                .fullScreen(false)
                .statusBarDarkFont(true)   //状态栏字体是深色，不写默认为亮色
                .init(); //初始化，默认透明状态栏和黑色导航栏

        //去掉默认的标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null)actionBar.hide();

        //获取最大的布局的id
        biglayouts=findViewById(R.id.biglayouts);

        //找到主页和分类所在的有搜索栏的布局的id和
        //主页和分类的布局的id
        //订单web的id
        //个人中心的布局的id
        sousuolan=findViewById(R.id.yousuosou);
        homepages=findViewById(R.id.shouye);
        fenlei=findViewById(R.id.fenlei_webs);
        webdingdan=findViewById(R.id.webdingdans);
        mein=findViewById(R.id.mein);

        //找到在分类布局下的分类web的id
        webfeilei=findViewById(R.id.webfenlei);
        //找到在主页布局下的推荐web的id
        tuijianweb=findViewById(R.id.tuijianweb);

        //点击主页和分类的时候搜索框失去焦点
        homepages.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                homepages.setFocusable(true);
                homepages.setFocusableInTouchMode(true);
                homepages.requestFocus();
                return false;
            }
        });
        fenlei.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fenlei.setFocusable(true);
                fenlei.setFocusableInTouchMode(true);
                fenlei.requestFocus();
                return false;
            }
        });


        //设置主页和分类布局所在的大布局为可见
        //大布局为可见初始主页布局为可见
        //分类布局为不可见
        //订单web为不可见
        //个人中心布局为不可见
        sousuolan.setVisibility(View.VISIBLE);
        homepages.setVisibility(View.VISIBLE);
        fenlei.setVisibility(View.GONE);
        webdingdan.setVisibility(View.GONE);
        mein.setVisibility(View.GONE);

        //设置分类web的js功能可以使用
        //设置分类web的打开方式为内置在app打开
        webfeilei.getSettings().setJavaScriptEnabled(true);
        webfeilei.setWebViewClient(new WebViewClient());

        //设置订单web的js功能可以使用
        //设置订单web的打开方式为内置在app打开
        webdingdan.getSettings().setJavaScriptEnabled(true);
        webdingdan.setWebViewClient(new WebViewClient());
        //设置推荐web的js功能可以使用
        //设置推荐web的打开方式为内置在app打开
        tuijianweb.getSettings().setJavaScriptEnabled(true);
        tuijianweb.setWebChromeClient(new WebChromeClient());
        tuijianweb.loadUrl("file:///android_asset/imgs.htm");

        //点击主页返回到主页就是把其他布局全部隐藏
        homepage=findViewById(R.id.home_page);
        homepage.setOnClickListener(this);

        //点击分类进入到分类网页把其他布局全部隐藏
        classify=findViewById(R.id.classification);
        classify.setOnClickListener(this);

        //点击订单进入到订单网页把其他布局全部隐藏
        order=findViewById(R.id.order);
        order.setOnClickListener(this);

        //设置打开个人中心把其他布局全部隐藏
        myhome=findViewById(R.id.My);
        myhome.setOnClickListener(this);

        //打开消息界面
        top_news=findViewById(R.id.top_news);
        top_news.setOnClickListener(this);
        me_news=findViewById(R.id.me_news);
        me_news.setOnClickListener(this);

        //打开扫一扫
        top_sao=findViewById(R.id.top_sao);
        top_sao.setOnClickListener(this);

    }

    @Override //各种的点击事件
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.home_page://点击主页返回到主页就是把其他布局全部隐藏
                sousuolan.setVisibility(View.VISIBLE);
                homepages.setVisibility(View.VISIBLE);
                fenlei.setVisibility(View.GONE);
                webdingdan.setVisibility(View.GONE);
                mein.setVisibility(View.GONE);
                //每次点击就初始化一次android的状态栏颜色

                //并设置最大的布局的颜色为相应的颜色
                biglayouts.setBackgroundColor(getResources().getColor(R.color.bgcolor));
                break;
            case R.id.classification://点击分类进入到分类网页把其他布局全部隐藏
                webfeilei.loadUrl("http://spk18384229726.gz01.bdysite.com/html/class/index.htm");
                sousuolan.setVisibility(View.VISIBLE);
                homepages.setVisibility(View.GONE);
                fenlei.setVisibility(View.VISIBLE);
                webdingdan.setVisibility(View.GONE);
                mein.setVisibility(View.GONE);
                //每次点击就初始化一次android的状态栏颜色

                //并设置最大的布局的颜色为相应的颜色
                biglayouts.setBackgroundColor(getResources().getColor(R.color.bgcolor));
                break;
            case R.id.order://点击订单进入到订单网页把其他布局全部隐藏
                webdingdan.loadUrl("http://spk18384229726.gz01.bdysite.com/html/RollCall/RollCall.html");
                sousuolan.setVisibility(View.GONE);
                homepages.setVisibility(View.GONE);
                fenlei.setVisibility(View.GONE);
                webdingdan.setVisibility(View.VISIBLE);
                mein.setVisibility(View.GONE);
                //当点击订单时 根据订单的颜色改变android状态栏的颜色

                //并设置最大的布局的颜色为相应的颜色
                biglayouts.setBackgroundColor(getResources().getColor(R.color.dingdan));
                break;
            case R.id.My://设置打开个人中心把其他布局全部隐藏
                sousuolan.setVisibility(View.GONE);
                homepages.setVisibility(View.GONE);
                fenlei.setVisibility(View.GONE);
                webdingdan.setVisibility(View.GONE);
                mein.setVisibility(View.VISIBLE);
                //每次点击就初始化一次android的状态栏颜色

                //并设置最大的布局的颜色为相应的颜色
                biglayouts.setBackgroundColor(getResources().getColor(R.color.bgcolor));
                break;
            case R.id.top_news://打开消息界面
            case R.id.me_news:
                Intent intent1=new Intent(MainActivity.this,NewActivity.class);
                startActivity(intent1);
                break;
            case R.id.top_sao://打开扫一扫
                Intent intent2=new Intent(MainActivity.this,ScanActivity.class);
                startActivity(intent2);
                break;
            default:
        }
    }

}
