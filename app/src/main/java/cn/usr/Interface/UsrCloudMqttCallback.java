//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.usr.Interface;

public interface UsrCloudMqttCallback {
    void onConnectAck(int var1, String var2);

    void onSubscribeAck(int var1, String var2, String var3, int var4);

    void onDisSubscribeAck(int var1, String var2, String var3, int var4);

    void onReceiveEvent(int var1, String var2, byte[] var3);

    void onReceiveParsedEvent(int var1, String var2, String var3);

    void onPublishDataAck(int var1, String var2, boolean var3);

    void onPublishDataResult(int var1, String var2);
}
