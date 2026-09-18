import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;

// ==========================================
// 1. DATA MODELS & ENTITIES
// ==========================================

abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String userId;
    private String name;
    private String email;
    private String passwordHash;
    private String role;

    public User(String userId, String name, String email, String passwordHash, String role) {
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getRole() { return role; }

    public abstract void displayProfile();
}

class Student extends User {
    private static final long serialVersionUID = 1L;
    private String rollNumber;
    private String roomNumber;
    private boolean messActive;

    public Student(String userId, String name, String email, String passwordHash, String rollNumber) {
        super(userId, name, email, passwordHash, "STUDENT");
        this.rollNumber = rollNumber;
        this.roomNumber = "UNASSIGNED";
        this.messActive = true;
    }

    public String getRollNumber() { return rollNumber; }
    public String getRoomNumber() { return roomNumber; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public boolean isMessActive() { return messActive; }
    public void setMessActive(boolean messActive) { this.messActive = messActive; }

    @Override
    public void displayProfile() {
        System.out.println("\n================ STUDENT PROFILE ================");
        System.out.println("User ID     : " + getUserId());
        System.out.println("Name        : " + getName());
        System.out.println("Email       : " + getEmail());
        System.out.println("Roll Number : " + rollNumber);
        System.out.println("Room Number : " + roomNumber);
        System.out.println("Mess Status : " + (messActive ? "Active" : "Inactive"));
        System.out.println("=================================================");
    }
}

class AdminWarden extends User {
    private static final long serialVersionUID = 1L;
    private String assignedBlock;

    public AdminWarden(String userId, String name, String email, String passwordHash, String assignedBlock) {
        super(userId, name, email, passwordHash, "ADMIN");
        this.assignedBlock = assignedBlock;
    }

    public String getAssignedBlock() { return assignedBlock; }
    public void setAssignedBlock(String assignedBlock) { this.assignedBlock = assignedBlock; }

    @Override
    public void displayProfile() {
        System.out.println("\n================ WARDEN PROFILE ================");
        System.out.println("Admin ID       : " + getUserId());
        System.out.println("Name           : " + getName());
        System.out.println("Email          : " + getEmail());
        System.out.println("Assigned Block : " + assignedBlock);
        System.out.println("=================================================");
    }
}

class Room implements Serializable {
    private static final long serialVersionUID = 1L;
    private String roomNumber;
    private String block;
    private int capacity;
    private List<String> occupantIds;

    public Room(String roomNumber, String block, int capacity) {
        this.roomNumber = roomNumber;
        this.block = block;
        this.capacity = capacity;
        this.occupantIds = new ArrayList<>();
    }

    public String getRoomNumber() { return roomNumber; }
    public String getBlock() { return block; }
    public int getCapacity() { return capacity; }
    public List<String> getOccupantIds() { return occupantIds; }

    public boolean isAvailable() {
        return occupantIds.size() < capacity;
    }

    public boolean addOccupant(String studentId) {
        if (isAvailable() && !occupantIds.contains(studentId)) {
            occupantIds.add(studentId);
            return true;
        }
        return false;
    }

    public boolean removeOccupant(String studentId) {
        return occupantIds.remove(studentId);
    }
}

class MessRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String studentId;
    private String month;
    private int daysAttended;
    private double dailyRate;

    public MessRecord(String studentId, String month, int daysAttended, double dailyRate) {
        this.studentId = studentId;
        this.month = month;
        this.daysAttended = daysAttended;
        this.dailyRate = dailyRate;
    }

    public String getStudentId() { return studentId; }
    public String getMonth() { return month; }
    public int getDaysAttended() { return daysAttended; }
    public double getDailyRate() { return dailyRate; }

    public double calculateTotalBill() {
        return daysAttended * dailyRate;
    }
}

// ==========================================
// 2. DATA ACCESS LAYER (DAO)
// ==========================================

interface GenericDAO<T, ID> {
    void save(ID id, T entity);
    T findById(ID id);
    List<T> findAll();
    void delete(ID id);
}

class FileGenericDAO<T extends Serializable, ID> implements GenericDAO<T, ID> {
    private final String filePath;
    private Map<ID, T> storageMap;

    public FileGenericDAO(String filePath) {
        this.filePath = filePath;
        this.storageMap = new HashMap<>();
        loadFromFile();
    }

    @Override
    public synchronized void save(ID id, T entity) {
        storageMap.put(id, entity);
        persistToFile();
    }

    @Override
    public synchronized T findById(ID id) {
        return storageMap.get(id);
    }

    @Override
    public synchronized List<T> findAll() {
        return new ArrayList<>(storageMap.values());
    }

    @Override
    public synchronized void delete(ID id) {
        storageMap.remove(id);
        persistToFile();
    }

