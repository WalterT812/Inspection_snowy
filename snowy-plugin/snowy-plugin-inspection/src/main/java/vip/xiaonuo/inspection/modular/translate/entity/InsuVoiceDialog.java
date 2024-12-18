package vip.xiaonuo.inspection.modular.translate.entity;


import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import vip.xiaonuo.common.pojo.CommonEntity;

@Getter
@Setter
@TableName("INSU_VOICE_DIALOG")
public class InsuVoiceDialog extends CommonEntity {
    /** 主键 */
    @TableId
    @Schema(description = "主键")
    private Integer id;

    /** 关联语音ID */
    @Schema(description = "关联语音ID")
    private Integer insuVoiceId;

    /** 对话文本内容 */
    @Schema(description = "对话文本内容")
    private String dialogText;

    /** 角色（可根据业务定义具体角色含义） */
    @Schema(description = "角色")
    private Integer role;

    /** 开始时间 */
    @Schema(description = "开始时间")
    private Integer startTime;

    /** 结束时间 */
    @Schema(description = "结束时间")
    private Integer endTime;

    /** 对话文本ID */
    @Schema(description = "对话文本ID")
    private String dialogTextId;

    /** 创建时间 */
    @Schema(description = "创建时间")
    private java.util.Date createTime;

    /** 更新时间 */
    @Schema(description = "更新时间")
    private java.util.Date updateTime;
}
