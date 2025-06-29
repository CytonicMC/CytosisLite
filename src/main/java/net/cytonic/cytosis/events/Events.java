package net.cytonic.cytosis.events;

import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.utils.events.PlayerJoinEventResponse;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * A ultility class to increase development speed by creating a simple consumer for
 * common use events. If order is really important, use a tradicional event listener.
 * The events contained in this class run at a priority of 50, which is fairly low as
 * event priorities go. If the event is async by nature, ie {@link AsyncPlayerConfigurationEvent},
 * then the event will be run off of ticking threads. Otherwise, the even will be run
 * on ticking threads. These handlers ignore if the event has already been cancelled.
 */
@SuppressWarnings({"unused"})
public class Events {
    private static final List<Consumer<AsyncPlayerConfigurationEvent>> config = new ArrayList<>();
    private static final List<Consumer<PlayerLoadedEvent>> join = new ArrayList<>();
    private static final List<Consumer<PlayerDisconnectEvent>> disconnect = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOut = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetIn = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOutHigh = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetInHigh = new ArrayList<>();
    private static final List<Consumer<PlayerPacketOutEvent>> packetOutLow = new ArrayList<>();
    private static final List<Consumer<PlayerPacketEvent>> packetInLow = new ArrayList<>();

    private Events() {

    }

    @Listener
    public void onEvent(final PlayerLoadedEvent event) {
        join.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final AsyncPlayerConfigurationEvent event) {
        config.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerDisconnectEvent event) {
        disconnect.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerPacketEvent event) {
        packetIn.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    private void onEvent(final PlayerPacketOutEvent event) {
        packetOut.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(100)
    private void onEventLow(final PlayerPacketEvent event) {
        packetInLow.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(100)
    private void onEventLow(final PlayerPacketOutEvent event) {
        packetOutLow.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(0)
    private void onEventHigh(final PlayerPacketEvent event) {
        packetInHigh.forEach(consumer -> consumer.accept(event));
    }

    @Listener
    @Priority(0)
    private void onEventHigh(final PlayerPacketOutEvent event) {
        packetOutHigh.forEach(consumer -> consumer.accept(event));
    }

    /**
     * A simplified wrapper around {@link #onConfigRaw(Consumer)} providing the
     * player object when a player joins. The player has not yet spawned into an instance at the time of calling this event.
     *
     * @param eventConsumer the consumer consuming the player joining
     */
    public static void onConfig(Consumer<Player> eventConsumer) {
        config.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Registers a consumer to be executed when a player joins.
     * This method adds the specified consumer to handle the event, providing the
     * {@link Player} object associated with the joining player. This is called
     * on the {@link PlayerLoadedEvent}, and this method is a wrapper around the
     * {@link #onJoinRaw(Consumer)}.
     *
     * @param eventConsumer the consumer that processes the {@link Player} object when a player joins
     */
    public static void onJoin(Consumer<Player> eventConsumer) {
        join.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Registers a consumer to be executed when a player has fully loaded and joined.
     * This method adds the given consumer to handle the {@link PlayerLoadedEvent}.
     *
     * @param eventConsumer the consumer that processes the event fired when a player has completed loading and joined
     */
    public static void onJoinRaw(Consumer<PlayerLoadedEvent> eventConsumer) {
        join.add(eventConsumer);
    }

    /**
     * A simplified wrapper around {@link #onConfigRaw(Consumer)} providing more
     * flexibility than {@link #onConfig(Consumer)} with a functional interface providing more than just a player --an intance.
     *
     * @param response The functional interface to be called on the execution of the event
     */
    public static void onConfig(PlayerJoinEventResponse response) {
        config.add(event -> response.accept(event.getPlayer(), event.getSpawningInstance()));
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still
     * called if the event is cancelled. This is called on the {@link AsyncPlayerConfigurationEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onConfigRaw(Consumer<AsyncPlayerConfigurationEvent> event) {
        config.add(event);
    }

    /**
     * A simplified wrapper around {@link Events#onLeaveRaw(Consumer)}. It is still always called, even if
     * the event is cancelled. This is always run synchronously.
     * This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param eventConsumer The consumer consuming the player leaving.
     */
    public static void onLeave(Consumer<Player> eventConsumer) {
        disconnect.add(event -> eventConsumer.accept(event.getPlayer()));
    }

    /**
     * Adds a tracked consumer that is <strong>always</strong> executed, synchronously. This is still called if the event is cancelled.
     * This is called on the {@link PlayerDisconnectEvent} event.
     *
     * @param event The consumer, consuming the entire event object.
     */
    public static void onLeaveRaw(Consumer<PlayerDisconnectEvent> event) {
        disconnect.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a player packet is received.
     * This method adds the given consumer to the internal packet input handler.
     *
     * @param event The consumer that processes the incoming player packet event.
     */
    public static void onPacketIn(Consumer<PlayerPacketEvent> event) {
        packetIn.add(event);
    }

    /**
     * Registers a consumer that will be executed whenever a packet is sent to a player.
     * This method adds the given consumer to the internal packet output handler.
     *
     * @param event The consumer that processes the outgoing player packet event.
     */
    public static void onPacketOut(Consumer<PlayerPacketOutEvent> event) {
        packetOut.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a player packet is received.
     * This method adds the given consumer to the internal handler for incoming packets with high priority.
     *
     * @param event The consumer that processes the incoming player packet event with high priority.
     */
    public static void onPacketInHighPriority(Consumer<PlayerPacketEvent> event) {
        packetInHigh.add(event);
    }

    /**
     * Registers a consumer that will be executed with high priority whenever a packet
     * is sent to a player. This method adds the given consumer to the internal handler
     * for outgoing packets with high priority.
     *
     * @param event The consumer that processes the outgoing player packet event with high priority.
     */
    public static void onPacketOutHighPriority(Consumer<PlayerPacketOutEvent> event) {
        packetOutHigh.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a player packet is received.
     * This method adds the given consumer to the internal handler for incoming packets with low priority.
     *
     * @param event The consumer that processes the incoming player packet event with low priority.
     */
    public static void onPacketInLowPriority(Consumer<PlayerPacketEvent> event) {
        packetInLow.add(event);
    }

    /**
     * Registers a consumer that will be executed with low priority whenever a packet
     * is sent to a player. This method adds the given consumer to the internal handler
     * for outgoing packets with low priority.
     *
     * @param event The consumer that processes the outgoing player packet event with low priority.
     */
    public static void onPacketOutLowPriority(Consumer<PlayerPacketOutEvent> event) {
        packetOutLow.add(event);
    }
}
