package Controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.reflect.TypeToken;
import org.w3c.dom.Document;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

public class AdoptionXML_Controller {
    private static final String FILE_PATH = "res/adoptionForms.json";

    // Create the JSON file if it doesn't exist
    private static void createJSONFileIfNotExist() {
        File file = new File(FILE_PATH);
        File directory = new File("res");

        // Create "res" directory if it doesn't exist
        if (!directory.exists()) {
            directory.mkdir();
        }

        // Create JSON file with an empty array if it doesn't exist
        if (!file.exists()) {
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new Gson();
                JsonArray jsonArray = new JsonArray(); // Empty JSON array
                gson.toJson(jsonArray, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Save the given Document to the XML file
    private static void saveXML(Document document) {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            // For pretty printing
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            DOMSource source = new DOMSource(document);
            StreamResult result = new StreamResult(new File(FILE_PATH));
            transformer.transform(source, result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------------------------------------------------------
    public static boolean storeAdoptionForm(String username, String formData) {
        createJSONFileIfNotExist(); // Ensure JSON file exists
        File file = new File(FILE_PATH);
        List<Map<String, String>> formsList = new ArrayList<>();

        // Read existing JSON file
        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> existingForms = gson.fromJson(reader, listType);

            if (existingForms != null) {
                formsList = existingForms;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Create a new form entry
        Map<String, String> newForm = new HashMap<>();
        newForm.put("username", username);
        newForm.put("formData", formData); // Store form data as a JSON string

        formsList.add(newForm);

        // Save updated list back to JSON
        try (Writer writer = new FileWriter(file)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(formsList, writer);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static Vector<Vector<Object>> loadAdoptionForms() {
        createJSONFileIfNotExist(); // Ensure JSON file exists
        File file = new File(FILE_PATH);
        Vector<Vector<Object>> adoptionForms = new Vector<>();

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> formsList = gson.fromJson(reader, listType);

            if (formsList != null) {
                for (Map<String, String> form : formsList) {
                    Vector<Object> row = new Vector<>();
                    row.add(form.get("username")); // Username field
                    row.add(form.get("petId"));    // Pet ID field (if available)
                    adoptionForms.add(row);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return adoptionForms;
    }

    //------------------------------------------------------------------------------------------------------------------
    public static Vector<Vector<Object>> loadPendingAdoptionForms() {
        createJSONFileIfNotExist(); // Ensure JSON file exists
        File file = new File(FILE_PATH);
        Vector<Vector<Object>> adoptionForms = new Vector<>();

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> formsList = gson.fromJson(reader, listType);

            if (formsList != null) {
                for (Map<String, String> form : formsList) {
                    String status = form.get("status"); // Get status field

                    if ("Pending".equalsIgnoreCase(status)) { // Only add pending forms
                        Vector<Object> row = new Vector<>();
                        row.add(form.get("username")); // Username
                        row.add(form.get("petId"));    // Pet ID
                        adoptionForms.add(row);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return adoptionForms;
    }

    //------------------------------------------------------------------------------------------------------------------


    public static Vector<Vector<Object>> loadAdoptionFormsForUser(String username) {
        createJSONFileIfNotExist(); // Ensure JSON file exists
        File file = new File(FILE_PATH);
        Vector<Vector<Object>> adoptionForms = new Vector<>();

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> formsList = gson.fromJson(reader, listType);

            if (formsList != null) {
                for (Map<String, String> form : formsList) {
                    if (username.equals(form.get("username"))) { // Check if username matches
                        Vector<Object> row = new Vector<>();
                        row.add(form.get("petId"));  // ✅ First column: Pet ID
                        row.add(form.get("status")); // ✅ Second column: Status
                        adoptionForms.add(row);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return adoptionForms;
    }

    //------------------------------------------------------------------------------------------------------------------

    public static void changeAdoptionFormStatusJSON(String username, String petId, String newStatus) {
        createJSONFileIfNotExist();
        File file = new File(FILE_PATH);

        try (Reader reader = new FileReader(file)) {
            Gson gson = new Gson();
            Type listType = new TypeToken<List<Map<String, String>>>() {}.getType();
            List<Map<String, String>> formsList = gson.fromJson(reader, listType);

            boolean updated = false;

            if (formsList != null) {
                for (Map<String, String> form : formsList) {
                    if (username.equals(form.get("username")) && petId.equals(form.get("petId"))) {
                        form.put("status", newStatus); // Update the status
                        updated = true;
                        System.out.println("✅ Updated status for: " + username + " | " + petId + " | " + newStatus);
                        break;
                    }
                }
            }

            if (updated) {
                try (Writer writer = new FileWriter(file)) {
                    gson.toJson(formsList, writer); // Save updated JSON file
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
