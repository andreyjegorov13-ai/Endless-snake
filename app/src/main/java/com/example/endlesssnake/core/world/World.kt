package com.example.endlesssnake.core.world

import com.example.endlesssnake.core.ChunkData
import com.example.endlesssnake.core.math.ChunkPos
import com.example.endlesssnake.core.math.WorldPoint
import com.example.endlesssnake.data.Difficulty
import java.util.Random
import kotlin.math.min

class World(val seed: Long = System.currentTimeMillis()) {
    private val chunks = HashMap<ChunkPos, Chunk>()

    fun hasObstacle(p: WorldPoint, score: Int = 0, difficulty: Difficulty = Difficulty.NORMAL): Boolean {
        val chunkPos = ChunkPos.fromWorld(p)
        val chunk = chunks.getOrPut(chunkPos) { generateChunk(chunkPos, score, difficulty) }
        return chunk.hasObstacle(p)
    }

    fun clearObstacle(p: WorldPoint, score: Int = 0, difficulty: Difficulty = Difficulty.NORMAL) {
        val chunkPos = ChunkPos.fromWorld(p)
        val chunk = chunks.getOrPut(chunkPos) { generateChunk(chunkPos, score, difficulty) }
        chunk.removeObstacle(p)
    }

    fun exportChunks(): List<ChunkData> =
        chunks.map { (pos, chunk) -> ChunkData(pos.x, pos.y, chunk.obstacles.toList()) }

    fun importChunks(data: List<ChunkData>) {
        chunks.clear()
        data.forEach { cd ->
            val pos = ChunkPos(cd.cx, cd.cy)
            val chunk = Chunk(pos)
            cd.obstacles.forEach { chunk.addObstacle(it) }
            chunks[pos] = chunk
        }
    }

    private fun generateChunk(pos: ChunkPos, score: Int, difficulty: Difficulty): Chunk {
        val chunk = Chunk(pos)
        val chunkSeed = pos.x * 31337L + pos.y * 73313L + seed
        val rnd = Random(chunkSeed)
        val startX = pos.x * ChunkPos.SIZE
        val startY = pos.y * ChunkPos.SIZE

        val (startDens, maxDens) = when (difficulty) {
            Difficulty.EASY -> 0.01f to 0.10f
            Difficulty.NORMAL -> 0.01f to 0.15f
            Difficulty.HARD -> 0.03f to 0.20f
        }
        val density = min(maxDens, startDens + score * 0.003f)

        for (lx in 0 until ChunkPos.SIZE) {
            for (ly in 0 until ChunkPos.SIZE) {
                val worldX = startX + lx
                val worldY = startY + ly
                if (worldX in -5..5 && worldY in -5..5) continue
                if (rnd.nextFloat() < density) {
                    chunk.addObstacle(WorldPoint(worldX, worldY))
                }
            }
        }
        return chunk
    }
}