package ua.com.kisit.course_project.Entity;

public enum UserRole {
    CLIENT("Клієнт"),
    ADMIN("Адміністратор"),
    MANAGER("Менеджер");

    private final String displayName;

    UserRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static UserRole fromString(String role) {
        for (UserRole userRole : UserRole.values()) {
            if (userRole.name().equalsIgnoreCase(role)) {
                return userRole;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}