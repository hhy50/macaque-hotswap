package six.eared.macaque.plugin.idea.hotswap;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import six.eared.macaque.plugin.idea.http.interfaces.HotSwap;
import six.eared.macaque.plugin.idea.notify.NotifyGroupName;
import six.eared.macaque.plugin.idea.settings.Settings;

public class ClassHotSwapAction extends AnAction {

    private static final NotificationGroup NOTIFY_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(NotifyGroupName.BALLOON);

    private String pid;

    public ClassHotSwapAction(String pid, String processName) {
        super(String.format("%s | %s", pid, processName));

        this.pid = pid;
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
                Settings settings = Settings.getInstance(project);
                if (settings != null) {
                    try {
                        // TODO IDEA 文件刷新有延迟
                        byte[] fileBytes = psiFile.getVirtualFile().contentsToByteArray();

                        HotSwap hotSwap = new HotSwap(settings.getState().getUrl());
                        hotSwap.setPid(pid);
                        hotSwap.setFileType("java");
                        hotSwap.setFileName(psiFile.getName());
                        hotSwap.setFileData(fileBytes);

                        hotSwap.execute((response) -> {
                            if (response.isSuccess()) {
                                Notification notify = NOTIFY_GROUP.createNotification("success", NotificationType.INFORMATION);
                                Notifications.Bus.notify(notify);
                            }
                        });
                    } catch (Exception e) {
                        Notification notify = NOTIFY_GROUP.createNotification(e.getMessage(), NotificationType.ERROR);
                        Notifications.Bus.notify(notify);
                    }
                }
            }
        }
    }
}
