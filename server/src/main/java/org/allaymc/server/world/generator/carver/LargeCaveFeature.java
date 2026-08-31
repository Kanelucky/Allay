package org.allaymc.server.world.generator.carver;

import org.allaymc.api.world.chunk.UnsafeChunk;

import java.util.Random;

import static org.allaymc.server.world.generator.OverworldNoiser.GEN_DEPTH;

public final class LargeCaveFeature extends LargeFeature {
    @Override
    protected void addFeature(int startChunkX, int startChunkZ, int targetChunkX, int targetChunkZ, UnsafeChunk chunk) {
        int caves = random.nextInt(random.nextInt(random.nextInt(40) + 1) + 1);
        if (random.nextInt(15) != 0) {
            caves = 0;
        }

        for (int cave = 0; cave < caves; cave++) {
            double xCave = startChunkX * 16.0 + random.nextInt(16);
            double yCave = random.nextInt(random.nextInt(GEN_DEPTH - 8) + 8);
            double zCave = startChunkZ * 16.0 + random.nextInt(16);

            int tunnels = 1;
            if (random.nextInt(4) == 0) {
                addCaveRoom(random.nextLong(), targetChunkX, targetChunkZ, chunk, xCave, yCave, zCave);
                tunnels += random.nextInt(4);
            }

            for (int i = 0; i < tunnels; i++) {
                float yRot = random.nextFloat() * (float) Math.PI * 2.0f;
                float xRot = ((random.nextFloat() - 0.5f) * 2.0f) / 8.0f;
                float thickness = random.nextFloat() * 2.0f + random.nextFloat();
                if (random.nextInt(10) == 0) {
                    thickness *= random.nextFloat() * random.nextFloat() * 3.0f + 1.0f;
                }
                addCaveTunnel(
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
                        1.0);
            }
        }
    }

    private void addCaveRoom(
            long seed, int chunkX, int chunkZ, UnsafeChunk chunk, double xRoom, double yRoom, double zRoom) {
        addCaveTunnel(
                seed,
                chunkX,
                chunkZ,
                chunk,
                xRoom,
                yRoom,
                zRoom,
                1.0f + random.nextFloat() * 6.0f,
                0.0f,
                0.0f,
                -1,
                -1,
                0.5);
    }

    private void addCaveTunnel(
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
        double xMid = chunkX * 16.0 + 8.0;
        double zMid = chunkZ * 16.0 + 8.0;

        float yRota = 0.0f;
        float xRota = 0.0f;
        Random tunnelRandom = new Random(tunnelSeed);

        if (dist <= 0) {
            int max = caveRadius * 16 - 16;
            dist = max - tunnelRandom.nextInt(max / 4);
        }

        boolean singleStep = false;
        if (step == -1) {
            step = dist / 2;
            singleStep = true;
        }

        int splitPoint = tunnelRandom.nextInt(dist / 2) + dist / 4;
        boolean steep = tunnelRandom.nextInt(6) == 0;

        for (; step < dist; step++) {
            double rad = 1.5 + Math.sin(step * Math.PI / dist) * thickness;
            double yRad = rad * yScale;

            float xc = (float) Math.cos(xRot);
            float xs = (float) Math.sin(xRot);
            xCave += Math.cos(yRot) * xc;
            yCave += xs;
            zCave += Math.sin(yRot) * xc;

            xRot *= steep ? 0.92f : 0.7f;
            xRot += xRota * 0.1f;
            yRot += yRota * 0.1f;
            xRota *= 0.90f;
            yRota *= 0.75f;
            xRota += (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 2.0f;
            yRota += (tunnelRandom.nextFloat() - tunnelRandom.nextFloat()) * tunnelRandom.nextFloat() * 4.0f;

            if (!singleStep && step == splitPoint && thickness > 1.0f && dist > 0) {
                addCaveTunnel(
                        tunnelRandom.nextLong(),
                        chunkX,
                        chunkZ,
                        chunk,
                        xCave,
                        yCave,
                        zCave,
                        tunnelRandom.nextFloat() * 0.5f + 0.5f,
                        yRot - (float) Math.PI / 2.0f,
                        xRot / 3.0f,
                        step,
                        dist,
                        1.0);
                addCaveTunnel(
                        tunnelRandom.nextLong(),
                        chunkX,
                        chunkZ,
                        chunk,
                        xCave,
                        yCave,
                        zCave,
                        tunnelRandom.nextFloat() * 0.5f + 0.5f,
                        yRot + (float) Math.PI / 2.0f,
                        xRot / 3.0f,
                        step,
                        dist,
                        1.0);
                return;
            }

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
            int y0 = Math.max(1, floor(yCave - yRad) - 1);
            int y1 = Math.min(GEN_DEPTH - 8, floor(yCave + yRad) + 1);
            int z0 = Math.max(0, floor(zCave - rad) - chunkZ * 16 - 1);
            int z1 = Math.min(16, floor(zCave + rad) - chunkZ * 16 + 1);

            if (isWater(chunk, x0, x1, y0, y1, z0, z1)) {
                continue;
            }

            carveEllipsoid(chunk, chunkX, chunkZ, xCave, yCave, zCave, rad, yRad, x0, x1, y0, y1, z0, z1, false, null);
            if (singleStep) {
                break;
            }
        }
    }
}