    private void persistToFile() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath))) {
            oos.writeObject(storageMap);
        } catch (IOException e) {
            System.err.println("Persistence warning: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadFromFile() {
        File file = new File(filePath);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            storageMap = (Map<ID, T>) ois.readObject();
        } catch (Exception e) {
            storageMap = new HashMap<>();
        }
    }
}

// ==========================================
// 3. SERVICE LAYER
// ==========================================

class AuthService {
    private final GenericDAO<User, String> userDAO;

    public AuthService(GenericDAO<User, String> userDAO) {
        this.userDAO = userDAO;
    }

    public static String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return password;
        }
    }

    public void registerUser(User user) {
        userDAO.save(user.getUserId(), user);
    }

    public User login(String userId, String password) throws Exception {
        User user = userDAO.findById(userId);
        if (user == null) {
            throw new Exception("Authentication Error: User ID not found.");
        }
        String hashedInput = hashPassword(password);
        if (!user.getPasswordHash().equals(hashedInput)) {
            throw new Exception("Authentication Error: Invalid password.");
        }
        return user;
    }
}

class AllocationService {
    private final GenericDAO<Room, String> roomDAO;
    private final GenericDAO<Student, String> studentDAO;

    public AllocationService(GenericDAO<Room, String> roomDAO, GenericDAO<Student, String> studentDAO) {
        this.roomDAO = roomDAO;
        this.studentDAO = studentDAO;
    }

    public void addRoom(Room room) {
        roomDAO.save(room.getRoomNumber(), room);
    }

    public boolean allocateRoom(String studentId, String roomNumber) throws Exception {
        Student student = studentDAO.findById(studentId);
        Room room = roomDAO.findById(roomNumber);

        if (student == null) throw new Exception("Allocation Error: Student record not found.");
        if (room == null) throw new Exception("Allocation Error: Room " + roomNumber + " does not exist.");
        if (!room.isAvailable()) throw new Exception("Allocation Error: Room " + roomNumber + " is fully occupied.");

        room.addOccupant(studentId);
        student.setRoomNumber(roomNumber);

        roomDAO.save(room.getRoomNumber(), room);
        studentDAO.save(student.getUserId(), student);
        return true;
    }

    public List<Room> getAllRooms() {
        return roomDAO.findAll();
    }
}

class BillingService {
    private final GenericDAO<MessRecord, String> messDAO;

    public BillingService(GenericDAO<MessRecord, String> messDAO) {
        this.messDAO = messDAO;
    }

    public void recordAttendance(String studentId, String month, int days, double rate) {
        String key = studentId + "_" + month.toUpperCase();
        MessRecord record = new MessRecord(studentId, month, days, rate);
        messDAO.save(key, record);
    }

    public MessRecord getBill(String studentId, String month) {
        return messDAO.findById(studentId + "_" + month.toUpperCase());
    }

    public String exportInvoice(String studentId, String month) {
        MessRecord record = getBill(studentId, month);
        if (record == null) return "Export Error: No billing record found for " + month;

        String filename = "Invoice_" + studentId + "_" + month.toUpperCase() + ".txt";
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("==================================================");
            writer.println("         EDULODGE CAMPUS MESS INVOICE             ");
            writer.println("==================================================");
            writer.println("Student ID    : " + record.getStudentId());
            writer.println("Billing Month : " + record.getMonth());
            writer.println("Days Attended : " + record.getDaysAttended());
            writer.println("Daily Rate    : ₹" + String.format("%.2f", record.getDailyRate()));
            writer.println("--------------------------------------------------");
            writer.println("TOTAL DUE     : ₹" + String.format("%.2f", record.calculateTotalBill()));
            writer.println("==================================================");
            return "Success: Invoice successfully generated at " + filename;
        } catch (Exception e) {
            return "Export Error: " + e.getMessage();
        }
    }
}

// ==========================================
// 4. MAIN APPLICATION INTERFACE
// ==========================================

