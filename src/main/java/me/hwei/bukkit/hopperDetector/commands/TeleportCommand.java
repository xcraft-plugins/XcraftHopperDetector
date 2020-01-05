// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.commands;

import java.util.List;
import me.hwei.bukkit.hopperDetector.util.IOutput;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.ChatColor;
import me.hwei.bukkit.hopperDetector.util.UsageException;
import me.hwei.bukkit.hopperDetector.util.OutputManager;
import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;
import me.hwei.bukkit.hopperDetector.HopperDetector;
import me.hwei.bukkit.hopperDetector.util.AbstractCommand;

public class TeleportCommand extends AbstractCommand
{
    private HopperDetector plugin;
    
    public TeleportCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        Player player = (sender instanceof Player) ? (Player) sender : null;
        int tpNum = 0;
        final OutputManager outputManager = OutputManager.GetInstance();
        final IOutput toSender = outputManager.toSender(sender);
        if (data.length == 0) {
            if (player == null) {
                throw new UsageException(this.coloredUsage, "Must specify which player to teleport.");
            }
        }
        else if (data.length == 1) {
            if (player == null) {
                final String playerName = data[0].getString();
                player = this.plugin.getServer().getPlayer(playerName);
                if (player == null) {
                    toSender.output(String.format("Can not find player " + ChatColor.GREEN + "%d" + ChatColor.WHITE + ".", playerName));
                    return true;
                }
            }
            else {
                final Integer numData = data[0].getInteger();
                if (numData == null || numData <= 0) {
                    throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
                }
                tpNum = numData - 1;
            }
        }
        else if (data.length == 2) {
            final String playerName = data[0].getString();
            player = this.plugin.getServer().getPlayer(playerName);
            if (player == null) {
                toSender.output(String.format("Can not find player " + ChatColor.GREEN + "%d" + ChatColor.WHITE + ".", playerName));
                return true;
            }
            final Integer numData2 = data[1].getInteger();
            if (numData2 == null || numData2 <= 0) {
                throw new UsageException(this.coloredUsage, "Location num must be a positive integer.");
            }
            tpNum = numData2 - 1;
        }
        final List<Map.Entry<Location, AtomicInteger>> actList = this.plugin.getRedstoneActivityList();
        if (tpNum >= actList.size()) {
            toSender.output(String.format("Location num " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "dose not exist.", tpNum + 1));
        }
        else {
            player.teleport((Location)actList.get(tpNum).getKey());
            final IOutput toPlayer = outputManager.prefix(outputManager.toSender((CommandSender)player));
            if (player == sender) {
                toPlayer.output("Teleporting...");
            }
            else {
                toPlayer.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "is teleporting you...", sender.getName()));
            }
        }
        return true;
    }
}
