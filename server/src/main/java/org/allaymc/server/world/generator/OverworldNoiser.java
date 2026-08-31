package org.allaymc.server.world.generator;

import org.allaymc.api.block.type.BlockState;
import org.allaymc.api.block.type.BlockTypes;
import org.allaymc.api.world.biome.BiomeType;
import org.allaymc.api.world.chunk.UnsafeChunk;
import org.allaymc.api.world.generator.context.NoiseContext;
import org.allaymc.api.world.generator.function.Noiser;
import org.allaymc.server.world.generator.carver.CanyonFeature;
import org.allaymc.server.world.generator.carver.LargeCaveFeature;
import org.allaymc.server.world.generator.noise.PerlinNoise;

import java.util.Arrays;
import java.util.Random;

/**
 * @author zernix2077, Kanelucky
 */
public class OverworldNoiser implements Noiser {

    private static final int SEA_LEVEL = 62;
    private static final int CHUNK_WIDTH = 4;
    private static final int CHUNK_HEIGHT = 8;
    public static final int GEN_DEPTH = 128;
    private static final Random random = new Random();
    private float[] pows;
    private final PerlinNoise lperlinNoise1 = new PerlinNoise(random, 16);
    private final PerlinNoise lperlinNoise2 = new PerlinNoise(random, 16);
    private final PerlinNoise perlinNoise1 = new PerlinNoise(random, 8);
    private final PerlinNoise perlinNoise3 = new PerlinNoise(random, 4);
    private final PerlinNoise scaleNoise = new PerlinNoise(random, 10);
    private final PerlinNoise depthNoise = new PerlinNoise(random, 16);
    private final LargeCaveFeature caveFeature = new LargeCaveFeature();
    private final CanyonFeature canyonFeature = new CanyonFeature();
    private long worldSeed = 0L;

    @Override
    public void init(String preset) {
        parsePreset(preset);
    }

