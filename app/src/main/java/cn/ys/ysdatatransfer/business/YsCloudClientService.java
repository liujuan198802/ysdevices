package cn.ys.ysdatatransfer.business;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android_serialport_api.SerialPort;
import cn.Ysserver.YsCloudMqttCallbackAdapter;
import cn.ys.ysdatatransfer.base.YsApplication;

/**
 * Created by shizhiyuan on 2017/7/21.
 */

public class YsCloudClientService extends Service {

    private static final String TAG = "YS_service";
    private static String uName = "";
    private static String uPW = "";
    private YsCloudClient ysCloudClient;
    private YsCloudClientCallback ysCloudClientCallback;
    private MyBinder mBinder = new MyBinder();
    private static String deviceid="00009385000000000001";
    private static long data_count_com_get = 0;
    private static long data_count_com_send = 0;
    private static long data_count_mqtt_get =0;
    private static long data_count_mqtt_send =0;

    public YsCloudClientService getInstance() {
        return YsCloudClientService.this;
    }
    /*******
     *
     *串口部分代码
     *
     *
     * *****/
    private ReadThread comReadThread_1;
    private ReadThread comReadThread_2;
   // String portPath0 = "/dev/ttyMT0";
    String portPath2 = "/dev/ttyMT2";
    String portPath3 = "/dev/ttyMT3";
    private final int baudrate=57600;
    private int uart_port=2;
    private SerialPort mSerialPort_1 = null;
    protected OutputStream mOutputStream_1 = null;
    private SerialPort mSerialPort_2 = null;
    protected OutputStream mOutputStream_2 = null;
    private ReadThread mReadThread_1;
    private ReadThread mReadThread_2;
    //读取串口数据线程
    private class ReadThread extends Thread {

