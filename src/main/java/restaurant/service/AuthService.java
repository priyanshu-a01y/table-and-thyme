package restaurant.service;

import restaurant.dao.UserDAO;
import restaurant.model.User;
import restaurant.util.PasswordUtil;
import restaurant.util.Session;
import java.sql.SQLException;

public class AuthService {

    private UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws SQLException {
        User user = userDAO.getUserByUsername(username);
        if (user == null) {
            return null;
        }
        
        boolean passwordMatches = PasswordUtil.verifyPassword(password, user.getPasswordHash());
        if (passwordMatches) {
            Session.setCurrentUser(user);
            return user;
        }
        
        return null;
    }

    public void logout() {
        Session.clear();
    }

    public boolean isAdmin() {
        return Session.isAdmin();
    }

    public User getCurrentUser() {
        return Session.getCurrentUser();
    }
}
