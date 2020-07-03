package com.hapi.memotrancer

import android.os.Build
import android.os.Debug
import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.android.tools.perflib.captures.DataBuffer
import com.android.tools.perflib.captures.MemoryMappedFileBuffer
import com.hapi.baseprofile.ActivityCollection
import com.hapi.baseprofile.NotificationHelper
import com.squareup.haha.perflib.ClassObj
import com.squareup.haha.perflib.Snapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.File


class MemoryProfile {

    // 触顶次数
    var memoryTouchTopCount = 0
        private set

    var gcTimeCount = 0
        private set

    //采集次数
    var collectionCount = 0
        private set
    var lastGcCount =0


    private val proportionThreshold = 0.6

    private var timeMinuteSpan = 0L
    private var isStar = false

    private val handler = Handler()
    private val run by lazy {
        Runnable {
            collectionMemory()
            if(isStar){
                repeat()
            }
        }
    }
    private fun repeat(){
        handler.postDelayed(run, timeMinuteSpan * 60 * 1000)
    }

    fun start(timeMinuteSpan: Int) {
        if (isStar) {
            return
        }
        isStar = true
        this.timeMinuteSpan = timeMinuteSpan.toLong()
        repeat()
    }

    fun stop() {
        isStar = false
        handler.removeCallbacks(run)
    }


    private fun collectionMemory() {

        Log.d("collectionMemory","开始收集")

        GlobalScope.launch (Dispatchers.Main){

            val res = async<MemoryIssure?>(Dispatchers.IO) {
                val issure = MemoryIssure()
                collectionCount++
                val javaMax = Runtime.getRuntime().maxMemory()
                val javaTotal = Runtime.getRuntime().totalMemory();
                val javaUsed = javaTotal - Runtime.getRuntime().freeMemory();
                val proportion = javaUsed.toFloat() / javaMax

                var gc =""
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    gc = Debug.getRuntimeStat("art.gc.blocking-gc-count")
                    Log.d("collectionMemory","collectionMemory "+issure.gcCount)

                };
                if(!TextUtils.isEmpty(gc)){
                    issure.gcCount  = (gc.toInt() - lastGcCount).toString()
                    lastGcCount=   gc.toInt()
                }

                if (proportion > proportionThreshold) {
                    memoryTouchTopCount++

                    issure.javaMax = javaMax;
                    issure.javaUsedMemory = javaUsed
                    issure.touchRate = memoryTouchTopCount / collectionCount.toDouble()

                    val heapDumpFile = DumpFileUtils.createDumpFile(ActivityCollection.appContext)

                    if(!heapDumpFile.isEmpty()){
                        val buffer: DataBuffer = MemoryMappedFileBuffer(File( heapDumpFile))
                        val snapshot: Snapshot = Snapshot.createSnapshot(buffer)
                        val bm: ClassObj = snapshot.findClass("android.graphics.Bitmap")
                        val bitMapSize = bm.getShallowSize()

                        issure.dumpFilePatch = heapDumpFile
                        issure.bitmapSize = bitMapSize;
                    }
                }
                issure
            }
            res.await()?.let {
                if(it.javaMax>0){
                    NotificationHelper.send(it)
                }
            }
        }


    }


}