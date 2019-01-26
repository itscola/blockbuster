package mchorse.blockbuster.commands.action;

import mchorse.blockbuster.CommonProxy;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;

/**
 * Sub-command /action stop
 *
 * This sub-command is responsible for stopping the action recording of current
 * player.
 */
public class SubCommandActionStop extends CommandBase
{
    @Override
    public String getName()
    {
        return "stop";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "blockbuster.commands.action.stop";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        CommonProxy.manager.stopRecording(getCommandSenderAsPlayer(sender), false, true);
    }
}