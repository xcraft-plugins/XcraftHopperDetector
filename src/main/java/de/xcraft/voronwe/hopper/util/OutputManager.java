// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.util;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class OutputManager
{
    private static OutputManager instance;
    private final String prefix;
    
    private OutputManager(final String prefix) {
        this.prefix = prefix;
    }
    
    public static OutputManager GetInstance() {
        return OutputManager.instance;
    }
    
    public static void Setup(final String prefix) {
        OutputManager.instance = new OutputManager(prefix);
    }
    
    public IOutput toSender(final CommandSender sender) {
        return new OutputToSender(sender);
    }
    
    public IOutput prefix(final IOutput output) {
        return new OutputPrefix(output);
    }
    
    static {
        OutputManager.instance = null;
    }
    
    class OutputToSender implements IOutput
    {
        final CommandSender sender;
        
        OutputToSender(final CommandSender sender) {
            this.sender = sender;
        }
        
        @Override
        public void output(final String message) {
            this.sender.sendMessage(message);
        }
    }
    
    class OutputPrefix implements IOutput
    {
        final String prefix;
        final IOutput output;
        
        OutputPrefix(final IOutput output) {
            this.prefix = OutputManager.this.prefix;
            this.output = output;
        }
        
        @Override
        public void output(final String message) {
            if (this.output == null) {
                return;
            }
            final String prefixedMessage = this.prefix + message;
            this.output.output(prefixedMessage);
        }
    }
    
    class OutputCombo implements IOutput
    {
        final IOutput[] outputs;
        
        public OutputCombo(final IOutput[] outputs) {
            this.outputs = outputs;
        }
        
        @Override
        public void output(final String message) {
            for (final IOutput output : this.outputs) {
                output.output(message);
            }
        }
    }
    
    public interface IPlayerGetter
    {
        Player get(final String p0);
    }
}
