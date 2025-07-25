package net.cytonic.cytosis.managers;

import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.events.Events;
import net.cytonic.cytosis.utils.ActionbarSupplier;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.network.packet.server.play.ActionBarPacket;
import net.minestom.server.timer.TaskSchedule;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Getter
public class ActionbarManager {
    private final Map<UUID, Queue<Component>> messageQueues = new ConcurrentHashMap<>();
    private final Set<UUID> cooldowns = new HashSet<>();
    @Setter
    private ActionbarSupplier defaultSupplier = ActionbarSupplier.EMPTY;

    /**
     * Sets up the manager, registering event listeners, and starting the loop.
     */
    public void init() {
        Events.onConfig((player) -> {
            messageQueues.put(player.getUuid(), new LinkedList<>());
            cooldowns.add(player.getUuid());
            // prevent sending packets too early
            MinecraftServer.getSchedulerManager().buildTask(() -> cooldowns.remove(player.getUuid())).delay(TaskSchedule.tick(5)).schedule();
        });
        Events.onLeave((player) -> {
            messageQueues.remove(player.getUuid());
            cooldowns.remove(player.getUuid());
        });

        MinecraftServer.getSchedulerManager().scheduleTask(() -> messageQueues.forEach((uuid, queue) -> {
            if (cooldowns.contains(uuid)) return;
            Cytosis.getPlayer(uuid).ifPresentOrElse(p -> {
                if (queue.isEmpty()) {
                    // we have to use a packet here to avoid an endless recursion
                    p.sendPacket(new ActionBarPacket(defaultSupplier.getActionbar(p)));
                    return;
                }
                // we have to use a packet here to avoid an endless recursion
                p.sendPacket(new ActionBarPacket(queue.poll()));

            }, () -> {
                messageQueues.remove(uuid);
                cooldowns.remove(uuid);
            });
        }), TaskSchedule.tick(20), TaskSchedule.tick(20));

    }

    /**
     * Adds a message to the actionbar queue. If the queue is empty, the message is displayed on the next 20 tick interval.
     *
     * @param uuid    The player to show the message to
     * @param message the message to display
     */
    public void addToQueue(UUID uuid, Component message) {
        Queue<Component> queue = messageQueues.get(uuid);
        if (queue == null) {
            queue = new LinkedList<>();
        }
        queue.add(message);
        messageQueues.put(uuid, queue);
    }

    /**
     * Adds the specified message to the queue {@code iterations} times. The message is displayed for the specified number
     * of iterations. If the the current queue is empty, then the message is displayed on the next 20 tick interval.
     * Otherwise, the messages are displayed once the queue reaches the messages.
     *
     * @param uuid       The player to send the actionbar to
     * @param message    the message to display
     * @param iterations the number of seconds (20 tick intervals) to display the message for
     */
    public void addToQueue(UUID uuid, Component message, int iterations) {
        for (int i = 0; i < iterations; i++) {
            addToQueue(uuid, message);
        }
    }

}
