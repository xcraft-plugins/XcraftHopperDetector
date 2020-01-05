// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.util;

public class UsageException extends Exception
{
    private final String usage;
    private final String message;
    private static final long serialVersionUID = 1L;
    
    public UsageException(final String usage, final String message) {
        this.usage = usage;
        this.message = message;
    }
    
    public String getUsage() {
        return this.usage;
    }
    
    @Override
    public String getMessage() {
        return this.message;
    }
}
