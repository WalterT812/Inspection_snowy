package vip.xiaonuo.inspection.modular.inspection.param;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class InspectionRuleEditParam {
    
    @Schema(description = "规则ID")
    @NotNull(message = "规则ID不能为空")
    private Integer id;
    
    @Schema(description = "规则编号")
    private String ruleCode;
    
    @Schema(description = "规则名称")
    private String ruleName;
    
    @Schema(description = "规则描述")
    private String ruleDescription;
    
    @Schema(description = "规则等级")
    private String ruleLevel;
    
    @Schema(description = "规则状态")
    private Boolean ruleStatus;
} 