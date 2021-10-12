package com.sptracer.configuration;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ServiceNameUtil {
    private static final String JAR_VERSION_SUFFIX = "-(\\d+\\.)+(\\d+)(.*)?$";

    public static String getDefaultServiceName() {
        return getDefaultServiceName(System.getProperty("sun.java.command"));
    }

    static String getDefaultServiceName(@Nullable String sunJavaCommand) {
        String serviceName = parseSunJavaCommand(sunJavaCommand);
        if (serviceName != null) {
            serviceName = replaceDisallowedChars(serviceName);
            serviceName = serviceName.trim();
        }
        if (serviceName == null || serviceName.isEmpty()) {
            serviceName = "my-service";
        }
        return serviceName;
    }

    @Nullable
    private static String parseSunJavaCommand(@Nullable String command) {
        if (command == null) {
            return null;
        }
        command = command.trim();
        String serviceName = getContainerServiceName(command);
        if (serviceName != null) {
            return serviceName;
        }
        if (command.contains(".jar")) {
            serviceName = parseJarCommand(command);
        } else {
            serviceName = parseMainClass(command);
        }
        return serviceName;
    }

    @Nullable
    private static String getContainerServiceName(String command) {
        if (command.startsWith("org.apache.catalina.startup.Bootstrap")) {
            return "tomcat-application";
        } else if (command.startsWith("org.eclipse.jetty")) {
            return "jetty-application";
        } else if (command.startsWith("com.sun.enterprise.glassfish")) {
            return "glassfish-application";
        } else if (command.contains("ws-server.jar")) {
            return "websphere-application";
        } else if (command.contains("jboss-modules.jar")) {
            return "jboss-application";
        } else if (command.contains("weblogic")) {
            return "weblogic-application";
        }
        return null;
    }

    public static String replaceDisallowedChars(String serviceName) {
        return serviceName.replaceAll("[^a-zA-Z0-9 _-]", "-");
    }

    @Nullable
    private static String parseJarCommand(String command) {
        final String[] commandParts = command.split(" ");
        String result = null;
        for (String commandPart : commandParts) {
            if (commandPart.endsWith(".jar")) {
                result = removeVersionFromJar(removePath(removeJarExtension(commandPart)));
                break;
            }
        }
        return result;
    }

    @Nonnull
    private static String removeJarExtension(String commandPart) {
        return commandPart.substring(0, commandPart.indexOf(".jar"));
    }

    private static String removePath(String path) {
        return path.substring(path.lastIndexOf("/") + 1).substring(path.lastIndexOf("\\") + 1);
    }

    private static String removeVersionFromJar(String jarFileName) {
        return jarFileName.replaceFirst(JAR_VERSION_SUFFIX, "");
    }

    private static String parseMainClass(String command) {
        final String mainClassName;
        int indexOfSpace = command.indexOf(' ');
        if (indexOfSpace != -1) {
            mainClassName = command.substring(0, indexOfSpace);
        } else {
            mainClassName = command;
        }
        return mainClassName.substring(mainClassName.lastIndexOf('.') + 1);
    }
}
