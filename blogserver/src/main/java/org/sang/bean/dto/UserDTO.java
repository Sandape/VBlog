package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 用户DTO
 * 用于用户信息操作
 */
@Getter
@Setter
public class UserDTO {
    private Integer id;
    private String username;
    private String nickname;
    private String password;
    private Boolean enabled;
    private String email;
    private String userface;
    private Timestamp regTime;
}
