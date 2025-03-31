import java.util.*;
import java.io.*;

class Test {
    private static final Scanner scanner = new Scanner(System.in);
    private static final Map<String, String> users = new HashMap<>();
    private static final Map<String, Long> completionTimes = new HashMap<>();
    private static final String USERS_FILE = "users.txt";
    private static final String COMPLETION_TIMES_FILE = "completion_times.txt";
    private static final String QUESTIONS_FILE = "questions.txt";
    private static final List<Question> questions = new ArrayList<>();

    public static void main(String[] args) {
        loadUsers();
        loadCompletionTimes();
        loadQuestions();
        showWelcomeScreen();
    }

    // Starting screen
    private static void showWelcomeScreen() {
        System.out.println("---------------------- Welcome to Riddle Quiz Game! ---------------------- ");
        System.out.println(" \"Test your wits with challenging riddles across different difficulty levels.\" ");
        
        while (true) {
            System.out.print("\nDo you have an account yet?\nEnter (Yes/No): ");
            String response = scanner.nextLine().trim().toLowerCase();
            
            if (response.equals("yes") || response.equals("y")) {
                String username = login();
                if (username != null) {
                    showMainMenu(username);
                }
                break;
            } else if (response.equals("no") || response.equals("n")) {
                if (signUp()) {
                    showWelcomeScreen(); // Loop back to login after signup
                }
                break;
            } else {
                System.out.println("Invalid input. Please enter 'Yes' or 'No'.");
            }
        }
    }

