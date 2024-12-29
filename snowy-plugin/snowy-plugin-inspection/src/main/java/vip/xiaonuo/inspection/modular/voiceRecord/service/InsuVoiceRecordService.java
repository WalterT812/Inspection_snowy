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
package vip.xiaonuo.inspection.modular.voiceRecord.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordAddParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordEditParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordIdParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;

import java.util.List;

/**
 * 存储录音Service接口
 *
 * @author tanghaoyu
 * @date 2024/12/13 12:08
 **/
public interface InsuVoiceRecordService extends IService<InsuVoiceRecord> {

    /**
     * 获取存储录音分页
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     */
    Page<InsuVoiceRecord> page(InsuVoiceRecordPageParam insuVoiceRecordPageParam);

    /**
     * 添加存储录音
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     */
    void add(InsuVoiceRecordAddParam insuVoiceRecordAddParam);

    /**
     * 编辑存储录音
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     */
    void edit(InsuVoiceRecordEditParam insuVoiceRecordEditParam);

    /**
     * 删除存储录音
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     */
    void delete(List<InsuVoiceRecordIdParam> insuVoiceRecordIdParamList);

    /**
     * 获取存储录音详情
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     */
    InsuVoiceRecord detail(InsuVoiceRecordIdParam insuVoiceRecordIdParam);

    /**
     * 请求Entity
     *
     * @author tanghaoyu
     * @date 2024/12/13 12:08
     **/
    InsuVoiceRecord queryEntity(Integer id);
}
