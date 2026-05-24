#!/bin/bash

# Meteor modules
METEOR_DIR="meteor-client-26.1.2-22-decompiled/meteordevelopment/meteorclient/systems/modules"
PULSE_DIR="PulseClient/src/main/java/com/pulseclient/modules"

categories=("combat" "movement" "player" "render" "world" "misc")

for cat in "${categories[@]}"; do
    if [ -d "$METEOR_DIR/$cat" ]; then
        for file in "$METEOR_DIR/$cat"/*.java; do
            filename=$(basename "$file")
            # Don't overwrite unified ones
            if [[ "$filename" == "KillAura.java" || "$filename" == "Flight.java" ]]; then continue; fi

            target_name="Pulse${filename}"
            target_path="$PULSE_DIR/$cat/$target_name"

            echo "package com.pulseclient.modules.$cat;" > "$target_path"
            echo "" >> "$target_path"
            echo "import com.pulseclient.Category;" >> "$target_path"
            echo "import com.pulseclient.Module;" >> "$target_path"
            echo "" >> "$target_path"
            echo "public class Pulse${filename%.java} extends Module {" >> "$target_path"
            echo "    public Pulse${filename%.java}() {" >> "$target_path"
            echo "        super(\"Pulse${filename%.java}\", \"Ported from Meteor\", Category.${cat^^});" >> "$target_path"
            echo "    }" >> "$target_path"
            echo "}" >> "$target_path"
        done
    fi
done

# Wurst hacks
WURST_DIR="Wurst-Client-v7.53.1-MC1.21.11-decompiled/net/wurstclient/hacks"

# Map Wurst categories to Pulse categories
# Wurst doesn't have subfolders for all, so we'll put them in misc or try to guess

for file in "$WURST_DIR"/*.java; do
    filename=$(basename "$file")
    if [[ "$filename" == "KillauraHack.java" || "$filename" == "FlightHack.java" ]]; then continue; fi

    # Simple heuristic for category
    cat="misc"
    if [[ "$filename" =~ (Aura|Attack|Crit|Aim|Bow|Trigger|Sword|Armor|Potion|Soup) ]]; then cat="combat"; fi
    if [[ "$filename" =~ (Fly|Speed|Step|Jump|Walk|Sprint|Sneak|Spider|Blink|Jesus|Movement) ]]; then cat="movement"; fi
    if [[ "$filename" =~ (Eat|Fish|Respawn|Mine|Tool|Break|Place|Drop|Build|Steal|Inv|Chest) ]]; then cat="player"; fi
    if [[ "$filename" =~ (Esp|Tracer|Tag|Fullbright|XRay|Radar|Finder|Search|Hud|Ui|View|Overlay|Camera) ]]; then cat="render"; fi
    if [[ "$filename" =~ (Nuker|Timer|Weather|Sign|Farm|Tree|Boat|Sign) ]]; then cat="world"; fi

    target_name="Pulse${filename}"
    target_path="$PULSE_DIR/$cat/$target_name"

    # Check if already exists from Meteor
    if [ -f "$target_path" ]; then
        target_name="PulseWurst${filename}"
        target_path="$PULSE_DIR/$cat/$target_name"
    fi

    echo "package com.pulseclient.modules.$cat;" > "$target_path"
    echo "" >> "$target_path"
    echo "import com.pulseclient.Category;" >> "$target_path"
    echo "import com.pulseclient.Module;" >> "$target_path"
    echo "" >> "$target_path"
    echo "public class Pulse${filename%.java} extends Module {" >> "$target_path"
    echo "    public Pulse${filename%.java}() {" >> "$target_path"
    echo "        super(\"Pulse${filename%.java}\", \"Ported from Wurst\", Category.${cat^^});" >> "$target_path"
    echo "    }" >> "$target_path"
    echo "}" >> "$target_path"
done
