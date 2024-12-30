package vip.xiaonuo.inspection.modular.translate.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
/**
 * @author tanghaoyu
 * @date 2024/12/18
 * @description 获取翻译结果
 **/
@Getter
@Setter
public class TranslateResultParam {
    /**
     * 对应 insu_voice_dialog 的 INSU_VOICE_ID
     */
    @Schema(description = "对应 insu_voice_dialog 的 INSU_VOICE_ID")
    private Integer insuVoiceId;
}
