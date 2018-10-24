package utile;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.administrator.materialmanagement.R;


public class DialogUtil {
    /**
     * 得到自定义的progressDialog
     *
     * @param context
     * @param msg     文字显示
     * @return
     */
    public static Dialog createLoadingDialog(Context context, String msg) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.loading_rl);// 加载布局
        TextView tipTextView = (TextView) v.findViewById(R.id.loading_text);// 提示文字
        tipTextView.setText(msg);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loadingDialog);// 创建自定义样式dialog
//        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
//        loadingDialog.setCanceledOnTouchOutside(true); //点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.show();
        return loadingDialog;
    }


    public static Dialog createLoadingDialog(Context context, int id) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View v = inflater.inflate(R.layout.loading_dialog, null);// 得到加载view
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.loading_rl);// 加载布局
        TextView tipTextView = (TextView) v.findViewById(R.id.loading_text);// 提示文字
        tipTextView.setText(id);// 设置加载信息

        Dialog loadingDialog = new Dialog(context, R.style.loadingDialog);// 创建自定义样式dialog
//        loadingDialog.setCancelable(true); // 是否可以按“返回键”消失
//        loadingDialog.setCanceledOnTouchOutside(true); //点击加载框以外的区域
        loadingDialog.setContentView(layout, new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT));// 设置布局
        loadingDialog.show();

        return loadingDialog;
    }
}
