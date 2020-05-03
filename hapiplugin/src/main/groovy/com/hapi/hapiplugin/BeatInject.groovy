package com.hapi.hapiplugin


import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import javassist.CtNewMethod
import javassist.Modifier
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.compress.utils.IOUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project
import java.lang.String
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry


class BeatInject {


    static final ClassPool sClassPool = ClassPool.getDefault();

    static void injectJarCost(JarInput jarInput, Project project, TransformOutputProvider outputProvider) {
        println "injectJarCost ${jarInput.name}"

        //添加Android相关的类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())

        def jarName = jarInput.name
        def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())

        if (jarName.endsWith(".jar")) {
            jarName = jarName.substring(0, jarName.length() - 4)
        }

        JarFile jarFile = new JarFile(jarInput.file)
        Enumeration enumeration = jarFile.entries()
        File tmpFile = new File(jarInput.file.getParent() + File.separator + "classes_temp.jar")
        //避免上次的缓存被重复插入
        if (tmpFile.exists()) {
            tmpFile.delete()
        }

        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(tmpFile))

        //用于保存
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            String entryName = jarEntry.getName()
            ZipEntry zipEntry = new ZipEntry(entryName)
            InputStream inputStream = jarFile.getInputStream(jarEntry)

            if (checkStr(entryName)) {

                jarOutputStream.putNextEntry(zipEntry)
                //class文件处理
                println "@@@@@@@@@@@@@@@@@@@@ deal with jar class file " + entryName

                //entryName是class文件的全路径  把/替换成.  然后把后面的.class去掉
                entryName = entryName.replace("/", ".").substring(0, entryName.length() - 6)

                sClassPool.appendClassPath(jarInput.file.getAbsolutePath())
                sClassPool.appendClassPath("/home/mjl/Downloads/aop/hapiaop/build/intermediates/javac/debug/classes/")
                CtClass ctClass = sClassPool.getCtClass(entryName)

                if (ctClass.isFrozen()) {
                    // 如果冻结就解冻
                    ctClass.defrost()
                }

                def  error = false

                ctClass.getDeclaredMethods().each { ctMethod ->

                    try {
                        if(  !ctMethod.isEmpty() && !Modifier.isNative(ctMethod.getModifiers())){
                            def methodSign = ctMethod.getLongName().toString()
                            ctMethod.insertBefore("com.hapi.aop.MethodBeatMonitorJava.logS( \"${methodSign}\");")
                            ctMethod.insertAfter("com.hapi.aop.MethodBeatMonitorJava.logE( \"${methodSign}\");")
                        }
                        println " ctMethod ${ctMethod.getLongName()} 成功"
                    }catch(Exception e){
                        error = true
                        println " ctMethod ${ctMethod.getLongName()} 失败 ${e.toString()}"
                    }

                }
                jarOutputStream.write(ctClass.toBytecode())

            } else {
                jarOutputStream.putNextEntry(zipEntry)
                jarOutputStream.write(IOUtils.toByteArray(inputStream))
            }
            jarOutputStream.closeEntry()
        }
        //结束
        jarOutputStream.close()
        jarFile.close()

        //处理完输入文件之后，要把输出给下一个任务
        def dest = outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
        FileUtils.copyFile(tmpFile, dest)
        tmpFile.delete()
    }


    static void injectCost(File baseClassPath, Project project) {
        println "injectUtil ${baseClassPath.path}"

        //添加Android相关的类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())

        if (baseClassPath.isDirectory()) {
            //遍历文件获取类
            baseClassPath.eachFileRecurse { classFile ->
                //过滤掉一些生成的类
                if (check(classFile)) {
                    println "find class : ${classFile.path}"

                    //把类文件路径转成类名
                    def className = convertClass(baseClassPath.path, classFile.path)
                    println "className" + className

                    //注入代码
                    inject(baseClassPath.path, className)
                }
            }
        }
    }

    static void injectSingleCost(File file, String baseClassPath, Project project) {
        println "project.android.bootClasspath[0].toString()  ${project.android.bootClasspath[0].toString()}"
        //把类路径添加到classpool

        //添加Android相关的类
        sClassPool.appendClassPath(project.android.bootClasspath[0].toString())
        def classFile = file.path

        if (check(file)) {
            println "injectSingleCost find class : ${classFile}"

            //把类文件路径转成类名
            def className = convertClass(baseClassPath, classFile)
            println "injectSingleCost" + className
            //注入代码
            inject(baseClassPath, className)
        }
    }

    /**
     * 向目标类注入耗时计算代码,生成同名的代理方法，在代理方法中调用原方法计算耗时
     * @param baseClassPath 写回原路径
     * @param clazz
     */
    private static void inject(String baseClassPath, String clazz) {

        println "把类路径添加到classpool ${baseClassPath}  ${clazz}"
        sClassPool.appendClassPath(baseClassPath)
        sClassPool.appendClassPath("/home/mjl/Downloads/aop/hapiaop/build/intermediates/javac/debug/classes/")
        try {
            def ctClass = sClassPool.get(clazz)
            //解冻
            if (ctClass.isFrozen()) {
                ctClass.defrost()
            }
            ctClass.getDeclaredMethods().each { ctMethod ->
                println " ctMethod ${ctMethod.getLongName()}"
                if(  !ctMethod.isEmpty() && !Modifier.isNative(ctMethod.getModifiers())){
                    def methodSign = ctMethod.getLongName().toString()
                    ctMethod.insertBefore("com.hapi.aop.MethodBeatMonitorJava.logS( \"${methodSign}\");")
                    ctMethod.insertAfter("com.hapi.aop.MethodBeatMonitorJava.logE( \"${methodSign}\");")
                }

            }
            ctClass.writeFile(baseClassPath)
            ctClass.detach()//释放
        } catch (Exception e) {
            println "e 插桩 错误  ${e.toString()}"
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
        println returnType
        //生产的方法返回值
        def methodResult = "${newName}(\$\$);"
        if (!"void".equals(returnType)) {
            //处理返回值
            methodResult = "${returnType} result = " + methodResult
        }
        println methodResult
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


        return filePath.contains('.class') && !filePath.contains('R$') &&
                !filePath.contains('R.class') &&
                !filePath.startsWith('kotlinx/') &&
                !filePath.startsWith('kotlin/') &&
                !filePath.contains('com/hapi/aop/') &&
                !filePath.contains('BuildConfig.class')
    }


}