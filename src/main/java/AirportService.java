import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.Transaction;
import org.apache.tinkerpop.gremlin.structure.Vertex;

import java.util.Map;
import java.util.NoSuchElementException;

public interface AirportService {
    public boolean addAirport(Map<String, Object> airportData);
    public boolean addRoute(String fromAirport, String toAirport, int distance);

    public Map<String, Object> getAirportData(String airportCode);

    public int getRouteDistance(String fromAirportCode, String toAirportCode);

    public boolean hasRoute(String fromAirport, String toAirport);
    public boolean removeAirport(String airportCode);

    public boolean removeRoute(String fromAirportCode, String toAirportCode);
}
