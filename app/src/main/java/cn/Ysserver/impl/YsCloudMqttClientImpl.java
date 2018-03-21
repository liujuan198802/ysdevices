//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.Ysserver.impl;

import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.Arrays;

import cn.Ysserver.Interface.YsCloudMqttCallback;
import cn.Ysserver.Interface.YsCloudMqttClient;
import cn.Ysserver.entity.MqttPropertise;
import cn.Ysserver.utils.BeasUtils;

public class YsCloudMqttClientImpl implements MqttCallbackExtended, YsCloudMqttClient {
    private YsCloudMqttCallback usrCloudMqttCallback;
    private volatile String userName;
    private volatile MqttAsyncClient mqttAsyncClient;

    public YsCloudMqttClientImpl() {
    }

    public void setUsrCloudMqttCallback(YsCloudMqttCallback usrCloudMqttCallback) {
        this.usrCloudMqttCallback = usrCloudMqttCallback;
    }

    public void Connect(String userName, String passWord,String device_id) throws MqttException {
        String clientId = device_id;
        this.userName = userName;
        this.mqttAsyncClient = new MqttAsyncClient(MqttPropertise.SERVER_ADDRESS, clientId, new MemoryPersistence());
        MqttConnectOptions options = new MqttConnectOptions();
        options.setCleanSession(true);
        options.setUserName(userName);
        options.setPassword(BeasUtils.getMD5(passWord).toCharArray());
        options.setConnectionTimeout(20);
        options.setKeepAliveInterval(600);
        options.setAutomaticReconnect(true);
        this.mqttAsyncClient.setCallback(this);
        this.mqttAsyncClient.connect(options, (Object)null, new IMqttActionListener() {
            public void onSuccess(IMqttToken token) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onConnectAck(0, "连接成功");
                }

            }

            public void onFailure(IMqttToken token, Throwable throwable) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onConnectAck(1, throwable.toString());
                }

            }
        });
    }
    public boolean is_mqtt_connected()
    {
        return  mqttAsyncClient.isConnected();
    }
    public boolean DisConnectUnCheck() throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            return false;
        } else {
            this.mqttAsyncClient.disconnect();
            return true;
        }
    }

    public void SubscribeForDevId(String devId) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_RAW.replaceAll("<Id>", devId);
            this.Subscribe(topic);
        }
    }
    public void SubscribeForTopic(String topic) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            this.Subscribe(topic);
        }
    }

    public void SubscribeForUsername() throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_USER_RAW.replaceAll("<Account>", this.userName);
            this.Subscribe(topic);
        }
    }

    public void SubscribeParsedByDevId(String devId) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_PARSED.replaceAll("<Id>", devId);
            this.Subscribe(topic);
        }
    }

    public void SubscribeParsedForUsername() throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_USER_PARSED.replaceAll("<Account>", this.userName);
            this.Subscribe(topic);
        }
    }

    private void Subscribe(String topic) throws MqttException {
        int[] Qos = new int[]{0};
        String[] topics = new String[]{topic.trim()};
        this.mqttAsyncClient.subscribe(topics, Qos).setActionCallback(new IMqttActionListener() {
            public void onSuccess(IMqttToken iMqttToken) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onSubscribeAck(iMqttToken.getMessageId(), iMqttToken.getClient().getClientId(), Arrays.toString(iMqttToken.getTopics()), 0);
                }

            }

            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onSubscribeAck(iMqttToken.getMessageId(), iMqttToken.getClient().getClientId(), Arrays.toString(iMqttToken.getTopics()), 1);
                }

            }
        });
    }

    public void DisSubscribeforDevId(String devId) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_RAW.replaceAll("<Id>", devId);
            this.UnSubscribe(topic);
        }
    }

    public void DisSubscribeforuName() throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_USER_RAW.replaceAll("<Account>", this.userName);
            this.UnSubscribe(topic);
        }
    }

    public void DisSubscribeParsedforDevId(String devId) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_DEV_PARSED.replaceAll("<Id>", devId);
            this.UnSubscribe(topic);
        }
    }

    public void DisSubscribeParsedForUsername() throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_SUBSCRIBE_USER_PARSED.replaceAll("<Account>", this.userName);
            this.UnSubscribe(topic);
        }
    }

    private void UnSubscribe(String topic) throws MqttException {
        this.mqttAsyncClient.unsubscribe(topic).setActionCallback(new IMqttActionListener() {
            public void onSuccess(IMqttToken iMqttToken) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onDisSubscribeAck(iMqttToken.getMessageId(), iMqttToken.getClient().getClientId(), Arrays.toString(iMqttToken.getTopics()), 0);
                }

            }

            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onDisSubscribeAck(iMqttToken.getMessageId(), iMqttToken.getClient().getClientId(), Arrays.toString(iMqttToken.getTopics()), 1);
                }

            }
        });
    }

    public void publishForDevId(String devId, byte[] data) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_PUBLISH_DEV_RAW.replaceAll("<Id>", devId);
            this.PublishData(topic, data);
        }
    }

    public void publishForDevId2(String devId, byte[] data) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_PUBLISH_DEV_RAW2.replaceAll("<Id>", devId);
            this.PublishData(topic, data);
        }
    }
    public void publishForDevTopic(String topic, byte[] data) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            this.PublishData(topic, data);
        }
    }
    public void publishForuName(byte[] data) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_PUBLISH_USER_RAW.replaceAll("<Account>", this.userName);
            this.PublishData(topic, data);
        }
    }

    public void publishParsedSetDataPoint(String devId, String slaveIndex, String pointId, String value) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_PUBLISH_DEV_PARSED.replaceAll("<Id>", devId);
            String data = MqttPropertise.JSON_SETDATAPOINT.replaceAll("%SLAVEINDEX%", slaveIndex).replaceAll("%POINTID%", pointId).replaceAll("%POINTVALUE%", value);
            this.PublishData(topic, data.getBytes());
        }
    }

    public void publishParsedQueryDataPoint(String devId, String slaveIndex, String pointId) throws MqttException {
        if(this.mqttAsyncClient == null && !this.mqttAsyncClient.isConnected()) {
            throw new MqttException(32104);
        } else {
            String topic = MqttPropertise.TOPIC_PUBLISH_DEV_PARSED.replaceAll("<Id>", devId);
            String data = MqttPropertise.JSON_QUERYDATAPOINT.replaceAll("%SLAVEINDEX%", slaveIndex).replaceAll("%POINTID%", pointId);
            this.PublishData(topic, data.getBytes());
        }
    }

    private void PublishData(String topic, byte[] data) throws MqttException {
        MqttMessage mqttMessage = new MqttMessage();
        /**
         * 最多一次（0）
         最少一次（1）
         只一次（2）
         *后面，分别评估效果
         */
        mqttMessage.setQos(1);
        mqttMessage.setRetained(true);
        mqttMessage.setPayload(data);
        IMqttDeliveryToken publish = this.mqttAsyncClient.publish(topic, mqttMessage);
        if(this.usrCloudMqttCallback != null) {
            this.usrCloudMqttCallback.onPublishDataResult(publish.getMessageId(), Arrays.toString(publish.getTopics()));
        }

        publish.setActionCallback(new IMqttActionListener() {
            public void onSuccess(IMqttToken iMqttToken) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onPublishDataAck(iMqttToken.getMessageId(), Arrays.toString(iMqttToken.getTopics()), true);
                }

            }

            public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                if(YsCloudMqttClientImpl.this.usrCloudMqttCallback != null) {
                    YsCloudMqttClientImpl.this.usrCloudMqttCallback.onPublishDataAck(iMqttToken.getMessageId(), Arrays.toString(iMqttToken.getTopics()), false);
                }

            }
        });
    }

    public void connectionLost(Throwable cause) {
        if(this.usrCloudMqttCallback != null) {
            this.usrCloudMqttCallback.onConnectAck(3, cause.toString());
        }

    }

    public void deliveryComplete(IMqttDeliveryToken token) {
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        if(this.usrCloudMqttCallback != null) {
            if(topic.contains("JsonTx")) {
                this.usrCloudMqttCallback.onReceiveParsedEvent(message.getId(), topic, message.toString());
            } else {
                this.usrCloudMqttCallback.onReceiveEvent(message.getId(), topic, message.getPayload());
            }
        }

    }

    public void connectComplete(boolean b, String s) {
        if(this.usrCloudMqttCallback != null) {
            this.usrCloudMqttCallback.onConnectAck(2, "与服务器完成连接");
        }

    }
}
