package com.example.s8534.myaitme;

import android.Manifest;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import com.example.s8534.myaitme.caera2.Camera2Config;
import com.example.s8534.myaitme.caera2.Camera2RecordActivity;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.multi.SnackbarOnAnyDeniedMultiplePermissionsListener;


public class Caera2_startActivity extends AppCompatActivity {
    private Button btnOpenCamera2;
    private String picPath;//图片地址
    private ImageView iv;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //去掉标题栏
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null)actionBar.hide();

        setContentView(R.layout.caera2_start);
        btnOpenCamera2=findViewById(R.id.btn_openCamera2);

        //配置Camera2相关参数，
        Camera2Config.PREVIEW_MAX_HEIGHT=1300;
        Camera2Config.PATH_SAVE_PIC= Environment.getExternalStorageDirectory().getAbsolutePath() + "/CameraV2222/CameraV22222222/";
        Camera2Config.ENABLE_CAPTURE=true;
        Camera2Config.ENABLE_CAPTURE=true;
        Camera2Config.ACTIVITY_AFTER_CAPTURE = Caera2_startActivity.class;

        MultiplePermissionsListener mMultiplePermissionListener =
                SnackbarOnAnyDeniedMultiplePermissionsListener.Builder
                        .with(findViewById(android.R.id.content), "有些权限需要访问!!!")
                        .withOpenSettingsButton("设置")
                        .withCallback(new Snackbar.Callback() {
                            @Override
                            public void onShown(Snackbar sb) {
                                super.onShown(sb);
                            }

                            @Override
                            public void onDismissed(Snackbar transientBottomBar, int event) {
                                super.onDismissed(transientBottomBar, event);
                            }
                        }).build();
                Dexter.withActivity(this)
                .withPermissions(Manifest.permission.CAMERA,
                        Manifest.permission.READ_CONTACTS,
                        Manifest.permission.WRITE_CONTACTS,
                        Manifest.permission.RECORD_AUDIO)
                .withListener(mMultiplePermissionListener).check();


        btnOpenCamera2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // 意图实现activity的跳转
                Intent intent = new Intent(Caera2_startActivity.this,
                        Camera2RecordActivity.class);
                // 这种启动方式：startActivity(intent);并不能返回结果
                startActivityForResult(intent, 1);
//                startActivity(new Intent(Caera2_startActivity.this, Camera2RecordActivity.class));
            }
        });

        iv = findViewById(R.id.iv);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // RESULT_OK，判断另外一个activity已经结束数据输入功能，Standard activity result:
        // operation succeeded. 默认值是-1
        if (resultCode == 2) {
            if (requestCode == 1) {
                if (getIntent() != null) {
                    //获取传递过来的图片地址
                    picPath = data.getStringExtra(Camera2Config.INTENT_PATH_SAVE_PIC);
                    if (TextUtils.isEmpty(picPath)) {
                        iv.setVisibility(View.GONE);
                    } else {
                        iv.setImageBitmap(BitmapFactory.decodeFile(picPath));
                        btnOpenCamera2.setText("重拍");
                    }
                }
            }
        }
    }
}
