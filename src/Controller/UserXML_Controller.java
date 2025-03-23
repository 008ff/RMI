package Controller;

import Model.User_Model;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UserXML_Controller {
    private static final String FILE_PATH = "res/users.json";

    public static void createJSONFileIfNotExist() {
        File file = new File(FILE_PATH);
        File directory = new File("res");

        try {
            // Ensure directory exists
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // Ensure JSON file exists
            if (!file.exists()) {
                file.createNewFile(); // Create an empty file
                List<Object> emptyList = new ArrayList<>(); // Create an empty JSON array
                try (FileWriter writer = new FileWriter(file)) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(emptyList, writer); // Write empty array to JSON
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

// -------------------------------JSON ---------------------------------------------------------------------------------


    public static boolean addUser(String username, String password, String role) {
        // Validate input
        if (username == null || username.trim().isEmpty() ||
                password == null || password.trim().isEmpty() ||
                role == null || role.trim().isEmpty()) {
            return false; // Invalid data
        }

        File file = new File(FILE_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<Map<String, String>> users = new ArrayList<>();

        try {
            // Ensure directory exists
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            // Read existing users if the file exists
            if (file.exists()) {
                try (FileReader reader = new FileReader(file)) {
                    Type listType = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
                    Map<String, List<Map<String, String>>> data = gson.fromJson(reader, listType);
                    if (data != null) {
                        users = data.get("users"); // Get the existing users list
                    }
                } catch (JsonSyntaxException e) {
                    System.err.println("Warning: Corrupt JSON file, creating a new list.");
                }
            }

            // Check if the user already exists
            boolean userExists = false;
            for (Map<String, String> user : users) {
                if (user.get("username").equals(username)) {
                    // Update existing user
                    user.put("password", password);
                    user.put("role", role);
                    userExists = true;
                    System.out.println("User updated: " + username);
                    break;
                }
            }

            // If user doesn't exist, add new user
            if (!userExists) {
                Map<String, String> newUser = new HashMap<>();
                newUser.put("username", username.trim());
                newUser.put("password", password.trim());
                newUser.put("role", role.trim());
                users.add(newUser);
                System.out.println("New user added: " + username);
            }

            // Write updated list to file
            try (FileWriter writer = new FileWriter(file)) {
                Map<String, List<Map<String, String>>> data = new HashMap<>();
                data.put("users", users);
                gson.toJson(data, writer);
            }

            return true;

        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
//------------------------------------------------------------------------------------------------------------------

    public static String validateUser(String username, String password) {
        createJSONFileIfNotExist(); // Ensure file exists

        File file = new File(FILE_PATH);
        if (!file.exists()) {
            return null; // No users exist yet
        }

        try (FileReader reader = new FileReader(file)) {
            Gson gson = new Gson();
            // The root is an object with a "users" key pointing to a list of users
            Type type = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            Map<String, List<Map<String, String>>> data = gson.fromJson(reader, type);

            if (data != null) {
                List<Map<String, String>> users = data.get("users");

                if (users != null) {
                    for (Map<String, String> user : users) {
                        if (username.equals(user.get("username")) && password.equals(user.get("password"))) {
                            return user.get("role"); // Return role if credentials match
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace(); // Handle IOException, e.g., file read errors
            return "Error reading the file.";
        } catch (JsonSyntaxException e) {
            e.printStackTrace(); // Handle JSON format issues
            return "Error: Invalid JSON format in the user data.";
        }

        return null; // Return null if no match found
    }

    //------------------------------------------------------------------------------------------------------------------

    public static boolean deleteUser(String username) {
        File file = new File(FILE_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        List<User_Model> userList = new ArrayList<>();

        try {
            // Check if file exists
            if (!file.exists()) {
                System.out.println("User database not found.");
                return false;
            }

            // Read existing users from JSON
            try (FileReader reader = new FileReader(file)) {
                Type listType = new TypeToken<List<User_Model>>() {}.getType();
                userList = gson.fromJson(reader, listType);
                if (userList == null) {
                    userList = new ArrayList<>();
                }
            } catch (JsonSyntaxException e) {
                System.err.println("Warning: Corrupt JSON file, creating a new list.");
                userList = new ArrayList<>();
            }

            // Remove user if found
            boolean userDeleted = userList.removeIf(user -> user.getUsername().equals(username));

            // If user was deleted, update the JSON file
            if (userDeleted) {
                try (FileWriter writer = new FileWriter(file)) {
                    gson.toJson(userList, writer);
                }
                System.out.println("User " + username + " deleted successfully from JSON.");
                return true;
            } else {
                System.out.println("User " + username + " not found in JSON.");
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    //------------------------------------------------------------------------------------------------------------------

    // Did not do the JSON yet
    public static boolean updateAdoptionFormStatus(String username, String petId, String newStatus) {
        createJSONFileIfNotExist();
        try {
            File xmlFile = new File(FILE_PATH);
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(xmlFile);
            document.getDocumentElement().normalize();

            NodeList formList = document.getElementsByTagName("AdoptionForm");
            boolean updated = false;

            for (int i = 0; i < formList.getLength(); i++) {
                Node node = formList.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element formElem = (Element) node;
                    String formUsername = formElem.getAttribute("username");
                    String formPetId = formElem.getElementsByTagName("PetID").item(0).getTextContent();

                    // Find the matching adoption form
                    if (formUsername.equals(username) && formPetId.equals(petId)) {
                        // Update the status
                        formElem.getElementsByTagName("Status").item(0).setTextContent(newStatus);
                        updated = true;
                        break;
                    }
                }
            }

            if (updated) {
                // Save the updated XML file
                TransformerFactory transformerFactory = TransformerFactory.newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                DOMSource source = new DOMSource(document);
                StreamResult result = new StreamResult(xmlFile);
                transformer.transform(source, result);
                System.out.println("Adoption form for " + username + " (Pet ID: " + petId + ") updated to " + newStatus);
                return true;
            } else {
                System.out.println("Adoption form not found for user: " + username + " and Pet ID: " + petId);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


}
