package vip.xiaonuo.inspection.modular.translate.dto;

import lombok.Data;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;

/**
 * @author tanghaoyu
 * @date 2024/12/18
 * @description
 **/
@Data
public class SubmitTaskRequestBody {
    private App app;
    private User user;
    private Audio audio;
    private Additions additions;

    @Data
    public static class App {
        private String appid;
        private String token;
        private String cluster;
    }

    @Data
    public static class User {
        private String uid;
    }

    @Data
    public static class Audio {
        private String format;
        private String url;
        private Integer channel;
    }

    @Data
    public static class Additions {
        private String use_itn;
        private String with_speaker_info;
    }

    /**
     * 构建请求体
     *
     * @param translateParam 翻译参数
     * @return 构建好的请求体对象
     */
    public static SubmitTaskRequestBody build(TranslateParam translateParam) {
        SubmitTaskRequestBody requestBody = new SubmitTaskRequestBody();

        App app = new App();
        app.setAppid(translateParam.getAppid());
        app.setToken(translateParam.getToken());
        app.setCluster(translateParam.getCluster());
        requestBody.setApp(app);

        User user = new User();
        user.setUid(translateParam.getUid());
        requestBody.setUser(user);

        Audio audio = new Audio();
        audio.setFormat(translateParam.getFileFormat());
        audio.setUrl(translateParam.getVoiceUrl());
        audio.setChannel(2); // 如果需要，可以从 TranslateParam 中获取
        requestBody.setAudio(audio);

        Additions additions = new Additions();
        additions.setUse_itn("False");
        additions.setWith_speaker_info("True");
        requestBody.setAdditions(additions);

        return requestBody;
    }
}