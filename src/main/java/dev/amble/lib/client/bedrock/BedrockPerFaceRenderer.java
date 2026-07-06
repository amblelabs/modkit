package dev.amble.lib.client.bedrock;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BedrockPerFaceRenderer {
    private BedrockPerFaceRenderer() {}

    /**
     * Renders deferred per-face cubes with:
     * - recursive subgroup lookup fix
     * - correct transform order
     * - single unit conversion (/16) at root only
     */
    public static void render(ModelPart root,
                              BedrockModel model,
                              Map<String, ModelPart> partsByName,
                              MatrixStack matrices,
                              VertexConsumer vertices,
                              int light, int overlay,
                              float red, float green, float blue, float alpha,
                              int textureWidth, int textureHeight) {

        List<BedrockModel.PerFaceCube> deferred = model.deferredPerFaceCubes();
        if (deferred.isEmpty()) return;

        // Group fix: ensure nested children are indexed too.
        // (non-destructive; keeps existing entries)
        indexChildrenRecursive(root, partsByName);

        matrices.push();
        // One global conversion from model units -> MC model units
        matrices.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);

        for (BedrockModel.PerFaceCube cube : deferred) {
            ModelPart bonePart = partsByName.get(cube.boneName());
            if (bonePart == null) continue;

            matrices.push();

            float px = cube.cubePivot().get(0);
            float py = cube.cubePivot().get(1);
            float pz = cube.cubePivot().get(2);

            float rx = cube.cubeRotation().get(0);
            float ry = cube.cubeRotation().get(1);
            float rz = cube.cubeRotation().get(2);

            matrices.scale(
                    cube.cubeScale().get(0),
                    cube.cubeScale().get(1),
                    cube.cubeScale().get(2)
            );

            matrices.translate(px, -py, pz);

            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(rx));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(ry));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(rz));

            MatrixStack.Entry entry = matrices.peek();
            for (BedrockPerFaceQuad q : buildQuads(cube, textureWidth, textureHeight)) {
                q.render(entry, vertices, light, overlay, red, green, blue, alpha);
            }

            matrices.pop();
        }

        matrices.pop();
    }

    @SuppressWarnings("unchecked")
    private static void indexChildrenRecursive(ModelPart part, Map<String, ModelPart> out) {
        try {
            Field childrenField = ModelPart.class.getDeclaredField("children");
            childrenField.setAccessible(true);
            Map<String, ModelPart> children = (Map<String, ModelPart>) childrenField.get(part);
            if (children == null || children.isEmpty()) return;

            for (Map.Entry<String, ModelPart> e : children.entrySet()) {
                out.putIfAbsent(e.getKey(), e.getValue());
                indexChildrenRecursive(e.getValue(), out);
            }
        } catch (Throwable ignored) {
            // If field name changes in mappings, we fail soft and keep existing map.
        }
    }

    public static List<BedrockPerFaceQuad> buildQuads(BedrockModel.PerFaceCube cube, int texW, int texH) {
        List<BedrockPerFaceQuad> quads = new ArrayList<>();

        float x0 = cube.x() - cube.inflate();
        float y0 = cube.y() - cube.inflate();
        float z0 = cube.z() - cube.inflate();
        float x1 = cube.x() + cube.sizeX() + cube.inflate();
        float y1 = cube.y() + cube.sizeY() + cube.inflate();
        float z1 = cube.z() + cube.sizeZ() + cube.inflate();

        emit(quads, cube.uv().north(), "north", cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);
        emit(quads, cube.uv().south(), "south", cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);
        emit(quads, cube.uv().east(),  "west",  cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);
        emit(quads, cube.uv().west(),  "east",  cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);
        emit(quads, cube.uv().up(),    "up",    cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);
        emit(quads, cube.uv().down(),  "down",  cube.mirror(), x0, y0, z0, x1, y1, z1, texW, texH);

        return quads;
    }

    private static void emit(List<BedrockPerFaceQuad> out, BedrockModel.Face face, String dir, boolean mirror,
                             float x0, float y0, float z0, float x1, float y1, float z1,
                             int texW, int texH) {
        if (face == null || face.uv() == null || face.uv().size() < 2) return;

        float u = face.uv().get(0);
        float v = face.uv().get(1);

        float w = (face.uvSize() != null && face.uvSize().size() >= 2) ? face.uvSize().get(0) : 1f;
        float h = (face.uvSize() != null && face.uvSize().size() >= 2) ? face.uvSize().get(1) : 1f;

        float u0 = u / texW;
        float v0 = v / texH;
        float u1 = (u + w) / texW;
        float v1 = (v + h) / texH;

        if (mirror) {
            float t = u0; u0 = u1; u1 = t;
        }

        switch (dir) {
            case "north" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x1, y0, z0), new Vector3f(x0, y0, z0), new Vector3f(x0, y1, z0), new Vector3f(x1, y1, z0),
                    // rotate/flip for front plane alignment
                    u1, v0, u0, v1,
                    new Vector3f(0, 0, -1)
            ));
            case "south" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x0, y0, z1), new Vector3f(x1, y0, z1), new Vector3f(x1, y1, z1), new Vector3f(x0, y1, z1),
                    // keep opposite side consistent
                    u1, v0, u0, v1,
                    new Vector3f(0, 0, 1)
            ));
            case "east" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x1, y0, z1), new Vector3f(x1, y0, z0), new Vector3f(x1, y1, z0), new Vector3f(x1, y1, z1),
                    u1, v0, u0, v1, new Vector3f(1, 0, 0)
            ));
            case "west" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x0, y0, z0), new Vector3f(x0, y0, z1), new Vector3f(x0, y1, z1), new Vector3f(x0, y1, z0),
                    u1, v0, u0, v1, new Vector3f(-1, 0, 0)
            ));
            case "up" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x0, y0, z0),
                    new Vector3f(x1, y0, z0),
                    new Vector3f(x1, y0, z1),
                    new Vector3f(x0, y0, z1),
                    u0, v1, u1, v0,
                    new Vector3f(0, -1, 0)
            ));
            case "down" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x0, y1, z1), new Vector3f(x1, y1, z1), new Vector3f(x1, y1, z0), new Vector3f(x0, y1, z0),
                    u0, v1, u1, v0, new Vector3f(0, 1, 0)
            ));
        }
    }
}