    // Main menu
    private static void showMainMenu(String username) {
        while (true) {
            System.out.println("\n\n                                Main Menu                                ");
            System.out.println("1. Play Quiz");
            System.out.println("2. View Leaderboard");
            System.out.println("3. Edit Profile");
            System.out.println("4. Logout");
            System.out.print("Choose an option: ");

            try {
                int choice = scanner.nextInt();
                scanner.nextLine();

                switch (choice) {
                    case 1:
                        playQuiz(username);
                        break;
                    case 2:
                        showLeaderboard();
                        break;
                    case 3:
                        editProfile(username);
                        break;
                    case 4:
                        System.out.println("Logging out... Goodbye, " + username + "!");
                        showWelcomeScreen();
                        return;
                    default:
                        System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number (1-4).");
                scanner.nextLine();
            }
        }
    }

    // Edit profile menu
    private static void editProfile(String currentUsername) {
        System.out.println("\n\n                                Edit Profile                                ");
        System.out.println("1. Change Username");
        System.out.println("2. Change Password");
        System.out.println("3. Back to Main Menu");
        System.out.print("Choose an option: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            switch (choice) {
                case 1:
                    changeUsername(currentUsername);
                    break;
                case 2:
                    changePassword(currentUsername);
                    break;
                case 3:
                    System.out.println("Returning to main menu...");
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Please enter a valid number (1-3).");
            scanner.nextLine();
        }
    }
    
    // Edit profile method
    private static void changeUsername(String currentUsername) {
        System.out.print("\nEnter new username: ");
        String newUsername = scanner.nextLine();

        if (users.containsKey(newUsername)) {
            System.out.println("Username already exists. Please choose another one.");
            return;
        }

        // Update all records with the new username
        String password = users.get(currentUsername);
        long time = completionTimes.getOrDefault(currentUsername, Long.MAX_VALUE);

        // Remove old entries
        users.remove(currentUsername);
        completionTimes.remove(currentUsername);

        // Add new entries
        users.put(newUsername, password);
        completionTimes.put(newUsername, time);

        saveUsers();
        saveCompletionTimes();

        System.out.println("Username changed successfully to: " + newUsername);
        
        // Return to welcome screen with new username
        System.out.println("Please login again with your new username.");
        showWelcomeScreen();
    }

    // Change password method
    private static void changePassword(String username) {
        System.out.print("\nEnter current password: ");
        String currentPassword = scanner.nextLine();

        if (!users.get(username).equals(currentPassword)) {
            System.out.println("Incorrect current password.");
            return;
        }

        System.out.print("Enter new password: ");
        String newPassword = scanner.nextLine();

        System.out.print("Confirm new password: ");
        String confirmPassword = scanner.nextLine();

        if (!newPassword.equals(confirmPassword)) {
            System.out.println("Passwords do not match.");
            return;
        }

        users.put(username, newPassword);
        saveUsers();
        System.out.println("Password changed successfully!");
    }

    // Signup method
    private static boolean signUp() {
        System.out.println("\n\n                                Sign-Up Menu                               ");
        System.out.print("Enter a username: ");
        String username = scanner.nextLine();

        if (users.containsKey(username)) {
            System.out.println("Username already exists. Try another one.");
            return false;
        }

        System.out.print("Enter a password: ");
        String password = scanner.nextLine();
        users.put(username, password);
        completionTimes.put(username, Long.MAX_VALUE);
        saveUsers();
        saveCompletionTimes();
        System.out.println("Sign-up successful! You can now log in.");
        return true;
    }

    // Login method
    private static String login() {
        System.out.println("\n\n                                Login Menu                                ");
        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (users.containsKey(username) && users.get(username).equals(password)) {
            System.out.println("Login successful! Welcome, " + username + "!");
            return username;
        } else {
            System.out.println("Invalid username or password.");
            return null;
        }
    }

    // Quiz method
    private static void playQuiz(String username) {
        if (questions.isEmpty()) {
            System.out.println("No questions available.");
            return;
        }   

        // Group questions by difficulty
        Map<String, List<Question>> questionsByDifficulty = new HashMap<>();
        questionsByDifficulty.put("Easy", new ArrayList<>());
        questionsByDifficulty.put("Medium", new ArrayList<>());
        questionsByDifficulty.put("Hard", new ArrayList<>());

        for (Question q : questions) {
            questionsByDifficulty.get(q.getDifficulty()).add(q);
        }

        long startTime = System.currentTimeMillis();

        for (String difficulty : new String[]{"Easy", "Medium", "Hard"}) {
            List<Question> difficultyQuestions = questionsByDifficulty.get(difficulty);
            if (difficultyQuestions.isEmpty()) continue;
        
            System.out.println("\n\nStarting " + difficulty + " difficulty questions!");
            
            for (Question q : difficultyQuestions) {
                boolean answeredCorrectly = false;
                while (!answeredCorrectly) {
                    System.out.println("\n" + q.getNumber() + ". (" + q.getDifficulty() + ") " + q.getText());
                    char optionChar = 'A';
                    for (String option : q.getOptions()) {
                        System.out.println(optionChar + ". " + option);
                        optionChar++;
                    }
                    System.out.print("Your answer: ");
                    char answer;
                    try {
                        answer = Character.toUpperCase(scanner.next().charAt(0));
                        scanner.nextLine();
                    } catch (InputMismatchException e) {
                        System.out.println("Invalid input. Please enter a letter (A, B, C, etc.).");
                        scanner.nextLine();
                        continue;
                    }

                    if (answer == q.getCorrectAnswer()) {
                        System.out.println("Correct!");
                        answeredCorrectly = true;
                    } else {
                        System.out.println("Wrong! Try again.");
                    }
                }
            }
            
            System.out.println("\nCongratulations on completing the " + difficulty + " difficulty!");
        }

        long endTime = System.currentTimeMillis();
        long durationMillis = endTime - startTime;
        String formattedTime = formatDuration(durationMillis);
        System.out.println("\n\nQuiz Over!");
        System.out.println("Total time taken: " + formattedTime);

        // Update completion times if this is the fastest completion
        if (!completionTimes.containsKey(username) || durationMillis < completionTimes.get(username)) {
            completionTimes.put(username, durationMillis);
            saveCompletionTimes();
        }
    }

    private static String formatDuration(long durationMillis) {
        long seconds = (durationMillis / 1000) % 60;
        long minutes = (durationMillis / (1000 * 60)) % 60;
        long hours = (durationMillis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Leaderboard method
    private static void showLeaderboard() {
        while (true) {
            System.out.println("\n\n                                Leaderboard                              ");
            completionTimes.entrySet().stream()
                .sorted(Map.Entry.comparingByValue())
                .forEach(entry -> {
                    String username = entry.getKey();
                    long time = entry.getValue();
                    System.out.println(username + " - Fastest time: " + 
                        (time == Long.MAX_VALUE ? "N/A" : formatDuration(time)));
                });
    
            System.out.println("\n1. Back to Main Menu");
            System.out.print("Choose an option: ");
            
            try {
                int choice = scanner.nextInt();
                scanner.nextLine();
                if (choice == 1) {
                    return;
                } else {
                    System.out.println("Invalid choice. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Please enter a valid number (1).");
                scanner.nextLine();
            }
        }
    }

    // Loading user data
    private static void loadUsers() {
        try (Scanner fileScanner = new Scanner(new File(USERS_FILE))) {
            while (fileScanner.hasNextLine()) {
                String[] data = fileScanner.nextLine().split(",");
                if (data.length >= 2) {
                    users.put(data[0], data[1]);
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous user data found. Starting fresh.");
        }
    }

    // Load completion times data
    private static void loadCompletionTimes() {
        try (Scanner fileScanner = new Scanner(new File(COMPLETION_TIMES_FILE))) {
            while (fileScanner.hasNextLine()) {
                String[] data = fileScanner.nextLine().split(",");
                if (data.length >= 2) {
                    try {
                        completionTimes.put(data[0], Long.parseLong(data[1]));
                    } catch (NumberFormatException e) {
                        completionTimes.put(data[0], Long.MAX_VALUE);
                    }
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No previous completion times data found. Starting fresh.");
        }
    }

    // Load questions from file
    private static void loadQuestions() {
        try (Scanner fileScanner = new Scanner(new File(QUESTIONS_FILE))) {
            int questionNumber = 1;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                // Split on commas that are not inside quotes
                String[] parts = line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
                
                // Trim each part and remove surrounding quotes if present
                for (int i = 0; i < parts.length; i++) {
                    parts[i] = parts[i].trim().replaceAll("^\"|\"$", "");
                }
                
                // Question files need difficulty, text, 3 options, and answer
                if (parts.length >= 6) {
                    String difficulty = parts[0];
                    String text = parts[1];
                    String[] options = Arrays.copyOfRange(parts, 2, parts.length - 1);
                    char correctAnswer = parts[parts.length - 1].trim().charAt(0);
                    questions.add(new Question(questionNumber++, difficulty, text, options, correctAnswer));
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("No questions found. Please add questions to " + QUESTIONS_FILE);
        }
    }

    // Save user data method
    private static void saveUsers() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(USERS_FILE))) {
            users.forEach((user, pass) -> writer.println(user + "," + pass));
        } catch (IOException e) {
            System.out.println("Error saving users.");
        }
    }

    // Save completion times data method
    private static void saveCompletionTimes() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(COMPLETION_TIMES_FILE))) {
            completionTimes.forEach((user, time) -> 
                writer.println(user + "," + (time == Long.MAX_VALUE ? "" : time)));
        } catch (IOException e) {
            System.out.println("Error saving completion times.");
        }
    }
}

class Question {
    private final int number;
    private final String difficulty;
    private final String text;
    private final String[] options;
    private final char correctAnswer;

    public Question(int number, String difficulty, String text, String[] options, char correctAnswer) {
        this.number = number;
        this.difficulty = difficulty;
        this.text = text;
        this.options = options;
        this.correctAnswer = correctAnswer;
    }

    public int getNumber() { return number; }
    public String getDifficulty() { return difficulty; }
    public String getText() { return text; }
    public String[] getOptions() { return options; }
    public char getCorrectAnswer() { return correctAnswer; }
}
