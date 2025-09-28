package org.sang.service;

import com.alibaba.druid.support.json.JSONUtils;
import com.fasterxml.jackson.core.type.TypeReference;
import org.sang.bean.Project;
import org.sang.bean.PromptLog;
import org.sang.bean.User;
import org.sang.mapper.PromptLogMapper;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Prompt生成器服务类
 */
@Service
@Transactional
public class PromptGeneratorService {

    @Autowired
    private PromptLogMapper promptLogMapper;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private LLMService llmService;


    /**
     * 生成AI解读Prompt
     * @param projectId 项目ID
     * @param apiName 接口名
     * @param apiPath 接口路径
     * @param apiDesc 接口描述
     * @param apiRequest 接口请求体
     * @param apiResponse 接口响应体
     * @param apiSqlList 相关数据库表SQL列表
     * @return 生成的Prompt内容
     */
    public String generatePrompt(Long projectId, String apiName, String apiPath, String apiDesc,
                                String apiRequest, String apiResponse, List<String> apiSqlList) {
        try {
            User currentUser = Util.getCurrentUser();
            
            // 1. 检查项目权限
            Project project = projectService.getProjectDetail(projectId);
            if (project == null) {
                throw new RuntimeException("项目不存在或无权限访问");
            }

            // 2. 创建日志记录
            PromptLog promptLog = new PromptLog();
            promptLog.setProjectId(projectId);
            promptLog.setUserId(currentUser.getId());
            promptLog.setApiName(apiName);
            promptLog.setApiPath(apiPath);
            promptLog.setApiDesc(apiDesc);
            promptLog.setApiRequest(apiRequest);
            promptLog.setApiResponse(apiResponse);
            
            // 将SQL列表转为JSON存储
            String apiSqlJson = JSONUtils.toJSONString(apiSqlList);
            promptLog.setApiSql(apiSqlJson);

            // 插入日志记录
            int insertResult = promptLogMapper.insertPromptLog(promptLog);
            if (insertResult <= 0) {
                throw new RuntimeException("日志记录创建失败");
            }

            // 3. 解析SQL并更新项目的sql_list字段
            updateProjectSqlList(project, apiSqlList);

            // 4. 构建模板A并调用AI
            String templateA = buildTemplateA(apiName, apiRequest, apiResponse, apiSqlList);
            String aiResponse = callAI(project, templateA);

            // 5. 构建模板B生成最终Prompt
            String finalPrompt = buildTemplateB(project, currentUser, apiName, apiPath, 
                                              apiRequest, apiResponse, aiResponse);

            // 6. 更新日志记录
            promptLogMapper.updatePromptLogResult(promptLog.getId(), aiResponse, finalPrompt);

            return finalPrompt;

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("生成Prompt失败: " + e.getMessage());
        }
    }

