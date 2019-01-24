package com.netease.audioroom.demo.base;

import com.netease.audioroom.demo.model.QueueInfo;
import com.netease.audioroom.demo.model.QueueMember;

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
     *
     * @param queueMember
     * @param index
     */
    void linkRequest(QueueMember queueMember, int index);


    /**
     * 有人取消了连麦请求
     */
    void linkRequestCancel();


    /**
     * 拒绝连麦
     *
     * @param queueMember
     */
    void rejectLink(QueueMember queueMember);


    /**
     * 同意连麦
     *
     * @param queueInfo
     */
    void acceptLink(QueueInfo queueInfo);


    /**
     * 抱麦（对方不可拒绝）
     */
    void invitedLink(QueueInfo queueInfo);

    /**
     * 踢人下麦
     */
    void removeLink(QueueInfo queueInfo);


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
    void mutedAudio(QueueInfo queueInfo);

    /**
     * 关闭麦位
     * 对于“空麦位”而言，可以关闭麦位，关闭后，该麦位不可以抱人上麦，观众也不可以申请上麦。
     * <p>
     * • 如果“空麦位”处于“屏蔽状态”，则同样可以关闭麦位，关闭后只要显示“关闭状态”即可，不需要显示“屏蔽状态”。关闭后的麦位，其屏蔽状态会被清空，再次打开麦位后，该麦位应该处于“未屏蔽状态”
     */
    void closeAudio(QueueInfo queueInfo);
}
