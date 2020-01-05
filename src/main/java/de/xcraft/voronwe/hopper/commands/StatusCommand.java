// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.commands;

import de.xcraft.voronwe.hopper.util.UsageException;
import de.xcraft.voronwe.hopper.util.IOutput;
import de.xcraft.voronwe.hopper.util.OutputManager;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.ChatColor;
import de.xcraft.voronwe.hopper.HopperDetector;
import de.xcraft.voronwe.hopper.util.AbstractCommand;

public class StatusCommand extends AbstractCommand
{
    private HopperDetector plugin;
    private String pluginInfo;
    
    public StatusCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
        final PluginDescriptionFile des = plugin.getDescription();
        this.pluginInfo = String.format("Version: " + ChatColor.YELLOW + "%s" + ChatColor.WHITE + ", Author: " + ChatColor.YELLOW + "%s", des.getVersion(), des.getAuthors().get(0));
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        final IOutput toSender = OutputManager.GetInstance().toSender(sender);
        OutputManager.GetInstance().prefix(toSender).output(this.pluginInfo);
        final CommandSender user = this.plugin.getUser();
        if (user != null) {
            toSender.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "has started a scan, remaining " + ChatColor.YELLOW + "%d " + ChatColor.WHITE + "seconds to finish.", user.getName(), this.plugin.getSecondsRemain()));
        }
        return true;
    }
}
