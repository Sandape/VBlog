package org.sang.controller;

import org.sang.bean.Project;
import org.sang.bean.ProjectMember;
import org.sang.bean.RespBean;
import org.sang.bean.TableMetaData;
import org.sang.service.ProjectService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 项目控制器
 */
@RestController
@RequestMapping("/project")
public class ProjectController {

    @Autowired
    private ProjectService projectService;

    /**
     * 创建项目
     */
    @PostMapping("/create")
    public RespBean createProject(@RequestBody Project project) {
        int result = projectService.createProject(project);
        switch (result) {
            case 0:
                return RespBean.success("项目创建成功！项目编码：" + project.getProjectCode(), project);
            case 1:
                return RespBean.error("项目名称已存在！");
            default:
                return RespBean.error("项目创建失败！");
        }
    }

    /**
     * 通过项目编码加入项目
     */
    @PostMapping("/join")
    public RespBean joinProject(@RequestBody java.util.Map<String, String> request) {
        String projectCode = request.get("projectCode");
        int result = projectService.joinProject(projectCode);
        switch (result) {
            case 0:
                return RespBean.success("成功加入项目！");
            case 1:
                return RespBean.error("项目不存在或已被删除！");
            case 2:
                return RespBean.error("您已经是该项目的成员！");
            default:
                return RespBean.error("加入项目失败！");
        }
    }

    /**
     * 获取项目详情
     */
    @GetMapping("/detail/{id}")
    public RespBean getProjectDetail(@PathVariable Long id) {
        Project project = projectService.getProjectDetail(id);
        if (project != null) {
            return RespBean.success("获取成功", project);
        }
        return RespBean.error("项目不存在或无权限访问！");
    }

    /**
     * 更新项目信息（仅项目拥有者）
     */
    @PutMapping("/update")
    public RespBean updateProject(@RequestBody Project project) {
        int result = projectService.updateProject(project);
        switch (result) {
            case 0:
                return RespBean.success("项目更新成功！");
            case 1:
                return RespBean.error("无权限操作！");
            default:
                return RespBean.error("项目更新失败！");
        }
    }

    /**
     * 删除项目（仅项目拥有者）
     */
    @DeleteMapping("/delete/{id}")
    public RespBean deleteProject(@PathVariable Long id) {
        int result = projectService.deleteProject(id);
        switch (result) {
            case 0:
                return RespBean.success("项目删除成功！");
            case 1:
                return RespBean.error("无权限操作！");
            default:
                return RespBean.error("项目删除失败！");
        }
    }

    /**
     * 获取用户参与的项目列表
     */
    @GetMapping("/my-projects")
    public RespBean getUserProjects() {
        List<Project> projects = projectService.getUserProjects();
        return RespBean.success("获取成功", projects);
    }

    /**
     * 获取用户拥有的项目列表
     */
    @GetMapping("/owned-projects")
    public RespBean getOwnedProjects() {
        List<Project> projects = projectService.getOwnedProjects();
        return RespBean.success("获取成功", projects);
    }

    /**
     * 获取用户所有项目列表（参与的+拥有的）
     */
    @GetMapping("/list")
    public RespBean getAllUserProjects() {
        List<Project> projects = projectService.getUserProjects();
        return RespBean.success("获取成功", projects);
    }

    /**
     * 搜索项目
     */
    @GetMapping("/search")
    public RespBean searchProjects(@RequestParam String projectName) {
        List<Project> projects = projectService.searchProjects(projectName);
        return RespBean.success("搜索完成", projects);
    }

    /**
     * 退出项目
     */
    @PostMapping("/leave/{id}")
    public RespBean leaveProject(@PathVariable Long id) {
        int result = projectService.leaveProject(id);
        switch (result) {
            case 0:
                return RespBean.success("成功退出项目！");
            case 1:
                return RespBean.error("您不是该项目的成员！");
            case 2:
                return RespBean.error("项目拥有者不能退出项目！");
            default:
                return RespBean.error("退出项目失败！");
        }
    }

    /**
     * 获取项目成员列表
     */
    @GetMapping("/members/{id}")
    public RespBean getProjectMembers(@PathVariable Long id) {
        List<ProjectMember> members = projectService.getProjectMembers(id);
        if (members != null) {
            return RespBean.success("获取成功", members);
        }
        return RespBean.error("无权限查看项目成员！");
    }

