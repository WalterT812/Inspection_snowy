package vip.xiaonuo.inspection.modular.translate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description 表示单个utterance
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Utterance {
    private String text;

    @JsonProperty("start_time")
    private Integer startTime;

    @JsonProperty("end_time")
    private Integer endTime;

    private Additions additions;

    private String speaker;
}
