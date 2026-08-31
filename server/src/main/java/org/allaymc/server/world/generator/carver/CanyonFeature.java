package org.allaymc.server.world.generator.carver;

import org.allaymc.api.world.chunk.UnsafeChunk;

import java.util.Random;

import static org.allaymc.server.world.generator.OverworldNoiser.DEEPSLATE_LEVEL_INTERNAL;
import static org.allaymc.server.world.generator.OverworldNoiser.GEN_DEPTH;

public final class CanyonFeature extends LargeFeature {
    private final float[] rs = new float[GEN_DEPTH];

    @Override
    public void addFeature(int startChunkX, int startChunkZ, int targetChunkX, int targetChunkZ, UnsafeChunk chunk) {
        if (random.nextInt(50) != 0) {
            return;
        }

        double xCave = startChunkX * 16.0 + random.nextInt(16);
        double yCave = DEEPSLATE_LEVEL_INTERNAL + random.nextInt(random.nextInt(40) + 8) + 20;
        double zCave = startChunkZ * 16.0 + random.nextInt(16);
        float yRot = random.nextFloat() * (float) Math.PI * 2.0f;
        float xRot = ((random.nextFloat() - 0.5f) * 2.0f) / 8.0f;
        float thickness = (random.nextFloat() * 2.0f + random.nextFloat()) * 2.0f;
        addTunnel(
                random.nextLong(),
                targetChunkX,
                targetChunkZ,
                chunk,
                xCave,
                yCave,
                zCave,
                thickness,
                yRot,
                xRot,
                0,
                0,
                3.0);
    }

    private void addTunnel(
            long tunnelSeed,
            int chunkX,
            int chunkZ,
            UnsafeChunk chunk,
            double xCave,
            double yCave,
            double zCave,
            float thickness,
            float yRot,
            float xRot,
            int step,
            int dist,
            double yScale) {
        Random tunnelRandom = new Random(tunnelSeed);
        double xMid = chunkX * 16.0 + 8.0;
        double zMid = chunkZ * 16.0 + 8.0;
        float yRota = 0.0f;
        float xRota = 0.0f;

        if (dist <= 0) {
            int max = caveRadius * 16 - 16;
            dist = max - tunnelRandom.nextInt(max / 4);
        }

        boolean singleStep = false;
        if (step == -1) {
            step = dist / 2;
            singleStep = true;
        }

        float f = 1.0f;
        for (int i = 0; i < GEN_DEPTH; i++) {
            if (i == 0 || tunnelRandom.nextInt(3) == 0) {
                f = 1.0f + (tunnelRandom.nextFloat() * tunnelRandom.nextFloat());
            }
            rs[i] = f * f;
        }

        for (; step < dist; step++) {
            double rad = 1.5 + Math.sin(step * Math.PI / dist) * thickness;
            double yRad = rad * yScale;

            rad *= tunnelRandom.nextFloat() * 0.25 + 0.75;
            yRad *= tunnelRandom.nextFloat() * 0.25 + 0.75;

            float xc = (float) Math.cos(xRot);
            float xs = (float) Math.sin(xRot);
            xCave += Math.cos(yRot) * xc;
            yCave += xs;
            zCave += Math.sin(yRot) * xc;

            xRot *= 0.7f;
            xRot += xRota * 0.05f;
            yRot += yRota * 0.05f;
            xRota *= 0.80f;
            yRota *= 0.50f;
            xRota += (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 2.0f;
            yRota += (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 4.0f;

            if (!singleStep && tunnelRandom.nextInt(4) == 0) {
                continue;
            }

            double xd = xCave - xMid;
            double zd = zCave - zMid;
            double remaining = dist - step;
            double rr = thickness + 18.0;
            if (xd * xd + zd * zd - remaining * remaining > rr * rr) {
                return;
            }

            if (xCave < xMid - 16.0 - rad * 2.0
                    || zCave < zMid - 16.0 - rad * 2.0
                    || xCave > xMid + 16.0 + rad * 2.0
                    || zCave > zMid + 16.0 + rad * 2.0) {
                continue;
            }

            int x0 = Math.max(0, floor(xCave - rad) - chunkX * 16 - 1);
            int x1 = Math.min(16, floor(xCave + rad) - chunkX * 16 + 1);
            int y0 = Math.max(DEEPSLATE_LEVEL_INTERNAL, floor(yCave - yRad) - 1);
            int y1 = Math.min(GEN_DEPTH - 8, floor(yCave + yRad) + 1);
            int z0 = Math.max(0, floor(zCave - rad) - chunkZ * 16 - 1);
            int z1 = Math.min(16, floor(zCave + rad) - chunkZ * 16 + 1);

            if (isWater(chunk, x0, x1, y0, y1, z0, z1)) {
                continue;
            }

            carveEllipsoid(chunk, chunkX, chunkZ, xCave, yCave, zCave, rad, yRad, x0, x1, y0, y1, z0, z1, true, rs);
            if (singleStep) {
                break;
            }
        }
    }
}
