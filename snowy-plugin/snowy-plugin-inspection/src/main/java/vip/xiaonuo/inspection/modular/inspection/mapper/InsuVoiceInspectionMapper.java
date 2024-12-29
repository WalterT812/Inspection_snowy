package vip.xiaonuo.inspection.modular.inspection.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuVoiceInspection;
import java.util.Date;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 语音质检记录数据访问接口
 */
@Mapper
public interface InsuVoiceInspectionMapper extends BaseMapper<InsuVoiceInspection> {
    /**
     * 更新质检状态
     * @param insuVoiceId 语音记录ID
     * @param inspectionTime 质检时间
     * @return 更新的记录数
     */
    @Update("UPDATE INSU_VOICE_RECORD SET IS_INSPECTED = 1, INSPECTION_TIME = #{inspectionTime} " +
            "WHERE INSU_VOICE_ID = #{insuVoiceId}")
    int updateInspectionStatus(@Param("insuVoiceId") Integer insuVoiceId, 
                             @Param("inspectionTime") Date inspectionTime);
} 