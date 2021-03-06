package com.dyonovan.neotech.tools.modifier

import com.dyonovan.neotech.lib.Reference
import com.dyonovan.neotech.tools.ToolHelper
import com.dyonovan.neotech.tools.upgradeitems.BaseUpgradeItem
import com.teambr.bookshelf.annotations.ModItem
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NBTTagCompound

import scala.collection.mutable.ArrayBuffer

/**
  * This file was created for NeoTech
  *
  * NeoTech is licensed under the
  * Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License:
  * http://creativecommons.org/licenses/by-nc-sa/4.0/
  *
  * @author Paul Davis <pauljoda>
  * @since 2/24/2016
  */
object ModifierSmite extends Modifier("smite") {
    lazy val SMITE = "Smite"

    /**
      * Get smite level
      */
    def getSmiteLevel(stack : ItemStack) : Int = {
        val tag = getModifierTagFromStack(stack)
        if(tag != null && tag.hasKey(SMITE))
            return tag.getInteger(SMITE)
        0
    }

    /**
      * Used to get the level for this modifier
      *
      * @param tag The tag that the level is stored on
      * @return The level
      */
    override def getLevel(tag : NBTTagCompound) = tag.getInteger(SMITE)

    /**
      * Write info to tag
      */
    def writeToNBT(tag: NBTTagCompound, stack: ItemStack, count: Int): NBTTagCompound = {
        ToolHelper.writeVanillaEnchantment(tag, stack, Enchantment.smite.effectId, getSmiteLevel(stack) + count)
        tag.setInteger(SMITE, getSmiteLevel(stack) + count)
        super.writeToNBT(tag, stack)
        tag
    }

    /**
      * Used to get the tool tip for this modifier
      *
      * @param stack The stack in
      * @return A list of tips
      */
    override def getToolTipForWriting(stack: ItemStack, tag : NBTTagCompound): ArrayBuffer[String] =
        new ArrayBuffer[String]() //Vanilla handles this

    @ModItem(modid = Reference.MOD_ID)
    class ItemModifierSmite extends BaseUpgradeItem("smite", 5) {

        /**
          * Can this upgrade item allow more to be applied to the item
          *
          * @param stack The stack we want to apply to, get count from there
          * @param count The stack size of the input
          * @return True if there is space for the entire count
          */
        override def canAcceptLevel(stack: ItemStack, count: Int, name: String): Boolean =
            ModifierSmite.getSmiteLevel(stack) + count <= maxStackSize

        /**
          * Use this to put information onto the stack, called when put onto the stack
          *
          * @param stack The stack to put onto
          * @return The tag passed
          */
        override def writeInfoToNBT(stack: ItemStack, tag: NBTTagCompound,  writingStack : ItemStack): Unit = {
            var localTag = ModifierSmite.getModifierTagFromStack(stack)
            if(localTag == null)
                localTag = new NBTTagCompound
            ModifierSmite.writeToNBT(localTag, stack, writingStack.stackSize)
            ModifierSmite.overrideModifierTag(stack, localTag)
        }
    }
}
