package vip.xiaonuo.inspection.modular.inspection.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuInspectionRule;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleAddParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleEditParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRulePageParam;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionRuleParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionRuleService;

import java.util.List;

/**
 * @author tanghaoyu
 * @date 2024/1/8
 * @description 质检规则控制器
 */
@Tag(name = "质检规则控制器")
@RestController
@Validated
@RequestMapping("/inspection/rule")
public class InspectionRuleController {

    @Resource
    private InspectionRuleService inspectionRuleService;

    /**
     * 获取规则列表
     */
    @Operation(summary = "获取规则列表")
    @GetMapping("/list")
    public CommonResult<List<InsuInspectionRule>> list() {
        return CommonResult.data(inspectionRuleService.getEnabledRules());
    }

    /**
     * 获取指定等级的规则
     */
    @Operation(summary = "获取指定等级的规则")
    @GetMapping("/listByLevel")
    public CommonResult<List<InsuInspectionRule>> listByLevel(@RequestParam String level) {
        return CommonResult.data(inspectionRuleService.getRulesByLevel(level));
    }

    /**
     * 更新规则状态
     */
    @CommonLog("更新规则状态")
    @Operation(summary = "更新规则状态")
    @PostMapping("/updateStatus")
    public CommonResult<String> updateStatus(@RequestBody @Valid InspectionRuleParam param) {
        InsuInspectionRule rule = inspectionRuleService.getById(param.getId());
        if (rule != null) {
            rule.setRuleStatus(param.getRuleStatus());
            inspectionRuleService.updateById(rule);
            return CommonResult.ok("更新成功");
        }
        return CommonResult.error("规则不存在");
    }

    /**
     * 更新规则
     */
    @CommonLog("更新规则")
    @Operation(summary = "更新规则")
    @PostMapping("/update")
    public CommonResult<String> update(@RequestBody @Valid InspectionRuleParam param) {
        InsuInspectionRule rule = inspectionRuleService.getById(param.getId());
        if (rule != null) {
            rule.setRuleName(param.getRuleName());
            rule.setRuleDescription(param.getRuleDescription());
            inspectionRuleService.updateById(rule);
            return CommonResult.ok("更新成功");
        }
        return CommonResult.error("规则不存在");
    }

    /**
     * 获取规则分页列表
     */
    @Operation(summary = "获取规则分页列表")
    @GetMapping("/page")
    public CommonResult<Page<InsuInspectionRule>> page(InspectionRulePageParam param) {
        return CommonResult.data(inspectionRuleService.page(param));
    }

    @Operation(summary = "添加规则")
    @CommonLog("添加规则")
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody @Valid InspectionRuleAddParam param) {
        inspectionRuleService.add(param);
        return CommonResult.ok();
    }

    @Operation(summary = "编辑规则")
    @CommonLog("编辑规则")
    @PostMapping("/edit")
    public CommonResult<String> edit(@RequestBody @Valid InspectionRuleEditParam param) {
        inspectionRuleService.edit(param);
        return CommonResult.ok();
    }
} 