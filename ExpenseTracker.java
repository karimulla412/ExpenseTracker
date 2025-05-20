import java.io.*;
import java.text.*;
import java.util.*;

class Transaction {
    private String type;       // income or expense
    private String category;   // salary, food, etc.
    private double amount;
    private String description;
    private Date date;

    public Transaction(String type, String category, double amount, String description, Date date) {
        this.type = type;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.date = date;
    }

    public String toCSV() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return String.join(",",
                sdf.format(date),
                type,
                category,
                String.valueOf(amount),
                description.replace(",", ";") // avoid breaking CSV
        );
    }

    public static Transaction fromCSV(String line) throws ParseException {
        String[] parts = line.split(",");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return new Transaction(
                parts[1],
                parts[2],
                Double.parseDouble(parts[3]),
                parts.length > 4 ? parts[4].replace(";", ",") : "",
                sdf.parse(parts[0])
        );
    }

    public String getType() { return type; }
    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public Date getDate() { return date; }
}

public class ExpenseTracker {
    private static final String FILE_NAME = "transactions.csv";
    private static final Scanner scanner = new Scanner(System.in);
    private static final List<Transaction> transactions = new ArrayList<>();

    public static void main(String[] args) {
        loadFromFile();

        while (true) {
            System.out.println("\n=== Expense Tracker Menu ===");
            System.out.println("1. Add Transaction");
            System.out.println("2. View Monthly Summary");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            int choice = Integer.parseInt(scanner.nextLine());

            switch (choice) {
                case 1:
                    addTransaction();
                    break;
                case 2:
                    showMonthlySummary();
                    break;
                case 3:
                    saveToFile();
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice.");
            }
        }
    }

    private static void addTransaction() {
        System.out.print("Enter type (income/expense): ");
        String type = scanner.nextLine().trim().toLowerCase();

        String category = "";
        if (type.equals("income")) {
            System.out.print("Enter category (salary/business): ");
        } else {
            System.out.print("Enter category (food/rent/travel): ");
        }
        category = scanner.nextLine().trim().toLowerCase();

        System.out.print("Enter amount: ");
        double amount = Double.parseDouble(scanner.nextLine());

        System.out.print("Enter description (optional): ");
        String description = scanner.nextLine();

        System.out.print("Enter date (yyyy-MM-dd) or 'today': ");
        String dateStr = scanner.nextLine();
        Date date;
        try {
            if (dateStr.equalsIgnoreCase("today")) {
                date = new Date();
            } else {
                date = new SimpleDateFormat("yyyy-MM-dd").parse(dateStr);
            }
        } catch (ParseException e) {
            System.out.println("Invalid date. Using today.");
            date = new Date();
        }

        Transaction t = new Transaction(type, category, amount, description, date);
        transactions.add(t);
        saveToFile(); // Save immediately
        System.out.println("Transaction added and saved.");
    }

    private static void showMonthlySummary() {
        if (transactions.isEmpty()) {
            System.out.println("No transactions available.");
            return;
        }

        Map<String, List<Transaction>> byMonth = new HashMap<>();
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyy-MM");

        for (Transaction t : transactions) {
            String month = monthFormat.format(t.getDate());
            byMonth.putIfAbsent(month, new ArrayList<>());
            byMonth.get(month).add(t);
        }

        for (String month : byMonth.keySet()) {
            double income = 0, expense = 0;
            Map<String, Double> categoryIncome = new HashMap<>();
            Map<String, Double> categoryExpense = new HashMap<>();

            for (Transaction t : byMonth.get(month)) {
                if (t.getType().equals("income")) {
                    income += t.getAmount();
                    categoryIncome.put(t.getCategory(), categoryIncome.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                } else {
                    expense += t.getAmount();
                    categoryExpense.put(t.getCategory(), categoryExpense.getOrDefault(t.getCategory(), 0.0) + t.getAmount());
                }
            }

            System.out.println("\n--- " + month + " Summary ---");
            System.out.printf("Total Income: %.2f\n", income);
            for (String cat : categoryIncome.keySet()) {
                System.out.printf("  %s: %.2f\n", cat, categoryIncome.get(cat));
            }

            System.out.printf("Total Expense: %.2f\n", expense);
            for (String cat : categoryExpense.keySet()) {
                System.out.printf("  %s: %.2f\n", cat, categoryExpense.get(cat));
            }

            System.out.printf("Net Balance: %.2f\n", (income - expense));
        }
    }

    private static void saveToFile() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Transaction t : transactions) {
                writer.println(t.toCSV());
            }
            System.out.println("Transactions saved to file.");
        } catch (IOException e) {
            System.out.println("Failed to save file: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("No previous data found.");
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                transactions.add(Transaction.fromCSV(line));
            }
            System.out.println("Loaded transactions from file.");
        } catch (Exception e) {
            System.out.println("Failed to load file: " + e.getMessage());
        }
    }
}
