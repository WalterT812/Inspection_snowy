package vip.xiaonuo.inspection.modular.inspection.service;

import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检服务接口
 */
public interface InspectionService {
    /**
     * 执行质检
     * @param param 质检参数
     * @return 质检结果
     */
    AuditResult performInspection(InspectionParam param);

    /**
     * 保存质检结果
     * @param insuVoiceId 语音记录ID
     * @param result 质检结果JSON字符串
     */
    void saveInspectionResult(Integer insuVoiceId, String result);
} 