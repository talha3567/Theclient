package meteordevelopment.meteorclient.systems.modules.combat;

import com.google.common.util.concurrent.AtomicDouble;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import meteordevelopment.meteorclient.events.entity.EntityAddedEvent;
import meteordevelopment.meteorclient.events.entity.EntityRemovedEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.events.render.Render3DEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IAABB;
import meteordevelopment.meteorclient.mixininterface.IClipContext;
import meteordevelopment.meteorclient.mixininterface.IVec3;
import meteordevelopment.meteorclient.renderer.ShapeMode;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.ColorSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EntityTypeListSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.ModuleListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.combat.BedAura;
import meteordevelopment.meteorclient.utils.entity.DamageUtils;
import meteordevelopment.meteorclient.utils.entity.EntityUtils;
import meteordevelopment.meteorclient.utils.entity.Target;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.render.NametagUtils;
import meteordevelopment.meteorclient.utils.render.RenderUtils;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;
import meteordevelopment.meteorclient.utils.world.BlockIterator;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import meteordevelopment.meteorclient.utils.world.TickRate;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.network.protocol.game.ServerboundSwingPacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3d;

public class CrystalAura
extends Module {
    private final SettingGroup sgGeneral;
    private final SettingGroup sgSwitch;
    private final SettingGroup sgPlace;
    private final SettingGroup sgFacePlace;
    private final SettingGroup sgBreak;
    private final SettingGroup sgPause;
    private final SettingGroup sgRender;
    private final Setting<Double> targetRange;
    private final Setting<Boolean> predictMovement;
    private final Setting<Double> minDamage;
    private final Setting<Double> maxDamage;
    private final Setting<Boolean> antiSuicide;
    private final Setting<Boolean> ignoreNakeds;
    private final Setting<Boolean> rotate;
    private final Setting<YawStepMode> yawStepMode;
    private final Setting<Double> yawSteps;
    private final Setting<Set<EntityType<?>>> entities;
    private final Setting<AutoSwitchMode> autoSwitch;
    private final Setting<Integer> switchDelay;
    private final Setting<Boolean> noGapSwitch;
    private final Setting<Boolean> noBowSwitch;
    private final Setting<Boolean> antiWeakness;
    private final Setting<Boolean> doPlace;
    public final Setting<Integer> placeDelay;
    private final Setting<Double> placeRange;
    private final Setting<Double> placeWallsRange;
    private final Setting<Boolean> placement112;
    private final Setting<SupportMode> support;
    private final Setting<Integer> supportDelay;
    private final Setting<Boolean> facePlace;
    private final Setting<Double> facePlaceHealth;
    private final Setting<Double> facePlaceDurability;
    private final Setting<Boolean> facePlaceArmor;
    private final Setting<Keybind> forceFacePlace;
    private final Setting<Boolean> doBreak;
    private final Setting<Integer> breakDelay;
    private final Setting<Boolean> smartDelay;
    private final Setting<Double> breakRange;
    private final Setting<Double> breakWallsRange;
    private final Setting<Boolean> onlyBreakOwn;
    private final Setting<Integer> breakAttempts;
    private final Setting<Integer> ticksExisted;
    private final Setting<Integer> attackFrequency;
    private final Setting<Boolean> fastBreak;
    public final Setting<PauseMode> pauseOnUse;
    public final Setting<PauseMode> pauseOnMine;
    private final Setting<Boolean> pauseOnLag;
    public final Setting<List<Module>> pauseModules;
    public final Setting<Double> pauseHealth;
    public final Setting<SwingMode> swingMode;
    private final Setting<RenderMode> renderMode;
    private final Setting<Boolean> renderPlace;
    private final Setting<Integer> placeRenderTime;
    private final Setting<Boolean> renderBreak;
    private final Setting<Integer> breakRenderTime;
    private final Setting<Integer> smoothness;
    private final Setting<Double> height;
    private final Setting<Integer> renderTime;
    private final Setting<ShapeMode> shapeMode;
    private final Setting<SettingColor> sideColor;
    private final Setting<SettingColor> lineColor;
    private final Setting<Boolean> renderDamageText;
    private final Setting<SettingColor> damageColor;
    private final Setting<Double> damageTextScale;
    private Item mainItem;
    private Item offItem;
    private int breakTimer;
    private int placeTimer;
    private int switchTimer;
    private int ticksPassed;
    private final List<LivingEntity> targets;
    private final Vec3 vec3d;
    private final Vec3 playerEyePos;
    private final Vector3d vec3;
    private final BlockPos.MutableBlockPos blockPos;
    private final AABB box;
    private final Vec3 vec3dRayTraceEnd;
    private ClipContext clipContext;
    private final IntSet placedCrystals;
    private boolean placing;
    private int placingTimer;
    public int kaTimer;
    private final BlockPos.MutableBlockPos placingCrystalBlockPos;
    private final IntSet removed;
    private final Int2IntMap attemptedBreaks;
    private final Int2IntMap waitingToExplode;
    private int attacks;
    private double serverYaw;
    private LivingEntity bestTarget;
    private double bestTargetDamage;
    private int bestTargetTimer;
    private boolean didRotateThisTick;
    private boolean isLastRotationPos;
    private final Vec3 lastRotationPos;
    private double lastYaw;
    private double lastPitch;
    private int lastRotationTimer;
    private int placeRenderTimer;
    private int breakRenderTimer;
    private final BlockPos.MutableBlockPos placeRenderPos;
    private final BlockPos.MutableBlockPos breakRenderPos;
    private AABB renderBoxOne;
    private AABB renderBoxTwo;
    private double renderDamage;

    public CrystalAura() {
        super(Categories.Combat, "crystal-aura", "Automatically places and attacks crystals.");
        this.sgGeneral = this.settings.getDefaultGroup();
        this.sgSwitch = this.settings.createGroup("Switch");
        this.sgPlace = this.settings.createGroup("Place");
        this.sgFacePlace = this.settings.createGroup("Face Place");
        this.sgBreak = this.settings.createGroup("Break");
        this.sgPause = this.settings.createGroup("Pause");
        this.sgRender = this.settings.createGroup("Render");
        this.targetRange = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("target-range")).description("Range in which to target players.")).defaultValue(10.0).min(0.0).sliderMax(16.0).build());
        this.predictMovement = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("predict-movement")).description("Predicts target movement.")).defaultValue(false)).build());
        this.minDamage = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("min-damage")).description("Minimum damage the crystal needs to deal to your target.")).defaultValue(6.0).min(0.0).build());
        this.maxDamage = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("max-damage")).description("Maximum damage crystals can deal to yourself.")).defaultValue(6.0).range(0.0, 36.0).sliderMax(36.0).build());
        this.antiSuicide = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-suicide")).description("Will not place and break crystals if they will kill you.")).defaultValue(true)).build());
        this.ignoreNakeds = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("ignore-nakeds")).description("Ignore players with no items.")).defaultValue(false)).build());
        this.rotate = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("rotate")).description("Rotates server-side towards the crystals being hit/placed.")).defaultValue(true)).build());
        this.yawStepMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("yaw-steps-mode")).description("When to run the yaw steps check.")).defaultValue(YawStepMode.Break)).visible(this.rotate::get)).build());
        this.yawSteps = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("yaw-steps")).description("Maximum number of degrees its allowed to rotate in one tick.")).defaultValue(180.0).range(1.0, 180.0).visible(this.rotate::get)).build());
        this.entities = this.sgGeneral.add(((EntityTypeListSetting.Builder)((EntityTypeListSetting.Builder)new EntityTypeListSetting.Builder().name("entities")).description("Entities to attack.")).onlyAttackable().defaultValue(EntityType.PLAYER, EntityType.WARDEN, EntityType.WITHER).build());
        this.autoSwitch = this.sgSwitch.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("auto-switch")).description("Switches to crystals in your hotbar once a target is found.")).defaultValue(AutoSwitchMode.Normal)).build());
        this.switchDelay = this.sgSwitch.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("switch-delay")).description("The delay in ticks to wait to break a crystal after switching hotbar slot.")).defaultValue(0)).min(0).build());
        this.noGapSwitch = this.sgSwitch.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-gap-switch")).description("Won't auto switch if you're holding a gapple.")).defaultValue(true)).visible(() -> this.autoSwitch.get() == AutoSwitchMode.Normal)).build());
        this.noBowSwitch = this.sgSwitch.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("no-bow-switch")).description("Won't auto switch if you're holding a bow.")).defaultValue(true)).build());
        this.antiWeakness = this.sgSwitch.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("anti-weakness")).description("Switches to tools with so you can break crystals with the weakness effect.")).defaultValue(true)).build());
        this.doPlace = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("place")).description("If the CA should place crystals.")).defaultValue(true)).build());
        this.placeDelay = this.sgPlace.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-delay")).description("The delay in ticks to wait to place a crystal after it's exploded.")).defaultValue(0)).min(0).sliderMax(20).build());
        this.placeRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("place-range")).description("Range in which to place crystals.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.placeWallsRange = this.sgPlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to place crystals when behind blocks.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.placement112 = this.sgPlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("1.12-placement")).description("Uses 1.12 crystal placement.")).defaultValue(false)).build());
        this.support = this.sgPlace.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("support")).description("Places a support block in air if no other position have been found.")).defaultValue(SupportMode.Disabled)).build());
        this.supportDelay = this.sgPlace.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("support-delay")).description("Delay in ticks after placing support block.")).defaultValue(1)).min(0).visible(() -> this.support.get() != SupportMode.Disabled)).build());
        this.facePlace = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("face-place")).description("Will face-place when target is below a certain health or armor durability threshold.")).defaultValue(true)).build());
        this.facePlaceHealth = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("face-place-health")).description("The health the target has to be at to start face placing.")).defaultValue(8.0).min(1.0).sliderMin(1.0).sliderMax(36.0).visible(this.facePlace::get)).build());
        this.facePlaceDurability = this.sgFacePlace.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("face-place-durability")).description("The durability threshold percentage to be able to face-place.")).defaultValue(2.0).min(1.0).sliderMin(1.0).sliderMax(100.0).visible(this.facePlace::get)).build());
        this.facePlaceArmor = this.sgFacePlace.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("face-place-missing-armor")).description("Automatically starts face placing when a target misses a piece of armor.")).defaultValue(false)).visible(this.facePlace::get)).build());
        this.forceFacePlace = this.sgFacePlace.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)new KeybindSetting.Builder().name("force-face-place")).description("Starts face place when this button is pressed.")).defaultValue(Keybind.none())).build());
        this.doBreak = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("break")).description("If the CA should break crystals.")).defaultValue(true)).build());
        this.breakDelay = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-delay")).description("The delay in ticks to wait to break a crystal after it's placed.")).defaultValue(0)).min(0).sliderMax(20).build());
        this.smartDelay = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("smart-delay")).description("Only breaks crystals when the target can receive damage.")).defaultValue(false)).build());
        this.breakRange = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("break-range")).description("Range in which to break crystals.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.breakWallsRange = this.sgBreak.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("walls-range")).description("Range in which to break crystals when behind blocks.")).defaultValue(4.5).min(0.0).sliderMax(6.0).build());
        this.onlyBreakOwn = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("only-own")).description("Only breaks own crystals.")).defaultValue(false)).build());
        this.breakAttempts = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-attempts")).description("How many times to hit a crystal before stopping to target it.")).defaultValue(2)).sliderMin(1).sliderMax(5).build());
        this.ticksExisted = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("ticks-existed")).description("Amount of ticks a crystal needs to have lived for it to be attacked by CrystalAura.")).defaultValue(0)).min(0).build());
        this.attackFrequency = this.sgBreak.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("attack-frequency")).description("Maximum hits to do per second.")).defaultValue(25)).min(1).sliderRange(1, 30).build());
        this.fastBreak = this.sgBreak.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("fast-break")).description("Ignores break delay and tries to break the crystal as soon as it's spawned in the world.")).defaultValue(true)).build());
        this.pauseOnUse = this.sgPause.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("pause-on-use")).description("Which processes should be paused while using an item.")).defaultValue(PauseMode.Place)).build());
        this.pauseOnMine = this.sgPause.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("pause-on-mine")).description("Which processes should be paused while mining a block.")).defaultValue(PauseMode.None)).build());
        this.pauseOnLag = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("pause-on-lag")).description("Whether to pause if the server is not responding.")).defaultValue(true)).build());
        this.pauseModules = this.sgPause.add(((ModuleListSetting.Builder)((ModuleListSetting.Builder)new ModuleListSetting.Builder().name("pause-modules")).description("Pauses while any of the selected modules are active.")).defaultValue(BedAura.class).build());
        this.pauseHealth = this.sgPause.add(((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("pause-health")).description("Pauses when you go below a certain health.")).defaultValue(5.0).range(0.0, 36.0).sliderRange(0.0, 36.0).build());
        this.swingMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("swing-mode")).description("How to swing when placing.")).defaultValue(SwingMode.Both)).build());
        this.renderMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("render-mode")).description("The mode to render in.")).defaultValue(RenderMode.Normal)).build());
        this.renderPlace = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-place")).description("Renders a block overlay over the block the crystals are being placed on.")).defaultValue(true)).visible(() -> this.renderMode.get() == RenderMode.Normal)).build());
        this.placeRenderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("place-time")).description("How long to render placements.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.renderMode.get() == RenderMode.Normal && this.renderPlace.get() != false)).build());
        this.renderBreak = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("render-break")).description("Renders a block overlay over the block the crystals are broken on.")).defaultValue(false)).visible(() -> this.renderMode.get() == RenderMode.Normal)).build());
        this.breakRenderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("break-time")).description("How long to render breaking for.")).defaultValue(13)).min(0).sliderMax(20).visible(() -> this.renderMode.get() == RenderMode.Normal && this.renderBreak.get() != false)).build());
        this.smoothness = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("smoothness")).description("How smoothly the render should move around.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.renderMode.get() == RenderMode.Smooth)).build());
        this.height = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("height")).description("How tall the gradient should be.")).defaultValue(0.7).min(0.0).sliderMax(1.0).visible(() -> this.renderMode.get() == RenderMode.Gradient)).build());
        this.renderTime = this.sgRender.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)new IntSetting.Builder().name("render-time")).description("How long to render placements.")).defaultValue(10)).min(0).sliderMax(20).visible(() -> this.renderMode.get() == RenderMode.Smooth || this.renderMode.get() == RenderMode.Fading)).build());
        this.shapeMode = this.sgRender.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)new EnumSetting.Builder().name("shape-mode")).description("How the shapes are rendered.")).defaultValue(ShapeMode.Both)).visible(() -> this.renderMode.get() != RenderMode.None)).build());
        this.sideColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("side-color")).description("The side color of the block overlay.")).defaultValue(new SettingColor(255, 255, 255, 45)).visible(() -> this.shapeMode.get().sides() && this.renderMode.get() != RenderMode.None)).build());
        this.lineColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("line-color")).description("The line color of the block overlay.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.shapeMode.get().lines() && this.renderMode.get() != RenderMode.None)).build());
        this.renderDamageText = this.sgRender.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)new BoolSetting.Builder().name("damage")).description("Renders crystal damage text in the block overlay.")).defaultValue(true)).visible(() -> this.renderMode.get() != RenderMode.None)).build());
        this.damageColor = this.sgRender.add(((ColorSetting.Builder)((ColorSetting.Builder)((ColorSetting.Builder)new ColorSetting.Builder().name("damage-color")).description("The color of the damage text.")).defaultValue(new SettingColor(255, 255, 255)).visible(() -> this.renderMode.get() != RenderMode.None && this.renderDamageText.get() != false)).build());
        this.damageTextScale = this.sgRender.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)new DoubleSetting.Builder().name("damage-scale")).description("How big the damage text should be.")).defaultValue(1.25).min(1.0).sliderMax(4.0).visible(() -> this.renderMode.get() != RenderMode.None && this.renderDamageText.get() != false)).build());
        this.targets = new ArrayList<LivingEntity>();
        this.vec3d = new Vec3(0.0, 0.0, 0.0);
        this.playerEyePos = new Vec3(0.0, 0.0, 0.0);
        this.vec3 = new Vector3d();
        this.blockPos = new BlockPos.MutableBlockPos();
        this.box = new AABB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);
        this.vec3dRayTraceEnd = new Vec3(0.0, 0.0, 0.0);
        this.placedCrystals = new IntOpenHashSet();
        this.placingCrystalBlockPos = new BlockPos.MutableBlockPos();
        this.removed = new IntOpenHashSet();
        this.attemptedBreaks = new Int2IntOpenHashMap();
        this.waitingToExplode = new Int2IntOpenHashMap();
        this.lastRotationPos = new Vec3(0.0, 0.0, 0.0);
        this.placeRenderPos = new BlockPos.MutableBlockPos();
        this.breakRenderPos = new BlockPos.MutableBlockPos();
    }

    @Override
    public void onActivate() {
        this.breakTimer = 0;
        this.placeTimer = 0;
        this.ticksPassed = 0;
        this.clipContext = new ClipContext(new Vec3(0.0, 0.0, 0.0), new Vec3(0.0, 0.0, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
        this.placing = false;
        this.placingTimer = 0;
        this.kaTimer = 0;
        this.attacks = 0;
        this.serverYaw = this.mc.player.getYRot();
        this.bestTargetDamage = 0.0;
        this.bestTargetTimer = 0;
        this.lastRotationTimer = this.getLastRotationStopDelay();
        this.placeRenderTimer = 0;
        this.breakRenderTimer = 0;
    }

    @Override
    public void onDeactivate() {
        this.targets.clear();
        this.placedCrystals.clear();
        this.attemptedBreaks.clear();
        this.waitingToExplode.clear();
        this.removed.clear();
        this.bestTarget = null;
    }

    private int getLastRotationStopDelay() {
        return Math.max(10, this.placeDelay.get() / 2 + this.breakDelay.get() / 2 + 10);
    }

    @EventHandler(priority=100)
    private void onPreTick(TickEvent.Pre event) {
        this.didRotateThisTick = false;
        ++this.lastRotationTimer;
        if (this.placing) {
            if (this.placingTimer > 0) {
                --this.placingTimer;
            } else {
                this.placing = false;
            }
        }
        if (this.kaTimer > 0) {
            --this.kaTimer;
        }
        if (this.ticksPassed < 20) {
            ++this.ticksPassed;
        } else {
            this.ticksPassed = 0;
            this.attacks = 0;
        }
        if (this.bestTargetTimer > 0) {
            --this.bestTargetTimer;
        }
        this.bestTargetDamage = 0.0;
        if (this.breakTimer > 0) {
            --this.breakTimer;
        }
        if (this.placeTimer > 0) {
            --this.placeTimer;
        }
        if (this.switchTimer > 0) {
            --this.switchTimer;
        }
        if (this.placeRenderTimer > 0) {
            --this.placeRenderTimer;
        }
        if (this.breakRenderTimer > 0) {
            --this.breakRenderTimer;
        }
        this.mainItem = this.mc.player.getMainHandItem().getItem();
        this.offItem = this.mc.player.getOffhandItem().getItem();
        IntIterator it = this.waitingToExplode.keySet().iterator();
        while (it.hasNext()) {
            int id = it.nextInt();
            int ticks = this.waitingToExplode.get(id);
            if (ticks > 3) {
                it.remove();
                this.removed.remove(id);
                continue;
            }
            this.waitingToExplode.put(id, ticks + 1);
        }
        ((IVec3)this.playerEyePos).meteor$set(this.mc.player.position().x, this.mc.player.position().y + (double)this.mc.player.getEyeHeight(this.mc.player.getPose()), this.mc.player.position().z);
        this.findTargets();
        if (!this.targets.isEmpty()) {
            if (!this.didRotateThisTick) {
                this.doBreak();
            }
            if (!this.didRotateThisTick) {
                this.doPlace();
            }
        }
    }

    @EventHandler(priority=-866)
    private void onPreTickLast(TickEvent.Pre event) {
        if (this.rotate.get().booleanValue() && this.lastRotationTimer < this.getLastRotationStopDelay() && !this.didRotateThisTick) {
            Rotations.rotate(this.isLastRotationPos ? Rotations.getYaw(this.lastRotationPos) : this.lastYaw, this.isLastRotationPos ? Rotations.getPitch(this.lastRotationPos) : this.lastPitch, -100, null);
        }
    }

    @EventHandler
    private void onEntityAdded(EntityAddedEvent event) {
        float damage;
        if (!(event.entity instanceof EndCrystal)) {
            return;
        }
        if (this.placing && event.entity.blockPosition().equals((Object)this.placingCrystalBlockPos)) {
            this.placing = false;
            this.placingTimer = 0;
            this.placedCrystals.add(event.entity.getId());
        }
        if (this.fastBreak.get().booleanValue() && !this.didRotateThisTick && this.attacks < this.attackFrequency.get() && (double)(damage = this.getBreakDamage(event.entity, true)) > this.minDamage.get()) {
            this.doBreak(event.entity);
        }
    }

    @EventHandler
    private void onEntityRemoved(EntityRemovedEvent event) {
        if (event.entity instanceof EndCrystal) {
            this.placedCrystals.remove(event.entity.getId());
            this.removed.remove(event.entity.getId());
            this.waitingToExplode.remove(event.entity.getId());
        }
    }

    private void setRotation(boolean isPos, Vec3 pos, double yaw, double pitch) {
        this.didRotateThisTick = true;
        this.isLastRotationPos = isPos;
        if (isPos) {
            ((IVec3)this.lastRotationPos).meteor$set(pos.x, pos.y, pos.z);
        } else {
            this.lastYaw = yaw;
            this.lastPitch = pitch;
        }
        this.lastRotationTimer = 0;
    }

    private void doBreak() {
        if (!this.doBreak.get().booleanValue() || this.breakTimer > 0 || this.switchTimer > 0 || this.attacks >= this.attackFrequency.get()) {
            return;
        }
        if (this.shouldPause(PauseMode.Break)) {
            return;
        }
        float bestDamage = 0.0f;
        Entity crystal = null;
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            float damage = this.getBreakDamage(entity, true);
            if (!(damage > bestDamage)) continue;
            bestDamage = damage;
            crystal = entity;
        }
        if (crystal != null) {
            this.doBreak(crystal);
        }
    }

    private float getBreakDamage(Entity entity, boolean checkCrystalAge) {
        double minimumDamage;
        if (!(entity instanceof EndCrystal)) {
            return 0.0f;
        }
        if (this.onlyBreakOwn.get().booleanValue() && !this.placedCrystals.contains(entity.getId())) {
            return 0.0f;
        }
        if (this.removed.contains(entity.getId())) {
            return 0.0f;
        }
        if (this.attemptedBreaks.get(entity.getId()) > this.breakAttempts.get()) {
            return 0.0f;
        }
        if (checkCrystalAge && entity.tickCount < this.ticksExisted.get()) {
            return 0.0f;
        }
        if (this.isOutOfRange(entity.position(), entity.blockPosition(), false)) {
            return 0.0f;
        }
        this.blockPos.set((Vec3i)entity.blockPosition()).move(0, -1, 0);
        float selfDamage = DamageUtils.crystalDamage((LivingEntity)this.mc.player, entity.position(), this.predictMovement.get(), (BlockPos)this.blockPos);
        if ((double)selfDamage > this.maxDamage.get() || this.antiSuicide.get().booleanValue() && selfDamage >= EntityUtils.getTotalHealth((LivingEntity)this.mc.player)) {
            return 0.0f;
        }
        float damage = this.getDamageToTargets(entity.position(), (BlockPos)this.blockPos, true, false);
        boolean shouldFacePlace = this.shouldFacePlace();
        double d = minimumDamage = shouldFacePlace ? Math.min(this.minDamage.get(), 1.5) : this.minDamage.get();
        if ((double)damage < minimumDamage) {
            return 0.0f;
        }
        return damage;
    }

    private void doBreak(Entity crystal) {
        if (this.antiWeakness.get().booleanValue()) {
            MobEffectInstance weakness = this.mc.player.getEffect(MobEffects.WEAKNESS);
            MobEffectInstance strength = this.mc.player.getEffect(MobEffects.STRENGTH);
            if (!(weakness == null || strength != null && strength.getAmplifier() > weakness.getAmplifier() || this.isValidWeaknessItem(this.mc.player.getMainHandItem(), crystal))) {
                if (!InvUtils.swap(InvUtils.findInHotbar(stack -> this.isValidWeaknessItem((ItemStack)stack, crystal)).slot(), false)) {
                    return;
                }
                this.switchTimer = 1;
                return;
            }
        }
        boolean attacked = true;
        if (this.rotate.get().booleanValue()) {
            double pitch;
            double yaw = Rotations.getYaw(crystal);
            if (this.doYawSteps(yaw, pitch = Rotations.getPitch(crystal, Target.Feet))) {
                this.setRotation(true, crystal.position(), 0.0, 0.0);
                Rotations.rotate(yaw, pitch, 50, () -> this.attackCrystal(crystal));
                this.breakTimer = this.breakDelay.get();
            } else {
                attacked = false;
            }
        } else {
            this.attackCrystal(crystal);
            this.breakTimer = this.breakDelay.get();
        }
        if (attacked) {
            this.removed.add(crystal.getId());
            this.attemptedBreaks.put(crystal.getId(), this.attemptedBreaks.get(crystal.getId()) + 1);
            this.waitingToExplode.put(crystal.getId(), 0);
            this.breakRenderPos.set((Vec3i)crystal.blockPosition().below());
            this.breakRenderTimer = this.breakRenderTime.get();
        }
    }

    private boolean isValidWeaknessItem(ItemStack itemStack, Entity crystal) {
        return DamageUtils.getAttackDamage((LivingEntity)this.mc.player, crystal, itemStack) > 0.0f;
    }

    private void attackCrystal(Entity entity) {
        this.mc.player.connection.send((Packet)new ServerboundAttackPacket(entity.getId()));
        InteractionHand hand = InvUtils.findInHotbar(Items.END_CRYSTAL).getHand();
        if (hand == null) {
            hand = InteractionHand.MAIN_HAND;
        }
        if (this.swingMode.get().client()) {
            this.mc.player.swing(hand);
        }
        if (this.swingMode.get().packet()) {
            this.mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
        }
        ++this.attacks;
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (event.packet instanceof ServerboundSetCarriedItemPacket) {
            this.switchTimer = this.switchDelay.get();
        }
    }

    private void doPlace() {
        if (!this.doPlace.get().booleanValue() || this.placeTimer > 0) {
            return;
        }
        if (this.shouldPause(PauseMode.Place)) {
            return;
        }
        if (!InvUtils.testInHotbar(Items.END_CRYSTAL)) {
            return;
        }
        if (this.autoSwitch.get() != AutoSwitchMode.None) {
            if (this.noGapSwitch.get().booleanValue() && this.autoSwitch.get() == AutoSwitchMode.Normal && this.offItem != Items.END_CRYSTAL && (this.mainItem == Items.ENCHANTED_GOLDEN_APPLE || this.offItem == Items.ENCHANTED_GOLDEN_APPLE || this.mainItem == Items.GOLDEN_APPLE || this.offItem == Items.GOLDEN_APPLE)) {
                return;
            }
            if (this.noBowSwitch.get().booleanValue() && (this.mainItem == Items.BOW || this.offItem == Items.BOW)) {
                return;
            }
        } else if (this.mainItem != Items.END_CRYSTAL && this.offItem != Items.END_CRYSTAL) {
            return;
        }
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!(this.getBreakDamage(entity, false) > 0.0f)) continue;
            return;
        }
        AtomicDouble bestDamage = new AtomicDouble(0.0);
        AtomicReference<BlockPos.MutableBlockPos> bestBlockPos = new AtomicReference<BlockPos.MutableBlockPos>(new BlockPos.MutableBlockPos());
        AtomicBoolean isSupport = new AtomicBoolean(this.support.get() != SupportMode.Disabled);
        BlockIterator.register((int)Math.ceil(this.placeRange.get()), (int)Math.ceil(this.placeRange.get()), (bp, blockState) -> {
            boolean hasBlock;
            boolean bl = hasBlock = blockState.is((Object)Blocks.BEDROCK) || blockState.is((Object)Blocks.OBSIDIAN);
            if (!(hasBlock || isSupport.get() && blockState.canBeReplaced())) {
                return;
            }
            this.blockPos.set(bp.getX(), bp.getY() + 1, bp.getZ());
            if (!this.mc.level.getBlockState((BlockPos)this.blockPos).isAir()) {
                return;
            }
            if (this.placement112.get().booleanValue()) {
                this.blockPos.move(0, 1, 0);
                if (!this.mc.level.getBlockState((BlockPos)this.blockPos).isAir()) {
                    return;
                }
            }
            ((IVec3)this.vec3d).meteor$set((double)bp.getX() + 0.5, bp.getY() + 1, (double)bp.getZ() + 0.5);
            this.blockPos.set((Vec3i)bp).move(0, 1, 0);
            if (this.isOutOfRange(this.vec3d, (BlockPos)this.blockPos, true)) {
                return;
            }
            float selfDamage = DamageUtils.crystalDamage((LivingEntity)this.mc.player, this.vec3d, this.predictMovement.get(), bp);
            if ((double)selfDamage > this.maxDamage.get() || this.antiSuicide.get().booleanValue() && selfDamage >= EntityUtils.getTotalHealth((LivingEntity)this.mc.player)) {
                return;
            }
            float damage = this.getDamageToTargets(this.vec3d, (BlockPos)bp, false, !hasBlock && this.support.get() == SupportMode.Fast);
            boolean shouldFacePlace = this.shouldFacePlace();
            double minimumDamage = Math.min(this.minDamage.get(), shouldFacePlace ? 1.5 : this.minDamage.get());
            if ((double)damage < minimumDamage) {
                return;
            }
            double x = bp.getX();
            double y = bp.getY() + 1;
            double z = bp.getZ();
            ((IAABB)this.box).meteor$set(x, y, z, x + 1.0, y + (double)(this.placement112.get() != false ? 1 : 2), z + 1.0);
            if (this.intersectsWithEntities(this.box)) {
                return;
            }
            if ((double)damage > bestDamage.get() || isSupport.get() && hasBlock) {
                bestDamage.set(damage);
                ((BlockPos.MutableBlockPos)bestBlockPos.get()).set((Vec3i)bp);
            }
            if (hasBlock) {
                isSupport.set(false);
            }
        });
        BlockIterator.after(() -> {
            if (bestDamage.get() == 0.0) {
                return;
            }
            BlockHitResult result = this.getPlaceInfo((BlockPos)bestBlockPos.get());
            ((IVec3)this.vec3d).meteor$set((double)result.getBlockPos().getX() + 0.5 + (double)result.getDirection().getUnitVec3i().getX() * 1.0 / 2.0, (double)result.getBlockPos().getY() + 0.5 + (double)result.getDirection().getUnitVec3i().getY() * 1.0 / 2.0, (double)result.getBlockPos().getZ() + 0.5 + (double)result.getDirection().getUnitVec3i().getZ() * 1.0 / 2.0);
            if (this.rotate.get().booleanValue()) {
                double yaw = Rotations.getYaw(this.vec3d);
                double pitch = Rotations.getPitch(this.vec3d);
                if (this.yawStepMode.get() == YawStepMode.Break || this.doYawSteps(yaw, pitch)) {
                    this.setRotation(true, this.vec3d, 0.0, 0.0);
                    Rotations.rotate(yaw, pitch, 50, () -> this.placeCrystal(result, bestDamage.get(), isSupport.get() ? (BlockPos)bestBlockPos.get() : null));
                    this.placeTimer += this.placeDelay.get().intValue();
                }
            } else {
                this.placeCrystal(result, bestDamage.get(), isSupport.get() ? (BlockPos)bestBlockPos.get() : null);
                this.placeTimer += this.placeDelay.get().intValue();
            }
        });
    }

    private BlockHitResult getPlaceInfo(BlockPos blockPos) {
        ((IVec3)this.vec3d).meteor$set(this.mc.player.getX(), this.mc.player.getY() + (double)this.mc.player.getEyeHeight(this.mc.player.getPose()), this.mc.player.getZ());
        for (Direction side : Direction.values()) {
            ((IVec3)this.vec3dRayTraceEnd).meteor$set((double)blockPos.getX() + 0.5 + (double)side.getUnitVec3i().getX() * 0.5, (double)blockPos.getY() + 0.5 + (double)side.getUnitVec3i().getY() * 0.5, (double)blockPos.getZ() + 0.5 + (double)side.getUnitVec3i().getZ() * 0.5);
            ((IClipContext)this.clipContext).meteor$set(this.vec3d, this.vec3dRayTraceEnd, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
            BlockHitResult result = this.mc.level.clip(this.clipContext);
            if (result == null || result.getType() != HitResult.Type.BLOCK || !result.getBlockPos().equals((Object)blockPos)) continue;
            return result;
        }
        Direction side = (double)blockPos.getY() > this.vec3d.y ? Direction.DOWN : Direction.UP;
        return new BlockHitResult(this.vec3d, side, blockPos, false);
    }

    private void placeCrystal(BlockHitResult result, double damage, BlockPos supportBlock) {
        InteractionHand hand;
        Item targetItem = supportBlock == null ? Items.END_CRYSTAL : Items.OBSIDIAN;
        FindItemResult item = InvUtils.findInHotbar(targetItem);
        if (!item.found()) {
            return;
        }
        int prevSlot = this.mc.player.getInventory().getSelectedSlot();
        if (this.autoSwitch.get() != AutoSwitchMode.None && !item.isOffhand()) {
            InvUtils.swap(item.slot(), false);
        }
        if ((hand = item.getHand()) == null) {
            return;
        }
        if (supportBlock == null) {
            this.mc.gameMode.startPrediction(this.mc.level, sequence -> new ServerboundUseItemOnPacket(hand, result, sequence));
            if (this.swingMode.get().client()) {
                this.mc.player.swing(hand);
            }
            if (this.swingMode.get().packet()) {
                this.mc.getConnection().send((Packet)new ServerboundSwingPacket(hand));
            }
            this.placing = true;
            this.placingTimer = 4;
            this.kaTimer = 8;
            this.placingCrystalBlockPos.set((Vec3i)result.getBlockPos()).move(0, 1, 0);
            this.placeRenderPos.set((Vec3i)result.getBlockPos());
            this.renderDamage = damage;
            if (this.renderMode.get() == RenderMode.Normal) {
                this.placeRenderTimer = this.placeRenderTime.get();
            } else {
                this.placeRenderTimer = this.renderTime.get();
                if (this.renderMode.get() == RenderMode.Fading) {
                    RenderUtils.renderTickingBlock((BlockPos)this.placeRenderPos, this.sideColor.get(), this.lineColor.get(), this.shapeMode.get(), 0, this.renderTime.get(), true, false);
                }
            }
        } else {
            BlockUtils.place(supportBlock, item, false, 0, this.swingMode.get().client(), true, false);
            this.placeTimer += this.supportDelay.get().intValue();
            if (this.supportDelay.get() == 0) {
                this.placeCrystal(result, damage, null);
            }
        }
        if (this.autoSwitch.get() == AutoSwitchMode.Silent) {
            InvUtils.swap(prevSlot, false);
        }
    }

    @EventHandler
    private void onPacketSent(PacketEvent.Sent event) {
        Packet<?> packet = event.packet;
        if (packet instanceof ServerboundMovePlayerPacket) {
            ServerboundMovePlayerPacket packet2 = (ServerboundMovePlayerPacket)packet;
            this.serverYaw = packet2.getYRot((float)this.serverYaw);
        }
    }

    public boolean doYawSteps(double targetYaw, double targetPitch) {
        targetYaw = Mth.wrapDegrees((double)targetYaw) + 180.0;
        double serverYaw = Mth.wrapDegrees((double)this.serverYaw) + 180.0;
        if (CrystalAura.distanceBetweenAngles(serverYaw, targetYaw) <= this.yawSteps.get()) {
            return true;
        }
        double delta = Math.abs(targetYaw - serverYaw);
        double yaw = this.serverYaw;
        yaw = serverYaw < targetYaw ? (delta < 180.0 ? (yaw += this.yawSteps.get().doubleValue()) : (yaw -= this.yawSteps.get().doubleValue())) : (delta < 180.0 ? (yaw -= this.yawSteps.get().doubleValue()) : (yaw += this.yawSteps.get().doubleValue()));
        this.setRotation(false, null, yaw, targetPitch);
        Rotations.rotate(yaw, targetPitch, -100, null);
        return false;
    }

    private static double distanceBetweenAngles(double alpha, double beta) {
        double phi = Math.abs(beta - alpha) % 360.0;
        return phi > 180.0 ? 360.0 - phi : phi;
    }

    private boolean shouldFacePlace() {
        if (!this.facePlace.get().booleanValue()) {
            return false;
        }
        if (this.forceFacePlace.get().isPressed()) {
            return true;
        }
        for (LivingEntity target : this.targets) {
            if ((double)EntityUtils.getTotalHealth(target) <= this.facePlaceHealth.get()) {
                return true;
            }
            for (EquipmentSlot slot : EquipmentSlotGroup.ARMOR) {
                ItemStack itemStack = target.getItemBySlot(slot);
                if (!(itemStack == null || itemStack.isEmpty() ? this.facePlaceArmor.get() != false : (double)(itemStack.getMaxDamage() - itemStack.getDamageValue()) / (double)itemStack.getMaxDamage() * 100.0 <= this.facePlaceDurability.get())) continue;
                return true;
            }
        }
        return false;
    }

    private boolean shouldPause(PauseMode process) {
        if ((this.mc.player.isUsingItem() || this.mc.options.keyUse.isDown()) && this.pauseOnUse.get().matches(process)) {
            return true;
        }
        if (this.pauseOnLag.get().booleanValue() && TickRate.INSTANCE.getTimeSinceLastTick() >= 1.0f) {
            return true;
        }
        for (Module module : this.pauseModules.get()) {
            if (!module.isActive()) continue;
            return true;
        }
        if (this.pauseOnMine.get().matches(process) && this.mc.gameMode.isDestroying()) {
            return true;
        }
        return (double)EntityUtils.getTotalHealth((LivingEntity)this.mc.player) <= this.pauseHealth.get();
    }

    private boolean isOutOfRange(Vec3 vec3d, BlockPos blockPos, boolean place) {
        ((IClipContext)this.clipContext).meteor$set(this.playerEyePos, vec3d, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this.mc.player);
        BlockHitResult result = this.mc.level.clip(this.clipContext);
        if (result == null || !result.getBlockPos().equals((Object)blockPos)) {
            return !PlayerUtils.isWithin(vec3d, (double)(place ? this.placeWallsRange : this.breakWallsRange).get());
        }
        return !PlayerUtils.isWithin(vec3d, (double)(place ? this.placeRange : this.breakRange).get());
    }

    private LivingEntity getNearestTarget() {
        LivingEntity nearestTarget = null;
        double nearestDistance = Double.MAX_VALUE;
        for (LivingEntity target : this.targets) {
            double distance = PlayerUtils.squaredDistanceTo((Entity)target);
            if (!(distance < nearestDistance)) continue;
            nearestTarget = target;
            nearestDistance = distance;
        }
        return nearestTarget;
    }

    private float getDamageToTargets(Vec3 vec3d, BlockPos obsidianPos, boolean breaking, boolean fast) {
        float damage = 0.0f;
        if (fast) {
            LivingEntity target = this.getNearestTarget();
            if (!this.smartDelay.get().booleanValue() || !breaking || target.hurtTime <= 0) {
                damage = DamageUtils.crystalDamage(target, vec3d, this.predictMovement.get(), obsidianPos);
            }
        } else {
            for (LivingEntity target : this.targets) {
                if (this.smartDelay.get().booleanValue() && breaking && target.hurtTime > 0) continue;
                float dmg = DamageUtils.crystalDamage(target, vec3d, this.predictMovement.get(), obsidianPos);
                if ((double)dmg > this.bestTargetDamage) {
                    this.bestTarget = target;
                    this.bestTargetDamage = dmg;
                    this.bestTargetTimer = 10;
                }
                damage += dmg;
            }
        }
        return damage;
    }

    @Override
    public String getInfoString() {
        return this.bestTarget != null && this.bestTargetTimer > 0 ? EntityUtils.getName((Entity)this.bestTarget) : null;
    }

    private void findTargets() {
        this.targets.clear();
        for (Entity entity : this.mc.level.entitiesForRendering()) {
            if (!(entity instanceof LivingEntity)) continue;
            LivingEntity livingEntity = (LivingEntity)entity;
            if (livingEntity instanceof Player) {
                Player player = (Player)livingEntity;
                if (player.getAbilities().instabuild || livingEntity == this.mc.player || !player.isAlive() || !Friends.get().shouldAttack(player) || this.ignoreNakeds.get().booleanValue() && player.getOffhandItem().isEmpty() && player.getMainHandItem().isEmpty() && player.getItemBySlot(EquipmentSlot.FEET).isEmpty() && player.getItemBySlot(EquipmentSlot.LEGS).isEmpty() && player.getItemBySlot(EquipmentSlot.CHEST).isEmpty() && player.getItemBySlot(EquipmentSlot.HEAD).isEmpty()) continue;
            }
            if (!this.entities.get().contains(livingEntity.getType()) || livingEntity.distanceToSqr((Entity)this.mc.player) > this.targetRange.get() * this.targetRange.get()) continue;
            this.targets.add(livingEntity);
        }
    }

    private boolean intersectsWithEntities(AABB box) {
        return EntityUtils.intersectsWithEntity(box, entity -> !entity.isSpectator() && !this.removed.contains(entity.getId()));
    }

    @EventHandler
    private void onRender(Render3DEvent event) {
        if (this.renderMode.get() == RenderMode.None) {
            return;
        }
        switch (this.renderMode.get().ordinal()) {
            case 0: {
                if (this.renderPlace.get().booleanValue() && this.placeRenderTimer > 0) {
                    event.renderer.box((BlockPos)this.placeRenderPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
                }
                if (!this.renderBreak.get().booleanValue() || this.breakRenderTimer <= 0) break;
                event.renderer.box((BlockPos)this.breakRenderPos, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
                break;
            }
            case 1: {
                if (this.placeRenderTimer <= 0) {
                    return;
                }
                if (this.renderBoxOne == null) {
                    this.renderBoxOne = new AABB((BlockPos)this.placeRenderPos);
                }
                if (this.renderBoxTwo == null) {
                    this.renderBoxTwo = new AABB((BlockPos)this.placeRenderPos);
                } else {
                    ((IAABB)this.renderBoxTwo).meteor$set((BlockPos)this.placeRenderPos);
                }
                double offsetX = (this.renderBoxTwo.minX - this.renderBoxOne.minX) / (double)this.smoothness.get().intValue();
                double offsetY = (this.renderBoxTwo.minY - this.renderBoxOne.minY) / (double)this.smoothness.get().intValue();
                double offsetZ = (this.renderBoxTwo.minZ - this.renderBoxOne.minZ) / (double)this.smoothness.get().intValue();
                ((IAABB)this.renderBoxOne).meteor$set(this.renderBoxOne.minX + offsetX, this.renderBoxOne.minY + offsetY, this.renderBoxOne.minZ + offsetZ, this.renderBoxOne.maxX + offsetX, this.renderBoxOne.maxY + offsetY, this.renderBoxOne.maxZ + offsetZ);
                event.renderer.box(this.renderBoxOne, (Color)this.sideColor.get(), (Color)this.lineColor.get(), this.shapeMode.get(), 0);
                break;
            }
            case 3: {
                if (this.placeRenderTimer <= 0) {
                    return;
                }
                Color bottom = new Color(0, 0, 0, 0);
                int x = this.placeRenderPos.getX();
                int y = this.placeRenderPos.getY() + 1;
                int z = this.placeRenderPos.getZ();
                if (this.shapeMode.get().sides()) {
                    event.renderer.quadHorizontal(x, y, z, x + 1, z + 1, this.sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x + 1, (double)y - this.height.get(), z, bottom, this.sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z, x, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                    event.renderer.gradientQuadVertical(x + 1, y, z, x + 1, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                    event.renderer.gradientQuadVertical(x, y, z + 1, x + 1, (double)y - this.height.get(), z + 1, bottom, this.sideColor.get());
                }
                if (!this.shapeMode.get().lines()) break;
                event.renderer.line(x, y, z, x + 1, y, z, this.lineColor.get());
                event.renderer.line(x, y, z, x, y, z + 1, this.lineColor.get());
                event.renderer.line(x + 1, y, z, x + 1, y, z + 1, this.lineColor.get());
                event.renderer.line(x, y, z + 1, x + 1, y, z + 1, this.lineColor.get());
                event.renderer.line(x, y, z, x, (double)y - this.height.get(), z, this.lineColor.get(), bottom);
                event.renderer.line(x + 1, y, z, x + 1, (double)y - this.height.get(), z, this.lineColor.get(), bottom);
                event.renderer.line(x, y, z + 1, x, (double)y - this.height.get(), z + 1, this.lineColor.get(), bottom);
                event.renderer.line(x + 1, y, z + 1, x + 1, (double)y - this.height.get(), z + 1, this.lineColor.get(), bottom);
            }
        }
    }

    @EventHandler
    private void onRender2D(Render2DEvent event) {
        if (this.renderMode.get() == RenderMode.None || !this.renderDamageText.get().booleanValue()) {
            return;
        }
        if (this.placeRenderTimer <= 0 && this.breakRenderTimer <= 0) {
            return;
        }
        if (this.renderMode.get() == RenderMode.Smooth) {
            if (this.renderBoxOne == null) {
                return;
            }
            this.vec3.set(this.renderBoxOne.minX + 0.5, this.renderBoxOne.minY + 0.5, this.renderBoxOne.minZ + 0.5);
        } else {
            this.vec3.set((double)this.placeRenderPos.getX() + 0.5, (double)this.placeRenderPos.getY() + 0.5, (double)this.placeRenderPos.getZ() + 0.5);
        }
        if (NametagUtils.to2D(this.vec3, this.damageTextScale.get())) {
            NametagUtils.begin(this.vec3);
            TextRenderer.get().begin(1.0, false, true);
            String text = String.format("%.1f", this.renderDamage);
            double w = TextRenderer.get().getWidth(text) / 2.0;
            TextRenderer.get().render(text, -w, 0.0, this.damageColor.get(), true);
            TextRenderer.get().end();
            NametagUtils.end();
        }
    }

    public static enum YawStepMode {
        Break,
        All;

    }

    public static enum AutoSwitchMode {
        Normal,
        Silent,
        None;

    }

    public static enum SupportMode {
        Disabled,
        Accurate,
        Fast;

    }

    public static enum PauseMode {
        Both,
        Place,
        Break,
        None;


        public boolean matches(PauseMode process) {
            return this == process || this == Both;
        }
    }

    public static enum SwingMode {
        Both,
        Packet,
        Client,
        None;


        public boolean packet() {
            return this == Packet || this == Both;
        }

        public boolean client() {
            return this == Client || this == Both;
        }
    }

    public static enum RenderMode {
        Normal,
        Smooth,
        Fading,
        Gradient,
        None;

    }
}
