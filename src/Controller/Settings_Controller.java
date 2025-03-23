package Controller;

import Model.User_Model;
import View.Admin_Pet_Profile_View;
import View.Login_Interface_View;
import View.Settings_View;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.*;
import Controller.Image_Controller;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.lang.reflect.Type;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class Settings_Controller {
    private User_Model model;
    private Settings_View view;
    private String serverAddress;
    private int serverPort;


    private static final String FILE_PATH = "res/users.json";

    public Settings_Controller(String username, String role , String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        String password = getPasswordFromJSON(username); // Your existing logic
        this.model = new User_Model(username, password, role);
        this.view = new Settings_View(this);
        this.view.setUserDetails(model);
        this.view.setVisible(true);
    }


    private String getPasswordFromJSON(String username) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Error: users.json not found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
                return null;
            }

            Gson gson = new Gson();
            FileReader reader = new FileReader(file);

            // Define the root structure to contain "users" key holding the array of users
            Type dataType = new TypeToken<Map<String, List<User_Model>>>() {}.getType();
            Map<String, List<User_Model>> data = gson.fromJson(reader, dataType);
            reader.close();

            // Get the list of users from the "users" key
            List<User_Model> users = data.get("users");

            // Iterate through the users and return the password if the username matches
            if (users != null) {
                for (User_Model user : users) {
                    if (user.getUsername().equals(username)) {
                        return user.getPassword(); // Return password if username matches
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null; // Return null if username not found
    }
    //------------------------------------------------------------------------------------------------------------------

    public void updateUserSettings(String newUsername, String newPassword) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Error: users.json not found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader reader = new FileReader(file);

            // Check if the JSON is an object with a "users" field
            Map<String, List<Map<String, String>>> data = null;
            try {
                Type listType = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
                data = gson.fromJson(reader, listType);
            } catch (JsonSyntaxException e) {
                // If it's not in the expected format, try parsing as an array
                reader.close();
                reader = new FileReader(file);
                Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
                List<Map<String, String>> users = gson.fromJson(reader, listType);
                data = new HashMap<>();
                data.put("users", users);
            } finally {
                reader.close();
            }

            List<Map<String, String>> users = data.get("users");
            if (users != null) {
                for (Map<String, String> user : users) {
                    if (user.get("username").equals(model.getUsername())) {
                        user.put("username", newUsername);
                        user.put("password", newPassword);
                        model.setUsername(newUsername);
                        model.setPassword(newPassword);
                        break;
                    }
                }
            }

            // Write the updated JSON back to the file
            FileWriter writer = new FileWriter(file);
            gson.toJson(data, writer);
            writer.close();

            JOptionPane.showMessageDialog(view, "User settings updated successfully!");

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Failed to update settings!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    public void deleteAccount(String trim) {
        try {
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                JOptionPane.showMessageDialog(null, "Error: users.json not found!", "File Not Found", JOptionPane.ERROR_MESSAGE);
                return;
            }

            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            FileReader reader = new FileReader(file);
            Type listType = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            Map<String, List<Map<String, String>>> data = gson.fromJson(reader, listType);
            reader.close();

            List<Map<String, String>> users = data.get("users");
            boolean userDeleted = false;

            if (users != null) {
                Iterator<Map<String, String>> iterator = users.iterator();
                while (iterator.hasNext()) {
                    Map<String, String> user = iterator.next();
                    if (user.get("username").equals(model.getUsername())) {
                        iterator.remove(); // Remove the user from the list
                        userDeleted = true;
                        break;
                    }
                }
            }

            if (userDeleted) {
                // Write the updated JSON back to the file
                FileWriter writer = new FileWriter(file);
                gson.toJson(data, writer);
                writer.close();

                JOptionPane.showMessageDialog(view, "Account deleted successfully!");

                // Close the settings window and open the login window

                view.dispose();
                openLoginInterfaceView();
                view.dispose();

            } else {
                JOptionPane.showMessageDialog(view, "Account not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }

        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(view, "Failed to delete account!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    //------------------------------------------------------------------------------------------------------------------



    public void openLoginInterfaceView() {

        SwingUtilities.invokeLater(() -> {
            Login_Interface_View loginInterfaceView = new Login_Interface_View(serverAddress, serverPort);
                    loginInterfaceView.setVisible(true);
                    loginInterfaceView.setLocationRelativeTo(null);
        });
    }

    public void uploadProfileImage(String username, String imageBase64) {
        try (Socket socket = new Socket(Login_Interface_View.serverAddress, Login_Interface_View.serverPort);
             PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {

            // Send command and data
            out.println("upload-profile-image");
            out.println(username);

            // Read server response
            String response = in.readLine();
            System.out.println("Server response: " + response);
            if (!"Image uploaded successfully".equalsIgnoreCase(response)) {
                JOptionPane.showMessageDialog(null, "Error uploading profile image on server!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to server for image upload: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void saveImageToXML(String username, String imageBase64) {
        try {
            // Ensure the resource directory exists.
            File directory = new File("res");
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Use a separate file for each user.
            File file = new File(directory, username + ".xml");

            // Create or parse the XML document.
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document doc;
            Element rootElement;

            if (file.exists()) {
                doc = docBuilder.parse(file);
                rootElement = doc.getDocumentElement();
                // Overwrite the existing content with the new Base64 image.
                rootElement.setTextContent(imageBase64);
            } else {
                doc = docBuilder.newDocument();
                rootElement = doc.createElement("image");
                rootElement.appendChild(doc.createTextNode(imageBase64));
                doc.appendChild(rootElement);
            }

            // Write the XML document to the file with indentation.
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);
            StreamResult result = new StreamResult(file);
            transformer.transform(source, result);

            System.out.println("Profile image saved successfully for user: " + username);
        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error saving image to XML for user: " + username);
        }
    }


    public void handleLogout(String username) {
        try (Socket socket = new Socket(serverAddress, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            System.out.println("Client: Logout button clicked. Logging out user: " + username);

            // Send logout request to the server
            out.println("Logout");
            out.println(username);
            out.flush();

            // Read server response
            String response = in.readLine();
            System.out.println("Client: Server response -> " + response);

            System.out.println("Client: Sent logout command for user: " + username);

            // Close the settings window and open login screen
            view.dispose();
            openLoginInterfaceView();

        } catch (IOException e) {
            System.out.println("Client: Error sending logout request - " + e.getMessage());
        }
    }
    public static String loadImageFromXML(String username) {
        try {
            File file = new File("res/" + username + ".xml");
            if (!file.exists()) {
                return null; // No XML file exists for this user
            }
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(file);
            doc.getDocumentElement().normalize();

            // Assuming your XML file has a root element <image> containing the Base64 string
            Element root = doc.getDocumentElement();
            return root.getTextContent();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}