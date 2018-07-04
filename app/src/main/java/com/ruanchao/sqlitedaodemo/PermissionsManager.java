package com.ruanchao.sqlitedaodemo;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by ruanchao on 2018/1/9.
 */

public class PermissionsManager {

    private Activity mActivity;

    public PermissionsManager(Activity activity){
        mActivity = activity;
    }

    /**
     * 检查是否具有权限
     * @param permissions
     * @return true表示已经具有权限
     *         false表示不具有权限，接下来自动申请动态权限
     */
    public boolean checkIsGrantedPermissions(int requestPermissionCode, String... permissions) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && mActivity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M) {
                List<String> needRequestPermissionList = getNeedRequestPermissions(permissions);
                if (null != needRequestPermissionList
                        && needRequestPermissionList.size() > 0) {
                    //不具有权限,动态申请权限
                    String[] permissionArr = needRequestPermissionList.toArray(new String[needRequestPermissionList.size()]);
                    mActivity.requestPermissions(permissionArr, requestPermissionCode);
                    //不具有权限
                    return false;
                }
            }
        } catch (Throwable e) {
        }
        return true;
    }

    /**
     * 获取权限集中需要申请权限的列表
     * @param permissions
     * @return
     *
     */
    private List<String> getNeedRequestPermissions(String[] permissions) {
        List<String> needRequestPermissionList = new ArrayList<String>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && mActivity.getApplicationInfo().targetSdkVersion >= Build.VERSION_CODES.M){
            try {
                for (String perm : permissions) {
                    if (mActivity.checkSelfPermission(perm) != PackageManager.PERMISSION_GRANTED
                            || mActivity.shouldShowRequestPermissionRationale(perm)){
                        needRequestPermissionList.add(perm);
                    }
                }
            } catch (Throwable e) {
                Log.e("err", e.getMessage());
            }
        }
        return needRequestPermissionList;
    }



    /**
     * 检测是否所有的权限都已经授权
     * @param grantResults
     * @return
     *
     */
    public boolean verifyPermissions(int[] grantResults) {
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    /**
     * 显示提示信息
     *
     */
    public void showMissingPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setTitle("权限提醒");
        builder.setMessage("请授予权限");

        // 拒绝, 退出应用
        builder.setNegativeButton("取消",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //退出程序
                        mActivity.finish();
                    }
                });

        builder.setPositiveButton("设置",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startAppSettings();
                    }
                });

        builder.setCancelable(false);

        builder.show();
    }

    /**
     *  启动应用的设置
     *
     */
    private void startAppSettings() {
        Intent intent = new Intent(
                Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + mActivity.getPackageName()));
        mActivity.startActivity(intent);
    }

}
