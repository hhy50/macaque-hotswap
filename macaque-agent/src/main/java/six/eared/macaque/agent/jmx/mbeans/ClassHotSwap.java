package six.eared.macaque.agent.jmx.mbeans;

import org.objectweb.asm.ClassReader;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.type.FileType;
import six.eared.macaque.mbean.MBeanObjectName;
import six.eared.macaque.mbean.rmi.ClassHotSwapRmiData;
import six.eared.macaque.mbean.rmi.RmiResult;

import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.lang.instrument.ClassDefinition;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 热加载MBean
 */
public class ClassHotSwap implements ClassHotSwapMBean {

    /**
     * 热加载
     *
     * @param request 热加载数据
     * @return 热加载结果
     */
    @Override
    public RmiResult process(ClassHotSwapRmiData request) {
        String errMsg = null;
        Map<String, Object> result = new HashMap<>();

        String fileType = request.getFileType();
        byte[] fileData = request.getFileData();
        try {

            byte[] classData = fileData;
            if (FileType.Java.match(fileType)) {
//                classData = ;

            }

            ClassReader classReader = new ClassReader(classData);
            String className = classReader.getClassName();
            int redefineCount = redefine(className, classData);

            result.put(className, redefineCount);

            return RmiResult.success()
                    .data(result);
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("ClassHotSwap.process error");
                e.printStackTrace();
            }
        }
        return RmiResult.error(errMsg);
    }


    /**
     *
     * @param className
     * @param newClassData
     * @return
     * @throws UnmodifiableClassException
     * @throws ClassNotFoundException
     */
    private int redefine(String className, byte[] newClassData) throws UnmodifiableClassException, ClassNotFoundException {
        Instrumentation inst = Environment.getInst();
        List<ClassDefinition> needRedefineClass = new ArrayList<>();
        // 遍历所有已加载的类
        for (Class<?> clazz : inst.getAllLoadedClasses()) {
            // 如果类名相同，则加入到需要重新定义的类列表中，用于热加载
            if (clazz.getName().equals(className)) {
                needRedefineClass.add(new ClassDefinition(clazz, newClassData));
            }
        }
        if (Environment.isDebug()) {
            System.out.printf("[ClassHotSwap.process] className:[%s], find class instance count: [%d]%n", className, needRedefineClass.size());
        }
        // 重新定义类，完成热加载
        ClassDefinition[] array = needRedefineClass.toArray(new ClassDefinition[0]);
        inst.redefineClasses(array);

        return array.length;
    }

    @Override
    public ObjectName getMBeanName() throws MalformedObjectNameException {
        return new ObjectName(MBeanObjectName.HOT_SWAP_MBEAN);
    }
}
