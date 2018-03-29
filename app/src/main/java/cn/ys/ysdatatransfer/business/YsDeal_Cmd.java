package cn.ys.ysdatatransfer.business;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Process;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

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
        Device_info device_info = new Device_info();
        device_info.setClient_id(YsApplication.getCLIENTID());
        device_info.setInfo_name("text");
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
                return  null;
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
                    device_info.setInfo_state("重启应用成功");
                }
                catch (RuntimeException e)
                {
                    device_info.setInfo_state("重启应用失败");
                    Log.d("宇时4G","重启应用失败");
                }
                // main_btn_disconnent.performClick();
              return  device_info;
            }
        if(device_cmd.getCmd_name().equals("set_pwm"))
        {
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(device_cmd.getCmd_state());
                String   pwm1 =jsonObject.getString("pwm1");
                String   pwm2 =jsonObject.getString("pwm1");
                String   pwm3 =jsonObject.getString("pwm1");
                PWMUtils.set_pwm(Integer.valueOf(pwm1),Integer.valueOf(pwm1),Integer.valueOf(pwm1));
                device_info.setInfo_state("set pwm success!");
                return  device_info;
            } catch (JSONException e) {
                e.printStackTrace();
            }
            catch (NumberFormatException e) {
                e.printStackTrace();
            }
            device_info.setInfo_state("set pwm failed!");
            return  device_info;
        }
        if(device_cmd.getCmd_name().equals("get_pwm"))
        {
            try{
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("pwm1",PWMUtils.getPwm1());
                jsonObject1.put("pwm2",PWMUtils.getPwm2());
                jsonObject1.put("pwm3",PWMUtils.getPwm3());
                device_info.setInfo_name("get_pwm_ack");
                device_info.setInfo_state(jsonObject1.toString());
                return device_info;
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            device_info.setInfo_state("get_pwm failed!");
            return device_info;
        }
        if(device_cmd.getCmd_name().equals("set_gpio"))
        {
                String   pwm3 = device_cmd.getCmd_state();
                if(!pwm3.equals("0"))
                {
                    PWMUtils.set_gpio_out(true);
                    device_info.setInfo_state("set gpio hign!");
                }
                else
                {
                    PWMUtils.set_gpio_out(false);
                    device_info.setInfo_state("set pwm low!");
                }
            return  device_info;
        }
        if(device_cmd.getCmd_name().equals("get_gpio"))
        {
            String   pwm3 = device_cmd.getCmd_state();
           device_info.setInfo_name("get_gpio_ack");
           if(PWMUtils.getGpio_state())
            device_info.setInfo_state("hign");
           else
               device_info.setInfo_state("low");
            return  device_info;
        }
            if(device_cmd.getCmd_name().equals("set_property"))
            {
                try{
                    JSONObject jsonObject = new JSONObject(device_cmd.getCmd_state());
                  String   property_name =jsonObject.getString("property_name");
                    String   property_vale =jsonObject.getString("property_vale");
                    MqttPropertise.setproperty(property_name,property_vale);
                    JSONObject jsonObject1 = new JSONObject();
                    jsonObject1.put("property_name",property_name);
                    jsonObject1.put("property_vale",  MqttPropertise.getproperty(property_name));
                    jsonObject1.put("property_text", "重启设备，参数生效!");
                    device_info.setInfo_name("set_property_ack");
                    device_info.setInfo_state(jsonObject1.toString());
                    return device_info;
                }
                 catch (JSONException e) {
                    e.printStackTrace();
                }
                device_info.setInfo_state("set_property failed!");
                return device_info;
            }
        if(device_cmd.getCmd_name().equals("get_property"))
        {
            try{
               String value = MqttPropertise.getproperty(device_cmd.getCmd_state());
                JSONObject jsonObject1 = new JSONObject();
                jsonObject1.put("property_name",device_cmd.getCmd_state());
                jsonObject1.put("property_vale", value);
                device_info.setInfo_name("get_property_ack");
                device_info.setInfo_state(jsonObject1.toString());
                return device_info;
            }
            catch (JSONException e) {
                e.printStackTrace();
            }
            device_info.setInfo_state("get_property failed!");
            return device_info;
        }
            //上报设备信息
            if(device_cmd.getCmd_name().equals("get_sys_info"))
            {
            }
        if(device_cmd.getCmd_name().equals("get_pos"))
        {
            device_info.setClient_id(deviceid);
            device_info.setInfo_name("device_pos");
            device_info.setInfo_state(YsApplication.get_pos());
            return device_info;
        }
           return  device_info;
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
