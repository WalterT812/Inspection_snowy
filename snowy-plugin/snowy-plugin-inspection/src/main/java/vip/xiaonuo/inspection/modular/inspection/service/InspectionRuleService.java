package vip.xiaonuo.inspection.modular.inspection.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuInspectionRule;
import java.util.List;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRulePageParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleAddParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleEditParam;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检规则服务接口
 */
public interface InspectionRuleService extends IService<InsuInspectionRule> {
    
    /**
     * 获取所有启用的规则
     * @return 规则列表
     */
    List<InsuInspectionRule> getEnabledRules();

    /**
     * 根据规则等级获取规则
     * @param level 规则等级
     * @return 规则列表
     */
    List<InsuInspectionRule> getRulesByLevel(String level);

    /**
     * 构建规则提示文本
     * @return 规则提示文本
     */
    String buildRulePrompt();

    /**
     * 获取规则分页列表
     * @param param 分页参数
     * @return 分页结果
     */
    Page<InsuInspectionRule> page(InspectionRulePageParam param);

    void add(InspectionRuleAddParam param);
    void edit(InspectionRuleEditParam param);
} 