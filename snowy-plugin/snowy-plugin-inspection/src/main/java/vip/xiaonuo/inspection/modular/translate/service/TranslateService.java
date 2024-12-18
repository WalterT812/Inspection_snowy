package vip.xiaonuo.inspection.modular.translate.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tanghaoyu
 * @date 2024/12/12
 * @description 语音翻译服务接口
 **/
public interface TranslateService extends IService<InsuVoiceDialog> {

    /**
     * 执行语音转文字操作提交任务，并返回任务ID
     *
     * @param translateParam 语音转文字操作参数，包含语音文件链接等信息
     * @return 提交任务后服务端返回的任务ID
     */
    String submitTask(TranslateParam translateParam);

    /**
     * 根据任务ID查询语音转文字任务的结果
     *
     * @param taskId 任务的ID
     * @return 解析后的服务端响应结果，以Map形式返回（可根据实际进一步处理转换为具体业务对象等）
     */
    Map<String, Object> queryTaskResult(String taskId);

    /**
     * 根据 INSU_VOICE_ID 提交语音转文字任务
     *
     * @param insuVoiceId 录音 ID
     * @return 提交任务后服务端返回的任务 ID
     */
    CommonResult<HashMap<String, Object>> submitTaskByInsuVoiceId(Integer insuVoiceId);

    /**
     * 根据 INSU_VOICE_ID 获取 VOICE_URL
     *
     * @param insuVoiceId 录音 ID
     * @return 录音文件的 URL
     */
    InsuVoiceRecord getByInsuVoiceId(Integer insuVoiceId);
}