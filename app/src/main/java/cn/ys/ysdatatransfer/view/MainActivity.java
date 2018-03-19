package cn.ys.ysdatatransfer.view;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Process;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.MqttException;

import cn.ys.ysdatatransfer.R;
import cn.ys.ysdatatransfer.base.YsBaseActivity;
import cn.ys.ysdatatransfer.business.YsCloudClientService;


public class MainActivity extends YsBaseActivity implements View.OnClickListener {
    private Button main_btn_stop;
    private Button main_btn_start;
    private Button main_btn_disconnent;
    private Button main_btn_clear;
    private String deviceid;
    private YsCloudClientService myService;
    private TextView local_port;
    private TextView local_ip;
    private TextView txt_tcp_data_count;
    private TextView txt_mqtt_data_count;
    private TextView txt_device_id;
    private TextView txt_client_num;
    private ImageView img_mqtt_state;
    private Handler timer_handler = new Handler();
    private  boolean mqtt_state = false;
    private OnSubscribeReceiver onSubscribeReceiver;
    Runnable runnable = new Runnable(){

        public void run(){
            // TODO Auto-generated method stub
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作
            if(myService!=null)
            {
                String mqtt_data=Long.toString(myService.get_mqtt_data_count());
                String tcp_data = Long.toString(myService.get_com_data_count());
                txt_mqtt_data_count.setText(mqtt_data);
                txt_tcp_data_count.setText(tcp_data);
                img_mqtt_state.setBackgroundColor(Color.RED);
                txt_client_num.setText(String.valueOf(myService.get_client_count()));
                if(myService.get_mqtt_state())
                {
                    mqtt_state =!mqtt_state;
                    if(mqtt_state)
                        img_mqtt_state.setBackgroundColor(Color.RED);
                    else
                        img_mqtt_state.setBackgroundColor(Color.GREEN);
                }
                myService.publishForDevId(deviceid,"test".getBytes());
            }

            timer_handler.postDelayed(this, 400);
        }
    };
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((YsCloudClientService.MyBinder) service).getService();
         //   myService.set_deviceId(deviceid);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
          //  myService.stop_tcp_listen();
            myService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        //从Intent当中根据key取得value
        if (intent != null) {
             deviceid = intent.getStringExtra("deviceid");
        }
        setContentView(R.layout.activity_main);
        initView();

    }

    @Override
    protected void onStart() {
        super.onStart();
        setListener();
        final Intent intent = new Intent(this, YsCloudClientService.class);
        bindService(intent, serviceConnection, Service.BIND_AUTO_CREATE);
        timer_handler.postDelayed(runnable, 400);
        onSubscribeReceiver = new OnSubscribeReceiver();
        //新添代码，在代码中注册广播接收程序
        IntentFilter filter = new IntentFilter();
        filter.addAction("onSubscribeAck");
        filter.addAction("onDisSubscribeAck");
        filter.addAction("onReceiveEvent");
        registerReceiver(onSubscribeReceiver, filter);
    }

    @Override
    public void initView() {
        super.initView();
        main_btn_stop = (Button) findViewById(R.id.main_btn_publish);
        main_btn_start = (Button) findViewById(R.id.main_btn_subscribe);
        main_btn_disconnent = (Button) findViewById(R.id.main_btn_disconnent);
        main_btn_clear = (Button) findViewById(R.id.btn_cler_count);
        local_port = (TextView) findViewById(R.id.local_port);
        local_ip = (TextView) findViewById(R.id.local_ip);
        txt_mqtt_data_count = (TextView) findViewById(R.id.txt_mqtt_data_count);
        txt_tcp_data_count = (TextView) findViewById(R.id.txt_tcp_data_count);
        txt_client_num = (TextView) findViewById(R.id.txt_client_num);
        img_mqtt_state = (ImageView) findViewById(R.id.img_tcp_state);
        txt_device_id =(TextView) findViewById(R.id.text_device_id);
        local_ip.setText(getIp(this));
        txt_device_id.setText(deviceid);
    }


    @Override
    public void setListener() {
        super.setListener();
        main_btn_stop.setOnClickListener(this);
        main_btn_start.setOnClickListener(this);
        main_btn_disconnent.setOnClickListener(this);
        main_btn_clear.setOnClickListener(this);

        if(myService!=null) {
//            if(myService.get_tcp_state())
//                set_click_state(false);
        }
        else
            set_click_state(true);
    }
    private void set_click_state(boolean state)
    {
        main_btn_start.setClickable(state);
        main_btn_stop.setClickable(!state);
        if(state)
        {
            main_btn_stop.setBackgroundResource(R.drawable.cancleshape);
            main_btn_start.setBackgroundResource(R.drawable.button_shape);
            local_port.setEnabled(true);
        }
        else
        {
            local_port.setEnabled(false);
            main_btn_start.setBackgroundResource(R.drawable.cancleshape);
            main_btn_stop.setBackgroundResource(R.drawable.button_shape);
        }
    }
    public  String getIp(Context contxext) {
        WifiManager wm = (WifiManager) contxext.getSystemService(Context.WIFI_SERVICE);
        // 检查Wifi状态
        if (!wm.isWifiEnabled())
            wm.setWifiEnabled(true);
        WifiInfo wi = wm.getConnectionInfo();
        int ipAdd = wi.getIpAddress();
        String ip = intToIp(ipAdd);
        return ip;
    }

    private String intToIp(int i) {
        return (i & 0xFF ) + "." +
                ((i >> 8 ) & 0xFF) + "." +
                ((i >> 16 ) & 0xFF) + "." +
                ( i >> 24 & 0xFF) ;
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_btn_subscribe:
                myService.doSubscribeForDevId(deviceid);
//                try {
//                 //   myService.start_tcp_server(local_port.getText().toString());
//                } catch
//            {
//                    new AlertDialog.Builder(this)
//                            .setTitle("错误")
//                            .setMessage("请检查本地端口是否被占用！") .show();
//                    set_click_state(true);
//                    Toast.makeText(this, "宇时4G数传服务启动失败！", Toast.LENGTH_SHORT).show();
//                    break;
//                }
                Toast.makeText(this, "宇时4G数传服务启动成功！", Toast.LENGTH_SHORT).show();
                set_click_state(false);
                break;
            case R.id.main_btn_publish:
                //关闭TCP服务
              //  myService.stop_tcp_listen();
                set_click_state(true);
                Toast.makeText(this, "宇时4G数传服务已停止！", Toast.LENGTH_SHORT).show();
                break;
            case R.id.main_btn_disconnent:
                try {
                    if (myService.DisConnectUnCheck()) {
                        Intent intent = new Intent(this, YsCloudClientService.class);
                        stopService(intent);
                        timer_handler.removeCallbacks(runnable);
                        this.unbindService(serviceConnection);
                        Process.killProcess(Process.myPid());
                        finish();
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                break;
            case  R.id.btn_cler_count:
                if(myService!=null)
                    myService.set_data_count_zero();
                break;
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
                if(returnCode!=0)
                {
                  //  myService.stop_tcp_listen();
                    set_click_state(true);
                    Toast.makeText(MainActivity.this, "宇时4G数传连接设备失败！", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (myService.doDisConnect()) {
            Intent intent = new Intent(this, YsCloudClientService.class);
            stopService(intent);
            timer_handler.removeCallbacks(runnable);
            this.unbindService(serviceConnection);
            Process.killProcess(Process.myPid());
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(onSubscribeReceiver);
        timer_handler.removeCallbacks(runnable);
        this.unbindService(serviceConnection);
    }
}
