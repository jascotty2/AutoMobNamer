package net.ultibyte.AutoMobNamer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class CommandHandler implements CommandExecutor {

	private AutoMobNamer plugin; // pointer to the main class.
	Player p;

	public CommandHandler(AutoMobNamer plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender commandsender, Command command, String label, String[] args) {
		if (command.getName().equalsIgnoreCase("namer")) {
			if (args.length <= 1) {
				if (args[0].equalsIgnoreCase("help")) {
					commandsender.sendMessage("*****************" + ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "*****************");
					commandsender.sendMessage(ChatColor.AQUA + "The Commands are:");
					commandsender.sendMessage(ChatColor.GREEN + "/namer add [MobType] [Name (Use _ for spaces!)]");
					commandsender.sendMessage(ChatColor.GREEN + "/namer remove [MobType] [Name (Use _ for spaces!)]");
					commandsender.sendMessage(ChatColor.GREEN + "/namer ColorOff  -  Turns colors off for newly spawned mobs");
					commandsender.sendMessage(ChatColor.GREEN + "/namer ColorOn  -  Turns color on for newly spawned mobs");
					commandsender.sendMessage(ChatColor.GREEN + "/namer clear [MobType]  -  Removes all names for this Mob Type (Leaving just the mob type as its name which you can also remove if you wish.)");
					commandsender.sendMessage(ChatColor.GREEN + "/namer setvisibility [visible/invisible] - Causes mob names to only be visible when looked at at close proximity");
					commandsender.sendMessage("*****************" + ChatColor.YELLOW + "Thats it!" + ChatColor.WHITE + "*****************");
					return true;
				}
				if (args[0].equalsIgnoreCase("add")) {
					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer add [MobType] [Name]");
				}
				if (args[0].equalsIgnoreCase("remove")) {
					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer remove [MobType] [Name]");
				}
				if (args[0].equalsIgnoreCase("ColorOff")) {
					VariableStuff.NoColor = true;
					commandsender.sendMessage(ChatColor.GREEN + "Mob name colors have been turned off!");
				}
				if (args[0].equalsIgnoreCase("ColorOn")) {
					VariableStuff.NoColor = false;
					commandsender.sendMessage(ChatColor.GREEN + "Mob name colors are now on!");
				}
				if (args[0].equalsIgnoreCase("clear")) {
					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer clear [MobType]");
				}

			}

			EntityType type = this.getType(args[1]);
			if (type == null) {
				commandsender.sendMessage(ChatColor.RED + "Entity Type \"" + args[1] + "\" was not found");
				return true;
			}
			final List<String> names = plugin.entityNames.get(type);


			if (args.length == 2) {
				if (args[0].equalsIgnoreCase("add")) {
					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer add [MobType] [Name]");
					return true;
				}
				if (args[0].equalsIgnoreCase("setvisibility")) {
					if (args[1].equalsIgnoreCase("visible") || args[1].equalsIgnoreCase("invisible")) {
						if (args[1].equalsIgnoreCase("visible")) {
							setVisibilityAll(true);
						} else {
							setVisibilityAll(false);
						}
						commandsender.sendMessage(ChatColor.GREEN + "Done!");
					} else {
						commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer setvisibility [visible/invisible]");
					}
					return true;
				}
				if (args[0].equalsIgnoreCase("remove")) {
					commandsender.sendMessage(ChatColor.GREEN + "Go on... it's /namer remove [MobType] [Name]");
					return true;
				}

				if (args[0].equalsIgnoreCase("list")) {
					if (names == null || names.isEmpty()) {
						commandsender.sendMessage(ChatColor.BLUE + "(No names set for " + type + ")");
					} else {
						commandsender.sendMessage(ChatColor.BLUE + "******************** Names for " + type + " ********************");
						for (String name : names) {
							commandsender.sendMessage(ChatColor.AQUA + name);
						}
					}
				}
				if (args[0].equalsIgnoreCase("clear")) {
					if (names == null || names.isEmpty()) {
						commandsender.sendMessage(ChatColor.BLUE + "(No names set for " + type + ")");
					} else {
						names.clear();
						commandsender.sendMessage(ChatColor.GREEN.toString() + type + " names have been cleared!");
						plugin.saveNames();
					}
				}
			}
			if (args.length == 3) {
				if (args[0].equalsIgnoreCase("add")) {
					if (names == null) {
						plugin.entityNames.put(type, new ArrayList<String>(Arrays.asList(args[2])));
					} else {
						names.add(args[2]);
					}
					commandsender.sendMessage(String.format("%s'%s'%s has been added to %s names",
							ChatColor.AQUA.toString(), args[2], ChatColor.GREEN.toString(), type.getName()));
					plugin.saveNames();
					return true;
				}

				if (args[0].equalsIgnoreCase("remove")) {
					if (names == null) {
						commandsender.sendMessage(ChatColor.BLUE + "(No names set for " + type + ")");
					} else {
						boolean removed = false;
						for (int i = 0; i < names.size() && !removed; ++i) {
							if (names.get(i).equalsIgnoreCase(args[2])) {
								names.remove(i);
							}
						}
						if (removed) {
							commandsender.sendMessage(String.format("%s'%s'%s has been removed from %s names",
									ChatColor.AQUA.toString(), args[2], ChatColor.GREEN.toString(), type.getName()));
							plugin.saveNames();
						} else {
							commandsender.sendMessage(String.format("%s'%s'%s was not found in %s names.. did you misspell it?",
									ChatColor.GREEN.toString(), args[2], ChatColor.AQUA.toString(), type.getName()));
						}
					}
					return true;
				}
			}
			return false;
		}
		return false;
	}

	public EntityType getType(String search) {
		EntityType type = EntityType.valueOf(search.toUpperCase());
		if (type == null) {
			type = EntityType.fromName(search);
			if (type == null) {
				// different from game setting?
				if (search.equalsIgnoreCase("IronGolem") || search.equalsIgnoreCase("Villager_Golem")) {
					type = EntityType.IRON_GOLEM;
				} else if (search.equalsIgnoreCase("Mooshroom") || search.equalsIgnoreCase("Mushroom_Cow")) {
					type = EntityType.MUSHROOM_COW;
				} else if (search.equalsIgnoreCase("Cat")) {
					type = EntityType.OCELOT;
				} else if (search.equalsIgnoreCase("Dog")) {
					type = EntityType.WOLF;
				} else if (search.equalsIgnoreCase("Dragon")) {
					type = EntityType.ENDER_DRAGON;
				} else if (search.equalsIgnoreCase("MagmaCube") || search.equalsIgnoreCase("LavaCube") || search.equalsIgnoreCase("MagmaSlime")) {
					type = EntityType.MAGMA_CUBE;
				} else if (search.equalsIgnoreCase("SnowGolem")) {
					type = EntityType.SNOWMAN;
				} else if (search.equalsIgnoreCase("ZombiePigman") || search.equalsIgnoreCase("Pigman")) {
					type = EntityType.PIG_ZOMBIE;
				}
			}
		}
		return type;
	}

	public void setVisibilityAll(boolean value) {
		for (World w : plugin.getServer().getWorlds()) {
			for (Entity e : w.getEntities()) {
				if (e.getType().isAlive() && !e.getType().equals(EntityType.PLAYER)) {
					((LivingEntity) e).setCustomNameVisible(value);
				}
			}
		}
	}
}
