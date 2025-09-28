package org.sang.service;

import org.sang.bean.Project;
import org.sang.bean.ProjectMember;
import org.sang.mapper.ProjectMapper;
import org.sang.mapper.ProjectMemberMapper;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;

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
}
