package Data;

public class Rule {
    public String id;
    public String category; // id của Category
    public int iconRes;
    public String text;

    public Rule() {}
    public Rule(String id, String category, int iconRes, String text) {
        this.id       = id;
        this.category = category;
        this.iconRes  = iconRes;
        this.text     = text;
    }
}
