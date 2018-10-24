package utile;


import android.Manifest;

public class PermissionUtil {
    //读写文件权限
    public static String WriteFilePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    //创建删除文件权限
    public static String CreateDeleteFilePermission = Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS;
    //定位权限
    public static String LocationPermission = Manifest.permission.ACCESS_COARSE_LOCATION;
    //调用相机权限
    public static String CameraPermission = Manifest.permission.CAMERA;
    //网络权限
    public static String InternetPermission = Manifest.permission.INTERNET;
    //读写文件权限响应码
    public static final int STORAGE_REQUESTCODE = 100;
    //创建删除文件权限响应码
    public static int FILESYSTEMS_REQUESTCODE = 101;
    //相机权限响应码
    public static int CAMERA_REQUESTCODE = 102;
    //地图权限响应码
    public static int LOCATION_REQUESTCODE = 103;
    //网络权限响应码
    public static int INTERNET_REQUESTCODE = 104;

}
