import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;

public class FlightBookingSystem extends JFrame {

    private JTabbedPane tabbedPane;
    private JTextField nameField, priceField;
    private JComboBox<String> sourceCombo, destinationCombo, flightCombo, dateCombo, timeCombo;
    private JTextField searchField, cancelField;
    private JTextArea adminArea;
    private File bookingFile = new File("bookings.txt");

    // Cities and Airlines
    private String[] indianCities = {
        "Mumbai", "Delhi", "Bangalore", "Hyderabad", "Chennai", "Kolkata",
        "Pune", "Ahmedabad", "Jaipur", "Lucknow", "Bhubaneswar", "Goa",
        "Coimbatore", "Chandigarh", "Nagpur", "Thiruvananthapuram", "Patna",
        "Amritsar", "Indore", "Guwahati"
    };
    private String[] airlines = { "Indigo", "Air India", "SpiceJet", "Vistara", "GoAir" };
    private String[] dates = { "2025-10-16", "2025-10-17", "2025-10-18", "2025-10-19", "2025-10-20", "2025-10-21" };
    private String[] times = { "06:00", "09:00", "12:00", "15:00", "18:00", "21:00" };

    public FlightBookingSystem() {
        setTitle("✈ Flight Booking System");
        setSize(800, 550);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Ensure booking file exists
        try {
            if (!bookingFile.exists()) bookingFile.createNewFile();
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Cannot create booking file!");
        }

        boolean isAdmin = showAdminLogin();
        tabbedPane = new JTabbedPane();

        // ===== Book Ticket Tab =====
        JPanel bookPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        bookPanel.setBorder(BorderFactory.createTitledBorder("Book Your Flight"));

        bookPanel.add(new JLabel("Passenger Name:"));
        nameField = new JTextField();
        bookPanel.add(nameField);

        // Source Combo
        bookPanel.add(new JLabel("Starting Location:"));
        sourceCombo = new JComboBox<>(indianCities);
        bookPanel.add(sourceCombo);

        // Destination Combo
        bookPanel.add(new JLabel("Destination:"));
        destinationCombo = new JComboBox<>(indianCities);
        bookPanel.add(destinationCombo);

        // Prevent same city in destination
        sourceCombo.addActionListener(e -> updateDestinationOptions());

        // Flight Name
        bookPanel.add(new JLabel("Flight Name:"));
        flightCombo = new JComboBox<>(airlines);
        bookPanel.add(flightCombo);

        // Date
        bookPanel.add(new JLabel("Flight Date:"));
        dateCombo = new JComboBox<>(dates);
        bookPanel.add(dateCombo);

        // Time
        bookPanel.add(new JLabel("Flight Time:"));
        timeCombo = new JComboBox<>(times);
        bookPanel.add(timeCombo);

        // Price Field
        bookPanel.add(new JLabel("Price:"));
        priceField = new JTextField();
        priceField.setEditable(false);
        bookPanel.add(priceField);

        JButton bookButton = new JButton("Book Ticket");
        JButton clearButton = new JButton("Clear");
        bookPanel.add(bookButton);
        bookPanel.add(clearButton);

        // ===== Listeners =====
        bookButton.addActionListener(e -> bookTicket());
        clearButton.addActionListener(e -> clearBookingFields());

        // Auto-update price
        sourceCombo.addActionListener(e -> updatePrice());
        destinationCombo.addActionListener(e -> updatePrice());
        flightCombo.addActionListener(e -> updatePrice());
        dateCombo.addActionListener(e -> updatePrice());
        timeCombo.addActionListener(e -> updatePrice());

        // ===== Manage Tab =====
        JPanel managePanel = new JPanel(new GridLayout(2, 3, 10, 10));
        managePanel.setBorder(BorderFactory.createTitledBorder("Manage Bookings"));

        managePanel.add(new JLabel("Search by Name or Booking ID:"));
        searchField = new JTextField();
        managePanel.add(searchField);
        JButton searchBtn = new JButton("Search");
        managePanel.add(searchBtn);

        managePanel.add(new JLabel("Cancel Booking ID:"));
        cancelField = new JTextField();
        managePanel.add(cancelField);
        JButton cancelBtn = new JButton("Cancel Booking");
        managePanel.add(cancelBtn);

        searchBtn.addActionListener(e -> {
            String key = searchField.getText().trim();
            if (!key.isEmpty()) searchBooking(key);
        });

        cancelBtn.addActionListener(e -> {
            String key = cancelField.getText().trim();
            if (!key.isEmpty()) cancelBooking(key);
        });

        // ===== Admin Tab =====
        JPanel adminPanel = new JPanel(new BorderLayout());
        adminPanel.setBorder(BorderFactory.createTitledBorder("Admin Panel - All Bookings"));

        adminArea = new JTextArea();
        adminArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(adminArea);
        adminPanel.add(scrollPane, BorderLayout.CENTER);

        JButton refreshBtn = new JButton("Refresh Bookings");
        adminPanel.add(refreshBtn, BorderLayout.SOUTH);
        refreshBtn.addActionListener(e -> loadAllBookings());

        // Add Tabs
        tabbedPane.addTab("Book Ticket", bookPanel);
        tabbedPane.addTab("Manage (Search/Cancel)", managePanel);
        if (isAdmin) tabbedPane.addTab("Admin", adminPanel);

        add(tabbedPane);
        setVisible(true);

        // Initialize destination options and price
        updateDestinationOptions();
        updatePrice();
    }

