#!/bin/bash

MODULES_DIR="PulseClient/src/main/java/com/pulseclient/modules"
MANAGER_FILE="PulseClient/src/main/java/com/pulseclient/ModuleManager.java"

echo "package com.pulseclient;" > "$MANAGER_FILE"
echo "" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.combat.*;" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.movement.*;" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.player.*;" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.render.*;" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.world.*;" >> "$MANAGER_FILE"
echo "import com.pulseclient.modules.misc.*;" >> "$MANAGER_FILE"
echo "import java.util.ArrayList;" >> "$MANAGER_FILE"
echo "import java.util.List;" >> "$MANAGER_FILE"
echo "" >> "$MANAGER_FILE"
echo "public class ModuleManager {" >> "$MANAGER_FILE"
echo "    private final List<Module> modules = new ArrayList<>();" >> "$MANAGER_FILE"
echo "" >> "$MANAGER_FILE"
echo "    public void init() {" >> "$MANAGER_FILE"

# Find all Java files in subdirectories and add them
find "$MODULES_DIR" -name "*.java" | while read -r file; do
    classname=$(basename "$file" .java)
    echo "        modules.add(new $classname());" >> "$MANAGER_FILE"
done

echo "    }" >> "$MANAGER_FILE"
echo "" >> "$MANAGER_FILE"
echo "    public List<Module> getModules() {" >> "$MANAGER_FILE"
echo "        return modules;" >> "$MANAGER_FILE"
echo "    }" >> "$MANAGER_FILE"
echo "}" >> "$MANAGER_FILE"
