package vip.xiaonuo.inspection.modular.translate.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 翻译参数
 */
@Data
public class TranslateParam {

    /**
     * 应用ID
     */
    @Schema(description = "应用ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "appid不能为空")
    private String appid;

    /**
     * Token
     */
    @Schema(description = "Token", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "token不能为空")
    private String token;

    /**
     * 集群
     */
    @Schema(description = "集群", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "cluster不能为空")
    private String cluster;

    /**
     * 服务URL
     */
    @Schema(description = "服务URL", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "serviceUrl不能为空")
    private String serviceUrl;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "uid不能为空")
    private String uid;

    /**
     * 语音文件格式，默认大多为MP3格式
     */
    @Schema(description = "语音文件格式，默认值为MP3", defaultValue = "MP3")
    private String fileFormat = "MP3";

    /**
     * 语音文件链接地址
     */
    @Schema(description = "语音文件链接地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "voiceUrl不能为空")
    private String voiceUrl;

    /**
     * 是否实时返回结果
     */
    @Schema(description = "是否实时返回结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "realTimeReturn不能为空")
    private Boolean realTimeReturn;
}