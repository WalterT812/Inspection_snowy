-- 创建质检记录表
CREATE TABLE IF NOT EXISTS `INSU_VOICE_INSPECTION` (
    `ID` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `INSU_VOICE_ID` int NOT NULL COMMENT '语音记录ID',
    `INSPECTION_RESULT` text COMMENT '质检结果JSON',
    `INSPECTION_TIME` datetime DEFAULT NULL COMMENT '质检时间',
    `INSPECTION_STATUS` int DEFAULT '0' COMMENT '质检状态：0-未质检，1-已质检',
    `CREATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`ID`),
    KEY `IDX_VOICE_ID` (`INSU_VOICE_ID`),
    CONSTRAINT `FK_VOICE_ID` FOREIGN KEY (`INSU_VOICE_ID`) REFERENCES `INSU_VOICE_RECORD` (`INSU_VOICE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='语音质检记录表';

-- 在INSU_VOICE_RECORD表中添加质检相关字段
ALTER TABLE `INSU_VOICE_RECORD`
ADD COLUMN `IS_INSPECTED` tinyint(1) DEFAULT '0' COMMENT '是否已质检：0-未质检，1-已质检',
ADD COLUMN `INSPECTION_TIME` datetime DEFAULT NULL COMMENT '质检时间';

-- 添加索引
CREATE INDEX `IDX_IS_INSPECTED` ON `INSU_VOICE_RECORD` (`IS_INSPECTED`);

-- 初始化现有记录的质检状态
UPDATE `INSU_VOICE_RECORD` SET `IS_INSPECTED` = 0 WHERE `IS_INSPECTED` IS NULL;

-- 创建质检规则表
CREATE TABLE IF NOT EXISTS `INSU_INSPECTION_RULE` (
    `ID` int NOT NULL AUTO_INCREMENT COMMENT '主键',
    `RULE_CODE` varchar(10) NOT NULL COMMENT '规则编号，如A1、B1',
    `RULE_NAME` varchar(100) NOT NULL COMMENT '规则名称',
    `RULE_DESCRIPTION` text COMMENT '规则详细描述',
    `RULE_LEVEL` varchar(5) NOT NULL COMMENT '规则等级：A+,A,B,C',
    `RULE_STATUS` tinyint(1) DEFAULT '1' COMMENT '规则状态：0-禁用，1-启用',
    `CREATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `UPDATE_TIME` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`ID`),
    UNIQUE KEY `UK_RULE_CODE` (`RULE_CODE`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci COMMENT='质检规则表';

-- 插入规则数据
INSERT INTO `INSU_INSPECTION_RULE` (`RULE_CODE`, `RULE_NAME`, `RULE_DESCRIPTION`, `RULE_LEVEL`) VALUES 
-- B级规则
('B1', '疾病举例不符', '坐席介绍疾病举例和合同约定疾病不符-非故意', 'B'),
('B2', '误解未纠正', '在沟通过程中客户理解产品内容或条款有误的，TSR须正面给客户解释清楚，不能转移话题，（包括不限于：客户误解返还时间、客户误解认为什么病都理赔等）', 'B'),
('B3', '虚假停售', '以保险产品即将停售为由进行宣传销售，实际并未停售/利用公司真实的停售信息促销保险产品（保监禁止行为）', 'B'),
('B4', '诋毁同业', '诋毁同业或不实评判其他公司产品、服务等', 'B'),
('B5', '虚假政策宣传', '禁用媒体、国家政策等对与保险业务相关的法律、法规、政策作虚假宣传', 'B'),
('B6', '超越授权', '超越公司授权范围，假借其他名义或方式招揽保险', 'B'),
('B7', '成交确认不规范', '无成交确认，或未按标准话术进行成单确认', 'B'),
('B8', '混淆金融产品', '以银行理财产品、银行存款等其他金融产品的名义宣传销售保险产品或跟银行的利息做片面的比较', 'B'),
('B9', '混淆概念', '将保险产品与银行存款等其他金融产品，混淆概念介绍', 'B'),
('B10', '免费误导', '表示保障不需要花钱、不需要消费购买', 'B'),
('B11', '返还误导', '坐席沟通话术中，将非返还性质的产品误导返还', 'B'),
('B12', '违规发送截图', '开通企业微信的项目，出现坐席给客户发送系统内的任何截图', 'B'),

-- C级规则
('C1', '开场结束不规范', '开场未告知姓名、工号、职务或无结束语或结束语不完整', 'C'),
('C2', '未按线索类型开场', '未按线索类型进行开场，直接销售产品', 'C'),
('C3', 'QA核听不符', '提交QA核听的退保挽留件与实际挽单情况不符', 'C'),
('C4', '保障责任过于绝对', '保障责任介绍过于绝对，与其他保险公司的产品，服务做片面、简单比较', 'C'),
('C5', '极致词语', '禁止出现极致词语', 'C'),
('C6', '回访引导', '在线回访坐席引导客户填写/阻碍投保人接受回访/诱导投保人不如实回答回访问题', 'C'),
('C7', '承诺寄送', '承诺客户投保后会有保单寄送', 'C'),
('C8', '错误邮箱', '禁止TSR指导用户填写错误邮箱', 'C'),
('C9', '理赔介绍不严谨', '关于理赔条件、时效、流程等介绍不严谨或与规定不符', 'C'),
('C10', '受益人告知错误', '法定受益人告知不正确', 'C'),
('C11', '品牌衔接', '未能与众安（爱邦）公司品牌及活动自然衔接', 'C'),
('C12', '身份不一致', '禁止坐席身份介绍前后不一，给客户造成众安（爱邦）将客户信息透露给共建合作方印象', 'C'),
('C13', '品牌不符', '品牌与活动不符合官网官微官宣内容，有意修改及夸大', 'C'),
('C14', '普通话不标准', '坐席普通话极其不标准、禁止使用方言', 'C'),
('C15', '语速问题', '坐席语速过快、表述含糊', 'C'),
('C16', '话术不熟', '话术不熟悉、表述磕绊', 'C'),
('C17', '话术不严谨', '在沟通过程中话术不严谨', 'C'),

-- A+级规则
('A+1', '虚假记录', 'TSR违反诚实义务，制造虚假记录或保单，侵害众安（爱邦）公司利益、欺骗公司', 'A+'),
('A+2', '私下联系', 'TSR索要客户本人联系方式及提供个人联系方式给用户', 'A+'),
('A+3', '引导他平台', '以多种方式引导客户在非本平台选购保险产品', 'A+'),
('A+4', '泄露数据', '窃取、泄露、倒卖公司客户数据，严重损害公司利益', 'A+'),
('A+5', '态度恶劣', '销售态度及服务态度恶劣', 'A+'),
('A+6', '带病投保', '客户健康职业等拒保，因TSR过失无法强退或无法拒绝客户的索赔请求而导致公司遭受损失', 'A+'),
('A+7', '泄露隐私', '泄露投保人、被保险人、受益人、保险公司、合作方的商业秘密或者个人隐私，导致客户投诉', 'A+'),
('A+8', '伪造信息', '诱导客户提供不真实的客户信息、伪造、篡改客户信息', 'A+'),
('A+9', '诋毁平台', '禁止诋毁众安（爱邦）平台及短险产品、和不实评判众安（爱邦）自身平台其他渠道人员、产品、服务等', 'A+'),
('A+10', '贷款误导', '主动提及"贷款"、"保单贷款"且解释错误；误导客户钱可以随时取', 'A+'),

-- A级规则
('A1', '退保未告知损失', '如客户问及退保问题，不告知客户提前解除合同会产生损失/错误承诺退保比例', 'A'),
('A2', '强制销售', '当客户委婉表示不需要、拒绝等行为时，禁止坐席继续进行销售行为', 'A'),
('A3', '纠缠客户', '客户多次表示不需要', 'A'),
('A4', '承诺额外利益', '以个人或假借公司的名义给客户提供非保险合同上的利益', 'A'),
('A5', '升级误导', '以原保障升级或替换为由促单导致客户不清楚是加入新的保障', 'A'),
('A6', '退佣承诺', '承诺给客户退佣金或给予保费折扣、替客户交保费', 'A'),
('A7', '未经审核宣传', '通过短信、微信、朋友圈等制造传播未经众安（爱邦）公司或合作方相关部门审核同意的宣传资料', 'A'),
('A8', '态度强硬', '客户对产品出现疑问时，坐席态度强硬', 'A'),
('A9', '不文明用语', '坐席话术中禁止出现不文明、不尊重客户、挑衅性的语言', 'A'),
('A10', '必说项遗漏', '规定保障内容必说项遗漏', 'A'),
('A11', '身份告知错误', '身份的错误告知或解释错误', 'A'),
('A12', '夸大保障', '夸大保障责任', 'A'),
('A13', '保单贷款解释', '主动提及保单贷款解释正确的；客户问及保单贷款且解释错误的', 'A'),
('A14', '未经同意提交', '在成交确认中，客户未明确同意参加保险时，坐席不允许私自提交保单、进行扣费', 'A'); 