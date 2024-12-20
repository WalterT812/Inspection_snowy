package vip.xiaonuo.inspection.modular.translate.service.impl;

import cn.hutool.json.JSONUtil;
import org.springframework.stereotype.Service;
import vip.xiaonuo.inspection.modular.translate.Util.LoggerUtil;
import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;
import vip.xiaonuo.inspection.modular.translate.dto.Utterance;
import vip.xiaonuo.inspection.modular.translate.service.QueryResultProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * @Description 处理查询结果，包括从翻译服务查询数据和格式化处理
 * @Author tang
 * @Date 2024/12/19
 */
@Service
public class QueryResultProcessorImpl implements QueryResultProcessor {

    /**
     * 处理查询结果，提取utterances中的内容，并去掉additions的内容，同时调整时间字段
     *
     * @param queryResponse 原始查询响应对象
     * @return 处理后的Map对象，只包含utterances中的text字段，并且调整了时间字段
     */
    @Override
    public QueryTaskResponse processQueryResult(QueryResponse queryResponse) {
        QueryTaskResponse processedResult = new QueryTaskResponse();
        List<Utterance> utterancesList = new ArrayList<>();
        Integer firstStartTime = null; // 用于记录第一个utterance的start_time

        // 记录原始数据
        LoggerUtil.logRequest("处理查询结果", "原始数据", JSONUtil.toJsonStr(queryResponse));

        if (queryResponse != null && queryResponse.getResp() != null && queryResponse.getResp().getUtterances() != null) {
            for (Utterance utterance : queryResponse.getResp().getUtterances()) {
                Utterance newUtterance = new Utterance();
                newUtterance.setSpeaker(utterance.getAdditions() != null ? utterance.getAdditions().getSpeaker() : null);
                newUtterance.setText(utterance.getText());

                // 处理 start_time 和 end_time
                if (utterance.getStartTime() != null) {
                    newUtterance.setStartTime(utterance.getStartTime() / 1000);
                }
                if (utterance.getEndTime() != null) {
                    newUtterance.setEndTime(utterance.getEndTime() / 1000);
                }

                // 记录第一个 utterance 的 start_time
                if (firstStartTime == null && newUtterance.getStartTime() != null) {
                    firstStartTime = newUtterance.getStartTime();
                }

                // 调整时间字段
                if (firstStartTime != null) {
                    adjustTimeFieldsBasedOnFirstStartTime(newUtterance, firstStartTime);
                }

                // 移除 additions 字段，以免在输出中出现
                newUtterance.setAdditions(null);

                utterancesList.add(newUtterance);
            }

            // 将处理后的 utterances 添加到结果中
            processedResult.setUtterances(utterancesList);
        } else {
            LoggerUtil.logRequest("处理查询结果", "resp 或 utterances 为 null", null);
        }

        // 记录处理后的数据
        LoggerUtil.logRequest("处理查询结果", "处理后数据", JSONUtil.toJsonStr(processedResult));
        return processedResult;
    }

    /**
     * 根据第一个utterance的start_time调整其他utterance的时间字段
     */
    private void adjustTimeFieldsBasedOnFirstStartTime(Utterance utterance, Integer firstStartTime) {
        if (utterance.getStartTime() != null) {
            utterance.setStartTime(utterance.getStartTime() - firstStartTime + 1);
        }
        if (utterance.getEndTime() != null) {
            utterance.setEndTime(utterance.getEndTime() - firstStartTime + 1);
        }
    }
}