    // ===== Admin Login =====
    private boolean showAdminLogin() {
        JPanel panel = new JPanel(new GridLayout(2, 2));
        JTextField userField = new JTextField();
        JPasswordField passField = new JPasswordField();
        panel.add(new JLabel("Username:"));
        panel.add(userField);
        panel.add(new JLabel("Password:"));
        panel.add(passField);

        int option = JOptionPane.showConfirmDialog(null, panel, "Admin Login (cancel to skip)",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword()).trim();
            if (user.equals("admin") && pass.equals("1234")) {
                JOptionPane.showMessageDialog(null, "✅ Admin Login Successful!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "❌ Invalid Admin Credentials! Admin tab will be hidden.");
            }
        }
        return false;
    }

    // ===== Update Destination Options =====
    private void updateDestinationOptions() {
        String selectedSource = (String) sourceCombo.getSelectedItem();
        if (selectedSource == null) return;

        String currentDest = (String) destinationCombo.getSelectedItem();
        destinationCombo.removeAllItems();
        for (String city : indianCities) {
            if (!city.equals(selectedSource)) destinationCombo.addItem(city);
        }

        if (currentDest != null && !currentDest.equals(selectedSource)) {
            destinationCombo.setSelectedItem(currentDest);
        } else {
            destinationCombo.setSelectedIndex(0);
        }
        updatePrice();
    }

    // ===== Update Price =====
    private void updatePrice() {
        String source = (String) sourceCombo.getSelectedItem();
        String dest = (String) destinationCombo.getSelectedItem();
        String flight = (String) flightCombo.getSelectedItem();
        String time = (String) timeCombo.getSelectedItem();

        if (source == null || dest == null || source.equals(dest)) {
            priceField.setText("");
            return;
        }

        int price = 1000; // base price
        price += Math.abs(source.hashCode()) % 500;
        price += Math.abs(dest.hashCode()) % 500;

        switch (flight) {
            case "Indigo": price += 200; break;
            case "Air India": price += 150; break;
            case "SpiceJet": price += 180; break;
            case "Vistara": price += 220; break;
            case "GoAir": price += 170; break;
        }

        switch (time) {
            case "06:00": price += 100; break;
            case "09:00": price += 120; break;
            case "12:00": price += 150; break;
            case "15:00": price += 130; break;
            case "18:00": price += 180; break;
            case "21:00": price += 160; break;
        }

        priceField.setText("₹ " + price);
    }

