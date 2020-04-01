package com.naruto.mycamera;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private RequestPermissionsCallBack callBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        callBack = new RequestPermissionsCallBack() {
            @Override
            public void onGranted() {
                openCamera();
            }
        };

        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 100, callBack);
    }

    /**
     * 打开相机
     */
    private void openCamera() {
        //私有外部存储路径
        final String PHOTO_PATH = getExternalFilesDir(null).getPath() + "/photo";//拍照存储路径

        File tempFile = getFile(PHOTO_PATH, new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + ".temp");
        if (tempFile == null) {
            return;
        }

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);//打开相机的Intent
        //兼容android7.0 使用共享文件的形式
        ContentValues contentValues = new ContentValues(1);
        contentValues.put(MediaStore.Images.Media.DATA, tempFile.getAbsolutePath());
        Uri imageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivity(intent);
    }


    /**
     * 申请权限
     *
     * @param permissions
     * @param callBack
     */
    public void requestPermissions(String[] permissions, int requestCode, RequestPermissionsCallBack callBack) {
        if (Build.VERSION.SDK_INT < 23) {//6.0以下系统无需动态申请权限
            if (callBack != null) callBack.onGranted();
            return;
        }

        List<String> requestPermissionsList = new ArrayList<>();//记录需要申请的权限
        for (String p : permissions) {
            if (ContextCompat.checkSelfPermission(this, p) != PackageManager.PERMISSION_GRANTED) {//未授权，记录下来
                requestPermissionsList.add(p);
            }
        }
        if (requestPermissionsList.isEmpty()) {//已授权
            if (callBack != null) callBack.onGranted();
        } else {//申请
            String[] requestPermissionsArray = requestPermissionsList.toArray(new String[requestPermissionsList.size()]);
            ActivityCompat.requestPermissions(this, requestPermissionsArray, requestCode);
        }
    }


    /**
     * 判断是否有存储设备
     *
     * @return
     */
    public static boolean hasSdcard() {
        return Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED);
    }


    /**
     * 获取文件
     *
     * @param path 路径
     * @param name 名称
     * @return
     */
    public static File getFile(String path, String name) {
        //判断是否有存储卡
        if (!hasSdcard()) {
            return null;
        }

        //文件夹操作
        File dirFile = new File(Environment.getExternalStorageDirectory().toString() + path);
        //创建文件夹
        if (!dirFile.exists()) {
            dirFile.mkdirs();
        }

        //创建文件
        File tempFile = new File(dirFile, name);

        if (tempFile.exists()) {
            tempFile.delete();
        }
        try {
            tempFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return tempFile;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean isAllGranted = true;//是否全部已授权
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                isAllGranted = false;
                break;
            }
        }
        if (isAllGranted) {//全部已授权
            if (callBack != null) callBack.onGranted();
        } else {//被拒绝
            if (callBack == null) {
                Toast.makeText(this, "授权失败", Toast.LENGTH_SHORT).show();
            } else {
                callBack.onDenied(this);
            }
        }
    }


    /**
     * @Purpose 申请权限后处理接口
     * @Author Naruto Yang
     * @CreateDate 2019/12/19
     * @Note
     */
    public static abstract class RequestPermissionsCallBack {
        /**
         * 已授权
         */
        public abstract void onGranted();

        /**
         * 被拒绝
         */
        public void onDenied(Context context) {
            Toast.makeText(context, "授权失败", Toast.LENGTH_SHORT).show();
        }
    }
}
