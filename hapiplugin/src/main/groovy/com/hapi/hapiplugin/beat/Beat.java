package com.hapi.hapiplugin.beat;

import android.os.Parcel;
import android.os.Parcelable;

public class Beat implements Parcelable {
    public int cost;
    public String sign="";

    public Beat(){}
    public Beat(int cost, String sign) {
        this.cost = cost;
        this.sign = sign;
    }

    protected Beat(Parcel in) {
        cost = in.readInt();
        sign = in.readString();
    }

    public static final Creator<Beat> CREATOR = new Creator<Beat>() {
        @Override
        public Beat createFromParcel(Parcel in) {
            return new Beat(in);
        }

        @Override
        public Beat[] newArray(int size) {
            return new Beat[size];
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
        dest.writeInt(cost);
        dest.writeString(sig