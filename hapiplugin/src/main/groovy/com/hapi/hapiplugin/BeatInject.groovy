package com.hapi.hapiplugin

import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider
import com.hapi.aopbeat.MethodBeatMonitorJava
import javassist.ClassClassPath
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.LoaderClassPath
import javassist.Modifier
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class BeatInject {


    static final ClassPool sClassPool = ClassPool.getDefault();
    static def methodBeatClass = null
    static def blackList = []
    static def MethodCollector methodCollector;


    static void injectJarCost(JarInput jarInput, Project project, TransformOutputProvider outputProvider) {
        ClassPool.cacheOpenedJarFile = false
        if (methodBeatClass == null) {
            methodBeatClass = MethodBeatMonitorJava.classLoader;//project.rootProject.projectDir.toString() + "/hapiaop/build/intermediates/javac/debug/classes/"
        }
        //添加Android相关的类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        sClassPool.insertClassPath(new ClassClassPath(MethodBeatMonitorJava.class));
        def jarName = jarInput.name
        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }

        JarFile jarFile = new JarFile(jarInput.file)
        Enumeration enumeration = jarFile.entries()
        File tmpFile = new File(jarInput.file.getParent() + File.separator +(Math.random()*100)+ "classes_temp.jar")

        //避免上次的缓存被重复插入
        if(tmpFile.exists()){
            FileUtils.forceDelete(tmpFile)
        }
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))
        sClassPool.insertClassPath(jarInput.file.getAbsolutePath())
        //用于保存
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream inputStream = jarFile.getInputStream(jarEntry)
            if (checkStr(entryName)) {

                jarOutputStream.putNextEntry(zipEntry)
                //class文件处理
                //entryName是class文件的全路径  把/替换成.  然后把后面的.class去掉
                entryName = entryName.replace("/", ".").substring(0, entryName.length() - 6)

               // sClassPool.removeCached2(entryName)
                CtClass ctClass = sClassPool.getCtClass(entryName)

                if (ctClass.isFrozen()) {
                    // 如果冻结就解冻
                    ctClass.defrost()
                }
                initMethod(ctClass,entryName)
                jarOutputStream.write(ctClass.toBytecode())
                try {
                    ctClass.writeFile()
                    ctClass.detach()
                } catch (Exception e) {
                    e.printStackTrace()
                }

            //    sClassPool.removeCache(ctClass.name)

            } else {
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            jarOutputStream.closeEntry()
        }
        //结束
        jarOutputStream.close()
        jarFile.close()

        File dest = outputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR)

        //处理完输入文件之后，要把输出给下一个任务
      //  def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)

        FileUtils.copyFile(tmpFile, dest)
        FileUtils.forceDelete(tmpFile)

    }

    private static void initMethod(CtClass ctClass, String entryName) {
        ctClass.getDeclaredMethods().each { ctMethod ->
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
                def str= "entryName ${entryName} ctMethod ${ctMethod.getLongName()}   成功"
                println(str)
            } catch (Exception e) {
               // e.printStackTrace()
                def str= "entryName ${entryName} ctMethod ${ctMethod.getLongName()} 失败 ${e.toString()}"
                 println(str)
            }

        }

    }




    static void injectFileCost(String baseClassPath,File file, Project project){
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        sClassPool.insertClassPath(new ClassClassPath(MethodBeatMonitorJava.class));
        //过滤掉一些生成的类
        if (check(file)) {

            //把类文件路径转成类名
            def className = convertClass(baseClassPath, file.path)

            //注入代码
            inject(sClassPool,baseClassPath, className)
        }
    }

    /**
     * 向目标类注入耗时计算代码,生成同名的代理方法，在代理方法中调用原方法计算耗时
     * @param baseClassPath 写回原路径
     * @param clazz
     */
    private static void inject(ClassPool sClassPool, String baseClassPath, String clazz) {

        sClassPool.appendClassPath(baseClassPath)
        try {
            def ctClass = sClassPool.get(clazz)

            //解冻
            if (ctClass.isFrozen()) {
                ctClass.defrost()
            }

            initMethod(ctClass,clazz)
            ctClass.writeFile(baseClassPath)
            ctClass.detach()//释放
        } catch (Exception e) {

        }

    }

    /**
     * 生成代理方法体，包含原方法的调用和耗时打印
     * @param ctClass
     * @param ctMethod
     * @param newName
     * @return
     */
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        //生产的方法返回值
        def methodResult = "${newName}(\$\$);"
        if (!"void".equals(returnType)) {
            //处理返回值
            methodResult = "${returnType} result = " + methodResult
        }
        return "{long costStartTime = System.currentTimeMillis();" +
                //调用原方法 xxx$$Impl() $$表示方法接收的所有参数
                methodResult +
                "android.util.Log.e(\"METHOD_COST\", \"${ctClass.name}.${ctMethod.name}() 耗时：\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //处理一下返回值 void 类型不处理
                ("void".equals(returnType) ? "}" : "return result;}")

    }

    private static String convertClass(String baseClassPath, String classPath) {
        //截取包之后的路径
        def packagePath = classPath.substring(baseClassPath.length() + 1)
        //把 / 替换成.
        def clazz = packagePath.replaceAll("/", ".")
        //去掉.class 扩展名

        return clazz.substring(0, packagePath.length() - ".class".length())
    }


    //过滤掉一些生成的类
    private static boolean check(File file) {
        if (file.isDirectory()) {
            return false
        }

        def filePath = file.path

        return checkStr(filePath)
    }
    //过滤掉一些生成的类
    private static boolean checkStr(String filePath) {

        blackList.each{ it ->

            if(filePath.contains(it)){
                return false
            }
        }

        return filePath.contains('.class') && !filePath.contains('R$') &&
                !filePath.contains('R.class') &&
                !filePath.startsWith('kotlinx/') &&
                !filePath.startsWith('kotlin/') &&
                !filePath.contains('com/hapi/aop/') &&
                !filePath.contains('BuildConfig.class')
    }




}