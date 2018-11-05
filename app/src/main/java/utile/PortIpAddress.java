package utile;

import android.content.Context;

public class PortIpAddress {
    public static String SUCCESS_CODE = "true";
    public static String ERR_CODE = "0001";
    public static String CODE = "success";
    public static String MESSAGE = "errormessage";
    public static String JsonArrName = "data";
    public static String TOKEN_KEY = "access_token";
    public static String USERID_KEY = "userid";
    public static String Bean = "bean.";


    public static String getUserId(Context context){
        return SharedPrefsUtil.getValue(context,"userInfo","userid","");
    }

    public static String getUserToken(Context context){
        return SharedPrefsUtil.getValue(context,"userInfo","user_token","");
    }

    public static String getUserType(Context context){
        return SharedPrefsUtil.getValue(context,"userInfo","usertype","");
    }




    //正式环境
//    public static String host = "http://www.lcseed.com.cn/lczy/mobile/";

    //徐
    //  public static String host = "http://192.168.5.49:8087/lczy/mobile/";

    //外网
    public static String host = "http://112.74.124.217:9797/lczy/mobile/";

    //登录
    public static String Login() {
        return host + "login.action";
    }

    public static String updateApp() {
        return host + "appversion.action";
    }

    //修改密码
    public static String updatePwd() {
        return host + "updatepwd.action";
    }

    //经销商列表
    public static String DlsJxsList() {
        return host + "mdealers/doList.action";
    }

    //新增代理商、经销商
    public static String AddDlsJxs() {
        return host + "mdealers/doAddSave.action";
    }

    //代理商、经销商查一条详情
    public static String DlsJxsDetail() {
        return host + "mdealers/doView.action";
    }

    //删除一条
    public static String DeleteOne() {
        return host + "mdealers/delete.action";
    }

    //代理商、经销商信息修改完成
    public static String ModifyDlsJxs() {
        return host + "mdealers/doModifySave.action";
    }

    //一级出库任务第二步接口
    public static String OutLibraryStepTwo() {
        return host + "mouttask/doAddSave.action";
    }

    //一级出库任务第三步接口-扫码保存
    public static String OutLibraryResult() {
        return host + "mouttask/doOutdataSave.action";
    }

    //出库历史-经销商列表
    public static String CkHistoryJxsList() {
        return host + "mdealers/listByOutTask.action";
    }

    //出库历史-任务列表
    public static String CkHistoryRwList() {
        return host + "mouttask/listByOutTask.action";
    }

    //出库历史-详情结果
    public static String OutLibraryDetail() {
        return host + "mouttask/doView.action";
    }

    //出库历史-详情结果-大码清单
    public static String OutLibraryDetailBmList() {
        return host + "mouttask/doViewMaxcode.action";
    }

    //一级退库任务第二步接口
    public static String TkStepTwo() {
        return host + "mrefundtask/doAddSave.action";
    }

    //一级退库任务第三步接口-扫码保存
    public static String TkLibraryResult() {
        return host + "mrefundtask/doTaskdataSave.action";
    }

    //退库历史-经销商列表
    public static String TkHistoryJxsList() {
        return host + "mdealers/listByRefundTask.action";
    }

    //退库历史-任务列表接口
    public static String TkHistoryRwList() {
        return host + "mrefundtask/listByTask.action";
    }

    //退库历史-详情结果
    public static String TkLibraryDetail() {
        return host + "mrefundtask/doView.action";
    }

    //退库历史-详情结果-大码清单
    public static String TkLibraryDetailBmList() {
        return host + "mrefundtask/doViewMaxcode.action";
    }

    //调剂任务第二步接口
    public static String TiaoJiLibraryStepTwo() {
        return host + "mregulatetask/doAddSave.action";
    }

    //调剂任务第三步接口-扫码
    public static String TiaoJiLibraryResult() {
        return host + "mregulatetask/doTaskdataSave.action";
    }

    //调剂历史-经销商列表
    public static String TiaoJiHistoryJxsList() {
        return host + "mdealers/listByRegulateTask.action";
    }

    //调剂历史-任务列表接口
    public static String TiaoJiHistoryRwList() {
        return host + "mregulatetask/listByTask.action";
    }

    //调剂历史-详情结果
    public static String TiaoJiLibraryDetail() {
        return host + "mregulatetask/doView.action";
    }

    //调剂历史-详情结果-大码清单
    public static String TiaoJiLibraryDetailBmList() {
        return host + "mregulatetask/doViewMaxcode.action";
    }

    //防窜查询接口
    public static String FangCuan() {
        return host + "mfc/fangcuan.action";
    }

    //离线批量出库
    public static String OffLineCk(){
        return host+"mouttask/doBatchOutSave.action";
    }
}
