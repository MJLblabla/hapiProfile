package com.hapi.hapiplugin


import com.android.build.gradle.BaseExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * 接收额外的输入，如是否需要注入代码
 * author : pxq
 * date : 19-9-25 下午10:24
 */
class HapiAppMonitorPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.extensions.create("hapi", Hapi)


            def android = project.extensions.getByType(BaseExtension)
            android.registerTransform(new MethodBeatTransForm(project), Collections.EMPTY_LIST)


    }


}
