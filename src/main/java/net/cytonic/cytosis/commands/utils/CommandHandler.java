package net.cytonic.cytosis.commands.utils;

import lombok.NoArgsConstructor;
import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.commands.defaultMinecraft.GamemodeCommand;
import net.cytonic.cytosis.commands.server.*;
import net.cytonic.cytosis.commands.staff.*;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.CommandManager;
import net.minestom.server.entity.Player;

/**
 * A class that handles the commands, their execution, and allegedly a console.
 */
@NoArgsConstructor
public class CommandHandler {

    /**
     * Registers the default Cytosis commands
     */
    public void registerCytosisCommands() {
        CommandManager cm = Cytosis.getCommandManager();
        cm.setUnknownCommandCallback((commandSender, s) -> {
            if (!(commandSender instanceof CytosisPlayer player)) return;
            player.sendMessage(Msg.redSplash("UNKNOWN COMMAND!", "The command '/%s' does not exist.", s));
        });

        cm.register(
                new GamemodeCommand(),
                new FlyCommand(),
                new TimeCommand(),
                new PingCommand(),
                new LoopCommand(),
                new RecalculatePermissionsCommand()
        );
    }

    /**
     * Sends a packet to the player to recalculate command permissions
     *
     * @param player The player to send the packet to
     */
    public void recalculateCommands(Player player) {
        player.sendPacket(Cytosis.getCommandManager().createDeclareCommandsPacket(player));
    }
}
