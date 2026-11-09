package net.luojiuoscar.isaac_disaster.client.model.familiar.depression;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DepressionMeshTest {
    @Test
    void meshPreservesSmoothNormalsUvsAndAuthoredBlockScale() {
        assertEquals(136, DepressionMesh.QUADS.length);
        double[] min = {Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE};
        double[] max = {-Double.MAX_VALUE, -Double.MAX_VALUE, -Double.MAX_VALUE};
        boolean smooth = false;
        for (float[] face : DepressionMesh.QUADS) {
            assertEquals(32, face.length);
            for (int v = 0; v < 4; v++) {
                int offset = v * 8;
                for (float value : face) assertTrue(Float.isFinite(value));
                for (int axis = 0; axis < 3; axis++) {
                    min[axis] = Math.min(min[axis], face[offset + axis]);
                    max[axis] = Math.max(max[axis], face[offset + axis]);
                    if (v > 0 && face[offset + 5 + axis] != face[5 + axis]) smooth = true;
                }
                assertTrue(face[offset + 3] >= 0 && face[offset + 3] <= 1);
                assertTrue(face[offset + 4] >= 0 && face[offset + 4] <= 1);
                double normalLength = 0;
                for (int axis = 5; axis < 8; axis++) normalLength += face[offset + axis] * face[offset + axis];
                assertEquals(1, normalLength, 1e-5);
            }
        }
        assertTrue(smooth, "Authored smooth normals must not collapse to one normal per face");
        assertEquals(0.708668125, max[0] - min[0], 1e-7);
        assertEquals(0.5125, max[1] - min[1], 1e-7);
        for (int axis = 0; axis < 3; axis++) assertEquals(0, min[axis] + max[axis], 1e-7);
    }
}
