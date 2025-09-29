package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 登录记录DTO
 * 用于封装登录记录列表数据
 */
@Getter
@Setter

public class LoginRecordDTO {
    private Long id;
    private Long userId;
    private String username;
    private String nickname;
    private String ipAddress;
    private String loginStatus;
    private Timestamp loginTime;

  public LoginRecordDTO(Long id, Long userId, String username, String nickname, String ipAddress, String loginStatus, Timestamp loginTime) {
    this.id = id;
    this.userId = userId;
    this.username = username;
    this.nickname = nickname;
    this.ipAddress = ipAddress;
    this.loginStatus = loginStatus;
    this.loginTime = loginTime;
  }
}

