package net.cytonic.cytosis.player;

import io.github.togar2.pvp.player.CombatPlayerImpl;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import net.minestom.server.command.builder.CommandResult;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;

import java.util.Random;
import java.util.UUID;

/**
 * A wrapper class for the {@link Player} object which includes a few more useful utilities that avoids calling the managers themselves.
 */
@SuppressWarnings("unused")
public class CytosisPlayer extends CombatPlayerImpl {

    private static final PlayerSkin FOXIKLE = PlayerSkin.fromUuid("7f3208cb-05b7-4b35-819c-a58438158fbc");
    private static final PlayerSkin WEBHEAD1104 = PlayerSkin.fromUuid("1e27cf68-9d59-45ba-8190-e4b55fabaf57");

    private PlayerSkin skin;

    public void setupSkin() {
        if (new Random().nextInt(0, 1) == 1) {
            sendMessage(Msg.yellowSplash("LOL!", "We gave you the skin of Webhead1104, thanks to the CytosisLite team for the prank!"));
            skin = WEBHEAD1104;
        } else {
            sendMessage(Msg.yellowSplash("LOL!", "We gave you Foxikle's skin, thanks to the CytosisLite team for the genius tomfoolery!"));
            skin = FOXIKLE;
        }

    }

    /**
     * Creates a new instance of a player
     *
     * @param uuid             the player's UUID
     * @param username         the player's Username
     * @param playerConnection the player's connection
     */
    public CytosisPlayer(@NotNull UUID uuid, @NotNull String username, @NotNull PlayerConnection playerConnection) {
        this(playerConnection, new GameProfile(uuid, username));
    }

    public CytosisPlayer(@NotNull PlayerConnection playerConnection, GameProfile gameProfile) {
        super(playerConnection, gameProfile);
    }

    /**
     * Executes the given command as the player
     *
     * @param command the command to execute
     * @return the result of processing the command
     */
    @SuppressWarnings("UnusedReturnValue")
    public CommandResult dispatchCommand(String command) {
        return Cytosis.getCommandManager().getDispatcher().execute(this, command);
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        Cytosis.getActionbarManager().addToQueue(getUuid(), message);
    }

    @Override
    public void sendActionBar(@NotNull ComponentLike message) {
        this.sendActionBar(message.asComponent());
    }

    /**
     * Gets the player's name as a component. This will either return the display name
     * (if set) or a component holding the username.
     *
     * @return the name
     */
    @Override
    public @NotNull Component getName() {
        return Component.text(getUsername());
    }

    @Override
    public PlayerSkin getSkin() {
        return skin;
    }
}
