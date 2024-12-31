package vip.xiaonuo.inspection.modular.translate.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceDialogMapper;
import vip.xiaonuo.inspection.modular.translate.service.InsuVoiceDialogService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.translate.service.TranslateDataService;

import java.util.List;

@Service
public class InsuVoiceDialogServiceImpl extends ServiceImpl<InsuVoiceDialogMapper, InsuVoiceDialog> implements InsuVoiceDialogService {
    
    @Autowired
    private TranslateDataService translateDataService;

    @Override
    public List<InsuVoiceDialog> getDialogs(Integer insuVoiceId) {
        // 检查录音记录是否存在
        InsuVoiceRecord record = translateDataService.getByInsuVoiceId(insuVoiceId);
        if (record == null) {
            throw new CommonException("录音记录不存在");
        }

        // 检查是否已翻译
        if (record.getIsTranslated() != 1) {
            throw new CommonException("该录音尚未翻译");
        }

        // 从数据库获取对话内容并按开始时间排序
        List<InsuVoiceDialog> dialogs = this.list(
            new LambdaQueryWrapper<InsuVoiceDialog>()
                .eq(InsuVoiceDialog::getInsuVoiceId, insuVoiceId)
                .orderByAsc(InsuVoiceDialog::getStartTime)
        );

        if (dialogs == null || dialogs.isEmpty()) {
            throw new CommonException("未找到对话内容");
        }

        return dialogs;
    }
}
