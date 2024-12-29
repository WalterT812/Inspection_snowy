/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.inspection.modular.voiceRecord.param;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * 存储录音添加参数
 *
 * @author tanghaoyu
 * @date 2024/12/13 12:08
 **/
@Getter
@Setter
public class InsuVoiceRecordAddParam {

    /**
     * INSU_VOICE_ID
     */
    @Schema(description = "INSU_VOICE_ID")
    private Integer insuVoiceId;

    /**
     * 录音文件的 URL
     */
    @Schema(description = "录音URL")
    private String voiceUrl;

    /**
     * 翻译状态
     */
    @Schema(description = "翻译状态")
    private Integer isTranslated;

    /**
     * 质检状态
     */
    @Schema(description = "质检状态")
    private Integer isInspected;

    /**
     * 查询状态
     */
    @Schema(description = "查询状态")
    private Integer isQueried;

    /**
     * 上传时间
     */
    @Schema(description = "上传时间")
    private Date uploadTime;

    /**
     * 翻译完成时间
     */
    @Schema(description = "翻译完成时间")
    private Date translateTime;

    /**
     * 质检完成时间
     */
    @Schema(description = "翻译完成时间")
    private Date inspectionTime;

    /**
     * 任务Id
     */
    @Schema(description = "TASK_ID")
    private String taskId;
}
