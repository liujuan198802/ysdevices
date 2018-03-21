//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.Ysserver;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.Ysserver.Interface.YsCloudMqttCallback;
import cn.Ysserver.Interface.YsCloudMqttClient;
import cn.Ysserver.impl.YsCloudMqttClientImpl;

public class YsCloudMqttClientAdapter implements YsCloudMqttClient {
    private YsCloudMqttClient usrCloudMqttClient = new YsCloudMqttClientImpl();

    public YsCloudMqttClientAdapter() {
    }

    public void Connect(String userName, String passWord,String device_id) throws MqttException {
        this.usrCloudMqttClient.Connect(userName, passWord,device_id);
    }
    public boolean is_mqtt_connected()
    {
        return  this.usrCloudMqttClient.is_mqtt_connected();
    }
    public boolean DisConnectUnCheck() throws MqttException {
        return this.usrCloudMqttClient.DisConnectUnCheck();
    }

    public void SubscribeForDevId(String devId) throws MqttException {
        this.usrCloudMqttClient.SubscribeForDevId(devId);
    }
    public void SubscribeForTopic(String topic) throws MqttException
    {
        this.usrCloudMqttClient.SubscribeForTopic(topic);
    }
    public void SubscribeForUsername() throws MqttException {
        this.usrCloudMqttClient.SubscribeForUsername();
    }

    public void DisSubscribeforDevId(String devId) throws MqttException {
        this.usrCloudMqttClient.DisSubscribeforDevId(devId);
    }

    public void DisSubscribeforuName() throws MqttException {
        this.usrCloudMqttClient.DisSubscribeforuName();
    }

    public void setUsrCloudMqttCallback(YsCloudMqttCallback CloudMqttCallback) {
        this.usrCloudMqttClient.setUsrCloudMqttCallback(CloudMqttCallback);
    }

    public void publishForDevId(String devId, byte[] data) throws MqttException {
        this.usrCloudMqttClient.publishForDevId(devId, data);
    }
    public void publishForDevId2(String devId, byte[] data) throws MqttException {
        this.usrCloudMqttClient.publishForDevId2(devId, data);
    }
    public void publishForDevTopic(String devId, byte[] data) throws MqttException
    {
        this.usrCloudMqttClient.publishForDevTopic(devId, data);
    }
    public void publishForuName(byte[] data) throws MqttException {
        this.usrCloudMqttClient.publishForuName(data);
    }

    public void SubscribeParsedByDevId(String devId) throws MqttException {
        this.usrCloudMqttClient.SubscribeParsedByDevId(devId);
    }

    public void SubscribeParsedForUsername() throws MqttException {
        this.usrCloudMqttClient.SubscribeParsedForUsername();
    }

    public void DisSubscribeParsedforDevId(String devId) throws MqttException {
        this.usrCloudMqttClient.DisSubscribeParsedforDevId(devId);
    }

    public void DisSubscribeParsedForUsername() throws MqttException {
        this.usrCloudMqttClient.DisSubscribeParsedForUsername();
    }

    public void publishParsedSetDataPoint(String devId, String slaveIndex, String pointId, String value) throws MqttException {
        this.usrCloudMqttClient.publishParsedSetDataPoint(devId, slaveIndex, pointId, value);
    }

    public void publishParsedQueryDataPoint(String devId, String slaveIndex, String pointId) throws MqttException {
        this.usrCloudMqttClient.publishParsedQueryDataPoint(devId, slaveIndex, pointId);
    }
}
