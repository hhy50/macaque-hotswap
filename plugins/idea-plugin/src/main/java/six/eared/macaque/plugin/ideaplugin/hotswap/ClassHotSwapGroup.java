package six.eared.macaque.plugin.ideaplugin.hotswap;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.ideaplugin.notify.NotifyGroupName;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;


public class ClassHotSwapGroup extends ActionGroup {

    private static Map<Integer, String> process = new HashMap <>();

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{new ClassHotSwapAction("1234 | Admin"),
                new ClassHotSwapAction("4321 | Billiards-Center"),
                new Separator(),
                new RefreshJavaProcessAction()};
    }


    public class ClassHotSwapAction extends AnAction {

        private final NotificationGroupManager manager = NotificationGroupManager.getInstance();

        private final NotificationGroup balloon = manager.getNotificationGroup(NotifyGroupName.BALLOON);

        public ClassHotSwapAction(String text) {
            super(text);
        }

        @Override
        public void actionPerformed(AnActionEvent e) {
            int confirm = Messages.showYesNoCancelDialog(
                    "This operation will replace the class already loaded in the target process",
                    "Warning", null);
            System.out.println(confirm);
            if (confirm == 0) {
                Notification notify = balloon.createNotification("success", NotificationType.INFORMATION);
                Notifications.Bus.notify(notify);
            }
        }
    }

    // 进程
    public class RefreshJavaProcessAction extends AnAction {

        public RefreshJavaProcessAction() {
            super("RefreshJavaProcess");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

        }
    }
}
