package com.anguomob.lib.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;


import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AppUtils {

    /**
     * 获取包名
     * @param context
     * @return
     */
    public static String getPackageName(Context context){
        return context.getPackageName();
    }

    /**
     * 获取VersionName(版本名称)
     * @param context
     * @return
     * 失败时返回""
     */
    public static String getVersionName(Context context){
        PackageManager packageManager = getPackageManager(context);
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(context), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取VersionCode(版本号)
     * @param context
     * @return
     * 失败时返回-1
     */
    public static int getVersionCode(Context context){
        PackageManager packageManager = getPackageManager(context);
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(context), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取所有安装的应用程序,不包含系统应用
     * @param context
     * @return
     */
    public static List<PackageInfo> getInstalledPackages(Context context){
        PackageManager packageManager = getPackageManager(context);
        List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
        List<PackageInfo> packageInfoList  = new ArrayList<PackageInfo>();
        for(int i=0; i < packageInfos.size();i++){
            if ((packageInfos.get(i).applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                packageInfoList.add(packageInfos.get(i));
            }
        }
        return packageInfoList;
    }

    /**
     * 获取应用程序的icon图标
     * @param context
     * @return
     * 当包名错误时，返回null
     */
    public static Drawable getApplicationIcon(Context context){
        PackageManager packageManager = getPackageManager(context);
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(context), 0);
            return packageInfo.applicationInfo.loadIcon(packageManager);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 启动安装应用程序
     * @param activity
     * @param path	应用程序路径
     */
    public static void installApk(Activity activity, String path){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(path)),
                "application/vnd.android.package-archive");
        activity.startActivity(intent);
    }

    /**
     * 获取PackageManager对象
     * @param context
     * @return
     */
    private static PackageManager getPackageManager(Context context){
        return context.getPackageManager();
    }


    /*获取cpu 核心数*/
    public static String getNumCores() {
        // Private Class to display only CPU devices in the directory listing
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                // Check if filename is "cpu", followed by a single digit number
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            // Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            // Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            // Return the number of cores (virtual CPU devices)
            return files.length + "";
        } catch (Exception e) {
            // Default to return 1 core
            return "1";
        }
    }

    /**
     * 获取CPU型号
     *
     * @return
     */
    public static String getCpuName() {

        String str1 = "/proc/cpuinfo";
        String str2 = "";

        try {
            FileReader fr = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(fr);
            while ((str2 = localBufferedReader.readLine()) != null) {
                if (str2.contains("Hardware")) {
                    return str2.split(":")[1];
                }
            }
            localBufferedReader.close();
        } catch (IOException e) {
        }
        return null;

    }

    /**
     *
     * 获取CPU最大频率（单位KHZ）
     * @return
     */

    // "/system/bin/cat" 命令行


    // "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径
//返回GHZ /10^6
    public static String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();


        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        if (result.length() >= 6) {

            double res = 0;
            try {
                res = Float.parseFloat(getGroup(result)) / Math.pow(10, 6);
                return String.format("%.1f", res) + "GHZ";

            } catch (NumberFormatException e) {
                e.printStackTrace();
                return "N/A";
            }
        }
        return result.trim();
    }

    private static String getGroup(String result) {
        // 2188800
        //    ��������������������������������

        Pattern compile = Pattern.compile(".+\\d+");
        Matcher matcher = compile.matcher(result);
        boolean isFind = matcher.find();
        if (isFind) {
            return matcher.group(0);

        } else {
            return "N/A";
        }

    }


    /**
     * 获得SD卡总大小
     *
     * @param context
     * @return
     */
    public static long getSDTotalSize(Context context) {
        File path = Environment.getExternalStorageDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
//        Formatter.formatFileSize(context,)
        return blockSize * totalBlocks;
    }

    public static long getTotalRam(Context context){//GB
        String path = "/proc/meminfo";
        String firstLine = null;
        long totalRam = 0 ;
        try{
            FileReader fileReader = new FileReader(path);
            BufferedReader br = new BufferedReader(fileReader,8192);
            firstLine = br.readLine().split("\\s+")[1];
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }
        if(firstLine != null){
            totalRam = (int) Math.ceil((new Float(Float.valueOf(firstLine) ).doubleValue()));
        }

        return totalRam *1024;//返回1GB/2GB/3GB/4GB
    }

    public static String getScreenWidth(Context activity) {
//        return activity.getWindowManager().getDefaultDisplay().getWidth() + "";
        return  activity.getResources().getDisplayMetrics().heightPixels + "";
    }

    public static String getScreenHeight(Context activity) {
//        return activity.getWindowManager().getDefaultDisplay().getHeight() + "";
        return  activity.getResources().getDisplayMetrics().widthPixels + "";
    }

    /**
     * 获取本地软件版本号名称
     */
    public static String getLocalVersionName(Context ctx) {
        String localVersion = "";
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }
//    ym 获取渠道号AnalyticsConfig.getChannel(this)




    /**
     * JAVA获得0-9,a-z,A-Z范围的随机数
     *
     * @param length 随机数长度
     * @return String
     */
    public static String getRandomChar(int length) {
        char[] chr = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '~', '_', '!'};
        Random random = new Random();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < length; i++) {
            buffer.append(chr[random.nextInt(62)]);
        }
        return buffer.toString();
    }

    // 获取可用内存空间大小
    public static long getAvailMemory(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo outInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(outInfo);
        return Math.abs(outInfo.availMem);
    }

    // 获取总运存大小
    public static long getTotalMemory(Context context) {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        long initial_memory = 0;

        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split("\\s+");
            for (String num : arrayOfString) {
                Log.i("tatolram:" + str2, num + "\t");
            }
            initial_memory = Long.valueOf(arrayOfString[1]).longValue() * 1024;// 获得系统总内存，单位是KB，乘以1024转换为Byte
            localBufferedReader.close();

        } catch (Exception e) {
        }
        return Math.abs(initial_memory);
    }

    public static void startAPP(Context context , String appPackageName){
        try{
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(appPackageName);
            context.startActivity(intent);
        }catch(Exception e){
            //Toast.makeText(context, "没有安装", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * SDCARD是否存
     */
    public static boolean externalMemoryAvailable() {
        return android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
    }

    /**
     * 获取SDCARD剩余存储空间
     *
     * @return
     */
    public static long getAvailableExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            }else{
                blockSize = stat.getBlockSize();
            }
            long availableBlocks = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                availableBlocks = stat.getAvailableBlocksLong();
            }else{
                availableBlocks = stat.getAvailableBlocks();
            }
            return availableBlocks * blockSize;
        } else {
            return 0;
        }
    }

    /**
     * 获取SDCARD总的存储空间
     *
     * @return
     */
    public static long getTotalExternalMemorySize() {
        if (externalMemoryAvailable()) {
            File path = Environment.getExternalStorageDirectory();
            StatFs stat = new StatFs(path.getPath());
            long blockSize = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                blockSize = stat.getBlockSizeLong();
            }else{
                blockSize =stat.getBlockSize();
            }
            long totalBlocks = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
                totalBlocks = stat.getBlockCountLong();
            }else{
                totalBlocks = stat.getBlockCount();
            }
            return totalBlocks * blockSize;
        } else {
            return 0;
        }
    }



    /**
     * 获取手机内部剩余存储空间
     *
     * @return
     */
    public static long getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
        }else{
            blockSize = stat.getBlockSize();
        }
        long availableBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            availableBlocks = stat.getAvailableBlocksLong();
        }else{
            availableBlocks = stat.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * 获取手机内部总的存储空间
     *
     * @return
     */
    public static long getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = stat.getBlockSizeLong();
        }else{
            blockSize = stat.getBlockSize();
        }
        long totalBlocks = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            totalBlocks = stat.getBlockCountLong();
        }else{
            totalBlocks = stat.getBlockCount();
        }
        return totalBlocks * blockSize;
    }




}
