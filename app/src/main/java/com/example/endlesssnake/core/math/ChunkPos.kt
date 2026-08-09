package com.example.endlesssnake.core.math

data class ChunkPos(val x: Int, val y: Int) {
    companion object {
        const val SIZE = 16
        fun fromWorld(p: WorldPoint): ChunkPos {
            return ChunkPos(Math.floorDiv(p.x, SIZE), Math.floorDiv(p.y, SIZE))
        }
    }
}