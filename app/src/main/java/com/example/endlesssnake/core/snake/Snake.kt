package com.example.endlesssnake.core.snake
import com.example.endlesssnake.core.math.Direction
import com.example.endlesssnake.core.math.WorldPoint
class Snake(start: WorldPoint) {
    val body = ArrayDeque<WorldPoint>()
    var direction = Direction.RIGHT
    private var grow = 0
    init {
        body.addLast(start)
        body.addLast(WorldPoint(start.x - 1, start.y))
        body.addLast(WorldPoint(start.x - 2, start.y))
    }
    val head: WorldPoint get() = body.first()
    fun turn(newDir: Direction) {
        if (!direction.isOpposite(newDir) && body.size > 1) { direction = newDir }
    }
    fun move() {
        val newHead = WorldPoint(head.x + direction.dx, head.y + direction.dy)
        body.addFirst(newHead)
        if (grow > 0) grow-- else body.removeLast()
    }
    fun feed() { grow++ }
    fun contains(p: WorldPoint): Boolean = body.contains(p)
}