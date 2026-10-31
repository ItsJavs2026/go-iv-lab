package com.goivlab.iv

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import java.util.ArrayDeque

/** Evolution stages are actual graph links from the pinned PvPoke gamemaster.
 * Family ID alone is insufficient: alternate/Shadow forms cannot be assumed
 * to evolve into every other member with the same family identifier.
 */
data class EvolutionStage(
    val species: PokemonSpecies,
    val stage: String,
    val depth: Int,
    val isFinal: Boolean
)

class EvolutionFamilies private constructor(private val graph: Map<String, Set<String>>) {
    fun stagesFor(speciesId: String, catalog: PokemonStatsCatalog): List<EvolutionStage> {
        if (catalog.find(speciesId) == null) return emptyList()

        val parents = mutableMapOf<String, MutableSet<String>>()
        val neighbors = mutableMapOf<String, MutableSet<String>>()
        for ((from, children) in graph) {
            for (to in children) {
                if (catalog.find(from) == null || catalog.find(to) == null) continue
                neighbors.getOrPut(from) { linkedSetOf() }.add(to)
                neighbors.getOrPut(to) { linkedSetOf() }.add(from)
                parents.getOrPut(to) { linkedSetOf() }.add(from)
            }
        }
        if (neighbors[speciesId].isNullOrEmpty()) return emptyList()
        val component = linkedSetOf(speciesId)
        val toVisit = ArrayDeque<String>()
        toVisit.add(speciesId)
        while (toVisit.isNotEmpty()) {
            val current = toVisit.removeFirst()
            for (adjacent in neighbors[current].orEmpty()) {
                if (component.add(adjacent)) toVisit.add(adjacent)
            }
        }
        val roots = component.filter { node ->
            parents[node].orEmpty().none { it in component }
        }
        // Bad/circular metadata must never cause a fabricated family ordering.
        if (roots.isEmpty()) return emptyList()
        val depth = mutableMapOf<String, Int>()
        val queue = ArrayDeque<String>()
        for (root in roots) {
            depth[root] = 0
            queue.add(root)
        }
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            val newDepth = depth.getValue(current) + 1
            for (child in graph[current].orEmpty().filter { it in component }) {
                if (depth[child] == null || newDepth < depth.getValue(child)) {
                    depth[child] = newDepth
                    queue.add(child)
                }
            }
        }
        if (depth.size != component.size) return emptyList()
        return component.map { id ->
            val level = depth.getValue(id)
            val final = graph[id].orEmpty().none { it in component }
            EvolutionStage(
                species = catalog.requireSpecies(id),
                stage = if (level == 0) "First" else if (final) "Final" else "Middle",
                depth = level,
                isFinal = final
            )
        }.sortedWith(compareBy<EvolutionStage> { it.depth }.thenBy { it.species.name })
    }

    companion object {
        fun parse(jsonText: String): EvolutionFamilies {
            val root = Json.parseToJsonElement(jsonText).jsonObject
            require(root.getValue("schemaVersion").jsonPrimitive.content == "1")
            require(root.getValue("sourceGameMasterGitBlobSha1").jsonPrimitive.content ==
                "f5a1c50c9162fac0f0c34bb7a662f995c6506b74")
            val children = linkedMapOf<String, MutableSet<String>>()
            val seen = hashSetOf<String>()
            for (element in root.getValue("entries").jsonArray) {
                val obj = element.jsonObject
                val id = obj.getValue("id").jsonPrimitive.content
                require(id.isNotBlank() && seen.add(id)) { "Invalid/duplicate evolution species ID: $id" }
                children.getOrPut(id) { linkedSetOf() }
                val parent = obj["parent"]?.jsonPrimitive?.contentOrNull
                if (!parent.isNullOrBlank()) {
                    require(parent != id) { "Self-parenting evolution: $id" }
                    children.getOrPut(parent) { linkedSetOf() }.add(id)
                }
                for (child in obj["evolutions"]?.jsonArray.orEmpty()) {
                    val target = child.jsonPrimitive.content
                    require(target.isNotBlank() && target != id)
                    children.getOrPut(id) { linkedSetOf() }.add(target)
                }
            }
            return EvolutionFamilies(children.mapValues { (_, to) -> to.toSet() })
        }
    }
}
