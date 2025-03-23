package Model;

import Controller.UserXML_Controller;

public class UserDatabase_Model {
    public static boolean addUser(String username, String password, String role) {
        return UserXML_Controller.addUser(username, password, role);
    }

    public static String validateUser(String usrname, String password) {
        return UserXML_Controller.validateUser(usrname, password);
    }
}
