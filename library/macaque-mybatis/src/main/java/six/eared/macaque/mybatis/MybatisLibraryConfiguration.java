package six.eared.macaque.mybatis;


import six.eared.macaque.library.annotation.Library;


@Library(name = "mybatis", hooks = MybatisXmlMapperHandler.class)
public class MybatisLibraryConfiguration {

    public static boolean PATCHED = false;

    public synchronized static void patchStrictMap() {
        if (!PATCHED) {

            PATCHED = true;
        }
    }


}
