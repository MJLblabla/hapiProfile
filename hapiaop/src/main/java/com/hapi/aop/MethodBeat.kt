package com.hapi.aop

import android.view.ViewParent
import com.alibaba.fastjson.annotation.JSONField

class MethodBeat {

    constructor()

    constructor(
        methodSign: String
    ) {
        this.methodSign = methodSign
    }

    var methodSign: String? = ""
    var cost: Int = 0



    @JSONField(serialize = false)
    var startTime = 0



}



