// 
// Decompiled by Procyon v0.5.36
// 

package de.xcraft.voronwe.hopper.util;

import java.util.Iterator;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public abstract class AbstractCommand
{
    private final String perm;
    private final AbstractCommand[] children;
    protected String coloredUsage;
    private Token[] tokens;
    
    protected AbstractCommand(final String usage, final String perm, final AbstractCommand[] children) throws Exception {
        this.perm = perm;
        this.children = ((children == null) ? new AbstractCommand[0] : children);
        this.buildTokens(usage);
    }
    
    public void showUsage(final CommandSender sender, final String rootCommand) {
        if (sender.hasPermission(this.perm)) {
            final IOutput toSender = OutputManager.GetInstance().toSender(sender);
            toSender.output(ChatColor.YELLOW.toString() + rootCommand + ChatColor.WHITE + " " + this.coloredUsage);
        }
        for (final AbstractCommand child : this.children) {
            child.showUsage(sender, rootCommand);
        }
    }
    
    public boolean execute(final CommandSender sender, final String... args) throws PermissionsException, UsageException {
        return this.execute(sender, args, new MatchResult[0]);
    }
    
    private boolean execute(final CommandSender sender, final String[] args, final MatchResult[] matchResults) throws PermissionsException, UsageException {
        final List<String> argList = new ArrayList<String>(Arrays.asList(args));
        for (int i = 0; i < this.tokens.length - args.length; ++i) {
            argList.add(null);
        }
        final List<MatchResult> matchResultList = new ArrayList<MatchResult>(Arrays.asList(matchResults));
        for (int j = matchResultList.size(); j < this.tokens.length; ++j) {
            final MatchResult matchResult = this.tokens[j].match(argList.get(j));
            if (matchResult == null) {
                return false;
            }
            matchResultList.add(matchResult);
        }
        if (args.length > this.tokens.length) {
            boolean matched = false;
            for (final AbstractCommand child : this.children) {
                matched = (matched || child.execute(sender, args, matchResultList.toArray(new MatchResult[0])));
            }
            return matched;
        }
        if (this.perm != null && !sender.hasPermission(this.perm)) {
            throw new PermissionsException(this.perm);
        }
        final List<MatchResult> dataList = new ArrayList<MatchResult>(args.length);
        for (final MatchResult matchResult2 : matchResultList) {
            if (matchResult2.hasData()) {
                dataList.add(matchResult2);
            }
        }
        return this.execute(sender, dataList.toArray(new MatchResult[0]));
    }
    
    protected abstract boolean execute(final CommandSender p0, final MatchResult[] p1) throws UsageException;
    
    private void buildTokens(final String usage) throws Exception {
        final int splitPos = usage.indexOf("  ");
        String tokenDefine;
        if (splitPos == -1) {
            tokenDefine = usage;
            this.coloredUsage = tokenDefine;
        }
        else {
            tokenDefine = usage.substring(0, splitPos);
            this.coloredUsage = tokenDefine + ChatColor.GRAY + usage.substring(splitPos);
        }
        final String[] tokenStrings = tokenDefine.split(" ");
        final List<Token> tokenList = new ArrayList<Token>(tokenStrings.length);
        for (final String tokenString : tokenStrings) {
            if (!tokenString.isEmpty()) {
                tokenList.add(new Token(tokenString));
            }
        }
        this.tokens = tokenList.toArray(new Token[0]);
        for (final AbstractCommand child : this.children) {
            if (child.tokens.length < this.tokens.length) {
                throw new Exception("Child command is shorter than parent command.");
            }
            for (int i = 0; i < this.tokens.length; ++i) {
                if (!this.tokens[i].equals(child.tokens[i])) {
                    throw new Exception("Child command does not have a prefix of parent command.");
                }
            }
        }
    }
    
    static class Token
    {
        private final boolean optional;
        private final String template;
        
        Token(final String template) {
            if (template.length() >= 2) {
                if (template.charAt(0) == '[' && template.charAt(template.length() - 1) == ']') {
                    this.template = null;
                    this.optional = true;
                    return;
                }
                if (template.charAt(0) == '<' && template.charAt(template.length() - 1) == '>') {
                    this.template = null;
                    this.optional = false;
                    return;
                }
            }
            this.template = template;
            this.optional = false;
        }
        
        MatchResult match(final String part) {
            if (part == null) {
                return this.optional ? new MatchResult() : null;
            }
            if (this.template == null) {
                return new MatchResult(part);
            }
            return this.template.equalsIgnoreCase(part) ? new MatchResult() : null;
        }
        
        boolean equals(final Token token) {
            if (this.template == null) {
                return token.template == null;
            }
            return token.template != null && this.template.equalsIgnoreCase(token.template);
        }
    }
    
    protected static class MatchResult
    {
        private final String data;
        
        MatchResult(final String data) {
            this.data = data;
        }
        
        MatchResult() {
            this.data = null;
        }
        
        boolean hasData() {
            return this.data != null;
        }
        
        public Integer getInteger() {
            if (this.data == null) {
                return null;
            }
            Integer result = null;
            try {
                result = Integer.parseInt(this.data);
            }
            catch (Exception ex) {}
            return result;
        }
        
        public String getString() {
            return this.data;
        }
    }
}
