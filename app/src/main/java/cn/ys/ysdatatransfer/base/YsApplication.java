package cn.ys.ysdatatransfer.base;

import android.app.Application;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.widget.Toast;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.SocketException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.Ysserver.entity.MqttPropertise;
import cn.ys.ysdatatransfer.business.PWMUtils;

import static android.content.ContentValues.TAG;

/**
 * Created by shizhiyuan on 2017/7/19.
 */

public class YsApplication extends Application {

    private YsCrashHandler ysCrashHandler;
    private static YsApplication instance;
    private static Toast toast;

    public static String getUSERNAME() {
        return USERNAME;
    }

    private  static String USERNAME ;;

    public static String getCLIENTID() {
        return CLIENTID;
    }

    private  static String CLIENTID = "";
    public LocationService locationService;
    public Vibrator mVibrator;
    //定位出来的位置
    private static String location_time;
    private static String location_dir;
    private static String location_alt;
    private static String location_lat;
    private static String location_lon;
    private static String location_pos;

    public static String getBoradcastIP() {
        return BoradcastIP;
    }

    private static String BoradcastIP;


    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
         initUsrCrashHandler();
        USERNAME = MqttPropertise.USR_NAME;
        CLIENTID = getDeviceSerial();
        //强制设定WIFI模式为AP模式
      //  setWifiApEnabled(true,(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE),"Y4GClient:"+YsApplication.getCLIENTID(),"cloudoftime");
        //强制打开GPS
        openGPS(this);
        try {
            BoradcastIP = IPUtils.getIp(this);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        locationService = new LocationService(this);
        mVibrator = (Vibrator) getApplicationContext().getSystemService(Service.VIBRATOR_SERVICE);
        locationService.registerListener(mListener);
        locationService.setLocationOption(locationService.getDefaultLocationClientOption());
        locationService.start();// 定位SDK
        PWMUtils.init_pwm_mode();
        // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
    }
    public static String getNowTime(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }
    public  void  sendtxt_udp_mqtt(String info_name,String info_state )
    {
        Intent intent = new Intent();
        intent.setAction("udp_mqtt_send");//用隐式意图来启动广播
        Bundle bundle = new Bundle();
        bundle.putString("info_name", info_name);
        bundle.putString("info_state", info_state);
        bundle.putString("clientid",CLIENTID);
        intent.putExtras(bundle);
        this.getApplicationContext().sendBroadcast(intent);
    }
    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled, WifiManager wifiManager, String ap_name, String ap_pwd) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = ap_name;
            //配置热点的密码
            apConfig.preSharedKey=ap_pwd;
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    /**
     * 强制帮打开GPS
     * @param context
     */
    public static final void openGPS(Context context) {
        Intent GPSIntent = new Intent();
        GPSIntent.setClassName("com.android.settings",
                "com.android.settings.widget.SettingsAppWidgetProvider");
        GPSIntent.addCategory("android.intent.category.ALTERNATIVE");
        GPSIntent.setData(Uri.parse("custom:3"));
        try {
            PendingIntent.getBroadcast(context, 0, GPSIntent, 0).send();
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        }
    }
    //Application 的回掉方法
    public static final YsApplication getInstance() {
        return instance;
    }

    /**
     * 初始化异常处理类
     */
    private void initUsrCrashHandler() {
        ysCrashHandler = YsCrashHandler.getmusrCrashHandler();
        if (ysCrashHandler != null) {
            ysCrashHandler.initCrashHandler(this);
        }
    }

    @Override
    public void onTerminate() {
        //程序终止的时候执行
        Log.d(TAG, "EpcApplication-----------onTerminate程序终止的时候执行");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        //低内存的时候执行
        Log.d(TAG, "EpcApplication-----------onLowMemory低内存的时候执行");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        Log.d(TAG, "EpcApplication-----------onTrimMemory 程序在内存清理的时候执行");
        super.onTrimMemory(level);
    }

    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

    public static String get_pos()
    {
        JSONObject jsonObject = new JSONObject();
        try {
            if(location_lat==null)
                return  null;
            jsonObject.put("location_time",location_time);
            jsonObject.put("location_dir",location_dir);
            jsonObject.put("location_alt",location_alt);
            jsonObject.put("location_lat",location_lat);
            jsonObject.put("location_lon",location_lon);
            jsonObject.put("location_pos",location_pos);
            return  jsonObject.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    public static String getDeviceSerial() {
        String serial = "unknown";
        try {
            Class clazz = Class.forName("android.os.Build");
            Class paraTypes = Class.forName("java.lang.String");
            Method method = clazz.getDeclaredMethod("getString", paraTypes);
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }
            serial = (String)method.invoke(new Build(), "ro.serialno");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return serial;
    }
    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        @Override
        public void onReceiveLocation(BDLocation location) {
            // TODO Auto-generated method stub
            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                location_time =location.getTime();
                location_alt =Double.toString(location.getAltitude());
                location_lat = Double.toString(location.getLatitude());
                location_lon = Double.toString(location.getLongitude());
                location_dir = Double.toString(location.getDirection());
                location_pos= location.getLocationDescribe();
            }
        }
    };
}
