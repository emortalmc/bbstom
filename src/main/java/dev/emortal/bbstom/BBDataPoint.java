package dev.emortal.bbstom;

import com.alibaba.fastjson2.annotation.JSONCreator;
import dev.omega.arcane.ast.ObjectAwareExpression;
import dev.omega.arcane.exception.MolangLexException;
import dev.omega.arcane.exception.MolangParseException;
import dev.omega.arcane.parser.MolangParser;
import dev.omega.arcane.reference.ExpressionBindingContext;
import dev.omega.arcane.reference.ReferenceType;
import net.minestom.server.MinecraftServer;
import org.joml.Vector3f;

@SuppressWarnings("unused")
public record BBDataPoint(Vector3f vector, String xExp, String yExp, String zExp) {
    private static final ExpressionBindingContext ANIM_TIME_CONTEXT = ExpressionBindingContext.create();

    static {
        ANIM_TIME_CONTEXT.registerReferenceResolver(
                ReferenceType.QUERY, // query.anim_time
                "anim_time",
                Float.class,
                r -> new ObjectAwareExpression<>(r) {
                    @Override
                    public float evaluate() {
                        return r;
                    }
                }
        );
    }

    @JSONCreator
    public BBDataPoint(String x, String y, String z) {
        Vector3f vec = new Vector3f();

        String xExp = "";
        String yExp = "";
        String zExp = "";

        try {
            vec.setComponent(0, Float.parseFloat(x));
        } catch (NumberFormatException e) {
            xExp = x.replaceAll("\n", "");
        }
        try {
            vec.setComponent(1, Float.parseFloat(y));
        } catch (NumberFormatException e) {
            yExp = y.replaceAll("\n", "");
        }
        try {
            vec.setComponent(2, Float.parseFloat(z));
        } catch (NumberFormatException e) {
            zExp = z.replaceAll("\n", "");
        }

        this(vec, xExp, yExp, zExp);
    }

    public Vector3f vector(float time) {
        Vector3f vec = new Vector3f(vector);

        try {
            if (!xExp.isEmpty()) {
                float evaluate = MolangParser.parse(xExp).bind(ANIM_TIME_CONTEXT, time).evaluate();
                vec.setComponent(0, evaluate);
            }
            if (!yExp.isEmpty()) {
                float evaluate = MolangParser.parse(yExp).bind(ANIM_TIME_CONTEXT, time).evaluate();
                vec.setComponent(1, evaluate);
            }
            if (!zExp.isEmpty()) {
                float evaluate = MolangParser.parse(zExp).bind(ANIM_TIME_CONTEXT, time).evaluate();
                vec.setComponent(2, evaluate);
            }
        } catch (MolangLexException | MolangParseException e) {
            MinecraftServer.getExceptionManager().handleException(e);
        }

        return vec;
    }
}
