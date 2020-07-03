package com.hapi.memotrancer;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DumpFileUtils {
    public static String  createDumpFile(Context context) {
        String LOG_PATH = "/dump.gc/";
        boolean bool = false;
        String dumpFilePatch = ""; 
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH.mm.ssss", Locale.getDefault());
        String createTime = sdf.format(new Date(System.currentTimeMillis()));
        String state = android.os.Environment.getExternalStorageState();

        // 判断SdCard是否存在并且是可用的
        if (android.os.Environment.MEDIA_MOUNTED.equals(state)) {
            File file = new File(Environment.getExternalStorageDirectory().getPath() + LOG_PATH);
            if (!file.exists()) {
                file.mkdirs();
            }

            String hprofPath = file.getAbsolutePath();
            if (!hprofPath.endsWith("/")) {
                hprofPath += "/";
            }

            hprofPath += createTime + ".hprof";
            try {
                dumpFilePatch = hprofPath;
                android.os.Debug.dumpHprofData(hprofPath);
                bool = true;
                Log.d("DumpFileUtils", "create dumpfile done!");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            bool = false;
            Log.d("DumpFileUtils", "no sdcard!");
        }
        return dumpFilePatch;
    }   
}
