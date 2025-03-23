package Model;


public class Pet_Model {
    private  String id;
    private  String breed;
    private  String type;
    private  String age;
    private  String healthStatus;
    private  String adoptionStatus;
    private  String imageBase64;

    public Pet_Model (String id, String breed, String type,
                      String age, String healthStatus,
                      String adoptionStatus, String imageBase64) {
        this.id = id;
        this.breed = breed;
        this.type = type;
        this.age = age;
        this.healthStatus = healthStatus;
        this.adoptionStatus = adoptionStatus;
        this.imageBase64 = imageBase64;
    }


    public String getId() {
        return id;
    }

    public String getBreed() {
        return breed;
    }

    public String getType() {
        return type;
    }

    public String getAge() {
        return age;
    }

    public String getHealthStatus() {
        return healthStatus;
    }

    public String getAdoptionStatus() {
        return adoptionStatus;
    }

    public String getImageBase64() {
        return imageBase64;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public void setHealthStatus(String healthStatus) {
        this.healthStatus = healthStatus;
    }

    public void setAdoptionStatus(String adoptionStatus) {
        this.adoptionStatus = adoptionStatus;
    }

    public void setImageBase64(String imageBase64) {
        this.imageBase64 = imageBase64;
    }


}
