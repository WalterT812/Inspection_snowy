package vip.xiaonuo.inspection.modular.translate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description 表示utterance中的additions字段
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Additions {
    private String event;
    private String speaker;
}
