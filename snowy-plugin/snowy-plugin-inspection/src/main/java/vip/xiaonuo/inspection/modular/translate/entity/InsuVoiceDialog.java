package vip.xiaonuo.inspection.modular.translate.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@TableName("INSU_VOICE_DIALOG")
public class InsuVoiceDialog {
    /**
     * 主键
     */
    @TableId
    @Schema(description = "主键")
    private Integer id;

    /**
     * 录音ID
     */
    @Schema(description = "录音ID")
    private Integer insuVoiceId;

    /**
     * 对话文本
     */
    @Schema(description = "对话文本")
    private String dialogText;

    /**
     * 角色
     */
    @Schema(description = "角色")
    private Integer role;

    /**
     * 开始时间
     */
    @Schema(description = "开始时间")
    private Integer startTime;

    /**
     * 结束时间
     */
    @Schema(description = "结束时间")
    private Integer endTime;

    /**
     * 对话文本编号
     */
    @Schema(description = "对话文本编号")
    private String dialogTextId;

//    /** 创建时间 */
//    @Schema(description = "创建时间")
//    private Date createTime;
//
//    /** 更新时间 */
//    @Schema(description = "更新时间")
//    private Date updateTime;
}
