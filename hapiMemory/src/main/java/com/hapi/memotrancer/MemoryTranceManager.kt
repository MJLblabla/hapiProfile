package com.hapi.memotrancer

import android.app.Application
import android.graphics.Bitmap
import android.os.Build
import android.widget.ImageView
import com.hapi.baseprofile.ActivityCollection
import com.taobao.android.dexposed.DexposedBridge
import com.taobao.android.dexposed.XC_MethodHook
import java.lang.Exception

object  MemoryTranceManager {

    private val mMemoryProfile:MemoryProfile = MemoryProfile()

    fun init(application: Application){
        ActivityCollection.init(application)

        if (android.os.Build.VERSION.SDK_INT <= Build.VERSION_CODES.O) {
            try {
                // hook ImageView
                DexposedBridge.hookAllConstructors(ImageView::class.java, object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam?) {
                        super.beforeHookedMethod(param)
                        DexposedBridge.findAndHookMethod(ImageView::class.java,"setImageBitmap",Bitmap::class.java,ImageHook())
                    }
                })
            }catch (e:Exception){
                e.printStackTrace()
            }
        }
    }
    fun startMemoryProfile(timeMinuteSpan:Int){
        mMemoryProfile.start(timeMinuteSpan)
    }

    fun stopMemoryProfile(){
        mMemoryProfile.stop()
    }
    
}