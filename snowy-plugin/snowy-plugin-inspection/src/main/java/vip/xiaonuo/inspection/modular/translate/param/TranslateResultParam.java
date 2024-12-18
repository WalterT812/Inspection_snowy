package vip.xiaonuo.inspection.modular.translate.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    /* TASK_ID */
    @Schema(description = "TASK_ID")
    @NotBlank(message = "TASK_ID 不能为空")
    private String taskId;
}
