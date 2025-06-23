package net.cytonic.cytosis;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.Strictness;
import eu.koboo.minestom.stomui.api.ViewRegistry;
import eu.koboo.minestom.stomui.core.MinestomUI;
import io.github.togar2.pvp.MinestomPvP;
import io.github.togar2.pvp.feature.CombatFeatureSet;
import io.github.togar2.pvp.feature.CombatFeatures;
import lombok.Getter;
import lombok.Setter;
import net.cytonic.cytosis.commands.utils.CommandHandler;
import net.cytonic.cytosis.config.CytosisSettings;
import net.cytonic.cytosis.data.adapters.InstantAdapter;
import net.cytonic.cytosis.data.adapters.KeyAdapter;
import net.cytonic.cytosis.data.serializers.KeySerializer;
import net.cytonic.cytosis.data.serializers.PosSerializer;
import net.cytonic.cytosis.events.EventHandler;
import net.cytonic.cytosis.events.EventListener;
import net.cytonic.cytosis.events.ServerEventListeners;
import net.cytonic.cytosis.events.api.Async;
import net.cytonic.cytosis.events.api.Listener;
import net.cytonic.cytosis.events.api.Priority;
import net.cytonic.cytosis.files.FileManager;
import net.cytonic.cytosis.logging.Logger;
import net.cytonic.cytosis.managers.ActionbarManager;
import net.cytonic.cytosis.managers.NPCManager;
import net.cytonic.cytosis.managers.PlayerListManager;
import net.cytonic.cytosis.managers.SideboardManager;
import net.cytonic.cytosis.player.CytosisPlayer;
import net.cytonic.cytosis.player.CytosisPlayerProvider;
import net.cytonic.cytosis.utils.BlockPlacementUtils;
import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.network.packet.client.play.ClientCommandChatPacket;
import net.minestom.server.network.packet.client.play.ClientSignedCommandChatPacket;
import net.minestom.server.timer.TaskSchedule;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.configurate.gson.GsonConfigurationLoader;
import org.spongepowered.configurate.objectmapping.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The main class for Cytosis
 */
