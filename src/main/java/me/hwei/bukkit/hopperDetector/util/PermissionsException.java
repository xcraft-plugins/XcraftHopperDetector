// 
// Decompiled by Procyon v0.5.36
// 

package me.hwei.bukkit.hopperDetector.util;

public class PermissionsException extends Exception
{
    private final String perms;
    private static final long serialVersionUID = 1L;
    
    PermissionsException(final String perms) {
        this.perms = perms;
    }
    
    public String getPerms() {
        return this.perms;
    }
}
