# hapiProfile
性能监控，主线程卡顿，单帧渲染超时，帧率




apply plugin: 'com.hapi.hapiplugin'
implementation "com.pince.maven:hapiAop:1.0.0"
  

hapiProfile
===========

插件
    
    
    apply plugin: 'com.hapi.hapiplugin'
    
      hapi{
        isOpen true //是否开启
        blackList  "androidx/core/graphics,com/alibaba/fastjson" //黑名单
    }


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


  [1]: https://github.com/MJLblabla/hapiProfile/blob/master/app/a.jpg
  [2]: https://github.com/MJLblabla/hapiProfile/blob/master/app/b.jpg
