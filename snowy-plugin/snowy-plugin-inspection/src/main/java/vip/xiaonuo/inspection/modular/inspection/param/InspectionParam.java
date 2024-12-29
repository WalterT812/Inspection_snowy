package vip.xiaonuo.inspection.modular.inspection.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检请求参数类
 */
@Data
public class InspectionParam {
    /** 语音记录ID */
    @NotNull(message = "录音ID不能为空")
    @Schema(description = "录音ID")
    private Integer insuVoiceId;
    
    /** 对话内容 */
    @Schema(description = "对话内容")
    private String dialogContent;
} 