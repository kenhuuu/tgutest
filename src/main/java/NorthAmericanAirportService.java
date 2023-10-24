import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;

public class NorthAmericanAirportService implements AirportService {
    private GraphTraversalSource g;

    public NorthAmericanAirportService(GraphTraversalSource g) {
        this.g = g;
    }
    public boolean addAirport(Map<String, Object> airportData) {
        return false;
    }
    public Edge addRoute2(String fromAirportCode, String toAirportCode, int distance) {
        Transaction tx = g.tx();
        GraphTraversalSource gtx = tx.begin(); // Explicitly starting the transaction.

        // It is recommended to use this try-catch-rollback approach with TinkerPop transactions.
        try {

            Edge edge = g.V().has("code", fromAirportCode)
                            .addE("route")
                            .to(__.V().has("code", toAirportCode))
                            .next();

            tx.commit();
            return edge;
        } catch (Exception e) {
            tx.rollback();
            return null;
        }
    }

    /**
     * Adds a route between two airports.
     *
     * @param fromAirportCode   The airport code of airport where the route begins.
     * @param toAirportCode     The airport code of airport where the route ends.
     * @param distance          The distance between the two airports.
     * @return                  True if the route was added; false otherwise.
     */
    public boolean addRoute(String fromAirportCode, String toAirportCode, int distance) {
        Transaction tx = g.tx();
        GraphTraversalSource gtx = tx.begin(); // Explicitly starting the transaction.

        // It is recommended to use this try-catch-rollback approach with TinkerPop transactions.
        try {
            final Vertex fromV = gtx.V().has("code", fromAirportCode).next();
            final Vertex toV = gtx.V().has("code", toAirportCode).next();
            gtx.addE("route").from(fromV).to(toV).next();
            tx.commit();

            return true;
        } catch (Exception e) {
            tx.rollback();
            return false;
        }
    }

    public Map<String, Object> getAirportData(String airportName) {
        return null;
    }

    public boolean hasRoute(String fromAirport, String toAirport) {
        return false;
    }
    public boolean removeAirport(String airportName) {
        return false;
    }
    public int getRouteDistance(String fromAirportCode, String toAirportCode) {return 0;}
    public boolean removeRoute(String fromAirport, String toAirport) {
        return false;
    }

    /**
     * Removes incoming routes to an airport.
     *
     * @param airportCode   The airport code of the airport to remove incoming routes.
     */
    public void stopIncomingTraffic(String airportCode) {
        Transaction tx = g.tx();

        try {
            g.V().has("code", airportCode).inE().drop().iterate();
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }
}
