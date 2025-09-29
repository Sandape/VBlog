package org.sang.bean;

import lombok.Data;
import java.sql.Date;
import lombok.Getter;
import lombok.Setter;

/**
 * 页面访问统计表实体类
 */
@Getter
@Setter
public class Pv {
    private Integer id;
    private Date countDate;
    private Integer pv;
    private Integer uid;
}
