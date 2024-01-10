import java.util.*
import kotlin.collections.ArrayDeque

typealias FlightPlan = MutableMap<Airport, List<Route>>

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

private val singapore = Airport("Singapore")
private val hongKong = Airport("Hong Kong")
private val tokyo = Airport("Tokyo")
private val detroit = Airport("Detroit")
private val dc = Airport("Washington DC")
private val sanFran = Airport("San Francisco")
private val austin = Airport("Austin")
private val seattle = Airport("Seattle")

fun main() {

    val flights: FlightPlan = mutableMapOf()
    flights.connectFlights()

    flights.forEach {
        println("${it.key.name} --> ${it.value.map { it.to.name }}")
    }
    println("----------------------------------------------------")

    println(flights.weight(singapore, detroit) ?: "No direct flight found :(")
    println(flights.weight(singapore, hongKong) ?: "No direct flight found :(")
    println("----------------------------------------------------")

    val traversalStart = dc
    println("Basic DFS from ${traversalStart.name}:")
    println(flights.dfs(traversalStart))
    println("----------------------------------------------------")

    println("Basic BFS from ${traversalStart.name}:")
    println(flights.bfs(traversalStart))
    println("----------------------------------------------------")

    val dijkstraStart = singapore
    val dijkstraDest = detroit

    print("The cheapest flight from ${dijkstraStart.name} to ${dijkstraDest.name} costs: ")
    flights.cheapestFlight(dijkstraStart, dijkstraDest)
    print("All these airports can be reached from ${dijkstraStart.name}: ")
    flights.cheapestFlight(dijkstraStart)
    println("----------------------------------------------------")

}

private fun FlightPlan.connectFlights() {
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

fun FlightPlan.add(type: EdgeType, route: Route) {
    if (route.from in keys) {
        this[route.from] = this[route.from]!! + route
    } else {
        this[route.from] = listOf(route)
    }
    if (type == EdgeType.Undirected) {
        if (route.to in keys) {
            this[route.to] = this[route.to]!! + Route(route.to, route.from, route.cost)
        } else {
            this[route.to] = listOf(Route(route.to, route.from, route.cost))
        }
    }
}

fun FlightPlan.weight(from: Airport, to: Airport) = this[from]?.firstOrNull { it.to == to }?.cost

fun FlightPlan.dfs(from: Airport): List<Airport> {
    val seen = mutableSetOf<Airport>()
    seen += from
    dfsHelper(from, seen)
    return seen.toList()
}

private fun FlightPlan.dfsHelper(from: Airport, seen: MutableSet<Airport>) {
    this[from]?.let {
        it.forEach {
            if (it.to !in seen) {
                seen += it.to
                dfsHelper(it.to, seen)
            }
        }
    }
}

fun FlightPlan.bfs(from: Airport): List<Airport> {
    val seen = mutableSetOf<Airport>()
    val queue = ArrayDeque<Airport>()
    queue.addFirst(from)
    while (queue.isNotEmpty()) {
        val current = queue.removeLast()
        if (current !in seen) {
            seen += current
            this[current]?.let {
                it.forEach {
                    queue.addFirst(it.to)
                }
            }
        }
    }
    return seen.toList()
}

// dijkstra
fun FlightPlan.cheapestFlight(from: Airport, to: Airport? = null): Double? {
    val seen = mutableSetOf<Airport>()
    val queue = PriorityQueue<Flight>()
    val costPerDestination = mutableMapOf<Airport, Double>()

    queue.addAll(this[from]?.map { Flight(it, it.cost) } ?: emptyList()) // add starting outbounds from source
    while (queue.isNotEmpty()) { // always find the local cheapest path (min heap property by priority queue)
        val currentFlight = queue.poll()
        if (currentFlight.route.to !in seen) {
            if (currentFlight.route.to != from) costPerDestination[currentFlight.route.to] = currentFlight.totalCost
            this[currentFlight.route.to]?.let {
                queue += it
                    .filterNot { it.to in seen } // prevent flying back
                    .map {
                        Flight(it, currentFlight.totalCost + it.cost)
                    }
            }
            seen += currentFlight.route.to
        }
    }

    if (to != null) {
        // desired final airport
        println(costPerDestination[to] ?: "Airport ${to.name} not reachable from ${from.name}")
        return costPerDestination[to]
    } else {
        // show all the reachable airports for source
        println(costPerDestination.map {
            buildString {
                append("${it.key.name} -> ${it.value}")
            }
        })
    }
    return null
}