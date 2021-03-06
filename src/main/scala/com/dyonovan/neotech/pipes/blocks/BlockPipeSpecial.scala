package com.dyonovan.neotech.pipes.blocks

import java.util.Random

import com.dyonovan.neotech.NeoTech
import com.dyonovan.neotech.common.blocks.traits.Upgradeable
import com.dyonovan.neotech.lib.Reference
import com.dyonovan.neotech.managers.ItemManager
import com.dyonovan.neotech.pipes.collections.WorldPipes
import com.dyonovan.neotech.pipes.container.ContainerAdvancedPipeMenu
import com.dyonovan.neotech.pipes.gui.GuiAdvancedPipeMenu
import com.dyonovan.neotech.pipes.types.{AdvancedPipe, SimplePipe}
import com.teambr.bookshelf.client.gui.GuiColor
import com.teambr.bookshelf.loadables.ILoadActionProvider
import com.teambr.bookshelf.traits.HasToolTip
import mcmultipart.block.{BlockCoverable, BlockMultipart}
import mcmultipart.client.multipart.ModelMultipartContainer
import net.minecraft.block.Block
import net.minecraft.block.material.Material
import net.minecraft.block.properties.IProperty
import net.minecraft.block.state.{BlockState, IBlockState}
import net.minecraft.client.resources.model.{IBakedModel, ModelResourceLocation}
import net.minecraft.entity.Entity
import net.minecraft.entity.item.EntityItem
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.tileentity.TileEntity
import net.minecraft.util._
import net.minecraft.world.{IBlockAccess, World, WorldServer}
import net.minecraftforge.client.event.ModelBakeEvent
import net.minecraftforge.common.property.{ExtendedBlockState, IUnlistedProperty}
import net.minecraftforge.fml.relauncher.{Side, SideOnly}
import org.lwjgl.input.Keyboard

import scala.collection.mutable.ArrayBuffer
import scala.collection.JavaConversions._


/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Paul Davis pauljoda
  * @since August 14, 2015
  */
