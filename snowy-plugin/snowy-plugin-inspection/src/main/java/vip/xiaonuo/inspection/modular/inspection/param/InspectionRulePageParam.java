package vip.xiaonuo.inspection.modular.inspection.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 质检规则分页查询参数
 */
@Getter
@Setter
public class InspectionRulePageParam {
    
    /** 当前页 */
    @Schema(description = "当前页码")
    private Integer current;
    
    /** 每页条数 */
    @Schema(description = "每页条数")
    private Integer size;
    
    /** 排序字段 */
    @Schema(description = "排序字段，字段驼峰名称，如：ruleCode")
    private String sortField;
    
    /** 排序方式 */
    @Schema(description = "排序方式，升序：ASC；降序：DESC")
    private String sortOrder;
    
    /** 规则编号 */
    @Schema(description = "规则编号")
    private String ruleCode;
    
    /** 规则等级 */
    @Schema(description = "规则等级")
    private String ruleLevel;
} 