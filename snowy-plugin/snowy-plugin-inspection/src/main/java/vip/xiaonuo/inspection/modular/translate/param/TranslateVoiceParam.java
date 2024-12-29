package vip.xiaonuo.inspection.modular.translate.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tanghaoyu
 * @date 2024/12/16
 * @description 处理翻译任务
 **/
@Getter
@Setter
public class TranslateVoiceParam {
    /**
     * INSU_VOICE_ID
     */
    @Schema(description = "INSU_VOICE_ID")
    @NotNull(message = "INSU_VOICE_ID 不能为空")
    private Integer insuVoiceId;
}
