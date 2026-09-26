package com.nekodon.feedassist;

import com.nekodon.feedassist.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionResult;

import java.util.Comparator;

public class FeedAssist implements ModInitializer {
    @Override
    public void onInitialize() {
        ModConfig.load();
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClientSide() || !ModConfig.isEnabled() || player.isSpectator()
                    || player.isShiftKeyDown() || !(entity instanceof Animal target)) {
                return InteractionResult.PASS;
            }
            ItemStack food = player.getItemInHand(hand).copy();
            if (!canFeed(target, food)) {
                return InteractionResult.PASS;
            }

            // Use each animal's own feeding logic to retain its food rules and effects.
            // Calling mobInteract directly does not fire UseEntityCallback again.
            InteractionResult result = target.mobInteract(player, hand);
            if (!result.consumesAction() || !target.isInLove()) {
                return result;
            }

            int range = ModConfig.getFeedRange();
            var nearby = world.getEntitiesOfClass(Animal.class,
                    player.getBoundingBox().inflate(range),
                    animal -> animal != target && animal.distanceToSqr(player) <= range * range);
            nearby.sort(Comparator.comparingDouble(animal -> animal.distanceToSqr(player)));
            for (Animal animal : nearby) {
                ItemStack remaining = player.getItemInHand(hand);
                if (remaining.isEmpty() || !ItemStack.isSameItem(food, remaining)) {
                    break;
                }
                if (canFeed(animal, remaining)) {
                    animal.mobInteract(player, hand);
                }
            }
            return result;
        });
    }

    private static boolean canFeed(Animal animal, ItemStack food) {
        if (!animal.isAlive() || food.isEmpty() || animal.getAge() != 0
                || !animal.canFallInLove() || !animal.isFood(food)) {
            return false;
        }
        if (animal instanceof TamableAnimal tameable && !tameable.isTame()) {
            return false;
        }
        return !(animal instanceof AbstractHorse horse) || horse.isTamed();
    }
}

