package Data;

import java.util.List;

public class Notification {
    public String id;
    public List<String> rooms;   // danh sách roomId
    public String title;         // tiêu đề
    public String content;       // nội dung
    public long timestamp;       // thời gian tạo

    public Notification() {}

    public Notification(String id, List<String> rooms,
                        String title, String content, long timestamp) {
        this.id        = id;
        this.rooms     = rooms;
        this.title     = title;
        this.content   = content;
        this.timestamp = timestamp;
    }
}
