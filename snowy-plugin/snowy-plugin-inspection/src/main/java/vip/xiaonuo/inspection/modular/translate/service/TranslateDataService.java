package vip.xiaonuo.inspection.modular.translate.service;

import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceQueryResult;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;

/**
 * @author tanghaoyu
 * @date 2024/12/19
 * @description 与数据库交互，包括读取和保存翻译任务或查询结果
 **/
public interface TranslateDataService {
    void saveTaskId(Integer insuVoiceId, String taskId);
    boolean isResultAlreadySaved(Integer insuVoiceId);
    InsuVoiceQueryResult getSavedResult(Integer insuVoiceId);
    String getTaskIdByInsuVoiceId(Integer insuVoiceId);
    void saveQueryResult(Integer insuVoiceId, String taskId, String queryResult);
    InsuVoiceRecord getByInsuVoiceId(Integer insuVoiceId);
}
