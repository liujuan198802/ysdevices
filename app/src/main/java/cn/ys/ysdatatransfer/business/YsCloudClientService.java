package cn.ys.ysdatatransfer.business;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android_serialport_api.SerialPort;
import cn.Ysserver.YsCloudMqttCallbackAdapter;
import cn.Ysserver.entity.MqttPropertise;
import cn.ys.ysdatatransfer.base.YsApplication;
import cn.ys.ysdatatransfer.entity.Device_info;

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
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    public YsCloudClientService getInstance() {
        return YsCloudClientService.this;
    }
    /*******
     *
     *串口部分代码
     *
     *
     * *****/


    private boolean serail1_enable;
    private boolean serail2_enable;
    private int serail1_rate;
    private int serail2_rate;


    private ReadThread comReadThread_1;
    private ReadThread comReadThread_2;
   // String portPath0 = "/dev/ttyMT0";
    String portPath2 = "/dev/ttyMT2";
    String portPath3 = "/dev/ttyMT3";
    private SerialPort mSerialPort_1 = null;
    private SerialPort mSerialPort_2 = null;
    private ReadThread mReadThread_1;
    private ReadThread mReadThread_2;
    private Handler charg_handler = new Handler();
    Device_info device_info_retrun = new Device_info();

    public void send_state_all(Device_info device_info)
    {
        try {
            try {
                udpClients_cmd.send(device_info.toString().getBytes("GBK"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            publishForDevIdInfo(device_info);
        }
       catch (RuntimeException e)
       {

       }
    }
    /**
     * 定时器 每隔一段时间执行一次
     */
    private Timer mTimer = null;
    private TimerTask mTimerTask = null;
    private boolean isStop = false;
    private void startTimer() {
        isStop = true;//定时器启动后，修改标识，关闭定时器的开关
        if (mTimer == null) {
            mTimer = new Timer();
        }
        if (mTimerTask == null) {
            mTimerTask = new TimerTask() {
                @Override
                public void run() {
                    do {
                        timer_count--;
                        device_info_retrun.setInfo_state("宇时4G数传将在"+timer_count+"S后关闭...."+YsApplication.getNowTime());
                        send_state_all(device_info_retrun);
                        Device_info device_info =new Device_info();
                        device_info.setClient_id(YsApplication.getCLIENTID());
                        device_info.setInfo_name("device_pos");
                        device_info.setInfo_state(YsApplication.get_pos());
                        send_state_all(device_info);
                        if(timer_count<=0)
                        {
                            device_info_retrun.setInfo_state("宇时4G数传将在即将关闭，谢谢使用...."+YsApplication.getNowTime());
                            send_state_all(device_info_retrun);
                            YsDeal_Cmd.shutdown_device();
                        }
                        try {
                            Thread.sleep(1000);//3秒后再次执行
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    } while (isStop);

                }
            };
        }

        if (mTimer != null && mTimerTask != null) {
            Log.d("tag", "mTimer.schedule(mTimerTask, delay)");
            mTimer.schedule(mTimerTask, 0);//执行定时器中的任务
        }
    }
    /**
     * 停止定时器，初始化定时器开关
     */
    private void stopTimer() {

        if (mTimer != null) {
            mTimer.cancel();
            mTimer = null;
        }
        if (mTimerTask != null) {
            mTimerTask.cancel();
            mTimerTask = null;
        }
        isStop = false;//重新打开定时器开关
        Log.d("tag", "isStop="+isStop);

    }
    private  static int  timer_count = Integer.valueOf(MqttPropertise.getproperty("shoutdown_time"));;
    BroadcastReceiver powerbroadcastReceiver= new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            //充电类型 BatteryManager.BATTERY_PLUGGED_AC 表示是充电器，不是这个值，表示是 USB
            int plugged = intent.getIntExtra("plugged",0);
                if(plugged!=0) {
                    stopTimer();
                    timer_count =Integer.valueOf(MqttPropertise.getproperty("shoutdown_time"));
                    device_info_retrun.setInfo_state("宇时4G数传正在工作..."+YsApplication.getNowTime());
                        send_state_all(device_info_retrun);
                    }
                else {
                       startTimer();
                        device_info_retrun.setInfo_state("宇时4G数传电源已断开..."+YsApplication.getNowTime());
                        send_state_all(device_info_retrun);
                }
            }
    };
    //读取串口数据线程
    public class ReadThread extends Thread {

        private SerialPort mSerialPort = null;
        protected InputStream mInputStream=null;
        protected OutputStream mOutputStream=null;
        private int port_num;
        private Com_SendMsgThread com_sendMsgThread =null;
        private UDPClient udpClient=null;
        public ReadThread(SerialPort serialPort,int port_no,UDPClient udpClient)
        {
            this.udpClient = udpClient;
            this.port_num= port_no;
            mSerialPort = serialPort;
            if(mSerialPort!=null)
            {
              mInputStream = mSerialPort.getInputStream();
              mOutputStream =mSerialPort.getOutputStream();
            }
            com_sendMsgThread = new Com_SendMsgThread();
        }
        public void com_send(final byte[]  msgSend){
            com_sendMsgThread.putMsg(msgSend);
        }
        @Override
        public void run() {
            super.run();
            com_sendMsgThread.start();
            while(!isInterrupted()) {
                int size;
                Log.d(TAG, "串口线程已启动！");
                try {
                    byte[] buffer = new byte[512];
                    if (mInputStream == null) return;
                    while((size=mInputStream.read(buffer)) != -1){
                        if (size > 0) {
                            data_count_com_get+=size;
                            Log.d(TAG, port_num+"com receive data,size:"+size+buffer.toString() );
                            byte[] data = new byte[size];
                            System.arraycopy(buffer, 0, data, 0, size);
                            Log.d(TAG, port_num+"com receive data,size:"+size+data.toString() );
                            udpClient.send(data);
                            if(port_num==1)
                            {
                                publishForDevId(deviceid,data);
                            }
                            else
                            {
                                publishForDevId2(deviceid,data);
                            }
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
            Log.v(TAG, "串口线程已结束！");
        }
        private class Com_SendMsgThread extends Thread {
            // 发送消息的队列
            private Queue<byte[]> sendMsgQuene = new LinkedList<byte[]>();
            // 是否发送消息
            private boolean send = true;

            public synchronized void putMsg(byte[] msg) {
                // 唤醒线程
                if (sendMsgQuene.size() == 0)
                    notify();
                sendMsgQuene.offer(msg);
            }
            public void run() {
                synchronized (this) {
                    while (send) {
                        Log.d("串口发送", "串口发送线程已启动");
                        // 当队列里的消息发送完毕后，线程等待
                        while (sendMsgQuene.size() > 0) {
                            byte[] msg = sendMsgQuene.poll();
                            try {
                                mOutputStream.write(msg,0,msg.length);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        try {
                            wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
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
        device_info_retrun.setClient_id(YsApplication.getCLIENTID());
        device_info_retrun.setInfo_name("text");
        ysCloudClientCallback = new YsCloudClientCallback();
        ysCloudClient = new YsCloudClient();
        serail1_enable = MqttPropertise.SERIAL1_ENABLE.equals("true");
        serail2_enable = MqttPropertise.SERIAL2_ENABLE.equals("true");
        serail1_rate = Integer.valueOf(MqttPropertise.SERIAL1_RATE);
        serail2_rate = Integer.valueOf(MqttPropertise.SERIAL2_RATE);
        start_serail(serail1_enable,serail2_enable,serail1_rate,serail2_rate);
        udpClients_cmd =new UDPClient(null,3840);
        ExecutorService exec = Executors.newCachedThreadPool();
        exec.execute(udpClients_cmd);
        udpClients_cmd.send("i am setting server".getBytes());
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        this.registerReceiver(powerbroadcastReceiver, filter);
    }
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        try {
            Bundle bundle = intent.getExtras();
            uName = bundle.getString("uname");
            uPW = bundle.getString("upw");
            deviceid = bundle.getString("clientid");
            doClientConnection(uName, uPW,deviceid);
        }
        catch (RuntimeException e)
        {
        }
        return super.onStartCommand(intent, flags, startId);
    }
    private void close_com_port()
    {
        if (mReadThread_1 != null)
            mReadThread_1.interrupt();
        if(mSerialPort_1 != null){
            mSerialPort_1.close();
            mSerialPort_1 = null;
        }
        if (mReadThread_2 != null)
            mReadThread_2.interrupt();
        if(mSerialPort_2 != null){
            mSerialPort_2.close();
            mSerialPort_2 = null;
        }
    }
    private UDPClient udpClients1=null;
    private  UDPClient udpClients2 =null;
    private UDPClient udpClients_cmd=null;
    private void start_serail(boolean s1,boolean s2 ,int rate1,int rate2)
    {
        close_com_port();
        if(s1) {
            try {
                mSerialPort_1 = new SerialPort(new File(portPath2), rate1, 0);
                udpClients1 =new UDPClient(mSerialPort_1,Integer.valueOf(MqttPropertise.UDP_PORT1));
                ExecutorService exec = Executors.newCachedThreadPool();
                exec.execute(udpClients1);
                mReadThread_1 = new ReadThread(mSerialPort_1, 1,udpClients1);
                mReadThread_1.start();
                udpClients1.send(("I am serail1\'s udp client..."+YsApplication.getNowTime()).getBytes("GBK"));
       } catch (SecurityException e) {
                //
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(s2) {
            try {
                mSerialPort_2 = new SerialPort(new File(portPath3), rate2, 0);
                udpClients2 =new UDPClient(mSerialPort_2,Integer.valueOf(MqttPropertise.UDP_PORT2));
                ExecutorService exec = Executors.newCachedThreadPool();
                exec.execute(udpClients2);
                mReadThread_2 = new ReadThread(mSerialPort_2, 2,udpClients2);
                udpClients2.send(("I am serail2\'s udp client..."+YsApplication.getNowTime()).getBytes("GBK"));
                    } catch (SecurityException e) {
                //
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
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
    public void doSubscribeForDevId2(String devId) {
        if (isConnectIsNomarl()) {
            try {
                String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_RAW2.replaceAll("<Id>", devId);
                ysCloudClient.SubscribeForTopic(topic);
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
    public void doSubscribeForDevCmd(String devId) {
        if (isConnectIsNomarl()) {
            try {
                String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_CMD.replaceAll("<Id>", devId);
                ysCloudClient.SubscribeForTopic(topic);
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

            try {
                ysCloudClient.publishForDevId(devId, data);
                data_count_mqtt_send+=data.length;
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }
    public void publishForDevId2(String devId, byte[] data) {
            try {
                ysCloudClient.publishForDevId2(devId, data);
            } catch (MqttException e) {
                e.printStackTrace();
            }
    }
    //直接设备状态
    public void publishForDevIdInfo(Device_info device_info) {
            try {
                String topic = MqttPropertise.TOPIC_PUBLISH_USER_INFO.replaceAll("<Id>", deviceid);
                ysCloudClient.publishForDevTopic(topic, device_info.toString().getBytes("GBK"));
            } catch (MqttException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
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
        stopTimer();
        doDisConnect();
        close_com_port();
        udpClients_cmd.setUdpLife(false);
        if(udpClients1!=null)
            udpClients1.setUdpLife(false);
        if(udpClients2!=null)
            udpClients2.setUdpLife(false);
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
                doSubscribeForDevCmd(deviceid);
                if(serail1_enable)
                doSubscribeForDevId(deviceid);
                if(serail2_enable)
                doSubscribeForDevId2(deviceid);
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
            if(topic.contains("DevRx2"))
            { try
            {
                mReadThread_2.com_send(data);
            }
             catch (Exception e)
             {
             }
             return;
            }
            if(topic.contains("DevRx")){
                try
                {
                    mReadThread_1.com_send(data);
                }
                catch (Exception e)
                {
                }
                return;
            }
            Intent intent = new Intent();
            intent.setAction("onReceiveCmdEvent");//用隐式意图来启动广播
            Bundle bundle = new Bundle();
            bundle.putInt("messageId", messageId);
            bundle.putString("topic", topic);
            bundle.putString("cmddata", new String(data));
            intent.putExtras(bundle);
            mcontext.sendBroadcast(intent);
        }

    }
}
