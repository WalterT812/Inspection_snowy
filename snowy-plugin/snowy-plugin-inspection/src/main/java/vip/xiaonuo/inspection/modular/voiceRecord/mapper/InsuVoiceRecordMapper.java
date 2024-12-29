/*
 * Copyright [2022] [https://www.xiaonuo.vip]
 *
 * Snowy采用APACHE LICENSE 2.0开源协议，您在使用过程中，需要注意以下几点：
 *
 * 1.请不要删除和修改根目录下的LICENSE文件。
 * 2.请不要删除和修改Snowy源码头部的版权声明。
 * 3.本项目代码可免费商业使用，商业使用请保留源码和相关描述文件的项目出处，作者声明等。
 * 4.分发源码时候，请注明软件出处 https://www.xiaonuo.vip
 * 5.不可二次分发开源参与同类竞品，如有想法可联系团队xiaonuobase@qq.com商议合作。
 * 6.若您的项目无法满足以上几点，需要更多功能代码，获取Snowy商业授权许可，请在官网购买授权，地址为 https://www.xiaonuo.vip
 */
package vip.xiaonuo.inspection.modular.voiceRecord.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 存储录音Mapper接口
 *
 * @author tanghaoyu
 * @date 2024/12/13 12:08
 **/
public interface InsuVoiceRecordMapper extends BaseMapper<InsuVoiceRecord> {
    // 查询最大 INSU_VOICE_ID
    Integer selectMaxInsuVoiceId();

    /**
     * 根据 insuVoiceId 更新 IS_QUERY 字段为 1
     *
     * @param insuVoiceId 录音 ID
     * @return 更新的行数
     */
    Integer updateIsQueryByInsuVoiceId(Integer insuVoiceId);

    /**
     * 根据ID列表删除录音记录
     */
    void deleteByIds(@Param("voiceIds") List<Integer> voiceIds);
}
