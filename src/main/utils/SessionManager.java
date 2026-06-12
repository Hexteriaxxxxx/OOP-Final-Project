package main.utils;

import models.User;
import java.util.function.BiConsumer;

/**
 * SessionManager — Global singleton that persists user info across all panels.
 *
 * Set once on login:
 *   SessionManager.setCurrentUser(user)
 *
 * Apply in any controller's initialize():
 *   SessionManager.apply(this::initSession);
 *
 * Where initSession(String username, String role) updates the sidebar labels.
 */
public class SessionManager {

    private static User currentUser = null;

    private SessionManager() {}

    /** Call once at login to store the user globally. */
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static String getUsername() {
        return currentUser != null ? currentUser.getUsername() : "Admin";
    }

    public static String getRole() {
        return currentUser != null ? currentUser.getRole() : "Administrator";
    }

    /**
     * Convenience: calls callback(username, role) with current session.
     * Use in controller initialize():
     *   SessionManager.apply(this::initSession);
     */
    public static void apply(BiConsumer<String, String> initSession) {
        if (initSession != null) {
            initSession.accept(getUsername(), getRole());
        }
    }

    public static void clear() {
        currentUser = null;
    }
}
