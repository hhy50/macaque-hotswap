package six.eared.macaque.common.type;

import six.eared.macaque.common.util.StringUtil;

public enum FileType {

    Java {
        @Override
        public boolean match(String fileType) {
            return StringUtil.isNotEmpty(fileType) && fileType.endsWith("java");
        }

        @Override
        public String getType() {
            return "java";
        }
    },

    Class {
        @Override
        public boolean match(String fileType) {
            return StringUtil.isNotEmpty(fileType) && fileType.endsWith("class");
        }

        @Override
        public String getType() {
            return "class";
        }
    },

    ;

    public abstract boolean match(String fileType);
    public abstract String getType();
}
