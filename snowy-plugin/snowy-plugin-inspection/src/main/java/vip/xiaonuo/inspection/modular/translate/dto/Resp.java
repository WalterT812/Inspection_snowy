package vip.xiaonuo.inspection.modular.translate.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author tanghaoyu
 * @date 2024/12/20
 * @description 表示响应中的 resp 字段
 **/
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Resp {
    private Additions additions;
    private Integer code;
    private String id;
    private String message;
    private String text;
    private List<Utterance> utterances;
}