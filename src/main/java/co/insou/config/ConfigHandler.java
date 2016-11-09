package co.insou.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

public abstract class ConfigHandler<T extends JavaPlugin> {

    protected final T plugin;
    protected final FileConfiguration config;

    public ConfigHandler(T plugin) {
        this(plugin, plugin.getConfig());
    }

    public ConfigHandler(T plugin, String config) {
        this(plugin, new File(createIfAbsentAndGetDir(plugin.getDataFolder()), config.replace(".yml", "") + ".yml"));
    }

    public ConfigHandler(T plugin, File config) {
        this(plugin, YamlConfiguration.loadConfiguration(createIfAbsentAndGetFile(config)));
    }

    public ConfigHandler(T plugin, FileConfiguration config) {
        this.plugin = plugin;
        this.config = config;

        this.populate();
    }

    private void populate() {
        for (Field field : this.getClass().getDeclaredFields()) {

            if (!field.isAccessible()) {
                field.setAccessible(true);
            }

            for (Annotation annotation : field.getDeclaredAnnotations()) {

                if (annotation.getClass().equals(ConfigPopulate.class)) {

                    ConfigPopulate populate = (ConfigPopulate) annotation;

                    String key = populate.key();

                    Object value = this.getConfigValue(key);

                    if (value != null) {

                        try {
                            field.set(this, value);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }

                    }
                }
            }
        }
    }

    protected <X> X getConfigValue(String key) {
        Object ret = this.config.get(key);

        if (ret == null || !ConfigHandler.<X>isInstance(ret)) {
            return null;
        }

        return (X) ret;
    }

    private static <X> boolean isInstance(Object obj) {
        try {
            X x = (X) obj;

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static File createIfAbsentAndGetDir(File file) {
        if (!file.exists()) {
            file.mkdirs();
        }
        return file;
    }

    private static File createIfAbsentAndGetFile(File file) {
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

}
