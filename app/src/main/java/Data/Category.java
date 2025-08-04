package Data;

public class Category {
    public String id;       // "hygiene", "security", ...
    public String title;    // lấy từ string resources
    public int iconRes;     // drawable resource

    public Category() {}
    public Category(String id, String title, int iconRes) {
        this.id      = id;
        this.title   = title;
        this.iconRes = iconRes;
    }
}
