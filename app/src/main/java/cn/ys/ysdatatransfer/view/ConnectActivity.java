package cn.ys.ysdatatransfer.view;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.PrivateKey;

import cn.ys.ysdatatransfer.R;
import cn.ys.ysdatatransfer.base.PermissionUtil;
import cn.ys.ysdatatransfer.base.YsApplication;
import cn.ys.ysdatatransfer.base.YsBaseActivity;
import cn.ys.ysdatatransfer.business.SignalStrengthsHandler;
import cn.ys.ysdatatransfer.business.YsCloudClientService;
import cn.ys.ysdatatransfer.business.YsDeal_Cmd;
import cn.ys.ysdatatransfer.entity.Device_cmd;
import cn.ys.ysdatatransfer.entity.Device_info;

/**
 * Created by shizhiyuan on 2017/7/21.
 */

public class ConnectActivity extends YsBaseActivity {

    Device_info device_info = new Device_info();
    private Button con_btn_connect;
    private Receiver receiver;
    private OnSubscribeReceiver onSubscribeReceiver;
    private ProgressBar progressBar;
    private SharedPreferences mSharedPreferences;
    private SharedPreferences.Editor editor;
    private  Button btn_get_path;
    private TextView  device_id;
    private static String string_device_pwd;
    private YsCloudClientService myService;
    private final  static String default_key_path ="/storage/emulated/0/Download/keyconfig";
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((YsCloudClientService.MyBinder) service).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            myService = null;
        }
    };
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
    Runnable runnable_send_sys_info = new Runnable(){
        public void run(){
            Device_info device_info =new Device_info();
            device_info.setClient_id(YsApplication.getCLIENTID());
            device_info.setInfo_name("device_pos");
            device_info.setInfo_state(YsApplication.get_pos());
            showToast(device_info.toString());
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
        device_info.setInfo_name("text");
        device_info.setClient_id(YsApplication.getCLIENTID());
        receiver = new Receiver();
        //新添代码，在代码中注册广播接收程序
        IntentFilter filter = new IntentFilter();
        filter.addAction("onConnectAck");
        registerReceiver(receiver, filter);
        onSubscribeReceiver = new OnSubscribeReceiver();
        //新添代码，在代码中注册广播接收程序
        IntentFilter filter_n = new IntentFilter();
        filter_n.addAction("onReceiveCmdEvent");
        filter_n.addAction("onDisSubscribeAck");
        filter_n.addAction("onReceiveCmdEvent");
        filter_n.addAction("onGetNewPoosition");
        registerReceiver(onSubscribeReceiver, filter_n);
        //连续启动Service
        //连续启动Service
        Intent intent = new Intent(this, YsCloudClientService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initView();
    }
    @Override
    public void initView() {
        super.initView();
        mSharedPreferences = getSharedPreferences("keypath", MODE_PRIVATE);
        editor = mSharedPreferences.edit();
        con_btn_connect = (Button) findViewById(R.id.con_btn_connect);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btn_get_path = (Button)   findViewById(R.id.btn_choosekey);
        device_id  = (TextView) findViewById(R.id.textView);
        btn_get_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtil.isLacksOfPermission(PermissionUtil.PERMISSION[0])) {
                    SignalStrengthsHandler signalStrengthsHandler = SignalStrengthsHandler.getInstance();
                   // get_keyconfig_and_jump(default_key_path);
                }
                SignalStrengthsHandler signalStrengthsHandler = SignalStrengthsHandler.getInstance();
            }
        });
        con_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                con_btn_connect.setText("正在启动宇时4G数传...");
                showToast("正在连接服务器！");
                con_btn_connect.setClickable(false);
                con_btn_connect.setBackgroundResource(R.drawable.cancleshape);
                String uname = YsApplication.getUSERNAME();
                Bundle bundle = new Bundle();
                bundle.putString("uname", uname);
                bundle.putString("upw", string_device_pwd);
                bundle.putString("clientid", YsApplication.getCLIENTID());
                startServiceWithParm(YsCloudClientService.class, bundle);
            }
        });
       // setWifiApEnabled(true,(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE),"YS4Gdevice","YS4Gdevice");
        con_btn_connect.setText("请先选择keyconfig文件！");
        progressBar.setVisibility(View.INVISIBLE);
        //   string_device_id = mSharedPreferences.getString("deviceID","-1");
        con_btn_connect.setClickable(false);
        con_btn_connect.setBackgroundResource(R.drawable.cancleshape);
        myHandler.postDelayed(runnable_read,1000);
    }


    private static final int NETUPDATE=10;
   private boolean get_keyconfig_and_jump(String path)
   {
       showToast("正在读取keyconfig文件....");
       try {
           Thread.sleep(500);
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       String keyconfig= readFile(path);
       if(keyconfig == null)
       {
           showToast("打开keyconfig文件失败,请检查设备keyconfig文件是否完整！");
           return false;
       }
       try
       {
           string_device_pwd = new String(RsaHelper.decryptData(
                   Base64Helper.decode(keyconfig), privateKey), "UTF-8");
           if(string_device_pwd.length()!=20)
           {
               showToast("keyconfig文件异常！");
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
           showToast("读取keyconfig异常！");
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

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        showToast("宇时4G即将停止服务....");
        this.unregisterReceiver(receiver);
        this.unregisterReceiver(onSubscribeReceiver);
        this.unbindService(serviceConnection);
    }

//    private static final int REQUEST_EXTERNAL_STORAGE = 1;
//    private static String[] PERMISSIONS_STORAGE = {
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE
//    };
    public class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            progressBar.setVisibility(View.INVISIBLE);
            con_btn_connect.setText("正在启动宇时4G数传...");
            con_btn_connect.setClickable(true);
            con_btn_connect.setBackgroundResource(R.drawable.button_shape);
            if (intent.getIntExtra("onConnectAckreturnCode", 1) == 0) {
                try {
                    myHandler.removeCallbacks(runnable_connect);
                }
                catch (RuntimeException e)
                {
                }
                showToast("连接服务器成功！");
//               Intent intent1= new Intent(ConnectActivity.this, MainActivity.class);
//                intent1.putExtra("deviceid",string_device_pwd);
//                startActivity(intent1);
            } else if (intent.getIntExtra("onConnectAckreturnCode", 1) == 1) {
                showToast("宇时4G数传启动失败,请检查网络是否畅通!");
                //2s后自动重连
                try {
                    myHandler.removeCallbacks(runnable_connect);
                }
                catch (RuntimeException e)
                {
                }
                myHandler.postDelayed(runnable_connect,4000);
            }

        }
    }
    public class OnSubscribeReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("onSubscribeAck")) {
                Bundle bundle = intent.getExtras();
                int messageId = bundle.getInt("messageId");
                String devId = bundle.getString("CliendID");
                int returnCode = bundle.getInt("returnCode");
                if (returnCode != 0) {
                    showToast( "宇时4G数传连接设备失败！");
                }
            return;
            }
            else if (action.equals("onGetNewPoosition")) {
                myHandler.postDelayed(runnable_send_sys_info,200);
                return;
            }
          else if (action.equals("onReceiveCmdEvent")) {
                Bundle bundle = intent.getExtras();
                String msg = "";
                int messageId = bundle.getInt("messageId");
                String topic = bundle.getString("topic");
                String jsondata = bundle.getString("cmddata");
                try {
                    JSONObject jsonObject = JSONObject.parseObject(jsondata);
                    JSONObject jsonObject1 = jsonObject.getJSONObject("deviceCmd");
                    Device_cmd device_cmd = new Device_cmd();
                    device_cmd.setClient_id(jsonObject1.getString("client_id"));
                    device_cmd.setCmd_name(jsonObject1.getString("cmd_name"));
                    device_cmd.setCmd_state(jsonObject1.getString("cmd_state"));
                    Log.d("宇时4G：","收到JSon："+device_cmd.toString());
                    Device_info device_info1= YsDeal_Cmd.dealwithcmd(device_cmd);
                    if(device_info1!=null)
                        myService.send_state_all(device_info1);
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }
    @Override
    public void showToast(String msg) {
        device_info.setInfo_state(msg);
        try {
            myService.send_state_all(device_info);
        }
      catch (RuntimeException e)
      {
          e.printStackTrace();
      }
       // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}
