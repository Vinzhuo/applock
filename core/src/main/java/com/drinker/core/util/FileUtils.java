package com.drinker.core.util;

import android.content.Context;
import android.text.TextUtils;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class FileUtils {

    private static final String TAG = FileUtils.class.getSimpleName();
    private static final HashMap<String, String> reportMap = new HashMap<>(1);
    private static String lastDeviceID = "";

    public static boolean isFileExist(String filePath) {
        File file = new File(filePath);
        return file.exists();
    }

    /**
     * 解压缩zip文件，耗时操作，建议放入异步线程
     * @return how many files the ZIP file includs.
     * */
    public static int unzip(String targetPath, String zipFilePath) {
        int fileNumbers = 0;
        try {
            int BUFFER = 2048;
            String fileName = zipFilePath;
            String filePath = targetPath;
            ZipFile zipFile = new ZipFile(fileName);
            Enumeration emu = zipFile.entries();

            while (emu.hasMoreElements()) {
                ZipEntry entry = (ZipEntry) emu.nextElement();
                if(entry.getName().contains("../"))
                    continue;
                if (entry.isDirectory()) {
                    new File(filePath + entry.getName()).mkdirs();
                    continue;
                }
                BufferedInputStream bis = new BufferedInputStream(zipFile.getInputStream(entry));
                File file = new File(filePath + entry.getName());
                File parent = file.getParentFile();
                if (parent != null && (!parent.exists())) {
                    parent.mkdirs();
                }
                FileOutputStream fos = new FileOutputStream(file);
                BufferedOutputStream bos = new BufferedOutputStream(fos, BUFFER);

                int count;
                byte data[] = new byte[BUFFER];
                while ((count = bis.read(data, 0, BUFFER)) != -1) {
                    bos.write(data, 0, count);
                }
                bos.flush();
                bos.close();
                bis.close();
                fileNumbers++;
            }
            zipFile.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return fileNumbers;
    }

    /**
     * copy db file from assets folder to databases folder
     */
    public static void copyAssertDatabasesIfnoExist(Context context, String name) {

        //if file is not exist, copy it.
        File file = context.getDatabasePath(name);

        File dir = file.getParentFile();
        if(!dir.exists()) {
            dir.mkdir();
        }

        if(!file.exists()) {
            try {
                InputStream ins = context.getAssets().open(name);
                FileOutputStream ous = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                int len;

                while((len = ins.read(buffer)) > 0) {
                    ous.write(buffer, 0, len);
                }

                ous.flush();
                ous.close();
                ins.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * copy db file from assets folder to databases folder
     */
    public static void copyAssertDatabases(Context context, String name) {

        //if file is not exist, copy it.
        File file = context.getDatabasePath(name);

        File dir = file.getParentFile();
        if(!dir.exists()) {
            dir.mkdir();
        }
        if (file.exists()) {
            file.delete();
        }
        try {
            InputStream ins = context.getAssets().open(name);
            FileOutputStream ous = new FileOutputStream(file);
            byte[] buffer = new byte[1024];
            int len;

            while((len = ins.read(buffer)) > 0) {
                ous.write(buffer, 0, len);
            }

            ous.flush();
            ous.close();
            ins.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String readJsonFile(Context context, String fileName) {
        String json = "";
        try {
            InputStream ins = context.getAssets().open(fileName);
            InputStreamReader inputreader = new InputStreamReader(ins);
            BufferedReader buffreader = new BufferedReader(inputreader);
            String line = null;
            while ((line = buffreader.readLine()) != null) {
                json += line;
            }
            ins.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    /*
    *  copy src file to dst file
    */
    public static boolean copyFile(String srcFile, String dstFile) {
        if (TextUtils.isEmpty(srcFile) || TextUtils.isEmpty(dstFile)) {
            return false;
        }
        boolean result = false;
        File src = new File(srcFile);
        File dst = new File(dstFile);
        if (!src.exists()) {
            return false;
        }
        if (dst.exists()) {
            return false;
        }
        File parent = dst.getParentFile();
        if (parent.exists() || parent.mkdirs()) {
            try {
                InputStream ins = new FileInputStream(srcFile);
                FileOutputStream ous = new FileOutputStream(dstFile);
                byte[] buffer = new byte[1024];
                int len;

                while((len = ins.read(buffer)) > 0) {
                    ous.write(buffer, 0, len);
                }

                ous.flush();
                ous.close();
                ins.close();

                result = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        return result;
    }/*
    *  mv src file to dst file
    */
    public static boolean moveFile(String srcFile, String dstFile) {
        if (TextUtils.isEmpty(srcFile) || TextUtils.isEmpty(dstFile)) {
            return false;
        }
        boolean result = false;
        File src = new File(srcFile);
        File dst = new File(dstFile);
        if (!src.exists()) {
            return false;
        }
        if (dst.exists()) {
            return false;
        }
        File parent = dst.getParentFile();
        if (parent.exists() || parent.mkdirs()) {
            try {
                InputStream ins = new FileInputStream(srcFile);
                FileOutputStream ous = new FileOutputStream(dstFile);
                byte[] buffer = new byte[1024];
                int len;

                while((len = ins.read(buffer)) > 0) {
                    ous.write(buffer, 0, len);
                }

                ous.flush();
                ous.close();
                ins.close();

                result = true;

            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return false;
        }
        if (result) {
            if (src.delete()) {
                result = true;
            } else {
                dst.delete();
                result = false;
            }
        }
        return result;
    }

    public static boolean deleteDictionary(File dir) {
        if (dir != null && dir.isDirectory()) {
            File[]childFiles = dir.listFiles();
            if (childFiles != null && childFiles.length > 0) {
                for (File file : childFiles) {
                    if (file == null) {
                        continue;
                    }
                    if (file.isFile()) {
                        file.delete();
                    } else {
                        deleteDictionary(file);
                    }
                }
            }
            return dir.delete();
        }
        return false;
    }

    public static boolean delFile(File file) {
        if (file == null) {
            return false;
        }
        if (file.isFile()) {
            return file.delete();
        }
        if (file.isDirectory()) {
            File[]childFiles = file.listFiles();
            if (childFiles != null && childFiles.length > 0) {
                for (File child : childFiles) {
                    if (child == null) {
                        continue;
                    }
                    delFile(file);
                }
            }
            return file.delete();
        }
        return false;
    }


    public static boolean moveFile(String src, String dst, String deviceid) {
        reportMap.clear();
        if (TextUtils.isEmpty(src) || TextUtils.isEmpty(dst)) {

            return false;
        }
        File srcFile = new File(src);
        File dstFile = new File(dst);
        if (srcFile.exists()) {
            if (dstFile.exists()) {
                return false;
            }
            File dp = dstFile.getParentFile();
            if (dp.exists()) {
                boolean result =  srcFile.renameTo(dstFile);
                if (result) {
                    return true;
                } else {
                    if (moveFile(src,dst)){
                        return true;
                    }
                }
            } else {
                if (dp.mkdirs()) {
                    boolean result =  srcFile.renameTo(dstFile);
                    if (result) {
                        return true;
                    } else {
                        if (moveFile(src,dst)) {
                            return true;
                        } else {
                        }
                    }
                }
            }
        }
        return false;
    }

}
