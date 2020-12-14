package morningsage.telecraft.network.connection.events;

@FunctionalInterface
public interface DataReceivedEvent {
    void onDataReceived(byte[] payload);
}