    // ===== Book Ticket =====
    private void bookTicket() {
        String name = nameField.getText().trim();
        String source = (String) sourceCombo.getSelectedItem();
        String dest = (String) destinationCombo.getSelectedItem();
        String flight = (String) flightCombo.getSelectedItem();
        String date = (String) dateCombo.getSelectedItem();
        String time = (String) timeCombo.getSelectedItem();
        String price = priceField.getText();

        if (name.isEmpty() || source.isEmpty() || dest.isEmpty() || flight.isEmpty() || price.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all fields! Price will auto-update.");
            return;
        }

        String bookingId = "BKG" + new Random().nextInt(9999);

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(bookingFile, true))) {
            bw.write("Booking ID: " + bookingId); bw.newLine();
            bw.write("Name: " + name); bw.newLine();
            bw.write("From: " + source); bw.newLine();
            bw.write("To: " + dest); bw.newLine();
            bw.write("Flight: " + flight); bw.newLine();
            bw.write("Date: " + date); bw.newLine();
            bw.write("Time: " + time); bw.newLine();
            bw.write("Price: " + price); bw.newLine();
            bw.write("---------------------"); bw.newLine();

            JOptionPane.showMessageDialog(this,
                "✅ Ticket Booked!\nBooking ID: " + bookingId +
                        "\nName: " + name +
                        "\nFrom: " + source +
                        "\nTo: " + dest +
                        "\nFlight: " + flight +
                        "\nDate: " + date +
                        "\nTime: " + time +
                        "\nPrice: " + price
            );
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this, "Error saving booking.");
        }
    }

    private void clearBookingFields() {
        nameField.setText("");
        sourceCombo.setSelectedIndex(0);
        updateDestinationOptions();
        flightCombo.setSelectedIndex(0);
        dateCombo.setSelectedIndex(0);
        timeCombo.setSelectedIndex(0);
        priceField.setText("");
    }

    // ===== Search Booking =====
    private void searchBooking(String key) {
        boolean found = false;
        StringBuilder result = new StringBuilder();

        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.contains(key)) {
                    result.append(line).append("\n");
                    found = true;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file.");
        }

        if (found)
            JOptionPane.showMessageDialog(this, result.toString(), "Booking Details", JOptionPane.INFORMATION_MESSAGE);
        else
            JOptionPane.showMessageDialog(this, "No booking found for: " + key);
    }

    // ===== Cancel Booking =====
    private void cancelBooking(String key) {
        // First, check if key is a Booking ID or a Name
        ArrayList<String> matchedBookingIds = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            String currentBookingId = "";
            boolean inBooking = false;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Booking ID: ")) {
                    currentBookingId = line.substring(12).trim();
                    inBooking = true;
                }
                if (inBooking && line.startsWith("Name: ")) {
                    String name = line.substring(6).trim();
                    if (name.equalsIgnoreCase(key)) {
                        matchedBookingIds.add(currentBookingId);
                    }
                }
                if (line.equals("---------------------")) {
                    inBooking = false;
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading booking file.");
            return;
        }

        if (matchedBookingIds.size() > 1 || (matchedBookingIds.size() == 1 && !key.startsWith("BKG"))) {
            // Show all matching Booking IDs for this name
            StringBuilder msg = new StringBuilder("Booking IDs for name '" + key + "':\n");
            for (String id : matchedBookingIds) {
                msg.append(id).append("\n");
            }
            JOptionPane.showMessageDialog(this, msg.toString() + "Enter Booking ID in field to cancel.");
            return; // do not cancel yet
        }

        // Now proceed to cancel using Booking ID
        File tempFile = new File("temp.txt");
        boolean deleted = false;
        String bookingDetails = "";

        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile));
             BufferedWriter bw = new BufferedWriter(new FileWriter(tempFile))) {

            String line;
            boolean skip = false;

            while ((line = br.readLine()) != null) {
                if (line.startsWith("Booking ID: ") && line.contains(key)) {
                    deleted = true;
                    bookingDetails += line + "\n"; // store Booking ID
                    skip = true;
                    continue;
                }
                if (skip) {
                    bookingDetails += line + "\n"; // store full booking details
                    if (line.equals("---------------------")) {
                        skip = false;
                    }
                    continue;
                }
                bw.write(line);
                bw.newLine();
            }

        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error processing file.");
            return;
        }

        bookingFile.delete();
        tempFile.renameTo(bookingFile);

        if (deleted)
            JOptionPane.showMessageDialog(this, "❌ Booking cancelled successfully!\n" + bookingDetails);
        else
            JOptionPane.showMessageDialog(this, "Booking ID not found!");
    }

    // ===== Load all bookings =====
    private void loadAllBookings() {
        StringBuilder allBookings = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new FileReader(bookingFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                allBookings.append(line).append("\n");
            }
            adminArea.setText(allBookings.toString());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error reading file for admin.");
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new FlightBookingSystem());
    }
}
