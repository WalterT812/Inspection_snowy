package vip.xiaonuo.inspection.modular.translate.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TranslateParam {

    /** 语音文件格式，默认大多为MP3格式 */
    @Schema(description = "语音文件格式，默认值为MP3", defaultValue = "MP3")
    private String fileFormat = "MP3";

    /** 语音文件链接地址 */
    @Schema(description = "语音文件链接地址", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "voiceUrl不能为空")
    private String voiceUrl;

    /** 是否实时返回结果 */
    @Schema(description = "是否实时返回结果", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "realTimeReturn不能为空")
    private Boolean realTimeReturn;

}
