package six.eared.macaque.plugin.idea.http.response;

import java.util.List;

public class JpsResponse extends CommonResponse {

    private List<Item> data;

    public List<Item> getData() {
        return data;
    }

    public void setData(List<Item> data) {
        this.data = data;
    }

    public static class Item {

        String pid;

        String process;

        public String getPid() {
            return pid;
        }

        public void setPid(String pid) {
            this.pid = pid;
        }

        public String getProcess() {
            return process;
        }

        public void setProcess(String process) {
            this.process = process;
        }

        @Override
        public String toString() {
            return "Item{" +
                    "process='" + process + '\'' +
                    ", pid='" + pid + '\'' +
                    '}';
        }
    }
}
