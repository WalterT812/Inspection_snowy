package vip.xiaonuo.inspection.modular.inspection.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 语音质检记录实体类
 */
@Data
@TableName("INSU_VOICE_INSPECTION")
public class InsuVoiceInspection {
    /** 主键ID */
    @TableId
    @Schema(description = "主键")
    private Integer id;
    
    /** 语音记录ID */
    @Schema(description = "语音记录ID")
    private Integer insuVoiceId;
    
    /** 质检结果JSON */
    @Schema(description = "质检结果")
    private String inspectionResult;
    
    /** 质检时间 */
    @Schema(description = "质检时间")
    private Date inspectionTime;
    
    /** 质检状态 */
    @Schema(description = "质检状态：0-未质检，1-已质检")
    private Integer inspectionStatus;
} 