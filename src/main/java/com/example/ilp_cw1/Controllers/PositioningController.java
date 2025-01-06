package com.example.ilp_cw1.Controllers;

import com.example.ilp_cw1.DTO.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
public class PositioningController {

    @GetMapping("/isAlive")
    public boolean isAlive(){
        return true;
    }

    @GetMapping("/uuid")
    public ResponseEntity<String> uuid() {
        return new ResponseEntity<>("s2330567", HttpStatus.OK);
    }

    @PostMapping("/distanceTo")
    public ResponseEntity<Object> getDistanceTo(@RequestBody LngLatPair request) {
        try {
            LngLat position1 = request.getPosition1();
            LngLat position2 = request.getPosition2();

            if (position1 == null || position2 == null ||
                    position1.getLng() == null || position1.getLat() == null ||
                    position2.getLng() == null || position2.getLat() == null) {
                return ResponseEntity.badRequest().body("Longitude and latitude must be provided for both positions.");
            }

            if (!isValidCoordinates(position1.getLng(), position1.getLat()) ||
                    !isValidCoordinates(position2.getLng(), position2.getLat())) {
                return ResponseEntity.badRequest().body("Invalid coordinates.");
            }

            double distance = calculateDistance(position1, position2);

            return new ResponseEntity<>(distance, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/isCloseTo")
    public ResponseEntity<Object> getIsCloseTo(@RequestBody LngLatPair request) {
        try{
            LngLat position1 = request.getPosition1();
            LngLat position2 = request.getPosition2();

            if (!validatePositions(position1, position2)) {
                return ResponseEntity.badRequest().body("Invalid Positions");
            }

            if (calculateDistance(position1, position2) < 0.00015){
                return new ResponseEntity<>(true, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(false, HttpStatus.OK);
            }
        }
        catch(Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/nextPosition")
    public ResponseEntity<Object> getNextPosition(@RequestBody NextPosition request) {
        try {
            LngLat start = request.getStart();
            double angle = request.getAngle();

            if (start == null || start.getLng() == null || start.getLat() == null
                    || angle < 0 || angle > 360 || !(angle % 22.5 == 0) ) {
                return ResponseEntity.badRequest().body("Start position and angle must be valid.");
            }

            double startLng = start.getLng();
            double startLat = start.getLat();

            if (!isValidCoordinates(startLng, startLat)) {
                return ResponseEntity.badRequest().body("Invalid coordinates.");
            }

            double distance = 0.00015;
            LngLat newCoordinates = calculateNextPosition(startLng, startLat, distance, angle);

            if (!isValidCoordinates(newCoordinates.getLng(), newCoordinates.getLat())) {
                return ResponseEntity.badRequest().body("Travelled out of this world.");
            }

            return ResponseEntity.ok(newCoordinates);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/isInRegion")
    public ResponseEntity<Boolean> isInRegion(@RequestBody NamedRegionAndPoint request) {
        try {
            LngLat point = request.getPosition();
            List<LngLat> polygon = request.getRegion().getVertices();

            if (point == null || point.getLng() == null || point.getLat() == null) {
                return ResponseEntity.badRequest().body(false);
            }

            if (polygon == null || polygon.size() < 3) {
                return ResponseEntity.badRequest().body(false);
            }

            if (!isValidCoordinates(point.getLng(), point.getLat())) {
                return ResponseEntity.badRequest().body(false);
            }

            for (LngLat position : polygon) {
                if (position == null || position.getLng() == null || position.getLat() == null) {
                    return ResponseEntity.badRequest().body(false);
                }
                if (!isValidCoordinates(position.getLng(), position.getLat())) {
                    return ResponseEntity.badRequest().body(false);
                }
            }

            if (Objects.equals(polygon.get(0).getLat(), polygon.get(polygon.size() - 1).getLat()) &&
                    Objects.equals(polygon.get(0).getLng(), polygon.get(polygon.size() - 1).getLng())) {
                return ResponseEntity.badRequest().body(false);
            }

            for (int i = 0; i < polygon.size(); i++) {
                if (isCollinear(polygon.get(i).getLat(), polygon.get(i).getLng(), polygon.get(i+1).getLat(),
                        polygon.get(i+1).getLng(), polygon.get(i+2).getLat(), polygon.get(i+2).getLng())){
                    return ResponseEntity.badRequest().body(false);
                }
            }

            boolean isInside = isPointInPolygon(point, polygon);
            return new ResponseEntity<>(isInside, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private boolean isCollinear(Double lat1, Double lng1, Double lat2, Double lng2, Double lat3, Double lng3) {
        return !(Math.abs((lat2 - lat1) * (lng3 - lng2) - (lat3 - lat2) * (lng2 - lng1)) > 1e-10);
    }

    public boolean isPointInPolygon(LngLat point, List<LngLat> polygon) {
        int n = polygon.size();
        boolean inside = false; //

        for (int i = 0, j = n - 1; i < n; j = i++) {
            LngLat pi = polygon.get(i);
            LngLat pj = polygon.get(j);

            double lati = pi.getLat();
            double lngi = pi.getLng();
            double latj = pj.getLat();
            double lngj = pj.getLng();

            if (point.getLat() == lati && point.getLng() == lngi) {
                return true;
            }

            if (isPointOnLine(lngi, lati, lngj, latj, point.getLng(), point.getLat())) {
                return true;
            }

            boolean intersect = ((lati > point.getLat()) != (latj > point.getLat())) &&
                    (point.getLng() < (lngj - lngi) * (point.getLat() - lati) / (latj - lati) + lngi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    public boolean isPointOnLine(double x1, double y1, double x2, double y2, double x, double y) {
        double crossProduct = (y - y1) * (x2 - x1) - (y2 - y1) * (x - x1);

        if (Math.abs(crossProduct) > 1e-10) {
            return false;
        }

        boolean withinXBounds = (x >= Math.min(x1, x2) && x <= Math.max(x1, x2));
        boolean withinYBounds = (y >= Math.min(y1, y2) && y <= Math.max(y1, y2));

        return withinXBounds && withinYBounds;
    }


    public double calculateDistance(LngLat position1, LngLat position2) {
        double lngDiff = position1.getLng() - position2.getLng();
        double latDiff = position1.getLat() - position2.getLat();
        return Math.sqrt(Math.pow(lngDiff, 2) + Math.pow(latDiff, 2));
    }

    public LngLat calculateNextPosition(double lng1, double lat1, double distance, double angle) {
        double angleRadians = Math.toRadians(angle);
        double lngChange = distance * Math.cos(angleRadians);
        double latChange = distance * Math.sin(angleRadians);

        double newLng = lng1 + lngChange;
        double newLat = lat1 + latChange;

        return new LngLat(newLng, newLat);
    }

    private boolean validatePositions(LngLat position1, LngLat position2){
        if (position1 == null || position2 == null ||
                position1.getLng() == null || position1.getLat() == null ||
                position2.getLng() == null || position2.getLat() == null) {
            return false;
        }

        return isValidCoordinates(position1.getLng(), position1.getLat()) &&
                isValidCoordinates(position2.getLng(), position2.getLat());
    }

    private boolean isValidCoordinates(double lng, double lat) {
        boolean validLongitude = lng >= -180 && lng <= 180;
        boolean validLatitude = lat >= -90 && lat <= 90;
        return validLongitude && validLatitude;
    }

}
