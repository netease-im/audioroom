package com.netease.audioroom.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

public class RequestMember implements Parcelable {
    QueueMember queueMember;
    int index;

    public RequestMember(QueueMember queueMember, int index) {
        this.queueMember = queueMember;
        this.index = index;
    }

    public QueueMember getQueueMember() {
        return queueMember;
    }

    public void setQueueMember(QueueMember queueMember) {
        this.queueMember = queueMember;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.queueMember);
        dest.writeInt(this.index);
    }

    protected RequestMember(Parcel in) {
        this.queueMember = (QueueMember) in.readSerializable();
        this.index = in.readInt();
    }

    public static final Parcelable.Creator<RequestMember> CREATOR = new Parcelable.Creator<RequestMember>() {
        @Override
        public RequestMember createFromParcel(Parcel source) {
            return new RequestMember(source);
        }

        @Override
        public RequestMember[] newArray(int size) {
            return new RequestMember[size];
        }
    };
}
