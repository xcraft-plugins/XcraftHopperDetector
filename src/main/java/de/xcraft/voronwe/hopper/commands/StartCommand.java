// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.commands;

import de.xcraft.voronwe.hopper.util.IOutput;
import org.bukkit.ChatColor;
import de.xcraft.voronwe.hopper.util.OutputManager;
import de.xcraft.voronwe.hopper.util.UsageException;
import org.bukkit.command.CommandSender;
import de.xcraft.voronwe.hopper.HopperDetector;
import de.xcraft.voronwe.hopper.util.AbstractCommand;

public class StartCommand extends AbstractCommand
{
    private HopperDetector plugin;
    private AbstractCommand listCommand;
    
    public StartCommand(final String usage, final String perm, final AbstractCommand[] children, final HopperDetector plugin, final AbstractCommand listCommand) throws Exception {
        super(usage, perm, children);
        this.plugin = plugin;
        this.listCommand = listCommand;
    }
    
    @Override
    protected boolean execute(final CommandSender sender, final MatchResult[] data) throws UsageException {
        final Integer seconds = data[0].getInteger();
        if (seconds == null) {
            return false;
        }
        if (seconds <= 0) {
            throw new UsageException(this.coloredUsage, "seconds number should be a positive integer.");
        }
        final CommandSender user = this.plugin.getUser();
        final OutputManager outputManager = OutputManager.GetInstance();
        final IOutput toSender = outputManager.toSender(sender);
        if (user != null) {
            toSender.output(String.format(ChatColor.GREEN.toString() + "%s " + ChatColor.WHITE + "has already started a scan.", user.getName()));
            return true;
        }
        final IOutput toSenderPrefix = outputManager.prefix(outputManager.toSender(sender));
        this.plugin.start(sender, seconds, new ProgressReporter(toSenderPrefix, new FinishCallback(this.listCommand, sender)));
        toSender.output(String.format("Start a scan of %d seconds.", seconds));
        return true;
    }
    
    protected class ProgressReporter implements HopperDetector.IProgressReporter
    {
        final IOutput toSender;
        final FinishCallback finishCallback;
        
        ProgressReporter(final IOutput toSender, final FinishCallback finishCallback) {
            this.toSender = toSender;
            this.finishCallback = finishCallback;
        }
        
        @Override
        public void onProgress(final int secondsRemain) {
            if (secondsRemain <= 0) {
                this.finishCallback.onFinish();
            }
            else if (secondsRemain <= 5 || (secondsRemain <= 60 && secondsRemain % 10 == 0)) {
                this.toSender.output(String.format("Remain %d seconds.", secondsRemain));
            }
            else if (secondsRemain % 60 == 0) {
                this.toSender.output(String.format("Remain %d minutes.", secondsRemain / 60));
            }
        }
    }
    
    class FinishCallback
    {
        final AbstractCommand listCommand;
        final CommandSender sender;
        
        FinishCallback(final AbstractCommand listCommand, final CommandSender sender) {
            this.listCommand = listCommand;
            this.sender = sender;
        }
        
        void onFinish() {
            try {
                this.listCommand.execute(this.sender, "list");
            }
            catch (Exception ex) {}
        }
    }
}
