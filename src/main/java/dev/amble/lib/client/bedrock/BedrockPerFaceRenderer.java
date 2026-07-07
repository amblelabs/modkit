package dev.amble.lib.client.bedrock;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.RotationAxis;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public final class BedrockPerFaceRenderer {

    public static void render(ModelPart root,
                              List<BedrockModel.PerFaceCube> deferred,
                              MatrixStack matrices,
                              VertexConsumer vertices,
                              int light, int overlay,
                              float red, float green, float blue, float alpha,
                              int textureWidth, int textureHeight) {

        matrices.push();
        matrices.scale(1.0F / 16.0F, 1.0F / 16.0F, 1.0F / 16.0F);

        for (BedrockModel.PerFaceCube cube : deferred) {
            ModelPart bonePart = BedrockAnimation.getBoneMap(root).get(cube.partName());
            if (bonePart == null) continue;

            matrices.push();

            float px = cube.cubePivot().get(0);
            float py = cube.cubePivot().get(1);
            float pz = cube.cubePivot().get(2);

            float rx = cube.cubeRotation().get(0);
            float ry = cube.cubeRotation().get(1);
            float rz = cube.cubeRotation().get(2);

            matrices.scale(bonePart.xScale, bonePart.yScale, bonePart.zScale);
            matrices.translate(px, -py, pz);

            matrices.multiply(RotationAxis.POSITIVE_X.rotation(bonePart.pitch == 0 ? (float) Math.toRadians(rx) : bonePart.pitch));
            matrices.multiply(RotationAxis.POSITIVE_Y.rotation(bonePart.yaw == 0 ? (float) Math.toRadians(ry) : bonePart.yaw));
            matrices.multiply(RotationAxis.POSITIVE_Z.rotation(bonePart.roll == 0 ? (float) Math.toRadians(rz) : bonePart.roll));

            MatrixStack.Entry entry = matrices.peek();
            for (BedrockPerFaceQuad q : buildQuads(cube, textureWidth, textureHeight)) {
                q.render(entry, vertices, light, overlay, red, green, blue, alpha);
            }

            matrices.pop();
        }

        matrices.pop();
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
                    u1, v0, u0, v1,
                    new Vector3f(0, 0, -1)
            ));
            case "south" -> out.add(new BedrockPerFaceQuad(
                    new Vector3f(x0, y0, z1), new Vector3f(x1, y0, z1), new Vector3f(x1, y1, z1), new Vector3f(x0, y1, z1),
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
                    new Vector3f(x0, y0, z0), new Vector3f(x1, y0, z0), new Vector3f(x1, y0, z1), new Vector3f(x0, y0, z1),
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