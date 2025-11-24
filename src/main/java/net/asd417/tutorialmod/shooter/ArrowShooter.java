package net.asd417.tutorialmod.shooter;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredRegister;

//https://www.youtube.com/watch?v=bk8dIjjCeFM

public class ArrowShooter {
    public static void spawnFlyingArrow(Level level, Vec3 from, Vec3 dir) {
        ArrowShooter.spawnFlyingArrow(level,from,dir,1.0f,0.0f);
    }
    public static void spawnFlyingArrow(Level level, Vec3 from, Vec3 dir, float power,float inaccuracy) {
        if (level.isClientSide) return; // only spawn on server
        Arrow arrow = new Arrow(level,from.x,from.y,from.z,new ItemStack(Items.ARROW, 1),null);
        arrow.shoot(dir.x, dir.y, dir.z, power, inaccuracy);
        arrow.pickup = Arrow.Pickup.DISALLOWED;
        level.addFreshEntity(arrow);
    }
    public static void spawnFlyingArrowToTarget(Level level, Vec3 from, Vec3 to, float power, float inaccuracy) {
        Vec3 d = to.subtract(from);
        double dx = Math.sqrt(d.x*d.x + d.z*d.z);
        double dy = d.y;
        double angle = GetFireAngleArrow(power,dx,dy,true);
        System.out.printf("Solved Angle: %f\n", angle);
        double ny = Math.tan(angle) * dx;
        Vec3 direction = new Vec3(d.x,ny,d.z);
        spawnFlyingArrow(level, from, direction, power, inaccuracy);
    }

    //projectile motion equation
    private static double phi(double v, double targetx, double targety, double theta, double drag, double g){
        //drag for arrow = 0.01
        //gravity for arrow = -0.05
        double vt = g/drag; //terminal velocity
        double cos = Math.cos(theta);
        double exp = -(targetx*drag*Math.tan(theta)/vt - targety*drag/vt - targetx*drag/(v*cos));
        return 1 - (1 - targetx*drag/(v*cos))*Math.exp(exp);
    }
    private static double dphi(double v, double targetx, double targety, double theta, double drag, double g){
        double cos = Math.cos(theta);
        double tan = Math.tan(theta);
        double exp = targetx * drag / (v*cos) - targetx * drag * drag * tan / g + targety * drag * drag / g;
        return (targetx*drag*drag / (cos*cos))*(targetx*tan / (v*v) + 1/g - targetx*drag/(g*v*cos)) * Math.exp(exp);
    }
    private static double Newtonian(double prev, double v, double targetx, double targety, double theta, double drag, double g){
        return prev - phi(v, targetx, targety, theta, drag, g) / dphi(v, targetx, targety, theta, drag, g);
    }
    //returns radian
    public static double GetFireAngle(double v, double targetx, double targety, double drag, double g, boolean low){
        double angle = low ? 0 : 89.99f;
        for(int i = 0; i < 4;i++){
            angle = Newtonian(angle, v, targetx, targety, angle, drag, g);
        }
        return angle;
    }
    public static double GetFireAngleArrow(double v, double targetx, double targety, boolean low){
        return GetFireAngle(v,targetx,targety,0.01,-0.05, low);
    }
}
