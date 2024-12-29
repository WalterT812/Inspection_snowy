package vip.xiaonuo.inspection.modular.inspection.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 违规规则数据传输对象
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ViolationRule {
    /** 违规规则编号 */
    private String rule;
    
    /** 违规内容 */
    private String message;
    
    /** 违规描述 */
    private String describe;
} 