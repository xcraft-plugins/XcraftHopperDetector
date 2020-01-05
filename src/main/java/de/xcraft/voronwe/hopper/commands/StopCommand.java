// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.commands;

import de.xcraft.voronwe.hopper.util.UsageException;
import de.xcraft.voronwe.hopper.util.IOutput;
import de.xcraft.voronwe.hopper.util.OutputManager;
import org.bukkit.command.CommandSender;
import de.xcraft.voronwe.hopper.HopperDetector;
import de.xcraft.voronwe.hopper.util.AbstractCommand;

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
