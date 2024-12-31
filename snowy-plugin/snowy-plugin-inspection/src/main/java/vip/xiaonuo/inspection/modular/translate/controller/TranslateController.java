package vip.xiaonuo.inspection.modular.translate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.entity.InsuVoiceDialog;
import vip.xiaonuo.inspection.modular.translate.param.TranslateResultParam;
import vip.xiaonuo.inspection.modular.translate.service.InsuVoiceDialogService;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;

import java.util.List;

/**
 * 语音转文字控制器
 *
 * @author tanghaoyu
 * @date 2024/12/12
 */
@Tag(name = "语音转文字控制器")
@RestController
@Validated
@RequestMapping("/inspection/translate")
public class TranslateController {

    @Resource
    private TranslateService translateService;

    @Resource
    private InsuVoiceDialogService insuVoiceDialogService;


    @Operation(summary = "翻译并查询任务")
    @CommonLog("翻译并查询任务")
    @PostMapping("/submit")
    public CommonResult<QueryTaskResponse> translateAndQuery(@RequestBody TranslateResultParam translateResultParam) {
        return CommonResult.data(translateService.translate(translateResultParam.getInsuVoiceId()));
    }

    
    @Operation(summary = "获取对话内容")
    @CommonLog("获取对话内容")
    @GetMapping("/dialogs")
    public CommonResult<List<InsuVoiceDialog>> getDialogs(@RequestParam Integer insuVoiceId) {
        return CommonResult.data(insuVoiceDialogService.getDialogs(insuVoiceId));
    }
}
