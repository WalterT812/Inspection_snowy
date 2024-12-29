package vip.xiaonuo.inspection.modular.inspection.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuInspectionRule;
import vip.xiaonuo.inspection.modular.inspection.mapper.InsuInspectionRuleMapper;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRulePageParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuInspectionRule;
import vip.xiaonuo.inspection.modular.inspection.mapper.InsuInspectionRuleMapper;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRulePageParam;
import vip.xiaonuo.inspection.modular.inspection.enums.CommonSortOrderEnum;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleAddParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleEditParam;
import cn.hutool.core.bean.BeanUtil;
import vip.xiaonuo.common.exception.CommonException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检规则服务实现类
 */
@Service
public class InspectionRuleServiceImpl extends ServiceImpl<InsuInspectionRuleMapper, InsuInspectionRule> 
    implements InspectionRuleService {

    @Override
    public List<InsuInspectionRule> getEnabledRules() {
        return this.list(new LambdaQueryWrapper<InsuInspectionRule>()
            .eq(InsuInspectionRule::getRuleStatus, true)
            .orderByAsc(InsuInspectionRule::getRuleCode));
    }

    @Override
    public List<InsuInspectionRule> getRulesByLevel(String level) {
        return this.list(new LambdaQueryWrapper<InsuInspectionRule>()
            .eq(InsuInspectionRule::getRuleLevel, level)
            .eq(InsuInspectionRule::getRuleStatus, true)
            .orderByAsc(InsuInspectionRule::getRuleCode));
    }

    @Override
    public String buildRulePrompt() {
        List<InsuInspectionRule> rules = getEnabledRules();
        return rules.stream()
            .map(rule -> rule.getRuleCode() + ":" + rule.getRuleDescription())
            .collect(Collectors.joining("\n"));
    }

    @Override
    public Page<InsuInspectionRule> page(InspectionRulePageParam param) {
        QueryWrapper<InsuInspectionRule> queryWrapper = new QueryWrapper<InsuInspectionRule>().checkSqlInjection();
        
        // 添加查询条件
        if (StrUtil.isNotBlank(param.getRuleCode())) {
            queryWrapper.like("RULE_CODE", param.getRuleCode());
        }
        if (StrUtil.isNotBlank(param.getRuleLevel())) {
            queryWrapper.eq("RULE_LEVEL", param.getRuleLevel());
        }
        
        // 检查是否有排序字段和排序顺序
        if (ObjectUtil.isAllNotEmpty(param.getSortField(), param.getSortOrder())) {
            CommonSortOrderEnum.validate(param.getSortOrder());
            queryWrapper.orderBy(true, param.getSortOrder().equals(CommonSortOrderEnum.ASC.getValue()),
                    StrUtil.toUnderlineCase(param.getSortField()));
        } else {
            // 默认按照规则编号升序排序
            queryWrapper.orderByAsc("RULE_CODE");
        }
        
        return this.page(new Page<>(param.getCurrent(), param.getSize()), queryWrapper);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void add(InspectionRuleAddParam param) {
        InsuInspectionRule rule = BeanUtil.toBean(param, InsuInspectionRule.class);
        this.save(rule);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void edit(InspectionRuleEditParam param) {
        InsuInspectionRule rule = this.getById(param.getId());
        if (rule == null) {
            throw new CommonException("规则不存在，id值为：{}", param.getId());
        }
        BeanUtil.copyProperties(param, rule);
        this.updateById(rule);
    }
} 