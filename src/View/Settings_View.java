package View;

import Controller.Settings_Controller;
import Model.User_Model;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

import static View.Login_Interface_View.serverAddress;
import static View.Login_Interface_View.serverPort;

public class Settings_View extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField, confirmPasswordField;
    private JLabel profilePictureLabel;
    private JButton editButton, saveButton, deleteButton, logoutButton;
    private Settings_Controller controller;
    private JLabel userImage;

    public Settings_View(Settings_Controller controller) {
        this.controller = controller;
        setTitle("Settings");
        setSize(400, 500);
        setLayout(new GridBagLayout());
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;

        JLabel userInfoLabel = new JLabel("User Info:");
        userInfoLabel.setFont(new Font("Arial", Font.BOLD, 16));
        add(userInfoLabel, gbc);

        // Username
        gbc.gridy++;
        gbc.gridwidth = 1;
        add(new JLabel("Username:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        usernameField = new JTextField();
        usernameField.setPreferredSize(new Dimension(200, 30));
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setEnabled(false);
        add(usernameField, gbc);

        // Password
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(new JLabel("Password:"), gbc);

        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.BOTH;
        passwordField = new JPasswordField();
        passwordField.setPreferredSize(new Dimension(200, 30));
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setEnabled(false);
        add(passwordField, gbc);

        // Confirm Password
        // Confirm Password Label (Initially Hidden)
        JLabel confirmPasswordLabel = new JLabel("Confirm Password:");
        confirmPasswordLabel.setVisible(false); // Hide label initially
        gbc.gridy++;
        gbc.gridx = 0;
        add(confirmPasswordLabel, gbc);

// Confirm Password Field (Initially Hidden)
        gbc.gridx = 1;
        confirmPasswordField = new JPasswordField();
        confirmPasswordField.setPreferredSize(new Dimension(200, 30));
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPasswordField.setEnabled(false);
        confirmPasswordField.setVisible(false); // Hide field initially
        add(confirmPasswordField, gbc);

// Add FocusListener to Password Field
        passwordField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                confirmPasswordField.setVisible(true); // Show only the field
            }
        });

        // Edit Button
        gbc.gridy++;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        editButton = new JButton("Edit");
        editButton.setPreferredSize(new Dimension(70, 30));
        add(editButton, gbc);
        editButton.addActionListener(e -> {
            // Enable text fields and password fields for editing
            usernameField.setEnabled(true);
            passwordField.setEnabled(true);
            confirmPasswordField.setEnabled(true);


            // Show save button and hide edit button
            saveButton.setVisible(true);
            editButton.setVisible(false);
            confirmPasswordField.setVisible(true);
        });

        // Save Button (Initially Hidden)
        gbc.gridy++;
        saveButton = new JButton("Confirm Changes");
        saveButton.setPreferredSize(new Dimension(70, 30));
        saveButton.setVisible(false);
        add(saveButton, gbc);

        // Space before delete button
        gbc.gridy++;
        add(Box.createRigidArea(new Dimension(0, 20)), gbc);

        // Delete Account Button
        // Delete Account Button
        gbc.gridy++;
        deleteButton = new JButton("Delete Account");
        deleteButton.setForeground(Color.RED);
        deleteButton.setPreferredSize(new Dimension(70, 30));
        add(deleteButton, gbc);

// Logout Button (added below Delete Account)
        gbc.gridy++;  // Move to next row
        logoutButton = new JButton("Logout And Exit");
        logoutButton.setPreferredSize(new Dimension(90, 30));
        logoutButton.addActionListener(e -> {
            logoutButton.setEnabled(false);
            new Thread(() -> {
                String username = usernameField.getText().trim();
                if (username.isEmpty()) {
                    System.err.println("Client: Username is empty. Cannot logout.");
                    return;
                }
                try (Socket socket = new Socket(serverAddress, serverPort);
                     PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)) {

                    // Send logout command to server
                    out.println("Logout");
                    out.println(username);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // After sending the logout request, clear all session-related data
                SwingUtilities.invokeLater(() -> {
                    // Close all open windows
                    for (Window window : Window.getWindows()) {
                        window.dispose(); // Dispose of all open windows
                    }

                    // Clear username and other session details
                    usernameField.setText("");   // Clear username field
                    passwordField.setText("");   // Clear password field
                    confirmPasswordField.setText("");   // Clear confirm password field


                    System.exit(0);
                });
            }).start();
        });

        add(logoutButton, gbc);

        saveButton.addActionListener(e -> {
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match!", "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Call the controller method to update the user settings
            controller.updateUserSettings(usernameField.getText(), password);

            // Hide Save button and show Edit button again
            saveButton.setVisible(false);
            editButton.setVisible(true);

            // Disable the fields again after saving
            usernameField.setEnabled(false);
            passwordField.setEnabled(false);
            confirmPasswordField.setEnabled(false);

            // Hide the Confirm Password field again
            confirmPasswordField.setVisible(false);
        });


        // Delete Button Action
        deleteButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to delete your account?", "Confirm Deletion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                controller.deleteAccount(usernameField.getText().trim());
                dispose();
                SwingUtilities.invokeLater(() -> controller.openLoginInterfaceView());
            }
        });

        // Show Save Button Only When Fields Are Edited
        KeyAdapter keyAdapter = new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                saveButton.setVisible(true);
            }
        };

        usernameField.addKeyListener(keyAdapter);
        passwordField.addKeyListener(keyAdapter);
        confirmPasswordField.addKeyListener(keyAdapter);
    }

    /**
     * Updates user details and sets the profile image based on the user model.
     */
    public void setUserDetails(User_Model user) {
        usernameField.setText(user.getUsername());
        passwordField.setText(user.getPassword());
        // Fetch the image Base64 from the XML file using the username.
        String xmlImage = Controller.Image_Controller.loadImageFromJSON(user.getUsername());
    }

}
