//package com.hhy.attach;
//
//
//import com.sun.tools.attach.VirtualMachine;
//import com.sun.tools.attach.VirtualMachineDescriptor;
//
//import java.io.IOException;
//import java.util.List;
//import java.util.Properties;
//
//public class AttachMain {
//    public void premain() {
//
//    }
//    public static void main(String[] args) {
//        Integer myPid = 5160;
//        List<VirtualMachineDescriptor> list = VirtualMachine.list();
//        VirtualMachineDescriptor virtualMachineDescriptor = null;
//        for (VirtualMachineDescriptor descriptor : VirtualMachine.list()) {
//            String pid = descriptor.id();
//            if (pid.equals(Integer.toString(myPid))) {
//                virtualMachineDescriptor = descriptor;
//                break;
//            }
//        }
//        System.out.println(virtualMachineDescriptor);
//        VirtualMachine virtualMachine = null;
//        try {
//            if (null == virtualMachineDescriptor) { // 使用 attach(String pid) 这种方式
//                virtualMachine = VirtualMachine.attach("" + configure.getJavaPid());
//            } else {
//                virtualMachine = VirtualMachine.attach(virtualMachineDescriptor);
//            }
//
//            Properties targetSystemProperties = virtualMachine.getSystemProperties();
//            String targetJavaVersion = JavaVersionUtils.javaVersionStr(targetSystemProperties);
//            String currentJavaVersion = JavaVersionUtils.javaVersionStr();
//            if (targetJavaVersion != null && currentJavaVersion != null) {
//                if (!targetJavaVersion.equals(currentJavaVersion)) {
//                    AnsiLog.warn("Current VM java version: {} do not match target VM java version: {}, attach may fail.",
//                            currentJavaVersion, targetJavaVersion);
//                    AnsiLog.warn("Target VM JAVA_HOME is {}, arthas-boot JAVA_HOME is {}, try to set the same JAVA_HOME.",
//                            targetSystemProperties.getProperty("java.home"), System.getProperty("java.home"));
//                }
//            }
//
//            String arthasAgentPath = configure.getArthasAgent();
//            //convert jar path to unicode string
//            configure.setArthasAgent(encodeArg(arthasAgentPath));
//            configure.setArthasCore(encodeArg(configure.getArthasCore()));
//            try {
//                virtualMachine.loadAgent(arthasAgentPath,
//                        configure.getArthasCore() + ";" + configure.toString());
//            } catch (IOException e) {
//                if (e.getMessage() != null && e.getMessage().contains("Non-numeric value found")) {
//                    AnsiLog.warn(e);
//                    AnsiLog.warn("It seems to use the lower version of JDK to attach the higher version of JDK.");
//                    AnsiLog.warn(
//                            "This error message can be ignored, the attach may have been successful, and it will still try to connect.");
//                } else {
//                    throw e;
//                }
//            } catch (com.sun.tools.attach.AgentLoadException ex) {
//                if ("0".equals(ex.getMessage())) {
//                    // https://stackoverflow.com/a/54454418
//                    AnsiLog.warn(ex);
//                    AnsiLog.warn("It seems to use the higher version of JDK to attach the lower version of JDK.");
//                    AnsiLog.warn(
//                            "This error message can be ignored, the attach may have been successful, and it will still try to connect.");
//                } else {
//                    throw ex;
//                }
//            }
//        } finally {
//            if (null != virtualMachine) {
//                virtualMachine.detach();
//            }
//        }
//
//    }
//}
