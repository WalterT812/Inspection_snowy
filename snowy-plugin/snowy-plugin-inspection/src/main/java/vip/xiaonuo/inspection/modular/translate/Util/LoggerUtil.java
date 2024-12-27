package vip.xiaonuo.inspection.modular.translate.Util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author tang
 * @date 2024/12/19
 * @description 日志处理和异常抛出
 **/
public class LoggerUtil {
    private static final Logger logger = LoggerFactory.getLogger(LoggerUtil.class);

    /**
     * 统一日志输出
     *
     * @param action       操作名称
     * @param infoType     信息类型（如 URL、数据等）
     * @param infoContent  信息内容
     */
    public static void logRequest(String action, String infoType, Object infoContent) {
        logger.info("[{}] {}: {}", action, infoType, infoContent);
    }

    /**
     * 统一异常处理
     *
     * @param message 错误消息
     * @param e       异常
     */
    public static void handleException(String message, Exception e) {
        if (e != null) {
            logger.error(message, e);
            throw new TranslateServiceException(message, e);
        } else {
            logger.error(message);
            throw new TranslateServiceException(message);
        }
    }

    /**
     * 自定义异常类
     */
    public static class TranslateServiceException extends RuntimeException {
        public TranslateServiceException(String message) {
            super(message);
        }

        public TranslateServiceException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
