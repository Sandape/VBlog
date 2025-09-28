package org.sang.service;

import org.sang.bean.Notification;
import org.sang.mapper.NotificationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 通知服务类
 * 处理用户通知相关的业务逻辑
 *
 * @author sang
 * @date 2024
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    /**
     * 创建通知
     */
    public boolean createNotification(Notification notification) {
        try {
            return notificationMapper.createNotification(notification) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 创建AI解析完成通知
     */
    public void createParseCompletedNotification(Long userId, Long projectId, String tableName) {
        Notification notification = new Notification(
            userId,
            "PARSE_COMPLETED",
            "表元解析完成",
            "表 '" + tableName + "' 的AI解析已完成，您可以查看解析结果。",
            projectId,
            tableName
        );
        createNotification(notification);
    }

    /**
     * 创建AI解析失败通知
     */
    public void createParseFailedNotification(Long userId, Long projectId, String tableName, String errorMessage) {
        Notification notification = new Notification(
            userId,
            "PARSE_FAILED",
            "表元解析失败",
            "表 '" + tableName + "' 的AI解析失败：" + errorMessage,
            projectId,
            tableName
        );
        createNotification(notification);
    }

    /**
     * 获取用户未读通知数量
     */
    public int getUnreadCount(Long userId) {
        try {
            return notificationMapper.getUnreadCount(userId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    /**
     * 获取用户通知列表
     */
    public List<Notification> getUserNotifications(Long userId, int limit) {
        try {
            return notificationMapper.getUserNotifications(userId, limit);
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        }
    }

    /**
     * 标记通知为已读
     */
    public boolean markAsRead(Long notificationId, Long userId) {
        try {
            return notificationMapper.markAsRead(notificationId, userId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 批量标记所有通知为已读
     */
    public boolean markAllAsRead(Long userId) {
        try {
            return notificationMapper.markAllAsRead(userId) > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 删除通知
     */
    public int deleteNotification(Long notificationId, Long userId) {
        try {
            return notificationMapper.deleteNotification(notificationId, userId);
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }
}
