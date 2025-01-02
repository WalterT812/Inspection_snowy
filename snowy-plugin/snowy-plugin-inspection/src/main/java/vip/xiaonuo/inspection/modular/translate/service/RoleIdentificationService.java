package vip.xiaonuo.inspection.modular.translate.service;

import vip.xiaonuo.inspection.modular.translate.dto.Utterance;
import java.util.List;

/**
 * 对话角色识别服务
 */
public interface RoleIdentificationService {
    /**
     * 识别对话中的坐席角色
     * @param utterances 对话列表
     * @return 坐席的speaker编号(1或2)
     */
    String identifyStaffSpeaker(List<Utterance> utterances);
} 