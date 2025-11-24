package net.asd417.tutorialmod.block.custom;

import com.mojang.serialization.MapCodec;
import net.asd417.tutorialmod.block.entity.PedestalBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class PedestalBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2,0,2,14,13,14);
    public static final MapCodec<PedestalBlock> CODEC = simpleCodec(PedestalBlock::new);

    public PedestalBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Block Entity

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PedestalBlockEntity(pos,state);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.getBlock() != newState.getBlock()){
            if(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestalBLockEntity) {
                pedestalBLockEntity.drops();
                level.updateNeighbourForOutputSignal(pos, this);
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    // take 1 item from hand and add it to the pedestal
    protected void AddItemToPedestal(PedestalBlockEntity pedestalBlockEntity, ItemStack stack){
        ItemStack temp = new ItemStack(stack.getItem(),1);
        pedestalBlockEntity.inventory.insertItem(0,temp.copy(),false);
        stack.shrink(1);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if(level.getBlockEntity(pos) instanceof PedestalBlockEntity pedestalBlockEntity){

            boolean canInsertItem = pedestalBlockEntity.inventory.getStackInSlot(0).isEmpty() && !stack.isEmpty();
            boolean full = pedestalBlockEntity.inventory.getStackInSlot(0).getCount() == pedestalBlockEntity.maxItem();
            boolean canAddItem = !full && pedestalBlockEntity.inventory.getStackInSlot(0).getItem() == stack.getItem() && !stack.isEmpty();
            boolean canRetrieve = stack.isEmpty() && !pedestalBlockEntity.inventory.getStackInSlot(0).isEmpty();

            if(canInsertItem || canAddItem) {
                AddItemToPedestal(pedestalBlockEntity, stack);
                level.playSound(player, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            }
            else if(canRetrieve)
            {
                ItemStack stackOnPedestal = pedestalBlockEntity.inventory.extractItem(0,pedestalBlockEntity.inventory.getStackInSlot(0).getCount(),false);
                player.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                pedestalBlockEntity.clearContents();
                level.playSound(player,pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }

}