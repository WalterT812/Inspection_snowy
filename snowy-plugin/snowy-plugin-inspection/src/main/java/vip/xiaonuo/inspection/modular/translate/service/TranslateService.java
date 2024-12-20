package vip.xiaonuo.inspection.modular.translate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskResponse;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;

/**
 *
 * @author tanghaoyu
 * @date 2024/12/12
 * @description 语音翻译服务接口
 **/
public interface TranslateService extends IService<InsuVoiceRecord> {

    /**
     * 根据任务ID查询语音转文字任务的结果
     *
     * @param insuVoiceId 任务的ID
     * @return 解析后的服务端响应结果，以Map形式返回（可根据实际进一步处理转换为具体业务对象等）
     */
    QueryTaskResponse queryTaskResult(Integer insuVoiceId);

    /**
     * 根据 INSU_VOICE_ID 提交语音转文字任务
     *
     * @param insuVoiceId 录音 ID
     * @return 提交任务后服务端返回的任务 ID
     */
    SubmitTaskResponse submitTaskByInsuVoiceId(Integer insuVoiceId);
}