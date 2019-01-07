package com.netease.audioroom.demo.base;

/**
 * 主播行为
 */
public interface IAudioLive {


    /**
     * 进入聊天室
     *
     * @param roomId 聊天室ID
     */
    void enterChatRoom(String roomId);


    /**
     * 有人请求连麦
     */
    void linkRequest();


    /**
     * 有人取消了连麦请求
     */
    void linkRequestCancel();


    /**
     * 拒绝连麦
     */
    void rejectLink();


    /**
     * 同意连麦
     */
    void acceptLink();


    /**
     * 抱麦（对方不可拒绝）
     */
    void invitedLink();

    /**
     * 踢人下麦
     */
    void removeLink();


    /**
     * 有人主动下麦
     */
    void linkCanceled();


    /**
     * 禁言某人
     */
    void mutedText();


    /**
     * 禁言所有人
     */
    void muteTextAll();


    /**
     * 屏蔽某个麦位的语音
     */
    void mutedAudio();
}
