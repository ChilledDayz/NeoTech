package com.dyonovan.neotech.common.blocks.misc

import com.dyonovan.neotech.common.blocks.BaseBlock
import com.dyonovan.neotech.common.tiles.misc.TileDisplayPanel
import com.teambr.bookshelf.common.blocks.properties.PropertyRotation
import com.teambr.bookshelf.util.WorldUtils
import net.minecraft.block.material.Material
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.entity.EntityLivingBase
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.util.{BlockPos, EnumFacing, MathHelper}
import net.minecraft.world.World
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Dyonovan
  * @since 3/10/2016
  */
class BlockDisplayPanel extends BaseBlock(Material.iron, "displayPanel", classOf[TileDisplayPanel]) {

    override def getRenderType: Int = 3

    override def isOpaqueCube : Boolean = false

    @SideOnly(Side.CLIENT)
    override def isTranslucent : Boolean = true

    override def isFullCube = false

    override def rotateBlock(world: World, pos: BlockPos, side: EnumFacing): Boolean = {
        val tag = new NBTTagCompound
        world.getTileEntity(pos).writeToNBT(tag)
        if (side != EnumFacing.UP && side != EnumFacing.DOWN)
            world.setBlockState(pos, world.getBlockState(pos).withProperty(PropertyRotation.FOUR_WAY, side))
        else
            world.setBlockState(pos, world.getBlockState(pos).withProperty(PropertyRotation.FOUR_WAY, WorldUtils.rotateRight(world.getBlockState(pos).getValue(PropertyRotation.FOUR_WAY))))
        if (tag != null) {
            world.getTileEntity(pos).readFromNBT(tag)
        }
        true
    }

    override def onBlockPlaced(world: World, blockPos: BlockPos, facing: EnumFacing, hitX: Float, hitY: Float, hitZ: Float, meta: Int, placer: EntityLivingBase): IBlockState = {
        val playerFacingDirection = if (placer == null) 0 else MathHelper.floor_double((placer.rotationYaw / 90.0F) + 0.5D) & 3
        val enumFacing = EnumFacing.getHorizontal(playerFacingDirection).getOpposite
        this.getDefaultState.withProperty(PropertyRotation.FOUR_WAY, enumFacing)
    }

    override def createBlockState(): BlockState = {
        new BlockState(this, PropertyRotation.FOUR_WAY)
    }

    override def getStateFromMeta(meta: Int): IBlockState =
        getDefaultState.withProperty(PropertyRotation.FOUR_WAY, EnumFacing.getFront(meta))

    override def getMetaFromState(state: IBlockState) =
        state.getValue(PropertyRotation.FOUR_WAY).getIndex

}
