import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.apache.tinkerpop.gremlin.driver.Cluster;
import org.apache.tinkerpop.gremlin.driver.remote.DriverRemoteConnection;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.T;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.util.TransactionException;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.AbstractTinkerGraph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerTransactionGraph;
import org.junit.Test;

import java.util.List;

import static org.apache.tinkerpop.gremlin.process.traversal.AnonymousTraversalSource.traversal;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.in;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.inV;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.out;
import static org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__.outV;
import static org.apache.tinkerpop.gremlin.structure.io.IoCore.graphml;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class AirportServiceTest {

    // In this example, a "STAGING_ENV" property is used to determine whether to test against TinkerGraph or Amazon Neptune.
    private static boolean STAGING_ENV = (null != System.getProperty("STAGING_ENV"));
    private static Cluster cluster;

    static {
        if (STAGING_ENV) {
            cluster = Cluster.build().create();
        }
    }
    public GraphTraversalSource getGraphTraversalSource() {
        GraphTraversalSource g = null;
        if (STAGING_ENV) { // In this example, STAGING_ENV is a system property used to determine which database to use.
            g = traversal().withRemote(DriverRemoteConnection.using(cluster));
        } else {
            // Create a default, empty instance of the transactional TinkerGraph.
            g = TinkerTransactionGraph.open().traversal();

            g.io("air-routes-small.xml").read().iterate(); // This is how you insert your GraphML test data.
            g.tx().commit(); // By default, transactions are automatically opened, so commit the changes from io().
        }

        return g;
    }

    public GraphTraversalSource getCustomGraphTraversalSource() {
        // Create a TinkerTransactionGraph with a custom configuration.
        Configuration conf = new BaseConfiguration();
        conf.setProperty(AbstractTinkerGraph.GREMLIN_TINKERGRAPH_VERTEX_ID_MANAGER, AbstractTinkerGraph.DefaultIdManager.ANY);
        conf.setProperty(AbstractTinkerGraph.GREMLIN_TINKERGRAPH_EDGE_ID_MANAGER, AbstractTinkerGraph.DefaultIdManager.ANY);
        return TinkerTransactionGraph.open(conf).traversal();
    }

    @Test
    public void testAddRouteWithIncorrectAirportCode() {
        final GraphTraversalSource g = getGraphTraversalSource();
        final NorthAmericanAirportService service = new NorthAmericanAirportService(g);

        // Add route with airport code that doesn't exist.
        final boolean wasAdded = service.addRoute("INCORRECT", "AUS", 500);

        assertFalse(wasAdded);
        // Check to see if any routes exist between those two airports.
        assertEquals(0L, g.E().where(inV().has("code", "INCORRECT")).where(outV().has("code", "AUS")).count().next().longValue());
    }

    @Test
    public void testAddRouteWithValidAirportCodes() {
        final GraphTraversalSource g = getGraphTraversalSource();
        final NorthAmericanAirportService service = new NorthAmericanAirportService(g);

        final boolean wasAdded = service.addRoute("PBI", "ORD", 500);

        assertTrue(wasAdded);
        assertEquals(1L, g.E().where(inV().has("code", "PBI")).where(outV().has("code", "ORD")).count().next().longValue());
    }

    @Test
    public void testStoppingTrafficToAus() {
        final GraphTraversalSource g = getGraphTraversalSource();
        final NorthAmericanAirportService service = new NorthAmericanAirportService(g);
        final String airport = "AUS";

        service.stopIncomingTraffic(airport);

        // Check that there are no outgoing routes into that airport.
        assertEquals(0, g.V().out().has("code", airport).toList().size());
    }
}
