package org.allaymc.server.world.generator.carver;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.world.chunk.UnsafeChunk;

import java.util.Random;

import static org.allaymc.server.world.generator.OverworldNoiser.GEN_DEPTH;

abstract class LargeFeature {
    protected final int caveRadius = 8;
    protected final Random random = new Random(0L);
    protected long seed;

    public synchronized void apply(long seed, int chunkX, int chunkZ, UnsafeChunk chunk) {
        this.seed = seed;
        random.setSeed(seed);
        long xScale = random.nextLong();
        long zScale = random.nextLong();
        int count = 0;
        for (int x = chunkX - caveRadius; x <= chunkX + caveRadius; x++) {
            for (int z = chunkZ - caveRadius; z <= chunkZ + caveRadius; z++) {
                random.setSeed((x * xScale) ^ (z * zScale) ^ seed);
                addFeature(x, z, chunkX, chunkZ, chunk);
                count++;
            }
        }
        if (count != 289) {
            System.out.println("[CaveDebug] chunk (" + chunkX + "," + chunkZ + ") only looped " + count + " times!");
        }
    }

    protected abstract void addFeature(
            int startChunkX, int startChunkZ, int targetChunkX, int targetChunkZ, UnsafeChunk chunk);

    protected static int floor(double value) {
        int floor = (int) value;
        return value < floor ? floor - 1 : floor;
    }

    protected boolean isWater(UnsafeChunk chunk, int x0, int x1, int y0, int y1, int z0, int z1) {
        for (int xx = x0; xx < x1; xx++) {
            for (int zz = z0; zz < z1; zz++) {
                for (int yy = y1 + 1; yy >= y0 - 1; yy--) {
                    if (yy < 0 || yy >= GEN_DEPTH) {
                        continue;
                    }
                    BlockState block = chunk.getBlockState(xx, yy, zz);
                    if (block.getBlockType() == BlockTypes.WATER) {
                        return true;
                    }
                    if (yy != y0 - 1 && xx != x0 && xx != x1 - 1 && zz != z0 && zz != z1 - 1) {
                        yy = y0;
                    }
                }
            }
        }
        return false;
    }

    protected void carveEllipsoid(
            UnsafeChunk chunk,
            int chunkX,
            int chunkZ,
            double xCave,
            double yCave,
            double zCave,
            double rad,
            double yRad,
            int x0,
            int x1,
            int y0,
            int y1,
            int z0,
            int z1,
            boolean canyon,
            float[] rs) {
        for (int xx = x0; xx < x1; xx++) {
            double xd = ((xx + chunkX * 16 + 0.5) - xCave) / rad;
            for (int zz = z0; zz < z1; zz++) {
                double zd = ((zz + chunkZ * 16 + 0.5) - zCave) / rad;
                boolean hasGrass = false;
                if (xd * xd + zd * zd >= 1.0) {
                    continue;
                }

                for (int yy = y1 - 1; yy >= y0; yy--) {
                    double yd = (yy + 0.5 - yCave) / yRad;
                    double test =
                            canyon ? ((xd * xd + zd * zd) * rs[yy] + (yd * yd / 6.0)) : (xd * xd + yd * yd + zd * zd);
                    if ((!canyon && yd > -0.7 && test < 1.0) || (canyon && test < 1.0)) {
                        BlockState block = chunk.getBlockState(xx, yy, zz);
                        if (block.getBlockType() == BlockTypes.GRASS_BLOCK) {
                            hasGrass = true;
                        }
                        if (block.getBlockType() == BlockTypes.STONE
                                || block.getBlockType() == BlockTypes.DIRT
                                || block.getBlockType() == BlockTypes.GRASS_BLOCK) {
                            if (yy < 10) {
                                chunk.setBlockState(xx, yy, zz, BlockTypes.LAVA.getDefaultState());
                            } else {
                                chunk.setBlockState(xx, yy, zz, BlockTypes.AIR.getDefaultState());
                                if (hasGrass
                                        && yy > 0
                                        && chunk.getBlockState(xx, yy - 1, zz).getBlockType() == BlockTypes.DIRT) {
                                    chunk.setBlockState(xx, yy - 1, zz, BlockTypes.DIRT.getDefaultState());
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
