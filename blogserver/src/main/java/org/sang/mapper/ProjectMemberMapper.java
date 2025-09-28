package org.sang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.bean.ProjectMember;

import java.util.List;

/**
 * 项目成员Mapper接口
 */
@Mapper
public interface ProjectMemberMapper {

    /**
     * 添加项目成员
     */
    int addProjectMember(ProjectMember projectMember);

    /**
     * 移除项目成员
     */
    int removeProjectMember(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 查询项目成员列表
     */
    List<ProjectMember> getProjectMembers(@Param("projectId") Long projectId);

    /**
     * 查询用户在项目中的角色
     */
    ProjectMember getUserProjectRole(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 检查用户是否为项目成员
     */
    int checkUserInProject(@Param("projectId") Long projectId, @Param("userId") Long userId);

    /**
     * 更新成员状态
     */
    int updateMemberStatus(@Param("projectId") Long projectId, @Param("userId") Long userId, @Param("status") Integer status);

    /**
     * 查询项目成员数量
     */
    int getProjectMemberCount(@Param("projectId") Long projectId);

    /**
     * 查询用户参与的项目成员记录
     */
    List<ProjectMember> getUserProjectMembers(@Param("userId") Long userId);
}
