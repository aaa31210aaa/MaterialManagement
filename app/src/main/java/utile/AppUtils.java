package utile;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

/**
 * 应用辅助类
 */
public class AppUtils {

    private AppUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 获取本地软件版本号
     */
    public static int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }


    /**
     * 获取应用程序名称
     */
    public static String getAppName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            int labelRes = packageInfo.applicationInfo.labelRes;
            return context.getResources().getString(labelRes);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * [获取应用程序版本名称信息]
     *
     * @param context
     * @return 当前应用的版本名称
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionName;

        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取系统SDK版本
     *
     * @return
     */
    public static int getSDKVersionNumber() {
        int sdkVersion;
        try {
            sdkVersion = Integer.valueOf(android.os.Build.VERSION.SDK_INT);
        } catch (NumberFormatException e) {
            sdkVersion = 0;
        }
        return sdkVersion;
    }


    /**
     * 禁止edittext 使用空格
     */
    public static void PwdNoSpace(final EditText editText) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                NoSpace(editText, s, start);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    /**
     * 禁止输入框输入空格
     */
    public static void NoSpace(EditText editText, CharSequence str, int start) {
        if (str.toString().contains(" ")) {
            String[] strs = str.toString().split(" ");
            String str1 = "";
            for (int i = 0; i < strs.length; i++) {
                str1 += strs[i];
            }
            editText.setText(str1);
            editText.setSelection(start);
        }
    }

}
