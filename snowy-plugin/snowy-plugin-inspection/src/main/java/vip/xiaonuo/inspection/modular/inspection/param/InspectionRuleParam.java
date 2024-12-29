package vip.xiaonuo.inspection.modular.inspection.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检规则参数
 */
@Data
public class InspectionRuleParam {
    
    /** 规则ID */
    @Schema(description = "规则ID")
    @NotNull(message = "规则ID不能为空")
    private Integer id;
    
    /** 规则名称 */
    @Schema(description = "规则名称")
    private String ruleName;
    
    /** 规则描述 */
    @Schema(description = "规则描述")
    private String ruleDescription;
    
    /** 规则状态 */
    @Schema(description = "规则状态")
    private Boolean ruleStatus;
} 