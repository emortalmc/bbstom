package dev.emortal.bbstom;

import com.alibaba.fastjson2.annotation.JSONCreator;
import net.hollowcube.molang.MolangExpr;
import net.hollowcube.molang.eval.MolangEvaluator;
import net.minestom.server.MinecraftServer;
import org.joml.Vector3f;

import java.util.Map;

@SuppressWarnings("unused")
public record BBDataPoint(Vector3f vector, String xExp, String yExp, String zExp) {

    @JSONCreator
    public BBDataPoint(String x, String y, String z) {
        Vector3f vec = new Vector3f();

        String xExp = "";
        String yExp = "";
        String zExp = "";

        try {
            vec.setComponent(0, Float.parseFloat(x));
        } catch (NumberFormatException e) {
            xExp = x.replace("\n", "");
        }
        try {
            vec.setComponent(1, Float.parseFloat(y));
        } catch (NumberFormatException e) {
            yExp = y.replace("\n", "");
        }
        try {
            vec.setComponent(2, Float.parseFloat(z));
        } catch (NumberFormatException e) {
            zExp = z.replace("\n", "");
        }

        this(vec, xExp, yExp, zExp);
    }

    public Vector3f vector(float time) {
        Vector3f vec = new Vector3f(vector);

        try {
            MolangEvaluator molangEvaluator = new MolangEvaluator(Map.of());
            if (!xExp.isEmpty()) {
                MolangExpr molangExpr = MolangExpr.parseOrThrow(xExp);
                vec.x = (float) molangEvaluator.eval(molangExpr);
            }
            if (!yExp.isEmpty()) {
                MolangExpr molangExpr = MolangExpr.parseOrThrow(yExp);
                vec.y = (float) molangEvaluator.eval(molangExpr);
            }
            if (!zExp.isEmpty()) {
                MolangExpr molangExpr = MolangExpr.parseOrThrow(zExp);
                vec.z = (float) molangEvaluator.eval(molangExpr);
            }
        } catch (Exception e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return vec;
    }
}
