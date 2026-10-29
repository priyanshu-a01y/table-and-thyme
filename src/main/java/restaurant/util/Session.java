package restaurant.util;

import restaurant.model.User;

public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static boolean isAdmin() {
        return currentUser != null && currentUser.getRole() == User.Role.ADMIN;
    }

    public static void clear() {
        currentUser = null;
    }
}
