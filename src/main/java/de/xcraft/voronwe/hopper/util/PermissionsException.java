// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.util;

public class PermissionsException extends Exception
{
    private static final long serialVersionUID = 1L;
    private final String perms;
    
    PermissionsException(final String perms) {
        this.perms = perms;
    }
    
    public String getPerms() {
        return this.perms;
    }
}
