package cn.ys.ysdatatransfer.business;

import android.util.Log;

import com.alibaba.fastjson.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.Queue;

import android_serialport_api.SerialPort;
import cn.ys.ysdatatransfer.base.YsApplication;
import cn.ys.ysdatatransfer.entity.Device_cmd;
import cn.ys.ysdatatransfer.entity.Device_info;

/**
 * Created by lenovo on 2016/2/23.
 */
public class UDPClient implements Runnable{
    int udpPort ;
    private SerialPort serialPort;
    private   String hostIp = null;
    private  DatagramSocket socket = null;
    private  DatagramPacket packetSend,packetRcv;
    private boolean udpLife = true; //udp生命线程
    private byte[] msgRcv = new byte[512]; //接收消息
    private  InetAddress hostAddress = null;
    private  SendMsgThread sendMsgThread =null;
    public UDPClient(SerialPort serialPort,int port){
        super();
        udpPort = port;
        this.serialPort= serialPort;
        hostIp = YsApplication.getBoradcastIP();
        try {
            if(socket==null){
                socket = new DatagramSocket(null);
                socket.setBroadcast(true);
                socket.bind(new InetSocketAddress(udpPort-1));
            }
        } catch (SocketException e) {
            Log.i("udpClient","建立接收数据报失败");
            e.printStackTrace();
        }
        sendMsgThread = new SendMsgThread();
    }

    //返回udp生命线程因子是否存活
    public boolean isUdpLife(){
        if (udpLife){
            return true;
        }

        return false;
    }

    //更改UDP生命线程因子
    public void setUdpLife(boolean b){
        udpLife = b;
    }
    //发送消息

   public void send(final byte[]  msgSend){
       sendMsgThread.putMsg(msgSend);
   }

    @Override
    public void run() {
        sendMsgThread.start();
        packetRcv = new DatagramPacket(msgRcv, msgRcv.length);
        Log.i("udpClient", "UDP监听");
        if (serialPort == null) {
            while (udpLife) {
                try {
                    socket.receive(packetRcv);
                    String jsondata = new String(packetRcv.getData(),0,packetRcv.getLength());
                    try {
                        JSONObject jsonObject = JSONObject.parseObject(jsondata);
                        JSONObject jsonObject1 = jsonObject.getJSONObject("deviceCmd");
                        Device_cmd device_cmd = new Device_cmd();
                        device_cmd.setClient_id(jsonObject1.getString("client_id"));
                        device_cmd.setCmd_name(jsonObject1.getString("cmd_name"));
                        device_cmd.setCmd_state(jsonObject1.getString("cmd_state"));
                        Log.d("宇时4G：","收到JSon："+device_cmd.toString());
                            Device_info device_info=YsDeal_Cmd.dealwithcmd(device_cmd);
                            if(device_info!=null)
                            {
                                send(device_info.toString().getBytes("GBK"));
                            }
                    }
                    catch(Exception e)
                    {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            while (udpLife) {
                try {
                    socket.receive(packetRcv);
               
                    serialPort.getOutputStream().write(packetRcv.getData(),0,packetRcv.getLength());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        Log.i("udpClient","UDP监听关闭");
        socket.close();
    }
    private class SendMsgThread extends Thread {
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
                while (udpLife) {
                    Log.i("udpClient", "UDP发送");
                    // 当队列里的消息发送完毕后，线程等待
                    while (sendMsgQuene.size() > 0) {
                        byte[] msg = sendMsgQuene.poll();
                        if (socket != null)
                            try {
                                packetSend = new DatagramPacket(msg, msg.length,
                                        InetAddress.getByName(hostIp), udpPort);
                                socket.send(packetSend);
                            } catch (UnknownHostException e) {
                                e.printStackTrace();
                                System.out.println("发送失败");
                            } catch (IOException e) {
                                e.printStackTrace();
                                System.out.println("发送失败");
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