    /**
     * 移除项目成员（仅项目拥有者）
     */
    @DeleteMapping("/remove-member")
    public RespBean removeMember(@RequestParam Long projectId, @RequestParam Long memberId) {
        int result = projectService.removeMember(projectId, memberId);
        switch (result) {
            case 0:
                return RespBean.success("成员移除成功！");
            case 1:
                return RespBean.error("无权限操作！");
            case 2:
                return RespBean.error("不能移除自己！");
            default:
                return RespBean.error("移除成员失败！");
        }
    }

    /**
     * 根据项目编码获取项目基本信息（用于加入前预览）
     */
    @GetMapping("/preview/{projectCode}")
    public RespBean previewProject(@PathVariable String projectCode) {
        Project project = projectService.getProjectByCode(projectCode);
        if (project != null) {
            // 只返回基本信息，隐藏敏感信息
            Project previewProject = new Project();
            previewProject.setProjectName(project.getProjectName());
            previewProject.setOwnerNickname(project.getOwnerNickname());
            previewProject.setMemberCount(project.getMemberCount());
            return RespBean.success("获取成功", previewProject);
        }
        return RespBean.error("项目不存在！");
    }

    /**
     * 添加SQL表元
     * @param projectId 项目ID
     * @param request 请求体，包含sql字段
     */
    @PostMapping("/{projectId}/sql-tables")
    public RespBean addSqlTable(@PathVariable Long projectId, @RequestBody Map<String, String> request) {
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
            return RespBean.error("SQL语句不能为空！");
        }

        int result = projectService.addOrUpdateSqlTable(projectId, sql.trim());
        switch (result) {
            case 0:
                return RespBean.success("SQL表元添加成功！");
            case 1:
                return RespBean.error("无权限操作！");
            case 2:
                return RespBean.error("SQL语句无效！");
            case 3:
                return RespBean.error("项目AI配置缺失！");
            case 4:
                return RespBean.error("AI解析失败！");
            default:
                return RespBean.error("添加SQL表元失败！");
        }
    }

    /**
     * 获取项目SQL表元列表
     * @param projectId 项目ID
     */
    @GetMapping("/{projectId}/sql-tables")
    public RespBean getProjectSqlTables(@PathVariable Long projectId) {
        Map<String, TableMetaData> sqlTables = projectService.getProjectSqlTables(projectId);
        if (sqlTables == null) {
            return RespBean.error("无权限查看项目SQL表元！");
        }
        return RespBean.success("获取成功", sqlTables);
    }

    /**
     * 编辑SQL表元
     * @param projectId 项目ID
     * @param tableName 表名
     * @param request 请求体，包含sql字段
     */
    @PutMapping("/{projectId}/sql-tables/{tableName}")
    public RespBean updateSqlTable(@PathVariable Long projectId, @PathVariable String tableName,
                                   @RequestBody Map<String, String> request) {
        String sql = request.get("sql");

        if (sql == null || sql.trim().isEmpty()) {
            return RespBean.error("SQL语句不能为空！");
        }

        int result = projectService.updateSqlTable(projectId, tableName, sql.trim());
        switch (result) {
            case 0:
                return RespBean.success("SQL表元编辑成功！");
            case 1:
                return RespBean.error("无权限操作！");
            case 2:
                return RespBean.error("SQL语句无效！");
            case 3:
                return RespBean.error("表元不存在！");
            case 4:
                return RespBean.error("项目AI配置缺失！");
            case 5:
                return RespBean.error("AI解析失败！");
            default:
                return RespBean.error("编辑SQL表元失败！");
        }
    }

    /**
     * 删除SQL表元（仅项目拥有者）
     * @param projectId 项目ID
     * @param tableName 表名
     */
    @DeleteMapping("/{projectId}/sql-tables/{tableName}")
    public RespBean deleteSqlTable(@PathVariable Long projectId, @PathVariable String tableName) {
        int result = projectService.deleteSqlTable(projectId, tableName);
        switch (result) {
            case 0:
                return RespBean.success("SQL表元删除成功！");
            case 1:
                return RespBean.error("无权限操作！仅项目拥有者可以删除表元！");
            case 2:
                return RespBean.error("表元不存在！");
            default:
                return RespBean.error("删除SQL表元失败！");
        }
    }
}
