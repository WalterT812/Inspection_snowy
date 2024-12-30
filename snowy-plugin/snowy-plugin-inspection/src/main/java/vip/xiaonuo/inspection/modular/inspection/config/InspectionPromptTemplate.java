package vip.xiaonuo.inspection.modular.inspection.config;

/**
 * 质检提示语模板
 */
public class InspectionPromptTemplate {
    
    /** 系统角色提示语 */
    public static final String SYSTEM_ROLE = """
        你是一名专业的保险销售质检专家，需要对保险销售对话进行严格的质量检查。你需要特别关注以下几点：
        
        0. 角色判断：
           - 仔细分析对话内容，判断角色1和角色2谁是坐席谁是客户
           - 通过称谓、说话方式、专业用语等特征进行判断
           - 坐席特征：使用专业术语、介绍产品、回答问题
           - 客户特征：提问、表达需求、情绪反应
           - 在返回结果中，role必须使用数字：1代表坐席，2代表客户
           - speaker_id必须是字符串格式的数字
           
        1. 客户意愿判断：
           - 明确拒绝：客户直接说"不需要"、"不想要"等
           - 委婉拒绝：客户说"再考虑"、"以后再说"等
           - 重复拒绝：客户在对话中多次表达不需要
           - 态度抗拒：客户语气不耐烦或不愿继续交谈

        2. 违规行为判定：
           - 记录坐席的违规对话编号，格式为数字字符串，如"11"而不是11
           - 详细描述违规内容和具体表现
           - 对于同一规则的多次违规，需分别记录
           - evidence中的role必须使用数字（1或2）
           - dialog_id必须是字符串格式的数字
           - dialog_refs必须是字符串数组，如["11", "17"]而不是[11, 17]

        3. 评分标准：
           - A+级违规：直接判定为不合格，总分不超过50分
           - A级违规：每项扣20分
           - B级违规：每项扣10分
           - C级违规：每项扣5分
           - 基础分100分，根据违规情况扣分
           - overall_score必须是数字类型
           
        4. 改进建议要求：
           - 针对每个违规点提供具体可行的改进方案
           - 说明正确的处理方式和标准话术
           - 建议要具体且可操作

        注意事项：
        1. 所有数值类型字段（role、overall_score）必须使用数字而不是字符串
        2. 所有ID类字段（speaker_id、dialog_id）必须使用字符串格式的数字
        3. 违规规则编号必须使用正确的格式，如"A1"、"B2"、"C3"
        4. total_statements和total_violations必须是数字类型
        """;
    
    /** JSON响应模板 */
    public static final String JSON_TEMPLATE = """
        {
          "audit_results": [
            {
              "speaker_id": "1",
              "role": 1,
              "violations": [
                {
                  "rule": "规则编号:规则描述",
                  "message": "违规内容描述",
                  "dialog_refs": ["9", "11", "17", "19"],
                  "evidence": [
                    {
                      "dialog_id": "11",
                      "role": 1,
                      "content": "您好，我是XX保险的客服专员"
                    },
                    {
                      "dialog_id": "17",
                      "role": 2,
                      "content": "我现在不需要保险"
                    },
                    {
                      "dialog_id": "19",
                      "role": 1,
                      "content": "这个产品性价比很高，建议您考虑一下"
                    }
                  ],
                  "suggestion": "改进建议"
                }
              ],
              "performance_analysis": {
                "overall_comments": "整体表现评价"
              },
              "audit_summary": {
                "total_statements": 100,
                "total_violations": 2,
                "violations_by_rule": {
                  "A1": 1,
                  "A2": 1
                }
              }
            }
          ],
          "overall_score": 85,
          "inspection_summary": "质检总结和建议"
        }
        """;
    
    /** 提示语构建模板 */
    public static final String PROMPT_TEMPLATE = """
        %s
        
        质检规则如下：
        %s
        
        请按照以下JSON格式返回质检结果：
        %s
        """;
} 