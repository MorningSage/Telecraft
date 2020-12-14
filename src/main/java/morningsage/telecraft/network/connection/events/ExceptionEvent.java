package morningsage.telecraft.network.connection.events;

@FunctionalInterface
public interface ExceptionEvent {
    void onException(Exception exception);
}
