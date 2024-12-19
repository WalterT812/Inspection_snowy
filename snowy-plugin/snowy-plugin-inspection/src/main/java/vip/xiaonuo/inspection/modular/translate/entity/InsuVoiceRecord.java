package vip.xiaonuo.inspection.modular.translate.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 存储录音实体
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
@Getter
@Setter
@TableName("insu_voice_record")
public class InsuVoiceRecord {

    /** 主键，自动递增 */
    @TableId
    @Schema(description = "主键，自动递增")
    private Integer id;

    /** INSU_VOICE_ID */
    @Schema(description = "INSU_VOICE_ID")
    private Integer insuVoiceId;

    /** 录音URL */
    @Schema(description = "录音文件的 URL")
    private String voiceUrl;

    /** 翻译情况 */
    @Schema(description = "翻译情况")
    private Integer isTranslated;

    /** 质检情况 */
    @Schema(description = "质检情况")
    private Integer isInspected;

    /** 查询情况 */
    @Schema(description = "查询情况")
    private Integer isQueried;

    /** 上传时间 */
    @Schema(description = "上传时间")
    private Date uploadTime;

    /** 翻译完成时间 */
    @Schema(description = "翻译完成时间")
    private Date translateTime;

    /** 任务Id */
    @Schema(description = "TASK_ID")
    private String taskId;

}
