package com.hapi.aop.ui

import android.os.Parcel
import android.os.Parcelable
import com.hapi.hapiplugin.beat.Beat

class IssureWrap() : Parcelable {

    /**
     * 提示信息
     */
    var msg: String? = null


    /**
     * 可用内存
     */
    var availMemory: Long = 0

    /**
     *
     */
    var totalMemory //= DeviceUtil.getTotalMemory(HapiPlugin.context)
            : Long = 0

    /**
     * cup
     */
    var cpuRate //= DeviceUtil.getAppCpuRate()
            = 0.0

    /**
     * 页面名称
     */
    var foregroundPageName: String? = null

    /**
     * 函数调用链
     */
    var methodBeats: List<Beat>? = null

    constructor(parcel: Parcel) : this() {
        msg = parcel.readString()
        availMemory = parcel.readLong()
        totalMemory = parcel.readLong()
        cpuRate = parcel.readDouble()
        foregroundPageName = parcel.readString()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(msg)
        parcel.writeLong(availMemory)
        parcel.writeLong(totalMemory)
        parcel.writeDouble(cpuRate)
        parcel.writeString(foregroundPageName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<IssureWrap> {
        override fun createFromParcel(parcel: Parcel): IssureWrap {
            return IssureWrap(parcel)
        }

        override fun newArray(size: Int): Array<IssureWrap?> {
            return arrayOfNulls(size)
        }
    }
}