package com.forgottenisland;

import com.forgottenisland.entity.*;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.RegisterCommandsEvent;

public final class ShowcaseCommands {
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("island")
            .then(Commands.literal("guide").executes(ctx->{
                var p=ctx.getSource().getPlayerOrException();p.addItem(new ItemStack(ForgottenIsland.GUIDE.get()));return 1;
            }))
            .then(Commands.literal("showcase").requires(s->s.hasPermission(2)).executes(ctx->{
                var p=ctx.getSource().getPlayerOrException();
                p.addItem(new ItemStack(ForgottenIsland.GUIDE.get()));p.addItem(new ItemStack(ForgottenIsland.SHOWCASE.get()));
                p.addItem(new ItemStack(ForgottenIsland.WHISTLE.get()));
                int i=0;
                for(Species species:Species.values()) {
                    var c=ForgottenIsland.TYPES.get(species).get().create(p.level());
                    if(c==null)continue;
                    double x=p.getX()+(i%3-1)*9,z=p.getZ()+7+(i/3)*10;
                    int y=p.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,(int)Math.floor(x),(int)Math.floor(z));
                    c.moveTo(x,y,z,180,0);c.befriend(p);c.setOrderedToSit(true);c.setCustomName(Component.literal(species.displayName));
                    p.level().addFreshEntity(c);i++;
                }
                ctx.getSource().sendSuccess(()->Component.literal("Your Wild Pack is ready. All companions are staying. Whistle or R: ability; sneak + empty hand: follow."),false);
                return i;
            }))
            .then(Commands.literal("ability").then(Commands.argument("type",StringArgumentType.word())
                .suggests((ctx,b)->{for(Species s:Species.values())b.suggest(s.id);return b.buildFuture();})
                .executes(ctx->{
                    var p=ctx.getSource().getPlayerOrException();String id=StringArgumentType.getString(ctx,"type");
                    return p.level().getEntitiesOfClass(IslandCreature.class,p.getBoundingBox().inflate(12),c->c.isOwnedBy(p)&&c.species.id.equals(id))
                        .stream().findFirst().map(c->c.requestAbility(p,false)?1:0).orElse(0);
                }))));
    }
}
