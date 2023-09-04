package six.eared.macaque.agent.vcs;

public class VersionChainAccessor {


    private static final VersionChain VERSION_CHAIN = new VersionChain();

    public static VersionDescriptor getLastVersion(String clazzName) {
        return null;
    }

    public synchronized static VersionView getVersionView(VersionDescriptor vd) {
        return VERSION_CHAIN.find(vd);
    }
}
