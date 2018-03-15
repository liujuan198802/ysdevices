//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.usr;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.usr.Interface.UsrCloudMqttCallback;
import cn.usr.Interface.UsrCloudMqttClient;
import cn.usr.impl.UsrCloudMqttClientImpl;

public class UsrCloudMqttClientAdapter implements UsrCloudMqttClient {
    private UsrCloudMqttClient usrCloudMqttClient = new UsrCloudMqttClientImpl();

    public UsrCloudMqttClientAdapter() {
    }

    public void Connect(String userName, String passWord) throws MqttException {
        this.usrCloudMqttClient.Connect(userName, passWord);
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

    public void SubscribeForUsername() throws MqttException {
        this.usrCloudMqttClient.SubscribeForUsername();
    }

    public void DisSubscribeforDevId(String devId) throws MqttException {
        this.usrCloudMqttClient.DisSubscribeforDevId(devId);
    }

    public void DisSubscribeforuName() throws MqttException {
        this.usrCloudMqttClient.DisSubscribeforuName();
    }

    public void setUsrCloudMqttCallback(UsrCloudMqttCallback CloudMqttCallback) {
        this.usrCloudMqttClient.setUsrCloudMqttCallback(CloudMqttCallback);
    }

    public void publishForDevId(String devId, byte[] data) throws MqttException {
        this.usrCloudMqttClient.publishForDevId(devId, data);
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
