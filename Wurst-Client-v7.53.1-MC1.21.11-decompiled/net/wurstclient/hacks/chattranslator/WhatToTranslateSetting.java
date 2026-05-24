package net.wurstclient.hacks.chattranslator;

import net.wurstclient.settings.EnumSetting;

public final class WhatToTranslateSetting
extends EnumSetting<WhatToTranslate> {
    public WhatToTranslateSetting() {
        super("Translate", "", (Enum[])WhatToTranslate.values(), (Enum)WhatToTranslate.RECEIVED_MESSAGES);
    }

    public boolean includesReceived() {
        return ((WhatToTranslate)((Object)this.getSelected())).received;
    }

    public boolean includesSent() {
        return ((WhatToTranslate)((Object)this.getSelected())).sent;
    }

    public static enum WhatToTranslate {
        RECEIVED_MESSAGES("Received messages", true, false),
        SENT_MESSAGES("Sent messages", false, true),
        BOTH("Both", true, true);

        private final String name;
        private final boolean received;
        private final boolean sent;

        private WhatToTranslate(String name, boolean received, boolean sent) {
            this.name = name;
            this.received = received;
            this.sent = sent;
        }

        public String toString() {
            return this.name;
        }
    }
}
