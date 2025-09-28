package org.sang.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.sang.bean.Project;
import org.sang.bean.ProjectMember;
import org.sang.bean.TableMetaData;
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
     * 添加或更新SQL表元
     * @param projectId 项目ID
     * @param sql SQL语句
     * @return 0-成功，1-无权限，2-SQL无效，3-项目AI配置缺失，4-AI解析失败，5-操作失败
     */
    public int addOrUpdateSqlTable(Long projectId, String sql) {
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

            // 使用AI解析SQL
            Map<String, Object> parsedData;
            try {
                parsedData = sqlAiParserService.parseSql(sql, apiKey.trim(), apiUrl.trim(), modelName.trim());
            } catch (Exception e) {
                e.printStackTrace();
                return 4; // AI解析失败
            }

            // 创建TableMetaData对象
            TableMetaData tableMetaData = new TableMetaData();
            tableMetaData.setName((String) parsedData.get("name"));
            tableMetaData.setColum((java.util.List<TableMetaData.ColumnMetaData>) parsedData.get("colum"));
            tableMetaData.setEntityPath((String) parsedData.get("entityPath"));
            tableMetaData.setOriginalSql(sql);

            // 获取当前项目的表元数据Map
            Map<String, TableMetaData> tableMetaMap = getTableMetaMapFromProject(project);

            // 添加或更新表元数据
            tableMetaMap.put(tableMetaData.getName(), tableMetaData);

            // 更新项目的SQL列表
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            project.setSqlList(sqlListJson);

            int result = projectMapper.updateProject(project);
            return result > 0 ? 0 : 5;

        } catch (Exception e) {
            e.printStackTrace();
            return 5;
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
     * 编辑SQL表元
     * @param projectId 项目ID
     * @param tableName 表名
     * @param sql 新的SQL语句
     * @return 0-成功，1-无权限，2-SQL无效，3-表元不存在，4-项目AI配置缺失，5-AI解析失败，6-操作失败
     */
    public int updateSqlTable(Long projectId, String tableName, String sql) {
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

            // 使用AI解析SQL
            Map<String, Object> parsedData;
            try {
                parsedData = sqlAiParserService.parseSql(sql, apiKey.trim(), apiUrl.trim(), modelName.trim());
            } catch (Exception e) {
                e.printStackTrace();
                return 5; // AI解析失败
            }

            // 创建TableMetaData对象
            TableMetaData tableMetaData = new TableMetaData();
            tableMetaData.setName((String) parsedData.get("name"));
            tableMetaData.setColum((java.util.List<TableMetaData.ColumnMetaData>) parsedData.get("colum"));
            tableMetaData.setEntityPath((String) parsedData.get("entityPath"));
            tableMetaData.setOriginalSql(sql);

            // 更新表元数据（可能表名会改变）
            tableMetaMap.remove(tableName); // 移除旧的
            tableMetaMap.put(tableMetaData.getName(), tableMetaData); // 添加新的

            // 更新项目的SQL列表
            String sqlListJson = objectMapper.writeValueAsString(tableMetaMap);
            project.setSqlList(sqlListJson);

            int result = projectMapper.updateProject(project);
            return result > 0 ? 0 : 6;

        } catch (Exception e) {
            e.printStackTrace();
            return 6;
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
}
