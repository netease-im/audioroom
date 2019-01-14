package com.netease.audioroom.demo.custom;

import org.json.JSONObject;

/**
 * 解散聊天室的自定义消息
 */
public class CloseRoomAttach extends CustomAttachment {


    CloseRoomAttach(int type) {
        super(type);
    }

    @Override
    protected void parseData(JSONObject data) {

    }

    @Override
    protected JSONObject packData() {
        return null;
    }
}
