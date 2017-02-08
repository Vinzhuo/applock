package com.drinker.alock.util.process;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;

import com.drinker.alock.util.log.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by zhuolin on 15-7-30.
 */
public class ProcessUtils {


    private static final String TAG = "ProcessUtil";

    private static Map<String, String> specialForeProcess = new HashMap<>(1);

    private static int ppid = 0;


    static {
        specialForeProcess.put("com.ali.money.shield", "com.ali.money.shield:fore");
        specialForeProcess.put("com.tencent.qqpimsecure", "com.tencent.qqpimsecure:fore");
        specialForeProcess.put("com.google.android.music", "com.google.android.music:ui");
    }

    public static int getProcessPid(Context ctx, String packageName) {
        if (ctx == null) {
            return 0;
        }
        String processName = specialForeProcess.get(packageName);
        if (TextUtils.isEmpty(processName)) {
            processName = packageName;
        }
        int pid = getProcessPidDefault(ctx, processName);
        if (pid == 0) {
            pid = getProcessPidNew(ctx, processName);
        }
        return pid;
    }

    private static int getProcessPidNew(Context ctx, String processName) {
        int pid = 0;
        if (TextUtils.isEmpty(processName)) {
            return pid;
        }
        String cmd = "ps | grep " + processName;
        Process localProcess = null;
        BufferedReader in = null;
        DataOutputStream localDataOutputStream = null;
        try {
            localProcess = Runtime.getRuntime().exec("sh");
            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
            localDataOutputStream.writeBytes(cmd + " &\n");
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            String line;
            String[]temp;
            if (in.ready()) {
                while ((line = in.readLine()) != null) {
                    line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
                    temp = line.split(" ");
                    if (temp != null) {
                        String t8 = "";
                        if (temp.length >= 9) {
                            t8 = temp[8];
                        } else if (temp.length >= 8) {
                            t8 = temp[7];
                        }
                        if (TextUtils.isEmpty(t8)) {
                            continue;
                        }
                        if (t8.equals(processName)) {
                            return Integer.parseInt(temp[1]);
                        }
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (localDataOutputStream != null) {
                    localDataOutputStream.close();
                }
                if (localProcess != null) {
                    localProcess.waitFor();
                    localProcess.destroy();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static int getProcessPidDefault(Context cxt, String processName) {
        ActivityManager activityManager = (ActivityManager) cxt
                .getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
        if (appProcessInfos != null) {
            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
                int pid = appProcessInfo.pid;
                if (appProcessInfo.processName.compareTo(processName) == 0) {
                    return pid;
                }
            }
            return 0;
        }
        return 0;
    }

//    public static boolean processIsForeground(String processName) {
//        ActivityManager activityManager = (ActivityManager) PrivacyShieldApplication.get()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
//        if (appProcessInfos != null) {
//            for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfos) {
//                if (appProcessInfo.processName.compareTo(processName) == 0 && appProcessInfo.importance == 100) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }

//    public static boolean isProcessAlive(String name) {
//        boolean isAlive = false;
//        if (name != null) {
//            ActivityManager activityManager = (ActivityManager) PrivacyShieldApplication.get()
//                    .getSystemService(Context.ACTIVITY_SERVICE);
//            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
//            if (appProcessInfos != null) {
//                for (ActivityManager.RunningAppProcessInfo info : appProcessInfos) {
//                    if (info.processName.equalsIgnoreCase(name)) {
//                        isAlive = true;
//                        break;
//                    }
//                }
//            }
//        }
//
//        return isAlive;
//    }
//
//    public static boolean isProcessAlive(int pid) {
//        int mypid = android.os.Process.myPid();
//        if (pid == mypid) {
//            return true;
//        }
//        String cmd = "ps | grep " + pid;
//
//        Process localProcess;
//        BufferedReader in = null;
//        DataOutputStream localDataOutputStream = null;
//        try {
//            localProcess = Runtime.getRuntime().exec("sh");
//            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
//            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
//            localDataOutputStream.writeBytes(cmd + " &\n");
//            localDataOutputStream.flush();
//            localDataOutputStream.writeBytes("exit\n");
//            localProcess.waitFor();
//            String line;
//            String[]temp;
//            while ((line = in.readLine()) != null) {
//                line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
//                temp = line.split(" ");
//                if (temp.length >= 2 && !TextUtils.isEmpty(temp[1]) && temp[1].trim().equals(String.valueOf(pid))) {
//                    return true;
//                }
//            }
//
//        } catch (IOException | InterruptedException e) {
//            Log.e(TAG, e.getMessage());
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//                if (localDataOutputStream != null) {
//                    localDataOutputStream.close();
//                }
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage());
//            }
//        }
//
//        return false;
//    }


    public static int getPpid(int pid) {
        String cmd = "ps | grep " + pid;
        Process localProcess = null;
        BufferedReader in = null;
        DataOutputStream localDataOutputStream = null;
        try {
            localProcess = Runtime.getRuntime().exec("sh");
            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
            localDataOutputStream.writeBytes(cmd + " &\n");
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
//            localProcess.waitFor();
            String line;
            String[]temp;
            while ((line = in.readLine()) != null) {
                line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
                temp = line.split(" ");
                if (temp.length >= 3 && !TextUtils.isEmpty(temp[1]) && temp[1].trim().equals(String.valueOf(pid))) {
                    if (!TextUtils.isEmpty(temp[2])) {
                        return Integer.parseInt(temp[2]);
                    }
                }
            }

        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (localDataOutputStream != null) {
                    localDataOutputStream.close();
                }
                if (localProcess != null) {
                    localProcess.waitFor();
                    localProcess.destroy();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

//    public static String getProcessName(int pid) {
//        String processName = getProcessNameDefault(pid);
//        if (TextUtils.isEmpty(processName)) {
//            processName = getProcessNameNew(pid);
//        }
//        return processName;
//    }

//    private static String getProcessNameDefault(int pid) {
//        ActivityManager activityManager = (ActivityManager) PrivacyShieldApplication.get()
//                .getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
//        if (appProcessInfos != null) {
//            for (ActivityManager.RunningAppProcessInfo info : appProcessInfos) {
//                if (info.pid == pid) {
//                    return info.processName;
//                }
//            }
//        }
//        return "";
//    }
//
//    private static String getProcessNameNew(int pid) {
//        String cmd = "ps | grep " + pid;
//        Process localProcess;
//        BufferedReader in = null;
//        DataOutputStream localDataOutputStream = null;
//        try {
//            localProcess = Runtime.getRuntime().exec("sh");
//            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
//            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
//            localDataOutputStream.writeBytes(cmd + " &\n");
//            localDataOutputStream.flush();
//            localDataOutputStream.writeBytes("exit\n");
//            localProcess.waitFor();
//            String line;
//            String[]temp;
//            while ((line = in.readLine()) != null) {
//                line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
//                temp = line.split(" ");
//                if (temp.length >= 9 && !TextUtils.isEmpty(temp[1]) && temp[1].trim().equals(String.valueOf(pid))) {
//                    return temp[8];
//                }
//            }
//
//        } catch (IOException | InterruptedException e) {
//            Log.e(TAG, e.getMessage());
//        } finally {
//            try {
//                if (in != null) {
//                    in.close();
//                }
//                if (localDataOutputStream != null) {
//                    localDataOutputStream.close();
//                }
//            } catch (IOException e) {
//                Log.e(TAG, e.getMessage());
//            }
//        }
//        return "";
//    }

    public static void killProcess(int pid) {
        android.os.Process.killProcess(pid);
    }

    private static class ShellResult {
        private BufferedReader in;
        private Process ps;
        private void destroy() {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (ps != null) {
                try {
                    ps.waitFor();
                    ps.destroy();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private static ShellResult execShell(String cmd) {
        if (TextUtils.isEmpty(cmd)) {
            return null;
        }
        Process localProcess = null;
        BufferedReader in = null;
        ShellResult result = new ShellResult();
        DataOutputStream localDataOutputStream = null;
        try {
            localProcess = Runtime.getRuntime().exec("sh");
            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
            localDataOutputStream.writeBytes(cmd + " &\n");
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
//            localProcess.waitFor();
        } catch (IOException  e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (localDataOutputStream != null) {
                try {
                    localDataOutputStream.close();
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
            result.in = in;
            result.ps = localProcess;
            return result;
        }
    }

    private static boolean containString(String [] strings, String string) {
        if (TextUtils.isEmpty(string)) {
            return false;
        }
        for (String s : strings) {
            if (string.equals(s)) {
                return true;
            }
        }
        return false;
    }

    public static Collection<ProcessData> getProcessInfo(String... packageNames) {
        if (packageNames == null || packageNames.length <= 0) {
            return null;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("ps | grep ");

        for (String packageName : packageNames) {
            builder.append("-e ");
            builder.append(packageName);
            builder.append(" ");
        }
        String cmd = builder.toString();
        ShellResult result = execShell(cmd);
        BufferedReader in = result.in;
        Map<String, ProcessData> processDatas = new HashMap<>();
        String line;
        String[]temp;
        Collection<ProcessData> dataCollections = null;
        BufferedReader reader = null;
        if (in == null) {
            result.destroy();
            return processDatas.values();
        }
        try {
            while ((line = in.readLine()) != null) {
                line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
                temp = line.split(" ");
                if (temp != null) {
                    String t8 = "";
                    if (temp.length >= 9) {
                        t8 = temp[8];
                    } else if (temp.length >= 8) {
                        t8 = temp[7];
                    }
                    if (TextUtils.isEmpty(t8)) {
                        continue;
                    }
                    ProcessData processData = new ProcessData();
                    int index = t8.indexOf(":");
                    if(index == -1 && containString(packageNames, t8)) {
                        processData.packageName = t8;
                        processData.pid = Integer.parseInt(temp[1]);
                        processData.ppid = Integer.parseInt(temp[2]);
                        processDatas.put(processData.packageName, processData);
                    } else if (index != -1){
                        String name = t8.substring(0, index);
                        if (t8.equals(specialForeProcess.get(name))) {
                            processData.packageName = name;
                            processData.pid = Integer.parseInt(temp[1]);
                            processData.ppid = Integer.parseInt(temp[2]);
                            processDatas.remove(processData.packageName);
                            processDatas.put(processData.packageName, processData);
                        }
                    }
                }
            }
            dataCollections = processDatas.values();
            Iterator<ProcessData> iterator = dataCollections.iterator();
            while (iterator.hasNext()) {
                ProcessData data = iterator.next();
                if (data.ppid != getPpid()) {
                    iterator.remove();
                } else {
                    reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + data.pid + "/oom_score")));
                    String tmp = reader.readLine();
                    if (!TextUtils.isEmpty(tmp)) {
                        data.oomScore = Integer.valueOf(tmp);
                    }
                    if (reader != null) {
                        reader.close();
                        reader = null;
                    }
                }

            }
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                result.destroy();
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return dataCollections;
    }

    public static List<ProcessData> getTopProcess(String...packageNames) {
//        if (Build.VERSION.SDK_INT >= 23) {
            return getAndroidMTopProcess(packageNames);
//        }
    }

    private static List<ProcessData> getAndroidLTopProcess(Collection<ProcessData> processDatas) {
        if (processDatas == null || processDatas.size() <= 0) {
            return null;
        }
        List<ProcessData> topNames = new ArrayList<>(processDatas.size());
        BufferedReader reader1 = null;
        for (ProcessData processData : processDatas) {
            try {
                reader1 = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + processData.pid + "/oom_score_adj")));
                String line1 = reader1.readLine();
                if (!TextUtils.isEmpty(line1)) {
                    line1 = line1.trim();
                }
                if ("0".equals(line1)) {
                    topNames.add(processData);
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } finally {
                try {
                    if (reader1 != null) {
                        reader1.close();
                    }
                } catch (IOException e) {
                    Log.e(TAG, e.getMessage());
                }
            }
        }
        return topNames;
    }

    private static List<ProcessData> getAndroidMTopProcess(String...packageNames) {
        Collection<ProcessData> processDatas = getProcessInfo(packageNames);
        if (processDatas == null || processDatas.size() <= 0) {
            return null;
        }
        List<ProcessData> topNames = getTopProcess(processDatas);
        if (topNames.size() <= 0) {
            topNames = getTopProcessReverse(processDatas);
        }
        return topNames;
    }

    private static List<ProcessData> getTopProcess(Collection<ProcessData> processDatas) {
        List<ProcessData> topNames = new ArrayList<>(processDatas.size());
        ShellResult shellResult = null;
        BufferedReader in = null;
        String line1 = "";
        BufferedReader reader = null;
        String line2 = "";
        StringBuilder builder = new StringBuilder();
        builder.append("grep ");

        for (ProcessData processData : processDatas) {
            builder.append("-e ");
            builder.append(String.valueOf(processData.pid));
            builder.append(" ");
        }
        try {
            String cmd = "cat " + getForeTaskFile() + " | " + builder.toString();
            shellResult = execShell(cmd);
            in = shellResult.in;
            while (in != null && (line1 = in.readLine()) != null) {
                if (!TextUtils.isEmpty(line1)) {
                    line1 = line1.trim();
                }
                for (ProcessData processData : processDatas) {
                    if (String.valueOf(processData.pid).equals(line1)) {
                        if (Build.VERSION.SDK_INT < 23) {
                            try {
                                reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + processData.pid + "/oom_adj")));
                                line2 = reader.readLine();
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage());
                            }
                        }
                        if ((TextUtils.isEmpty(line2) || "0".equals(line2))) {
                            topNames.add(processData);
                        }
                    }
                    if (reader != null) {
                        reader.close();
                        reader = null;
                    }
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (shellResult != null) {
                    shellResult.destroy();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return topNames;
    }

    private static List<ProcessData> getTopProcessReverse(Collection<ProcessData> processDatas) {
        List<ProcessData> topNames = new ArrayList<>(processDatas.size());
        BufferedReader in = null;
        ShellResult shellResult = null;
        String line1 = "";
        BufferedReader reader = null;
        String line2 = "";
        StringBuilder builder = new StringBuilder();
        builder.append("grep ");

        for (ProcessData processData : processDatas) {
            builder.append("-e ");
            builder.append(String.valueOf(processData.pid));
            builder.append(" ");
        }
        try {
            String cmd = "cat " + getbgTaskFile() + " | " + builder.toString();
            shellResult = execShell(cmd);
            in = shellResult.in;
            while (in != null && (line1 = in.readLine()) != null) {
                if (!TextUtils.isEmpty(line1)) {
                    line1 = line1.trim();
                }
                for (ProcessData processData : processDatas) {
                    if (processData.status != ProcessData.BACKGROUND && String.valueOf(processData.pid).equals(line1)) {
                        processData.status = ProcessData.BACKGROUND;
                    }
                }
            }
            for (ProcessData processData : processDatas) {
                if (processData.status == ProcessData.UNKNOWN) {
                    processData.status = ProcessData.FORE;
                }
                if (processData.status == ProcessData.FORE) {
                    if (Build.VERSION.SDK_INT < 23) {
                        try {
                            reader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + processData.pid + "/oom_adj")));
                            line2 = reader.readLine();
                        } catch (IOException e) {
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    if ((TextUtils.isEmpty(line2) || "0".equals(line2))) {
                        topNames.add(processData);
                    }
                }
                if (reader != null) {
                    reader.close();
                    reader = null;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (shellResult != null) {
                    shellResult.destroy();
                }
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
        return topNames;
    }

    private static String getForeTaskFile() {
        String path = "";
        if (Build.VERSION.SDK_INT >= 22) {
            path = "/dev/cpuctl/tasks";
        } else if (Build.VERSION.SDK_INT == 21) {
            path = "/dev/cpuctl/apps/tasks";
        }
        return path;
    }

    private static String getbgTaskFile() {
        String path = "";
        if (Build.VERSION.SDK_INT >= 22) {
            path = "/dev/cpuctl/bg_non_interactive/tasks";
        } else if (Build.VERSION.SDK_INT == 21) {
            path = "/dev/cpuctl/apps/bg_non_interactive/tasks";
        }
        return path;
    }

    public static List<Integer> getChildPid(int pid) {
        String cmd = "ps | grep " + pid;
        Process localProcess = null;
        BufferedReader in = null;
        DataOutputStream localDataOutputStream = null;
        List<Integer> childPids = new ArrayList<>(1);
        try {
            localProcess = Runtime.getRuntime().exec("sh");
            in = new BufferedReader(new InputStreamReader(localProcess.getInputStream()));
            localDataOutputStream = new DataOutputStream(localProcess.getOutputStream());
            localDataOutputStream.writeBytes(cmd + " &\n");
            localDataOutputStream.flush();
            localDataOutputStream.writeBytes("exit\n");
            String line;
            String[]temp;
            while ((line = in.readLine()) != null) {
                line = line.replaceAll("\\s+", " ");//替换多个空格为单个空格
                temp = line.split(" ");
                if (temp.length >= 3 && !TextUtils.isEmpty(temp[2]) && temp[2].trim().equals(String.valueOf(pid))) {
                    if (!TextUtils.isEmpty(temp[1])) {
                        childPids.add(Integer.valueOf(temp[1].trim()));
                    }
                }
            }

        } catch (IOException  e) {
            Log.e(TAG, e.getMessage());
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (localDataOutputStream != null) {
                    localDataOutputStream.close();
                }
                if (localProcess != null) {
                    localProcess.waitFor();
                    localProcess.destroy();
                }
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return childPids;
    }



    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String queryUsageStats(Context context) {
        if (Build.VERSION.SDK_INT < 21 || context == null) {
            return "";
        }
        UsageStatsManager usageStatsManager = (UsageStatsManager) context.getSystemService(Activity.USAGE_STATS_SERVICE);
        String currentTopPackage = null;
        long currentTimeMillis = System.currentTimeMillis();
        long timegap = 10000;
        long inncgap = 5000;
        int i = 0;
        while (TextUtils.isEmpty(currentTopPackage) && timegap < 8 * 60 * 60 * 1000) {
            UsageEvents queryEvents = usageStatsManager.queryEvents(currentTimeMillis - timegap, currentTimeMillis);
            UsageEvents.Event event = new UsageEvents.Event();
            while (queryEvents.hasNextEvent()) {
                queryEvents.getNextEvent(event);
                if (event.getEventType() == 1) {
                    currentTopPackage = event.getPackageName();
                    Log.i(TAG, "Pkg : " + currentTopPackage + " timetamp : " + event.getTimeStamp() + " index " + i);
                }
            }
            timegap +=  (i * timegap + inncgap);
            i++;
        }
        return currentTopPackage;

    }

    public static int getPpid() {
        if (ppid > 0) {
            return ppid;
        }
        ppid = ProcessUtils.getPpid(android.os.Process.myPid());
        return ppid;
    }

}