        private SerialPort mSerialPort = null;
        protected InputStream mInputStream=null;
        private int port_num;
        public ReadThread(SerialPort serialPort,int port_no)
        {
            this.port_num= port_no;
            mSerialPort = serialPort;
            if(mSerialPort!=null)
            {
              mInputStream = mSerialPort.getInputStream();
            }
        }
        @Override
        public void run() {
            super.run();
            while(!isInterrupted()) {
                int size;
                Log.v(TAG, "串口线程已启动！");
                try {
                    byte[] buffer = new byte[512];
                    if (mInputStream == null) return;
                    while((size=mInputStream.read(buffer)) != -1){
                        if (size > 0) {
                         //   Log.v(TAG, "receive data :"+buffer.toString());
                            byte[] data = new byte[size];
                            System.arraycopy(buffer, 0, data, 0, size);
                            publishForDevId(deviceid,data);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            Log.v(TAG, "串口线程已结束！");
        }
    }
    public long get_com_data_count()
    {
        return data_count_com_get;
    }
    public long get_mqtt_data_count()
    {
        return data_count_mqtt_get;
    }
    public int get_client_count()
    {
        return 0;
    }
    public boolean get_mqtt_state()
    {
      return  ysCloudClient.is_mqtt_connected();
    }
    public void set_data_count_zero()
    {
        data_count_mqtt_get=0;
        data_count_com_get = 0;
        data_count_com_send=0;
        data_count_mqtt_send =0;
    }


    /*******
     *
     *
     * 服务代码
     *
     * *********/
    @Override
    public void onCreate() {
        super.onCreate();
        ysCloudClientCallback = new YsCloudClientCallback();
        ysCloudClient = new YsCloudClient();
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        Bundle bundle = intent.getExtras();
        uName = bundle.getString("uname");
        uPW = bundle.getString("upw");
        deviceid = bundle.getString("clientid");
        doClientConnection(uName, uPW,deviceid);
        try{
            mSerialPort_1 = new SerialPort(new File(portPath2), baudrate, 0);
            mOutputStream_1 = mSerialPort_1.getOutputStream();
            mReadThread_1 = new ReadThread(mSerialPort_1,1);
        }
        catch (SecurityException e) {
            //
            e.printStackTrace();
        }
        catch (IOException e){
            e.printStackTrace();
        }
        /*
         *
         * 串口2的数据暂时不使用，等待服务器搭建好后再开启
         *
         */
//        try{
//            mSerialPort_2 = new SerialPort(new File(portPath3), baudrate, 0);
//            mOutputStream_2 = mSerialPort_1.getOutputStream();
//            mReadThread_2 = new ReadThread(mSerialPort_2,2);
//        }catch (IOException e){
//            e.printStackTrace();
//        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void doClientConnection(String uname, String upw,String device_no) {
        //屏蔽网络判断，防止在没有网络的时候，没有自动重连机制
      //  if (isConnectIsNomarl())
        {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.Connect(uname, upw,device_no);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSubscribeForDevId(String devId) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.SubscribeForDevId(devId);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSubscribeForUsername() {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.SubscribeForUsername();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


    public void doSubscribeParsedByDevId(String devId) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.SubscribeParsedByDevId(devId);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void doSubscribeParsedForUsername() {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.SubscribeParsedForUsername();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void doDisSubscribeforDevId(String devId) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.DisSubscribeforDevId(devId);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    public void doDisSubscribeforuName() {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.DisSubscribeforuName();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    public void doDisSubscribeParsedforDevId(String devId) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.DisSubscribeParsedforDevId(devId);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void doDisSubscribeParsedForUsername() {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.DisSubscribeParsedForUsername();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }

    }

    public void publishForDevId(String devId, byte[] data) {
      //  if (isConnectIsNomarl())
        {
            try {
           //     ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.publishForDevId(devId, data);
                data_count_mqtt_send+=data.length;
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }

    public void publishParsedQueryDataPoint(String devId, String slaveIndex, String pointId) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.publishParsedQueryDataPoint(devId, slaveIndex, pointId);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
/********
 *
 * 现在默认使用 作为串口2的数据流
 * slaveIndex 99999
 * pointId   99999
 * *****/
    public void publishParsedSetDataPoint(String devId, String slaveIndex,  String pointId, String value) {
      //  if (isConnectIsNomarl())
        {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.publishParsedSetDataPoint(devId, slaveIndex,  pointId, value);
            } catch (MqttException e) {
                e.printStackTrace();

            }
        }
    }

    public void publishForuName(byte[] data) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.publishForuName(data);
            } catch (MqttException e) {
                e.printStackTrace();

            }
        }
    }

    public boolean doDisConnect() {
        if (isConnectIsNomarl()) {
            try {
                return ysCloudClient.DisConnectUnCheck();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public boolean DisConnectUnCheck() throws MqttException {
        if (isConnectIsNomarl()) {
            return ysCloudClient.DisConnectUnCheck();
        }
        return false;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "宇时4G服务已关闭！");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class MyBinder extends Binder {
        public YsCloudClientService getService() {
            return YsCloudClientService.this;
        }
    }

    /**
     * 判断网络是否连接
     */
    private boolean isConnectIsNomarl() {
        ConnectivityManager connectivityManager = (ConnectivityManager) this.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            String name = info.getTypeName();
            //  Log.i(TAG, "宇时4G当前网络名称：" + name);
            return true;
        } else {
            Log.i(TAG, "宇时4G没有可用网络");
            return false;
        }
    }

        /***
         *
         *
         * 数据回调部分
         *
         *
         * *********/
    public class YsCloudClientCallback extends YsCloudMqttCallbackAdapter {

        private Context mcontext = YsApplication.getInstance();
        @Override
        public void onConnectAck(int returnCode, String description) {
            super.onConnectAck(returnCode, description);
            Log.d(TAG, returnCode + "\n" + description);
            Intent intent = new Intent();
            intent.setAction("onConnectAck");//用隐式意图来启动广播
            intent.putExtra("onConnectAckreturnCode", returnCode);
            intent.putExtra("onConnectAckdescription", description);
            mcontext.sendBroadcast(intent);
            //连接到服务器
            if(returnCode==2)
            {
                //自动订阅设备原始数据流
                doSubscribeForDevId(deviceid);
                //自动订阅Jason数据流 暂时不用
              //  doSubscribeParsedByDevId(deviceid);
            }
        }
        @Override
        public void onSubscribeAck(int messageId, String clientId, String topics, int returnCode) {
            super.onSubscribeAck(messageId, clientId, topics, returnCode);
            //订阅主题成功
            if(returnCode!=0)
                return;
        }

        @Override
        public void onReceiveParsedEvent(int messageId, String topic, String jsonData) {

        }

        @Override
        public void onDisSubscribeAck(int messageId, String clientId, String topics, int returnCode) {

        }

        @Override
        public void onPublishDataAck(int messageId, String topic, boolean isSuccess) {
        }

        /********
        *
        *暂时只处理作为
         * 串口2的数据流
        *其他的命令不处理
        * ***********/
        @Override
        public void onPublishDataResult(int messageId, String topic) {

        }

        /**/
        @Override
        public void onReceiveEvent(int messageId, String topic, byte[] data) {
            //默认只会收到设备的非JSON数据
            data_count_mqtt_get+= data.length;
            try
            {
            mOutputStream_1.write(data);
            }
            catch (Exception e)
            {

            }
            //再采用串口发送数据出去
        }

    }
}
