package com.skannamu.item.weapon;

import net.minecraft.item.Item;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.world.World;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;

import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.SingletonGeoAnimatable;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class NanoBladeItem extends Item implements GeoItem {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public NanoBladeItem(Settings settings) {
        super(settings);
        SingletonGeoAnimatable.registerSyncedAnimatable(this);
    }
    @Override
    public void registerControllers(AnimatableManager<?> manager) {
        manager.addController(new AnimationController<>(this, "default_controller", 5, state -> PlayState.STOP));
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand){
        if (!world.isClient()) {
            player.sendMessage(Text.literal("Nano Blade: Use the Portable Terminal to execute exploit sequences."), false);
        }
        return ActionResult.PASS;
    }
}
