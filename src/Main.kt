import java.util.*
import kotlin.collections.ArrayDeque

typealias FlightPlan = Map<Airport, List<Route>>

data class Airport(val name: String)
data class Route(val from: Airport, val to: Airport, val cost: Double)
data class Flight(val route: Route, val totalCost: Double) : Comparable<Flight> {
    override fun compareTo(other: Flight): Int {
        val comparison = totalCost - other.totalCost
        return when {
            comparison > 0.0 -> 1
            comparison < 0.0 -> -1
            else -> 0
        }
    }
}

enum class EdgeType {
    Directed, Undirected
}

fun main() {
    val flightPlan = flightPlan()

    flightPlan.forEach {
        println("${it.key.name} --> ${it.value.map { it.to.name }}")
    }
    println("----------------------------------------------------")

    println(flightPlan.cost(singapore, detroit) ?: "No direct flight found :(")
    println(flightPlan.cost(singapore, hongKong) ?: "No direct flight found :(")
    println("----------------------------------------------------")

    val traversalStart = dc
    println("Basic DFS from ${traversalStart.name}:")
    println(flightPlan.dfs(traversalStart))
    println("----------------------------------------------------")

    println("Basic BFS from ${traversalStart.name}:")
    println(flightPlan.bfs(traversalStart))
    println("----------------------------------------------------")

    val dijkstraStart = singapore
    val dijkstraDest = detroit
    val costs = flightPlan.cheapestCostsFrom(dijkstraStart)
    println("The cheapest flight from ${dijkstraStart.name} to ${dijkstraDest.name} costs: ${costs[dijkstraDest]}")
    println("All these airports can be reached from ${dijkstraStart.name}: ")
    costs.forEach { (airport, cost) -> println("${airport.name} -> $cost") }
    println("----------------------------------------------------")
}

private val singapore = Airport("Singapore")
private val hongKong = Airport("Hong Kong")
private val tokyo = Airport("Tokyo")
private val detroit = Airport("Detroit")
private val dc = Airport("Washington DC")
private val sanFran = Airport("San Francisco")
private val austin = Airport("Austin")
private val seattle = Airport("Seattle")

private fun flightPlan(): FlightPlan = buildMap {
    add(EdgeType.Undirected, Route(singapore, hongKong, 300.0))
    add(EdgeType.Undirected, Route(tokyo, singapore, 500.0))
    add(EdgeType.Undirected, Route(hongKong, tokyo, 250.0))
    add(EdgeType.Undirected, Route(hongKong, sanFran, 600.0))
    add(EdgeType.Undirected, Route(tokyo, detroit, 450.0))
    add(EdgeType.Undirected, Route(tokyo, dc, 300.0))
    add(EdgeType.Undirected, Route(sanFran, seattle, 218.0))
    add(EdgeType.Undirected, Route(sanFran, austin, 297.0))
    add(EdgeType.Undirected, Route(sanFran, dc, 337.0))
    add(EdgeType.Undirected, Route(detroit, austin, 50.0))
    add(EdgeType.Undirected, Route(dc, austin, 292.0))
    add(EdgeType.Undirected, Route(dc, seattle, 277.0))
    add(EdgeType.Undirected, Route(dc, tokyo, 300.0))
}

fun MutableMap<Airport, List<Route>>.add(type: EdgeType, route: Route) {
    val fromRoutes = this[route.from] ?: emptyList()
    this[route.from] = fromRoutes + route

    if (type == EdgeType.Undirected) {
        val toRoutes = this[route.to] ?: emptyList()
        this[route.to] = toRoutes + route.invert()
    }
}

private fun Route.invert() = copy(from = to, to = from)

fun FlightPlan.cost(from: Airport, to: Airport) = this[from]?.firstOrNull { it.to == to }?.cost

fun FlightPlan.dfs(from: Airport): List<Airport> {
    val seen = mutableSetOf<Airport>()

    fun traverseChildrenFirst(from: Airport) {
        this[from]?.let { routes ->
            routes.forEach {
                if (it.to !in seen) {
                    seen += it.to
                    traverseChildrenFirst(it.to)
                }
            }
        }
    }
    seen += from
    traverseChildrenFirst(from)
    return seen.toList()
}

fun FlightPlan.bfs(from: Airport): List<Airport> {
    val seen = mutableSetOf<Airport>()
    val queue = ArrayDeque<Airport>()
    queue.addFirst(from)
    while (queue.isNotEmpty()) {
        val current = queue.removeLast()
        if (current !in seen) {
            seen += current
            this[current]?.let { routes ->
                routes.forEach {
                    queue.addFirst(it.to)
                }
            }
        }
    }
    return seen.toList()
}

// dijkstra
fun FlightPlan.cheapestCostsFrom(airport: Airport): Map<Airport, Double> {
    val queue = PriorityQueue<Flight>()
    queue.addAll(this[airport]?.map { Flight(it, it.cost) } ?: emptyList()) // add starting outbounds from source
    val seen = mutableSetOf<Airport>()
    val costPerDestination = mutableMapOf<Airport, Double>()
    while (queue.isNotEmpty()) { // always find the local cheapest path (min heap property by priority queue)
        val currentFlight = queue.poll()
        if (currentFlight.route.to !in seen) {
            if (currentFlight.route.to != airport) costPerDestination[currentFlight.route.to] = currentFlight.totalCost
            this[currentFlight.route.to]?.let { routes ->
                queue += routes
                    .filterNot { it.to in seen } // prevent flying back
                    .map { Flight(it, currentFlight.totalCost + it.cost) }
            }
            seen += currentFlight.route.to
        }
    }
    return costPerDestination
}