package six.eared.macaque.plugin.idea.hotswap;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import six.eared.macaque.plugin.idea.notify.NotifyGroupName;

import java.io.IOException;

public class ClassHotSwapAction extends AnAction {

//        private final NotificationGroupManager manager = NotificationGroup.findRegisteredGroup().getInstance();

    private final NotificationGroup balloon = NotificationGroup.findRegisteredGroup(NotifyGroupName.BALLOON);

    public ClassHotSwapAction(String pid, String processName) {
        super(String.format("%s | %s", pid, processName));
    }

    @Override
    public void actionPerformed(AnActionEvent event) {
        int confirm = Messages.showYesNoCancelDialog(
                "This operation will replace the class already loaded in the target process",
                "Warning", null);
        if (confirm == 0) {
            Project project = getEventProject(event);
            // 获取右击的文件
            PsiFile psiFile = event.getDataContext().getData(CommonDataKeys.PSI_FILE);
            if (psiFile != null) {
                psiFile.getFileElementType();
                try {
                    byte[] bytes = psiFile.getVirtualFile().contentsToByteArray();
                    System.out.println(new String(bytes));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            Notification notify = balloon.createNotification("success", NotificationType.INFORMATION);
            Notifications.Bus.notify(notify);
        }
    }
}
