package net.ultibyte.AutoMobNamer;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import me.jascotty2.lib.io.FileIO;

import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class AutoMobNamer extends JavaPlugin implements Listener {

	// Lists
	Map<EntityType, List<String>> entityNames = new EnumMap<EntityType, List<String>>(EntityType.class);
	
	// Settings
	boolean allwaysshowtags = true;

	private static Random rand = new Random();

	public AutoMobNamer() {
		rand.setSeed(System.currentTimeMillis());
	}
	
	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);
		loadConfig();
		try {
			MetricsLite metrics = new MetricsLite(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the Metrics stats :-(
		}

		getCommand("namer").setExecutor(new CommandHandler(this));

		Server server = this.getServer();
		ConsoleCommandSender console = server.getConsoleSender();
		console.sendMessage(ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "has been " + ChatColor.GREEN + ChatColor.BOLD + "enabled" + ChatColor.RESET + "!");
		VariableStuff.NoColor = false;
		
		// scan for any existing entities without names
		for(final World w : getServer().getWorlds()) {
			for(final LivingEntity e : w.getEntitiesByClass(LivingEntity.class)) {
				if(e.getCustomName() == null || e.getCustomName().isEmpty()) {
					setRandomName(e);
				}
			}
		}
	}

	@Override
	public void onDisable() {
		Server server = this.getServer();
		ConsoleCommandSender console = server.getConsoleSender();
		console.sendMessage(ChatColor.DARK_RED + "Auto" + ChatColor.BLUE + "Mob" + ChatColor.YELLOW + "Namer " + ChatColor.RESET + "has been " + ChatColor.RED + ChatColor.BOLD + "disabled" + ChatColor.RESET + ".");
		saveConfig();
	}
	List<String> colors = new ArrayList<String>();

	public String RandomColorMethod() {
		int ColorSelection = randInt(0, colors.size());
		String RandomlySelectedColor = colors.get(ColorSelection);
		return RandomlySelectedColor;
	}

	public void loadConfig() {
		getConfig().options().copyDefaults(true);

		allwaysshowtags = getConfig().getBoolean("a_settings.allwaysshowtags");
		try {
			// changed to names.yml
			FileIO.extractResource("names.yml", this.getDataFolder(), AutoMobNamer.class, FileIO.OVERWRITE_CASE.NEVER);

			List<String> nameLines = FileIO.loadFile(new File(this.getDataFolder(), "names.yml"));

			for (int i = 0; i < nameLines.size(); ++i) {
				final String line = nameLines.get(i).trim();
				if (!line.startsWith("#") && line.endsWith(":")) {
					EntityType type = EntityType.valueOf(line.substring(0, line.length() - 1));
					if(type != null) {
						if(!entityNames.containsKey(type)){
							entityNames.put(type, new ArrayList<String>());
						}
						i = loadSection(nameLines, i + 1, entityNames.get(type));
					} else {
						getLogger().info("Unknown Type \"" + line.substring(0, line.length() - 1) + "\" in names.yml");
					}
				}
			}
		} catch (Exception ex) {
			getLogger().log(Level.SEVERE, "Error loading names", ex);
		}

		
		colors.clear();
		if (VariableStuff.NoColor) {
			colors.add("§f");
		} else {
			colors.addAll(Arrays.asList(
					"§0", "§1", "§2", "§3", "§4", 
					"§5", "§6", "§7", "§8", "§9", 
					"§a", "§b", "§c", "§d", "§e", "§f"));
		}
	}
	
	public void saveNames() {
		StringBuilder namesFile = new StringBuilder("# Section names from https://github.com/Bukkit/Bukkit/blob/master/src/main/java/org/bukkit/entity/EntityType.java\n");
		for(Map.Entry<EntityType, List<String>> e : entityNames.entrySet()) {
			namesFile.append(e.getKey().name()).append(":\n");
			for(String name : e.getValue()) {
				namesFile.append("- ").append(name).append("\n");
			}
		}
		try {
			FileIO.saveFile(new File(this.getDataFolder(), "names.yml"), namesFile.toString());
		} catch (IOException ex) {
			getLogger().log(Level.SEVERE, "Error saving Names File", ex);
		}
	}

	private int loadSection(List<String> data, int start, List<String> saveTo) {
		int i = start;
		for (; i < data.size(); ++i) {
			final String line = data.get(i).trim();
			if (line.startsWith("#")) {
				continue;
			} else if (line.startsWith("-")) {
				saveTo.add(line.substring(1).trim());
			} else {
				break;
			}
		}
		return i - 1;
	}

	@EventHandler
	public void onCreatureSpawn(CreatureSpawnEvent event) {
		setRandomName(event.getEntity());
	}

	public void setRandomName(LivingEntity ent) {
		List<String> nameList = entityNames.get(ent.getType());
		if(nameList != null) {
			
			ent.setCustomName(RandomColorMethod() + nameList.get(randInt(0, nameList.size())));
			if (allwaysshowtags) {
				ent.setCustomNameVisible(true);
			}
		}
	}

	public int randInt(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
}
