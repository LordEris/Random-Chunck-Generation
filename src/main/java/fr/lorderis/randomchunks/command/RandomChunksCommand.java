package fr.lorderis.randomchunks.command;

import fr.lorderis.randomchunks.BlockPool;
import fr.lorderis.randomchunks.ChunkTransformer;
import fr.lorderis.randomchunks.RandomChunksPlugin;
import fr.lorderis.randomchunks.pregen.PregenerationTask;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

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
                plugin.updateConfig();
                sender.sendMessage("§a[RandomChunks] Configuration rechargée et mise à jour.");
            }
            case "reset" -> {
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
                ChunkTransformer.evictWorld(worldName);
                sender.sendMessage("§a[RandomChunks] Chunks réinitialisés pour le monde §e" + worldName + "§a.");
            }
            case "info" -> {
                BlockPool pool = plugin.getBlockPool();
                sender.sendMessage("§b[RandomChunks] §fBlocs dans le pool : §e" + pool.size());
                sender.sendMessage("§b[RandomChunks] §fChunks transformés : §e" + plugin.getDataManager().totalTransformed());
            }
            case "pregen" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /" + label + " pregen <start|stop|status>");
                    return true;
                }
                switch (args[1].toLowerCase()) {
                    case "start" -> {
                        if (args.length < 4) {
                            sender.sendMessage("§cUsage: /" + label + " pregen start <monde> <rayon> [x z]");
                            return true;
                        }
                        World world = plugin.getServer().getWorld(args[2]);
                        if (world == null) {
                            sender.sendMessage("§cMonde introuvable : " + args[2]);
                            return true;
                        }
                        int radius;
                        try {
                            radius = Integer.parseInt(args[3]);
                        } catch (NumberFormatException e) {
                            sender.sendMessage("§cRayon invalide : " + args[3]);
                            return true;
                        }
                        if (radius <= 0 || radius > 5000) {
                            sender.sendMessage("§cRayon doit être entre 1 et 5000.");
                            return true;
                        }
                        if (plugin.getPregenerationTask() != null) {
                            sender.sendMessage("§cUne prégen est déjà en cours. Utilisez /" + label + " pregen stop d'abord.");
                            return true;
                        }
                        int centerCX, centerCZ;
                        if (args.length >= 6) {
                            try {
                                centerCX = Integer.parseInt(args[4]) >> 4;
                                centerCZ = Integer.parseInt(args[5]) >> 4;
                            } catch (NumberFormatException e) {
                                sender.sendMessage("§cCoordonnées invalides.");
                                return true;
                            }
                        } else {
                            centerCX = world.getSpawnLocation().getBlockX() >> 4;
                            centerCZ = world.getSpawnLocation().getBlockZ() >> 4;
                        }
                        PregenerationTask task = new PregenerationTask(plugin, world, centerCX, centerCZ, radius);
                        plugin.setPregenerationTask(task);
                        task.runTaskTimer(plugin, 0L, 1L);
                        sender.sendMessage("§a[RandomChunks] Prégen démarrée sur §e" + world.getName()
                                + "§a, rayon §e" + radius + " §achunks (§e" + task.getTotal() + " §achunks au total).");
                        plugin.getLogger().info("[Pregen] Démarrage sur " + world.getName()
                                + ", rayon " + radius + " (" + task.getTotal() + " chunks).");
                    }
                    case "stop" -> {
                        PregenerationTask task = plugin.getPregenerationTask();
                        if (task == null) {
                            sender.sendMessage("§cAucune prégen en cours.");
                            return true;
                        }
                        task.cancel();
                        plugin.setPregenerationTask(null);
                        sender.sendMessage("§a[RandomChunks] Prégen annulée ("
                                + task.getProgress() + "/" + task.getTotal() + " chunks traités).");
                    }
                    case "status" -> {
                        PregenerationTask task = plugin.getPregenerationTask();
                        if (task == null) {
                            sender.sendMessage("§7[RandomChunks] Aucune prégen en cours.");
                            return true;
                        }
                        int pct = task.getProgress() * 100 / task.getTotal();
                        sender.sendMessage("§b[RandomChunks] Prégen §e" + task.getWorldName()
                                + "§b : §e" + task.getProgress() + "§b/§e" + task.getTotal()
                                + " §b(§e" + pct + "%§b)");
                    }
                    default -> sender.sendMessage("§cUsage: /" + label + " pregen <start|stop|status>");
                }
            }
            default -> sendHelp(sender, label);
        }
        return true;
    }

    private void sendHelp(CommandSender sender, String label) {
        sender.sendMessage("§b--- RandomChunks ---");
        sender.sendMessage("§e/" + label + " reload §f- Recharge la config");
        sender.sendMessage("§e/" + label + " reset <monde> §f- Réinitialise les chunks d'un monde");
        sender.sendMessage("§e/" + label + " info §f- Affiche les statistiques du plugin");
        sender.sendMessage("§e/" + label + " pregen start <monde> <rayon> [x z] §f- Lance la prégen");
        sender.sendMessage("§e/" + label + " pregen stop §f- Annule la prégen en cours");
        sender.sendMessage("§e/" + label + " pregen status §f- Affiche la progression");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return List.of("reload", "reset", "info", "pregen").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2) {
            if (args[0].equalsIgnoreCase("reset")) {
                return plugin.getServer().getWorlds().stream()
                        .map(World::getName)
                        .filter(n -> n.startsWith(args[1]))
                        .collect(Collectors.toList());
            }
            if (args[0].equalsIgnoreCase("pregen")) {
                return List.of("start", "stop", "status").stream()
                        .filter(s -> s.startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
            }
        }
        if (args.length == 3 && args[0].equalsIgnoreCase("pregen") && args[1].equalsIgnoreCase("start")) {
            return plugin.getServer().getWorlds().stream()
                    .map(World::getName)
                    .filter(n -> n.startsWith(args[2]))
                    .collect(Collectors.toList());
        }
        return List.of();
    }
}
