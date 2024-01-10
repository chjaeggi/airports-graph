import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FlightPlanDoes {
    private val flightPlan = flightPlan()

    @Test
    fun `not calculate cost when no route is available between two airports`() {
        assertNull(flightPlan.cost(london, zurich))
    }

    @Test
    fun `calculate cost between two airports when a route is available`() {
        assertEquals(200.0, flightPlan.cost(zurich, london))
    }

    @Test
    fun `list child airports before sibling airports when traversing depth first`() {
        assertEquals(
            listOf(singapore, hongKong, tokyo, detroit, austin, sanFran, seattle, dc),
            flightPlan.dfs(singapore)
        )
    }

    @Test
    fun `list sibling airports before child airports when traversing breadth first`() {
        assertEquals(
            listOf(singapore, hongKong, tokyo, sanFran, detroit, dc, seattle, austin),
            flightPlan.bfs(singapore)
        )
    }

    @Test
    fun `calculate cheapest costs from one airport to all other airports`() {
        val costs = mapOf(
            hongKong to 300.0,
            tokyo to 500.0,
            dc to 800.0,
            sanFran to 900.0,
            detroit to 950.0,
            austin to 1000.0,
            seattle to 1077.0
        )
        assertEquals(costs, flightPlan.cheapestCostsFrom(singapore))
    }
}

private val zurich = Airport("Zurich")
private val london = Airport("London")
private val singapore = Airport("Singapore")
private val hongKong = Airport("Hong Kong")
private val tokyo = Airport("Tokyo")
private val detroit = Airport("Detroit")
private val dc = Airport("Washington DC")
private val sanFran = Airport("San Francisco")
private val austin = Airport("Austin")
private val seattle = Airport("Seattle")

private fun flightPlan(): FlightPlan = buildMap {
    add(EdgeType.Directed, Route(zurich, london, 200.0))
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