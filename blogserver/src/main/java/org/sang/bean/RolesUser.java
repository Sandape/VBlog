package org.sang.bean;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户角色关联表实体类
 */
@Getter
@Setter
public class RolesUser {
    private Integer id;
    private Integer rid;
    private Integer uid;
}
