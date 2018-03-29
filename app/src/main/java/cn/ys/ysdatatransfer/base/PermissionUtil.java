package cn.ys.ysdatatransfer.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

/**
 * Created by Administrator on 2018-03-29.
 */

public class PermissionUtil {
    public static String[] PERMISSION = {Manifest.permission.READ_PHONE_STATE};

    public static boolean isLacksOfPermission(String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.checkSelfPermission(
                    YsApplication.getInstance().getApplicationContext(), permission) == PackageManager.PERMISSION_DENIED;
        }
        return false;
    }

}
