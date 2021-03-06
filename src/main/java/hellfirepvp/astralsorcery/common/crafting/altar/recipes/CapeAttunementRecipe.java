/*******************************************************************************
 * HellFirePvP / Astral Sorcery 2017
 *
 * This project is licensed under GNU GENERAL PUBLIC LICENSE Version 3.
 * The source code is available on github: https://github.com/HellFirePvP/AstralSorcery
 * For further details, see the License file there.
 ******************************************************************************/

package hellfirepvp.astralsorcery.common.crafting.altar.recipes;

import hellfirepvp.astralsorcery.common.constellation.IConstellation;
import hellfirepvp.astralsorcery.common.crafting.helper.AccessibleRecipe;
import hellfirepvp.astralsorcery.common.crafting.helper.ShapeMap;
import hellfirepvp.astralsorcery.common.item.wearable.ItemCape;
import hellfirepvp.astralsorcery.common.lib.ItemsAS;
import hellfirepvp.astralsorcery.common.tile.TileAltar;
import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

/**
 * This class is part of the Astral Sorcery Mod
 * The complete source code for this mod can be found on github.
 * Class: CapeAttunementRecipe
 * Created by HellFirePvP
 * Date: 19.10.2017 / 18:40
 */
public class CapeAttunementRecipe extends TraitRecipe {

    private final IConstellation constellation;

    public CapeAttunementRecipe(IConstellation constellation, AccessibleRecipe recipe) {
        super(recipe);
        setPassiveStarlightRequirement(3000);
        this.constellation = constellation;
        setRequiredConstellation(constellation);
    }

    @Nonnull
    @Override
    public ItemStack getOutputForRender() {
        ItemStack cPaper = new ItemStack(ItemsAS.armorImbuedCape);
        ItemCape.setAttunedConstellation(cPaper, constellation);
        return cPaper;
    }

    @Nonnull
    @Override
    public ItemStack getOutputForMatching() {
        ItemStack cPaper = new ItemStack(ItemsAS.armorImbuedCape);
        ItemCape.setAttunedConstellation(cPaper, constellation);
        return cPaper;
    }

    @Nonnull
    @Override
    public ItemStack getOutput(ShapeMap centralGridMap, TileAltar altar) {
        ItemStack cPaper = new ItemStack(ItemsAS.armorImbuedCape);
        ItemCape.setAttunedConstellation(cPaper, constellation);
        return cPaper;
    }

}