public class MainApp {
    public static void main(String[] args) {
        GenericDAO<User, String> userDAO = new FileGenericDAO<>("users.dat");
        GenericDAO<Student, String> studentDAO = new FileGenericDAO<>("students.dat");
        GenericDAO<Room, String> roomDAO = new FileGenericDAO<>("rooms.dat");
        GenericDAO<MessRecord, String> messDAO = new FileGenericDAO<>("mess.dat");

        AuthService authService = new AuthService(userDAO);
        AllocationService allocationService = new AllocationService(roomDAO, studentDAO);
        BillingService billingService = new BillingService(messDAO);

        if (userDAO.findById("ADMIN01") == null) {
            AdminWarden admin = new AdminWarden("ADMIN01", "Chief Warden", "warden@vityarthi.ac.in", AuthService.hashPassword("admin123"), "A-Block");
            authService.registerUser(admin);
            allocationService.addRoom(new Room("A-101", "A-Block", 2));
            allocationService.addRoom(new Room("A-102", "A-Block", 2));
        }

        Scanner scanner = new Scanner(System.in);
        System.out.println("=================================================");
        System.out.println("   WELCOME TO EDULODGE CAMPUS MANAGEMENT SYSTEM  ");
        System.out.println("=================================================");

        while (true) {
            System.out.println("\n1. Register Student");
            System.out.println("2. User Login");
            System.out.println("3. Exit System");
            System.out.print("Select Option: ");

            String choice = scanner.nextLine().trim();
            if (choice.equals("1")) {
                System.out.print("Enter User ID: ");
                String id = scanner.nextLine().trim();
                System.out.print("Enter Full Name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter Email: ");
                String email = scanner.nextLine().trim();
                System.out.print("Enter Roll Number: ");
                String roll = scanner.nextLine().trim();
                System.out.print("Enter Password: ");
                String pass = scanner.nextLine().trim();

                Student newStudent = new Student(id, name, email, AuthService.hashPassword(pass), roll);
                authService.registerUser(newStudent);
                studentDAO.save(id, newStudent);
                System.out.println("Student registered successfully!");

            } else if (choice.equals("2")) {
                System.out.print("Enter User ID: ");
                String id = scanner.nextLine().trim();
                System.out.print("Enter Password: ");
                String pass = scanner.nextLine().trim();

                try {
                    User user = authService.login(id, pass);
                    System.out.println("\nLogin Successful! Role: " + user.getRole());
                    
                    if (user instanceof Student) {
                        handleStudentMenu((Student) user, scanner, allocationService, billingService, studentDAO);
                    } else if (user instanceof AdminWarden) {
                        handleAdminMenu((AdminWarden) user, scanner, allocationService, billingService, studentDAO);
                    }
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else if (choice.equals("3")) {
                System.out.println("Exiting EduLodge System. Goodbye!");
                break;
            } else {
                System.out.println("Invalid selection. Try again.");
            }
        }
        scanner.close();
    }

    private static void handleStudentMenu(Student student, Scanner scanner, AllocationService allocation, BillingService billing, GenericDAO<Student, String> studentDAO) {
        while (true) {
            System.out.println("\n--- Student Portal ---");
            System.out.println("1. View Profile & Room Info");
            System.out.println("2. View Mess Bill");
            System.out.println("3. Download Mess Invoice");
            System.out.println("4. Logout");
            System.out.print("Select Action: ");
            String opt = scanner.nextLine().trim();

            if (opt.equals("1")) {
                Student current = studentDAO.findById(student.getUserId());
                current.displayProfile();
            } else if (opt.equals("2")) {
                System.out.print("Enter Month (e.g., April): ");
                String month = scanner.nextLine().trim();
                MessRecord record = billing.getBill(student.getUserId(), month);
                if (record != null) {
                    System.out.println("Days Attended: " + record.getDaysAttended() + " | Total Bill: ₹" + record.calculateTotalBill());
                } else {
                    System.out.println("No records found for " + month);
                }
            } else if (opt.equals("3")) {
                System.out.print("Enter Month: ");
                String month = scanner.nextLine().trim();
                System.out.println(billing.exportInvoice(student.getUserId(), month));
            } else if (opt.equals("4")) {
                break;
            }
        }
    }

    private static void handleAdminMenu(AdminWarden admin, Scanner scanner, AllocationService allocation, BillingService billing, GenericDAO<Student, String> studentDAO) {
        while (true) {
            System.out.println("\n--- Admin Warden Portal ---");
            System.out.println("1. View All Rooms");
            System.out.println("2. Allocate Room");
            System.out.println("3. Log Mess Attendance");
            System.out.println("4. Logout");
            System.out.print("Select Action: ");
            String opt = scanner.nextLine().trim();

            if (opt.equals("1")) {
                List<Room> rooms = allocation.getAllRooms();
                for (Room r : rooms) {
                    System.out.println("Room: " + r.getRoomNumber() + " | Block: " + r.getBlock() + " | Occupancy: " + r.getOccupantIds().size() + "/" + r.getCapacity());
                }
            } else if (opt.equals("2")) {
                System.out.print("Enter Student ID: ");
                String sId = scanner.nextLine().trim();
                System.out.print("Enter Room Number: ");
                String rNum = scanner.nextLine().trim();
                try {
                    allocation.allocateRoom(sId, rNum);
                    System.out.println("Room allocated successfully!");
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else if (opt.equals("3")) {
                System.out.print("Enter Student ID: ");
                String sId = scanner.nextLine().trim();
                System.out.print("Enter Month: ");
                String month = scanner.nextLine().trim();
                System.out.print("Enter Days Attended: ");
                int days = Integer.parseInt(scanner.nextLine().trim());
                System.out.print("Enter Daily Rate (₹): ");
                double rate = Double.parseDouble(scanner.nextLine().trim());

                billing.recordAttendance(sId, month, days, rate);
                System.out.println("Mess attendance logged successfully!");
            } else if (opt.equals("4")) {
                break;
            }
        }
    }
}