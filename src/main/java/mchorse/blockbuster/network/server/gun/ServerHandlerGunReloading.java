package mchorse.blockbuster.network.server.gun;

import mchorse.blockbuster.common.GunProps;
import mchorse.blockbuster.common.item.ItemGun;
import mchorse.blockbuster.network.Dispatcher;
import mchorse.blockbuster.network.common.guns.PacketGunInfo;
import mchorse.blockbuster.network.common.guns.PacketGunReloading;
import mchorse.blockbuster.utils.NBTUtils;
import mchorse.mclib.network.ServerMessageHandler;
import mchorse.mclib.utils.OpHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;

/**
 * \* User: Evanechecssss
 * \* https://evanechecssss.github.io
 * \
 */
public class ServerHandlerGunReloading extends ServerMessageHandler<PacketGunReloading>
{
    @Override
    public void run(EntityPlayerMP entityPlayerMP, PacketGunReloading packet)
    {
        if (!(packet.stack.getItem() instanceof ItemGun))
        {
            return;
        }

        ItemGun gun = (ItemGun) packet.stack.getItem();
        Entity entity = entityPlayerMP.world.getEntityByID(packet.id);
        GunProps props = NBTUtils.getGunProps(packet.stack);

        if (props == null)
        {
            return;
        }

        if (entity instanceof EntityPlayer)
        {
            EntityPlayer player = (EntityPlayer) entity;

            if (props.state == ItemGun.GunState.NEED_TO_BE_RELOAD)
            {
                if (!player.capabilities.isCreativeMode && !props.ammoStack.isEmpty())
                {
                    ItemStack ammo = props.ammoStack;

                    if (gun.consumeAmmoStack(player, ammo))
                    {
                        props.state = ItemGun.GunState.RELOADING;

                        this.update((EntityPlayerMP) player, props.toNBT());
                        this.ammo(packet.stack, props, player);
                    }
                }
                else
                {
                    props.state = ItemGun.GunState.RELOADING;

                    this.update((EntityPlayerMP) player, props.toNBT());
                    this.ammo(packet.stack, props, player);
                }
            }
        }
    }

    private void ammo(ItemStack stack, GunProps props, EntityPlayer player)
    {
        props.state = ItemGun.GunState.RELOADING;
        props.storedReloadingTime = props.reloadingTime;

        if (!props.reloadCommand.isEmpty())
        {
            player.getServer().commandManager.executeCommand(player, props.reloadCommand);
        }

        this.update((EntityPlayerMP) player, props.toNBT());
    }

    private void update(EntityPlayerMP player, NBTTagCompound compound)
    {
        if (!OpHelper.isPlayerOp(player))
        {
            return;
        }

        ItemStack stack = player.getHeldItemMainhand();

        if (NBTUtils.saveGunProps(stack, compound))
        {
            IMessage packet = new PacketGunInfo(compound, player.getEntityId());

            Dispatcher.sendTo(packet, player);
            Dispatcher.sendToTracked(player, packet);
        }
    }
}