class BlockPipeSpecial(val name : String, mat : Material, tileClass : Class[_ <: AdvancedPipe]) extends BlockCoverable(mat)
        with HasToolTip with ILoadActionProvider {

    /*******************************************************************************************************************
      * Constructor                                                                                                    *
      ******************************************************************************************************************/

    setUnlocalizedName(Reference.MOD_ID + ":" + name)
    setCreativeTab(NeoTech.tabPipes)
    setHardness(1.5F)
    setLightOpacity(0)
    setDefaultState(this.blockState.getBaseState
            .withProperty(PipeProperties.SPECIAL_UP, 0.asInstanceOf[Integer])
            .withProperty(PipeProperties.SPECIAL_DOWN, 0.asInstanceOf[Integer])
            .withProperty(PipeProperties.SPECIAL_NORTH, 0.asInstanceOf[Integer])
            .withProperty(PipeProperties.SPECIAL_EAST, 0.asInstanceOf[Integer])
            .withProperty(PipeProperties.SPECIAL_SOUTH, 0.asInstanceOf[Integer])
            .withProperty(PipeProperties.SPECIAL_WEST, 0.asInstanceOf[Integer]))

    /*******************************************************************************************************************
      * Block State                                                                                                    *
      ******************************************************************************************************************/

    /**
      * Used to create the block state of the pipes
      */
    protected override def createBlockState: BlockState = {
        val listed = new ArrayBuffer[IProperty[_]]()
        listed += PipeProperties.SPECIAL_UP
        listed += PipeProperties.SPECIAL_DOWN
        listed += PipeProperties.SPECIAL_NORTH
        listed += PipeProperties.SPECIAL_SOUTH
        listed += PipeProperties.SPECIAL_EAST
        listed += PipeProperties.SPECIAL_WEST
        new ExtendedBlockState(this, listed.toArray, BlockMultipart.properties.asInstanceOf[Array[IUnlistedProperty[_]]])
    }

    override def getActualState(state: IBlockState, worldIn: IBlockAccess, pos: BlockPos): IBlockState = {
        state.withProperty(PipeProperties.SPECIAL_UP, countConnections(worldIn, pos, EnumFacing.UP).asInstanceOf[Integer])
                .withProperty(PipeProperties.SPECIAL_DOWN, countConnections(worldIn, pos, EnumFacing.DOWN).asInstanceOf[Integer])
                .withProperty(PipeProperties.SPECIAL_NORTH, countConnections(worldIn, pos, EnumFacing.NORTH).asInstanceOf[Integer])
                .withProperty(PipeProperties.SPECIAL_EAST, countConnections(worldIn, pos, EnumFacing.EAST).asInstanceOf[Integer])
                .withProperty(PipeProperties.SPECIAL_SOUTH, countConnections(worldIn, pos, EnumFacing.SOUTH).asInstanceOf[Integer])
                .withProperty(PipeProperties.SPECIAL_WEST, countConnections(worldIn, pos, EnumFacing.WEST).asInstanceOf[Integer])
    }

    /**
      * Convert the given metadata into a BlockState for this Block
      */
    override def getStateFromMeta(meta: Int): IBlockState = getDefaultState

    /**
      * Convert the BlockState into the correct metadata value
      */
    override def getMetaFromState(state: IBlockState): Int = 0

    /*******************************************************************************************************************
      * Block Methods                                                                                                  *
      ******************************************************************************************************************/

    override def createNewTileEntity(worldIn: World, meta: Int): TileEntity = tileClass.newInstance()

    override def breakBlock(worldIn: World, pos: BlockPos, state: IBlockState): Unit = {
        if(worldIn.getTileEntity(pos) != null) {
            worldIn match {
                case _: WorldServer => //We are on a server
                    worldIn.getTileEntity(pos) match {
                        case upgradeable: Upgradeable =>
                            if (upgradeable.upgradeInventory.getStackInSlot(0) != null) {
                                val stack = upgradeable.upgradeInventory.getStackInSlot(0)
                                val random = new Random

                                if (stack != null && stack.stackSize > 0) {
                                    val rx = random.nextFloat * 0.8F + 0.1F
                                    val ry = random.nextFloat * 0.8F + 0.1F
                                    val rz = random.nextFloat * 0.8F + 0.1F

                                    val itemEntity = new EntityItem(worldIn,
                                        pos.getX + rx, pos.getY + ry, pos.getZ + rz,
                                        new ItemStack(stack.getItem, stack.stackSize, stack.getItemDamage))

                                    if (stack.hasTagCompound)
                                        itemEntity.getEntityItem.setTagCompound(stack.getTagCompound)

                                    val factor = 0.05F

                                    itemEntity.motionX = random.nextGaussian * factor
                                    itemEntity.motionY = random.nextGaussian * factor + 0.2F
                                    itemEntity.motionZ = random.nextGaussian * factor
                                    worldIn.spawnEntityInWorld(itemEntity)

                                    stack.stackSize = 0
                                }
                            }
                    }
            }

            worldIn.getTileEntity(pos) match {
                case pipe: SimplePipe =>
                    pipe.onPipeBroken()
                case _ =>
            }
        }
    }


    /**
      * Called when the block is activated
      *
      * If you want to override this but still call it, make sure you call
      *      super[OpensGui].onBlockActivated(...)
      */
    override def onBlockActivatedDefault(world : World, pos : BlockPos, state : IBlockState, playerIn : EntityPlayer, side : EnumFacing, hitX : Float, hitY : Float, hitZ : Float) : Boolean = {
        playerIn.getCurrentEquippedItem match {
            case stack : ItemStack if stack.getItem == ItemManager.wrench =>
                if(!world.isRemote && playerIn.isSneaking) {
                    val random = new Random
                    val stack = new ItemStack(world.getBlockState(pos).getBlock.getItemDropped(world.getBlockState(pos), random, 0), 1, damageDropped(world.getBlockState(pos)))
                    if(stack != null && stack.stackSize > 0) {
                        val rx = random.nextFloat * 0.8F + 0.1F
                        val ry = random.nextFloat * 0.8F + 0.1F
                        val rz = random.nextFloat * 0.8F + 0.1F

                        val itemEntity = new EntityItem(world,
                            pos.getX + rx, pos.getY + ry, pos.getZ + rz,
                            new ItemStack(stack.getItem, stack.stackSize, stack.getItemDamage))

                        if (stack.hasTagCompound)
                            itemEntity.getEntityItem.setTagCompound(stack.getTagCompound)

                        val factor = 0.05F

                        itemEntity.motionX = random.nextGaussian * factor
                        itemEntity.motionY = random.nextGaussian * factor + 0.2F
                        itemEntity.motionZ = random.nextGaussian * factor
                        world.spawnEntityInWorld(itemEntity)
                    }
                    world.setBlockToAir(pos)
                    world.markBlockForUpdate(pos)
                    return true
                } else if(!playerIn.isSneaking) {
                    playerIn.openGui(NeoTech, 0, world, pos.getX, pos.getY, pos.getZ)
                    playerIn.swingItem()
                    return true
                }

            case _ =>
        }
        false
    }

    override def setBlockBoundsBasedOnStateDefault(worldIn: IBlockAccess, pos: BlockPos) {
        var x1 = 5F / 16F
        var x2 = 1.0F - x1
        var y1 = x1
        var y2 = 1.0F - y1
        var z1 = x1
        var z2 = 1.0F - z1
        if (countConnections(worldIn, pos, EnumFacing.WEST) > 0) {
            x1 = 0.0F
        }

        if (countConnections(worldIn, pos, EnumFacing.EAST) > 0) {
            x2 = 1.0F
        }

        if (countConnections(worldIn, pos, EnumFacing.NORTH) > 0) {
            z1 = 0.0F
        }

        if (countConnections(worldIn, pos, EnumFacing.SOUTH) > 0) {
            z2 = 1.0F
        }

        if (countConnections(worldIn, pos, EnumFacing.DOWN) > 0) {
            y1 = 0.0F
        }

        if (countConnections(worldIn, pos, EnumFacing.UP) > 0) {
            y2 = 1.0F
        }

        this.setBlockBounds(x1, y1, z1, x2, y2, z2)
    }

    def isPipeConnected(world: IBlockAccess, pos: BlockPos, facing: EnumFacing): Boolean = {
        world.getTileEntity(pos) match {
            case pipe: SimplePipe =>
                pipe.canConnect(facing) || pipe.isSpecialConnection(facing)
            case _ => false
        }
    }

    def countConnections(world: IBlockAccess, pos: BlockPos, facing: EnumFacing): Int = {
        world.getTileEntity(pos) match {
            case pipe: AdvancedPipe =>
                if (pipe.canConnectExtract(facing) && pipe.canConnect(facing) && world.getTileEntity(pos.offset(facing)) != null
                        && !world.getTileEntity(pos.offset(facing)).isInstanceOf[SimplePipe])
                    2
                else if (pipe.canConnect(facing) && pipe.canConnectSink(facing))
                    1
                else
                    0
            case _ => 0
        }
    }

    override def addCollisionBoxesToListDefault(worldIn : World, pos : BlockPos, state : IBlockState, mask : AxisAlignedBB,
                                                list : java.util.List[AxisAlignedBB], collidingEntity : Entity) {
        this.setBlockBoundsBasedOnStateDefault(worldIn, pos)
        super.addCollisionBoxesToListDefault(worldIn, pos, state, mask, list, collidingEntity)
    }

    override def onNeighborBlockChangeDefault(world: World, pos: BlockPos, state: IBlockState, block: Block): Unit = {
        if (!world.isRemote)
            WorldPipes.notifyPipes()
    }

    def getServerGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
        if(player.inventory.getCurrentItem != null && player.inventory.getCurrentItem.getItem == ItemManager.wrench &&
                world.getTileEntity(new BlockPos(x, y, z)).isInstanceOf[AdvancedPipe])
            return new ContainerAdvancedPipeMenu(player.inventory, world.getTileEntity(new BlockPos(x, y, z)).asInstanceOf[AdvancedPipe])
        null
    }

    @SideOnly(Side.CLIENT)
    def getClientGuiElement(ID: Int, player: EntityPlayer, world: World, x: Int, y: Int, z: Int): AnyRef = {
        if(player.inventory.getCurrentItem != null && player.inventory.getCurrentItem.getItem == ItemManager.wrench &&
                world.getTileEntity(new BlockPos(x, y, z)).isInstanceOf[AdvancedPipe])
            return new GuiAdvancedPipeMenu(player, world.getTileEntity(new BlockPos(x, y, z)).asInstanceOf[AdvancedPipe])
        null
    }

    /*******************************************************************************************************************
      * Client Block Info                                                                                              *
      ******************************************************************************************************************/

    override def getRenderType: Int = 3
    override def isOpaqueCube: Boolean = false
    @SideOnly(Side.CLIENT)
    override def isTranslucent: Boolean = true
    override def isFullCube: Boolean = false
    @SideOnly(Side.CLIENT)
    override def getBlockLayer: EnumWorldBlockLayer = EnumWorldBlockLayer.SOLID
    override def canRenderInLayerDefault(layer: EnumWorldBlockLayer): Boolean =
        layer == EnumWorldBlockLayer.SOLID

    override def getToolTip() : List[String] = {
        if(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT))
            List[String](GuiColor.ORANGE + "Press <SHIFT> for possible upgrades...")
        else
            List[String](GuiColor.YELLOW + "Control Upgrade (Max 1): " + GuiColor.WHITE + "Adds Redstone Control.",
                GuiColor.YELLOW + "Expansion Upgrade (Max 1): " + GuiColor.WHITE + "Allows Mode Selection (Round Robin, etc).",
                GuiColor.YELLOW + "Hard Drive (Max 8): " + GuiColor.WHITE + "Increases amount extracted per tick.",
                GuiColor.YELLOW + "Processor Upgrade (Max 8): " + GuiColor.WHITE + "Increases Extraction Rate.")
    }

    override def performLoadAction(event: AnyRef, isClient: Boolean): Unit = {
        event match {
            case modelBake: ModelBakeEvent =>
                for(modelLocation <- modelBake.modelRegistry.asInstanceOf[RegistrySimple[ModelResourceLocation, IBakedModel]].getKeys) {
                    if(modelLocation.getResourceDomain.equalsIgnoreCase(Reference.MOD_ID) &&
                            modelLocation.getResourcePath.contains("BasicInterface")) {
                        // Create Multipart world obj
                        modelBake.modelRegistry.putObject(modelLocation,
                            new ModelMultipartContainer(modelBake.modelRegistry.getObject(modelLocation)))
                    }
                }
            case _ =>
        }
    }
}
