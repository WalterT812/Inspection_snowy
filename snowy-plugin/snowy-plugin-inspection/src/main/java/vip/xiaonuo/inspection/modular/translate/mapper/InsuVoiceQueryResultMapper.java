package vip.xiaonuo.inspection.modular.translate.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceQueryResult;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface InsuVoiceQueryResultMapper extends BaseMapper<InsuVoiceQueryResult> {

    /**
     * 根据录音ID列表删除查询结果
     */
    void deleteByInsuVoiceIds(@Param("voiceIds") List<Integer> voiceIds);
}