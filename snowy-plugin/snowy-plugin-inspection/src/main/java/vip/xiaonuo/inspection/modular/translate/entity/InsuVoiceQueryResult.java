package vip.xiaonuo.inspection.modular.translate.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author tanghaoyu
 * @date 2024/12/18
 * @description
 **/

@Getter
@Setter
@TableName("insu_voice_query_result")
public class InsuVoiceQueryResult {
    /** 主键，自动递增 */
    @TableId
    @Schema(description = "主键，自动递增")
    private Integer id;

    /** INSU_VOICE_ID */
    @Schema(description = "INSU_VOICE_ID")
    private Integer insuVoiceId;

    /** 任务Id */
    @Schema(description = "TASK_ID")
    private String taskId;

    /** 查询结果 */
    @Schema(description = "QUERY_RESULT")
    private String queryResult;
}
