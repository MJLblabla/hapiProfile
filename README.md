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
    
    
    
耗时分析源码：
-------

  
 baseprogfile ->ActivityCollection负责收集activity栈
 lib_aopbeat -> MethodBeatMonitorJava 负责记录方法调用栈
当一个方法执行完毕记录方法点：

    public class Beat implements Serializable {
    public int cost;　//耗时
    public String sign="";　//方法签名

    public int id;　//方法id 

    public Beat(){}
    public Beat(int cost, String sign) {
        this.cost = cost;
        this.sign = sign;
    }

目前的方案没有使用线上分析，如果使用线上分析需要生成一份方法id表，然后在服务器根据id分析方法，线下分析直接用了方法签名。

MethodBeatMonitorJava　中　 private static LinkedList<Beat> beats = new LinkedList<Beat>();
为一次主循环的方法的方法点，
　
执行完毕一个方法后

      public static void addBeat(Beat beat) {
        methodDeep++;
        beats.addFirst(beat);
    }

主loop执行一次msg清空方法记录

    public static void dispatchMsgStart() {
        methodDeep = 0;
        beats.clear();
    }

怎么调用addBeat方法呢，手动么，当然不是如果手动调用每一个方法都得调用还是外部jar你没法修改，这时候插桩派上用处了，

hapiplugin模块：AbsTransForm:通用插桩基类负责增量编译并发编译处理和判断该字节码码需不要插桩
对于Transform不懂同学可以查看资料这里简单介绍，就是把编译阶段的class文件给你一次修改的机会，









子类MethodBeatTransForm.groovy复杂耗时打点　－>

      @Override
        void transformJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
            BeatInject.injectJarCost(jarInput,mProject,outputProvider)
        }
        
    
        @Override
        void transformSingleFile(String baseClassPath,File file) {
            BeatInject.injectFileCost( baseClassPath,file,mProject)
        }
        
拿到jar和class路径后调用　BeatInject.injectFileCost方法
BeatInject负责javassist方案插桩－>

      private static void initMethod(CtClass ctClass, String entryName) {
            ctClass.getDeclaredMethods().each { ctMethod ->
                if(!ctMethod.name.equals("invokeSuspend")){
                    try {
                        if (!ctMethod.isEmpty() && !Modifier.isNative(ctMethod.getModifiers())) {
                            def methodSign = ctMethod.getLongName().toString()
                            ctMethod.addLocalVariable("needBeat", CtClass.booleanType);
                            ctMethod.addLocalVariable("costStartTime", CtClass.longType);
                            ctMethod.addLocalVariable("diff", CtClass.intType);
                            ctMethod.insertBefore("" +
                                    "  needBeat=com.hapi.aopbeat.MethodBeatMonitorJava.checkDeep();" +
                                    "         costStartTime =  com.hapi.aopbeat.MethodBeatMonitorJava.getTime();" +
                                    "")
                            ctMethod.insertAfter("" +
                                    "  diff = (int) (com.hapi.aopbeat.MethodBeatMonitorJava.getTime()-costStartTime);" +
                                    "        if(needBeat && diff>com.hapi.aopbeat.MethodBeatMonitorJava.getMinCostFilter()){" +
                                    "            com.hapi.aopbeat.MethodBeatMonitorJava.addBeat(new com.hapi.aopbeat.Beat(diff,\"${methodSign}\"));" +
                                    "        }" +
                                    "")
                        }
                    } catch (Exception e) {
                        // e.printStackTrace()
                    }
                }
            }
        }

其中：

     ctMethod.insertBefore("" +
                                    "  needBeat=com.hapi.aopbeat.MethodBeatMonitorJava.checkDeep();" +
                                    "         costStartTime =  com.hapi.aopbeat.MethodBeatMonitorJava.getTime();" +
                                    "")
                            ctMethod.insertAfter("" +
                                    "  diff = (int) (com.hapi.aopbeat.MethodBeatMonitorJava.getTime()-costStartTime);" +
                                    "        if(needBeat && diff>com.hapi.aopbeat.MethodBeatMonitorJava.getMinCostFilter()){" +
                                    "            com.hapi.aopbeat.MethodBeatMonitorJava.addBeat(new com.hapi.aopbeat.Beat(diff,\"${methodSign}\"));" +
                                    "        }" +
                                    "")
                                    
