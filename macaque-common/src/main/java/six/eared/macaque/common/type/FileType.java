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

    Xml {
        @Override
        public boolean match(String fileType) {
            return StringUtil.isNotEmpty(fileType) && fileType.endsWith("xml");
        }

        @Override
        public String getType() {
            return "xml";
        }
    },

    ;


    public static FileType ofType(String fileType) {
        for (FileType value : values()) {
            if (value.match(fileType)) {
                return value;
            }
        }
        return null;
    }

    public abstract boolean match(String fileType);
    public abstract String getType();
}
