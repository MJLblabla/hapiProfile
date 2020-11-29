# hapiProfile
性能监控，主线程卡顿，单帧渲染超时，帧率

 分支 ： marstDev


hapiProfile
===========
## 卡顿篇 ##

    

```javascript
hapi{
    androidBaseJarOnly true //系统库插桩
    jarTransform true    //三方jar
    isOpen true        //开启
    whiteJarList ":module_living," +   //模块
            ":lib_beautyui," +
            ":biz_gift,:base," +
            ":comp_im,:comp_living," +
            ":module_chat,:module_login," +
            ":module_main,module_user," +
            "com.pince.maven:lib-vmbaseframe," +  //三方库
            "com.pince.maven:lib-vmdialog," +
            "com.pince.maven:lib-lifecycleLiveTXAV"

    blackList  "androidx/core/graphics"
}
```


![此处输入图片的描述][1]


![此处输入图片的描述][2]
启动


         HapiMonitorPlugin.INSTANCE.init(this, new MonitorConfig());
        HapiMonitorPlugin.INSTANCE.startAllMonitor();

    MonitorConfig->默认配置


    配置参考MonitorConfig.kt
         /**
             * 单帧绘制超时
             */
            var frameCostIssues = 40
            /**
             * 主线程耗时卡顿
             */
            var blockCostIssue = 1700

            /**
             * 回调 （默认回调 log
             */
            var issureCallBack:IssureCallBack = object : IssureCallBack {
                override fun onIssure(issure: Issure) {
                    Log.d("IssureCallBack","msg  ${issure.msg}  availMemory ${issure.availMemory} " +
                            "totalMemory ${issure.totalMemory}   foregroundPageName  ${issure.foregroundPageName}  cpuRate" +
                            "${issure.cpuRate}"

                    )
                    NotificationIssure.send(issure)
                    issure.methodBeats?.forEachIndexed { index, methodBeat ->
                        Log.d("IssureCallBack", "top  ${index}  ${methodBeat.sign}  cost ${methodBeat.cost}")
                    }
                }

            }




## 内存篇 ##

    MemoryTranceManager.INSTANCE.init(this);
    MemoryTranceManager.INSTANCE.startMemoryProfile(1); //采集分钟间隔


    ##

**上报内容**
  1  //当内存触顶时

          "javaMax ${javaMax} \n" +
               "javaUsedMemory ${javaUsedMemory} \n" +
               "触顶率 ${touchRate} \n" +
               "gc次数 ${gcCount} \n" +

              "bitmapSize  ${bitmapSize}\n" + //bitmap占用

              "dumpFilePatch ${dumpFilePatch} \n" //hprof下载 可用用mat打开


  2 图片超宽监控： 支持8.0以下


     var bitmapWidth = 0
    var bitmapHeight =0
    var stack = ""


  [1]: http://git.7guoyouxi.com/android_repo/hapiProfile/blob/master/app/a.jpg
  [2]: http://git.7guoyouxi.com/android_repo/hapiProfile/blob/master/app/b.jpg
