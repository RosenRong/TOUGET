package com.example.s8534.myaitme.caera2;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import com.example.s8534.myaitme.R;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

public class Camera2RecordActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    //views
    private TextureView mTextureView;
    private ImageView ivTakePhoto;//拍照&录像按钮
    private ImageView ivSwitchCamera;//切换前后摄像头
    private ImageView ivLightOn;//开关闪光灯
    private ImageView ivClose;//关闭该Activity

    //拍照方向
    private static final SparseIntArray ORIENTATION = new SparseIntArray();

    static {
        ORIENTATION.append(Surface.ROTATION_0, 90);
        ORIENTATION.append(Surface.ROTATION_90, 0);
        ORIENTATION.append(Surface.ROTATION_180, 270);
        ORIENTATION.append(Surface.ROTATION_270, 180);
    }

    //constant
    private static final String TAG = "Camera2RecordActivity";
    private static final int PERMISSIONS_REQUEST = 1;//拍照完成回调
    private static final int CAPTURE_OK = 0;//拍照完成回调
    private String mCameraId;//后置摄像头ID
    private String mCameraIdFront;//前置摄像头ID
    private Size mPreviewSize;//预览的Size
    private Size mCaptureSize;//拍照Size
    private int width;//TextureView的宽
    private int height;//TextureView的高
    private boolean isCameraFront = false;//当前是否是前置摄像头
    private boolean isLightOn = false;//当前闪光灯是否开启

    //Camera2
    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder mPreviewBuilder;
    private CaptureRequest mCaptureRequest;
    private CameraCaptureSession mPreviewSession;
    private CameraCharacteristics characteristics;
    private ImageReader mImageReader;
    private String picSavePath;//图片保存路径


    //handler
    private HandlerThread mCameraThread;
    private Handler mCameraHandler;


    private MediaRecorder mMediaRecorder;

    public static void start(Context context) {
        Intent intent = new Intent(context, Camera2RecordActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏模式
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //透明导航栏
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_camera2_record);

        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null)actionBar.hide();

        initViews();
        initTextureView();
    }

    /**
     * **************************************初始化相关**********************************************
     */
    //初始化TextureView
    private void initTextureView() {
        mCameraThread = new HandlerThread("CameraThread");
        mCameraThread.start();
        mCameraHandler = new Handler(mCameraThread.getLooper());
        mTextureView.setSurfaceTextureListener(this);
    }

    //初始化视图控件
    private void initViews() {
        mTextureView = findViewById(R.id.textureView);
        ivTakePhoto = findViewById(R.id.iv_takePhoto);
        ivSwitchCamera = findViewById(R.id.iv_switchCamera);
        ivLightOn = findViewById(R.id.iv_lightOn);
        ivClose = findViewById(R.id.iv_close);

        ivSwitchCamera.setOnClickListener(clickListener);
        ivLightOn.setOnClickListener(clickListener);
        ivClose.setOnClickListener(clickListener);

        //触摸事件
        onTouchListner();
    }

    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            int i = view.getId();
            if (i == R.id.iv_switchCamera) {
                //切换摄像头
                switchCamera();
            } else if (i == R.id.iv_lightOn) {
                //开启关闭闪光灯
                openLight();
            } else if (i == R.id.iv_close) {
                //关闭Activity
                finish();
            }
        }
    };

    //拍照按钮触摸事件
    private void onTouchListner() {
        ivTakePhoto.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch (event.getAction()) {
//                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_DOWN:
                        capture();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                }
                return true;
            }
        });

        mTextureView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                //两指缩放
                changeZoom(event);
                return true;
            }
        });
    }



    /**
     * ******************************SurfaceTextureListener*****************************************
     */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
        //当SurefaceTexture可用的时候，设置相机参数并打开相机
        this.width = width;
        this.height = height;

        setupCamera(width, height);//配置相机参数
        openCamera(mCameraId);//打开相机
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {
        configureTransform(width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {
    }

    /**
     * ******************************SetupCamera(配置Camera)*****************************************
     */
    private void setupCamera(int width, int height) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            //0表示后置摄像头,1表示前置摄像头
            mCameraId = manager.getCameraIdList()[0];
            mCameraIdFront = manager.getCameraIdList()[1];

            //前置摄像头和后置摄像头的参数属性不同，所以这里要做下判断
            if (isCameraFront) {
                characteristics = manager.getCameraCharacteristics(mCameraIdFront);
            } else {
                characteristics = manager.getCameraCharacteristics(mCameraId);
            }

            //获取StreamConfigurationMap，它是管理摄像头支持的所有输出格式和尺寸
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            //选择预览尺寸
            mPreviewSize = Camera2Util.getMinPreSize(map.getOutputSizes(SurfaceTexture.class), width, height, Camera2Config.PREVIEW_MAX_HEIGHT);

            Log.e(TAG, mPreviewSize.getWidth() + "----" + mPreviewSize.getHeight());
            Log.e(TAG, height + "----" + width);

            //获取相机支持的最大拍照尺寸
            mCaptureSize = Collections.max(Arrays.asList(map.getOutputSizes(ImageFormat.JPEG)), new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getHeight() * rhs.getWidth());
                }
            });

            configureTransform(width, height);

            //此ImageReader用于拍照所需
            setupImageReader();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //配置ImageReader
    private void setupImageReader() {
        //2代表ImageReader中最多可以获取两帧图像流
        mImageReader = ImageReader.newInstance(mCaptureSize.getWidth(), mCaptureSize.getHeight(),
                ImageFormat.JPEG, 2);
        mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image mImage = reader.acquireNextImage();
                ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
                byte[] data = new byte[buffer.remaining()];
                buffer.get(data);
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                Camera2Util.createSavePath(Camera2Config.PATH_SAVE_PIC);//判断有没有这个文件夹，没有的话需要创建
                picSavePath = Camera2Config.PATH_SAVE_PIC + "IMG_" + timeStamp + ".jpg";
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(picSavePath);
                    fos.write(data, 0, data.length);

                    Message msg = new Message();
                    msg.what = CAPTURE_OK;
                    msg.obj = picSavePath;
                    mCameraHandler.sendMessage(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                mImage.close();
            }
        }, mCameraHandler);

        mCameraHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case CAPTURE_OK:
                        //这里拍照保存完成，可以进行相关的操作，例如再次压缩等(由于封装，这里我先跳转掉完成页面)
                        if (Camera2Config.ACTIVITY_AFTER_CAPTURE != null) {
                            Intent intent = new Intent(Camera2RecordActivity.this, Camera2Config.ACTIVITY_AFTER_CAPTURE);
                            intent.putExtra(Camera2Config.INTENT_PATH_SAVE_PIC, picSavePath);
                            setResult(2, intent);
                            finish(); //结束当前的activity的生命周期
                        }
                        break;
                }
            }
        };
    }

    /**
     * ******************************openCamera(打开Camera)*****************************************
     */
    private void openCamera(String CameraId) {
        //获取摄像头的管理者CameraManager
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        //检查权限
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            //打开相机，第一个参数指示打开哪个摄像头，第二个参数stateCallback为相机的状态回调接口，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            manager.openCamera(CameraId, mStateCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }


    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            mCameraDevice = camera;
            startPreview();

            if (null != mTextureView) {
                configureTransform(mTextureView.getWidth(), mTextureView.getHeight());
            }
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
        }
    };

    /**
     * ******************************Camera2成功打开，开始预览(startPreview)*************************
     */
    public void startPreview() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }

        SurfaceTexture mSurfaceTexture = mTextureView.getSurfaceTexture();//获取TextureView的SurfaceTexture，作为预览输出载体

        if (mSurfaceTexture == null) {
            return;
        }

        try {
            closePreviewSession();
            mSurfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());//设置TextureView的缓冲区大小
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);//创建CaptureRequestBuilder，TEMPLATE_PREVIEW比表示预览请求
            Surface mSurface = new Surface(mSurfaceTexture);//获取Surface显示预览数据
            mPreviewBuilder.addTarget(mSurface);//设置Surface作为预览数据的显示界面

            //默认预览不开启闪光灯
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);

            //创建相机捕获会话，第一个参数是捕获数据的输出Surface列表，第二个参数是CameraCaptureSession的状态回调接口，当它创建好后会回调onConfigured方法，第三个参数用来确定Callback在哪个线程执行，为null的话就在当前线程执行
            mCameraDevice.createCaptureSession(Arrays.asList(mSurface, mImageReader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        //创建捕获请求
                        mCaptureRequest = mPreviewBuilder.build();
                        mPreviewSession = session;
                        //不停的发送获取图像请求，完成连续预览
                        mPreviewSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("dasdadasd", "捕获的异常" + e.toString());
        }
    }

    /**
     * ********************************************拍照*********************************************
     */
    private void capture() {
        if (null == mCameraDevice || !mTextureView.isAvailable() || null == mPreviewSize) {
            return;
        }
        try {
            CaptureRequest.Builder mCaptureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            //获取屏幕方向
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            mCaptureBuilder.addTarget(mImageReader.getSurface());
            //isCameraFront是自定义的一个boolean值，用来判断是不是前置摄像头，是的话需要旋转180°，不然拍出来的照片会歪了
            if (isCameraFront) {
                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(Surface.ROTATION_180));
            } else {
                mCaptureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATION.get(rotation));
            }

            //锁定焦点
            mCaptureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);

            //判断预览的时候是不是已经开启了闪光灯
            if (isLightOn) {
                mCaptureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_TORCH);
            } else {
                mCaptureBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            }

            //判断预览的时候是否两指缩放过,是的话需要保持当前的缩放比例
            mCaptureBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);

            CameraCaptureSession.CaptureCallback CaptureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    //拍完照unLockFocus
                    unLockFocus();
                }
            };
            mPreviewSession.stopRepeating();
            //咔擦拍照
            mPreviewSession.capture(mCaptureBuilder.build(), CaptureCallback, null);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void unLockFocus() {
        try {
            // 构建失能AF的请求
            mPreviewBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
            //闪光灯重置为未开启状态
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE, CaptureRequest.FLASH_MODE_OFF);
            //继续开启预览
            mPreviewSession.setRepeatingRequest(mCaptureRequest, null, mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * **********************************************切换摄像头**************************************
     */
    public void switchCamera() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (isCameraFront) {
            isCameraFront = false;
            setupCamera(width, height);
            openCamera(mCameraId);
        } else {
            isCameraFront = true;
            setupCamera(width, height);
            openCamera(mCameraIdFront);
        }
    }

    /**
     * ***************************************打开和关闭闪光灯****************************************
     */
    public void openLight() {
        if (isLightOn) {
            isLightOn=false;
            ivLightOn.setBackgroundResource(R.mipmap.ic_capture_light_off);
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_OFF);
        } else {
            isLightOn=true;
            ivLightOn.setBackgroundResource(R.mipmap.ic_capture_light_on);
            mPreviewBuilder.set(CaptureRequest.FLASH_MODE,
                    CaptureRequest.FLASH_MODE_TORCH);
        }

        try {
            if (mPreviewSession != null)
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), null, mCameraHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * *********************************放大或者缩小**********************************
     */
    //手指按下的点为(x1, y1)手指离开屏幕的点为(x2, y2)
    float finger_spacing;
    int zoom_level = 0;
    Rect zoom;

    public void changeZoom(MotionEvent event) {
        try {
            //活动区域宽度和作物区域宽度之比和活动区域高度和作物区域高度之比的最大比率
            float maxZoom = (characteristics.get(CameraCharacteristics.SCALER_AVAILABLE_MAX_DIGITAL_ZOOM)) * 10;
            Rect m = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);

            int action = event.getAction();
            float current_finger_spacing;
            //判断当前屏幕的手指数
            if (event.getPointerCount() > 1) {
                //计算两个触摸点的距离
                current_finger_spacing = getFingerSpacing(event);

                if (finger_spacing != 0) {
                    if (current_finger_spacing > finger_spacing && maxZoom > zoom_level) {
                        zoom_level++;

                    } else if (current_finger_spacing < finger_spacing && zoom_level > 1) {
                        zoom_level--;
                    }

                    int minW = (int) (m.width() / maxZoom);
                    int minH = (int) (m.height() / maxZoom);
                    int difW = m.width() - minW;
                    int difH = m.height() - minH;
                    int cropW = difW / 100 * (int) zoom_level;
                    int cropH = difH / 100 * (int) zoom_level;
                    cropW -= cropW & 3;
                    cropH -= cropH & 3;
                    zoom = new Rect(cropW, cropH, m.width() - cropW, m.height() - cropH);
                    mPreviewBuilder.set(CaptureRequest.SCALER_CROP_REGION, zoom);
                }
                finger_spacing = current_finger_spacing;
            } else {
                if (action == MotionEvent.ACTION_UP) {
                    //single touch logic,可做点击聚焦操作
                }
            }

            try {
                mPreviewSession.setRepeatingRequest(mPreviewBuilder.build(), new CameraCaptureSession.CaptureCallback() {
                            @Override
                            public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                super.onCaptureCompleted(session, request, result);
                            }
                        },
                        null);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            throw new RuntimeException("can not access camera.", e);
        }
    }

    //计算两个触摸点的距离
    private float getFingerSpacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) Math.sqrt(x * x + y * y);
    }

    /**
     * **************************************清除操作************************************************
     */
    public void onFinishCapture() {
        try {
            if (mPreviewSession != null) {
                mPreviewSession.close();
                mPreviewSession = null;
            }

            if (mCameraDevice != null) {
                mCameraDevice.close();
                mCameraDevice = null;
            }

            if (mImageReader != null) {
                mImageReader.close();
                mImageReader = null;
            }

            if (mMediaRecorder != null) {
                mMediaRecorder.release();
                mMediaRecorder = null;
            }

            if (mCameraHandler != null) {
                mCameraHandler.removeCallbacksAndMessages(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("adsdadadad", e.toString() + "onFinish2()");
        }
    }


    //清除预览Session
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            mPreviewSession.close();
            mPreviewSession = null;
        }
    }

    //重新配置打开相机
    public void resetCamera() {
        if (TextUtils.isEmpty(mCameraId)) {
            return;
        }

        if (mCameraDevice != null) {
            mCameraDevice.close();
        }

        setupCamera(width, height);
        openCamera(mCameraId);
    }

    /**
     * 屏幕方向发生改变时调用转换数据方法
     *
     * @param viewWidth  mTextureView 的宽度
     * @param viewHeight mTextureView 的高度
     */
    private void configureTransform(int viewWidth, int viewHeight) {
        if (null == mTextureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        }
        mTextureView.setTransform(matrix);
    }

    /**
     * ******************************Activity生命周期*****************************************
     */
    @Override
    protected void onResume() {
        super.onResume();
        //从FinishActivity退回来的时候默认重置为初始状态，因为有些机型从不可见到可见不会执行onSurfaceTextureAvailable，像有些一加手机
        //所以也可以在这里在进行setupCamera()和openCamera()这两个方法
        //每次开启预览缩放重置为正常状态
        if (zoom != null) {
            zoom.setEmpty();
            zoom_level = 0;
        }

        //每次开启预览默认闪光灯没开启
        isLightOn = false;
        ivLightOn.setSelected(false);

        //每次开启预览默认是后置摄像头
        isCameraFront = false;
    }

    @Override
    protected void onPause() {
        onFinishCapture();//释放资源
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}