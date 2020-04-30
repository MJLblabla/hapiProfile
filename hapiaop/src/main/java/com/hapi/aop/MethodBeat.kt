package com.hapi.aop

import android.view.ViewParent

class MethodBeat(val methodSign: String,val cost:Int,val parent: MethodBeat?=null, val child:ArrayList<MethodBeat> = ArrayList<MethodBeat>())


class MethodBeatTemp (val methodSign: String,val startTime :Int)
