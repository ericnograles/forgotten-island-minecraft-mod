package com.forgottenisland.client;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;

/** A brief, original pixel memory charm. Never emits light blocks or fire. */
public class MemoryMoteParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    protected MemoryMoteParticle(ClientLevel level,double x,double y,double z,double dx,double dy,double dz,SpriteSet sprites) {
        super(level,x,y,z,dx,dy,dz);this.sprites=sprites;
        lifetime=28;quadSize=.16f;gravity=-.02f;friction=.94f;
        xd=dx;yd=dy+.02;zd=dz;rCol=.55f+random.nextFloat()*.3f;gCol=1;bCol=.85f;
        setSpriteFromAge(sprites);
    }
    @Override public void tick(){super.tick();setSpriteFromAge(sprites);alpha=1-(float)age/lifetime;roll+=.04f;}
    @Override public int getLightColor(float partial){return 0xf000f0;}
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    public record Provider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override public Particle createParticle(SimpleParticleType type,ClientLevel l,double x,double y,double z,double dx,double dy,double dz){return new MemoryMoteParticle(l,x,y,z,dx,dy,dz,sprites);}
    }
}
