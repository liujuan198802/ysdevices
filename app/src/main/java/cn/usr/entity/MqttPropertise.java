//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.usr.entity;

import java.util.Properties;

import cn.usr.utils.BeasUtils;

public class MqttPropertise {
    private static Properties prop = BeasUtils.getPropertie(MqttPropertise.class, "mqtt.properties");
    public static final String SERVER_ADDRESS;
    public static final String CLIENTID_PREFIX;
    public static final String TOPIC_SUBSCRIBE_DEV_RAW;
    public static final String TOPIC_SUBSCRIBE_USER_RAW;
    public static final String TOPIC_PUBLISH_DEV_RAW;
    public static final String TOPIC_PUBLISH_USER_RAW;
    public static final String TOPIC_SUBSCRIBE_DEV_PARSED;
    public static final String TOPIC_SUBSCRIBE_USER_PARSED;
    public static final String TOPIC_PUBLISH_DEV_PARSED;
    public static final String JSON_SETDATAPOINT;
    public static final String JSON_QUERYDATAPOINT;
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

    static {
        SERVER_ADDRESS = prop.getProperty("server_address");
        CLIENTID_PREFIX = prop.getProperty("clientid_prefix");
        TOPIC_SUBSCRIBE_DEV_RAW = prop.getProperty("topic_subscribe_dev_raw");
        TOPIC_SUBSCRIBE_USER_RAW = prop.getProperty("topic_subscribe_user_raw");
        TOPIC_PUBLISH_DEV_RAW = prop.getProperty("topic_publish_dev_raw");
        TOPIC_PUBLISH_USER_RAW = prop.getProperty("topic_publish_user_raw");
        TOPIC_SUBSCRIBE_DEV_PARSED = prop.getProperty("topic_subscribe_dev_parsed");
        TOPIC_SUBSCRIBE_USER_PARSED = prop.getProperty("topic_subscribe_user_parsed");
        TOPIC_PUBLISH_DEV_PARSED = prop.getProperty("topic_publish_dev_parsed");
        JSON_SETDATAPOINT = prop.getProperty("json_setDataPoint");
        JSON_QUERYDATAPOINT = prop.getProperty("json_queryDataPoint");
    }
}
