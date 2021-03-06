//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.Ysserver.entity;

import android.content.Context;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Properties;

import cn.Ysserver.utils.BeasUtils;
import cn.ys.ysdatatransfer.base.YsApplication;

public class MqttPropertise {
    private static Properties prop = BeasUtils.getPropertie(MqttPropertise.class, "mqtt.properties");
    private static OutputStream fos;
   // public static final String SERVER_ADDRESS ="tcp://service.cloudoftime.com:1883";
    //测试地址
    public static final String SERVER_ADDRESS ="tcp://192.168.88.180:1883";
    public static final String USR_NAME;
    public static final String CLIENTID_PREFIX;
    public static final String TOPIC_SUBSCRIBE_DEV_RAW;
    public static final String TOPIC_SUBSCRIBE_DEV_RAW2;
    public static final String TOPIC_SUBSCRIBE_USER_RAW;
    public static final String TOPIC_PUBLISH_DEV_RAW;
    public static final String TOPIC_PUBLISH_DEV_RAW2;
    public static final String TOPIC_PUBLISH_USER_RAW;
    public static final String TOPIC_SUBSCRIBE_DEV_PARSED;
    public static final String TOPIC_SUBSCRIBE_USER_PARSED;
    public static final String TOPIC_PUBLISH_DEV_PARSED;
    public static final String JSON_SETDATAPOINT;
    public static final String JSON_QUERYDATAPOINT;
    public static final String SERIAL1_RATE;
    public static final String SERIAL2_RATE;
    public static  final String SERIAL1_ENABLE;
    public static  final String SERIAL2_ENABLE;
    public static final String  UDP_PORT1;
    public static  final String UDP_PORT2;
    public static final String TOPIC_SUBSCRIBE_DEV_CMD;
    public static final String TOPIC_PUBLISH_USER_INFO;
    public static final String DEVID = "<Id>";
    public static final String USERACCOUNT = "<Account>";
    public static final String POINTID = "%POINTID%";
    public static final String POINTVALUE = "%POINTVALUE%";
    public static final String SLAVEINDEX = "%SLAVEINDEX%";
    public static final String JSONKEY = "JsonTx";
    public static final int SUCCESS = 0;
    public static final int FAILE = 1;
    public static final int CONNECTCOMPLETE = 2;
    public static final int CONNECTBREAK = 3;

    public MqttPropertise() {
    }
    public static void setproperty(String keyname ,String keyvalue)
    {
        prop.setProperty(keyname,keyvalue);
        try {
            fos =  YsApplication.getInstance().openFileOutput("mqtt.properties", Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }
        try {
            prop.store(fos, null);
        } catch ( IOException e ) {
            e.printStackTrace ( );
        }
        try {
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getproperty(String keyname)
    {
        try {
            return prop.getProperty(keyname);
        }
        catch (RuntimeException e)
        {
            return null;
        }
    }
    static {
        CLIENTID_PREFIX = prop.getProperty("clientid_prefix");
        TOPIC_SUBSCRIBE_DEV_RAW = "$USR/DevRx/<Id>";
        TOPIC_SUBSCRIBE_DEV_RAW2 ="$USR/DevRx2/<Id>";
        TOPIC_SUBSCRIBE_USER_RAW = "$USR/Dev2App/<Account>/+";
        TOPIC_PUBLISH_DEV_RAW ="$USR/DevTx/<Id>";
        TOPIC_PUBLISH_DEV_RAW2 = "$USR/DevTx2/<Id>";
        TOPIC_PUBLISH_USER_RAW = "$USR/App2Dev/<Account>";
        TOPIC_SUBSCRIBE_DEV_PARSED = "$USR/DevJsonRx/<Id>";
        TOPIC_SUBSCRIBE_USER_PARSED ="$USR/JsonRx/<Account>/+";
        TOPIC_PUBLISH_DEV_PARSED ="$USR/DevJsonTx/<Id>";
        JSON_SETDATAPOINT = prop.getProperty("json_setDataPoint");
        JSON_QUERYDATAPOINT = prop.getProperty("json_queryDataPoint");
        SERIAL1_RATE = prop.getProperty("baudrate_serail1");
        SERIAL2_RATE = prop.getProperty("baudrate_serial2");
        SERIAL1_ENABLE= prop.getProperty("enable_serail1");
        SERIAL2_ENABLE = prop.getProperty("enable_serail2");
        UDP_PORT1 = prop.getProperty("udp_port1");
        UDP_PORT2 = prop.getProperty("udp_port2");
        USR_NAME =prop.getProperty("device_usr_name");
        TOPIC_PUBLISH_USER_INFO ="$USR/DevInfo/<Id>";
        TOPIC_SUBSCRIBE_DEV_CMD = "$USR/DevCmd/<Id>";
    }
}
