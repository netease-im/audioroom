package com.netease.audioroom.demo.model;

import android.support.annotation.Nullable;

import com.alibaba.fastjson.JSONObject;

import java.io.Serializable;

/**
 * 麦位信息，也就是聊天室队列元素信息
 */
public class QueueInfo implements Serializable {

    /**
     * 麦位初始化状态
     */
    public static final int INIT_STATUS = 1;

    /**
     * 麦位上没人，但是被主播屏蔽
     */
    public static final int FORBID_STATUS = 2;


    /**
     * 麦位上有申请连麦人
     */
    public static final int APPLYING_STATUS = 3;


    /**
     * 麦位上有人，且能正常发言
     */
    public static final int NORMAL_STATUS = 4;

    /**
     * 麦位上有人，但是语音被屏蔽
     */
    public static final int BE_MUTED_AUDIO_STATUS = 5;


    /**
     * 麦位上有人，但是他关闭了自己的语音
     */
    public static final int CLOSE_SELF_AUDIO_STATUS = 6;


    private static final String STATUS_KEY = "status";
    private static final String MEMBER_KEY = "member";

    private MemberInfo memberInfo;
    private int status = INIT_STATUS;
    private int index = 0;


    public QueueInfo(@Nullable MemberInfo memberInfo, int status) {
        this.memberInfo = memberInfo;
        this.status = status;
    }

    public QueueInfo(@Nullable MemberInfo memberInfo) {
        this(memberInfo, INIT_STATUS);
    }

    public QueueInfo() {

    }

    public QueueInfo(JSONObject jsonObject) {
        fromJson(jsonObject);
    }

    private void fromJson(JSONObject jsonObject) {
        JSONObject memberJson = jsonObject.getJSONObject(MEMBER_KEY);
        if (memberJson != null) {
            memberInfo = new MemberInfo(memberJson);
        }
        status = jsonObject.getIntValue(STATUS_KEY);
    }

    public int getIndex() {
        return index;
    }

    @Nullable
    public MemberInfo getMemberInfo() {
        return memberInfo;
    }

    public int getStatus() {
        return status;
    }


    public void setMemberInfo(MemberInfo memberInfo) {
        this.memberInfo = memberInfo;
    }

    public void setStatus(int status) {
        this.status = status;
    }


    public void setIndex(int index) {
        this.index = index;
    }

    public JSONObject toJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(STATUS_KEY, status);

        if (memberInfo != null) {
            jsonObject.put(MEMBER_KEY, memberInfo.toJson());
        }
        return jsonObject;
    }


    @Override
    public String toString() {
        return toJson().toString();
    }
}
