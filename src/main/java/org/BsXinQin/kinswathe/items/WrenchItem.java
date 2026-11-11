package org.BsXinQin.kinswathe.items;

import dev.doctor4t.wathe.api.door.DoorApi;
import dev.doctor4t.wathe.block_entity.DoorBlockEntity;
import dev.doctor4t.wathe.util.AdventureUsable;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.BsXinQin.kinswathe.KinsWatheItems;
import org.jetbrains.annotations.NotNull;

public class WrenchItem extends Item implements AdventureUsable {

    public WrenchItem(@NotNull Settings settings) {
        super(settings);
    }

    @Override
    public @NotNull ActionResult useOnBlock(@NotNull ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();
        PlayerEntity player = context.getPlayer();
        if (player == null) return super.useOnBlock(context);
        if (player.getItemCooldownManager().isCoolingDown(this)) return ActionResult.FAIL;
        DoorBlockEntity door = DoorApi.findDoor(world, pos);
        if (door == null || !DoorApi.inspect(door).needsRepair()) {
            return super.useOnBlock(context);
        }
        if (world.isClient) {
            return ActionResult.SUCCESS;
        }

        /*
         * 扳手保留与 NoellesRoles 工具箱/巫妖不同的修复表现：整组门清除损坏和锁定后保持打开，
         * 并重新开始 Wathe 默认五秒自动关门倒计时。不能再分别 toggle 两侧，否则旧的一开一关状态
         * 只会互换，无法真正恢复双门一致性。
         */
        // 旧扳手开门使用 silent toggle；这里继续隐藏 Wathe 普通门声，只播放下方的活塞修理音。
        DoorApi.OperationResult result = DoorApi.repair(door, DoorApi.RepairMode.OPEN_THEN_AUTOCLOSE, true);
        if (!result.applied()) {
            return ActionResult.PASS;
        }
        KinsWatheItems.setItemAfterUsing(player, this, null);
        if (result.wasJammed()) {
            player.sendMessage(Text.literal(""), true);
        }
        world.playSound(null, door.getPos(), SoundEvents.BLOCK_PISTON_CONTRACT, SoundCategory.PLAYERS, 0.8F, 1.0F);
        return ActionResult.CONSUME;
    }
}
