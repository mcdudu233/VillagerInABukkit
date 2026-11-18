package com.imjustdoom.villagerinabucket;

import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    public static volatile boolean DISPENSER_VILLAGER_PICKUP = true;
    public static volatile boolean DISPENSER_VILLAGER_PLACE = true;
    public static volatile boolean DISPENSER_ZOMBIE_VILLAGER_PICKUP = true;
    public static volatile boolean DISPENSER_ZOMBIE_VILLAGER_PLACE = true;
    public static volatile boolean DISPENSER_WANDERING_TRADER_PICKUP = true;
    public static volatile boolean DISPENSER_WANDERING_TRADER_PLACE = true;

    public static volatile boolean RESOURCE_PACK = false;
    public static volatile String RESOURCE_PACK_URL = "";
    public static volatile String RESOURCE_PACK_HASH = "";
    public static volatile String RESOURCE_PACK_ID = "68a4b411-e409-4d89-b563-66049ba4914b";

    public static volatile boolean CONSOLE_LOGGING = false;
    public static volatile boolean FILE_LOGGING = false;

    public static void init() {
        VillagerInABucket.get().saveDefaultConfig();
        VillagerInABucket.get().reloadConfig();
        FileConfiguration fileConfiguration = VillagerInABucket.get().getConfig();

        if (fileConfiguration.contains("use-permissions", true) && !fileConfiguration.getBoolean("use-permissions", true)) {
            VLog.severe("当前版本通过权限控制村民桶的使用。请参考 https://github.com/JustDoom/VillagerInABukkit/wiki/Configuring-using-permissions 迁移旧配置。");
        }

        DISPENSER_VILLAGER_PICKUP = fileConfiguration.getBoolean("dispenser.villager.pickup", DISPENSER_VILLAGER_PICKUP);
        DISPENSER_VILLAGER_PLACE = fileConfiguration.getBoolean("dispenser.villager.place", DISPENSER_VILLAGER_PLACE);
        DISPENSER_WANDERING_TRADER_PICKUP = fileConfiguration.getBoolean("dispenser.wandering-trader.pickup", DISPENSER_WANDERING_TRADER_PICKUP);
        DISPENSER_WANDERING_TRADER_PLACE = fileConfiguration.getBoolean("dispenser.wandering-trader.place", DISPENSER_WANDERING_TRADER_PLACE);
        DISPENSER_ZOMBIE_VILLAGER_PICKUP = fileConfiguration.getBoolean("dispenser.zombie-villager.pickup", DISPENSER_ZOMBIE_VILLAGER_PICKUP);
        DISPENSER_ZOMBIE_VILLAGER_PLACE = fileConfiguration.getBoolean("dispenser.zombie-villager.place", DISPENSER_ZOMBIE_VILLAGER_PLACE);

        RESOURCE_PACK = fileConfiguration.getBoolean("resource-pack", false);
        RESOURCE_PACK_URL = fileConfiguration.getString("resource-pack-url", "");
        RESOURCE_PACK_HASH = fileConfiguration.getString("resource-pack-hash", "");
        RESOURCE_PACK_ID = fileConfiguration.getString("resource-pack-id", RESOURCE_PACK_ID);

        if (RESOURCE_PACK && (RESOURCE_PACK_URL.isBlank() || RESOURCE_PACK_HASH.isBlank())) {
            VLog.severe("自动发送资源包需要填写 resource-pack-url 和 resource-pack-hash。");
            RESOURCE_PACK = false;
        }

        CONSOLE_LOGGING = fileConfiguration.getBoolean("console-logging", CONSOLE_LOGGING);
        FILE_LOGGING = fileConfiguration.getBoolean("file-logging", FILE_LOGGING);

        synchronized (VLog.LOG_LOCK) {
            if (FILE_LOGGING) {
                try {
                    if (VLog.LOG_FILE_WRITER != null) {
                        VLog.LOG_FILE_WRITER.close();
                        VLog.LOG_FILE_WRITER = null;
                    }
                    File logFile = new File(VillagerInABucket.get().getDataFolder(), "villager-actions.log");
                    VLog.LOG_FILE_WRITER = new FileWriter(logFile, true);
                } catch (IOException e) {
                    FILE_LOGGING = false;
                    VillagerInABucket.get().getLogger().severe("打开村民操作日志失败： " + e.getMessage());
                }
            } else {
                if (VLog.LOG_FILE_WRITER != null) {
                    try {
                        VLog.LOG_FILE_WRITER.close();
                        VLog.LOG_FILE_WRITER = null;
                    } catch (IOException e) {
                        VillagerInABucket.get().getLogger().severe("关闭村民操作日志失败： " + e.getMessage());
                    }
                }
            }
        }
    }
}
