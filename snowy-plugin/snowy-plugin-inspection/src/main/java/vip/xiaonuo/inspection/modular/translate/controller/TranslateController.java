package vip.xiaonuo.inspection.modular.translate.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.inspection.modular.translate.param.TranslateParam;
import vip.xiaonuo.inspection.modular.translate.param.TranslateResultParam;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.param.TranslateVoiceParam;


import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

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

    private static final Logger logger = LoggerFactory.getLogger(TranslateController.class);

    @Resource
    private TranslateService translateService;

//    /**
//     * 通过传入的语音文件链接（voiceUrl）执行语音转文字操作提交任务，并返回任务ID
//     *
//     * @param voiceUrl 语音文件链接
//     * @return 包含提交任务后返回的任务ID的通用结果对象
//     */
//    @Operation(summary = "通过语音文件链接执行语音转文字操作提交任务，并返回任务ID")
//    @GetMapping("/submitTaskByUrl")
//    public CommonResult<String> submitTaskByUrl(@RequestParam("voiceUrl") @NotBlank String voiceUrl) {
//        logger.info("提交语音任务, 语音文件URL: {}", voiceUrl);
//        try {
//            TranslateParam translateParam = new TranslateParam();
//            translateParam.setVoiceUrl(voiceUrl);
//            String taskId = translateService.submitTask(translateParam);
//            logger.info("任务提交成功, 任务ID: {}", taskId);
//            return CommonResult.data(taskId);
//        } catch (Exception e) {
//            logger.error("任务提交失败", e);
//            return CommonResult.error("任务提交失败: " + e.getMessage());
//        }
//    }

    /**
     * 根据任务ID查询语音转文字任务的结果
     *
     * @param translateResultParam 任务的ID
     * @return 包含解析后的服务端响应结果的通用结果对象
     */
    @Operation(summary = "根据任务ID查询语音转文字任务的结果")
    @PostMapping("/queryTaskResult")
    public CommonResult<Map<String, Object>> queryTaskResult(@RequestBody TranslateResultParam translateResultParam) {
        Integer insuVoiceId = translateResultParam.getInsuVoiceId();
//        String taskId = translateResultParam.getTaskId();
        logger.info("查询任务结果, 录音Id: {}", insuVoiceId);
        try {
            Map<String, Object> queryResult = translateService.queryTaskResult(insuVoiceId);
            logger.info("任务查询成功, 结果: {}", queryResult);
            return CommonResult.data(queryResult);
        } catch (Exception e) {
            logger.error("查询任务失败", e);
            return CommonResult.error("查询失败: " + e.getMessage());
        }
    }

    /**
     * 提交语音翻译任务
     *
     * @param translateParam 录音ID
     * @return 包含提交任务后返回的任务ID的通用结果对象
     */
    @Operation(summary = "提交语音翻译任务")
    @CommonLog("提交语音翻译任务")
    @PostMapping("/submitTask")
    public CommonResult<HashMap<String, Object>> translateVoice(@RequestBody TranslateVoiceParam translateParam) {
        Integer insuVoiceId = translateParam.getInsuVoiceId();
        logger.info("提交语音翻译任务，录音ID: {}", insuVoiceId);
        try {
            // 调用 submitTaskByInsuVoiceId 获取 CommonResult 对象
            CommonResult<HashMap<String, Object>> result = translateService.submitTaskByInsuVoiceId(insuVoiceId);
            return result;
        } catch (Exception e) {
            logger.error("任务提交失败", e);
            return CommonResult.error("任务提交失败: " + e.getMessage());
        }
    }
}
