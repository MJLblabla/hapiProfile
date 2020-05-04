package com.hapi.aop;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

public class Issure implements Parcelable {
    public Issure(){

    }
    /**
     * 提示信息
     */
    public String msg;


    /**
     * 可用内存
     */
    public  long availMemory ;
    /**
     *
     */
    public  long totalMemory ;//= DeviceUtil.getTotalMemory(HapiPlugin.context)

    /**
     * cup
     */
    public  double cpuRate ;//= DeviceUtil.getAppCpuRate()

    /**
     * 页面名称
     */
    public String foregroundPageName;
    /**
     * 函数调用链
     */
    public List<Beat> methodBeats;

    protected Issure(Parcel in) {
        msg = in.readString();
        availMemory = in.readLong();
        totalMemory = in.readLong();
        cpuRate = in.readDouble();
        foregroundPageName = in.readString();
        methodBeats = in.createTypedArrayList(Beat.CREATOR);
    }

    public static final Creator<Issure> CREATOR = new Creator<Issure>() {
        @Override
        public Issure createFromParcel(Parcel in) {
            return new Issure(in);
        }

        @Override
        public Issure[] newArray(int size) {
            return new Issure[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable
     * instance's marshaled representation. For example, if the object will
     * include a file descriptor in the output of {@link #writeToParcel(Parcel, int)},
     * the return value of this method must include the
     * {@link #CONTENTS_FILE_DESCRIPTOR} bit.
     *
     * @return a bitmask indicating the set of special object types marshaled
     * by this Parcelable object instance.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(msg);
        dest.writeLong(availMemory);
        dest.writeLong(totalMemory);
        dest.writeDouble(cpuRate);
        dest.writeString(foregroundPageName);
        dest.writeTypedList(methodBeats);
    }
}