@Getter
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public final class Cytosis {

    /**
     * The instance of Gson for serializing and deserializing objects. (Mostly for preferences).
     */
    public static final Gson GSON = new GsonBuilder().registerTypeAdapter(Key.class, new KeyAdapter())
            .registerTypeAdapter(Instant.class, new InstantAdapter()).registerTypeAdapterFactory(new KeyAdapter())
            .enableComplexMapKeySerialization().setStrictness(Strictness.LENIENT).serializeNulls().create();
    public static final GsonConfigurationLoader.Builder GSON_CONFIGURATION_LOADER = GsonConfigurationLoader.builder()
            .indent(0)
            .defaultOptions(opts -> opts
                    .shouldCopyDefaults(true)
                    .serializers(builder -> {
                        builder.registerAnnotatedObjects(ObjectMapper.factory());
                        builder.register(Key.class, new KeySerializer());
                        builder.register(Pos.class, new PosSerializer());
                    })
            );
    /**
     * The version of Cytosis
     */
    public static final String VERSION = "0.1";
    public static final ViewRegistry VIEW_REGISTRY = MinestomUI.create();

    // manager stuff
    @Getter
    private static MinecraftServer minecraftServer;
    @Getter
    private static net.minestom.server.instance.InstanceManager minestomInstanceManager;
    @Setter
    @Getter
    private static InstanceContainer defaultInstance;
    @Getter
    private static EventHandler eventHandler;
    @Getter
    private static ConnectionManager connectionManager;
    @Getter
    private static CommandManager commandManager;
    @Getter
    private static CommandHandler commandHandler;
    @Getter
    private static FileManager fileManager;
    @Getter
    private static PlayerListManager playerListManager;
    @Getter
    private static SideboardManager sideboardManager;
    @Getter
    private static NPCManager npcManager;
    @Getter
    private static List<String> flags;
    @Getter
    private static ActionbarManager actionbarManager;

    private Cytosis() {
    }

    /**
     * The entry point for the Minecraft Server
     *
     * @param args Runtime flags
     */
    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        flags = List.of(args);
        // handle uncaught exceptions
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        Thread.currentThread().setUncaughtExceptionHandler((t, e) -> {
            try {
                Logger.error("Uncaught exception in thread " + t.getName(), e);
            } catch (Exception e1) {
                e1.printStackTrace(System.err);
            }
        });

        // Initialize the server
        Logger.info("Starting Cytosis server...");
        minecraftServer = MinecraftServer.init();
        MinecraftServer.getConnectionManager().setPlayerProvider(new CytosisPlayerProvider());
        MinecraftServer.setBrandName("Cytosis");

        MinecraftServer.getBenchmarkManager().enable(Duration.ofSeconds(10L));

        Logger.info("Starting instance manager.");
        minestomInstanceManager = MinecraftServer.getInstanceManager();

        Logger.info("Starting connection manager.");
        connectionManager = MinecraftServer.getConnectionManager();

        // Commands
        Logger.info("Starting command manager.");
        commandManager = MinecraftServer.getCommandManager();

        // instances
        Logger.info("Creating instance container");
        defaultInstance = minestomInstanceManager.createInstanceContainer();

        Logger.info("Creating file manager");
        fileManager = new FileManager();

        // Everything after this point depends on config contents
        Logger.info("Initializing file manager");
        fileManager.init();

        Logger.info("Loading Cytosis Settings");
        CytosisSettings.loadEnvironmentVariables();
        CytosisSettings.loadCommandArgs();

        Logger.info("Initializing block placements");
        BlockPlacementUtils.init();

        Logger.info("Initializing view registry");
        VIEW_REGISTRY.enable();

        Logger.info("Adding a singed command packet handler");
        // commands
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientSignedCommandChatPacket.class, (packet, p) -> MinecraftServer.getPacketListenerManager().processClientPacket(new ClientCommandChatPacket(packet.message()), p.getPlayerConnection(), p.getPlayerConnection().getConnectionState()));

        Logger.info("Initializing server commands");
        commandHandler = new CommandHandler();
        commandHandler.registerCytosisCommands();

        Logger.info("Setting up event handlers");
        eventHandler = new EventHandler(MinecraftServer.getGlobalEventHandler());
        eventHandler.init();


        Logger.info("Starting Player list manager");
        playerListManager = new PlayerListManager();

        Logger.info("Creating sideboard manager!");
        sideboardManager = new SideboardManager();
        sideboardManager.autoUpdateBoards(TaskSchedule.seconds(1L));

        Logger.info("Starting NPC manager!");
        npcManager = new NPCManager();

        Logger.info("starting actionbar manager");
        actionbarManager = new ActionbarManager();
        actionbarManager.init();

        Logger.info("Loading PVP");
        MinestomPvP.init();
        CombatFeatureSet modernVanilla = CombatFeatures.modernVanilla();
        MinecraftServer.getGlobalEventHandler().addChild(modernVanilla.createNode());
        MinecraftServer.getConnectionManager().setPlayerProvider(CytosisPlayer::new);


        AtomicInteger counter = new AtomicInteger(0);

        ServerEventListeners instance = new ServerEventListeners();
        for (Method method : ServerEventListeners.class.getDeclaredMethods()) {
            if (method.isAnnotationPresent(Listener.class)) {
                method.setAccessible(true); // make the method accessible so we can call it later on
                int priority = method.isAnnotationPresent(Priority.class) ? method.getAnnotation(Priority.class).value() : 50;
                boolean async = method.isAnnotationPresent(Async.class);

                Class<? extends Event> eventClass;
                try {
                    eventClass = (Class<? extends Event>) method.getParameterTypes()[0];
                } catch (ClassCastException e) {
                    Logger.error("The parameter of a method annotated with @Listener must be a valid event!", e);
                    return;
                } catch (ArrayIndexOutOfBoundsException e) {
                    Logger.error("Methods annotated with @Listener must have a valid event as a parameter!", e);
                    return;
                }

                eventHandler.registerListener(new EventListener<>(
                        "cytosis:annotation-listener-" + counter.getAndIncrement(),
                        async, priority, (Class<Event>) eventClass, event -> {
                    try {
                        method.invoke(instance, event);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        Logger.error("Failed to call @Listener!", e);
                    }
                }
                ));
            }
        }

        // Start the server
        Logger.info("Server started on port " + CytosisSettings.SERVER_PORT);
        minecraftServer.start("0.0.0.0", CytosisSettings.SERVER_PORT);
        MinecraftServer.getExceptionManager().setExceptionHandler(e -> Logger.error("Uncaught exception: ", e));

        long end = System.currentTimeMillis();
        Logger.info("Server started in " + (end - start) + "ms!");

        if (flags.contains("--ci-test")) {
            Logger.info("Stopping server due to '--ci-test' flag.");
            MinecraftServer.stopCleanly();
        }
    }

    /**
     * Gets the players currently on THIS instance
     *
     * @return a set of players
     */
    // every object the server makes is a CytosisPlayer -- or decentdant from one
    public static Set<CytosisPlayer> getOnlinePlayers() {
        HashSet<CytosisPlayer> players = new HashSet<>();

        for (@NotNull Player onlinePlayer : MinecraftServer.getConnectionManager().getOnlinePlayers()) {
            try {
                CytosisPlayer cp = (CytosisPlayer) onlinePlayer;
                players.add(cp);
            } catch (ClassCastException e) {
                // ignored
            }
        }
        return players;
    }

    /**
     * Gets the player if they are on THIS instance, by USERNAME
     *
     * @param username The name to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(String username) {
        if (username == null) return Optional.empty();
        return Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager().getOnlinePlayerByUsername(username));
    }

    /**
     * Gets the player if they are on THIS instance, by UUID
     *
     * @param uuid The uuid to fetch the player by
     * @return The optional holding the player if they exist
     */
    public static Optional<CytosisPlayer> getPlayer(UUID uuid) {
        if (uuid == null) return Optional.empty();
        return Optional.ofNullable((CytosisPlayer) MinecraftServer.getConnectionManager().getOnlinePlayerByUuid(uuid));
    }
}