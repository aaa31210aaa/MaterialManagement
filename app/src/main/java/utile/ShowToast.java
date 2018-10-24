package utile;

import android.content.Context;
import android.widget.Toast;

public class ShowToast {
    public static boolean isShow = true;
     public static Toast mToast;
     public static int refreshTime = 1500;
     public static int sendTime;

     /**
     * 无需等待的弹出展示
     *
     * @param context
     * @param rId
     */
    public static void showToastNoWait(Context context, int rId) {
        if (mToast == null) {
            mToast = Toast.makeText(context, rId, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(rId);
            mToast.show();
        }
    }

    public static void showToastNowait(Context context, String str) {
        if (mToast == null) {
            mToast = Toast.makeText(context, str, Toast.LENGTH_SHORT);
            mToast.show();
        } else {
            mToast.setText(str);
            mToast.show();
        }
    }


    /**
     * 短时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showShort(Context context, CharSequence message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }


    public static void showShort(Context context, int id) {
        if (isShow)
            Toast.makeText(context, id, Toast.LENGTH_SHORT).show();
    }


    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, CharSequence message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 长时间显示Toast
     *
     * @param context
     * @param message
     */
    public static void showLong(Context context, int message) {
        if (isShow)
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, CharSequence message, int duration) {
        if (isShow)
            Toast.makeText(context, message, duration).show();
    }

    /**
     * 自定义显示Toast时间
     *
     * @param context
     * @param message
     * @param duration
     */
    public static void show(Context context, int message, int duration) {
        if (isShow)
            Toast.makeText(context, message, duration).show();
    }
}