匹配方法开始和方法结束，如果方法不是毫秒级执行就调用MethodBeatMonitorJava.addBeat
把当前记录下来，其中MethodBeatMonitorJava.getTime()　是系统维护的时间戳，每５毫秒走一次，避免频繁调用系统时间，所以耗时分析有０到5毫秒的误差
这样在一次loop中就能拿到调用栈了，虽然不是完成和顺序保证的栈，往下会给出顺序保证栈（和实现运行方法顺序一致）的方案对比


hapiBloc模块：

    /**
     * 卡顿检查
     */
    class BlockTracer : ITracer() {
    
        private val TAG = "BlockTracer"
        private var startTime = 0
        override fun loopStart() {
            super.loopStart()
            startTime = LoopTimer.time
        }
    
        override fun loopStop() {
            super.loopStop()
            val endTime = LoopTimer.time
            val timeCost = endTime - startTime
            if (timeCost > BlockTranceManager.mMonitorConfig.blockCostIssue) {
                MethodBeatMonitorJava.issue("主线程卡顿 $timeCost",timeCost)
            }
        }
    }


BlockTracer监听主loop，在开始计时结束时计算时间差如果时间差超过阈值就是卡顿。 MethodBeatMonitorJava.issue上报卡顿。 MethodBeatMonitorJava.mBeatAdapter 取出耗时堆栈，排序过滤有效方法取内存信息等等。得到完整数据。

耗时堆栈的方案：
１上述方案
２放入入口入方法栈，方法出口弹出方法栈。
方案１，２对比１不能获取完整的调用顺序，但是可以在上报时附加通过java系统的dump完整堆栈，２能保证正确的顺序，但是如果调用链比较深需要存储过多无用数据，游戏方法可能很短但是入口你并不知道要多久。




整体介绍的比较粗略，感兴趣建议查看源码。存在的优化点，生成方法id，上报服务器分析方法id表能节省大量内存，方法签名比较占用内存。


单帧耗时监控：
主loop卡顿监听loop开始结束，帧耗时则是监听帧开始与结束　

hapi模块 ：TraversalTracer.kt
　　　

      init {
          //  callbackQueueLock = reflectObject<Any>(choreographer, "mLock")
            callbackQueues = reflectObject<Array<Any>>(choreographer, "mCallbackQueues")
            addTraversalQueue = reflectChoreographerMethod(
                callbackQueues!![CALLBACK_TRAVERSAL],
                ADD_CALLBACK,
                Long::class.java,
                Any::class.java,
                Any::class.java
            )
            frameIntervalNanos = reflectObject<Long>(choreographer, "mFrameIntervalNanos")?:16666666
    
        }
TraversalTracer　初始化往choreographer 的mCallbackQueues反射添加一个头runnable这样就能监听帧开始了，因帧开始都是   choreographer的doframe开始的然后取出mCallbackQueues执行run。


     override fun loopStop() {
            super.loopStop()
            if(isTraversal){
                isTraversal = false
                if(isStart){
                    val timeSpan = LoopTimer.time - startTime
                    if(timeSpan >  BlockTranceManager.mMonitorConfig.frameCostIssues){
                        MethodBeatMonitorJava.issue("单帧耗时过大 ${timeSpan}",timeSpan)
                    }
                    addCallbackQueues()
                }
            }
        }
        
在loopStop判断是不是在帧监听是的话取出耗时判断间隔，结束后再次反射添加runnable监控。






  [1]: https://github.com/MJLblabla/hapiProfile/blob/master/app/a.jpg?raw=true
  [2]: https://github.com/MJLblabla/hapiProfile/blob/master/app/b.jpg?raw=true
