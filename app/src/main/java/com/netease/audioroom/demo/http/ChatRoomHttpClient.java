package com.netease.audioroom.demo.http;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


import com.netease.audioroom.demo.cache.DemoCache;
import com.netease.audioroom.demo.model.DemoRoomInfo;
import com.netease.audioroom.demo.util.JsonUtil;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 网易云信Demo聊天室Http客户端。第三方开发者请连接自己的应用服务器。
 */
public class ChatRoomHttpClient {

    private static final String TAG = ChatRoomHttpClient.class.getSimpleName();

    // code
    private static final int RESULT_CODE_SUCCESS = 200;

    private static final String API_SERVER = "https://app.netease.im/api/chatroom/";
    // api
    private static final String API_NAME_MASTER_ENTRANCE = "hostEntrance";
    private static final String API_NAME_REQUEST_ADDRESS = "requestAddress";
    private static final String API_NAME_CHAT_ROOM_LIST = "homeList";

    // header
    private static final String HEADER_KEY_APP_KEY = "appkey";
    private static final String HEADER_KEY_CONTENT_TYPE = "Content-type";

    // result
    private static final String RESULT_KEY_ERROR_MSG = "errmsg";
    private static final String RESULT_KEY_RES = "res";
    private static final String RESULT_KEY_MSG = "msg";
    private static final String RESULT_KEY_ROOM_ID = "roomid";
    private static final String RESULT_KEY_AV_TYPE = "avType";
    private static final String RESULT_KEY_ORIENTATION = "orientation";
    private static final String RESULT_KEY_TOTAL = "total";
    private static final String RESULT_KEY_LIST = "list";
    private static final String RESULT_KEY_NAME = "name";
    private static final String RESULT_KEY_CREATOR = "creator";
    private static final String RESULT_KEY_STATUS = "status";
    private static final String RESULT_KEY_EXT = "ext";
    private static final String RESULT_KEY_BACKGROUND_URL = "backgrounurl";
    private static final String RESULT_KEY_ONLINE_USER_COUNT = "onlineusercount";


    private static final String RESULT_KEY_LIVE = "live";
    private static final String RESULT_KEY_PUSH_URL = "pushUrl";
    private static final String RESULT_KEY_PULL_URL = "rtmpPullUrl";

    // request
    private static final String REQUEST_USER_UID = "uid"; // 用户id
    private static final String REQUEST_ROOM_ID = "roomid"; // 直播间id
    private static final String REQUEST_ROOM_NAME = "name"; // 直播间名字
    private static final String REQUEST_AV_TYPE = "avType"; // 主播直播类型
    private static final String REQUEST_ORIENTATION = "orientation"; // 主播直播方向

    // param
    private static final String KEY_AUDIO = "AUDIO";
    private static final int PORTRAIT = 1;


    public static ChatRoomHttpClient getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private ChatRoomHttpClient() {
        NimHttpClient.getInstance().init(DemoCache.getContext());
    }

    /**
     * 主播创建直播间
     */
    public void masterEnterRoom(String account, String roomName, final ChatRoomHttpCallback<DemoRoomInfo> callback) {


        String url = API_SERVER + API_NAME_MASTER_ENTRANCE;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_UID).append("=").append(account).append("&")
                .append(REQUEST_ROOM_NAME).append("=").append(roomName).append("&")
                .append(REQUEST_AV_TYPE).append("=").append(KEY_AUDIO).append("&")
                .append(REQUEST_ORIENTATION).append("=").append(PORTRAIT);
        String bodyString = body.toString();

