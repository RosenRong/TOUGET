package com.example.s8534.myaitme.caera2;
public class Camera2Config {
    public static int PREVIEW_MAX_HEIGHT = 1000;//最大高度预览尺寸，默认大于1000的第一个
    public static String PATH_SAVE_PIC= Camera2Util.getCamera2Path();//图片保存地址，不设置的话默认在根目录的Camera2文件夹
    public static Class ACTIVITY_AFTER_CAPTURE;//拍照完成后需要跳转的Activity,一般这个activity做处理照片或者视频用
    public static String INTENT_PATH_SAVE_PIC = "INTENT_PATH_SAVE_PIC";//Intent跳转可用
    public static boolean ENABLE_CAPTURE=true;//是否需要拍照功能
}
