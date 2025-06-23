package net.cytonic.cytosis.commands.server;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.minestom.server.command.builder.Command;

public class RecalculatePermissionsCommand extends Command {

    public RecalculatePermissionsCommand() {
        super("recalculatepermissions", "recalcperms");
        setDefaultExecutor((sender, context) -> {
            if (!(sender instanceof CytosisPlayer player)) return;
            Cytosis.getCommandHandler().recalculateCommands(player);
        });
    }
}
