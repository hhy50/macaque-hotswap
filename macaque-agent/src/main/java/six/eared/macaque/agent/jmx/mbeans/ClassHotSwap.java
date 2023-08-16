package six.eared.macaque.agent.jmx.mbeans;

import six.eared.macaque.agent.asm2.classes.ClazzDefinition;
import six.eared.macaque.agent.asm2.classes.MultiClassReader;
import six.eared.macaque.agent.compiler.java.JavaSourceCompiler;
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
import java.util.*;

/**
 * 热加载MBean
 */
public class ClassHotSwap implements ClassHotSwapMBean {

    private final JavaSourceCompiler compiler = new JavaSourceCompiler();

    /**
     * 热加载
     *
     * @param request 热加载数据
     * @return 热加载结果
     */
    @Override
    public RmiResult process(ClassHotSwapRmiData request) {
        String errMsg = null;

        String fileType = request.getFileType();
        byte[] fileData = request.getFileData();
        try {

            List<byte[]> classDataList = Arrays.asList(fileData);
            if (FileType.Java.match(fileType)) {
                String fileName = request.getFileName();

                Map<String, String> sources = new HashMap<>();
                sources.put(fileName, new String(fileData));

                classDataList = compiler.compile(sources);
            }

            Map<String, Object> result = new HashMap<>();
            for (byte[] classData : classDataList) {
                MultiClassReader classReader = new MultiClassReader(classData);
                Iterator<ClazzDefinition> iterator = classReader.iterator();
                while (iterator.hasNext()) {
                    ClazzDefinition clazzDefinition = iterator.next();
//                    String className = classReader.getClassName().replaceAll("/", ".");
                    int redefineCount = redefine(clazzDefinition.getClassName(), classData);
                    result.put(clazzDefinition.getClassName(), redefineCount);
                }
            }

            return RmiResult.success()
                    .data(result);
        } catch (Exception e) {
            if (Environment.isDebug()) {
                System.out.println("ClassHotSwap.process error");
                e.printStackTrace();
            }
            errMsg = e.getMessage();
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
