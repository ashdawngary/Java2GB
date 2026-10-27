package dev.java2gb.planner;

final class ApplicationScope {
    private ApplicationScope() {}

    static boolean contains(String prefix, String name) {
        return name.startsWith(prefix) || name.startsWith("dev.java2gb.api.");
    }
}
