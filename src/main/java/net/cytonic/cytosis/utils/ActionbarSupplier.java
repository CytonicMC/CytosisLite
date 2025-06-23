package net.cytonic.cytosis.utils;

import net.cytonic.cytosis.player.CytosisPlayer;
import net.kyori.adventure.text.Component;

@FunctionalInterface
public interface ActionbarSupplier {
    ActionbarSupplier EMPTY = player -> Component.empty();

    Component getActionbar(CytosisPlayer player);
}
