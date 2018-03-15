package cn.ys.ysdatatransfer.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.security.PrivateKey;

import cn.ys.ysdatatransfer.R;
import cn.ys.ysdatatransfer.base.YsBaseActivity;
import cn.ys.ysdatatransfer.business.YsCloudClientService;

import static cn.ys.ysdatatransfer.base.YsApplication.USERNAME;

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
    private static String string_device_id;
    /**
     * 使用私钥进行解密
     */
    private String strprivatekey = "<RSAKeyValue><Modulus>uOqxIXLFeDL9n1u+yo9LNzRxoV6U04YYenAXIbJ/CAO6wbjBaf9m6Sw9mJXoJr3IA9w6Plw1wgLrBLP0ngW7cFSQVbT/p2zx5NhDLTZ6NeHV+lZNLSFL6niUvDCBIyrims8VG7+FLXqrjBa0YOny5MhCdP2K67lNCBUwlDr+nLs=</Modulus><Exponent>AQAB</Exponent><P>xyscDUZcb7beXz60mUSMtTOuzxSCmqqXlgP1bhGr5RCBSADIeZoy8+k2ILfzTDorlwd9pqSbFBf/W4rVGp6d4w==</P><Q>7a6Gx+flU3M2YJFQ+8qVD08j8vjcoSG3JaSfUB5Z3WE1ZPug36LSjIgZ4EsZQ9mjreVgYkxylkhAwiDfSHG9SQ==</Q><DP>BoM7XJfDaAfDx7uGLkjWjQpOmgjiqGoRoN8qRFohk9DxWUhlRcysA9vJYFKDiyePy1V8X1mclJCgUf79Luym3w==</DP><DQ>vEaFyZDuXd5j8rbp2aqtzQS5y1xLGPCmLZFsCYEhWnYIX8fbtYs7Ecs2BDA5AUBDohqS8Qrxsg3mDmEPvkkq0Q==</DQ><InverseQ>stTU8PLYXfq391Er67p5yfYdsEdnXQBaQESIWAnxUE2QmqRZ00rX3MnbhkvcMlFxfoOrfOWJWTEQQKiTtHQs5g==</InverseQ><D>AZpZP7VPjaU+VU3VaJjD3KN8+gGJFCQ+CtLC+pwYBskwtMHu5/vxtVmnlx0TgqrGhac4iBGUXIox+ZO8b1FgB63o+1ihdAefG7siXpsL5adO85hjuQs1XIpC63Fm2WMi8yYNnS8QB0v7G+CKdTtWDEkfpyUBB74Kb9bihEZ1AHE=</D></RSAKeyValue>";
    private  PrivateKey privateKey = RsaHelper.decodePrivateKeyFromXml(strprivatekey);
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
        btn_get_path.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myPermission();
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");//无类型限制
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 1);
            }
        });
        con_btn_connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                con_btn_connect.setText("正在启动宇时4G数传...");
                con_btn_connect.setClickable(false);
                con_btn_connect.setBackgroundResource(R.drawable.cancleshape);
                String uname = "18780126369";
                String upw = "5234970";
                Bundle bundle = new Bundle();
                bundle.putString("uname", uname);
                bundle.putString("upw", upw);
                USERNAME=uname;
                startServiceWithParm(YsCloudClientService.class, bundle);
            }
        });
        con_btn_connect.setBackgroundResource(R.drawable.cancleshape);
        con_btn_connect.setClickable(false);
        con_btn_connect.setText("请先选择keyconfig文件！");
        progressBar.setVisibility(View.INVISIBLE);
       string_device_id = mSharedPreferences.getString("deviceID","-1");

       if(string_device_id!="-1")
       {
           device_id.setText(string_device_id);
           con_btn_connect.setClickable(true);
           con_btn_connect.setText("启动宇时4G数传");
           //  progressBar.setVisibility(View.VISIBLE);
           con_btn_connect.setBackgroundResource(R.drawable.button_shape);
       }
    }
    Handler myHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case NETUPDATE:
                    string_device_id= ((String) msg.obj);
                    device_id.setText(string_device_id);
                    editor.putString("deviceID",string_device_id);
                    editor.commit();
                    con_btn_connect.setClickable(true);
                    con_btn_connect.setText("启动宇时4G数传");
                    //  progressBar.setVisibility(View.VISIBLE);
                    con_btn_connect.setBackgroundResource(R.drawable.button_shape);
                    break;
           }
        }
    };
    String path;
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            if ("file".equalsIgnoreCase(uri.getScheme())){//使用第三方应用打开
                path = uri.getPath();
                get_keyconfig_and_jump(path);
                return;
            }
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT) {//4.4以后
                path = getPath(this, uri);
                get_keyconfig_and_jump(path);
            } else {//4.4以下下系统调用方法
                path = getRealPathFromURI(uri);
                get_keyconfig_and_jump(path);
            }
        }
    }
    private static final int NETUPDATE=10;
   private void get_keyconfig_and_jump(String path)
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
           return;
       }
       try
       {
           string_device_id = new String(RsaHelper.decryptData(
                   Base64Helper.decode(keyconfig), privateKey), "UTF-8");
           if(string_device_id.length()!=20)
           {
               Toast.makeText(this,"deviceid错误！",Toast.LENGTH_SHORT).show();
               return;
           }
           Message tempMsg = myHandler.obtainMessage();
           tempMsg.what = NETUPDATE;
           tempMsg.obj = string_device_id;
           myHandler.sendMessage(tempMsg);

           Log.e("device_id", string_device_id);
       }
       catch (Exception e)
       {
           e.printStackTrace();
           Toast.makeText(this,"读取keyconfig异常！",Toast.LENGTH_SHORT).show();
           return;
       }

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

    public String getRealPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(contentUri, proj, null, null, null);
        if(null!=cursor&&cursor.moveToFirst()){;
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
            cursor.close();
        }
        return res;
    }

    /**
     * 专为Android4.4设计的从Uri获取文件绝对路径，以前的方法已不好使
     */
    @SuppressLint("NewApi")
    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
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

    public class Receiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.getIntExtra("onConnectAckreturnCode", 1) == 0) {
               Intent intent1= new Intent(ConnectActivity.this, MainActivity.class);
                intent1.putExtra("deviceid",string_device_id);
                startActivity(intent1);
            } else if (intent.getIntExtra("onConnectAckreturnCode", 1) == 1) {
                Toast.makeText(ConnectActivity.this, "宇时4G数传启动失败\r\n请检查网络是否畅通!", Toast.LENGTH_SHORT).show();
                con_btn_connect.setClickable(true);
                con_btn_connect.setText("启动宇时4G数传");
                progressBar.setVisibility(View.INVISIBLE);
                con_btn_connect.setBackgroundResource(R.drawable.button_shape);
            }

        }
    }
}
