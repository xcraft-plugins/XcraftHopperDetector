// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.util;

import org.bukkit.entity.Player;
import org.bukkit.command.CommandSender;

public class OutputManager
{
    private static OutputManager instance;
    private final String prefix;
    private final IOutput toConsole;
    private final IOutput toAll;
    private final IPlayerGetter playerGetter;
    
    public static OutputManager GetInstance() {
        return OutputManager.instance;
    }
    
    public static void Setup(final String prefix, final IOutput toConsole, final IOutput toAll, final IPlayerGetter playerGetter) {
        OutputManager.instance = new OutputManager(prefix, toConsole, toAll, playerGetter);
    }
    
    private OutputManager(final String prefix, final IOutput toConsole, final IOutput toAll, final IPlayerGetter playerGetter) {
        this.prefix = prefix;
        this.toConsole = toConsole;
        this.toAll = toAll;
        this.playerGetter = playerGetter;
    }
    
    public IOutput toSender(final CommandSender sender) {
        return new OutputToSender(sender);
    }
    
    public IOutput toSender(final String playerName) {
        final Player player = this.playerGetter.get(playerName);
        return (player == null) ? null : new OutputToSender((CommandSender)player);
    }
    
    public IOutput toConsole() {
        return this.toConsole;
    }
    
    public IOutput toAll() {
        return this.toAll;
    }
    
    public IOutput prefix(final IOutput output) {
        return new OutputPrefix(output);
    }
    
    public IOutput prefix(final String prefix, final IOutput output) {
        return new OutputPrefix(prefix, output);
    }
    
    public IOutput combo(final IOutput[] outputs) {
        return new OutputCombo(outputs);
    }
    
    static {
        OutputManager.instance = null;
    }
    
    protected class OutputToSender implements IOutput
    {
        final CommandSender sender;
        
        public OutputToSender(final CommandSender sender) {
            this.sender = sender;
        }
        
        @Override
        public void output(final String message) {
            this.sender.sendMessage(message);
        }
    }
    
    protected class OutputPrefix implements IOutput
    {
        final String prefix;
        final IOutput output;
        
        public OutputPrefix(final String prefix, final IOutput output) {
            this.prefix = prefix;
            this.output = output;
        }
        
        public OutputPrefix(final IOutput output) {
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
    
    protected class OutputCombo implements IOutput
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
