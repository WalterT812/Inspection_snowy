package vip.xiaonuo.inspection.modular.translate.controller;

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
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.SubmitTaskResponse;
import vip.xiaonuo.inspection.modular.translate.param.TranslateResultParam;
import vip.xiaonuo.inspection.modular.translate.param.TranslateVoiceParam;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;

/**
 * 语音转文字控制器
 *
 * @author tang
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

    /**
     * 查询语音转文字任务的结果
     *
     * @param translateResultParam 任务的ID
     * @return 包含解析后的服务端响应结果的通用结果对象
     */
    @Operation(summary = "根据任务ID查询语音转文字任务的结果")
    @PostMapping("/query")
    public CommonResult<QueryTaskResponse> queryTaskResult(@RequestBody TranslateResultParam translateResultParam) {
        Integer insuVoiceId = translateResultParam.getInsuVoiceId();

        logger.info("查询任务结果, 录音Id: {}", insuVoiceId);
        try {
            QueryTaskResponse queryResult = translateService.queryTaskResult(insuVoiceId);
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
    @PostMapping("/submit")
    public CommonResult<SubmitTaskResponse> translateVoice(@RequestBody TranslateVoiceParam translateParam) {
        Integer insuVoiceId = translateParam.getInsuVoiceId();
        logger.info("提交语音翻译任务，录音ID: {}", insuVoiceId);
        try {
            // 调用 submitTaskByInsuVoiceId 获取 SubmitTaskResponse 对象
            SubmitTaskResponse result = translateService.submitTaskByInsuVoiceId(insuVoiceId);
            return CommonResult.data(result);
        } catch (Exception e) {
            logger.error("任务提交失败", e);
            return CommonResult.error("任务提交失败: " + e.getMessage());
        }
    }
}
