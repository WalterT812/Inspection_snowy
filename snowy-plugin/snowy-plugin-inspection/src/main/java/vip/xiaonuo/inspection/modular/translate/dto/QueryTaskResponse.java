package vip.xiaonuo.inspection.modular.translate.dto;

import lombok.Data;

import java.util.List;

/**
 * @Description 查询任务结果的响应 DTO
 * @Author tang
 * @Date 2024/12/20
 */
@Data
public class QueryTaskResponse {
    private List<Utterance> utterances;
}
