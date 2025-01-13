package com.example.ilp_cw1.Controllers;

import com.example.ilp_cw1.DTO.*;
import com.example.ilp_cw1.Services.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import java.util.*;
import java.util.List;

@RestController
public class DeliveryPathController {

    private final OrderController orderController;

    private final RestaurantService restaurantService;

    private final CentralAreaService centralAreaService;

    private final NoFlyZoneService noFlyZoneService;

    private final PositioningController positioningController;

    public DeliveryPathController(OrderController orderController, RestaurantService restaurantService, CentralAreaService centralAreaService, NoFlyZoneService noFlyZoneService, PositioningController positioningController) {
        this.orderController = orderController;
        this.restaurantService = restaurantService;
        this.centralAreaService = centralAreaService;
        this.noFlyZoneService = noFlyZoneService;
        this.positioningController = positioningController;
    }

    @PostMapping("/calcDeliveryPath")
    public ResponseEntity<Object> calcDeliveryPath(@RequestBody Order order) {
        try {
            //checks for valid order
            if (!Objects.requireNonNull(orderController.validateOrder(order).getBody())
                    .getOrderStatus().equals(OrderStatus.VALID)) {
                return new ResponseEntity<>("Invalid Order: Validate Order First", HttpStatus.BAD_REQUEST);
            }

            //helper method to grab start and end positions
            List<LngLat> startAndGoal = setupForAStarSearch(order);
            if (startAndGoal.size() < 2) {
                return new ResponseEntity<>("Invalid start or goal for pathfinding", HttpStatus.BAD_REQUEST);
            }

            //generate path using A* algorithm
            List<LngLat> path = findPathUsingAStar(startAndGoal.get(0), startAndGoal.get(1));
            if (path == null || path.isEmpty()) {
                return new ResponseEntity<>("Path can't be found", HttpStatus.BAD_REQUEST);
            }

            return ResponseEntity.ok(path);
        }
        catch (Exception e) {
            // Log the exception for better debugging
            return new ResponseEntity<>("Unexpected error occurred: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PostMapping("/calcDeliveryPathAsGeoJson")
    public ResponseEntity<Object> calcDeliveryPathAsGeoJson(@RequestBody Order order) {
        try {
            //helper method to grab start and end positions
            List<LngLat> startAndGoal = setupForAStarSearch(order);

            //generate path using A* algorithm
            List<LngLat> path = findPathUsingAStar(startAndGoal.get(0), startAndGoal.get(1));
            if (path == null || path.isEmpty()) {
                return new ResponseEntity<>("No path found", HttpStatus.BAD_REQUEST);
            }

            //formats path so that it can be used in GeoJSON
            String geoJsonPath = formatPathToGeoJson(path);

            return ResponseEntity.ok(geoJsonPath);
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private List<LngLat> setupForAStarSearch(Order order) {
        Pizza[] orderPizzas = order.getPizzasInOrder();
        String pizzaName = orderPizzas[0].getName();

        //searches through the retrieved restaurants to find which restaurant contains a pizza from the order
        Optional<Restaurant> optionalRestaurant = restaurantService.fetchRestaurants().stream()
                .filter(restaurant -> restaurant.getMenu().stream().anyMatch(menuItem -> menuItem.getName()
                        .equals(pizzaName))).findFirst();
        if (optionalRestaurant.isEmpty()) {
            throw new IllegalArgumentException("No matching restaurant found for the order.");
        }
        Restaurant orderRestaurant = optionalRestaurant.get();

        LngLat startingPos = orderRestaurant.getLocation();
        LngLat goalPos = new LngLat(-3.186874, 55.944494);

        return new ArrayList<>(Arrays.asList(startingPos, goalPos));
    }

    public List<LngLat> findPathUsingAStar(LngLat startPosition, LngLat goalPosition) {
        //priority queue sorts nodes by the estimated total cost
        PriorityQueue<Node> openList = new PriorityQueue<>(Comparator.comparingDouble(Node::getFCost)); //open list contains nodes to be considered
        List<Node> closedList = new ArrayList<>(); //closed list contains nodes that have already been considered

        List<NamedRegion> noFlyZones = noFlyZoneService.fetchNoFlyZones();
        NamedRegion centralArea = centralAreaService.fetchCentralArea();

        //turns true once the path enters the central area
        boolean inCentralArea = false;

        //setup start and end nodes
        Node startNode = new Node(startPosition, null, 0, 0);
        Node goalNode = new Node(goalPosition, null, 0, 0);
        openList.add(startNode);

        //visitedPositions contains positions that have already been visited so the path does not backtrack
        Set<LngLat> visitedPositions = new HashSet<>();

        whileLoop:
        while (!openList.isEmpty()) {
            //gets the first (sorted) node from openList
            Node currentNode = openList.poll();
            if (currentNode == null) {
                continue;
            }
            LngLat currentPosition = currentNode.getPosition();

            //makes sure the position of the node has not already been visited
            for (LngLat visited : visitedPositions) {
                if (Objects.equals(visited.getLng(), currentNode.getPosition().getLng()) &&
                        Objects.equals(visited.getLat(), currentNode.getPosition().getLat())) {
                    openList.remove(currentNode);
                    continue whileLoop;
                }
            }

            //creates the path if current position is close to the end
            LngLatPair currentAndEnd = new LngLatPair(currentNode.getPosition(), goalNode.getPosition());
            if (Objects.requireNonNull(positioningController.getIsCloseTo(currentAndEnd).getBody()).equals(true)) {
                List<LngLat> path = new ArrayList<>();
                Node current = currentNode;
                while (current != null) {
                    path.add(current.getPosition());
                    current = current.getParent();
                }
                Collections.reverse(path);
                return path;
            }

            //now node is being considered, make sure it is not considered in the future
            closedList.add(currentNode);
            visitedPositions.add(currentNode.getPosition());

            //get children of current node to be considered
            List<LngLat> childrenNodes = getNeighbours(currentNode.getPosition());

            outerForLoop:
            for (LngLat childPosition : childrenNodes) {
                Node childNode = new Node(childPosition, currentNode, 0, 0);

                //excludes child if it has already been considered
                for (Node closedChild : closedList) {
                    if (childNode.equals(closedChild)) {
                        continue outerForLoop;
                    }
                }

                //excludes child if its position has already been considered
                if (visitedPositions.contains(childPosition)) {
                    continue;
                }

                //exclude child if it leaves the central area
                boolean isChildInCentralArea = positioningController.isPointInPolygon(
                        childPosition, centralArea.getVertices());
                if (!isChildInCentralArea && inCentralArea) {
                    continue;
                }

                for (NamedRegion noFlyZone : noFlyZones) {
                    //exclude child if it is in a no-fly zone
                    if (positioningController.isPointInPolygon(childPosition, noFlyZone.getVertices())) {
                        continue outerForLoop;
                    }
                    //exclude child if its path from the current node goes through a no-fly zone.
                    if (doLinesIntersect(noFlyZone, currentPosition, childPosition)) {
                        continue outerForLoop;
                    }
                }

                childNode.setGCost(currentNode.getGCost() + 0.00015); //cost to get to child position
                childNode.setHCost(calculateHeuristic(childPosition, goalPosition)); //estimated cost to get to the end
                childNode.setFCost(childNode.getGCost() + childNode.getHCost()); //estimated total cost

                if (isChildInCentralArea) {
                    //only nodes in the central area will be considered from now on
                    inCentralArea = true;
                }

                openList.add(childNode);
            }
        }
        return null;
    }

    private List<LngLat> getNeighbours(LngLat position) {
        List<LngLat> neighbours = new ArrayList<>();
        double moveDistance = 0.00015;

        //16 compass directions
        List<Double> allowedAngles = Arrays.asList(0.0, 22.5, 45.0, 67.5,
                90.0, 112.5, 135.0, 157.5,
                180.0, 202.5, 225.0, 247.5,
                270.0, 292.5, 315.0, 337.5);

        //creates 16 neighbours based on move distance and the above angles
        for (double angle : allowedAngles) {
            LngLat neighbour = positioningController.calculateNextPosition(
                    position.getLng(), position.getLat(), moveDistance, angle);
            neighbours.add(neighbour);
        }

        return neighbours;
    }

    public double calculateHeuristic(LngLat from, LngLat to) {
        //Euclidean distance with a coefficient of 2
        return 2 * Math.sqrt(Math.pow(from.getLng() - to.getLng(), 2) + Math.pow(from.getLat() - to.getLat(), 2));
    }

    public boolean doLinesIntersect(NamedRegion noFlyZone, LngLat currentPosition, LngLat childPosition){
        List<LngLat> noFlyZoneVertices = noFlyZone.getVertices();
        int n = noFlyZoneVertices.size();

        //converts positions to a suitable format
        Coordinate[] recentLineCoords = {
                new Coordinate(currentPosition.getLng(), currentPosition.getLat()),
                new Coordinate(childPosition.getLng(), childPosition.getLat())};
        GeometryFactory geometryFactory = new GeometryFactory();

        //creates line from coordinates of current and child positions
        LineString line1 = geometryFactory.createLineString(recentLineCoords);

        //loops through adjacent vertices of no-fly zone
        for (int i = 0, j = n - 1; i < n; j = i++) {
            LngLat pi = noFlyZoneVertices.get(i);
            LngLat pj = noFlyZoneVertices.get(j);

            Coordinate[] nfzEdgeCoords = {new Coordinate(pi.getLng(), pi.getLat()),
                    new Coordinate(pj.getLng(), pj.getLat())};

            //creates line of a no-fly zone edge
            LineString line2 = geometryFactory.createLineString(nfzEdgeCoords);

            if (line1.intersects(line2)) {
                return true;
            }
        }
        return false;
    }

    private String formatPathToGeoJson(List<LngLat> path){
        ObjectMapper mapper = new ObjectMapper();

        ObjectNode geoJson = mapper.createObjectNode();
        geoJson.put("type", "FeatureCollection");

        ArrayNode features = mapper.createArrayNode();

        ObjectNode feature = mapper.createObjectNode();
        feature.put("type", "Feature");

        ObjectNode geometry = mapper.createObjectNode();
        geometry.put("type", "LineString");
        ArrayNode lineCoordinates = mapper.createArrayNode();

        for (LngLat currentPosition : path) {
            ArrayNode point = mapper.createArrayNode();
            point.add(currentPosition.getLng());
            point.add(currentPosition.getLat());
            lineCoordinates.add(point);
        }

        geometry.set("coordinates", lineCoordinates);
        feature.set("geometry", geometry);
        feature.set("properties", mapper.createObjectNode());

        features.add(feature);
        geoJson.set("features", features);

        String geoJsonString;
        try {
            geoJsonString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(geoJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        return geoJsonString;
    }

}
