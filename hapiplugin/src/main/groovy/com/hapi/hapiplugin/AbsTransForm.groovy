package com.hapi.hapiplugin
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Status
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils;
import org.apache.commons.codec.digest.DigestUtils
import org.gradle.api.Project
import com.android.build.api.transform.Format


abstract class AbsTransForm extends Transform {


    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {
        return true
    }

    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation)

        boolean isIncremental = transformInvocation.isIncremental()

        //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
        TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()

        if (!isIncremental) {
            //不需要增量编译，先清除全部
            outputProvider.deleteAll()
        }

        transformInvocation.getInputs().each { TransformInput input ->
            input.jarInputs.each { JarInput jarInput ->
                //处理Jar
                processJarInputWithIncremental(jarInput, outputProvider, isIncremental)
//
//                //不处理jar文件
//                    // 重命名输出文件（同目录copyFile会冲突）
//                    def jarName = jarInput.name
//                    def md5Name = DigestUtils.md5Hex(jarInput.file.getAbsolutePath())
//                    if (jarName.endsWith(".jar")) {
//                        jarName = jarName.substring(0, jarName.length() - 4)
//                    }
//                    def dest = transformInvocation.outputProvider.getContentLocation(jarName + md5Name, jarInput.contentTypes, jarInput.scopes, Format.JAR)
//                    FileUtils.copyFile(jarInput.file, dest)
            }

            input.directoryInputs.each { DirectoryInput directoryInput ->
                //处理文件
                processDirectoryInputWithIncremental(directoryInput, outputProvider, isIncremental)
            }
        }
    }

    void processJarInputWithIncremental(JarInput jarInput, TransformOutputProvider outputProvider, boolean isIncremental) {
        File dest = outputProvider.getContentLocation(
                jarInput.getFile().getAbsolutePath(),
                jarInput.getContentTypes(),
                jarInput.getScopes(),
                Format.JAR)
        if (isIncremental) {
            //处理增量编译
            processJarInputWhenIncremental(jarInput, dest)
        } else {
            //不处理增量编译
            processJarInput(jarInput, dest)
        }
    }

    void processJarInput(JarInput jarInput, File dest) {
        transformJarInput(jarInput, dest)
    }

    void processJarInputWhenIncremental(JarInput jarInput, File dest) {
        switch (jarInput.status) {
            case Status.NOTCHANGED:
                break
            case Status.ADDED:
            case Status.CHANGED:
                //处理有变化的
                transformJarInputWhenIncremental(jarInput, dest, jarInput.status)
                break
            case Status.REMOVED:
                //移除Removed
                if (dest.exists()) {
                    FileUtils.forceDelete(dest)
                }
                break
        }
    }

    void transformJarInputWhenIncremental(JarInput jarInput, File dest, Status status) {
        if (status == Status.CHANGED) {
            //Changed的状态需要先删除之前的
            if (dest.exists()) {
                FileUtils.forceDelete(dest)
            }
        }
        //真正transform的地方
        transformJarInput(jarInput, dest)
    }

    abstract void transformJarInput(JarInput jarInput)

    abstract void transformDirectoryInput(DirectoryInput directoryInput)

    abstract void transformSingleFile(File inputFile, File destFile, String srcDirPath)

    void transformJarInput(JarInput jarInput, File dest) {
        transformJarInput(jarInput)
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(jarInput.getFile(), dest)
    }

    void processDirectoryInputWithIncremental(DirectoryInput directoryInput, TransformOutputProvider outputProvider, boolean isIncremental) {
        File dest = outputProvider.getContentLocation(
                directoryInput.getFile().getAbsolutePath(),
                directoryInput.getContentTypes(),
                directoryInput.getScopes(),
                Format.DIRECTORY)
        if (isIncremental) {
            //处理增量编译
            processDirectoryInputWhenIncremental(directoryInput, dest)
        } else {
            processDirectoryInput(directoryInput, dest)
        }
    }

    void processDirectoryInputWhenIncremental(DirectoryInput directoryInput, File dest) {
        FileUtils.forceMkdir(dest)
        String srcDirPath = directoryInput.getFile().getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
        fileStatusMap.each { Map.Entry<File, Status> entry ->
            File inputFile = entry.getKey()
            Status status = entry.getValue()
            String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
            File destFile = new File(destFilePath)
            switch (status) {
                case Status.NOTCHANGED:
                    break
                case Status.REMOVED:
                    if (destFile.exists()) {
                        FileUtils.forceDelete(destFile)
                    }
                    break
                case Status.ADDED:
                case Status.CHANGED:
                    FileUtils.touch(destFile)
                    transformSingleFile(inputFile, destFile, srcDirPath)
                    FileUtils.copyFile(inputFile, destFile);
                    break
            }
        }
    }

    void processDirectoryInput(DirectoryInput directoryInput, File dest) {
        transformDirectoryInput(directoryInput, dest)
    }

    void transformDirectoryInput(DirectoryInput directoryInput, File dest) {
        transformDirectoryInput(directoryInput)
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
    }


}