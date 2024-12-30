package vip.xiaonuo.inspection.modular.inspection.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.entity.InsuVoiceInspection;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionService;
import vip.xiaonuo.inspection.modular.inspection.service.InsuVoiceInspectionService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.InsuVoiceRecordPageParam;

@Tag(name = "质检控制器")
@RestController
@Validated
@RequestMapping("/inspection/quality")
public class InspectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(InspectionController.class);

    @Resource
    private InspectionService inspectionService;
    
    @Resource
    private InsuVoiceInspectionService insuVoiceInspectionService;

    @Operation(summary = "获取质检分页列表")
    @GetMapping("/page")
    public CommonResult<Page<InsuVoiceRecord>> page(InsuVoiceRecordPageParam param) {
        logger.info("获取质检分页列表，参数：{}", param);
        return CommonResult.data(inspectionService.page(param));
    }

    @Operation(summary = "提交质检任务")
    @CommonLog("提交质检任务")
    @PostMapping("/submit")
    public CommonResult<AuditResult> submitInspection(@RequestBody InspectionParam inspectionParam) {
        Integer insuVoiceId = inspectionParam.getInsuVoiceId();
        logger.info("提交质检任务，录音ID: {}", insuVoiceId);
        
        try {
            AuditResult result = inspectionService.performInspection(inspectionParam);
            return CommonResult.data(result);
        } catch (Exception e) {
            logger.error("质检任务提交失败", e);
            return CommonResult.error("质检任务提交失败: " + e.getMessage());
        }
    }

    @GetMapping("/result")
    @Operation(summary = "获取质检结果")
    public CommonResult<String> getInspectionResult(@RequestParam Integer insuVoiceId) {
        try {
            InsuVoiceInspection inspection = insuVoiceInspectionService.getOne(
                new QueryWrapper<InsuVoiceInspection>()
                    .eq("INSU_VOICE_ID", insuVoiceId)
                    .orderByDesc("INSPECTION_TIME")
                    .last("LIMIT 1")
            );
            
            if (inspection == null || inspection.getInspectionResult() == null) {
                return CommonResult.error("未找到质检结果");
            }
            
            return CommonResult.data(inspection.getInspectionResult());
        } catch (Exception e) {
            logger.error("获取质检结果失败", e);
            return CommonResult.error("获取质检结果失败: " + e.getMessage());
        }
    }
} 