        NimHttpClient.getInstance().execute(url, headers, bodyString, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {

                if (callback == null) {
                    return;
                }

                if (code != 0) {
                    Log.e(TAG, "masterEnterRoom failed : code = " + code + ", errorMsg = " + errorMsg);
                    callback.onFailed(code, errorMsg);
                    return;
                }

                try {
                    JSONObject res = JSONObject.parseObject(response);
                    int resCode = res.getIntValue(RESULT_KEY_RES);

                    if (resCode == RESULT_CODE_SUCCESS) {
                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);
                        DemoRoomInfo param = null;
                        if (msg != null) {
                            param = new DemoRoomInfo();
                            param.setRoomId(msg.getString(RESULT_KEY_ROOM_ID));

                        }
                        callback.onSuccess(param);
                        return;
                    }


                    Log.e(TAG, "masterEnterRoom failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                    callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));

                } catch (JSONException e) {
                    Log.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

//    public void audienceEnterRoom(String account, String roomId, final ChatRoomHttpCallback<DemoRoomInfo> callback) {
//        String url = API_SERVER + API_NAME_REQUEST_ADDRESS;
//
//        Map<String, String> headers = new HashMap<>(2);
//        String appKey = readAppKey();
//        headers.put(HEADER_KEY_APP_KEY, appKey);
//        headers.put(HEADER_KEY_CONTENT_TYPE, "application/json; charset=utf-8");
//
//        JSONObject jsonObject = new JSONObject();
//        jsonObject.put(REQUEST_ROOM_ID, roomId);
//        jsonObject.put(REQUEST_USER_UID, account);
//
//        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
//            @Override
//            public void onResponse(String response, int code, String errorMsg) {
//
//                if (callback == null) {
//
//                    return;
//                }
//                if (code != 0) {
//                    Log.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + errorMsg);
//                    callback.onFailed(code, errorMsg);
//                    return;
//                }
//
//                try {
//
//                    JSONObject res = JSONObject.parseObject(response);
//
//                    int resCode = res.getIntValue(RESULT_KEY_RES);
//                    if (resCode == RESULT_CODE_SUCCESS) {
//                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);
//                        String url = "";
//                        String avType = "";
//                        int orientation = 1;
//
//                        if (msg != null) {
//                            JSONObject live = msg.getJSONObject(RESULT_KEY_LIVE);
//                            url = live.getString(RESULT_KEY_PULL_URL);
//                            avType = live.getString(RESULT_KEY_AV_TYPE);
//                            orientation = live.getIntValue(RESULT_KEY_ORIENTATION);
//                        }
//                        DemoRoomInfo enterRoomParam = new DemoRoomInfo();
//
//                        // reply
//                        callback.onSuccess(enterRoomParam);
//
//                        return;
//                    }
//
//                    Log.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
//                    callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));
//
//                } catch (JSONException e) {
//                    Log.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
//                    callback.onFailed(-1, e.getMessage());
//                }
//            }
//        });
//    }


    /**
     * 向网易云信Demo应用服务器请求聊天室列表
     */
    public void fetchChatRoomList(final ChatRoomHttpCallback<List<DemoRoomInfo>> callback) {

        String url = API_SERVER + API_NAME_CHAT_ROOM_LIST;

        Map<String, String> headers = new HashMap<>(1);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);

        NimHttpClient.getInstance().execute(url, headers, null, false, new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {
                if (callback == null) {
                    return;
                }

                if (code != 200) {
                    Log.e(TAG, "fetchChatRoomList failed : code = " + code);
                    callback.onFailed(code, null);
                    return;
                }

                try {
                    // ret 0
                    JSONObject res = JSONObject.parseObject(response);
                    // res 1
                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        // msg 1
                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);
                        List<DemoRoomInfo> demoRoomInfoList = null;
                        if (msg != null) {
                            // total 2
                            demoRoomInfoList = new ArrayList<>(msg.getIntValue(RESULT_KEY_TOTAL));

                            // list 2
                            JSONArray rooms = msg.getJSONArray(RESULT_KEY_LIST);
                            for (int i = 0; i < rooms.size(); i++) {
                                // room 3
                                JSONObject room = rooms.getJSONObject(i);
                                DemoRoomInfo demoRoomInfo = new DemoRoomInfo();
                                demoRoomInfo.setName(room.getString(RESULT_KEY_NAME));
                                demoRoomInfo.setCreator(room.getString(RESULT_KEY_CREATOR));
                                demoRoomInfo.setValidFlag(room.getIntValue(RESULT_KEY_STATUS));
                                demoRoomInfo.setExtension(JsonUtil.getMapFromJsonString(room.getString(RESULT_KEY_EXT)));
                                demoRoomInfo.setBackgroundUrl(room.getString(RESULT_KEY_BACKGROUND_URL));
                                demoRoomInfo.setRoomId(room.getString(RESULT_KEY_ROOM_ID));
                                demoRoomInfo.setOnlineUserCount(room.getIntValue(RESULT_KEY_ONLINE_USER_COUNT));
                                demoRoomInfoList.add(demoRoomInfo);
                            }
                        }
                        // reply
                        callback.onSuccess(demoRoomInfoList);
                        return;
                    }

                    // msg == null
                    callback.onFailed(resCode, null);

                } catch (JSONException e) {
                    callback.onFailed(-1, e.getMessage());
                } catch (Exception e) {
                    callback.onFailed(-2, e.getMessage());
                }
            }
        });
    }

    private String readAppKey() {
        try {
            ApplicationInfo appInfo = DemoCache
                    .getContext()
                    .getPackageManager()
                    .getApplicationInfo(DemoCache.getContext().getPackageName(), PackageManager.GET_META_DATA);
            if (appInfo != null) {
                return appInfo.metaData.getString("com.netease.nim.appKey");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private static class InstanceHolder {
        private static final ChatRoomHttpClient INSTANCE = new ChatRoomHttpClient();
    }


    public interface ChatRoomHttpCallback<T> {

        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }


}
