package backend.academy.api.model;

public record NotificationPolicy(String strategy, Integer time) {
    public static final String INSTANT = "INSTANT";
    public static final String DELAYED = "DELAYED";
}
