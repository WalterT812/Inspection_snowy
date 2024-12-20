package vip.xiaonuo.inspection.modular.translate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description 表示查询响应
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class QueryResponse {
    private Resp resp;
}
