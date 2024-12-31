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
     * 翻译并查询
     *
     * @param insuVoiceId
     * @return QueryTaskResponse
     */
    QueryTaskResponse translate(Integer insuVoiceId);

}