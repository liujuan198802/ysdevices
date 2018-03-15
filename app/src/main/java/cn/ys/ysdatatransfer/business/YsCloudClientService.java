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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import static android.content.ContentValues.TAG;

/**
 * Created by shizhiyuan on 2017/7/21.
 */

public class YsCloudClientService extends Service {


    private static String uName = "";
    private static String uPW = "";
    private YsCloudClient ysCloudClient;
    private YsCloudClientCallback ysCloudClientCallback;
    private MyBinder mBinder = new MyBinder();
    private static String deviceid="00009385000000000001";
    private long data_count_tcp = 0;
    private long data_count_mqtt =0;
    //客户端端口
    private static ArrayList<Socket> socketList = new ArrayList<Socket>();

    public YsCloudClientService getInstance() {
        return YsCloudClientService.this;
    }
    //TCP 服务器的客户端记录
//    public void set_deviceId(String deviceId) {
//        this.deviceid= deviceId;
//    }
//    public String  get_deviceId()
//    {
//            return  deviceid;
//    }

    public void set_data_count_zero()
    {
        data_count_mqtt=0;
        data_count_tcp = 0;
    }
    public long get_tcp_data_count()
    {
            return data_count_tcp;
    }
    public long get_mqtt_data_count()
    {
        return data_count_mqtt;
    }
    public int get_client_count()
    {
        return socketList.size();
    }
    public boolean get_tcp_state()
    {
        if(ysCloudClient==null)
            return  false;
        if(serverlistenThread==null)
            return  false;
            return  (serverlistenThread.isAlive()&&ysCloudClient.is_mqtt_connected());
    }
    public   void start_tcp_server(String stingport) throws IOException {

        // 定义保存所有Socket的ArrayList
        int port;
        try {
            port = Integer.parseInt(stingport);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            throw new IOException();
        }
        ServerSocket ss = new ServerSocket(port);
        //回收已经开启的线程
        if(serverlistenThread!=null ) {
            try{
                serverlistenThread_local.ss.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
            serverlistenThread = null;
            serverlistenThread_local = new ServerlistenThread(ss);
            serverlistenThread = new Thread(serverlistenThread_local);
            serverlistenThread.start();
        }

    //关闭已经开启的TCP服务及线程
    public void stop_tcp_listen()   {
        doDisSubscribeforDevId(deviceid);
        for (Socket s : socketList)
        {
            try {
                s.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        socketList.clear();
        //回收已经开启的线程
        if(serverlistenThread!=null ) {
            try{
                serverlistenThread_local.ss.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            serverlistenThread = null;
        }
    }
    // TCPserver 监听接收线程，关闭的时候，关闭该线程
    private Thread serverlistenThread;
    private ServerlistenThread serverlistenThread_local;

    public class ServerlistenThread implements Runnable
    {
        ServerSocket ss = null;
        public ServerlistenThread(ServerSocket s) {
            ss = s;
        }
        public void run()
        {
            System.out.println("宇时4G数传TCP服务正在工作...");
            while (true) {
                Socket s = null;
                try {
                    s = ss.accept();
                } catch (Exception e) {
                    e.printStackTrace();
                    break;
                }
                //打印连接的设备的IP地址
                InetAddress address = s.getInetAddress();
                System.out.println(address);
                socketList.add(s);
                System.out.println("客户端："+s.getRemoteSocketAddress()+"连接到服务器");
                // 每当客户端连接后启动一条ServerThread线程为该客户端服务
                try {
                    new Thread(new ServerThread(s)).start();
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            //关闭已经开启的端口
            try {
                ss.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("宇时4G数传TCP服务已停止！");
        }
    }

    // 负责处理每个线程通信的线程类
    public class ServerThread implements Runnable
    {
        // 定义当前线程所处理的Socket
        Socket s = null;
        // 该线程所处理的Socket所对应的输入流
        InputStream br = null;
        public ServerThread(Socket s) throws IOException {
            this.s = s;
            // 初始化该Socket对应的输入流
            br = s.getInputStream();
            System.out.println("客户端："+s.getRemoteSocketAddress()+"初始成功");
        }

        public void run()
        {
            String content = null;
                // 采用循环不断从Socket中读取客户端发送过来的数据
            while (true)
            {
                byte buffer [] = new byte[256];
                int temp = 0;
                //从InputStream当中读取客户端所发送的数据
                try {
                    while((temp = br.read(buffer)) != -1){
                        data_count_tcp+=temp;
                       // System.out.println("客户端："+s.getRemoteSocketAddress()+"获取到数据："+new String(buffer,0,temp));
                               if(deviceid!=null)
                            //发送数据到MQTT服务器端
                            try {
                                   byte[] data = new byte[temp];
                                System.arraycopy(buffer, 0, data, 0, temp);
                                YsCloudClientService.this.publishForDevId(deviceid,data);
                            }
                            catch (Exception e)
                            {
                            }
                    }
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    break;
                }
            }
            socketList.remove(s);
            System.out.println("客户端:"+s.getRemoteSocketAddress()+"已断开链接！");
        }
    }
    public static int returnActualLength(byte[] data) {
        int i = 0;
        for (; i < data.length; i++) {
            if (data[i] == '\0')
                break;
        }
        return i;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        ysCloudClientCallback = new YsCloudClientCallback(){
            @Override
            public void onReceiveEvent(int messageId, String topic, byte[] data) {
                super.onReceiveEvent(messageId,topic,data);
                 data_count_mqtt += data.length;
                    for (Socket s : YsCloudClientService.socketList)
                    {
                        try {
                        OutputStream os = s.getOutputStream();
                        os.write(data,0,data.length);
                      }
                    catch (Exception e)
                    {
                        YsCloudClientService.socketList.remove(s);
                        e.printStackTrace();
                    }
                    }
                }
            @Override
            public void onConnectAck(int returnCode, String description) {
                super.onConnectAck(returnCode, description);
                //连接到服务器
                if(returnCode==2)
                {
                    //自动订阅设备
                    YsCloudClientService.this.doSubscribeForDevId(deviceid);
                }
            }
        };
        ysCloudClient = new YsCloudClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("onStartCommand");
        Bundle bundle = intent.getExtras();
        uName = bundle.getString("uname");
        uPW = bundle.getString("upw");
        deviceid = bundle.getString("clientid");
        doClientConnection(uName, uPW);
        return super.onStartCommand(intent, flags, startId);
    }
    private void doClientConnection(String uname, String upw) {
        if (isConnectIsNomarl()) {
            try {
                ysCloudClient.setUsrCloudMqttCallback(ysCloudClientCallback);
                ysCloudClient.Connect(uname, upw);
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

    public void publishParsedSetDataPoint(String devId, String slaveIndex,  String pointId, String value) {
        if (isConnectIsNomarl()) {
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
            stop_tcp_listen();
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

}
