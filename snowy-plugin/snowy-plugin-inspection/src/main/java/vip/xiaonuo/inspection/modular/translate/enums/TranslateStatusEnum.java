package vip.xiaonuo.inspection.modular.translate.enums;

import lombok.Getter;
import vip.xiaonuo.common.exception.CommonException;

@Getter
public enum TranslateStatusEnum {

    /**
     * 处理中
     */
    PROCESSING("PROCESSING"),

    /**
     * 转换成功
     */
    SUCCESS("SUCCESS"),

    /**
     * 转换失败
     */
    FAIL("FAIL");

    private final String value;

    TranslateStatusEnum(String value) {
        this.value = value;
    }

    public static void validate(String value) {
        boolean flag = PROCESSING.getValue().equals(value) || SUCCESS.getValue().equals(value) || FAIL.getValue().equals(value);
        if (!flag) {
            throw new CommonException("不支持的语音转文字状态：{}", value);
        }
    }
}