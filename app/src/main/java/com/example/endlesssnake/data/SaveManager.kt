package com.example.endlesssnake.data

import android.content.Context
import com.example.endlesssnake.core.Bonus
import com.example.endlesssnake.core.ChunkData
import com.example.endlesssnake.core.FoodType
import com.example.endlesssnake.core.GameSnapshot
import com.example.endlesssnake.core.math.Direction
import com.example.endlesssnake.core.math.WorldPoint
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object SaveManager {
    private fun saveFile(context: Context) = File(context.filesDir, "snake_save.json")

    fun hasSave(context: Context): Boolean = saveFile(context).exists()

    fun deleteSave(context: Context) {
        try { saveFile(context).delete() } catch (e: Exception) {}
    }

    fun save(context: Context, s: GameSnapshot) {
        try {
            val json = JSONObject()
            json.put("score", s.score)
            json.put("lives", s.lives)
            json.put("baseTickMs", s.baseTickMs)
            json.put("difficulty", s.difficulty.name)
            json.put("direction", s.direction.name)

            val snakeArr = JSONArray()
            s.snake.forEach { p -> snakeArr.put(JSONArray().put(p.x).put(p.y)) }
            json.put("snake", snakeArr)

            json.put("food", JSONArray().put(s.food.x).put(s.food.y))

            val bonArr = JSONArray()
            s.bonuses.forEach { b ->
                bonArr.put(JSONObject().put("x", b.pos.x).put("y", b.pos.y).put("t", b.type.name))
            }
            json.put("bonuses", bonArr)

            val chunksArr = JSONArray()
            s.chunks.forEach { cd ->
                val obs = JSONArray()
                cd.obstacles.forEach { p -> obs.put(JSONArray().put(p.x).put(p.y)) }
                chunksArr.put(JSONObject().put("cx", cd.cx).put("cy", cd.cy).put("obs", obs))
            }
            json.put("chunks", chunksArr)

            saveFile(context).writeText(json.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun load(context: Context): GameSnapshot? {
        return try {
            val json = JSONObject(saveFile(context).readText())

            val snake = mutableListOf<WorldPoint>()
            val snakeArr = json.getJSONArray("snake")
            for (i in 0 until snakeArr.length()) {
                val a = snakeArr.getJSONArray(i)
                snake.add(WorldPoint(a.getInt(0), a.getInt(1)))
            }
            if (snake.isEmpty()) return null

            val f = json.getJSONArray("food")
            val food = WorldPoint(f.getInt(0), f.getInt(1))

            val bonuses = mutableListOf<Bonus>()
            val bonArr = json.optJSONArray("bonuses") ?: JSONArray()
            for (i in 0 until bonArr.length()) {
                val o = bonArr.getJSONObject(i)
                bonuses.add(Bonus(
                    WorldPoint(o.getInt("x"), o.getInt("y")),
                    FoodType.valueOf(o.getString("t"))
                ))
            }

            val chunks = mutableListOf<ChunkData>()
            val chunksArr = json.optJSONArray("chunks") ?: JSONArray()
            for (i in 0 until chunksArr.length()) {
                val o = chunksArr.getJSONObject(i)
                val obs = mutableListOf<WorldPoint>()
                val obsArr = o.getJSONArray("obs")
                for (j in 0 until obsArr.length()) {
                    val a = obsArr.getJSONArray(j)
                    obs.add(WorldPoint(a.getInt(0), a.getInt(1)))
                }
                chunks.add(ChunkData(o.getInt("cx"), o.getInt("cy"), obs))
            }

            GameSnapshot(
                score = json.getInt("score"),
                lives = json.getInt("lives"),
                baseTickMs = json.getLong("baseTickMs"),
                difficulty = Difficulty.valueOf(json.getString("difficulty")),
                direction = Direction.valueOf(json.getString("direction")),
                snake = snake,
                food = food,
                bonuses = bonuses,
                chunks = chunks
            )
        } catch (e: Exception) {
            null
        }
    }
}