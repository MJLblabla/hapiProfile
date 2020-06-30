package com.hapi.hapiplugin;

import com.android.build.gradle.internal.scope.GlobalScope;
import com.android.build.gradle.internal.scope.VariantScope;
import com.google.common.base.Joiner;

import org.gradle.api.Project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.android.builder.model.AndroidProject.FD_OUTPUTS;

public class MethodCollector {

    private String mappingOut="";
    public static String methodMappingPath = "";
    AtomicInteger methodId =  new AtomicInteger(0);
    private List<String>  methodSigs = new LinkedList<String>();

    public MethodCollector(Project project, VariantScope variantScope){

        GlobalScope globalScope = variantScope.getGlobalScope();
        mappingOut = Joiner.on(File.separatorChar).join(
                String.valueOf(globalScope.getBuildDir()),
                FD_OUTPUTS,
                "mapping",
                variantScope.getVariantConfiguration().getDirName()) + "/methodMapping.txt";
        System.out.println("mappingOut "+mappingOut);

    }

    public void addMethod(String methodSig){
        methodSigs.add(methodSig);
    }

    public void saveCollectedMethod(){

        PrintWriter pw = null;
        try {
            File methodMapFile = new File(mappingOut);
            FileOutputStream fileOutputStream = new FileOutputStream(methodMapFile, false);
            Writer w = new OutputStreamWriter(fileOutputStream, "UTF-8");
            pw = new PrintWriter(w);
            for (String traceMethod : methodSigs) {
                pw.println(traceMethod+""+methodId.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (pw != null) {
                pw.flush();
                pw.close();
            }
        }
    }

}
