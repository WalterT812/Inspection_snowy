package vip.xiaonuo.inspection.modular.inspection.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.inspection.dto.AuditResult;
import vip.xiaonuo.inspection.modular.inspection.param.InspectionParam;
import vip.xiaonuo.inspection.modular.inspection.service.InspectionService;

@Tag(name = "质检控制器")
@RestController
@Validated
@RequestMapping("/inspection/quality")
public class InspectionController {
    
    private static final Logger logger = LoggerFactory.getLogger(InspectionController.class);

    @Resource
    private InspectionService inspectionService;

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
} 