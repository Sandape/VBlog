package org.sang.service.impl;

import org.sang.bean.Role;
import org.sang.bean.User;
import org.sang.bean.dto.UserDTO;
import org.sang.config.MyPasswordEncoder;
import org.sang.mapper.RolesMapper;
import org.sang.mapper.UserMapper;
import org.sang.service.IUserService;
import org.sang.utils.Util;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 用户服务实现类
 */
@Service
@Transactional
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RolesMapper rolesMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userMapper.loadUserByUsername(username);
        if (user == null) {
            return new User();
        }
        // 查询用户的角色信息
        List<Role> roles = rolesMapper.getRolesByUid(user.getId());
        user.setRoles(roles);
        return user;
    }

    @Override
    public int reg(UserDTO userDTO) {
        User loadUserByUsername = userMapper.loadUserByUsername(userDTO.getUsername());
        if (loadUserByUsername != null) {
            return 1; // 用户名重复
        }

        User user = new User();
        BeanUtils.copyProperties(userDTO, user);

        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setEnabled(true);

        long result = userMapper.reg(user);

        // 配置用户的角色，默认都是普通用户
        String[] roles = new String[]{"2"};
        int i = rolesMapper.addRoles(roles, user.getId());
        boolean b = i == roles.length && result == 1;

        if (b) {
            return 0; // 成功
        } else {
            return 2; // 失败
        }
    }

    @Override
    public int updateUserEmail(String email) {
        return userMapper.updateUserEmail(email, Util.getCurrentUser().getId());
    }

    @Override
    public List<User> getUserByNickname(String nickname) {
        return userMapper.getUserByNickname(nickname);
    }

    @Override
    public List<Role> getAllRole() {
        return userMapper.getAllRole();
    }

    @Override
    public int updateUserEnabled(Boolean enabled, Long uid) {
        return userMapper.updateUserEnabled(enabled, uid);
    }

    @Override
    public int deleteUserById(Long uid) {
        return userMapper.deleteUserById(uid);
    }

    @Override
    public int updateUserRoles(Long[] rids, Long id) {
        int i = userMapper.deleteUserRolesByUid(id);
        return userMapper.setUserRoles(rids, id);
    }

    @Override
    public User getUserById(Long id) {
        return userMapper.getUserById(id);
    }
}
