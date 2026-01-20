package dhbw.trasima;

/**
 * Generic publisher interface for outputting data.
 * Allows flexible implementations (Console, Database, Logger, MessageBroker, etc.)
 *
 * @param <T> The type of object to publish
 */
public interface IPublisher<T> {

    /**
     * Publishes data to the configured output medium.
     *
     * @param data The object to publish
     */
    void publish(T data);
}