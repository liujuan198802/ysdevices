package cn.ys.ysdatatransfer.business;

import android.util.Log;

import com.temolin.hardware.GPIO_Pin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import cn.Ysserver.entity.MqttPropertise;

/**
 * Created by Administrator on 2018-03-29.
 */

public class PWMUtils {
   static GPIO_Pin pin_pwm1 = new GPIO_Pin(100);
    static GPIO_Pin pin_pwm2 = new GPIO_Pin(101);
    static  GPIO_Pin pin_pwm3 = new GPIO_Pin(102);
    static GPIO_Pin pin_gpio = new GPIO_Pin(105);
    static String pwm_no1= "4 ";
    static String pwm_no2= "0 ";
    static String pwm_no3= "3 ";

    public static int getPwm1() {
        return pwm1;
    }

    public static int getPwm2() {
        return pwm2;
    }

    public static int getPwm3() {
        return pwm3;
    }

    public static boolean getGpio_state() {
        return gpio_state;
    }

    static String pwm_cmd = " 0 6 20000";
    private static int pwm1,pwm2,pwm3;
    private static  boolean gpio_state;
    private static  final String pwm_dvice ="/sys/bus/platform/drivers/mt-pwm/pwm_debug";
     public static void init_pwm_mode()
    {
        pin_pwm1.setMuxMode("5");
        pin_pwm2.setMuxMode("5");
        pin_pwm3.setMuxMode("5");
        pin_gpio.setToGpioMode();
        if(!pin_gpio.getPinMode().equals("out")){
            pin_gpio.setModeOUTPUT();
        }
        try {
            pwm1 = Integer.valueOf(MqttPropertise.getproperty("default_pwm1"));
            pwm2 = Integer.valueOf(MqttPropertise.getproperty("default_pwm1"));
            pwm3 = Integer.valueOf(MqttPropertise.getproperty("default_pwm1"));
            set_pwm(pwm1,pwm2,pwm3);
        }
        catch (NumberFormatException e)
        {
        }
        String gpio_en =MqttPropertise.getproperty("default_gpio1");
        if(gpio_en!=null && !gpio_en.equals("0"))
        {
            gpio_state = true;
            set_gpio_out(true);
        }
        else
        {
            gpio_state = false;
            set_gpio_out(false);
        }
        }
    public static void set_gpio_out(boolean high)
    {
        gpio_state = high;
       if(high)
           pin_gpio.setHIGH();
       else
           pin_gpio.setLOW();
    }
    public static void set_pwm(int pwm10,int pwm20,int pwm30)
    {
        pwm1 = pwm10;
        pwm2 = pwm20;
        pwm3 = pwm30;
        String  cmd1 = pwm_no1 +pwm1+pwm_cmd;
        String  cmd2 = pwm_no2 +pwm2+pwm_cmd;
        String  cmd3 = pwm_no3 +pwm3+pwm_cmd;
        writeToFile(pwm_dvice,cmd1);
        writeToFile(pwm_dvice,cmd2);
        writeToFile(pwm_dvice,cmd3);
    }
    private static void writeToFile(String URI, String data) {
        try {
            Log.d("GPIO", "Start Writing \"" + data + "\" to " + URI);
            File newFile = new File(URI);
            newFile.createNewFile();
            FileWriter writer = new FileWriter(URI);
            writer.write(data);
            writer.flush();
            writer.close();
            Log.d("GPIO", "write done !");
        } catch (IOException var5) {
            var5.printStackTrace();
        }

    }
}
