package Model;

public class ImageSelectionResult_Model {
    private final String base64Image;
    private final String filePath;

    public ImageSelectionResult_Model(String base64Image, String filePath) {
        this.base64Image = base64Image;
        this.filePath = filePath;
    }

    public String getBase64Image() {
        return base64Image;
    }

    public String getFilePath() {
        return filePath;
    }
}
