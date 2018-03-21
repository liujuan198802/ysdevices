package cn.ys.ysdatatransfer.entity;

/**
 * Created by Administrator on 2017/12/1 0001.
 */

public class Device_info {
    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    private  String client_id;


    public String getInfo_name() {
        return info_name;
    }

    public void setInfo_name(String info_name) {
        this.info_name = info_name;
    }

    private String info_name;

    public String getInfo_state() {
        return info_state;
    }

    public void setInfo_state(String info_state) {
        this.info_state = info_state;
    }

    private String info_state;

    @Override
    public String toString() {
        return  "{\"deviceInfo\":{\"client_id\":\""+client_id+"\",\"info_name\":\""+info_name+"\",\"info_state\":\""+info_state+"\"}}";
    }
}
