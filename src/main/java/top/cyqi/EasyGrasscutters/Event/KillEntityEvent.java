package top.cyqi.EasyGrasscutters.Event;

import emu.grasscutter.server.event.entity.EntityDeathEvent;
import emu.grasscutter.utils.EventConsumer;
import top.cyqi.EasyGrasscutters.Event.content.KillEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class KillEntityEvent implements EventConsumer<EntityDeathEvent> {

    //监听实体表
    public static Map<String, KillEntity> All_Entity = new HashMap<>();


    @Override
    public void consume(EntityDeathEvent entityDeathEvent) {


        for (String key : All_Entity.keySet()) {
            //获取当前实体监测对象
            KillEntity Entity = All_Entity.get(key);

            //获取死亡实体的ID和杀死该实体的ID
            int killed_id = 0;
            if (entityDeathEvent.getKiller() != null) {
                killed_id = entityDeathEvent.getKiller().getId();
            }
            int death_id = entityDeathEvent.getEntity().getId();

            // 调用实体监测
            if (Entity.check(death_id, killed_id)) {
                //符合实体，调用删除
                All_Entity.remove(key);
            }
        }

    }

    public static void delete(String msg_id) {
        for (String key : All_Entity.keySet()) {
            KillEntity Entity = All_Entity.get(key);
            if (Entity.msg_id.equals(msg_id)) {
                All_Entity.remove(key);
            }
        }
    }

    public static void add_killEntity(KillEntity killEntity) {
        String uuid = UUID.randomUUID().toString();
        All_Entity.put(uuid, killEntity);
    }

}