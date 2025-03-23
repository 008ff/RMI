package View;
import Controller.Image_Controller;
import Controller.PetsXML_Controller;
import Controller.Settings_Controller;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.Socket;
import java.util.Base64;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

import static Controller.Image_Controller.*;
import static Controller.PetsXML_Controller.loadPets;

public class AdminDashboardView extends javax.swing.JFrame {
    private static String serverAddress;
    private static int serverPort;
    private static String healthStatus;

    private JLabel userImage;
    static String username;
    static String role;

    private static Object image;
    private static String imageBase64;
    private Model.ImageSelectionResult_Model selectedImageResult;
    private static int id;
    private static String breed;
    private static String type;
    private static int age;
    /**
     * Creates new form Practice
     */
    public AdminDashboardView(String username, int id, String breed, String type, int age, String healthStatus, Object image, String serverAddress, int serverPort) {
        this.username = username;
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
        this.image = image;
        this.id = id;
        this.healthStatus = healthStatus;
        this.breed = breed;
        this.type = type;
        this.age = age;
        userImage = new JLabel();
        userImage.setBounds(32, 5, 150, 150);
        add(userImage);
        loadImage(username,userImage);

        initComponents();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">
    private void initComponents() {

        jScrollPane2 = new javax.swing.JScrollPane();
        jTable = new javax.swing.JTable();
        user_picture_Label = new javax.swing.JLabel();
        addPetProfile_Button = new javax.swing.JButton();
        searchField = new java.awt.TextField();
        deletePetProfile_Button = new javax.swing.JButton();
        settings_Button = new javax.swing.JButton();
        admin_Label = new javax.swing.JLabel();
        name_Label = new javax.swing.JLabel();
        adoptionForms = new javax.swing.JButton();
        refresh_Button = new javax.swing.JButton();
        petAutoPic = new javax.swing.JLabel();
        profilePicButton = new javax.swing.JButton();
        comboBox = new javax.swing.JComboBox<>();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        // Table
        jTable.setBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)));
        jTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        tableModel = new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Type", "Breed", "AdoptionStatus", "Age", "Health Status"}) {

            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jTable = new JTable(tableModel);

        new Thread(() -> {
            try {
                Socket socket = new Socket(serverAddress, serverPort);
                PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                writer.println("load-pets");
                writer.flush();
                Thread.sleep(100);

                Vector<Vector<Object>> petsData = new Vector<>();
                Vector<Object> currentRow = new Vector<>();

                String line;
                while ((line = reader.readLine()) != null) {
                    if ("end".equalsIgnoreCase(line)) {
                        break;
                    }
                    currentRow.add(line);
                    if (currentRow.size() == 6) {
                        petsData.add(currentRow);
                        currentRow = new Vector<>();
                    }
                }

                socket.close();

                SwingUtilities.invokeLater(() -> {
                    displayData(petsData);
                });

            } catch (IOException | InterruptedException ex) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(this, "Error loading pet data from server.", "Error", JOptionPane.ERROR_MESSAGE);
                });
                ex.printStackTrace();
            }
        }).start();

        jTable.setRowHeight(50);
        jTable.setShowGrid(true);
        jTable.setShowHorizontalLines(true);
        jTable.setShowVerticalLines(true);
        jTable.setGridColor(Color.black);
        jTable.setBackground(Color.lightGray);
        jTable.getTableHeader().setReorderingAllowed(false);


        jTable.getColumnModel().getColumn(4).setMinWidth(0);
        jTable.getColumnModel().getColumn(4).setMaxWidth(0);
        jTable.getColumnModel().getColumn(4).setWidth(0);

        jTable.getColumnModel().getColumn(5).setMinWidth(0);
        jTable.getColumnModel().getColumn(5).setMaxWidth(0);
        jTable.getColumnModel().getColumn(5).setWidth(0);


        jTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    JTable target = (JTable) e.getSource();
                    int row = target.getSelectedRow();

                    if (row != -1) {
                        // Table Display
                        String id = target.getValueAt(row, 0).toString();
                        String type = target.getValueAt(row, 1).toString();
                        String breed = target.getValueAt(row, 2).toString();
                        String adoptionStatus = target.getValueAt(row, 3).toString();
                        String age = target.getValueAt(row, 4).toString();
                        String healthStatus = target.getValueAt(row, 5).toString();

                        SwingUtilities.invokeLater(() -> {
                            new Admin_Pet_Profile_View(username, id, breed, type, age, healthStatus, adoptionStatus, image, serverAddress, serverPort)
                                    .setVisible(true);
                        });

                    }
                }
            }
        });

        // One Click to Display
        jTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int selectedRow = jTable.getSelectedRow();
                if (selectedRow != -1) {
                    String id = jTable.getValueAt(selectedRow, 0).toString();

                    new Thread(() -> {
                        try {
                            Socket socket = new Socket(serverAddress, serverPort);
                            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                            writer.println("pet-icon");
                            writer.flush();
                            Thread.sleep(100);

                            writer.println(id);
                            writer.flush();

                            String imageBase64 = reader.readLine();

                            if (imageBase64 != null && !"not-found".equalsIgnoreCase(imageBase64)) {
                                byte[] imageBytes = Base64.getDecoder().decode(imageBase64);

                                ImageIcon icon = new ImageIcon(imageBytes);
                                Image image = icon.getImage();


                                Image scaledImage = image.getScaledInstance(200, 100, Image.SCALE_SMOOTH);
                                ImageIcon scaledIcon = new ImageIcon(scaledImage);

                                SwingUtilities.invokeLater(() -> {
                                    petAutoPic.setIcon(scaledIcon);
                                });
                            }else {
                                SwingUtilities.invokeLater(() -> {
                                    petAutoPic.setIcon(null);
                                    JOptionPane.showMessageDialog(null, "Image not found for pet ID: " + id);
                                });
                            }
                            socket.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(null, "Error fetching image from server.");
                            });
                        }
                    }).start();
                }
            }
        });


        jScrollPane2.setViewportView(jTable);

        addPetProfile_Button.setText("Add Pet Profile");
        addPetProfile_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPetProfile_ButtonActionPerformed(evt);
            }
        });

        searchField.setFont(new java.awt.Font("Courier New", 0, 12)); // NOI18N
        searchField.setForeground(new java.awt.Color(102, 0, 0));
        searchField.setText("Search");

        final TableRowSorter<DefaultTableModel> sorter = new TableRowSorter<>(tableModel);
        jTable.setRowSorter(sorter);

        searchField.addTextListener(new java.awt.event.TextListener() {
            @Override
            public void textValueChanged(java.awt.event.TextEvent evt) {
                String text = searchField.getText();
                if (text.trim().length() == 0) {
                    sorter.setRowFilter(null);  // Show all rows when search field is empty
                } else {
                    // Create a case-insensitive regex filter.
                    sorter.setRowFilter(javax.swing.RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(text)));
                }
            }
        });
        searchField.addFocusListener(new java.awt.event.FocusAdapter() {
            @Override
            public void focusGained(java.awt.event.FocusEvent evt) {
                if ("Search".equals(searchField.getText())) {
                    searchField.setText("");
                }
            }
        });

        deletePetProfile_Button.setText("Delete Pet Profile");
        deletePetProfile_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deletePetProfile_ButtonActionPerformed(evt);
            }
        });

        settings_Button.setText("Settings");
        settings_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                settings_ButtonActionPerformed(evt);
            }
        });

        admin_Label.setText("ADMIN");

        name_Label.setText(username);

        adoptionForms.setText("Adoption Forms");
        adoptionForms.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                adoptionFormsActionPerformed(evt);
            }


        });

        refresh_Button.setText("Refresh");
        refresh_Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refresh_ButtonActionPerformed(evt);
            }
        });

        profilePicButton.setBackground(new java.awt.Color(255, 255, 204));
        profilePicButton.setFont(new java.awt.Font("Segoe UI", 1, 24)); // NOI18N
        profilePicButton.setForeground(new java.awt.Color(0, 0, 51));
        profilePicButton.setText("+");
        profilePicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                profilePictureActionPerformed(evt);
            }
        });

        user_picture_Label.setBackground(new java.awt.Color(204, 204, 204));
        user_picture_Label.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        user_picture_Label.setBorder(javax.swing.BorderFactory.createEtchedBorder(new java.awt.Color(51, 255, 255), new java.awt.Color(204, 255, 255)));

        petAutoPic.setBorder(javax.swing.BorderFactory.createMatteBorder(10, 10, 10, 10, new java.awt.Color(0, 255, 255)));


        comboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Default", "Adopted", "Not Adopted"  }));
        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterPets();
            }

            private void filterPets() {
                String selectedStatus = (String) comboBox.getSelectedItem();

                // Ensure selectedStatus is not null
                if (selectedStatus == null) return;

                // Create a RowFilter based on the selected status
                RowFilter<DefaultTableModel, Object> rf = null;
                try {
                    if (selectedStatus.equals("Default")) {
                        // Show all rows (if "Default" is selected)
                        rf = RowFilter.regexFilter(".*");  // Matches all rows
                    } else {
                        // Filter rows based on the status in the 4th column (index 3)
                        rf = RowFilter.regexFilter(selectedStatus, 3);  // Column 3 is where the status is assumed to be
                    }
                } catch (java.util.regex.PatternSyntaxException e) {
                    System.err.println("Invalid regex pattern");
                }

                // Apply the RowFilter to the TableRowSorter
                sorter.setRowFilter(rf);
            }
        });


        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGap(15, 15, 15)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(deletePetProfile_Button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(addPetProfile_Button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(adoptionForms, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 214, Short.MAX_VALUE)
                                                        .addComponent(refresh_Button, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addGap(0, 53, Short.MAX_VALUE))
                                        .addGroup(layout.createSequentialGroup()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addComponent(name_Label)
                                                        .addComponent(admin_Label)
                                                        .addGroup(layout.createSequentialGroup()
                                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                        .addComponent(user_picture_Label, javax.swing.GroupLayout.DEFAULT_SIZE, 182, Short.MAX_VALUE)
                                                                        .addComponent(petAutoPic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(profilePicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, 209, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(92, 92, 92)
                                                .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(settings_Button))
                                        .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 617, Short.MAX_VALUE))
                                .addGap(39, 39, 39))
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                        .addGroup(layout.createSequentialGroup()
                                                .addGap(32, 32, 32)
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(searchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                                                .addComponent(settings_Button)
                                                                .addComponent(comboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE))
                                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                                                .addContainerGap()
                                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                                        .addComponent(user_picture_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 137, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                        .addComponent(profilePicButton, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                                                .addGap(14, 14, 14)
                                                .addComponent(admin_Label)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(name_Label)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addComponent(petAutoPic, javax.swing.GroupLayout.PREFERRED_SIZE, 115, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addGap(47, 47, 47)
                                                .addComponent(refresh_Button)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(adoptionForms)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(addPetProfile_Button)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(deletePetProfile_Button)))
                                .addGap(17, 17, 17))
        );

        pack();
    }

    private void refresh_ButtonActionPerformed(java.awt.event.ActionEvent evt) {

        SwingUtilities.invokeLater(() -> {
            AdminDashboardView adminDashboardView = new AdminDashboardView(username, id, breed, type, age, healthStatus, image, serverAddress, serverPort);
            adminDashboardView.setVisible(true);
            adminDashboardView.setLocationRelativeTo(null);
            dispose();
        });
    }

    private void deletePetProfile_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        int selectedRow = jTable.getSelectedRow();

        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a pet to delete!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String petID = jTable.getValueAt(selectedRow, 0).toString();
        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete this pet?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            try (
                    Socket socket = new Socket(serverAddress, serverPort);
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            ) {
                writer.println("delete-pet");
                writer.println(petID);
                writer.flush();

                String response = reader.readLine();
                if ("Pet deleted successfully".equalsIgnoreCase(response)) {
                    ((DefaultTableModel) jTable.getModel()).removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Pet deleted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete pet!", "Error", JOptionPane.ERROR_MESSAGE);
                }

            } catch (IOException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error connecting to server.", "Connection Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    // User profile Picture
    private void profilePictureActionPerformed(java.awt.event.ActionEvent evt) {
        String imageBase64 = selectImage(this, user_picture_Label);

        if (imageBase64 != null) {
            // Convert Base64 to image bytes
            byte[] imageBytes = Base64.getDecoder().decode(imageBase64);
            ImageIcon imageIcon = new ImageIcon(new ImageIcon(imageBytes)
                    .getImage().getScaledInstance(200, 200, java.awt.Image.SCALE_SMOOTH));

            // Set the image to the label
            user_picture_Label.setIcon(imageIcon);

            // Save the image to XML
            saveImageToJSON(username, imageBase64);
        }
    }


    private void settings_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        String username = name_Label.getText(); // Get the username from the label
        String serverAddress = this.serverAddress; // or get from a field / passed parameter
        int serverPort = this.serverPort; // or get from a field / passed parameter

        // Open Settings, passing the username, server address, and server port.
        new Settings_Controller(username, role ,serverAddress, serverPort);

    }


    private void adoptionFormsActionPerformed(java.awt.event.ActionEvent evt) {

        SwingUtilities.invokeLater(() -> {
            AdoptionForms_View adoptionForms_View = new AdoptionForms_View(username, id, breed, type, age, healthStatus, image, serverAddress, serverPort);
            adoptionForms_View.setVisible(true);

        });
    }
    private void addPetProfile_ButtonActionPerformed(java.awt.event.ActionEvent evt) {
        openPetRegistrationDialog();
    }
    private void openPetRegistrationDialog() {
        JDialog petRegistration = new JDialog(this, "Pet Registration", true);
        petRegistration.getContentPane().setBackground(new Color(55, 234, 234, 192));
        petRegistration.setSize(400, 400);
        petRegistration.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Labels and Fields
        JLabel idLabel = new JLabel("Enter ID:");
        JTextField idField = new JTextField(15);
        JLabel breedLabel = new JLabel("Enter Breed:");
        JTextField breedField = new JTextField(15);
        JLabel typeLabel = new JLabel("Enter Type:");
        JTextField typeField = new JTextField(15);
        JLabel ageLabel = new JLabel("Enter Age:");
        JTextField ageField = new JTextField(15);

        JLabel healthLabel = new JLabel("Select Health Status:");
        String[] healthStatuses = {"Healthy", "Not Healthy"};
        JComboBox<String> healthBox = new JComboBox<>(healthStatuses);

        JLabel adoptionLabel = new JLabel("Select Adoption Status:");
        String[] adoptionStatuses = {"Adopted", "Not Adopted"};
        JComboBox<String> adoptionBox = new JComboBox<>(adoptionStatuses);

        // Image Selection
        JPanel imagePanel = new JPanel();
        JButton selectImageButton = new JButton("Select Image");
        JLabel imageLabel = new JLabel();
        imageLabel.setPreferredSize(new Dimension(100, 100));
        imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
        imagePanel.add(selectImageButton);
        imagePanel.add(imageLabel);
        selectImageButton.addActionListener(e -> {
            imageBase64 = selectImage(this, imageLabel);
            if (imageBase64 == null) {
                JOptionPane.showMessageDialog(this, "Please select an image!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        // Buttons
        JButton registerButton = new JButton("Register");
        JButton cancelButton = new JButton("Cancel");

        registerButton.addActionListener(e -> {
            String id = idField.getText().trim();
            String breed = breedField.getText().trim();
            String type = typeField.getText().trim();
            String age = ageField.getText().trim();
            String health = (String) healthBox.getSelectedItem();
            String adoption = (String) adoptionBox.getSelectedItem();

            if (id.isEmpty() || breed.isEmpty() || type.isEmpty() || age.isEmpty() || imageBase64 == null) {
                JOptionPane.showMessageDialog(petRegistration, "All fields are required!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Integer.parseInt(age);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(petRegistration, "Age must be a number!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try (Socket socket = new Socket(serverAddress, serverPort);
                 PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

                // Send data to the server
                writer.println("add-pet");
                writer.println(id);
                writer.println(breed);
                writer.println(type);
                writer.println(age);
                writer.println(health);
                writer.println(adoption);
                writer.println(imageBase64);

                // Read server response
                String serverResponse = reader.readLine();
                JOptionPane.showMessageDialog(petRegistration, serverResponse, "Server Response", JOptionPane.INFORMATION_MESSAGE);

                if ("Pet Registration Successful!".equals(serverResponse)) {
                    // After clicking OK on the message, dispose current dialog...
                    petRegistration.dispose();
                    dispose();
                    loadPets();  // Refresh the table
                    // ...and then reopen a fresh add pet dialog.
                    SwingUtilities.invokeLater(() -> openPetRegistrationDialog());
                }
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(petRegistration, "Error connecting to server.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> petRegistration.dispose());

        // Layout for dialog
        gbc.gridx = 0; gbc.gridy = 0; petRegistration.add(idLabel, gbc);
        gbc.gridx = 1; petRegistration.add(idField, gbc);
        gbc.gridx = 0; gbc.gridy = 1; petRegistration.add(breedLabel, gbc);
        gbc.gridx = 1; petRegistration.add(breedField, gbc);
        gbc.gridx = 0; gbc.gridy = 2; petRegistration.add(typeLabel, gbc);
        gbc.gridx = 1; petRegistration.add(typeField, gbc);
        gbc.gridx = 0; gbc.gridy = 3; petRegistration.add(ageLabel, gbc);
        gbc.gridx = 1; petRegistration.add(ageField, gbc);
        gbc.gridx = 0; gbc.gridy = 4; petRegistration.add(healthLabel, gbc);
        gbc.gridx = 1; petRegistration.add(healthBox, gbc);
        gbc.gridx = 0; gbc.gridy = 5; petRegistration.add(adoptionLabel, gbc);
        gbc.gridx = 1; petRegistration.add(adoptionBox, gbc);
        gbc.gridx = 0; gbc.gridy = 6; gbc.gridwidth = 2; petRegistration.add(imagePanel, gbc);
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2; petRegistration.add(registerButton, gbc);
        gbc.gridx = 1; petRegistration.add(cancelButton, gbc);

        petRegistration.setLocationRelativeTo(this);
        petRegistration.setVisible(true);
    }


    private void displayData(Vector<Vector<Object>> data) {
        tableModel.setRowCount(0);
        for (Vector<Object> row : data) {
            tableModel.addRow(row);
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(AdminDashboardView.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new AdminDashboardView(username, id, serverAddress, type, serverPort, healthStatus, image, serverAddress, serverPort).setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JComboBox<String> comboBox;
    private javax.swing.JButton profilePicButton;
    private javax.swing.JLabel petAutoPic;
    private javax.swing.JButton refresh_Button;
    private DefaultTableModel tableModel;
    private javax.swing.JButton addPetProfile_Button;
    private javax.swing.JLabel admin_Label;
    private javax.swing.JButton adoptionForms;
    private javax.swing.JButton deletePetProfile_Button;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable;
    private javax.swing.JLabel name_Label;
    private javax.swing.JLabel user_picture_Label;
    private java.awt.TextField searchField;
    private javax.swing.JButton settings_Button;

    // End of variables declaration
}
