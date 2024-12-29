package vip.xiaonuo.inspection.modular.inspection.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.util.Date;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检规则实体类
 */
@Data
@TableName("INSU_INSPECTION_RULE")
@Schema(description = "质检规则实体")
public class InsuInspectionRule {
    
    /** 主键ID */
    @TableId
    @Schema(description = "主键")
    private Integer id;
    
    /** 规则编号 */
    @Schema(description = "规则编号")
    private String ruleCode;
    
    /** 规则名称 */
    @Schema(description = "规则名称")
    private String ruleName;
    
    /** 规则描述 */
    @Schema(description = "规则描述")
    private String ruleDescription;
    
    /** 规则等级 */
    @Schema(description = "规则等级：A+,A,B,C")
    private String ruleLevel;
    
    /** 规则状态 */
    @Schema(description = "规则状态：0-禁用，1-启用")
    private Boolean ruleStatus;
    
    /** 创建时间 */
    @Schema(description = "创建时间")
    private Date createTime;
    
    /** 更新时间 */
    @Schema(description = "更新时间")
    private Date updateTime;
} 