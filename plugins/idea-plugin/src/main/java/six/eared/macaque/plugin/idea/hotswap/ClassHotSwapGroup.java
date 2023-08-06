package six.eared.macaque.plugin.idea.hotswap;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Separator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import six.eared.macaque.plugin.idea.jps.JpsHolder;

import java.util.ArrayList;
import java.util.List;


public class ClassHotSwapGroup extends ActionGroup {

    @Override
    public AnAction @NotNull [] getChildren(@Nullable AnActionEvent event) {
        Project project = event.getProject();
        JpsHolder instance = JpsHolder.getInstance(project);
        JpsHolder.State state = instance.getState();

        List<AnAction> actions = new ArrayList<>();
        for (JpsHolder.ProcessItem processItem : state.processList) {
            actions.add(new ClassHotSwapAction(processItem.pid, processItem.process));
        }

        actions.add(new Separator());
        actions.add(new RefreshJavaProcessAction());
        return actions.toArray(new AnAction[0]);
    }
}
