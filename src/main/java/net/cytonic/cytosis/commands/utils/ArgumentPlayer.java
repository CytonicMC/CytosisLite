package net.cytonic.cytosis.commands.utils;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.minestom.server.command.ArgumentParserType;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.exception.ArgumentSyntaxException;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.network.NetworkBuffer;
import org.jetbrains.annotations.NotNull;

public class ArgumentPlayer extends Argument<CytosisPlayer> {
    public ArgumentPlayer() {
        super("player");
        setSuggestionCallback((sender, context, suggestion) -> {
            for (CytosisPlayer player : Cytosis.getOnlinePlayers()) {
                suggestion.addEntry(new SuggestionEntry(player.getUsername()));
            }
        });
        setCallback((sender, exception) -> sender.sendMessage(Msg.whoops(exception.getMessage())));
    }

    @Override
    public @NotNull CytosisPlayer parse(@NotNull CommandSender sender, @NotNull String input) throws ArgumentSyntaxException {
        return Cytosis.getPlayer(input).orElse(null);
    }

    @Override
    public byte[] nodeProperties() {
        return NetworkBuffer.makeArray(NetworkBuffer.VAR_INT, 0); // Single word
    }

    @Override
    public ArgumentParserType parser() {
        return ArgumentParserType.STRING;
    }
}
