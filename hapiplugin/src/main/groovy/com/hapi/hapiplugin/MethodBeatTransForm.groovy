package com.hapi.hapiplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformOutputProvider
import javassist.ClassPool
import javassist.CtClass
import javassist.CtMethod
import org.gradle.api.Project

class MethodBeatTransForm extends AbsTransForm{


    Project mProject
    MethodBeatTransForm(Project project) {
        mProject = project

    }
    @Override
    void transformJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        BeatInject.injectJarCost(jarInput,mProject,outputProvider)
    }

    @Override
    void transformDirectoryInput(DirectoryInput directoryInput) {
        BeatInject.injectCost(directoryInput.file,mProject)
    }

    @Override
    void transformSingleFile(File inputFile, File destFile, String srcDirPath) {
        BeatInject.injectSingleCost(inputFile,srcDirPath,mProject)
    }

    /**
     * Returns the unique name of the transform.
     *
     * <p>This is associated with the type of work that the transform does. It does not have to be
     * unique per variant.
     */
    @Override
    String getName() {
        return "MethodBeatPlugin"
    }



    private static ClassPool sClassPool = ClassPool.getDefault()
    /**
     * 向目标类注入耗时计算代码,生成同名的代理方法，在代理方法中调用原方法计算耗时
     * @param baseClassPath 写回原路径
     * @param clazz
     */
    private static void inject(String baseClassPath, String clazz) {
        def ctClass = sClassPool.get(clazz)
        //解冻
        if (ctClass.isFrozen()) {
            ctClass.defrost()
        }
        ctClass.getDeclaredMethods().each { ctMethod ->
            //判断是否要处理
            if (ctMethod.hasAnnotation(MethodCost.class)) {
                println "before ${ctMethod.name}"
                //把原方法改名，生成一个同名的代理方法，添加耗时计算
                def name = ctMethod.name
                def newName = name + COST_SUFFIX
                println "after ${newName}"
                def body = generateBody(ctClass, ctMethod, newName)
                println "generateBody : ${body}"
                //原方法改名
                ctMethod.setName(newName)
                //生成代理方法
                def proxyMethod = CtNewMethod.make(ctMethod.modifiers, ctMethod.returnType, name, ctMethod.parameterTypes, ctMethod.exceptionTypes, body, ctClass)
                //把代理方法添加进来
                ctClass.addMethod(proxyMethod)
            }
        }
        ctClass.writeFile(baseClassPath)
        ctClass.detach()//释放
    }

    /**
     * 生成代理方法体，包含原方法的调用和耗时打印
     * @param ctClass
     * @param ctMethod
     * @param newName
     * @return
     */
    private static String generateBody(CtClass ctClass, CtMethod ctMethod, String newName){
        //方法返回类型
        def returnType = ctMethod.returnType.name
        println returnType
        //生产的方法返回值
        def methodResult = "${newName}(\$\$);"
        if (!"void".equals(returnType)){
            //处理返回值
            methodResult = "${returnType} result = "+ methodResult
        }
        println methodResult
        return "{long costStartTime = System.currentTimeMillis();" +
                //调用原方法 xxx$$Impl() $$表示方法接收的所有参数
                methodResult +
                "android.util.Log.e(\"METHOD_COST\", \"${ctClass.name}.${ctMethod.name}() 耗时：\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //处理一下返回值 void 类型不处理
                ("void".equals(returnType) ? "}" : "return result;}")

    }

}