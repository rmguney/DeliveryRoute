import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

// Class representing buildings
class Building {
    int ID;
    double x;
    double y;

    // Constructor for building properties
    public Building(int ID, double x, double y) {
        this.ID = ID;
        this.x = x;
        this.y = y;
    }
}

// Main class representing the MigrosDelivery application, extending JFrame for graph
public class MigrosDelivery extends JFrame {

    // Constructor for JFrame
    public MigrosDelivery() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 600);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // Method to find the shortest route using a nearest-neighbor algorithm
    public static List<Integer> findShortestRoute(List<Building> buildings) {
        List<Integer> route = new ArrayList<>();
        List<Building> remainingBuildings = new ArrayList<>(buildings);

        Building currentBuilding = remainingBuildings.remove(0);
        route.add(currentBuilding.ID);

        while (!remainingBuildings.isEmpty()) {
            Building nearestBuilding = findNearestBuilding(currentBuilding, remainingBuildings);
            route.add(nearestBuilding.ID);
            remainingBuildings.remove(nearestBuilding);
            currentBuilding = nearestBuilding;
        }

        route.add(route.get(0));

        return route;
    }

    // Method to calculate the total distance of a route
    public static double calculateDistance(List<Integer> route, List<Building> buildings) {
        double distance = 0;

        for (int i = 0; i < route.size() - 1; i++) {
            Building start = getBuildingById(route.get(i), buildings);
            Building end = getBuildingById(route.get(i + 1), buildings);
            distance += calculateDistance(start, end);
        }

        return distance;
    }

    // Method to draw the route on graph
    public static void drawRoute(Graphics g, List<Building> buildings, List<Integer> route) {
        Graphics2D g2d = (Graphics2D) g;
        int textOffsetX = 10;
        int textOffsetY = 10;

        // Draw buildings
        g2d.setColor(Color.GRAY);
        for (Building building : buildings) {
            int x = (int) (building.x * 500);
            int y = (int) (building.y * 500);
            g2d.fillOval(x - 5, y - 5, 10, 10);
            g2d.drawString(Integer.toString(building.ID), x + textOffsetX, y + textOffsetY);
        }

        // Draw Migros
        Building migros = getBuildingById(route.get(0), buildings);
        g2d.setColor(Color.BLUE);
        int migrosX = (int) (migros.x * 500);
        int migrosY = (int) (migros.y * 500);
        g2d.fillOval(migrosX - 8, migrosY - 8, 16, 16);
        g2d.drawString("Migros", migrosX + textOffsetX, migrosY + textOffsetY);

        // Draw route lines
        g2d.setColor(Color.BLUE);
        for (int i = 0; i < route.size() - 1; i++) {
            Building start = getBuildingById(route.get(i), buildings);
            Building end = getBuildingById(route.get(i + 1), buildings);

            int x1 = (int) (start.x * 500);
            int y1 = (int) (start.y * 500);
            int x2 = (int) (end.x * 500);
            int y2 = (int) (end.y * 500);

            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    // Method to find the nearest building to the current building
    private static Building findNearestBuilding(Building current, List<Building> remainingBuildings) {
        Building nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Building building : remainingBuildings) {
            double distance = calculateDistance(current, building);
            if (distance < minDistance) {
                minDistance = distance;
                nearest = building;
            }
        }

        return nearest;
    }

    // Method to calculate the distance between two buildings
    private static double calculateDistance(Building building1, Building building2) {
        return Math.sqrt(Math.pow(building1.x - building2.x, 2) + Math.pow(building1.y - building2.y, 2));
    }

    // Method to get a building from the list based on its ID
    private static Building getBuildingById(int ID, List<Building> buildings) {
        for (Building building : buildings) {
            if (building.ID == ID) {
                return building;
            }
        }
        return null;
    }

    // Method to read building information from a file and create a list of buildings
    private static List<Building> readBuildingsFromFile(String fileName) {
        List<Building> buildings = new ArrayList<>();
        Building migros = null;

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            int id = 1;

            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                double x = Double.parseDouble(parts[0]);
                double y = Double.parseDouble(parts[1]);

                if (parts.length > 2 && parts[2].trim().equalsIgnoreCase("Migros")) {
                    migros = new Building(id, x, y);
                } else {
                    buildings.add(new Building(id, x, y));
                }

                id++;
            }

            if (migros != null) {
                List<Building> buildingsToRemove = new ArrayList<>();

                for (Building building : buildings) {
                    if (building.ID == migros.ID) {
                        buildingsToRemove.add(building);
                        break;
                    }
                }

                buildings.removeAll(buildingsToRemove);
                buildings.add(0, migros);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return buildings;
    }

    // Main method to execute the application
    public static void main(String[] args) {
        // Change according to file path and file name
        String filePath = "inputs/input01.txt";

        // Read building information from the file
        List<Building> buildings = readBuildingsFromFile(filePath);

        // Find the shortest route and calculate the total distance
        List<Integer> shortestRoute = findShortestRoute(buildings);
        double distance = calculateDistance(shortestRoute, buildings);

        // Print the results to the console
        System.out.println("Shortest Route: " + shortestRoute);
        System.out.println("Distance: " + distance);

        // Set up Swing graph
        SwingUtilities.invokeLater(() -> {
            MigrosDelivery frame = new MigrosDelivery();
            frame.add(new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    // Draw the optimized route on graph
                    drawRoute(g, buildings, shortestRoute);
                }
            });
            frame.revalidate();
            frame.repaint();
        });
    }
}
