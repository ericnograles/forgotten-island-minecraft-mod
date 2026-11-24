package com.forgottenisland.network;

import com.forgottenisland.ForgottenIsland;
import com.forgottenisland.entity.IslandCreature;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.*;
import net.minecraftforge.network.simple.SimpleChannel;
import java.util.function.Supplier;

public final class IslandNetwork {
    public static final SimpleChannel CHANNEL=NetworkRegistry.newSimpleChannel(ForgottenIsland.id("companions"),()->"2","2"::equals,"2"::equals);
    public record Request(int entityId, boolean secondary) {
        public static void encode(Request p,FriendlyByteBuf b) {b.writeVarInt(p.entityId);b.writeBoolean(p.secondary);}
        public static Request decode(FriendlyByteBuf b) {return new Request(b.readVarInt(),b.readBoolean());}
        public static void handle(Request p,Supplier<NetworkEvent.Context> ctx) {
            var context=ctx.get();
            context.enqueueWork(()->{
                var sender=context.getSender();
                if(sender!=null && sender.isAlive() && !sender.isSpectator() && sender.level().getEntity(p.entityId) instanceof IslandCreature c)
                    c.requestAbility(sender,p.secondary);
            });context.setPacketHandled(true);
        }
    }
    public record Ride(int vertical) {
        public static void encode(Ride p,FriendlyByteBuf b) {b.writeByte(p.vertical);}
        public static Ride decode(FriendlyByteBuf b) {return new Ride(b.readByte());}
        public static void handle(Ride p,Supplier<NetworkEvent.Context> ctx) {
            var context=ctx.get();context.enqueueWork(()->{
                var sender=context.getSender();
                if(sender!=null && sender.isAlive() && sender.getVehicle() instanceof IslandCreature c && c.isOwnedBy(sender)) c.setRideInput(p.vertical);
            });context.setPacketHandled(true);
        }
    }
    public record Moonrise() {}
    public static void register() {
        CHANNEL.messageBuilder(Moonrise.class,2,NetworkDirection.PLAY_TO_CLIENT)
            .encoder((msg,buf) -> {}).decoder(buf -> new Moonrise())
            .consumerMainThread((msg,ctx) -> net.minecraftforge.fml.DistExecutor.unsafeRunWhenOn(
                net.minecraftforge.api.distmarker.Dist.CLIENT, () -> com.forgottenisland.client.MoonriseScreen::show)).add();
        CHANNEL.messageBuilder(Request.class,0,NetworkDirection.PLAY_TO_SERVER).encoder(Request::encode).decoder(Request::decode).consumerNetworkThread(Request::handle).add();
        CHANNEL.messageBuilder(Ride.class,1,NetworkDirection.PLAY_TO_SERVER).encoder(Ride::encode).decoder(Ride::decode).consumerNetworkThread(Ride::handle).add();
    }
}
