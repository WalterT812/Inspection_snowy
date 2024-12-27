package vip.xiaonuo.inspection.modular.voiceRecord.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollStreamUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vip.xiaonuo.common.enums.CommonSortOrderEnum;
import vip.xiaonuo.common.exception.CommonException;
import vip.xiaonuo.common.page.CommonPageRequest;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.mapper.InsuVoiceRecordMapper;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordAddParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordEditParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordIdParam;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;
import vip.xiaonuo.inspection.modular.voiceRecord.service.InsuVoiceRecordService;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceDialogMapper;
import vip.xiaonuo.inspection.modular.translate.mapper.InsuVoiceQueryResultMapper;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存储录音Service接口实现类
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 **/
@Service
public class InsuVoiceRecordServiceImpl extends ServiceImpl<InsuVoiceRecordMapper, InsuVoiceRecord> implements InsuVoiceRecordService {

    @Autowired
    private InsuVoiceDialogMapper insuVoiceDialogMapper;

    @Autowired
    private InsuVoiceQueryResultMapper insuVoiceQueryResultMapper;

    @Override
    public Page<InsuVoiceRecord> page(InsuVoiceRecordPageParam insuVoiceRecordPageParam) {
        QueryWrapper<InsuVoiceRecord> queryWrapper = new QueryWrapper<InsuVoiceRecord>().checkSqlInjection();
        // 检查是否有排序字段和排序顺序
        if(ObjectUtil.isAllNotEmpty(insuVoiceRecordPageParam.getSortField(), insuVoiceRecordPageParam.getSortOrder())) {
            CommonSortOrderEnum.validate(insuVoiceRecordPageParam.getSortOrder());

            // 判断排序字段是否是 INSU_VOICE_ID，如果是，则使用倒序排序
            if ("INSU_VOICE_ID".equalsIgnoreCase(insuVoiceRecordPageParam.getSortField())) {
                queryWrapper.orderByDesc("INSU_VOICE_ID");
            } else {
                queryWrapper.orderBy(true, insuVoiceRecordPageParam.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                        StrUtil.toUnderlineCase(insuVoiceRecordPageParam.getSortField()));
            }
        } else {
            // 默认按照 INSU_VOICE_ID 倒序排序
            queryWrapper.orderByDesc("INSU_VOICE_ID");
        }
        return this.page(CommonPageRequest.defaultPage(), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(InsuVoiceRecordAddParam insuVoiceRecordAddParam) {
        InsuVoiceRecord insuVoiceRecord = BeanUtil.toBean(insuVoiceRecordAddParam, InsuVoiceRecord.class);
        // 自动生成 INSU_VOICE_ID
        if (insuVoiceRecord.getInsuVoiceId() == null) {
            insuVoiceRecord.setInsuVoiceId(generateInsuVoiceId()); // 生成 INSU_VOICE_ID
        }

        // 如果上传时间为空，则设置为当前时间
        if (insuVoiceRecord.getUploadTime() == null) {
            insuVoiceRecord.setUploadTime(new Date());
        }

        // 保存数据
        this.save(insuVoiceRecord);
    }

    public Integer generateInsuVoiceId() {
        // 查询当前最大的 INSU_VOICE_ID
        Integer maxId = this.baseMapper.selectMaxInsuVoiceId();

        // 如果没有数据，返回 100000，否则返回 maxId+1
        if (maxId == null) {
            return 100000;
        }

        return maxId + 1;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(InsuVoiceRecordEditParam insuVoiceRecordEditParam) {
        InsuVoiceRecord insuVoiceRecord = this.queryEntity(insuVoiceRecordEditParam.getId());
        BeanUtil.copyProperties(insuVoiceRecordEditParam, insuVoiceRecord);
        this.updateById(insuVoiceRecord);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(List<InsuVoiceRecordIdParam> insuVoiceRecordIdParamList) {
        // 1. 获取要删除记录的ID列表
        List<Integer> ids = CollStreamUtil.toList(insuVoiceRecordIdParamList, InsuVoiceRecordIdParam::getId);
        
        // 2. 查询这些记录的 INSU_VOICE_ID
        List<InsuVoiceRecord> records = this.listByIds(ids);
        List<Integer> insuVoiceIds = records.stream()
                .map(InsuVoiceRecord::getInsuVoiceId)
                .collect(Collectors.toList());
        
        // 3. 删除对话记录（通过 INSU_VOICE_ID）
        insuVoiceDialogMapper.deleteByInsuVoiceIds(insuVoiceIds);
        
        // 4. 删除查询结果（通过 INSU_VOICE_ID）
        insuVoiceQueryResultMapper.deleteByInsuVoiceIds(insuVoiceIds);
        
        // 5. 删除录音记录（通过 ID）
        this.baseMapper.deleteByIds(ids);
    }

    @Override
    public InsuVoiceRecord detail(InsuVoiceRecordIdParam insuVoiceRecordIdParam) {
        return this.queryEntity(insuVoiceRecordIdParam.getId());
    }

    @Override
    public InsuVoiceRecord queryEntity(Integer id) {
        InsuVoiceRecord insuVoiceRecord = this.getById(id);
        if(ObjectUtil.isEmpty(insuVoiceRecord)) {
            throw new CommonException("存储录音不存在，id值为：{}", id);
        }
        return insuVoiceRecord;
    }
}
