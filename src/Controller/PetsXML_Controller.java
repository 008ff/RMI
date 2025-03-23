package Controller;

import Model.Pet_Model;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Vector;

public class PetsXML_Controller extends Component {

    private JTable jTable;
    private String serverAddress;
    private int serverPort;


    // Overloaded constructor that accepts a server address and port.
    public PetsXML_Controller(JTable petTable, String serverAddress, int serverPort) {
        this.jTable = petTable;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    //------------------------------------------------------------------------------------------------------------------
    public static Vector<Vector<Object>> loadPets() {
        File file = new File("res/pets.json");
        Vector<Vector<Object>> data = new Vector<>();
        Gson gson = new Gson();

        try {
            // If file doesn't exist, return empty data
            if (!file.exists()) return data;

            // Read JSON file
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Pet_Model>>() {}.getType();
                List<Pet_Model> petList = gson.fromJson(reader, listType);

                // Convert each Pet object to Vector<Object>
                for (Pet_Model pet : petList) {
                    Vector<Object> row = new Vector<>();
                    row.add(pet.getId());
                    row.add(pet.getType());
                    row.add(pet.getBreed());
                    row.add(pet.getAdoptionStatus());
                    row.add(pet.getAge());
                    row.add(pet.getHealthStatus());

                    data.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return data;
    }

    // -----------------------------------------------------------------------------------------------------------------

    public static void savePet(Pet_Model pet) {
        File file = new File("res/pets.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Pet_Model> petList = new ArrayList<>();

        try {
            // Ensure directory exists
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            // Read existing pets
            if (Files.exists(file.toPath())) {
                try (FileReader reader = new FileReader(file)) {
                    Type listType = new TypeToken<List<Pet_Model>>() {}.getType();
                    petList = gson.fromJson(reader, listType);
                    if (petList == null) {
                        petList = new ArrayList<>();
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Warning: Corrupt JSON file, creating a new list.");
                    petList = new ArrayList<>();
                }
            }

            // Add new pet
            petList.add(pet);

            // Write updated list
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(petList, writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    // -----------------------------------------------------------------------------------------------------------------



    /**
     * Deletes a pet with the given petID from the JSON file.
     *
     * @param petID
     * @return true if deletion was successful, false otherwise.
     */
    public static boolean deletePet(String petID) {
        File file = new File("res/pets.json");
        Gson gson = new Gson();

        try {
            // If file doesn't exist, return false
            if (!file.exists()) {
                System.out.println("Error: pets.json does not exist!");
                return false;
            }

            // Read the JSON file
            List<Pet_Model> petList;
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Pet_Model>>() {}.getType();
                petList = gson.fromJson(reader, listType);

                if (petList == null) petList = new ArrayList<>();
            }

            // Find and remove the pet
            boolean deleted = petList.removeIf(pet -> pet.getId().equals(petID));

            // If pet was deleted, write updated list back to JSON file
            if (deleted) {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(petList, writer);
                }
                System.out.println("Pet with ID " + petID + " deleted successfully.");
                return true;
            } else {
                System.out.println("Pet with ID " + petID + " not found.");
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //------------------------------------------------------------------------------------------------------------------


    public static boolean updatePet(Pet_Model updatedPet) {
        File file = new File("res/pets.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Pet_Model> petList = new ArrayList<>();

        try {
            // Ensure file exists
            if (!file.exists()) {
                System.out.println("Error: pets.json does not exist!");
                return false;
            }

            // Read existing data
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Pet_Model>>() {}.getType();
                petList = gson.fromJson(reader, listType);
                if (petList == null) petList = new ArrayList<>();
            }

            // Update the pet details
            boolean found = false;
            for (Pet_Model pet : petList) {
                if (pet.getId().equals(updatedPet.getId())) {
                    pet.setBreed(updatedPet.getBreed());
                    pet.setType(updatedPet.getType());
                    pet.setAge(updatedPet.getAge());
                    pet.setHealthStatus(updatedPet.getHealthStatus());
                    pet.setAdoptionStatus(updatedPet.getAdoptionStatus());
                    pet.setImageBase64(updatedPet.getImageBase64());
                    found = true;
                    break;
                }
            }

            // If pet was updated, save back to file
            if (found) {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(petList, writer);
                }
                System.out.println("Pet updated successfully: " + updatedPet.getId());
                return true;
            } else {
                System.out.println("Pet not found: " + updatedPet.getId());
                return false;
            }

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public static ImageIcon loadPetImage(String petID) {
        File file = new File("res/pets.json");
        Gson gson = new Gson();

        try {
            // Ensure file exists
            if (!file.exists()) {
                System.out.println("Error: pets.json does not exist!");
                return null;
            }

            // Read JSON file
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<Pet_Model>>() {}.getType();
                List<Pet_Model> petList = gson.fromJson(reader, listType);

                if (petList == null) return null;

                // Find pet by ID
                for (Pet_Model pet : petList) {
                    if (pet.getId().equals(petID)) {
                        return decodeBase64ToImage(pet.getImageBase64());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null; // Return null if pet not found
    }

    //-----------------------------------------------------------------------------------------------------------------

    // Converts Base64 string to ImageIcon
    private static ImageIcon decodeBase64ToImage(String base64Image) {
        if (base64Image == null || base64Image.isEmpty() || base64Image.equals("N/A")) {
            return null;
        }

        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image); // Decode the Base64 image
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            Image image = ImageIO.read(bis);

            if (image == null) {
                return null;
            }

            // Resize image to fit the label size (you can adjust these dimensions as needed)
            int labelWidth = 150;  // Adjust as per your label width
            int labelHeight = 150; // Adjust as per your label height

            Image resizedImage = image.getScaledInstance(labelWidth, labelHeight, Image.SCALE_SMOOTH);
            return new ImageIcon(resizedImage);

        } catch (IllegalArgumentException e) {
            System.err.println("Invalid Base64 image format: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Error decoding image: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Unexpected error during image processing: " + e.getMessage());
        }

        return null;
    }


    //------------------------------------------------------------------------------------------------------------------

    public static Pet_Model getPetDetails(String petId) {
        try {
            File file = new File("res/pets.json");
            if (!file.exists()) {
                System.err.println("Error: pets.json file not found.");
                return null;
            }

            // Read and parse JSON file
            Gson gson = new Gson();
            FileReader reader = new FileReader(file);
            List<Pet_Model> petList = gson.fromJson(reader, new TypeToken<List<Pet_Model>>() {}.getType());
            reader.close();

            // Search for the pet by ID
            for (Pet_Model pet : petList) {
                if (pet.getId().equals(petId)) {
                    return pet; // Return the matched pet
                }
            }
        } catch (Exception ex) {
            System.err.println("Error retrieving pet details: " + ex.getMessage());
        }
        return null; // Return null if no pet matches the given ID
    }

    //------------------------------------------------------------------------------------------------------------------


    // Helper method to retrieve the text content of a tag
    static String getTagValue(Element element, String tagName) {
        NodeList list = element.getElementsByTagName(tagName);
        if (list != null && list.getLength() > 0) {
            return list.item(0).getTextContent().trim();
        }
        return "";
    }




    //-------------------------------------------------------------------------------------------------------------------
    public static String getPetImageBase64(String petID) {
        try {
            File file = new File("res/pets.json");
            if (!file.exists()) return null;

            // Read and parse JSON file
            Gson gson = new Gson();
            FileReader reader = new FileReader(file);
            List<Pet_Model> petList = gson.fromJson(reader, new TypeToken<List<Pet_Model>>() {}.getType());
            reader.close();

            // Search for the pet by ID
            for (Pet_Model pet : petList) {
                if (pet.getId().equals(petID)) {
                    return pet.getImageBase64(); // Return the Base64 image string
                }
            }
        } catch (Exception e) {
            System.err.println("Error retrieving pet image: " + e.getMessage());
        }
        return null; // Return null if pet not found
    }


}
