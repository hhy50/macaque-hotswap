package six.eared.macaque.mybatis;

import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.define.MethodDescriptor;
import io.github.hhy50.linker.exceptions.LinkerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import six.eared.macaque.agent.asm2.AsmMethod;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClassVisitorDelegation;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.common.util.ClassUtil;
import six.eared.macaque.library.patch.MethodPatchWriter;
import six.eared.macaque.mybatis.mapping.MybatisStrictMap;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class StrictMapTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String classPath, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!classPath.equals("org/apache/ibatis/session/Configuration$StrictMap")) {
            return new byte[0];
        }

        ClassWriter classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        AsmUtil.visitClass(classfileBuffer, new ClassVisitorDelegation(classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("put")) {
                    try {
                        AsmMethod asmMethod = AsmMethod.AsmMethodBuilder
                                .builder()
                                .modifier(access)
                                .methodName(name)
                                .desc(descriptor)
                                .build();
                        methodVisitor = MethodPatchWriter.patchMethod(ClassUtil.classpath2name(classPath), methodVisitor, asmMethod,
                                MethodDescriptor.of(StrictMapTransformer.class.getDeclaredMethod("put", Map.class, Object.class, Object.class)));
                    } catch (Exception e) {
                        if (Environment.isDebug()) {
                            e.printStackTrace();
                        }
                    }
                }
                return methodVisitor;
            }
        });
        return classWriter.toByteArray();
    }

    public static Object put(Map strictMap, Object key, Object value) throws LinkerException {
        return LinkerFactory.createLinker(MybatisStrictMap.class, strictMap).put(key, value);
    }
}
