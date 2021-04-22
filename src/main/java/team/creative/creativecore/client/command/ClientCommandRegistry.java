package team.creative.creativecore.client.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponentUtils;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import team.creative.creativecore.CreativeCore;

public class ClientCommandRegistry {
    
    private static Minecraft mc = Minecraft.getInstance();
    
    private static CommandDispatcher<ISuggestionProvider> clientDispatcher = new CommandDispatcher<>();
    private static CombinedCommandDispatcher<ISuggestionProvider> combined = null;
    
    public static CommandDispatcher<ISuggestionProvider> getDispatcher(CommandDispatcher<ISuggestionProvider> vanillaDispatcher) {
        if (combined == null || !combined.is(vanillaDispatcher, clientDispatcher))
            combined = new CombinedCommandDispatcher<>(vanillaDispatcher, clientDispatcher);
        return combined;
    }
    
    public static LiteralCommandNode<ISuggestionProvider> register(final LiteralArgumentBuilder<ISuggestionProvider> command) {
        return clientDispatcher.register(command);
    }
    
    public static int handleCommand(CommandSource source, String command) {
        StringReader stringreader = new StringReader(command);
        if (stringreader.canRead() && stringreader.peek() == '/') {
            stringreader.skip();
        }
        
        mc.getProfiler().push(command);
        
        try {
            try {
                ParseResults<ISuggestionProvider> parse = clientDispatcher.parse(stringreader, source);
                return clientDispatcher.execute(parse);
            } catch (CommandException commandexception) {
                source.sendFailure(commandexception.getComponent());
                return 0;
            } catch (CommandSyntaxException commandsyntaxexception) {
                if (commandsyntaxexception.getType() == CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownCommand())
                    return -1;
                source.sendFailure(TextComponentUtils.fromMessage(commandsyntaxexception.getRawMessage()));
                if (commandsyntaxexception.getInput() != null && commandsyntaxexception.getCursor() >= 0) {
                    int k = Math.min(commandsyntaxexception.getInput().length(), commandsyntaxexception.getCursor());
                    IFormattableTextComponent itextcomponent1 = (new StringTextComponent("")).withStyle(TextFormatting.GRAY).withStyle((p_211705_1_) -> {
                        return p_211705_1_.withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, command));
                    });
                    if (k > 10) {
                        itextcomponent1.append(new StringTextComponent("..."));
                    }
                    
                    itextcomponent1.append(new StringTextComponent(commandsyntaxexception.getInput().substring(Math.max(0, k - 10), k)));
                    if (k < commandsyntaxexception.getInput().length()) {
                        ITextComponent itextcomponent2 = (new StringTextComponent(commandsyntaxexception.getInput().substring(k)))
                                .withStyle(new TextFormatting[] { TextFormatting.RED, TextFormatting.UNDERLINE });
                        itextcomponent1.append(itextcomponent2);
                    }
                    
                    itextcomponent1.append((new TranslationTextComponent("command.context.here")).withStyle(new TextFormatting[] { TextFormatting.RED, TextFormatting.ITALIC }));
                    source.sendFailure(itextcomponent1);
                }
            } catch (Exception exception) {
                StringTextComponent stringtextcomponent = new StringTextComponent(exception.getMessage() == null ? exception.getClass().getName() : exception.getMessage());
                IFormattableTextComponent itextcomponent = stringtextcomponent;
                if (CreativeCore.LOGGER.isDebugEnabled()) {
                    StackTraceElement[] astacktraceelement = exception.getStackTrace();
                    
                    for (int j = 0; j < Math.min(astacktraceelement.length, 3); ++j) {
                        itextcomponent.append("\n\n").append(astacktraceelement[j].getMethodName()).append("\n ").append(astacktraceelement[j].getFileName()).append(":")
                                .append(String.valueOf(astacktraceelement[j].getLineNumber()));
                    }
                }
                
                source.sendFailure((new TranslationTextComponent("command.failed")).withStyle((p_211704_1_) -> {
                    return p_211704_1_.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, itextcomponent));
                }));
                return 0;
            }
            
            return 0;
        } finally {
            mc.getProfiler().pop();
        }
    }
    
    public static class CombinedCommandDispatcher<T> extends CommandDispatcher<T> {
        
        public final CommandDispatcher<T> first;
        public final CommandDispatcher<T> second;
        
        public CombinedCommandDispatcher(CommandDispatcher<T> first, CommandDispatcher<T> second) {
            super();
            this.first = first;
            this.second = second;
            RootCommandNode<T> root = getRoot();
            for (CommandNode<T> node : first.getRoot().getChildren())
                root.addChild(node);
            for (CommandNode<T> node : second.getRoot().getChildren())
                root.addChild(node);
            
        }
        
        public boolean is(CommandDispatcher<T> first, CommandDispatcher<T> second) {
            return this.first == first && this.second == second;
        }
        
    }
}