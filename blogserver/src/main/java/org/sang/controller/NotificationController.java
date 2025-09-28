package org.sang.controller;

import org.sang.bean.Notification;
import org.sang.bean.RespBean;
import org.sang.service.NotificationService;
import org.sang.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 通知控制器
 * 处理通知相关的API请求
 *
 * @author sang
 * @date 2024
 */
@RestController
@RequestMapping("/notification")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /**
     * 获取未读通知数量
     */
    @GetMapping("/unreadCount")
    public RespBean getUnreadCount() {
        try {
            Long userId = Util.getCurrentUser().getId();
            int count = notificationService.getUnreadCount(userId);
            return RespBean.success("获取成功", count);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取失败");
        }
    }

    /**
     * 获取用户通知列表
     */
    @GetMapping("/list")
    public RespBean getUserNotifications(@RequestParam(defaultValue = "20") int limit) {
        try {
            Long userId = Util.getCurrentUser().getId();
            List<Notification> notifications = notificationService.getUserNotifications(userId, limit);
            return RespBean.success("获取成功", notifications);
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("获取失败");
        }
    }

    /**
     * 标记通知为已读
     */
    @PostMapping("/markRead/{id}")
    public RespBean markAsRead(@PathVariable Long id) {
        try {
            Long userId = Util.getCurrentUser().getId();
            boolean success = notificationService.markAsRead(id, userId);
            if (success) {
                return RespBean.success("标记成功");
            } else {
                return RespBean.error("标记失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("标记失败");
        }
    }

    /**
     * 标记所有通知为已读
     */
    @PostMapping("/markAllRead")
    public RespBean markAllAsRead() {
        try {
            Long userId = Util.getCurrentUser().getId();
            boolean success = notificationService.markAllAsRead(userId);
            if (success) {
                return RespBean.success("标记成功");
            } else {
                return RespBean.error("标记失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("标记失败");
        }
    }

    /**
     * 删除通知
     */
    @DeleteMapping("/{id}")
    public RespBean deleteNotification(@PathVariable Long id) {
        try {
            Long userId = Util.getCurrentUser().getId();
            int success = notificationService.deleteNotification(id, userId);
            if (success == 1) {
                return RespBean.success("删除成功");
            } else {
                return RespBean.error("删除失败");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return RespBean.error("删除失败");
        }
    }
}
