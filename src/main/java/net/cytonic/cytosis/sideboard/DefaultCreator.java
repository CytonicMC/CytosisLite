package net.cytonic.cytosis.sideboard;

import net.cytonic.cytosis.Cytosis;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.utils.Msg;
import net.kyori.adventure.text.Component;

import java.util.List;

/**
 * The default implementation of {@link SideboardCreator}; creating a baseline sideboard for Cytosis.
 */
public class DefaultCreator implements SideboardCreator {

    /**
     * The default constructor
     */
    public DefaultCreator() {
    }

    @Override
    public Sideboard sideboard(CytosisPlayer player) {
        Sideboard sideboard = new Sideboard(player);
        sideboard.updateLines(lines(player));
        return sideboard;
    }

    @Override
    public List<Component> lines(CytosisPlayer player) {
        try {
            return List.of(
                    Msg.mm(""),
                    Msg.mm(""),
                    Msg.mm("This is a sidebar."),
                    Msg.mm(""),
                    Msg.mm(""),
                    Msg.yellow("Foxikle & Webhead1104")
            );
        } catch (Exception e) {
            Logger.error("error", e);
            return List.of(Msg.mm("<red>Failed to get server information!"));
        }
    }

    @Override
    public Component title(CytosisPlayer player) {
        return Msg.mm("<yellow><bold>CytosisLite</bold></yellow>");
    }
}
