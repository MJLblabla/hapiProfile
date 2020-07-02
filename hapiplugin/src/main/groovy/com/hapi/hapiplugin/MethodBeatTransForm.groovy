package com.hapi.hapiplugin


import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import org.gradle.api.Project

class MethodBeatTransForm extends AbsTransForm{

    def androidBaseJarOnly  = true

    String[] whiteJarArray
    Project mProject
    private Boolean jarTransform = true
    MethodBeatTransForm(Project project) {
        mProject = project
//        println "variant  before"
//        mProject.afterEvaluate {
//            println "variant  afterEvaluate"
//            def android = mProject.extensions.android
//            android.applicationVariants.all { variant ->
//                println "variant"+ variant
//
//                BeatInject.methodCollector = new MethodCollector(mProject,variant.getVariantData().getScope())
//            }
//        }

    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)
    }


    @Override
    void transformJarInput(JarInput jarInput, TransformOutputProvider outputProvider) {
        BeatInject.injectJarCost(jarInput,mProject,outputProvider)
    }





    @Override
    void transformSingleFile(String baseClassPath,File file) {
        println("transformSingleFile" +file.absolutePath)
        BeatInject.injectFileCost( baseClassPath,file,mProject)
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

    @Override
     boolean  needTransform(){
        def hapi =  mProject.hapi
        def  isOpen = hapi.isOpen


        String black = hapi.blackList
        BeatInject.blackList = black.split(",")
        BeatInject.blackList.each{ it ->
            println("blackList each ${it} ")
        }

        androidBaseJarOnly = hapi.androidBaseJarOnly
        jarTransform = hapi.jarTransform
        whiteJarArray =hapi.whiteJarList.split(",")

        whiteJarArray.each{ it ->
            println("whiteJarArray each ${it} ")
        }


        return isOpen && !isReleaseBuildType()
    }

    @Override
    boolean needJarTransform(JarInput jarInput ) {
        if(jarTransform){
          if(androidBaseJarOnly){

              if(jarInput.name.startsWith("androidx")
                      ||jarInput.name.startsWith("android")
                  ||jarInput.name.startsWith("com.google.android")
              ){
                  println(jarInput.name +"  androidBaseJarOnly "+ androidBaseJarOnly + " "+ true)
                  return true
              }
          }
            boolean hasWhite = false


            if(whiteJarArray!=null&&whiteJarArray.length!=0){
                hasWhite = true
                for(String s:whiteJarArray){

                  def  eq=  jarInput.name.startsWith(s) || jarInput.name == s
                    println("jarInput.name "+jarInput.name + "    s "+s+" "+eq)
                    if( eq){
                     return true
                    }
                }
            }

            if(androidBaseJarOnly||hasWhite){
                return false
            }
            println(jarInput.name +"   处理")
              return true
        }else {
            return false
        }
    }

    boolean isReleaseBuildType(){
        for(String s : mProject.gradle.startParameter.taskNames) {
            if (s.contains("Release") | s.contains("release")) {
                return true
            }
        }
        return false
    }

}