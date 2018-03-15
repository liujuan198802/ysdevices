//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.usr.Interface;

import org.eclipse.paho.client.mqttv3.MqttException;

public interface UsrCloudMqttClient {
    void setUsrCloudMqttCallback(UsrCloudMqttCallback var1);

    void Connect(String var1, String var2) throws MqttException;

    boolean DisConnectUnCheck() throws MqttException;

    void SubscribeForDevId(String var1) throws MqttException;

    void SubscribeParsedByDevId(String var1) throws MqttException;

    void SubscribeForUsername() throws MqttException;

    void SubscribeParsedForUsername() throws MqttException;

    void DisSubscribeforDevId(String var1) throws MqttException;

    void DisSubscribeParsedforDevId(String var1) throws MqttException;

    void DisSubscribeforuName() throws MqttException;

    void DisSubscribeParsedForUsername() throws MqttException;

    void publishForDevId(String var1, byte[] var2) throws MqttException;

    void publishForuName(byte[] var1) throws MqttException;

    void publishParsedSetDataPoint(String var1, String var2, String var3, String var4) throws MqttException;

    void publishParsedQueryDataPoint(String var1, String var2, String var3) throws MqttException;
}
