package Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.Base64;

public class  Image_Controller extends JPanel {

    private static String imageBase64 = "";
    private JLabel imageLabel;

    public Image_Controller() {
        this.setLayout(new BorderLayout()); // Layout for centering
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(JLabel.CENTER);
        this.add(imageLabel, BorderLayout.CENTER);
    }


    public static String selectImage(JFrame parent, JLabel imageLabel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image Files", "jpg", "jpeg", "png"));

        if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String selectedImagePath = selectedFile.getAbsolutePath();

            // Display image in JLabel
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(selectedImagePath).getImage()
                    .getScaledInstance(100, 100, Image.SCALE_SMOOTH));
            imageLabel.setIcon(imageIcon);

            // Convert image to Base64
            try {
                byte[] imageBytes = new FileInputStream(selectedFile).readAllBytes();
                String imageBase64 = Base64.getEncoder().encodeToString(imageBytes);
                return imageBase64; // Return Base64 string
            } catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent, "Error encoding image!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return null; // Return null if no image was selected
    }

    public static boolean saveImageToJSON(String username, String imageBase64) {
        try {
            File directory = new File("res");
            if (!directory.exists()) {
                directory.mkdirs(); // Create "res" directory if it doesn't exist
            }

            File file = new File(directory, username + ".json");
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            JsonObject jsonObject;

            if (file.exists()) {
                // Read existing JSON file
                try (FileReader reader = new FileReader(file)) {
                    jsonObject = gson.fromJson(reader, JsonObject.class);
                    if (jsonObject == null) {
                        jsonObject = new JsonObject();
                    }
                }
            } else {
                jsonObject = new JsonObject(); // Create new JSON object if file doesn't exist
            }

            // Update imageBase64 data
            jsonObject.addProperty("username", username);
            jsonObject.addProperty("imageBase64", imageBase64);

            // Write updated JSON to file
            try (FileWriter writer = new FileWriter(file)) {
                gson.toJson(jsonObject, writer);
            }

            return true; // Successfully saved image
        } catch (IOException e) {
            System.err.println("Error saving JSON file: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false; // Failed to save
    }

    //-------------------------------------------------------------------------------------------------------
    public static void loadImage(String username, JLabel imageLabel) {
        try {
            File file = new File("res/" + username + ".json");
            if (!file.exists()) return; // No image for this user

            Gson gson = new Gson();
            JsonObject jsonObject;

            try (FileReader reader = new FileReader(file)) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            }

            if (jsonObject != null && jsonObject.has("imageBase64")) {
                String imageBase64 = jsonObject.get("imageBase64").getAsString();

                // Decode Base64 to image
                byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
                ImageIcon imageIcon = new ImageIcon(new ImageIcon(imageBytes)
                        .getImage().getScaledInstance(175, 120, Image.SCALE_SMOOTH));

                imageLabel.setIcon(imageIcon); // Set image to JLabel
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }





    public static String loadImageFromJSON(String username) {
        try {
            File file = new File("res/" + username + ".json");
            if (!file.exists()) {
                return null; // No JSON file exists for this user
            }

            Gson gson = new Gson();
            JsonObject jsonObject;

            try (FileReader reader = new FileReader(file)) {
                jsonObject = gson.fromJson(reader, JsonObject.class);
            }

            if (jsonObject != null && jsonObject.has("imageBase64")) {
                return jsonObject.get("imageBase64").getAsString(); // Return Base64 string
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if an error occurs
    }


}

