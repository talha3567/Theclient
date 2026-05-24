package meteordevelopment.meteorclient.events.render;

import java.util.Iterator;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.network.chat.Component;

public class RenderBossBarEvent {

    public static class BossIterator {
        private static final BossIterator INSTANCE = new BossIterator();
        public Iterator<LerpingBossEvent> iterator;

        public static BossIterator get(Iterator<LerpingBossEvent> iterator) {
            BossIterator.INSTANCE.iterator = iterator;
            return INSTANCE;
        }
    }

    public static class BossSpacing {
        private static final BossSpacing INSTANCE = new BossSpacing();
        public int spacing;

        public static BossSpacing get(int spacing) {
            BossSpacing.INSTANCE.spacing = spacing;
            return INSTANCE;
        }
    }

    public static class BossText {
        private static final BossText INSTANCE = new BossText();
        public LerpingBossEvent bossBar;
        public Component name;

        public static BossText get(LerpingBossEvent bossBar, Component name) {
            BossText.INSTANCE.bossBar = bossBar;
            BossText.INSTANCE.name = name;
            return INSTANCE;
        }
    }
}
