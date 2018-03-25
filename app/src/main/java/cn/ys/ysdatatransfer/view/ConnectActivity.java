package cn.ys.ysdatatransfer.view;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.security.PrivateKey;

import cn.ys.ysdatatransfer.R;
import cn.ys.ysdatatransfer.base.YsApplication;
import cn.ys.ysdatatransfer.base.YsBaseActivity;
import cn.ys.ysdatatransfer.business.YsCloudClientService;

/**
 * Created by shizhiyuan on 2017/7/21.
 */

public class ConnectActivity extends YsBaseActivity {


    private Button con_btn_connect;
    private Receiver receiver;
    private ProgressBar progressBar;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private  Button btn_get_path;
    private TextView  device_id;
    private static String string_device_pwd;
    private final  static String default_key_path ="/storage/emulated/0/Download/keyconfig";
    /**
     * 使用私钥进行解密
     */
    private String strprivatekey = "<RSAKeyValue><Modulus>uOqxIXLFeDL9n1u+yo9LNzRxoV6U04YYenAXIbJ/CAO6wbjBaf9m6Sw9mJXoJr3IA9w6Plw1wgLrBLP0ngW7cFSQVbT/p2zx5NhDLTZ6NeHV+lZNLSFL6niUvDCBIyrims8VG7+FLXqrjBa0YOny5MhCdP2K67lNCBUwlDr+nLs=</Modulus><Exponent>AQAB</Exponent><P>xyscDUZcb7beXz60mUSMtTOuzxSCmqqXlgP1bhGr5RCBSADIeZoy8+k2ILfzTDorlwd9pqSbFBf/W4rVGp6d4w==</P><Q>7a6Gx+flU3M2YJFQ+8qVD08j8vjcoSG3JaSfUB5Z3WE1ZPug36LSjIgZ4EsZQ9mjreVgYkxylkhAwiDfSHG9SQ==</Q><DP>BoM7XJfDaAfDx7uGLkjWjQpOmgjiqGoRoN8qRFohk9DxWUhlRcysA9vJYFKDiyePy1V8X1mclJCgUf79Luym3w==</DP><DQ>vEaFyZDuXd5j8rbp2aqtzQS5y1xLGPCmLZFsCYEhWnYIX8fbtYs7Ecs2BDA5AUBDohqS8Qrxsg3mDmEPvkkq0Q==</DQ><InverseQ>stTU8PLYXfq391Er67p5yfYdsEdnXQBaQESIWAnxUE2QmqRZ00rX3MnbhkvcMlFxfoOrfOWJWTEQQKiTtHQs5g==</InverseQ><D>AZpZP7VPjaU+VU3VaJjD3KN8+gGJFCQ+CtLC+pwYBskwtMHu5/vxtVmnlx0TgqrGhac4iBGUXIox+ZO8b1FgB63o+1ihdAefG7siXpsL5adO85hjuQs1XIpC63Fm2WMi8yYNnS8QB0v7G+CKdTtWDEkfpyUBB74Kb9bihEZ1AHE=</D></RSAKeyValue>";
    private  PrivateKey privateKey = RsaHelper.decodePrivateKeyFromXml(strprivatekey);

