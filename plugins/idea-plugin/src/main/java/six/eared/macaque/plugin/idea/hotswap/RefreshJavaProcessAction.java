package six.eared.macaque.plugin.idea.hotswap;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import six.eared.macaque.plugin.idea.jps.JpsHolder;

public class RefreshJavaProcessAction extends AnAction {

    public RefreshJavaProcessAction() {
        super("Refresh");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        JpsHolder.refresh(project);
    }
}