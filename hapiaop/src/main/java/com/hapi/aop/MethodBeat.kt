package com.hapi.aop

import android.view.ViewParent
import com.alibaba.fastjson.annotation.JSONField

class MethodBeat {

    constructor()

    constructor(
        methodSign: String,
        cost: Int,
        parent: MethodBeat? = null
    ) {
        this.methodSign = methodSign
        this.cost = cost
        this.parent = parent
    }

    var methodSign: String? = ""
    var cost: Int = 0

    @JSONField(serialize = false)
    var parent: MethodBeat? = null
    @JSONField(serialize = false)
    var startTime = 0
    val zChild: ArrayList<MethodBeat> = ArrayList<MethodBeat>()


}



