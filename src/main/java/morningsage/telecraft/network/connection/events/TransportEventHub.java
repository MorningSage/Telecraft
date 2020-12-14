package morningsage.telecraft.network.connection.events;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public final class TransportEventHub {
    public static final Event<ExceptionEvent> EXCEPTION_EVENT = EventFactory.createArrayBacked(ExceptionEvent.class,
        (listeners) -> (exception) -> {
            for (ExceptionEvent listener : listeners) {
                listener.onException(exception);
            }
        }
    );

    public static final Event<DataReceivedEvent> DATA_RECEIVED_EVENT = EventFactory.createArrayBacked(DataReceivedEvent.class,
        (listeners) -> (payload) -> {
            for (DataReceivedEvent listener : listeners) {
                listener.onDataReceived(payload);
            }
        }
    );
}
