package vip.xiaonuo.inspection.modular.inspection.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class InspectionRuleAddParam {
    
    @Schema(description = "规则编号")
    @NotBlank(message = "规则编号不能为空")
    private String ruleCode;
    
    @Schema(description = "规则名称")
    @NotBlank(message = "规则名称不能为空")
    private String ruleName;
    
    @Schema(description = "规则描述")
    private String ruleDescription;
    
    @Schema(description = "规则等级")
    private String ruleLevel;
    
    @Schema(description = "规则状态")
    private Boolean ruleStatus;
} 