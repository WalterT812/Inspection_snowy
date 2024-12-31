package vip.xiaonuo.inspection.modular.translate.service;

import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;

import java.util.List;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description
 **/
public interface InsuVoiceDialogService extends IService<InsuVoiceDialog> {
    /**
     * 获取对话内容
     *
     * @param insuVoiceId 录音ID
     * @return 对话列表
     */
    List<InsuVoiceDialog> getDialogs(Integer insuVoiceId);
}
