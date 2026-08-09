package com.example.endlesssnake.core.world

import com.example.endlesssnake.core.math.ChunkPos
import com.example.endlesssnake.core.math.WorldPoint

class Chunk(val pos: ChunkPos) {
    val obstacles = HashSet<WorldPoint>()
    fun hasObstacle(p: WorldPoint) = obstacles.contains(p)
    fun addObstacle(p: WorldPoint) { obstacles.add(p) }
    fun removeObstacle(p: WorldPoint) { obstacles.remove(p) }
}