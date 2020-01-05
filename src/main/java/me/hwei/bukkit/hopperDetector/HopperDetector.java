// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector;

import org.bukkit.entity.Player;
import me.hwei.bukkit.hopperDetector.util.UsageException;
import me.hwei.bukkit.hopperDetector.util.PermissionsException;
import org.bukkit.command.Command;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import java.util.Collection;
import java.util.TreeMap;
import java.util.Comparator;
import me.hwei.bukkit.hopperDetector.commands.StatusCommand;
import me.hwei.bukkit.hopperDetector.commands.BreakCommand;
import me.hwei.bukkit.hopperDetector.commands.TeleportCommand;
import me.hwei.bukkit.hopperDetector.commands.StopCommand;
import me.hwei.bukkit.hopperDetector.commands.StartCommand;
import me.hwei.bukkit.hopperDetector.commands.ListCommand;
import org.bukkit.plugin.Plugin;
import java.util.ArrayList;
import me.hwei.bukkit.hopperDetector.util.OutputManager;
import org.bukkit.ChatColor;
import me.hwei.bukkit.hopperDetector.util.AbstractCommand;
import me.hwei.bukkit.hopperDetector.util.IOutput;
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
    private HashMap<Location, AtomicInteger> redstoneActivityTable;
    private List<Map.Entry<Location, AtomicInteger>> redstoneActivityList;
    private Worker worker;
    private CommandSender sender;
    private int taskId;
    private IOutput toConsole;
    private AbstractCommand topCommand;
    
    public HopperDetector() {
        this.redstoneActivityTable = null;
        this.redstoneActivityList = null;
        this.worker = null;
        this.sender = null;
        this.taskId = Integer.MIN_VALUE;
        this.toConsole = null;
        this.topCommand = null;
    }
    
    public void onDisable() {
        this.stop();
        this.redstoneActivityTable = null;
        this.redstoneActivityList = null;
        this.toConsole.output("Disabled.");
    }
    
    public void onEnable() {
        final IOutput toConsole = message -> this.getServer().getConsoleSender().sendMessage(message);
        final IOutput toAll = message -> this.getServer().broadcastMessage(message);
        final OutputManager.IPlayerGetter playerGetter = name -> this.getServer().getPlayer(name);
        final String pluginName = this.getDescription().getName();
        OutputManager.Setup("[" + ChatColor.YELLOW + pluginName + ChatColor.WHITE + "] ", toConsole, toAll, playerGetter);
        (this.toConsole = OutputManager.GetInstance().prefix(toConsole)).output("Enabled.");
        this.redstoneActivityTable = new HashMap<Location, AtomicInteger>();
        this.redstoneActivityList = new ArrayList<Map.Entry<Location, AtomicInteger>>();
        this.stop();
        this.setupCommands();
        this.getServer().getPluginManager().registerEvents((Listener)this, (Plugin)this);
    }
    
    private void setupCommands() {
        try {
            final ListCommand listCommand = new ListCommand("list [page]  List locations of hopper activities.", "hopperdetector.list", null, this);
            final AbstractCommand[] childCommands = { new StartCommand("<sec>  Start scan for <sec> seconds.", "hopperdetector.start", null, this, listCommand), new StopCommand("stop  Stop scan.", "hopperdetector.stop", null, this), listCommand, new TeleportCommand("tp [player] [num]  Teleport player [player] to place of number [num] in list.", "hopperdetector.tp", null, this), new BreakCommand("break <num>  Break the block at place of number <num> in list.", "hopperdetector.break", null, this) };
            this.topCommand = new StatusCommand("  Status of plugin.", "hopperdetector", childCommands, this);
        }
        catch (Exception e) {
            this.toConsole.output("Can not setup commands!");
            e.printStackTrace();
        }
    }
    
    public List<Map.Entry<Location, AtomicInteger>> getRedstoneActivityList() {
        return this.redstoneActivityList;
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
            this.redstoneActivityTable.clear();
            return true;
        }
        return false;
    }
    
    private void sortList() {
        class ValueComparator implements Comparator<Location>
        {
            private Map<Location, AtomicInteger> base;
            
            ValueComparator() {
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
        final ValueComparator bvc = new ValueComparator();
        final TreeMap<Location, AtomicInteger> sortedMap = new TreeMap<Location, AtomicInteger>(bvc);
        sortedMap.putAll(this.redstoneActivityTable);
        this.redstoneActivityList.clear();
        this.redstoneActivityList.addAll(sortedMap.entrySet());
    }
    
    @EventHandler
    public void onBlockRedstoneChange(final InventoryMoveItemEvent event) {
        if (!event.getDestination().getType().equals((Object)InventoryType.HOPPER) && (event.getSource().getType().equals((Object)InventoryType.HOPPER) || event.getDestination().getType().equals((Object)InventoryType.CHEST))) {
            return;
        }
        if (this.taskId == Integer.MIN_VALUE) {
            return;
        }
        final Location loc = event.getDestination().getLocation();
        this.redstoneActivityTable.computeIfAbsent(loc, e -> new AtomicInteger()).incrementAndGet();
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