    protected void parsePreset(String preset) {
        long seed = 0L;
        if (preset != null && !preset.isBlank()) {
            for (var entry : preset.split("[,;]")) {
                var parts = entry.split("=", 2);
                if (parts.length != 2) {
                    continue;
                }

                var key = parts[0].trim().toLowerCase();
                var value = parts[1].trim();
                try {
                    switch (key) {
                        case "seed" -> seed = Long.parseLong(value);
                        default -> {
                        }
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        this.worldSeed = seed;
    }

    @Override
    public boolean apply(NoiseContext noiseContext) {
        var unsafeChunk = noiseContext.getCurrentChunk();
        generateChunk(unsafeChunk.getX(), unsafeChunk.getZ(), unsafeChunk);
        return true;
    }

    private void generateChunk(int chunkX, int chunkZ, UnsafeChunk chunk) {
        random.setSeed(chunkX * 341873128712L + chunkZ * 132897987541L);
        prepareHeights(chunkX, chunkZ, chunk);
        buildSurfaces(chunkX, chunkZ, chunk);
        caveFeature.apply(worldSeed, chunkX, chunkZ, chunk);
        canyonFeature.apply(worldSeed, chunkX, chunkZ, chunk);
    }

    private void prepareHeights(int chunkX, int chunkZ, UnsafeChunk chunk) {
        int xChunks = 16 / CHUNK_WIDTH;
        int yChunks = GEN_DEPTH / CHUNK_HEIGHT;
        int xSize = xChunks + 1;
        int ySize = yChunks + 1;
        int zSize = xChunks + 1;

        int biomeWidth = xSize + 4;
        int biomeHeight = zSize + 4;

        BiomeType[] biomes = new BiomeType[biomeWidth * biomeHeight];

        BiomeType biome = chunk.getBiome(0, 0, 0);

        Arrays.fill(biomes, biome);
        double[] heights = getHeights(chunkX * xChunks, 0, chunkZ * xChunks, xSize, ySize, zSize, biomes, biomeWidth);
        for (int xc = 0; xc < xChunks; xc++) {
            for (int zc = 0; zc < xChunks; zc++) {
                for (int yc = 0; yc < yChunks; yc++) {
                    double yStep = 1.0 / CHUNK_HEIGHT;
                    double s0 = heights[((xc) * zSize + zc) * ySize + yc];
                    int i = ((xc) * zSize + (zc + 1)) * ySize;
                    double s1 = heights[i + yc];
                    int i1 = ((xc + 1) * zSize + zc) * ySize;
                    double s2 = heights[i1 + yc];
                    int i2 = ((xc + 1) * zSize + (zc + 1)) * ySize;
                    double s3 = heights[i2 + yc];
                    double s0a = (heights[((xc) * zSize + zc) * ySize + (yc + 1)] - s0) * yStep;
                    double s1a = (heights[i + (yc + 1)] - s1) * yStep;
                    double s2a = (heights[i1 + (yc + 1)] - s2) * yStep;
                    double s3a = (heights[i2 + (yc + 1)] - s3) * yStep;

                    for (int y = 0; y < CHUNK_HEIGHT; y++) {
                        double xStep = 1.0 / CHUNK_WIDTH;
                        double currentS0 = s0;
                        double currentS1 = s1;
                        double currentS0a = (s2 - s0) * xStep;
                        double currentS1a = (s3 - s1) * xStep;

                        for (int x = 0; x < CHUNK_WIDTH; x++) {
                            double zStep = 1.0 / CHUNK_WIDTH;
                            double value = currentS0;
                            double valueStep = (currentS1 - currentS0) * zStep;
                            value -= valueStep;
                            for (int z = 0; z < CHUNK_WIDTH; z++) {
                                value += valueStep;
                                BlockState block = BlockTypes.AIR.getDefaultState();
                                int worldY = yc * CHUNK_HEIGHT + y;
                                if (value > 0.0) {
                                    block = BlockTypes.STONE.getDefaultState();
                                } else if (worldY < SEA_LEVEL) {
                                    block = BlockTypes.WATER.getDefaultState();
                                }
                                chunk.setBlockState(x + xc * CHUNK_WIDTH, worldY, z + zc * CHUNK_WIDTH, block);
                            }
                            currentS0 += currentS0a;
                            currentS1 += currentS1a;
                        }

                        s0 += s0a;
                        s1 += s1a;
                        s2 += s2a;
                        s3 += s3a;
                    }
                }
            }
        }
    }

    private void buildSurfaces(int chunkX, int chunkZ, UnsafeChunk chunk) {
        double scale = 1.0 / 32.0;

        double[] depthBuffer =
                perlinNoise3.getRegion(null, chunkX * 16, chunkZ * 16, 16, 16, scale * 2.0, scale * 2.0, scale * 2.0);

        BlockState grass = BlockTypes.GRASS_BLOCK.getDefaultState();
        BlockState dirt = BlockTypes.DIRT.getDefaultState();
        BlockState stone = BlockTypes.STONE.getDefaultState();
        BlockState air = BlockTypes.AIR.getDefaultState();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int runDepth = (int) (depthBuffer[x + z * 16] / 3.0 + 3.0 + random.nextDouble() * 0.25);
                int run = -1;
                for (int y = GEN_DEPTH - 1; y >= 0; y--) {
                    if (y <= 1 + random.nextInt(2)) {
                        chunk.setBlockState(x, y, z, BlockTypes.BEDROCK.getDefaultState());
                        continue;
                    }
                    BlockState old = chunk.getBlockState(x, y, z);
                    if (old == air) {
                        run = -1;
                    } else if (old == stone) {
                        if (run == -1) {
                            if (runDepth <= 0) {
                                run = 0;
                            } else {
                                run = runDepth;
                                chunk.setBlockState(x, y, z, grass);
                            }
                        } else if (run > 0) {
                            run--;
                            chunk.setBlockState(x, y, z, dirt);
                        }
                    }
                }
            }
        }
    }

    private double[] getHeights(
            int x, int y, int z, int xSize, int ySize, int zSize, BiomeType[] biomes, int biomeWidth) {
        if (pows == null) {
            pows = new float[25];
            for (int xb = -2; xb <= 2; xb++) {
                for (int zb = -2; zb <= 2; zb++) {
                    pows[xb + 2 + (zb + 2) * 5] = (float) (10.0 / Math.sqrt(xb * xb + zb * zb + 0.2));
                }
            }
        }

        double s = 684.412;
        double hs = 684.412;
        scaleNoise.getRegion(null, x, z, xSize, zSize, 1.121, 1.121, 0.5);
        double[] dr = depthNoise.getRegion(null, x, z, xSize, zSize, 200.0, 200.0, 0.5);
        double[] pnr = perlinNoise1.getRegion(null, x, y, z, xSize, ySize, zSize, s / 80.0, hs / 160.0, s / 80.0);
        double[] ar = lperlinNoise1.getRegion(null, x, y, z, xSize, ySize, zSize, s, hs, s);
        double[] br = lperlinNoise2.getRegion(null, x, y, z, xSize, ySize, zSize, s, hs, s);
        double[] buffer = new double[xSize * ySize * zSize];

        int p = 0;
        int pp = 0;
        for (int xx = 0; xx < xSize; xx++) {
            for (int zz = 0; zz < zSize; zz++) {
                float weightedScale = 0.0f;
                float weightedDepth = 0.0f;
                float weightTotal = 0.0f;
                BiomeType middleBiome = biomes[(xx + 2) + (zz + 2) * biomeWidth];
                for (int xb = -2; xb <= 2; xb++) {
                    for (int zb = -2; zb <= 2; zb++) {
                        BiomeType biome = biomes[(xx + xb + 2) + (zz + zb + 2) * biomeWidth];
                        float weight = pows[xb + 2 + (zb + 2) * 5]
                                / (biome.getBiomeData().depth() + 2.0f);
                        if (biome.getBiomeData().depth()
                                > middleBiome.getBiomeData().depth()) {
                            weight /= 2.0f;
                        }
                        weightedScale += biome.getBiomeData().scale() * weight;
                        weightedDepth += biome.getBiomeData().depth() * weight;
                        weightTotal += weight;
                    }
                }
                weightedScale /= weightTotal;
                weightedDepth /= weightTotal;
                weightedScale = weightedScale * 0.9f + 0.1f;
                weightedDepth = (weightedDepth * 4.0f - 1.0f) / 8.0f;
                double randomDepth = getRandomDepth(dr[pp]);
                pp++;
                for (int yy = 0; yy < ySize; yy++) {
                    double depth = weightedDepth + randomDepth * 0.2;
                    double scaleValue = weightedScale;
                    depth = depth * ySize / 16.0;
                    double yCenter = ySize / 2.0 + depth * 4.0;
                    double yOffset = (yy - yCenter) * 12.0 * 128.0 / GEN_DEPTH / scaleValue;
                    if (yOffset < 0.0) {
                        yOffset *= 4.0;
                    }

                    double low = ar[p] / 512.0;
                    double high = br[p] / 512.0;
                    double blend = (pnr[p] / 10.0 + 1.0) / 2.0;
                    double value = blend < 0.0 ? low : blend > 1.0 ? high : low + (high - low) * blend;
                    value -= yOffset;

                    if (yy > ySize - 4) {
                        double slide = (yy - (ySize - 4)) / 3.0;
                        value = value * (1.0 - slide) + -10.0 * slide;
                    }

                    buffer[p] = value;
                    p++;
                }
            }
        }

        return buffer;
    }

    private static double getRandomDepth(double dr) {
        double randomDepth = dr / 8000.0;
        if (randomDepth < 0.0) {
            randomDepth = -randomDepth * 0.3;
        }
        randomDepth = randomDepth * 3.0 - 2.0;
        if (randomDepth < 0.0) {
            randomDepth /= 2.0;
            if (randomDepth < -1.0) {
                randomDepth = -1.0;
            }
            randomDepth /= 1.4;
            randomDepth /= 2.0;
        } else {
            if (randomDepth > 1.0) {
                randomDepth = 1.0;
            }
            randomDepth /= 8.0;
        }
        return randomDepth;
    }
}
