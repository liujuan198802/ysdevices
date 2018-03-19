//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.Ysserver;

import cn.Ysserver.Interface.YsCloudMqttCallback;

public class YsCloudMqttCallbackAdapter implements YsCloudMqttCallback {
    public YsCloudMqttCallbackAdapter() {
    }

    public void onConnectAck(int returnCode, String description) {
    }

    public void onSubscribeAck(int messageId, String clientId, String topics, int returnCode) {
    }

    public void onDisSubscribeAck(int messageId, String clientId, String topics, int returnCode) {
    }

    public void onReceiveEvent(int messageId, String topic, byte[] data) {
    }

    public void onReceiveParsedEvent(int messageId, String topic, String jsonData) {
    }

    public void onPublishDataAck(int messageId, String topic, boolean isSuccess) {
    }

    public void onPublishDataResult(int messageId, String topic) {
    }
}
