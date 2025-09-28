package org.sang.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.sang.bean.Notification;

import java.util.List;

/**
 * 通知Mapper接口
 */
@Mapper
public interface NotificationMapper {

    /**
     * 创建通知
     */
    int createNotification(Notification notification);

    /**
     * 获取用户未读通知数量
     */
    int getUnreadCount(@Param("userId") Long userId);

    /**
     * 获取用户通知列表
     */
    List<Notification> getUserNotifications(@Param("userId") Long userId, @Param("limit") int limit);

    /**
     * 标记通知为已读
     */
    int markAsRead(@Param("id") Long id, @Param("userId") Long userId);

    /**
     * 批量标记通知为已读
     */
    int markAllAsRead(@Param("userId") Long userId);

    /**
     * 删除通知
     */
    int deleteNotification(@Param("id") Long id, @Param("userId") Long userId);
}
