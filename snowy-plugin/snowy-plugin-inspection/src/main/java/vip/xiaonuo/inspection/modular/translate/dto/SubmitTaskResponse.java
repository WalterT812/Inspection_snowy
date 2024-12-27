package vip.xiaonuo.inspection.modular.translate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @Description 提交任务的响应 DTO
 * @Author tang
 * @Date 2024/12/20
 */
@Data
public class SubmitTaskResponse {
    private Resp resp;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Resp {
        private int code;
        private String id;
        private String msg;
    }
}
