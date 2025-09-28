package org.sang.bean;

/**
 * 统一响应结果封装类
 * Created by sang on 2017/12/17.
 * Updated to support generic data return
 */
public class RespBean {
    private String status;
    private String msg;
    private Object obj; // 新增数据字段，用于返回具体的业务数据

    public RespBean() {
    }

    public RespBean(String status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    public RespBean(String status, String msg, Object obj) {
        this.status = status;
        this.msg = msg;
        this.obj = obj;
    }

    // 静态工厂方法，便于创建成功响应
    public static RespBean success(String msg) {
        return new RespBean("success", msg);
    }

    public static RespBean success(String msg, Object obj) {
        return new RespBean("success", msg, obj);
    }

    // 静态工厂方法，便于创建错误响应
    public static RespBean error(String msg) {
        return new RespBean("error", msg);
    }

    public static RespBean error(String msg, Object obj) {
        return new RespBean("error", msg, obj);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }
}
