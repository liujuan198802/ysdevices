package cn.ys.ysdatatransfer.business;

import android.content.Intent;

import cn.Ysserver.entity.MqttPropertise;
import cn.ys.ysdatatransfer.base.YsApplication;
import cn.ys.ysdatatransfer.entity.Device_cmd;
import cn.ys.ysdatatransfer.entity.Device_info;

/**
 * Created by Administrator on 2018-03-23.
 */

public class YsDeal_Cmd {
    public  static Device_info dealwithcmd(Device_cmd device_cmd)
    {
                String deviceid =YsApplication.getCLIENTID();
            if(device_cmd.getCmd_name()== null)
                return null;
            if(device_cmd.getClient_id()==null)
                return null;
            if(!device_cmd.getClient_id().equals(deviceid))
                return null;
            //重启设备
            if(device_cmd.getCmd_name().equals("reboot"))
            {
                Intent intent2 = new Intent(Intent.ACTION_REBOOT);
                intent2.putExtra("nowait", 1);
                intent2.putExtra("interval", 1);
                intent2.putExtra("window", 0);
                YsApplication.getInstance().sendBroadcast(intent2);
            };
        if(device_cmd.getCmd_name().equals("shutdown"))
        {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            YsApplication.getInstance().startActivity(intent);
            return  null;
        };
            //重启APP
            if(device_cmd.getCmd_name().equals("restart"))
            {
               // main_btn_disconnent.performClick();
                return null;
            };
            //上报设备信息
            if(device_cmd.getCmd_name().equals("get_sys_info"))
            {
            }
            if(device_cmd.getCmd_name().equals("baudrate_serail1"))
            {
                MqttPropertise.setproperty("baudrate_serail1",device_cmd.getCmd_state());
                Device_info device_info = new Device_info();
                device_info.setClient_id(deviceid);
                device_info.setInfo_name("baudrate_serail1");
                device_info.setInfo_state(MqttPropertise.getproperty("baudrate_serail1"));
                return device_info;
            }
            if(device_cmd.getCmd_name().equals("baudrate_serial2"))
            {
                MqttPropertise.setproperty("baudrate_serial2",device_cmd.getCmd_state());
                Device_info device_info = new Device_info();
                device_info.setClient_id(deviceid);
                device_info.setInfo_name("baudrate_serial2");
                device_info.setInfo_state(MqttPropertise.getproperty("baudrate_serial2"));
                return device_info;
            }
            if(device_cmd.getCmd_name().equals("enable_serail1"))
            {
                MqttPropertise.setproperty("enable_serail1",device_cmd.getCmd_state());
                Device_info device_info = new Device_info();
                device_info.setClient_id(deviceid);
                device_info.setInfo_name("enable_serail1");
                device_info.setInfo_state(MqttPropertise.getproperty("enable_serail1"));
                return device_info;
            }
            if(device_cmd.getCmd_name().equals("enable_serail2"))
            {
                MqttPropertise.setproperty("enable_serail2",device_cmd.getCmd_state());
                Device_info device_info = new Device_info();
                device_info.setClient_id(deviceid);
                device_info.setInfo_name("enable_serail2");
                device_info.setInfo_state(MqttPropertise.getproperty("enable_serail2"));
                  return device_info;
            }
           return  null;
    }
}
