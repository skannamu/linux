package com.skannamu.item.tool;

import com.skannamu.client.gui.TerminalScreen;
import com.skannamu.client.ClientHackingState;
import com.skannamu.client.renderer.PortableTerminalRenderer; // 💡 렌더러 임포트
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.text.Text;

// 💡 GeoItem 인터페이스를 구현합니다.
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.GeoRenderProvider;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animatable.manager.AnimatableManager;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.util.GeckoLibUtil;
import java.util.function.Consumer;


public class PortableTerminalItem extends Item implements GeoItem { // <-- GeoItem 추가

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PortableTerminalItem(Settings settings) {
        super(settings);
        GeoItem.registerSyncedAnimatable(this);
    }

    @Override
    public void createGeoRenderer(Consumer<GeoRenderProvider> consumer) {
        consumer.accept(new GeoRenderProvider() {
            private PortableTerminalRenderer renderer;

            @Override
            public GeoItemRenderer<?> getGeoItemRenderer() {
                if (this.renderer == null)
                    this.renderer = new PortableTerminalRenderer();
                return this.renderer;
            }
        });
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar registrar) {
        // 터미널의 Idle 애니메이션이나 열릴 때 애니메이션을 여기에 추가할 수 있습니다.
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    @Override
    public ActionResult use(World world, PlayerEntity player, Hand hand) {
        if (world.isClient()) {

            if (ClientHackingState.isTerminalHacked()) {
                player.sendMessage(Text.literal("Terminal locked. System overloaded (EMP effect)."), true);
                return ActionResult.FAIL;
            }

            openTerminalScreen();
            return ActionResult.SUCCESS;
        }
        return ActionResult.PASS;
    }

    @Environment(EnvType.CLIENT)
    private void openTerminalScreen() {
        MinecraftClient client = MinecraftClient.getInstance();
        client.setScreen(new TerminalScreen());
    }
}
