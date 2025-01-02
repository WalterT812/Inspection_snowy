package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.json.JSONUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.core.util.LoggerUtil;
import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.Utterance;
import vip.xiaonuo.inspection.modular.translate.service.QueryResultProcessor;
import vip.xiaonuo.inspection.modular.translate.service.RoleIdentificationService;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 处理查询结果，包括从翻译服务查询数据和格式化处理
 * @Author tang
 * @Date 2024/12/19
 */
@Service
public class QueryResultProcessorImpl implements QueryResultProcessor {

    @Autowired
    private RoleIdentificationService roleIdentificationService;

    /**
     * 处理查询结果，提取utterances中的内容，并去掉additions的内容
     *
     * @param queryResponse 原始查询响应对象
     * @return 处理后的Map对象，只包含utterances中的text字段
     */
    @Override
    public QueryTaskResponse processQueryResult(QueryResponse queryResponse) {
        QueryTaskResponse processedResult = new QueryTaskResponse();
        List<Utterance> utterancesList = new ArrayList<>();

        // 记录原始数据
        LoggerUtil.logRequest("处理查询结果", "原始数据", JSONUtil.toJsonStr(queryResponse));

        if (queryResponse != null && queryResponse.getResp() != null && queryResponse.getResp().getUtterances() != null) {
            // 获取坐席角色，如果识别失败则使用默认值
            String staffSpeaker = roleIdentificationService.identifyStaffSpeaker(queryResponse.getResp().getUtterances());
            
            // 如果角色识别失败，使用默认规则（例如第一个说话的是坐席）
            if (staffSpeaker == null) {
                LoggerUtil.logRequest("角色识别", "使用默认规则", "第一个说话的为坐席");
                staffSpeaker = "1";
            }
            
            LoggerUtil.logRequest("角色识别", "识别结果", String.format("坐席角色为 Speaker %s", staffSpeaker));
            
            for (Utterance utterance : queryResponse.getResp().getUtterances()) {
                Utterance newUtterance = new Utterance();
                String currentSpeaker = utterance.getAdditions() != null ? utterance.getAdditions().getSpeaker() : null;
                
                if (currentSpeaker != null && staffSpeaker != null) {
                    newUtterance.setSpeaker(currentSpeaker.equals(staffSpeaker) ? "1" : "2");
                } else {
                    newUtterance.setSpeaker(currentSpeaker);
                }
                
                newUtterance.setText(utterance.getText());
                newUtterance.setStartTime(utterance.getStartTime());
                newUtterance.setEndTime(utterance.getEndTime());
                newUtterance.setAdditions(null);

                utterancesList.add(newUtterance);
            }
            processedResult.setUtterances(utterancesList);
        } else {
            LoggerUtil.logRequest("处理查询结果", "resp 或 utterances 为 null", null);
        }

        // 记录处理后的数据
        LoggerUtil.logRequest("处理查询结果", "处理后数据", JSONUtil.toJsonStr(processedResult));
        return processedResult;
    }
}
