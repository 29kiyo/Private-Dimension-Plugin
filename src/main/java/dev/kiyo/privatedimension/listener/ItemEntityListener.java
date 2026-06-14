package dev.kiyo.privatedimension.listener;

import dev.kiyo.privatedimension.item.DimensionBottleItem;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * ドロップ状態の Dimension in a Bottle を消滅から守るリスナー
 *
 * setDamageResistant() でカバーできないダメージ種別:
 *   - サボテン（DamageType: cactus）
 *   - 爆発（DamageType: is_explosion）
 *   - その他の予期しないダメージ
 *
 * アイテムエンティティへのダメージイベントを監視し、
 * Dimension in a Bottle であればすべてキャンセルする。
 */
public class ItemEntityListener implements Listener {

    private final DimensionBottleItem bottleItem;

    public ItemEntityListener(DimensionBottleItem bottleItem) {
        this.bottleItem = bottleItem;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onItemDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();

        // ドロップアイテムエンティティのみ対象
        if (!(entity instanceof Item itemEntity)) return;

        // Dimension in a Bottle かチェック
        if (!bottleItem.isDimensionBottle(itemEntity.getItemStack())) return;

        // すべてのダメージをキャンセル（炎・溶岩・サボテン・爆発・その他）
        event.setCancelled(true);
    }
}
