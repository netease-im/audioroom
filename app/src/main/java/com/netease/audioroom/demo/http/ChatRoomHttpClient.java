package com.netease.audioroom.demo.http;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;


import com.netease.audioroom.demo.cache.DemoCache;


import java.util.HashMap;
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

    private static final String RESULT_KEY_LIVE = "live";
    private static final String RESULT_KEY_PUSH_URL = "pushUrl";
    private static final String RESULT_KEY_PULL_URL = "rtmpPullUrl";

    // request
    private static final String REQUEST_USER_UID = "uid"; // 用户id
    private static final String REQUEST_ROOM_ID = "roomid"; // 直播间id
    private static final String REQUEST_ROOM_EXT = "ext"; // 直播间扩展字段
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
    public void masterEnterRoom(String account, String ext, final ChatRoomHttpCallback<EnterRoomParam> callback) {


        String url = API_SERVER + API_NAME_MASTER_ENTRANCE;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/x-www-form-urlencoded;charset=utf-8");

        StringBuilder body = new StringBuilder();
        body.append(REQUEST_USER_UID).append("=").append(account).append("&")
                .append(REQUEST_ROOM_EXT).append("=").append(ext).append("&")
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
                        EnterRoomParam param = new EnterRoomParam();
                        param.setRoomId(msg.getString(RESULT_KEY_ROOM_ID));

                        if (msg != null) {
                            JSONObject live = msg.getJSONObject(RESULT_KEY_LIVE);
                            param.setPushUrl(live.getString(RESULT_KEY_PUSH_URL));
                            param.setPullUrl(live.getString(RESULT_KEY_PULL_URL));
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

    public void audienceEnterRoom(String account, String roomId, final ChatRoomHttpCallback<EnterRoomParam> callback) {
        String url = API_SERVER + API_NAME_REQUEST_ADDRESS;

        Map<String, String> headers = new HashMap<>(2);
        String appKey = readAppKey();
        headers.put(HEADER_KEY_APP_KEY, appKey);
        headers.put(HEADER_KEY_CONTENT_TYPE, "application/json; charset=utf-8");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(REQUEST_ROOM_ID, roomId);
        jsonObject.put(REQUEST_USER_UID, account);

        NimHttpClient.getInstance().execute(url, headers, jsonObject.toString(), new NimHttpClient.NimHttpCallback() {
            @Override
            public void onResponse(String response, int code, String errorMsg) {

                if (callback == null) {

                    return;
                }
                if (code != 0) {
                    Log.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + errorMsg);
                    callback.onFailed(code, errorMsg);
                    return;
                }

                try {

                    JSONObject res = JSONObject.parseObject(response);

                    int resCode = res.getIntValue(RESULT_KEY_RES);
                    if (resCode == RESULT_CODE_SUCCESS) {
                        JSONObject msg = res.getJSONObject(RESULT_KEY_MSG);
                        String url = "";
                        String avType = "";
                        int orientation = 1;

                        if (msg != null) {
                            JSONObject live = msg.getJSONObject(RESULT_KEY_LIVE);
                            url = live.getString(RESULT_KEY_PULL_URL);
                            avType = live.getString(RESULT_KEY_AV_TYPE);
                            orientation = live.getIntValue(RESULT_KEY_ORIENTATION);
                        }
                        EnterRoomParam enterRoomParam = new EnterRoomParam();
                        enterRoomParam.setPullUrl(url);
                        enterRoomParam.setAvType(avType);
                        enterRoomParam.setOrientation(orientation);
                        // reply
                        callback.onSuccess(enterRoomParam);

                        return;
                    }

                    Log.e(TAG, "studentEnterRoom failed : code = " + code + ", errorMsg = " + res.getString(RESULT_KEY_ERROR_MSG));
                    callback.onFailed(resCode, res.getString(RESULT_KEY_ERROR_MSG));

                } catch (JSONException e) {
                    Log.e(TAG, "NimHttpClient onResponse on JSONException, e=" + e.getMessage());
                    callback.onFailed(-1, e.getMessage());
                }
            }
        });
    }

    private String readAppKey() {
        try {
            ApplicationInfo appInfo = DemoCache.getContext().getPackageManager()
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

    public class EnterRoomParam {

        /**
         * 创建房间成功返回的房间号
         */
        private String roomId;
        /**
         * 推流地址
         */
        private String pushUrl;
        /**
         * 拉流地址
         */
        private String pullUrl;


        private String avType;

        private int orientation;

        public String getRoomId() {
            return roomId;
        }

        public void setRoomId(String roomId) {
            this.roomId = roomId;
        }

        public String getPushUrl() {
            return pushUrl;
        }

        public void setPushUrl(String pushUrl) {
            this.pushUrl = pushUrl;
        }

        public String getPullUrl() {
            return pullUrl;
        }

        public void setPullUrl(String pullUrl) {
            this.pullUrl = pullUrl;
        }

        public String getAvType() {
            return avType;
        }

        public void setAvType(String avType) {
            this.avType = avType;
        }

        public int getOrientation() {
            return orientation;
        }

        public void setOrientation(int orientation) {
            this.orientation = orientation;
        }
    }


    public interface ChatRoomHttpCallback<T> {

        void onSuccess(T t);

        void onFailed(int code, String errorMsg);
    }


}
