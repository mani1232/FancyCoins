package de.oliver.fancycoins;

import de.oliver.fancycoins.commands.BalanceCMD;
import de.oliver.fancycoins.commands.CoinsCMD;
import de.oliver.fancycoins.commands.FancyCoinsCMD;
import de.oliver.fancycoins.commands.PayCMD;
import de.oliver.fancycoins.integrations.VaultHook;
import de.oliver.fancycoins.listeners.PlayerJoinListener;
import de.oliver.fancycoins.utils.FoliaScheduler;
import de.oliver.fancylib.FancyLib;
import de.oliver.fancylib.VersionFetcher;
import de.oliver.fancylib.serverSoftware.ServerSoftware;
import de.oliver.fancylib.serverSoftware.schedulers.BukkitScheduler;
import de.oliver.fancylib.serverSoftware.schedulers.FancyScheduler;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.apache.maven.artifact.versioning.ComparableVersion;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class FancyCoins extends JavaPlugin {

    private static FancyCoins instance;
    private final VersionFetcher versionFetcher;
    private final FancyCoinsConfig config;
    private boolean usingVault;
    private final VaultsManager vaultsManager;
    private final FancyScheduler scheduler;

    public FancyCoins() {
        instance = this;
        this.scheduler = ServerSoftware.isFolia()
                ? new FoliaScheduler(instance)
                : new BukkitScheduler(instance);
        //loadDependencies(); not need for paper and forks
        vaultsManager = new VaultsManager();
        config = new FancyCoinsConfig();
        versionFetcher = new VersionFetcher("https://api.modrinth.com/v2/project/fancycoins/version", "https://modrinth.com/plugin/fancycoins/versions");
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();
        FancyLib.setPlugin(this);

        scheduler.runTaskAsynchronously(() -> {
            ComparableVersion newestVersion = versionFetcher.getNewestVersion();
            ComparableVersion currentVersion = new ComparableVersion(getDescription().getVersion());
            if (newestVersion == null) {
                getLogger().warning("Could not fetch latest plugin version");
            } else if (newestVersion.compareTo(currentVersion) > 0) {
                getLogger().warning("-------------------------------------------------------");
                getLogger().warning("You are not using the latest version the FancyCoins plugin.");
                getLogger().warning("Please update to the newest version (" + newestVersion + ").");
                getLogger().warning(versionFetcher.getDownloadUrl());
                getLogger().warning("-------------------------------------------------------");
            }
        });

        if (!ServerSoftware.isPaper()) {
            getLogger().warning("--------------------------------------------------");
            getLogger().warning("Plugin support Paper and its forks like Purpur or Folia.");
            getLogger().warning("Because you are using Bukkit or Spigot,");
            getLogger().warning("the plugin might not work correctly.");
            getLogger().warning("--------------------------------------------------");
        }

        config.reload();
        vaultsManager.loadFromConfig();

        Bukkit.getPluginManager().registerEvents(new PlayerJoinListener(), instance);

        CommandAPI.registerCommand(new FancyCoinsCMD(instance).getClass());
        CommandAPI.registerCommand(new CoinsCMD(instance).getClass());
        CommandAPI.registerCommand(new PayCMD(instance).getClass());
        CommandAPI.registerCommand(new BalanceCMD(instance).getClass());
    }

    @Override
    public void onDisable() {

    }

    //private void loadDependencies() {
    //    BukkitLibraryManager paperLibraryManager = new BukkitLibraryManager(instance);
    //    paperLibraryManager.addJitPack();
//
    //    boolean hasFancyLib;
    //    try {
    //        Class.forName("de.oliver.fancylib.FancyLib");
    //        hasFancyLib = true;
    //    } catch (ClassNotFoundException e) {
    //        hasFancyLib = false;
    //    }
//
    //    if (!hasFancyLib) {
    //        getLogger().info("Loading FancyLib");
    //        Library fancyLib = Library.builder()
    //                .groupId("com{}github{}FancyMcPlugins")
    //                .artifactId("FancyLib")
    //                .version("25458c9930")
    //                .build();
    //        paperLibraryManager.loadLibrary(fancyLib);
    //    }
    //}

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(instance).silentLogs(true));
        usingVault = Bukkit.getPluginManager().getPlugin("Vault") != null;
        if (isUsingVault()) {
            getServer().getServicesManager().register(net.milkbowl.vault.economy.Economy.class, new VaultHook(instance), instance, ServicePriority.Normal);
        } else {
            getLogger().warning("--------------------------------------------------");
            getLogger().warning("You must install the Vault plugin so that other");
            getLogger().warning("plugins can use the default economy.");
            getLogger().warning("--------------------------------------------------");
        }
    }

    public VaultsManager getVaultsManager() {
        return vaultsManager;
    }

    public FancyCoinsConfig getFancyCoinsConfig() {
        return config;
    }

    public static FancyCoins getInstance() {
        return instance;
    }

    public FancyScheduler getScheduler() {
        return scheduler;
    }

    public VersionFetcher getVersionFetcher() {
        return versionFetcher;
    }

    public boolean isUsingVault() {
        return usingVault;
    }
}
