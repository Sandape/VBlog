package org.sang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sang.bean.Project;
import org.sang.bean.ProjectMember;
import org.sang.bean.TableMetaData;
import org.sang.bean.TableMetaParseTask;
import org.sang.mapper.ProjectMapper;
import org.sang.mapper.ProjectMemberMapper;
import org.sang.service.SqlAiParserService;
import org.sang.utils.SqlParserUtil;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 项目服务类
 */
@Service
@Transactional
public class ProjectService {

    @Autowired
    private ProjectMapper projectMapper;

    @Autowired
    private ProjectMemberMapper projectMemberMapper;

    @Autowired
    private SqlAiParserService sqlAiParserService;

    @Autowired
    private AsyncTableMetaParseService asyncTableMetaParseService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建项目
     * @param project 项目信息
     * @return 0-成功，1-项目名称重复，2-创建失败
     */
    public int createProject(Project project) {
        try {
            // 设置项目拥有者
            project.setOwnerId(Util.getCurrentUser().getId());
            project.setStatus(1);
            
            // 生成8位随机项目编码
            String projectCode = generateProjectCode();
            while (projectMapper.checkProjectCodeExists(projectCode) > 0) {
                projectCode = generateProjectCode();
            }
            project.setProjectCode(projectCode);
            
            // 创建项目
            int result = projectMapper.createProject(project);
            if (result > 0) {
                // 将项目拥有者添加为项目成员
                ProjectMember ownerMember = new ProjectMember();
                ownerMember.setProjectId(project.getId());
                ownerMember.setUserId(project.getOwnerId());
                ownerMember.setRole(1); // 拥有者
                ownerMember.setStatus(1);
                projectMemberMapper.addProjectMember(ownerMember);
                return 0;
            }
            return 2;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    /**
     * 生成8位随机项目编码
     */
    private String generateProjectCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            code.append(random.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 根据项目编码加入项目
     * @param projectCode 项目编码
     * @return 0-成功，1-项目不存在，2-已是项目成员，3-加入失败
     */
    public int joinProject(String projectCode) {
        try {
            Long userId = Util.getCurrentUser().getId();
            
            // 查询项目是否存在
            Project project = projectMapper.getProjectByCode(projectCode);
            if (project == null) {
                return 1;
            }
            
            // 检查是否已是项目成员
            if (projectMemberMapper.checkUserInProject(project.getId(), userId) > 0) {
                return 2;
            }
            
            // 添加为项目成员
            ProjectMember member = new ProjectMember();
            member.setProjectId(project.getId());
            member.setUserId(userId);
            member.setRole(2); // 普通成员
            member.setStatus(1);
            
            int result = projectMemberMapper.addProjectMember(member);
            return result > 0 ? 0 : 3;
        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    /**
     * 获取项目详情
     */
    public Project getProjectDetail(Long projectId) {
        Long userId = Util.getCurrentUser().getId();
        return projectMapper.getProjectById(projectId, userId);
    }

    /**
     * 根据项目编码获取项目详情（用于预览）
     */
    public Project getProjectByCode(String projectCode) {
        return projectMapper.getProjectByCode(projectCode);
    }

    /**
     * 更新项目信息（仅项目拥有者可操作）
     */
    public int updateProject(Project project) {
        try {
            Long userId = Util.getCurrentUser().getId();
            
            // 检查是否为项目拥有者
            Project existProject = projectMapper.getProjectById(project.getId(), userId);
            if (existProject == null || !existProject.getOwnerId().equals(userId)) {
                return 1; // 无权限
            }
            
            int result = projectMapper.updateProject(project);
            return result > 0 ? 0 : 2;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    /**
     * 删除项目（仅项目拥有者可操作）
     */
    public int deleteProject(Long projectId) {
        try {
            Long userId = Util.getCurrentUser().getId();
            
            // 检查是否为项目拥有者
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null || !project.getOwnerId().equals(userId)) {
                return 1; // 无权限
            }
            
            int result = projectMapper.deleteProject(projectId);
            return result > 0 ? 0 : 2;
        } catch (Exception e) {
            e.printStackTrace();
            return 2;
        }
    }

    /**
     * 获取用户参与的项目列表
     */
    public List<Project> getUserProjects() {
        Long userId = Util.getCurrentUser().getId();
        return projectMapper.getUserProjects(userId);
    }

    /**
     * 获取用户拥有的项目列表
     */
    public List<Project> getOwnedProjects() {
        Long userId = Util.getCurrentUser().getId();
        return projectMapper.getOwnedProjects(userId);
    }

    /**
     * 搜索项目
     */
    public List<Project> searchProjects(String projectName) {
        Long userId = Util.getCurrentUser().getId();
        return projectMapper.searchProjectsByName(projectName, userId);
    }

    /**
     * 退出项目
     */
    public int leaveProject(Long projectId) {
        try {
            Long userId = Util.getCurrentUser().getId();
            
            // 检查是否为项目成员
            ProjectMember member = projectMemberMapper.getUserProjectRole(projectId, userId);
            if (member == null) {
                return 1; // 不是项目成员
            }
            
            // 项目拥有者不能退出项目
            if (member.getRole() == 1) {
                return 2; // 拥有者不能退出
            }
            
            int result = projectMemberMapper.removeProjectMember(projectId, userId);
            return result > 0 ? 0 : 3;
        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    /**
     * 获取项目成员列表
     */
    public List<ProjectMember> getProjectMembers(Long projectId) {
        Long userId = Util.getCurrentUser().getId();
        
        // 检查用户是否为项目成员
        if (projectMemberMapper.checkUserInProject(projectId, userId) == 0) {
            return null; // 无权限查看
        }
        
        return projectMemberMapper.getProjectMembers(projectId);
    }

    /**
     * 移除项目成员（仅项目拥有者可操作）
     */
    public int removeMember(Long projectId, Long memberId) {
        try {
            Long userId = Util.getCurrentUser().getId();
            
            // 检查是否为项目拥有者
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null || !project.getOwnerId().equals(userId)) {
                return 1; // 无权限
            }
            
            // 不能移除自己
            if (memberId.equals(userId)) {
                return 2; // 不能移除自己
            }
            
            int result = projectMemberMapper.removeProjectMember(projectId, memberId);
            return result > 0 ? 0 : 3;
        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    /**
     * 添加或更新SQL表元（异步AI解析）
     * @param projectId 项目ID
     * @param sql SQL语句
     * @param entityPath Entity路径（可选）
     * @return 0-成功，1-无权限，2-SQL无效，3-项目AI配置缺失，4-操作失败
     */
    public int addOrUpdateSqlTable(Long projectId, String sql, String entityPath) {
        try {
            Long userId = Util.getCurrentUser().getId();

            // 检查用户是否为项目成员
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null) {
                return 1; // 无权限
            }

            // 检查项目AI配置
            String apiKey = project.getApiKey();
            String apiUrl = project.getApiUrl();
            String modelName = project.getModelName();

            if (apiKey == null || apiKey.trim().isEmpty() ||
                apiUrl == null || apiUrl.trim().isEmpty() ||
                modelName == null || modelName.trim().isEmpty()) {
                return 3; // 项目AI配置缺失
            }

            // 验证SQL是否有效
            if (!sqlAiParserService.isValidSql(sql)) {
                return 2; // SQL无效
            }

            // 创建TableMetaData对象（初始状态为PENDING）
            TableMetaData tableMetaData = new TableMetaData();
            tableMetaData.setName("temp_" + System.currentTimeMillis()); // 临时表名，稍后会被AI解析覆盖
            tableMetaData.setOriginalSql(sql);
            tableMetaData.setParseStatus("PENDING");

            // 如果提供了entityPath，则设置它
            if (entityPath != null && !entityPath.trim().isEmpty()) {
                tableMetaData.setEntityPath(entityPath.trim());
            }

            // 获取当前项目的表元数据Map
            Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(project);

            // 使用SQL中的表名作为key（如果能解析出来）
            String tableName = extractTableNameFromSql(sql);
            if (tableName == null || tableName.trim().isEmpty()) {
                tableName = "table_" + System.currentTimeMillis();
            }

            // 添加或更新表元数据
            tableMetaMap.put(tableName, tableMetaData);

            // 更新项目的SQL列表
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            project.setSqlList(sqlListJson);

            int result = projectMapper.updateProject(project);
            if (result > 0) {
                // 异步启动AI解析任务
                TableMetaParseTask task = new TableMetaParseTask(
                    userId, projectId, tableName, apiKey.trim(), apiUrl.trim(), modelName.trim(), sql
                );
                asyncTableMetaParseService.parseTableMetaAsync(task);
                return 0;
            }
            return 4;

        } catch (Exception e) {
            e.printStackTrace();
            return 4;
        }
    }

    /**
     * 获取项目SQL表元列表
     * @param projectId 项目ID
     * @return SQL表元Map，key为表名，value为SQL语句
     */
    public Map<String, TableMetaData> getProjectSqlTables(Long projectId) {
        try {
            Long userId = Util.getCurrentUser().getId();

            // 检查用户是否为项目成员
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null) {
                return null; // 无权限
            }

            return getTableMetaMapFromProject(project);

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }

    /**
     * 编辑SQL表元（异步AI解析）
     * @param projectId 项目ID
     * @param tableName 表名
     * @param sql 新的SQL语句
     * @param entityPath Entity路径（可选）
     * @return 0-成功，1-无权限，2-SQL无效，3-表元不存在，4-项目AI配置缺失，5-操作失败
     */
    public int updateSqlTable(Long projectId, String tableName, String sql, String entityPath) {
        try {
            Long userId = Util.getCurrentUser().getId();

            // 检查用户是否为项目成员
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null) {
                return 1; // 无权限
            }

            // 检查项目AI配置
            String apiKey = project.getApiKey();
            String apiUrl = project.getApiUrl();
            String modelName = project.getModelName();

            if (apiKey == null || apiKey.trim().isEmpty() ||
                apiUrl == null || apiUrl.trim().isEmpty() ||
                modelName == null || modelName.trim().isEmpty()) {
                    return 4; // 项目AI配置缺失
            }

            // 验证SQL是否有效
            if (!sqlAiParserService.isValidSql(sql)) {
                return 2; // SQL无效
            }

            // 获取当前项目的表元数据Map
            Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(project);

            // 检查表元是否存在
            if (!tableMetaMap.containsKey(tableName)) {
                return 3; // 表元不存在
            }

            // 创建TableMetaData对象（初始状态为PENDING）
            TableMetaData tableMetaData = new TableMetaData();
            tableMetaData.setName("temp_" + System.currentTimeMillis()); // 临时表名，稍后会被AI解析覆盖
            tableMetaData.setOriginalSql(sql);
            tableMetaData.setParseStatus("PENDING");

            // 如果提供了entityPath，则设置它
            if (entityPath != null && !entityPath.trim().isEmpty()) {
                tableMetaData.setEntityPath(entityPath.trim());
            }

            // 更新表元数据（可能表名会改变）
            tableMetaMap.remove(tableName); // 移除旧的

            // 使用新的表名作为key
            String newTableName = extractTableNameFromSql(sql);
            if (newTableName == null || newTableName.trim().isEmpty()) {
                newTableName = tableName; // 如果无法提取，使用原来的表名
            }

            tableMetaMap.put(newTableName, tableMetaData); // 添加新的

            // 更新项目的SQL列表
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            project.setSqlList(sqlListJson);

            int result = projectMapper.updateProject(project);
            if (result > 0) {
                // 异步启动AI解析任务
                TableMetaParseTask task = new TableMetaParseTask(
                    userId, projectId, newTableName, apiKey.trim(), apiUrl.trim(), modelName.trim(), sql
                );
                asyncTableMetaParseService.parseTableMetaAsync(task);
                return 0;
            }
            return 5;

        } catch (Exception e) {
            e.printStackTrace();
            return 5;
        }
    }

    /**
     * 删除SQL表元（仅项目拥有者可操作）
     * @param projectId 项目ID
     * @param tableName 表名
     * @return 0-成功，1-无权限，2-表元不存在，3-操作失败
     */
    public int deleteSqlTable(Long projectId, String tableName) {
        try {
            Long userId = Util.getCurrentUser().getId();

            // 检查是否为项目拥有者
            Project project = projectMapper.getProjectById(projectId, userId);
            if (project == null || !project.getOwnerId().equals(userId)) {
                return 1; // 无权限
            }

            // 获取当前项目的表元数据Map
            Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(project);

            // 检查表元是否存在
            if (!tableMetaMap.containsKey(tableName)) {
                return 2; // 表元不存在
            }

            // 删除表元
            tableMetaMap.remove(tableName);

            // 更新项目的SQL列表
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            project.setSqlList(sqlListJson);

            int result = projectMapper.updateProject(project);
            return result > 0 ? 0 : 3;

        } catch (Exception e) {
            e.printStackTrace();
            return 3;
        }
    }

    /**
     * 从项目对象中获取SQL Map
     */
    private Map<String, TableMetaData> getTableMetaMapFromProject(Project project) {
        try {
            String sqlListJson = project.getSqlList();
            if (sqlListJson == null || sqlListJson.trim().isEmpty()) {
                return new HashMap<>();
            }
            return objectMapper.readValue(sqlListJson, new TypeReference<Map<String, TableMetaData>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap<>();
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
            // 简单的表名提取逻辑，查找CREATE TABLE或ALTER TABLE后的表名
            String upperSql = sql.trim().toUpperCase();

            // 处理CREATE TABLE语句
            if (upperSql.startsWith("CREATE TABLE")) {
                String afterCreate = sql.substring("CREATE TABLE".length()).trim();
                int spaceIndex = afterCreate.indexOf(' ');
                if (spaceIndex > 0) {
                    String tableName = afterCreate.substring(0, spaceIndex).trim();
                    // 移除可能的反引号
                    return tableName.replace("`", "").replace("[", "").replace("]", "");
                }
            }

            // 处理ALTER TABLE语句
            if (upperSql.startsWith("ALTER TABLE")) {
                String afterAlter = sql.substring("ALTER TABLE".length()).trim();
                int spaceIndex = afterAlter.indexOf(' ');
                if (spaceIndex > 0) {
                    String tableName = afterAlter.substring(0, spaceIndex).trim();
                    return tableName.replace("`", "").replace("[", "").replace("]", "");
                }
            }

            // 处理其他类型的SQL，尝试从FROM子句中提取
            int fromIndex = upperSql.indexOf(" FROM ");
            if (fromIndex > 0) {
                String afterFrom = sql.substring(fromIndex + " FROM ".length()).trim();
                int spaceIndex = afterFrom.indexOf(' ');
                if (spaceIndex > 0) {
                    String tableName = afterFrom.substring(0, spaceIndex).trim();
                    return tableName.replace("`", "").replace("[", "").replace("]", "");
                } else if (afterFrom.length() > 0) {
                    return afterFrom.split("\\s+")[0].replace("`", "").replace("[", "").replace("]", "");
                }
            }

        } catch (Exception e) {
            // 提取失败，返回null
        }

        return null;
    }
}
