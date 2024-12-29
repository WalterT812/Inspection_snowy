package vip.xiaonuo.inspection.modular.inspection.enums;

import lombok.Getter;

/**
 * 通用排序枚举
 */
@Getter
public enum CommonSortOrderEnum {
    
    /** 升序 */
    ASC("ASC"),
    
    /** 降序 */
    DESC("DESC");
    
    private final String value;
    
    CommonSortOrderEnum(String value) {
        this.value = value;
    }
    
    /**
     * 校验排序方式
     */
    public static void validate(String value) {
        boolean flag = ASC.getValue().equalsIgnoreCase(value) || DESC.getValue().equalsIgnoreCase(value);
        if (!flag) {
            throw new IllegalArgumentException("不支持的排序方式：" + value);
        }
    }
} 