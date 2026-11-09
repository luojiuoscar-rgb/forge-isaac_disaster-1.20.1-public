package net.luojiuoscar.isaac_disaster.registries.bullet_visual;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UranusIceTearTextureTest {
    @Test
    void everyExteriorBackgroundPixelIsTransparent() throws Exception {
        BufferedImage image = texture("uranus_ice_tear");
        int width = image.getWidth(), height = image.getHeight();
        var pending = new ArrayDeque<Integer>();
        Set<Integer> visited = new HashSet<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (x == 0 || y == 0 || x == width - 1 || y == height - 1) pending.add(y * width + x);
            }
        }
        while (!pending.isEmpty()) {
            int position = pending.removeFirst();
            if (!visited.add(position)) continue;
            int x = position % width, y = position / width;
            int pixel = image.getRGB(x, y);
            if (pixel >>> 24 != 0 && pixel != 0xFFFFFFFF) continue;
            assertEquals(0, pixel >>> 24, "Opaque white outside the outline at " + x + "," + y);
            if (x > 0) pending.add(position - 1);
            if (x + 1 < width) pending.add(position + 1);
            if (y > 0) pending.add(position - width);
            if (y + 1 < height) pending.add(position + width);
        }
    }

    @Test
    void internalHighlightsRemainOpaqueAndTheSpriteRemainsLargeEnough() throws Exception {
        BufferedImage image = texture("uranus_ice_tear");
        assertEquals(32, image.getWidth());
        assertEquals(32, image.getHeight());
        int white = 0, opaque = 0;
        int minX = 32, minY = 32, maxX = -1, maxY = -1;
        for (int y = 0; y < 32; y++) {
            for (int x = 0; x < 32; x++) {
                int pixel = image.getRGB(x, y);
                if (pixel >>> 24 == 0) continue;
                assertEquals(255, pixel >>> 24, "Pixel-art edges remain crisp");
                if (pixel == 0xFFFFFFFF) white++;
                opaque++;
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals(103, white, "Only the enclosed highlights remain opaque white");
        BufferedImage tear = texture("tear_bullet");
        int tearPixels = 0;
        for (int y = 0; y < tear.getHeight(); y++) {
            for (int x = 0; x < tear.getWidth(); x++) if (tear.getRGB(x, y) >>> 24 != 0) tearPixels++;
        }
        assertTrue(opaque >= tearPixels, "The ice sprite still covers at least as many pixels as a normal tear");
        assertTrue(maxX - minX + 1 >= 30 && maxY - minY + 1 >= 30, "Keep the approved enlarged silhouette");
    }

    private static BufferedImage texture(String name) throws Exception {
        try (var input = UranusIceTearTextureTest.class.getResourceAsStream(
                "/assets/isaac_disaster/textures/particle/" + name + ".png")) {
            assertNotNull(input, "Runtime tear texture must be packaged");
            return ImageIO.read(input);
        }
    }
}
