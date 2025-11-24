package net.asd417.tutorialmod.block.custom;

import net.asd417.tutorialmod.item.ModItems;
import net.asd417.tutorialmod.shooter.ArrowShooter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.ticks.TickPriority;

public class MagicBlock extends Block {

    public MagicBlock(Properties properties) {
        super(properties);
    }
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        level.playSound(player,pos, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.BLOCKS,1f,1f);
        //level.scheduleTick(pos, this, 10);
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        Player player = level.players().get(0);
        float velocity = 1.0f;
        Vec3 deltaV = player.getKnownMovement(); //getKnownMovement() is entity level function which can account for players as well
        Vec3 from = pos.getCenter();
        double leadT = from.distanceTo(player.position()) / velocity;

        Vec3 delta = deltaV.multiply(leadT,leadT,leadT);
        System.out.printf("Lead shot by (%f %f %f), deltamovement: (%f %f %f)\n", delta.x, delta.y, delta.z, deltaV.x, deltaV.y, deltaV.z);
        Vec3 finalpos = player.position().add(delta).add(0,0.5f,0);
        ArrowShooter.spawnFlyingArrowToTarget(level, from.add(0, 1, 0), finalpos, velocity, 0.0f);
        super.randomTick(state, level, pos, random);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if(entity instanceof ItemEntity itemEntity){
            if(itemEntity.getItem().getItem() == ModItems.RAW_BISMUTH.get()) {
                itemEntity.setItem(new ItemStack(Items.DIAMOND, itemEntity.getItem().getCount()));
            }
        }
        super.stepOn(level, pos, state, entity);
    }
}
