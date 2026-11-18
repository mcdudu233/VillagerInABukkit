package com.imjustdoom.villagerinabucket;

import com.imjustdoom.villagerinabucket.listener.DispenserListener;
import com.imjustdoom.villagerinabucket.listener.InteractListener;
import com.imjustdoom.villagerinabucket.listener.ReloadListener;
import com.imjustdoom.villagerinabucket.listener.protection.GriefPreventionProtectionListener;
import com.imjustdoom.villagerinabucket.listener.protection.WorldGuardProtectionListener;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.UUID;

public class VillagerInABucket extends JavaPlugin implements Listener {
    public static String PREFIX = "[VIAB]";
    public static TextColor TEXT_COLOR = TextColor.color(2, 220, 5);

    public NamespacedKey key = new NamespacedKey(this, "villager_data");


    public VillagerInABucket() {
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        Config.init();

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            LiteralCommandNode<CommandSourceStack> buildCommand = Commands.literal("villagerinabucket")
                    .requires(sender -> sender.getSender().hasPermission("villagerinabucket.commands"))
                    .executes(ctx -> {
                        ctx.getSource().getSender().sendMessage(Component.text(PREFIX + " 村民装起来 · 版本 " + getPluginMeta().getVersion(), TEXT_COLOR));
                        return Command.SINGLE_SUCCESS;
                    }).then(Commands.literal("reload").executes(ctx -> {
                        Config.init();
                        ctx.getSource().getSender().sendMessage(Component.text(PREFIX + " 村民装起来的配置已重新加载。", TEXT_COLOR));
                        return Command.SINGLE_SUCCESS;
                    })).then(Commands.literal("forceplace").executes(ctx -> {
                        if (!(ctx.getSource().getSender() instanceof Player player)) {
                            ctx.getSource().getSender().sendMessage(Component.text(PREFIX + " 请在游戏里使用这条命令，或在命令后加上玩家名。", TEXT_COLOR));
                            return Command.SINGLE_SUCCESS;
                        }

                        return forceSpawnLogic(ctx.getSource().getSender(), player);
                    }).then(Commands.argument("target", ArgumentTypes.player()).executes(ctx -> {
                        final PlayerSelectorArgumentResolver resolver = ctx.getArgument("target", PlayerSelectorArgumentResolver.class);
                        final Player player = resolver.resolve(ctx.getSource()).getFirst();
                        return forceSpawnLogic(ctx.getSource().getSender(), player);
                    })))
                    .build();
            commands.registrar().register(buildCommand, List.of("viab"));
        });

        PluginManager pluginManager = getServer().getPluginManager();

        if (pluginManager.isPluginEnabled("WorldGuard")) {
            pluginManager.registerEvents(new WorldGuardProtectionListener(), this);
            getLogger().info("已接入 WorldGuard 的区域保护。");
        }
        if (pluginManager.isPluginEnabled("GriefPrevention")) {
            pluginManager.registerEvents(new GriefPreventionProtectionListener(), this);
            getLogger().info("已接入 GriefPrevention 的领地保护。");
        }

        pluginManager.registerEvents(this, this);
        pluginManager.registerEvents(new InteractListener(), this);
        pluginManager.registerEvents(new DispenserListener(), this);

        if (pluginManager.isPluginEnabled("BetterReload")) {
            pluginManager.registerEvents(new ReloadListener(), this);
            getLogger().info("已接入 BetterReload 的配置重载。");
        }

        Metrics metrics = new Metrics(this, 25722);
        metrics.addCustomChart(new SimplePie("updated_to_new_settings", () -> "true"));
        metrics.addCustomChart(new SimplePie("usingResourcepack", () -> String.valueOf(Config.RESOURCE_PACK)));
    }

    @Override
    public void onDisable() {
        synchronized (VLog.LOG_LOCK) {
            if (VLog.LOG_FILE_WRITER != null) {
                try {
                    VLog.LOG_FILE_WRITER.close();
                    VLog.LOG_FILE_WRITER = null;
                } catch (IOException e) {
                    getLogger().severe("关闭村民操作日志失败。");
                }
            }
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!Config.RESOURCE_PACK) {
            return;
        }
        try {
            event.getPlayer().sendResourcePacks(ResourcePackInfo.resourcePackInfo(UUID.fromString(Config.RESOURCE_PACK_ID), URI.create(Config.RESOURCE_PACK_URL), Config.RESOURCE_PACK_HASH));
        } catch (IllegalArgumentException exception) {
            getLogger().severe("发送资源包失败，请检查配置里的 resource-pack-id、resource-pack-url 和 resource-pack-hash。");
        }
    }

    private int forceSpawnLogic(CommandSender sender, Player player) {
        ItemStack itemStack = player.getInventory().getItem(player.getActiveItemHand());

        if (!BucketUtil.hasEntityData(itemStack)) {
            sender.sendMessage(Component.text(PREFIX + " 请拿着装有村民、僵尸村民或流浪商人的桶。", TEXT_COLOR));
            return Command.SINGLE_SUCCESS;
        }

        LivingEntity entity = BucketUtil.entityFromBucket(itemStack, player.getWorld());
        Location location = player.getLocation();

        if (BucketUtil.spawnVillagerFromBucket(entity, player, itemStack, location)) {
            sender.sendMessage(Component.text(PREFIX + " 已在玩家所在位置放出生物。", TEXT_COLOR));
        } else {
            sender.sendMessage(Component.text(PREFIX + " 这里暂时放不出来，请试试换个位置，或检查领地权限。", TEXT_COLOR));
        }

        return Command.SINGLE_SUCCESS;
    }

    private static VillagerInABucket INSTANCE;

    /**
     * Gets the Villager In A Bucket instance
     *
     * @return the instance
     */
    public static VillagerInABucket get() {
        return INSTANCE;
    }
}