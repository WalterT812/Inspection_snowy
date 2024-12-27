package vip.xiaonuo.inspection.modular.translate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;

import java.util.List;

@Mapper
public interface InsuVoiceDialogMapper extends BaseMapper<InsuVoiceDialog> {
    /**
     * 根据录音ID列表删除对话记录
     */
    void deleteByInsuVoiceIds(@Param("voiceIds") List<Integer> voiceIds);
}
