import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.Date; 
public class BusReservationApp {
    private static final String DB_URL = "jdbc:mysql://localhost:3306/busapp";
    private static final String USER = "root";
    private static final String PASS = "alonelover1201";

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        while (true) {
            System.out.println("\n1. Admin Login (Add Bus)");
            System.out.println("2. View Buses");
            System.out.println("3. Book Ticket");
            System.out.println("4. Cancel Booking");
            System.out.println("5. Check Available Seats");
            System.out.println("6. Exit");
            System.out.print("Choose: ");
            int choice = sc.nextInt();
            sc.nextLine();

            switch (choice) {
                case 1 -> handleAdmin(sc);
                case 2 -> viewBuses();
                case 3 -> bookTicket(sc, sdf);
                case 4 -> cancelBooking(sc);
                case 5 -> checkAvailableSeats(sc, sdf);
                case 6 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option.");
            }
        }
    }

    // ---------- Admin Logic ----------
    private static void handleAdmin(Scanner sc) {
        System.out.print("Enter Admin Password: ");
        String password = sc.nextLine();
        if (!password.equals("admin123")) {
            System.out.println("❌ Incorrect password.");
            return;
        }

        try (Connection conn = getConnection()) {
            System.out.print("Bus No: ");
            int busNo = sc.nextInt();
            System.out.print("AC (true/false): ");
            boolean ac = sc.nextBoolean();
            System.out.print("Capacity: ");
            int capacity = sc.nextInt();
            sc.nextLine(); // consume newline
            System.out.print("Driver Name: ");
            String driver = sc.nextLine();
            System.out.print("From: ");
            String from = sc.nextLine();
            System.out.print("To: ");
            String to = sc.nextLine();
            System.out.print("Duration: ");
            String duration = sc.nextLine();

            PreparedStatement stmt = conn.prepareStatement("INSERT INTO buses VALUES (?, ?, ?, ?, ?, ?, ?)");
            stmt.setInt(1, busNo);
            stmt.setBoolean(2, ac);
            stmt.setInt(3, capacity);
            stmt.setString(4, driver);
            stmt.setString(5, from);
            stmt.setString(6, to);
            stmt.setString(7, duration);
            stmt.executeUpdate();
            System.out.println("✅ Bus added successfully.");
        } catch (Exception e) {
            System.out.println("Error adding bus: " + e.getMessage());
        }
    }

    // ---------- View Buses ----------
    private static void viewBuses() {
        try (Connection conn = getConnection()) {
            ResultSet rs = conn.createStatement().executeQuery("SELECT * FROM buses");
            while (rs.next()) {
                System.out.println("Bus No: " + rs.getInt(1) + ", AC: " + rs.getBoolean(2) + ", Cap: " + rs.getInt(3) +
                        ", Driver: " + rs.getString(4) + ", From: " + rs.getString(5) +
                        ", To: " + rs.getString(6) + ", Dur: " + rs.getString(7));
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving buses: " + e.getMessage());
        }
    }

    // ---------- Book Ticket ----------
    private static void bookTicket(Scanner sc, SimpleDateFormat sdf) {
        try (Connection conn = getConnection()) {
            System.out.print("Name: "); String name = sc.nextLine();
            System.out.print("Email: "); String email = sc.nextLine();
            System.out.print("Phone: "); String phone = sc.nextLine();
            System.out.print("Bus No: "); int busNo = sc.nextInt(); sc.nextLine();
            System.out.print("Date (dd/MM/yyyy): ");
            Date date = sdf.parse(sc.nextLine());
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            ResultSet seatCheck = conn.createStatement().executeQuery("SELECT COUNT(*) FROM bookings WHERE bus_no=" + busNo + " AND travel_date='" + sqlDate + "'");
            seatCheck.next();
            int booked = seatCheck.getInt(1);

            ResultSet capacityRS = conn.createStatement().executeQuery("SELECT capacity FROM buses WHERE bus_no=" + busNo);
            if (capacityRS.next()) {
                int capacity = capacityRS.getInt(1);
                if (booked < capacity) {
                    PreparedStatement stmt = conn.prepareStatement("INSERT INTO bookings (passenger_name, email, phone, bus_no, travel_date, seat_no) VALUES (?, ?, ?, ?, ?, ?)");
                    stmt.setString(1, name);
                    stmt.setString(2, email);
                    stmt.setString(3, phone);
                    stmt.setInt(4, busNo);
                    stmt.setDate(5, sqlDate);
                    stmt.setInt(6, booked + 1);
                    stmt.executeUpdate();
                    System.out.println("✅ Booking Confirmed! Seat No: " + (booked + 1));
                } else {
                    System.out.println("❌ No seats available.");
                }
            } else {
                System.out.println("❌ Bus not found.");
            }
        } catch (Exception e) {
            System.out.println("Booking error: " + e.getMessage());
        }
    }

    // ---------- Cancel Booking ----------
    private static void cancelBooking(Scanner sc){
        System.out.print("Enter email to cancel: ");
        String email = sc.nextLine();
        try (Connection conn = getConnection()) {
            PreparedStatement stmt = conn.prepareStatement("DELETE FROM bookings WHERE email=?");
            stmt.setString(1, email);
            int rows = stmt.executeUpdate();
            System.out.println(rows > 0 ? "✅ Booking canceled." : "❌ No booking found.");
        } catch (Exception e) {
            System.out.println("Cancel error: " + e.getMessage());
        }
    }

    // ---------- Check Available Seats ----------
    private static void checkAvailableSeats(Scanner sc, SimpleDateFormat sdf) {
        try (Connection conn = getConnection()) {
            System.out.print("Bus No: ");
            int busNo = sc.nextInt(); sc.nextLine();
            System.out.print("Date (dd/MM/yyyy): ");
            Date date = sdf.parse(sc.nextLine());
            java.sql.Date sqlDate = new java.sql.Date(date.getTime());

            ResultSet rs1 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM bookings WHERE bus_no=" + busNo + " AND travel_date='" + sqlDate + "'");
            rs1.next();
            int booked = rs1.getInt(1);

            ResultSet rs2 = conn.createStatement().executeQuery("SELECT capacity FROM buses WHERE bus_no=" + busNo);
            if (rs2.next()) {
                int cap = rs2.getInt(1);
                System.out.println("✅ Available Seats: " + (cap - booked));
            } else {
                System.out.println("❌ Bus not found.");
            }
        } catch (Exception e) {
            System.out.println("Availability error: " + e.getMessage());
        }
    }

    // ---------- Database Connection ----------
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, USER, PASS);
    }
}
