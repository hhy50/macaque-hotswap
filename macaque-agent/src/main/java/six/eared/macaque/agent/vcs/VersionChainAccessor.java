package six.eared.macaque.agent.vcs;

import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.common.util.FileUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class VersionChainAccessor {

    private static final VersionChain VERSION_CHAIN = new VersionChain();

    public static VersionDescriptor getLastVersion(String className) {
        return null;
    }

    public static ClazzDefinition findLastView(String className) {
        VersionDescriptor lastVd = VersionChainAccessor.getLastVersion(className);
        if (lastVd != null) {
            VersionView versionView = VersionChainAccessor.getVersionView(lastVd);
            Optional<ClazzDefinition> any = versionView.getDefinitions().stream()
                    .filter(item -> item.getClassName().equals(className)).findAny();
            if (any.isPresent()) {
                return any.get();
            }
        } else {
            try (InputStream is = ClassLoader.getSystemResourceAsStream(ClassUtil.className2path(className));) {
                if (is != null) {
                    return AsmUtil.readClass(FileUtil.is2bytes(is));
                }
            } catch (IOException e) {
                if (Environment.isDebug()) {
                    System.out.println("findLastView error");
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public synchronized static VersionView getVersionView(VersionDescriptor vd) {
        return VERSION_CHAIN.find(vd);
    }
}
