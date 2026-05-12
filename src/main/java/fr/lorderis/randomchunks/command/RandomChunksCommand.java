package fr.lorderis.randomchunks.command;

import fr.lorderis.randomchunks.BlockPool;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RandomChunksCommand implements CommandExecutor, TabCompleter {

    private final RandomChunksPlugin plugin;

    public RandomChunksCommand(RandomChunksPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("randomchunks.admin")) {
            sender.sendMessage("§cVous n'avez pas la permission.");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.reloadConfig();
                sender.sendMessage("§a[RandomChunks] Configuration rechargée.");
            }
            case "reset" -> {
                if (!sender.isOp() && !sender.hasPermission("randomchunks.admin")) {
                    sender.sendMessage("§cVous n'avez pas la permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /" + label + " reset <world>");
                    return true;
                }
                String worldName = args[1];
                World world = plugin.getServer().getWorld(worldName);
                if (world == null) {
                    sender.sendMessage("§cMonde introuvable : " + worldName);
                    return true;
                }
                plugin.getDataManager().resetWorld(worldName);
                plugin.getDataManager().save();
                sender.sendMessage("§a[RandomChunks] Chunks réinitialisés pour le monde §e" + worldName + "§a.");
            }
            case "info" -> {
                BlockPool pool = plugin.getBlockPool();
                int total = plugin.getDataManager().totalTransformed();
                sender.sendMessage("§b[RandomChunks] §fBlocs dans le pool : §e" + pool.size());
                sender.sendMessage("§b[RandomChunks] §fChunks transformés (session+disque) : §e" + total);
            }
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§b--- RandomChunks ---");
        sender.sendMessage("§e/" + label + " reload §f- Recharge la config");
        sender.sendMessage("§e/" + label + " reset <world> §f- Réinitialise les chunks d'un monde §7(op)");
        sender.sendMessage("§e/" + label + " info §f- Affiche les statistiques du plugin");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("reload", "reset", "info").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("reset")) {
            return plugin.getServer().getWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.startsWith(args[1]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
