//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package cn.Ysserver.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Properties;

import cn.ys.ysdatatransfer.base.YsApplication;

public class BeasUtils {
    public BeasUtils() {
    }

    public static Properties getPropertie(Class<?> obj, String propName) {
        Properties prop = new Properties();

        try {
            InputStream inStream = YsApplication.getInstance().getAssets().open(propName);
            prop.load(new InputStreamReader(inStream, "UTF-8"));
            return prop;
        } catch (IOException var4) {
            var4.printStackTrace();
            return prop;
        }
    }

    public static final String getMD5(String pwd) {
        char[] md5String = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            byte[] btInput = pwd.getBytes();
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            mdInst.update(btInput);
            byte[] md = mdInst.digest();
            int j = md.length;
            char[] str = new char[j * 2];
            int k = 0;

            for(int i = 0; i < j; ++i) {
                byte byte0 = md[i];
                str[k++] = md5String[byte0 >>> 4 & 15];
                str[k++] = md5String[byte0 & 15];
            }

            return new String(str);
        } catch (Exception var10) {
            return null;
        }
    }
}
