package com.example.endlesssnake.core

import com.example.endlesssnake.core.math.Direction
import com.example.endlesssnake.core.math.WorldPoint
import com.example.endlesssnake.data.Difficulty

data class ChunkData(val cx: Int, val cy: Int, val obstacles: List<WorldPoint>)

data class GameSnapshot(
    val score: Int,
    val lives: Int,
    val baseTickMs: Long,
    val difficulty: Difficulty,
    val direction: Direction,
    val snake: List<WorldPoint>,
    val food: WorldPoint,
    val bonuses: List<Bonus>,
    val chunks: List<ChunkData>
)