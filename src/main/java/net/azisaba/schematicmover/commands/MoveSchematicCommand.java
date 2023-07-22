package net.azisaba.schematicmover.commands;

import net.azisaba.schematicmover.SchematicMover;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class MoveSchematicCommand implements TabExecutor {
    private final SchematicMover plugin;

    public MoveSchematicCommand(SchematicMover plugin) {
        this.plugin = plugin;
    }

    private String sanitize(CommandSender sender, String s) {
        if (sender.hasPermission("schematicmover.allow-unsafe")) {
            return s;
        }
        return s.replace("/", "_");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length <= 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /move-schematic <from server> <to server> <name> [new name]");
            return true;
        }
        String fromServer = sanitize(sender, args[0]);
        String toServer = sanitize(sender, args[1]);
        if (!sender.hasPermission("schematicmover.use." + fromServer) || !sender.hasPermission("schematicmover.use." + toServer)) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to do this.");
            return true;
        }
        String name = sanitize(sender, args[2]);
        String newName = args.length > 3 ? sanitize(sender, args[3]) : name;
        File oldSchematicsLocation = new File(plugin.getFindIn().replace("{server}", fromServer));
        File oldLocation = new File(oldSchematicsLocation, name);
        File newSchematicsLocation = new File(plugin.getFindIn().replace("{server}", toServer));
        File newLocation = new File(newSchematicsLocation, newName);
        if (newLocation.exists()) {
            sender.sendMessage(ChatColor.RED + newName + " already exists in target location (add another argument to override the new filename)");
            return true;
        }
        try {
            Files.copy(oldLocation.toPath(), newLocation.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.RED + e.getClass().getTypeName() + ": " + e.getMessage());
            return true;
        }
        sender.sendMessage(ChatColor.GREEN + fromServer + ":" + name + " -> " + toServer + ":" + newName);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 || args.length == 2) {
            String serversFolder = plugin.getFindIn().substring(0, plugin.getFindIn().indexOf("{server}"));
            return Arrays.stream(Objects.requireNonNull(new File(serversFolder).listFiles(File::isDirectory)))
                    .map(File::getName)
                    .filter(s -> s.startsWith(args[args.length - 1]))
                    .filter(name -> sender.hasPermission("schematicmover.use." + name))
                    .collect(Collectors.toList());
        }
        if (args.length == 3) {
            File schematicsLocation = new File(plugin.getFindIn().replace("{server}", sanitize(sender, args[0])));
            if (!schematicsLocation.exists()) return Collections.emptyList();
            return Arrays.stream(Objects.requireNonNull(schematicsLocation.listFiles(File::isFile)))
                    .map(File::getName)
                    .filter(s -> s.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
