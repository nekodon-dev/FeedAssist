package com.nekodon.feedassist;

import com.nekodon.feedassist.config.ModConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;

import java.util.Comparator;

public class FeedAssist implements ModInitializer {
    @Override
    public void onInitialize() {
        ModConfig.load();
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (world.isClient || !ModConfig.isEnabled() || player.isSpectator()
                    || player.isSneaking() || !(entity instanceof AnimalEntity target)) {
                return ActionResult.PASS;
            }
            ItemStack food = player.getStackInHand(hand).copy();
            if (!canFeed(target, food)) {
                return ActionResult.PASS;
            }

            // Use each animal's own feeding logic to retain its food rules and effects.
            // Calling interactMob directly does not fire UseEntityCallback again.
            ActionResult result = target.interactMob(player, hand);
            if (!result.isAccepted() || !target.isInLove()) {
                return result;
            }

            int range = ModConfig.getFeedRange();
            var nearby = world.getEntitiesByClass(AnimalEntity.class,
                    player.getBoundingBox().expand(range),
                    animal -> animal != target && animal.squaredDistanceTo(player) <= range * range);
            nearby.sort(Comparator.comparingDouble(animal -> animal.squaredDistanceTo(player)));
            for (AnimalEntity animal : nearby) {
                ItemStack remaining = player.getStackInHand(hand);
                if (remaining.isEmpty() || !ItemStack.areItemsEqual(food, remaining)) {
                    break;
                }
                if (canFeed(animal, remaining)) {
                    animal.interactMob(player, hand);
                }
            }
            return result;
        });
    }

    private static boolean canFeed(AnimalEntity animal, ItemStack food) {
        if (!animal.isAlive() || food.isEmpty() || animal.getBreedingAge() != 0
                || !animal.canEat() || !animal.isBreedingItem(food)) {
            return false;
        }
        if (animal instanceof TameableEntity tameable && !tameable.isTamed()) {
            return false;
        }
        return !(animal instanceof AbstractHorseEntity horse) || horse.isTame();
    }
}
