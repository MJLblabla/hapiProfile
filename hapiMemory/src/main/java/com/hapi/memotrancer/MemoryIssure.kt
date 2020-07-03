package com.hapi.memotrancer

import android.content.Intent
import com.hapi.baseprofile.ActivityCollection
import com.hapi.baseprofile.NotificationAble


class MemoryIssure : NotificationAble {

    var javaMax =0L
    var javaUsedMemory =0L
    var touchRate = 0.0
    var gcCount =""

    var dumpFilePatch =""
    var bitmapSize =0

    override fun getTittle(): String {
       return "老铁 你的内存占用也太多了吧"
    }
    override fun getContent(): String {
      return "Activity ${ActivityCollection.currentActivity?.localClassName?:"null"} \n" +
               "javaMax ${javaMax} \n" +
               "javaUsedMemory ${javaUsedMemory} \n" +
               "触顶率 ${touchRate} \n" +
               "gc次数 ${gcCount} \n" +

              "bitmapSize  ${bitmapSize}\n" +

              "dumpFilePatch ${dumpFilePatch} \n"

    }
    override fun getIntent(): Intent? {
        val intent = if (ActivityCollection.currentActivity != null) {
            Intent(ActivityCollection.currentActivity, MemoryIssureActivity::class.java)
        } else {
            Intent(ActivityCollection.appContext, MemoryIssureActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        intent.putExtra("memoryIssure",getContent())
        return intent
    }
}