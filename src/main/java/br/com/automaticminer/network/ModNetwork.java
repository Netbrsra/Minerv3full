package br.com.automaticminer.network;

import br.com.automaticminer.AutomaticMiner;
import br.com.automaticminer.tile.TileAutomaticMiner;
import io.netty.buffer.ByteBuf;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class ModNetwork {
    public static final SimpleNetworkWrapper CHANNEL =
            NetworkRegistry.INSTANCE.newSimpleChannel(AutomaticMiner.MODID);

    public static void init() {
        CHANNEL.registerMessage(MinerButtonMessage.Handler.class,
                MinerButtonMessage.class, 0, Side.SERVER);
    }

    public static void sendButton(BlockPos pos, int action) {
        CHANNEL.sendToServer(new MinerButtonMessage(pos, action));
    }

    public static class MinerButtonMessage implements IMessage {
        private BlockPos pos;
        private int action;

        public MinerButtonMessage() {}

        public MinerButtonMessage(BlockPos pos, int action) {
            this.pos = pos;
            this.action = action;
        }

        @Override
        public void fromBytes(ByteBuf buf) {
            pos = BlockPos.fromLong(buf.readLong());
            action = buf.readInt();
        }

        @Override
        public void toBytes(ByteBuf buf) {
            buf.writeLong(pos.toLong());
            buf.writeInt(action);
        }

        public static class Handler implements IMessageHandler<MinerButtonMessage, IMessage> {
            @Override
            public IMessage onMessage(final MinerButtonMessage message, final MessageContext ctx) {
                ctx.getServerHandler().player.getServerWorld().addScheduledTask(new Runnable() {
                    @Override
                    public void run() {
                        TileEntity tile = ctx.getServerHandler().player.world.getTileEntity(message.pos);
                        if (!(tile instanceof TileAutomaticMiner)) return;

                        TileAutomaticMiner miner = (TileAutomaticMiner) tile;

                        switch (message.action) {
                            case 0: miner.toggleRunning(); break;
                            case 1: miner.toggleDown(); break;
                            case 2: miner.toggleRails(); break;
                            case 3: miner.setTarget(miner.target - 64); break;
                            case 4: miner.setTarget(miner.target + 64); break;
                            default: break;
                        }
                    }
                });
                return null;
            }
        }
    }
}
