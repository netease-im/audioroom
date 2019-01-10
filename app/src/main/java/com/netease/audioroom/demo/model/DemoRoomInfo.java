package com.netease.audioroom.demo.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.netease.audioroom.demo.util.JsonUtil;

import java.util.Map;

/**
 * 聊天室信息
 */
public class DemoRoomInfo implements  Parcelable {

    private String roomId;       // roomId
    private String name;         // 聊天室名称

    private String creator;      // 聊天室创建者账号
    private int validFlag;       // 聊天室有效标记, 1:有效,0:无效
    private int onlineUserCount; // 当前在线用户数量
    private int mute;            // 聊天室禁言标记
    private Map<String, Object> extension;  // 第三方扩展字段, 长度4k
    private String backgroundUrl; // 聊天室背景图

    private int queueLevel; // 队列管理权限，如是否有权限提交他人key和信息到队列中。


    public DemoRoomInfo() {
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public int getValidFlag() {
        return validFlag;
    }

    public void setValidFlag(int validFlag) {
        this.validFlag = validFlag;
    }

    public int getOnlineUserCount() {
        return onlineUserCount;
    }

    public void setOnlineUserCount(int onlineUserCount) {
        this.onlineUserCount = onlineUserCount;
    }

    public int getMute() {
        return mute;
    }

    public void setMute(int mute) {
        this.mute = mute;
    }

    public Map<String, Object> getExtension() {
        return extension;
    }

    public void setExtension(Map<String, Object> extension) {
        this.extension = extension;
    }

    public String getBackgroundUrl() {
        return backgroundUrl;
    }

    public void setBackgroundUrl(String backgroundUrl) {
        this.backgroundUrl = backgroundUrl;
    }

    public int getQueueLevel() {
        return queueLevel;
    }

    public void setQueueLevel(int queueLevel) {
        this.queueLevel = queueLevel;
    }

    /**
     * ********************************** 序列化 **********************************
     */
    private DemoRoomInfo(Parcel in) {
        roomId = in.readString();
        name = in.readString();
        creator = in.readString();
        validFlag = in.readInt();
        onlineUserCount = in.readInt();
        setExtension(JsonUtil.getMapFromJsonString(in.readString()));
        mute = in.readInt();
        queueLevel = in.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(roomId);
        dest.writeString(name);
        dest.writeString(creator);
        dest.writeInt(validFlag);
        dest.writeInt(onlineUserCount);
        dest.writeString(JsonUtil.getJsonStringFromMap(extension));
        dest.writeInt(mute);
        dest.writeInt(queueLevel);
    }

    public static final Creator<DemoRoomInfo> CREATOR = new Creator<DemoRoomInfo>() {
        @Override
        public DemoRoomInfo createFromParcel(Parcel in) {
            return new DemoRoomInfo(in);
        }

        @Override
        public DemoRoomInfo[] newArray(int size) {
            return new DemoRoomInfo[size];
        }
    };


}
