// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.commands;

import me.hwei.bukkit.hopperDetector.util.UsageException;
import me.hwei.bukkit.hopperDetector.util.IOutput;
import me.hwei.bukkit.hopperDetector.util.OutputManager;
import org.bukkit.command.CommandSender;
import me.hwei.bukkit.hopperDetector.HopperDetector;
import me.hwei.bukkit.hopperDetector.util.AbstractCommand;

public class StopCommand extends AbstractCommand
{
    private HopperDetector plugin;
    
    public StopCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        final IOutput toSender = OutputManager.GetInstance().toSender(sender);
        if (this.plugin.stop()) {
            toSender.output("Successfully stoped.");
        }
        else {
            toSender.output("Already stoped.");
        }
        return true;
    }
}
