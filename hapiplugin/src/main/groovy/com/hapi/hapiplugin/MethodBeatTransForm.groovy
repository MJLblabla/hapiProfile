package com.hapi.hapiplugin

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInvocation
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

    @Override
     boolean  needTransform(){
        def hapi =  mProject.hapi
        def  isOpen = hapi.isOpen
        println("hapi ${hapi.isOpen} ${hapi.msg} ")
        mProject.build
        return isOpen && !isReleaseBuildType()
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