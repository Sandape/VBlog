package org.sang.service;

import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.bean.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

/**
 * 用户服务接口
 */
public interface IUserService extends org.springframework.security.core.userdetails.UserDetailsService {

    /**
     * 用户注册
     * @param userDTO 用户DTO
     * @return 0表示成功, 1表示用户名重复, 2表示失败
     */
    int reg(UserDTO userDTO);

    /**
     * 更新用户邮箱
     * @param email 邮箱
     * @return 影响行数
     */
    int updateUserEmail(String email);

    /**
     * 根据昵称获取用户
     * @param nickname 昵称
     * @return 用户列表
     */
    List<User> getUserByNickname(String nickname);

    /**
     * 获取所有角色
     * @return 角色列表
     */
    List<Role> getAllRole();

    /**
     * 更新用户启用状态
     * @param enabled 启用状态
     * @param uid 用户ID
     * @return 影响行数
     */
    int updateUserEnabled(Boolean enabled, Long uid);

    /**
     * 删除用户
     * @param uid 用户ID
     * @return 影响行数
     */
    int deleteUserById(Long uid);

    /**
     * 更新用户角色
     * @param rids 角色ID数组
     * @param id 用户ID
     * @return 影响行数
     */
    int updateUserRoles(Long[] rids, Long id);

    /**
     * 根据ID获取用户
     * @param id 用户ID
     * @return 用户信息
     */
    User getUserById(Long id);
}
