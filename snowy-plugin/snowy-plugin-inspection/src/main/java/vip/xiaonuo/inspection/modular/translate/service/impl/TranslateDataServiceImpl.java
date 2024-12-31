package vip.xiaonuo.inspection.modular.translate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.modular.translate.Util.LoggerUtil;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceQueryResult;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceQueryResultMapper;
import vip.xiaonuo.inspection.modular.translate.service.TranslateDataService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;
import vip.xiaonuo.inspection.modular.voiceRecord.service.InsuVoiceRecordService;

import java.util.Date;

/**
 * @Author tanghaoyu
 * @Date 2024/12/19
 * @Description 与数据库交互，包括读取和保存翻译任务或查询结果
 */
@Service
@Slf4j
public class TranslateDataServiceImpl implements TranslateDataService {

    @Autowired
    private InsuVoiceRecordMapper insuVoiceRecordMapper;

    @Autowired
    private InsuVoiceQueryResultMapper insuVoiceQueryResultMapper;

    @Autowired
    private InsuVoiceRecordService insuVoiceRecordService;

    /**
     * 保存任务 ID 到数据库
     *
     * @param insuVoiceId 录音 ID
     * @param taskId      任务 ID
     */
    @Override
    public void saveTaskId(Integer insuVoiceId, String taskId) {
        // 根据录音 ID 查询记录
        InsuVoiceRecord record = insuVoiceRecordMapper.selectOne(
                new QueryWrapper<InsuVoiceRecord>().eq("INSU_VOICE_ID", insuVoiceId));

        if (record == null) {
            LoggerUtil.handleException("录音记录未找到，无法保存任务 ID", null);
        }

        // 更新任务 ID
        record.setTaskId(taskId);
        record.setTranslateTime(new Date()); // 更新翻译时间

        boolean updated = insuVoiceRecordService.updateById(record);
        if (!updated) {
            LoggerUtil.handleException("保存任务 ID 失败", null);
        }
        LoggerUtil.logRequest("保存任务 ID", "任务 ID", taskId);
    }

    /**
     * 检查是否已保存结果
     *
     * @param insuVoiceId 录音 ID
     * @return true 如果已保存，false 否则
     */
    @Override
    public boolean isResultAlreadySaved(Integer insuVoiceId) {
        // 查询是否存在记录
        QueryWrapper<InsuVoiceQueryResult> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("insu_voice_id", insuVoiceId);
        InsuVoiceQueryResult result = insuVoiceQueryResultMapper.selectOne(queryWrapper);
        return result != null;
    }

    /**
     * 获取保存的查询结果
     *
     * @param insuVoiceId 录音 ID
     * @return 查询结果，如果不存在则返回 null
     */
    @Override
    public InsuVoiceQueryResult getSavedResult(Integer insuVoiceId) {
        return insuVoiceQueryResultMapper.selectOne(
                new QueryWrapper<InsuVoiceQueryResult>().eq("INSU_VOICE_ID", insuVoiceId));
    }

    /**
     * 通过INSU_VOICE_ID获取TASK_ID
     *
     * @param insuVoiceId
     * @return TaskId
     */
    @Override
    public String getTaskIdByInsuVoiceId(Integer insuVoiceId) {
        QueryWrapper<InsuVoiceRecord> wrapper = new QueryWrapper<>();
        wrapper.eq("INSU_VOICE_ID", insuVoiceId);
        InsuVoiceRecord record = insuVoiceRecordMapper.selectOne(wrapper);
        if (record != null) {
            return record.getTaskId();
        }
        return null;
    }

    /**
     * 将数据存入表中
     *
     * @param insuVoiceId
     * @param taskId
     * @param queryResult
     */
    @Override
    public void saveQueryResult(Integer insuVoiceId, String taskId, String queryResult) {
        InsuVoiceQueryResult result = new InsuVoiceQueryResult();
        result.setInsuVoiceId(insuVoiceId);
        result.setTaskId(taskId);
        result.setQueryResult(queryResult);
        LoggerUtil.logRequest("保存查询结果", "queryResult", queryResult);
        insuVoiceQueryResultMapper.insert(result);
    }

    /**
     * 根据录音ID获取录音记录
     *
     * @param insuVoiceId 录音 ID
     * @return 录音记录
     */
    @Override
    public InsuVoiceRecord getByInsuVoiceId(Integer insuVoiceId) {
        // 查询对应的录音记录
        QueryWrapper<InsuVoiceRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("INSU_VOICE_ID", insuVoiceId);

        InsuVoiceRecord insuVoiceRecord = insuVoiceRecordService.getOne(queryWrapper);

        if (insuVoiceRecord == null) {
            LoggerUtil.handleException("找不到对应的录音记录，INSU_VOICE_ID: " + insuVoiceId, null);
        }

        // 返回完整的录音记录
        return insuVoiceRecord;
    }

    /**
     * 根据 insuVoiceId 更新 IS_QUERY 字段为 1
     *
     * @param insuVoiceId 录音 ID
     * @return 是否更新成功
     */
    public boolean updateIsQueryByInsuVoiceId(Integer insuVoiceId) {
        int rows = insuVoiceRecordMapper.updateIsQueryByInsuVoiceId(insuVoiceId);
        return rows > 0;
    }
}
