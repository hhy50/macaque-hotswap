package six.eared.macaque.plugin.idea.hotswap;

import com.intellij.notification.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.notify.NotifyGroupName;

import java.util.HashMap;
import java.util.Map;


public class ClassHotSwapGroup extends ActionGroup {

    private static Map<Integer, String> process = new HashMap<>();

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
        return new AnAction[]{
                new ClassHotSwapAction("1234", "Admin"),
                new ClassHotSwapAction("4321", "Billiards-Center"),
                new Separator(),
                new RefreshJavaProcessAction()}
                ;
    }

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
                    System.out.println("Right-clicked file: " + psiFile.toString());
                }
                Notification notify = balloon.createNotification("success", NotificationType.INFORMATION);
                Notifications.Bus.notify(notify);
            }
        }
    }

    public class RefreshJavaProcessAction extends AnAction {

        public RefreshJavaProcessAction() {
            super("Refresh");
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {

        }
    }
}
