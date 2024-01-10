import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import kotlin.math.sin

class MainKtTest {
    private val flights: FlightPlan = mutableMapOf()
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

    @Before
    fun setup() {
        flights.add(EdgeType.Directed, Route(zurich, london, 200.0))
        flights.add(EdgeType.Undirected, Route(singapore, hongKong, 300.0))
        flights.add(EdgeType.Undirected, Route(tokyo, singapore, 500.0))
        flights.add(EdgeType.Undirected, Route(hongKong, tokyo, 250.0))
        flights.add(EdgeType.Undirected, Route(hongKong, sanFran, 600.0))
        flights.add(EdgeType.Undirected, Route(tokyo, detroit, 450.0))
        flights.add(EdgeType.Undirected, Route(tokyo, dc, 300.0))
        flights.add(EdgeType.Undirected, Route(sanFran, seattle, 218.0))
        flights.add(EdgeType.Undirected, Route(sanFran, austin, 297.0))
        flights.add(EdgeType.Undirected, Route(sanFran, dc, 337.0))
        flights.add(EdgeType.Undirected, Route(detroit, austin, 50.0))
        flights.add(EdgeType.Undirected, Route(dc, austin, 292.0))
        flights.add(EdgeType.Undirected, Route(dc, seattle, 277.0))
        flights.add(EdgeType.Undirected, Route(dc, tokyo, 300.0))
    }

    @Test
    fun `when no route is available for a start and an end airport no cost is available`() {
        assertNull(flights.weight(london, zurich))
    }

    @Test
    fun `when a route is available for a start and an end airport the correct cost is available`() {
        assertEquals(200.0, flights.weight(zurich, london))
    }

    @Test
    fun `when traversing depth first the visited airports are listed in correct order`() {
        assertEquals(listOf(singapore, hongKong, tokyo, detroit, austin, sanFran, seattle, dc), flights.dfs(singapore))
    }

    @Test
    fun `when traversing breadth first the visited airports are listed in correct order`() {
        assertEquals(listOf(singapore, hongKong, tokyo, sanFran, detroit, dc, seattle, austin), flights.bfs(singapore))
    }

    @Test
    fun `when giving two airports that are not connected return no cheapest flight`() {
        assertNull(flights.cheapestFlight(singapore, zurich))
    }

    @Test
    fun `when giving two airports that are connected return flight cost`() {
        assertEquals(950.0, flights.cheapestFlight(singapore, detroit))
    }
}