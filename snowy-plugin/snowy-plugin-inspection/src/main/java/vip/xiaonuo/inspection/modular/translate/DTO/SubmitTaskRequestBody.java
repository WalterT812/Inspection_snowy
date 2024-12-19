package vip.xiaonuo.inspection.modular.translate.DTO;

import lombok.Getter;
import lombok.Setter;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tanghaoyu
 * @date 2024/12/18
 * @description
 **/
@Getter
@Setter
public class SubmitTaskRequestBody {
    private Map<String, Object> app;
    private Map<String, String> userInfo;
    private Map<String, Object> audioInfo;
    private Map<String, String> additions;

    public static SubmitTaskRequestBody build(TranslateParam translateParam, String appId, String token, String cluster, String uid) {
        SubmitTaskRequestBody requestBody = new SubmitTaskRequestBody();
        requestBody.setApp(buildCommonFields(appId, token, cluster));
        requestBody.setUserInfo(buildUserInfo(uid));
        requestBody.setAudioInfo(buildAudioInfo(translateParam));
        requestBody.setAdditions(buildAdditions());
        return requestBody;
    }

    private static Map<String, Object> buildCommonFields(String appId, String token, String cluster) {
        Map<String, Object> commonFields = new HashMap<>();
        commonFields.put("appid", appId);
        commonFields.put("token", token);
        commonFields.put("cluster", cluster);
        return commonFields;
    }

    private static Map<String, String> buildUserInfo(String uid) {
        Map<String, String> userInfo = new HashMap<>();
        userInfo.put("uid", uid);
        return userInfo;
    }

    private static Map<String, Object> buildAudioInfo(TranslateParam translateParam) {
        Map<String, Object> audioInfo = new HashMap<>();
        audioInfo.put("format", translateParam.getFileFormat());
        audioInfo.put("url", translateParam.getVoiceUrl());
        audioInfo.put("channel", 2);
        return audioInfo;
    }

    private static Map<String, String> buildAdditions() {
        Map<String, String> additions = new HashMap<>();
        additions.put("with_speaker_info", "True");
        return additions;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.putAll(this.getApp());
        result.putAll(this.getUserInfo());
        result.putAll(this.getAudioInfo());
        result.putAll(this.getAdditions());
        return result;
    }
}
