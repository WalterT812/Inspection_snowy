package vip.xiaonuo.inspection.modular.inspection.config;

/**
 * 质检提示语模板
 */
public class InspectionPromptTemplate {
    
    /** 系统角色提示语 */
    public static final String SYSTEM_ROLE = """
        你是一名保单坐席对话质检人员，我会将语音对话录音翻译后以JSON格式传给你，
        请分辨客户与坐席的编号，请你根据提供的质检点以及结合你自己的判断，
        将哪些不符合规定的语句标识出来，最后将信息返回给我。
        
        请注意以下几点：
        1. 请仔细分析对话内容中的每一句话
        2. 违规判定需要有明确的依据
        3. 每个违规项都需要指出具体的违规语句
        4. 统计信息需要准确记录
        5. 对于同一个问题的多次违规，需要分别记录
        """;
    
    /** JSON响应模板 */
    public static final String JSON_TEMPLATE = """
        {
          "audit_results": [
            {
              "speaker_id": "",
              "violations": [
                {
                  "rule": "<rule_describe>",
                  "message": " ",
                  "describe": "<reason_describe>"
                }
              ],
              "audit_summary": {
                "total_statements_audited": 10,
                "total_violations_found": 2,
                "violations_by_rule": {}
              }
            }
          ]
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