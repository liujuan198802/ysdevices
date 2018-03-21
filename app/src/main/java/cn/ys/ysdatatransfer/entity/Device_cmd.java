package cn.ys.ysdatatransfer.entity;

/**
 * Created by Administrator on 2017/12/1 0001.
 */

public class Device_cmd {
    public String getClient_id() {
        return client_id;
    }

    public void setClient_id(String client_id) {
        this.client_id = client_id;
    }

    private  String client_id;

    public String getCmd_state() {
        return cmd_state;
    }

    public void setCmd_state(String cmd_state) {
        this.cmd_state = cmd_state;
    }

    public String getCmd_name() {
        return cmd_name;
    }

    public void setCmd_name(String cmd_name) {
        this.cmd_name = cmd_name;
    }

    private String cmd_name;
    private String cmd_state;
    @Override
    public String toString() {
        return  "{\"deviceCmd\":{\"client_id\":\""+client_id+"\",\"cmd_name\":\""+cmd_name+"\",\"cmd_state\":\""+cmd_state+"\"}}";
    }
}
