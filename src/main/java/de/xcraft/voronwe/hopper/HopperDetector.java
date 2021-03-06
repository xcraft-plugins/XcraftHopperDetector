// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper;

import de.xcraft.voronwe.hopper.util.UsageException;
import de.xcraft.voronwe.hopper.util.PermissionsException;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Comparator;
import de.xcraft.voronwe.hopper.commands.StatusCommand;
import de.xcraft.voronwe.hopper.commands.TeleportCommand;
import de.xcraft.voronwe.hopper.commands.StopCommand;
import de.xcraft.voronwe.hopper.commands.StartCommand;
import de.xcraft.voronwe.hopper.commands.ListCommand;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import de.xcraft.voronwe.hopper.util.OutputManager;
import org.bukkit.ChatColor;
import de.xcraft.voronwe.hopper.util.AbstractCommand;
import de.xcraft.voronwe.hopper.util.IOutput;
import org.bukkit.command.CommandSender;
import java.util.Map;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import java.util.HashMap;
import org.bukkit.event.Listener;
import org.bukkit.command.CommandExecutor;
import org.bukkit.plugin.java.JavaPlugin;

public class HopperDetector extends JavaPlugin implements CommandExecutor, Listener
{
    private HashMap<Location, AtomicInteger> hopperActivityTable;
    private List<Map.Entry<Location, AtomicInteger>> hopperActivityList;
    private Worker worker;
    private CommandSender sender;
    private int taskId;
    private IOutput toConsole;
    private AbstractCommand topCommand;
    
    public HopperDetector() {
        this.hopperActivityTable = null;
        this.hopperActivityList = null;
        this.worker = null;
        this.sender = null;
        this.taskId = Integer.MIN_VALUE;
        this.toConsole = null;
        this.topCommand = null;
    }
    
    public void onDisable() {
        this.stop();
        this.hopperActivityTable = null;
        this.hopperActivityList = null;
        this.toConsole.output("Disabled.");
    }
    
    public void onEnable() {
        final IOutput toConsole = message -> this.getServer().getConsoleSender().sendMessage(message);
        final String pluginName = this.getDescription().getName();
        OutputManager.Setup("[" + ChatColor.YELLOW + pluginName + ChatColor.WHITE + "] ");
        (this.toConsole = OutputManager.GetInstance().prefix(toConsole)).output("Enabled.");
        this.hopperActivityTable = new HashMap<Location, AtomicInteger>();
        this.hopperActivityList = new ArrayList<Map.Entry<Location, AtomicInteger>>();
        this.stop();
        this.setupCommands();
        this.getServer().getPluginManager().registerEvents(this, this);
    }
    
    private void setupCommands() {
        try {
            final ListCommand listCommand = new ListCommand("list [page]  List locations of hopper activities.", "hopperdetector.list", null, this);
            final AbstractCommand[] childCommands = { new StartCommand("<sec>  Start scan for <sec> seconds.", "hopperdetector.start", null, this, listCommand), new StopCommand("stop  Stop scan.", "hopperdetector.stop", null, this), listCommand, new TeleportCommand("tp [player] [num]  Teleport player [player] to place of number [num] in list.", "hopperdetector.tp", null, this) };
            this.topCommand = new StatusCommand("  Status of plugin.", "hopperdetector", childCommands, this);
        }
        catch (Exception e) {
            this.toConsole.output("Can not setup commands!");
            e.printStackTrace();
        }
    }
    
    public List<Map.Entry<Location, AtomicInteger>> getHopperActivityList() {
        return this.hopperActivityList;
    }
    
    public CommandSender getUser() {
        return this.sender;
    }
    
    public int getSecondsRemain() {
        if (this.taskId == Integer.MIN_VALUE) {
            return -1;
        }
        return this.worker.getSecondsRemain();
    }
    
    public void start(final CommandSender sender, final int seconds, final IProgressReporter progressReporter) {
        if (this.taskId != Integer.MIN_VALUE) {
            return;
        }
        this.sender = sender;
        this.worker = new Worker(seconds, progressReporter);
        this.taskId = this.getServer().getScheduler().scheduleSyncRepeatingTask((Plugin)this, (Runnable)this.worker, 0L, 20L);
    }
    
    public boolean stop() {
        if (this.taskId != Integer.MIN_VALUE) {
            this.getServer().getScheduler().cancelTask(this.taskId);
            this.taskId = Integer.MIN_VALUE;
            this.sender = null;
            this.worker = null;
            this.sortList();
            this.hopperActivityTable.clear();
            return true;
        }
        return false;
    }
    
    private void sortList() {
        class ValueComparator implements Comparator<Location>
        {
            private Map<Location, AtomicInteger> base;
            
            ValueComparator(HashMap<Location, AtomicInteger> base) {
                this.base = base;
            }
            
            @Override
            public int compare(final Location a, final Location b) {
                if (this.base.get(a).get() < this.base.get(b).get()) {
                    return 1;
                }
                if (this.base.get(a).get() == this.base.get(b).get()) {
                    return 0;
                }
                return -1;
            }
        }
        final ValueComparator bvc = new ValueComparator(this.hopperActivityTable);
        final TreeMap<Location, AtomicInteger> sortedMap = new TreeMap<Location, AtomicInteger>(bvc);
        sortedMap.putAll(this.hopperActivityTable);
        this.hopperActivityList.clear();
        this.hopperActivityList.addAll(sortedMap.entrySet());
    }
    
    @EventHandler
    public void onHopperTransfer(final InventoryMoveItemEvent event) {
        if (this.taskId == Integer.MIN_VALUE) {
            return;
        }
        final InventoryType destination = event.getDestination().getType();
        final InventoryType source = event.getSource().getType();
        if (source.equals((Object)InventoryType.PLAYER) || destination.equals((Object)InventoryType.PLAYER)) {
            return;
        }
        final Location loc = event.getDestination().getLocation().getBlock().getLocation();
        final AtomicInteger count = this.hopperActivityTable.computeIfAbsent(loc, e -> new AtomicInteger());
        count.incrementAndGet();
    }
    
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        try {
            if (!this.topCommand.execute(sender, args)) {
                this.topCommand.showUsage(sender, command.getName());
            }
        }
        catch (PermissionsException e) {
            sender.sendMessage(String.format(ChatColor.RED.toString() + "You do not have permission of %s", e.getPerms()));
        }
        catch (UsageException e2) {
            sender.sendMessage("Usage: " + ChatColor.YELLOW + command.getName() + " " + e2.getUsage());
            sender.sendMessage(ChatColor.RED.toString() + e2.getMessage());
        }
        catch (Exception e3) {
            e3.printStackTrace();
        }
        return true;
    }
    
    class Worker implements Runnable
    {
        final IProgressReporter progressReporter;
        int secondsRemain;
        
        Worker(final int seconds, final IProgressReporter progressReporter) {
            this.progressReporter = progressReporter;
            this.secondsRemain = seconds;
        }
        
        @Override
        public void run() {
            if (this.secondsRemain <= 0) {
                if (HopperDetector.this.stop() && this.progressReporter != null) {
                    this.progressReporter.onProgress(this.secondsRemain);
                }
            }
            else {
                if (this.progressReporter != null) {
                    this.progressReporter.onProgress(this.secondsRemain);
                }
                --this.secondsRemain;
            }
        }
        
        int getSecondsRemain() {
            return this.secondsRemain;
        }
    }
    
    public interface IProgressReporter
    {
        void onProgress(final int p0);
    }
}
