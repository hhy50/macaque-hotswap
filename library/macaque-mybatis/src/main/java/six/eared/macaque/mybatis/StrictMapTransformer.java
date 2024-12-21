package six.eared.macaque.mybatis;

import io.github.hhy50.linker.LinkerFactory;
import io.github.hhy50.linker.define.MethodDescriptor;
import io.github.hhy50.linker.exceptions.LinkerException;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import six.eared.macaque.agent.asm2.AsmUtil;
import six.eared.macaque.agent.asm2.classes.ClassVisitorDelegation;
import six.eared.macaque.agent.env.Environment;
import six.eared.macaque.library.patch.MethodPatchWriter;
import six.eared.macaque.mybatis.mapping.MybatisStrictMap;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Map;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class StrictMapTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        String classPath = "org/apache/ibatis/session/Configuration$StrictMap";
        if (!className.equals(classPath)) {
            return new byte[0];
        }

        ClassWriter classWriter = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES);
        AsmUtil.visitClass(classfileBuffer, new ClassVisitorDelegation(classWriter) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                MethodVisitor methodVisitor = super.visitMethod(access, name, descriptor, signature, exceptions);
                if (name.equals("put")) {
                    Method bootstrap = null;
                    try {
                        MethodPatchWriter.patchMethod(methodVisitor, Type.getType(descriptor), false,
                                MethodDescriptor.of(StrictMapTransformer.class.getDeclaredMethod("put", Map.class, Object.class, Object.class)));
                    } catch (Exception e) {
                        if (Environment.isDebug()) {
                            e.printStackTrace();
                        }
                    }
                    return null;
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
