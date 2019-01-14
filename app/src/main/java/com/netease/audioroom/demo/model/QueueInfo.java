package com.netease.audioroom.demo.model;

import android.support.annotation.Nullable;


import org.json.JSONException;
import org.json.JSONObject;

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
    private static final String INDEX_KEY = "index";

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
        this(null, INIT_STATUS);
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
        try {
            jsonObject.put(STATUS_KEY, status);
            jsonObject.put(INDEX_KEY, index);
            if (memberInfo != null) {
                jsonObject.put(MEMBER_KEY, memberInfo.toJson());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject;
    }


    @Override
    public String toString() {
        return toJson().toString();
    }

    public static QueueInfo fromJson(String json) {
        try {
            QueueInfo queueInfo = new QueueInfo();
            JSONObject jsonObject = new JSONObject(json);
            queueInfo.setStatus(jsonObject.optInt(STATUS_KEY, INIT_STATUS));
            queueInfo.setIndex(jsonObject.optInt(INDEX_KEY));
            JSONObject memberJson = jsonObject.getJSONObject(MEMBER_KEY);
            if (memberJson != null) {
                MemberInfo memberInfo = new MemberInfo(memberJson);
                queueInfo.setMemberInfo(memberInfo);
            }
            return queueInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
}
