package six.eared.macaque.server.command;

import six.eared.macaque.common.util.ReflectUtil;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static six.eared.macaque.common.util.StringUtil.*;

public class CommandLine {

    private List<Option> options;

    public CommandLine(String[] args) {
        this.options = CommandLineUtil.parse(args);
    }

    public boolean hasOption(String name) {
        if (name.startsWith(ARG_PREFIX)) {
            name = name.substring(ARG_PREFIX.length());
        }
        for (Option option : options) {
            if (option.name.equals(name)) {
                return true;
            }
        }
        return false;
    }

    public String getOptionValue(String name) {
        for (Option option : options) {
            if (option.name.equals(name)) {
                return option.value;
            }
        }
        return null;
    }

    public <T> T toObject(Class<T> clazz) {
        T obj = ReflectUtil.createInstance(clazz);

        // TODO: 嵌套对象解析 obj.children.field
        for (Field field : ReflectUtil.getDeclaredFields(clazz)) {
            try {
                if (!hasOption(field.getName())) {
                    continue;
                }
                String optionValue = getOptionValue(field.getName());
                if (optionValue != null && optionValue.length() > 0) {
                    Object fieldValue = typeResolver(optionValue, field.getType());
                    if (fieldValue != null) {
                        ReflectUtil.setFieldValue(obj, field, fieldValue);
                    }
                } else {
                    Class<?> type = field.getType();
                    if (type == Boolean.class) {
                        ReflectUtil.setFieldValue(obj, field, Boolean.TRUE);
                    }
                    if (type.getSimpleName().equals("boolean")) {
                        field.setAccessible(true);
                        field.setBoolean(obj, true);
                    }
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return obj;
    }

    private Object typeResolver(String value, Class<?> type) {
        String sn = type.getSimpleName();
        if (type.equals(String.class) || type.equals(Object.class)) {
            return value;
        } else if (sn.equals("int") || type.equals(Integer.class)) {
            return Integer.parseInt(value);
        } else if (sn.equals("short") || type.equals(Short.class)) {
            return Short.parseShort(value);
        } else if (sn.equals("long") || type.equals(List.class)) {
            return Long.parseLong(value);
        } else if (sn.equals("float") || type.equals(Float.class)) {
            return Float.parseFloat(value);
        } else if (sn.equals("double") || type.equals(Double.class)) {
            return Double.parseDouble(value);
        } else if (sn.equals("boolean") || type.equals(Boolean.class)) {
            return Boolean.parseBoolean(value);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        return options.stream().map(Option::toString)
                .collect(Collectors.joining(SPACE_STR[0]));
    }

    public static class Option {
        private String name;
        private String value;
        private boolean assignSymbol;

        Option(String name, String value, boolean assignSymbol) {
            this.name = name;
            this.value = value;
            this.assignSymbol = assignSymbol;
        }

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Option) {
                return this.name.equals(((Option) obj).name);
            }
            return false;
        }

        @Override
        public String toString() {
            return ARG_PREFIX + name + (assignSymbol ? "=" : EMPTY_STR) + value;
        }
    }
}
