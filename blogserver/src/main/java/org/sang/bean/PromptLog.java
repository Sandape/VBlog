package org.sang.bean;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * AI解读请求日志表实体类
 */
@Getter
@Setter
public class PromptLog {
    private Long id;
    private Long projectId;
    private Long userId;
    private String apiName;
    private String apiPath;
    private String apiDesc;
    private String apiRequest;
    private String apiResponse;
    private String apiSql;
    private String aiResponse;
    private String finalPrompt;
    private Timestamp createTime;
    private Timestamp updateTime;
    private String username;
    private String userNickname;
    private String projectName;
}
