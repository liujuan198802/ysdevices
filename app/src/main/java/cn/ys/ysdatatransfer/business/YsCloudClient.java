package cn.ys.ysdatatransfer.business;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.Ysserver.Interface.YsCloudMqttCallback;
import cn.Ysserver.YsCloudMqttClientAdapter;

/**
 * Created by shizhiyuan on 2017/7/21.
 */

public class YsCloudClient extends YsCloudMqttClientAdapter {

    @Override
    public void Connect(String userName, String passWord,String device_id) throws MqttException {
        super.Connect(userName, passWord,device_id);
    }



    @Override
    public boolean DisConnectUnCheck() throws MqttException {
        return super.DisConnectUnCheck();
    }

    @Override
    public void SubscribeForDevId(String devId) throws MqttException {
        super.SubscribeForDevId(devId);
    }

    @Override
    public void SubscribeForUsername() throws MqttException {
        super.SubscribeForUsername();
    }
    @Override
    public void SubscribeForTopic(String topic) throws MqttException {
        super.SubscribeForTopic(topic);
    }

    @Override
    public void DisSubscribeforDevId(String devId) throws MqttException {
        super.DisSubscribeforDevId(devId);
    }

    @Override
    public void DisSubscribeforuName() throws MqttException {
        super.DisSubscribeforuName();
    }

    @Override
    public void setUsrCloudMqttCallback(YsCloudMqttCallback CloudMqttCallback) {
        super.setUsrCloudMqttCallback(CloudMqttCallback);
    }

    @Override
    public void publishForDevId(String devId, byte[] data) throws MqttException {
        super.publishForDevId(devId, data);
    }
    @Override
    public void publishForDevId2(String devId, byte[] data) throws MqttException {
        super.publishForDevId2(devId, data);
    }
    @Override
    public void publishForDevTopic(String topic, byte[] data) throws MqttException {
        super.publishForDevTopic(topic, data);
    }
    @Override
    public void publishForuName(byte[] data) throws MqttException {
        super.publishForuName(data);
    }
}
