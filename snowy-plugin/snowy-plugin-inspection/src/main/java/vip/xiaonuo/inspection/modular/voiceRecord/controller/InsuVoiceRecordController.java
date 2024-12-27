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
package vip.xiaonuo.inspection.modular.voiceRecord.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import vip.xiaonuo.common.annotation.CommonLog;
import vip.xiaonuo.common.pojo.CommonResult;
import vip.xiaonuo.inspection.modular.translate.service.TranslateService;
import vip.xiaonuo.inspection.modular.voiceRecord.entity.InsuVoiceRecord;
import vip.xiaonuo.inspection.modular.voiceRecord.param.*;
import vip.xiaonuo.inspection.modular.voiceRecord.service.InsuVoiceRecordService;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;


import java.util.List;

/**
 * 存储录音控制器
 *
 * @author tanghaoyu
 * @date  2024/12/13 12:08
 */
@Tag(name = "存储录音控制器")
@RestController
@RequestMapping("/inspection/voiceRecord")
@Validated
public class InsuVoiceRecordController {

    @Resource
    private InsuVoiceRecordService insuVoiceRecordService;
    @Resource
    private TranslateService translateService;

    /**
     * 获取存储录音分页
     *
     * @author tanghaoyu
     * @date  2024/12/13 12:08
     */
    @Operation(summary = "获取存储录音分页")
    @GetMapping("/page")
    public CommonResult<Page<InsuVoiceRecord>> page(InsuVoiceRecordPageParam insuVoiceRecordPageParam) {
        return CommonResult.data(insuVoiceRecordService.page(insuVoiceRecordPageParam));
    }

    /**
     * 添加存储录音
     *
     * @author tanghaoyu
     * @date  2024/12/13 12:08
     */
    @Operation(summary = "添加存储录音")
    @CommonLog("添加存储录音")
    @PostMapping("/add")
    public CommonResult<String> add(@RequestBody @Valid InsuVoiceRecordAddParam insuVoiceRecordAddParam) {
        insuVoiceRecordService.add(insuVoiceRecordAddParam);
        return CommonResult.ok();
    }

    /**
     * 编辑存储录音
     *
     * @author tanghaoyu
     * @date  2024/12/13 12:08
     */
    @Operation(summary = "编辑存储录音")
    @CommonLog("编辑存储录音")
    @PostMapping("/edit")
    public CommonResult<String> edit(@RequestBody @Valid InsuVoiceRecordEditParam insuVoiceRecordEditParam) {
        insuVoiceRecordService.edit(insuVoiceRecordEditParam);
        return CommonResult.ok();
    }

    /**
     * 删除存储录音
     *
     * @author tanghaoyu
     * @date  2024/12/13 12:08
     */
    @Operation(summary = "删除存储录音")
    @CommonLog("删除存储录音")
    @PostMapping("/delete")
    public CommonResult<String> delete(@RequestBody @Valid @NotEmpty(message = "集合不能为空")
                                           List<InsuVoiceRecordIdParam> insuVoiceRecordIdParamList) {
        insuVoiceRecordService.delete(insuVoiceRecordIdParamList);
        return CommonResult.ok();
    }

    /**
     * 获取存储录音详情
     *
     * @author tanghaoyu
     * @date  2024/12/13 12:08
     */
    @Operation(summary = "获取存储录音详情")
//    @SaCheckPermission("/inspection/voiceRecord/detail")
    @GetMapping("/detail")
    public CommonResult<InsuVoiceRecord> detail(@Valid InsuVoiceRecordIdParam insuVoiceRecordIdParam) {
        return CommonResult.data(insuVoiceRecordService.detail(insuVoiceRecordIdParam));
    }

}
