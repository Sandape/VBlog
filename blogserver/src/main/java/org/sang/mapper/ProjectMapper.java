package org.sang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.bean.Project;

import java.util.List;

/**
 * 项目Mapper接口
 */
@Mapper
public interface ProjectMapper {

    /**
     * 创建项目
     */
    int createProject(Project project);

    /**
     * 根据项目编码查询项目
     */
    Project getProjectByCode(@Param("projectCode") String projectCode);

    /**
     * 根据项目ID查询项目详情
     */
    Project getProjectById(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 更新项目信息
     */
    int updateProject(Project project);

    /**
     * 删除项目
     */
    int deleteProject(@Param("id") Long id);

    /**
     * 查询用户参与的项目列表
     */
    List<Project> getUserProjects(@Param("userId") Long userId);

    /**
     * 查询用户拥有的项目列表
     */
    List<Project> getOwnedProjects(@Param("ownerId") Long ownerId);

    /**
     * 检查项目编码是否存在
     */
    int checkProjectCodeExists(@Param("projectCode") String projectCode);

    /**
     * 根据项目名称模糊查询
     */
    List<Project> searchProjectsByName(@Param("projectName") String projectName, @Param("userId") Long userId);

    /**
     * 获取所有项目列表（用于Prompt生成器显示所有项目）
     */
    List<Project> getAllProjects(@Param("userId") Long userId);

    /**
     * 获取项目的SQL列表（仅用于异步更新）
     */
    String getProjectSqlListOnly(@Param("projectId") Long projectId);

    /**
     * 更新项目的SQL列表（仅用于异步更新）
     */
    int updateProjectSqlListOnly(@Param("projectId") Long projectId, @Param("sqlList") String sqlList);
}
