package cn.ys.ysdatatransfer.business;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

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
            shutdown_device();
            return  null;
        }
            //重启APP
            if(device_cmd.getCmd_name().equals("restart"))
            {
                try {
                    ActivityManager am = (ActivityManager) YsApplication.getInstance().getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
                    am.killBackgroundProcesses(YsApplication.getInstance().getApplicationContext().getPackageName());
                    am.killBackgroundProcesses("cn.ys.ysdatatransfer");
                    Intent intent = new Intent(YsApplication.getInstance(), YsCloudClientService.class);
                    YsApplication.getInstance().getApplicationContext().stopService(intent);
                    Process.killProcess(Process.myPid());
                    System.exit(0);//正常退出App
                }
                catch (RuntimeException e)
                {
                    Log.d("宇时4G","重启应用失败");
                }
                // main_btn_disconnent.performClick();
                return null;
            }
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
        if(device_cmd.getCmd_name().equals("get_pos"))
        {
            Device_info device_info = new Device_info();
            device_info.setClient_id(deviceid);
            device_info.setInfo_name("device_pos");
            device_info.setInfo_state(YsApplication.get_pos());
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
    public static void shutdown_device()
    {
        try {
            Intent intent = new Intent("android.intent.action.ACTION_REQUEST_SHUTDOWN");
            intent.putExtra("android.intent.extra.KEY_CONFIRM", false);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            YsApplication.getInstance().startActivity(intent);
        }
        catch (RuntimeException e)
        {
            Log.d("YsDeal_cm","shut down failed");
            e.printStackTrace();
        }
    }
}
