package six.eared.macaque.plugin.ideaplugin;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.MessageType;

public class ClassHotSwapAction extends AnAction {

    // 获取通知组管理器
    private NotificationGroupManager manager = NotificationGroupManager.getInstance();

    // 获取注册的通知组
    private NotificationGroup balloon = manager.getNotificationGroup("helloword.notification.balloon");

    private NotificationGroup sitckyBalloon = manager.getNotificationGroup("helloword.notification.sticky.balloon");

    private NotificationGroup toolWindow = manager.getNotificationGroup("helloword.notification.tool.window");

    private NotificationGroup none = manager.getNotificationGroup("helloword.notification.none");


    @Override
    public void actionPerformed(AnActionEvent e) {
//        Notification notify = new Notification("ToolsMenu", "success", NotificationType.IDE_UPDATE);
//        Notifications.Bus.notify(notify);
        System.out.println("helloword");
        // 使用通知组创建通知
        Notification balloonNotification = balloon.createNotification("helloword-balloon", NotificationType.INFORMATION);
        Notification sitskyBalloonNotification = sitckyBalloon.createNotification("helloword-sitskyBalloon", NotificationType.WARNING);
        Notification toolWindowNotification = toolWindow.createNotification("helloword-toolWindow", NotificationType.ERROR);
        Notification noneNotificattion = none.createNotification("helloword-none", NotificationType.INFORMATION);
        // 将通知放入通知总线
        Notifications.Bus.notify(balloonNotification);
        Notifications.Bus.notify(sitskyBalloonNotification);
        Notifications.Bus.notify(toolWindowNotification);
        Notifications.Bus.notify(noneNotificattion);
    }
}
