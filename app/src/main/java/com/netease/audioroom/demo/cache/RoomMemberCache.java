package com.netease.audioroom.demo.cache;

import android.support.annotation.Nullable;

import com.netease.nimlib.sdk.NIMClient;
import com.netease.nimlib.sdk.RequestCallback;
import com.netease.nimlib.sdk.chatroom.ChatRoomService;
import com.netease.nimlib.sdk.chatroom.constant.MemberQueryType;
import com.netease.nimlib.sdk.chatroom.model.ChatRoomMember;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用来管理聊天室的成员缓存
 */
public class RoomMemberCache {

    private ConcurrentHashMap<String, ChatRoomMember> memberCache = new ConcurrentHashMap<>();


    public static RoomMemberCache getInstance() {

        return InstanceHolder.INSTANCE;
    }


    /**
     * 根据成员帐号获取成员信息
     */
    @Nullable
    public ChatRoomMember getMember(String account) {

        return memberCache.get(account);
    }


    /**
     * 更新或增加成员信息
     */
    public void addOrUpdateMember(ChatRoomMember member) {
        if (member == null) {
            return;
        }
        memberCache.put(member.getAccount(), member);
    }


    /**
     * 从服务端拉取成员信息
     */
    public void fetchMembers(String roomId, long time, int limit) {

        NIMClient.getService(ChatRoomService.class).fetchRoomMembers(roomId, MemberQueryType.GUEST, time, limit).setCallback(new RequestCallback<List<ChatRoomMember>>() {
            @Override
            public void onSuccess(List<ChatRoomMember> chatRoomMembers) {
                for (ChatRoomMember member : chatRoomMembers) {
                    addOrUpdateMember(member);
                }

            }

            @Override
            public void onFailed(int i) {

            }

            @Override
            public void onException(Throwable throwable) {

            }
        });

    }


    private RoomMemberCache() {

    }

    private static class InstanceHolder {

        private static final RoomMemberCache INSTANCE = new RoomMemberCache();
    }

}
