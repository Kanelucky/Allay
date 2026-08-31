package org.allaymc.server.world.generator.noise;

import java.util.Random;

/**
 * @author zernix2077
 */
public final class PerlinNoise {
    private final ImprovedNoise[] noiseLevels;

    public PerlinNoise(Random random, int levels) {
        noiseLevels = new ImprovedNoise[levels];
        for (int i = 0; i < levels; i++) {
            noiseLevels[i] = new ImprovedNoise(random);
        }
    }

    public double[] getRegion(
            double[] buffer,
            int x,
            int y,
            int z,
            int xSize,
            int ySize,
            int zSize,
            double xScale,
            double yScale,
            double zScale) {
        if (buffer == null || buffer.length < xSize * ySize * zSize) {
            buffer = new double[xSize * ySize * zSize];
        } else {
            java.util.Arrays.fill(buffer, 0.0);
        }

        double pow = 1.0;
        for (ImprovedNoise noiseLevel : noiseLevels) {
            double xx = x * pow * xScale;
            double yy = y * pow * yScale;
            double zz = z * pow * zScale;

            long xb = lfloor(xx);
            long zb = lfloor(zz);
            xx -= xb;
            zz -= zb;
            xb %= 16777216L;
            zb %= 16777216L;
            xx += xb;
            zz += zb;

            noiseLevel.add(buffer, xx, yy, zz, xSize, ySize, zSize, xScale * pow, yScale * pow, zScale * pow, pow);
            pow /= 2.0;
        }

        return buffer;
    }

    public double[] getRegion(
            double[] buffer, int x, int z, int xSize, int zSize, double xScale, double zScale, double ignoredPow) {
        return getRegion(buffer, x, 10, z, xSize, 1, zSize, xScale, 1.0, zScale);
    }

    private static long lfloor(double value) {
        long floor = (long) value;
        return value < floor ? floor - 1L : floor;
    }
}
