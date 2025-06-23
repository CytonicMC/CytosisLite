package net.cytonic.cytosis.events;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.data.enums.NPCInteractType;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.events.npcs.NpcInteractEvent;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.npcs.NPC;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.EventDispatcher;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.*;
import net.minestom.server.event.server.ServerTickMonitorEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.time.TimeUnit;

import java.time.Duration;
import java.util.Optional;

/**
 * A class that registers Cytosis required server events
 */
@NoArgsConstructor
public final class ServerEventListeners {

    public static double RAW_MSPT = 0;

    @Listener
    @Priority(1)
    private void onInteract(PlayerEntityInteractEvent event) {
        Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
        if (optional.isPresent() && optional.get() == event.getTarget() && event.getHand() == PlayerHand.MAIN) {
            NPC npc = optional.get();
            EventDispatcher.call(new NpcInteractEvent(npc, (CytosisPlayer) event.getPlayer(), npc.getActions()));
            npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.INTERACT, event.getPlayer()));
        }
    }

    @Listener
    @Priority(100)
    private void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (event.getPlayer() instanceof CytosisPlayer player) {
            //todo: add a preference to disable block updates
            event.setDoBlockUpdates(true);
        } else throw new IllegalStateException("Invalid player object");
    }

    @Listener
    @Priority(1)
    private void onDrop(ItemDropEvent event) {
        final Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemStack();

        Pos playerPos = player.getPosition();
        ItemEntity itemEntity = new ItemEntity(droppedItem);
        itemEntity.setPickupDelay(Duration.of(2000, TimeUnit.MILLISECOND));
        itemEntity.setInstance(player.getInstance(), playerPos.withY(y -> y + 1.5));
        Vec velocity = playerPos.direction().mul(6);
        itemEntity.setVelocity(velocity);
    }

    @Listener
    @Async
    private void onTick(ServerTickMonitorEvent event) {
        RAW_MSPT = event.getTickMonitor().getTickTime();
    }

    @Listener
    private void onConfig(AsyncPlayerConfigurationEvent event) {
        Logger.debug("Player " + event.getPlayer().getUsername() + " has joined the server.");
        event.setSpawningInstance(Cytosis.getDefaultInstance());
    }

    @Listener
    @Priority(1)
    private void onSpawn(PlayerSpawnEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        player.setupSkin();
        player.setGameMode(GameMode.ADVENTURE);
        Cytosis.getSideboardManager().addPlayer(player);
        Cytosis.getPlayerListManager().setupPlayer(player);
        Cytosis.getCommandHandler().recalculateCommands(player);
    }

    @Listener
    @Priority(1)
    private void onQuit(PlayerDisconnectEvent event) {
        final CytosisPlayer player = (CytosisPlayer) event.getPlayer();
        Cytosis.getSideboardManager().removePlayer(player);
    }

    @Listener
    @Priority(1)
    private void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof CytosisPlayer player)) return;
        Optional<NPC> optional = Cytosis.getNpcManager().findNPC(event.getTarget().getUuid());
        if (optional.isPresent() && optional.get() == event.getTarget()) {
            NPC npc = optional.get();
            MinecraftServer.getGlobalEventHandler().call(new NpcInteractEvent(npc, player, npc.getActions()));
            npc.getActions().forEach((action) -> action.execute(npc, NPCInteractType.ATTACK, player));
        }
    }
}