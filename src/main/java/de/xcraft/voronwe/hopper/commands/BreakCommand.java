// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.commands;

import org.bukkit.block.Block;
import java.util.List;
import de.xcraft.voronwe.hopper.util.IOutput;
import org.bukkit.block.Sign;
import org.bukkit.Material;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import de.xcraft.voronwe.hopper.util.OutputManager;
import de.xcraft.voronwe.hopper.util.UsageException;
import org.bukkit.command.CommandSender;
import de.xcraft.voronwe.hopper.HopperDetector;
import de.xcraft.voronwe.hopper.util.AbstractCommand;

public class BreakCommand extends AbstractCommand
{
    private HopperDetector plugin;
    
    public BreakCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        final int tpNum = 0;
        if (data.length != 1) {
            throw new UsageException(this.coloredUsage, "Must specify location num.");
        }
        final Integer numData = data[0].getInteger();
        if (numData == null || numData <= 0) {
            throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
        }
        final OutputManager outputManager = OutputManager.GetInstance();
        final IOutput toSender = outputManager.toSender(sender);
        final List<Map.Entry<Location, AtomicInteger>> actList = this.plugin.getHopperActivityList();
        if (tpNum >= actList.size()) {
            toSender.output(String.format("Location num " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "dose not exist.", tpNum + 1));
        }
        else {
            final Location loc = actList.get(tpNum).getKey();
            final Block block = loc.getBlock();
            final String blockName = block.getType().toString();
            if (!block.breakNaturally()) {
                toSender.output(String.format("Can not break %s block at " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.WHITE + ".", blockName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
                return true;
            }
            block.setType(Material.OAK_SIGN);
            final Sign s = (Sign)block.getState();
            s.setLine(0, sender.getName());
            s.setLine(1, ChatColor.DARK_RED + "broke a");
            s.setLine(2, blockName);
            s.setLine(3, ChatColor.DARK_RED + "here.");
            s.update();
            toSender.output(String.format("Has Broken %s block at " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.WHITE + ".", blockName, loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getWorld().getName()));
        }
        return true;
    }
}
