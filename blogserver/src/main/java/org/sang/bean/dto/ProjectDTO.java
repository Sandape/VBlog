package org.sang.bean.dto;

import lombok.Data;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

/**
 * 项目DTO
 * 用于项目的增删改查操作
 */
@Getter
@Setter
public class ProjectDTO {
    private Long id;
    private String projectName;
    private String projectCode;
    private String developmentSpec;
    private String apiKey;
    private String apiUrl;
    private String modelName;
    private String exampleMapperPath;
    private String exampleEntityPath;
    private String exampleInterfacePath;
    private Long ownerId;
    private Timestamp createTime;
    private Timestamp updateTime;
    private Boolean status;
    private String sqlList;
}
