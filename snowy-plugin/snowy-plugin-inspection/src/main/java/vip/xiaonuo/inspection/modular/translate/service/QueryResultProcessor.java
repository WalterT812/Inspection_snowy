package vip.xiaonuo.inspection.modular.translate.service;

import vip.xiaonuo.inspection.modular.translate.dto.QueryResponse;
import vip.xiaonuo.inspection.modular.translate.dto.QueryTaskResponse;

/**
 * @author tanghaoyu
 * @date 2024/12/19
 * @description 处理查询结果，包括从翻译服务查询数据和格式化处理
 **/
public interface QueryResultProcessor {
    QueryTaskResponse processQueryResult(QueryResponse queryResponse);
}
