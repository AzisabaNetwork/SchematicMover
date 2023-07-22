package net.azisaba.schematicmover;

import net.azisaba.schematicmover.commands.MoveSchematicCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class SchematicMover extends JavaPlugin {
    private String findIn;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        findIn = getConfig().getString("find-in", "/srv/{server}/plugins/WorldEdit/schematics");
        assert findIn != null;
        if (!findIn.contains("{server}")) {
            throw new IllegalArgumentException("find-in does not contain {server}");
        }
        Objects.requireNonNull(getCommand("move-schematic")).setExecutor(new MoveSchematicCommand(this));
    }

    public @NotNull String getFindIn() {
        return Objects.requireNonNull(findIn);
    }
}
