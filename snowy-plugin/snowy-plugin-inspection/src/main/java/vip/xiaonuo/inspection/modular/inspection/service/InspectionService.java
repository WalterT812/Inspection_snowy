package vip.xiaonuo.inspection.modular.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检服务接口
 */
public interface InspectionService extends IService<InsuVoiceRecord> {
    /**
     * 执行质检
     *
     * @param param 质检参数
     * @return 质检结果
     */
    AuditResult performInspection(InspectionParam param);

    /**
     * 保存质检结果
     *
     * @param insuVoiceId 语音记录ID
     * @param result      质检结果JSON字符串
     */
    void saveInspectionResult(Integer insuVoiceId, String result);

    /**
     * 获取分页列表
     */
    Page<InsuVoiceRecord> page(InsuVoiceRecordPageParam param);
} 