    /**
     * 更新项目的SQL列表
     */
    private void updateProjectSqlList(Project project, List<String> apiSqlList) {
        try {
            // 获取当前项目的SQL列表
            Map<String, String> currentSqlMap = new HashMap<>();

            if (project.getSqlList() != null && !project.getSqlList().trim().isEmpty()) {
                String sqlListJson = project.getSqlList().trim();

                try {
                    // 验证JSON格式是否为Map<String, String>
                    currentSqlMap = (Map<String, String>) JSONUtils.parse(sqlListJson);
                } catch (Exception e) {
                    // 如果反序列化失败，尝试检查是否为其他格式
                    try {
                        // 检查是否为数组格式
                        @SuppressWarnings("unchecked")
                        List<String> sqlList = (List<String>) JSONUtils.parse(sqlListJson);
                        // 如果是数组格式，转换为Map格式（使用索引作为key）
                        if (sqlList != null) {
                            for (int i = 0; i < sqlList.size(); i++) {
                                currentSqlMap.put("sql_" + i, sqlList.get(i));
                            }
                        }
                    } catch (Exception e2) {
                        // 如果还是失败，检查是否为复杂对象格式
                        try {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> complexObj = (Map<String, Object>) JSONUtils.parse(sqlListJson);
                            // 如果是复杂对象，尝试提取其中的SQL相关信息
                            if (complexObj != null) {
                                Object projectObj = complexObj.get("project");
                                if (projectObj instanceof Map) {
                                    @SuppressWarnings("unchecked")
                                    Map<String, Object> projectMap = (Map<String, Object>) projectObj;
                                    // 忽略复杂对象，使用空Map
                                    System.err.println("Warning: Found complex project object in sqlList, ignoring: " + projectObj);
                                }
                            }
                        } catch (Exception e3) {
                            System.err.println("Error parsing sqlList JSON: " + e3.getMessage());
                        }
                        // 无论如何，使用空Map作为默认值
                        currentSqlMap = new HashMap<>();
                    }
                }
            }

            // 解析新的SQL并添加到Map中
            for (String sql : apiSqlList) {
                String tableName = extractTableNameFromSql(sql);
                if (tableName != null && !tableName.trim().isEmpty()) {
                    currentSqlMap.put(tableName, sql);
                }
            }

            // 更新项目
            String updatedSqlJson = JSONUtils.toJSONString(currentSqlMap);
            project.setSqlList(updatedSqlJson);
            projectService.updateProject(project);

        } catch (Exception e) {
            // 解析失败不影响主流程
            System.err.println("Error in updateProjectSqlList: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 从SQL语句中提取表名
     */
    private String extractTableNameFromSql(String sql) {
        if (sql == null || sql.trim().isEmpty()) {
            return null;
        }

        try {
            String upperSql = sql.trim().toUpperCase();
            
            // CREATE TABLE语句
            if (upperSql.startsWith("CREATE TABLE")) {
                String afterCreate = sql.substring("CREATE TABLE".length()).trim();
                int spaceIndex = afterCreate.indexOf(' ');
                if (spaceIndex > 0) {
                    return afterCreate.substring(0, spaceIndex)
                            .replace("`", "").replace("[", "").replace("]", "").trim();
                }
            }
            
            // INSERT INTO语句
            if (upperSql.startsWith("INSERT INTO")) {
                String afterInsert = sql.substring("INSERT INTO".length()).trim();
                int spaceIndex = afterInsert.indexOf(' ');
                if (spaceIndex > 0) {
                    return afterInsert.substring(0, spaceIndex)
                            .replace("`", "").replace("[", "").replace("]", "").trim();
                }
            }

            return "table_" + System.currentTimeMillis();
        } catch (Exception e) {
            return "table_" + System.currentTimeMillis();
        }
    }

    /**
     * 构建模板A
     */
    private String buildTemplateA(String apiName, String apiRequest, String apiResponse, 
                                 List<String> apiSqlList) {
        StringBuilder template = new StringBuilder();
        template.append("你是一名Java Web开发领域的经验丰富开发工程师，具有丰富的需求设计与开发经验，你会针对我发送给你的[接口名]、[接口请求体]、[接口响应体]与[表元信息]进行解析，按下面的Json报文格式返回给我解析结果：\n\n");
        
        template.append("# 解析内容\n");
        template.append("1. 你会综合考量需求方案设计与开发技术实现两个维度，评估接口请求体和接口响应体中各个参数在接口流程中是否必填、有何含义，以参数名、参数类型、是否必填、参数说明的四列markdown格式表格，填入返回报文的request和response两个markdown格式表单中；\n");
        template.append("2. 你会从你丰富的开发经验出发，综合【包括但不限于】以下两个标准进行综合评估，逐行分析接口请求体和响应体中的参数与表元中字段的关系，在返回的json报文的resource字段中填充每个响应报文字段的对应关系，填充markDown格式的接口响应报文结构表：\n");
        template.append("   1. 能够从数据库表中的下划线连接符字段，简单转译成Java应用中驼峰结构参数名的数据，一定是相关的，例如user_id和userId一定是同一个字段；\n");
        template.append("   2. 面对不能通过上一条规则匹配数据源的数据，你需要综合分析接口描述和响应报文字段名和数据库表字段含义进行评估，匹配有可能存在隐形关联的响应报文字段和数据库表字段。\n\n");

        template.append("# 返回格式 **只进行以下json报文的返回，禁止返回除了json报文以外的任何内容**\n");
        template.append("{\n");
        template.append("\t\"request\": \"\",\n");
        template.append("\t\"response\": \"\",\n");
        template.append("\t\"resource\": \"\"\n");
        template.append("}\n\n");

        template.append("# 接口名\n");
        template.append(apiName != null ? apiName : "").append("\n\n");

        template.append("# 接口请求体(可能为空)\n");
        template.append(apiRequest != null ? apiRequest : "").append("\n\n");

        template.append("# 接口响应体(可能为空)\n");
        template.append(apiResponse != null ? apiResponse : "").append("\n\n");

        template.append("# 表元信息\n");
        if (apiSqlList != null && !apiSqlList.isEmpty()) {
            for (String sql : apiSqlList) {
                template.append(sql).append("\n\n");
            }
        }

        return template.toString();
    }

    /**
     * 调用AI获取解析结果
     */
    private String callAI(Project project, String message) throws Exception {
        String apiKey = project.getApiKey();
        String apiUrl = project.getApiUrl();
        String modelName = project.getModelName();

        if (apiKey == null || apiKey.trim().isEmpty() ||
            apiUrl == null || apiUrl.trim().isEmpty() ||
            modelName == null || modelName.trim().isEmpty()) {
            throw new RuntimeException("项目AI配置缺失");
        }

        return llmService.getAIResponse(apiKey.trim(), apiUrl.trim(), modelName.trim(), message);
    }

    /**
     * 构建模板B
     */
    private String buildTemplateB(Project project, User currentUser, String apiName, String apiPath,
                                 String apiRequest, String apiResponse, String aiResponse) {
        try {
            // 解析AI响应的JSON
            Map<String, String> aiResult = parseAiResponse(aiResponse);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            String currentDate = sdf.format(new Date());
            
            StringBuilder template = new StringBuilder();
            template.append("- since: ").append(currentDate).append("\n");
            template.append("- author: ").append(currentUser.getNickname()).append("\n\n");
            
            template.append("# 核心任务\n\n");
            template.append("请你按要求完成").append(apiName).append("的开发\n\n");
            
            template.append("# 开发规范\n");
            template.append(project.getDevelopmentSpec() != null ? project.getDevelopmentSpec() : "").append("\n\n");
            
            template.append("# 开发示例\n");
            if (project.getExampleMapperPath() != null) {
                template.append("Mapper类示例：").append(project.getExampleMapperPath()).append("\n");
            }
            if (project.getExampleEntityPath() != null) {
                template.append("Entity类示例：").append(project.getExampleEntityPath()).append("\n");
            }
            if (project.getExampleInterfacePath() != null) {
                template.append("接口示例：").append(project.getExampleInterfacePath()).append("\n");
            }
            template.append("\n");
            
            template.append("# 接口API\n\n");
            template.append("**接口路径**\n");
            template.append("```http\n");
            template.append(apiPath).append("\n");
            template.append("```\n\n");
            
            // 请求体部分
            String requestTable = aiResult.get("request");
            if (requestTable != null && !requestTable.trim().isEmpty()) {
                template.append("**请求体：**\n");
                template.append(requestTable).append("\n\n");
            }
            
            // 响应结构部分
            String responseTable = aiResult.get("response");
            if (responseTable != null && !responseTable.trim().isEmpty()) {
                template.append("**响应结构：**\n");
                template.append(responseTable).append("\n\n");
            }
            
            template.append("**关联数据库表：**\n");
            // 这里可以根据项目的SQL列表填充表信息
            template.append("待补充具体表信息\n\n");
            
            template.append("**接口数据源关系**\n");
            String resourceTable = aiResult.get("resource");
            if (resourceTable != null && !resourceTable.trim().isEmpty()) {
                template.append(resourceTable).append("\n");
            }
            
            return template.toString();
            
        } catch (Exception e) {
            e.printStackTrace();
            // 如果解析失败，返回基础模板
            return buildBasicTemplateB(project, currentUser, apiName, apiPath);
        }
    }

    /**
     * 解析AI响应的JSON
     * 先使用JsonUtils的方法，将String解析成Json，然后再从Json转成Map
     */
    private Map<String, String> parseAiResponse(String aiResponse) {
        try {
            // 使用JsonUtils的方法进行解析
            return (Map<String, String>) JSONUtils.parse(aiResponse);
        } catch (Exception e) {
            // 解析失败返回空Map
            System.err.println("Error parsing AI response using JsonUtils: " + e.getMessage());
            return new HashMap<>();
        }
    }

    /**
     * 构建基础模板B（当AI解析失败时使用）
     */
    private String buildBasicTemplateB(Project project, User currentUser, String apiName, String apiPath) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
        String currentDate = sdf.format(new Date());
        
        StringBuilder template = new StringBuilder();
        template.append("- since: ").append(currentDate).append("\n");
        template.append("- author: ").append(currentUser.getNickname()).append("\n\n");
        template.append("# 核心任务\n\n");
        template.append("请你按要求完成").append(apiName).append("的开发\n\n");
        template.append("# 接口API\n\n");
        template.append("**接口路径**\n");
        template.append("```http\n");
        template.append(apiPath).append("\n");
        template.append("```\n\n");
        
        return template.toString();
    }

    /**
     * 获取用户的Prompt日志列表
     */
    public List<PromptLog> getUserPromptLogs() {
        User currentUser = Util.getCurrentUser();
        return promptLogMapper.getPromptLogsByUserId(currentUser.getId());
    }

    /**
     * 获取项目的Prompt日志列表
     */
    public List<PromptLog> getProjectPromptLogs(Long projectId) {
        User currentUser = Util.getCurrentUser();
        return promptLogMapper.getPromptLogsByProjectId(projectId, currentUser.getId());
    }

    /**
     * 根据ID获取Prompt日志详情
     */
    public PromptLog getPromptLogById(Long id) {
        return promptLogMapper.getPromptLogById(id);
    }

    /**
     * 删除Prompt日志
     */
    public boolean deletePromptLog(Long id) {
        User currentUser = Util.getCurrentUser();
        return promptLogMapper.deletePromptLog(id, currentUser.getId()) > 0;
    }
}
