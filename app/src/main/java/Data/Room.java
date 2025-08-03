package Data;

import java.util.List;
import java.util.ArrayList;

/**
 * Model lớp Room, chứa thông tin phòng, ảnh và danh sách nội thất.
 */
public class Room {
    private String id;
    private String number;
    private String name;
    private int maxOccupancy;
    private String imageUri;        // URI ảnh phòng
    private List<String> interiors; // Danh sách mã nội thất ("bed", "table", ...)
    private boolean hasContract;

    // Constructor mặc định cho Firestore
    public Room() {
    }

    // Constructor đầy đủ
    public Room(String id, String number, String name, int maxOccupancy,
                String imageUri, List<String> interiors) {
        this.id = id;
        this.number = number;
        this.name = name;
        this.maxOccupancy = maxOccupancy;
        this.imageUri = imageUri;
        this.interiors = interiors;
        this.hasContract = false;
    }

    // Constructor tiện lợi, không có ảnh và nội thất mặc định rỗng
    public Room(String id, String number, String name, int maxOccupancy) {
        this(id, number, name, maxOccupancy, null, new ArrayList<>());
    }

    // === Getters & Setters ===
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }
    public void setNumber(String number) {
        this.number = number;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getMaxOccupancy() {
        return maxOccupancy;
    }
    public void setMaxOccupancy(int maxOccupancy) {
        this.maxOccupancy = maxOccupancy;
    }

    public String getImageUri() {
        return imageUri;
    }
    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public List<String> getInteriors() {
        return interiors;
    }
    public void setInteriors(List<String> interiors) {
        this.interiors = interiors;
    }
    public boolean isHasContract() { return hasContract; }
    public void setHasContract(boolean hasContract) { this.hasContract = hasContract; }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", number='" + number + '\'' +
                ", name='" + name + '\'' +
                ", maxOccupancy=" + maxOccupancy +
                ", imageUri='" + imageUri + '\'' +
                ", interiors=" + interiors +
                '}';
    }
}