    Runnable runnable_read = new Runnable(){
        public void run(){
            // TODO Auto-generated method stub
           get_keyconfig_and_jump(default_key_path);
        }
    };
    Runnable runnable_connect = new Runnable(){
        public void run(){
            // TODO Auto-generated method stub
            con_btn_connect.setClickable(true);
            con_btn_connect.performClick();
        }
    };
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NETUPDATE:
                    string_device_pwd= ((String) msg.obj);
                    device_id.setText(string_device_pwd);
                    editor.putString("deviceID",string_device_pwd);
                    editor.commit();
                    con_btn_connect.setText("启动宇时4G数传");
                    con_btn_connect.setBackgroundResource(R.drawable.button_shape);
                    con_btn_connect.setClickable(true);
                    con_btn_connect.performClick();
                    break;
            }
        }
    };
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
        receiver = new Receiver();
        //新添代码，在代码中注册广播接收程序
        IntentFilter filter = new IntentFilter();
        filter.addAction("onConnectAck");
        registerReceiver(receiver, filter);
    }

    @Override
    public void initView() {
        super.initView();
        mSharedPreferences = getSharedPreferences("keypath", MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        con_btn_connect = (Button) findViewById(R.id.con_btn_connect);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_get_path = (Button)   findViewById(R.id.btn_choosekey);
        device_id  = (TextView) findViewById(R.id.textView);
        con_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                con_btn_connect.setText("正在启动宇时4G数传...");
                con_btn_connect.setClickable(false);
                String uname = YsApplication.USERNAME;
                Bundle bundle = new Bundle();
                bundle.putString("uname", uname);
                bundle.putString("upw", string_device_pwd);
                bundle.putString("clientid", YsApplication.getCLIENTID());
                startServiceWithParm(YsCloudClientService.class, bundle);
                myHandler.postDelayed(runnable_connect,3000);
            }
        });
       // setWifiApEnabled(true,(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE),"YS4Gdevice","YS4Gdevice");
        con_btn_connect.setText("请先选择keyconfig文件！");
        progressBar.setVisibility(View.INVISIBLE);
        //   string_device_id = mSharedPreferences.getString("deviceID","-1");
        myHandler.postDelayed(runnable_read,1000);
        setWifiApEnabled(true,(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE),"Y4GClient:"+YsApplication.getCLIENTID(),"cloudoftime");
    }


    private static final int NETUPDATE=10;
   private boolean get_keyconfig_and_jump(String path)
   {
       Toast.makeText(this,"正在读取文件："+path,Toast.LENGTH_SHORT).show();
       try {
           Thread.sleep(500);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       String keyconfig= readFile(path);
       if(keyconfig == null)
       {
           Toast.makeText(this,"打开keyconfig文件失败！",Toast.LENGTH_SHORT).show();
           return false;
       }
       try
       {
           string_device_pwd = new String(RsaHelper.decryptData(
                   Base64Helper.decode(keyconfig), privateKey), "UTF-8");
           if(string_device_pwd.length()!=20)
           {
               Toast.makeText(this,"deviceid错误！",Toast.LENGTH_SHORT).show();
               return false;
           }
           Message tempMsg = myHandler.obtainMessage();
           tempMsg.what = NETUPDATE;
           tempMsg.obj = string_device_pwd;
           myHandler.sendMessage(tempMsg);
       }
       catch (Exception e)
       {
           e.printStackTrace();
           Toast.makeText(this,"读取keyconfig异常！",Toast.LENGTH_SHORT).show();
           return false;
       }
            return  true;
   }
     //从文件从读取加密后的字符串
    private String readFile(String filePath){
        if(filePath == null) return null;
        String file_data =new String();
        File file = new File(filePath);
        if(file.isDirectory()){
            Log.d(TAG, filePath + " is directory");
            return null;
        }else{
            try {
                InputStream is = new FileInputStream(file);
                if(is != null){
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
                    String line;
                    while((line = br.readLine()) != null){
                        file_data += line;
                        Log.d(TAG, line);
                    }
                }
            } catch (FileNotFoundException e) {
                Log.d(TAG, filePath + " doesn't found!");
                return null;
            }catch (IOException e) {
                Log.d(TAG, filePath + " read exception, " + e.getMessage());
                return  null;
            }
        }
        return  file_data;
    }


    @Override
    protected void onStop() {
        super.onStop();
        this.unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    public void myPermission() {
        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    // wifi热点开关
    public boolean setWifiApEnabled(boolean enabled, WifiManager wifiManager, String ap_name, String ap_pwd) {
        if (enabled) { // disable WiFi in any case
            //wifi和热点不能同时打开，所以打开热点的时候需要关闭wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            //热点的配置类
            WifiConfiguration apConfig = new WifiConfiguration();
            //配置热点的名称(可以在名字后面加点随机数什么的)
            apConfig.SSID = ap_name;
            //配置热点的密码
            apConfig.preSharedKey=ap_pwd;
            //通过反射调用设置热点
            Method method = wifiManager.getClass().getMethod(
                    "setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            //返回热点打开状态
            return (Boolean) method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    public class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("onConnectAckreturnCode", 1) == 0) {
                try {
                    myHandler.removeCallbacks(runnable_connect);
                }
                catch (RuntimeException e)
                {
                }
               Intent intent1= new Intent(ConnectActivity.this, MainActivity.class);
                intent1.putExtra("deviceid",string_device_pwd);
                startActivity(intent1);
            } else if (intent.getIntExtra("onConnectAckreturnCode", 1) == 1) {
                Toast.makeText(ConnectActivity.this, "宇时4G数传启动失败\r\n请检查网络是否畅通!", Toast.LENGTH_SHORT).show();
                con_btn_connect.setClickable(true);
                con_btn_connect.setText("启动宇时4G数传");
                progressBar.setVisibility(View.INVISIBLE);
                con_btn_connect.setBackgroundResource(R.drawable.button_shape);
                //2s后自动重连
                try {
                    myHandler.removeCallbacks(runnable_connect);
                }
                catch (RuntimeException e)
                {
                }
                myHandler.postDelayed(runnable_connect,2000);
            }

        }
    }
}
