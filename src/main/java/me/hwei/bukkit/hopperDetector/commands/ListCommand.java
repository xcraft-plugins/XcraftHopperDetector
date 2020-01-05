// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.commands;

import java.util.List;
import me.hwei.bukkit.hopperDetector.util.IOutput;
import java.util.concurrent.atomic.AtomicInteger;
import org.bukkit.Location;
import java.util.Map;
import org.bukkit.ChatColor;
import me.hwei.bukkit.hopperDetector.util.OutputManager;
import me.hwei.bukkit.hopperDetector.util.UsageException;
import org.bukkit.command.CommandSender;
import me.hwei.bukkit.hopperDetector.HopperDetector;
import me.hwei.bukkit.hopperDetector.util.AbstractCommand;

public class ListCommand extends AbstractCommand
{
    private HopperDetector plugin;
    
    public ListCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        int pageNum = 1;
        if (data.length > 0) {
            final Integer pageData = data[0].getInteger();
            if (pageData == null || pageData <= 0) {
                throw new UsageException(this.coloredUsage, "page number should be a positive integer.");
            }
            pageNum = pageData;
        }
        final IOutput toSender = OutputManager.GetInstance().toSender(sender);
        final int pageSize = 10;
        final int startIndex = (pageNum - 1) * pageSize;
        final List<Map.Entry<Location, AtomicInteger>> actList = this.plugin.getRedstoneActivityList();
        final int totalPage = (actList.size() == 0) ? 0 : ((actList.size() - 1) / pageSize + 1);
        toSender.output(String.format("Page: " + ChatColor.YELLOW + "%d" + ChatColor.WHITE + "/" + ChatColor.GOLD + "%d", pageNum, totalPage));
        if (startIndex >= actList.size()) {
            toSender.output(ChatColor.GRAY.toString() + "No data.");
        }
        else {
            for (int i = startIndex, e = Math.min(startIndex + pageSize, actList.size()); i < e; ++i) {
                final Map.Entry<Location, AtomicInteger> entry = actList.get(i);
                final Location l = entry.getKey();
                toSender.output(String.format(ChatColor.YELLOW.toString() + "%d" + ChatColor.WHITE + ". " + ChatColor.GREEN + "(%d, %d, %d) %s " + ChatColor.DARK_GREEN + "%d", i + 1, l.getBlockX(), l.getBlockY(), l.getBlockZ(), l.getWorld().getName(), entry.getValue()));
            }
        }
        return true;
    }
}
