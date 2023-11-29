import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class TM {
    public static void main(String[] args) {
        // get input
        String[] input = args;
        // run time manager
        System.out.println(isProperCommand(input));
        if(isProperCommand(input)){
            TaskManager.getInstance().run(input);
        }
            
    }

    private static boolean isProperCommand(String[] input) {
       return input.length >= 2 && input.length <= 4;
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance;
    private static final String LOGFILE = "TM.log";
    
    private TaskManager() {
        // create or open log file
        File file = new File(LOGFILE);

        try {
            file.createNewFile();
        } catch (Exception e) {}

        writeLogfile("new log entry");
        // read log file
        try {
           Scanner scanner = new Scanner(file);
           while (scanner.hasNext()) {
                String line = scanner.nextLine();
                System.out.println(line);
                // Process the line as needed
            }
            scanner.close();
        } catch (Exception e) {}
    }

    public void run(String[] input) {
        System.out.println(Arrays.toString(input));
    }

    private String[] splitInput(String input) {
        String[] split = input.split("\\s+");
        return split;
    }

    private void writeLogfile(String log) {
        // write to log file
        try {
            FileWriter writer = new FileWriter(LOGFILE, true);
            writer.write(log);
            writer.close();
        } catch (Exception e) {}
    }

    public static TaskManager getInstance() {
        if(instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }
}

// Task Class
class Task {
    private String name;
    private String description;
    private String dateandtime;
    private String size;
    private TState status;

    public Task(String name) {
        this.name = name;
        this.description = "";
        this.dateandtime = "";
    }

    // setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setDateandtime(String dateandtime) {
        this.dateandtime = dateandtime;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getStatus() {
        return this.status.toString();
    }
}

// State Pattern
interface TState {
    public void start();
    public void stop();
    public String toString();
}

class TStateNew implements TState {
    public void start() {
        // do nothing
    }

    public void stop() {
        // do nothing
    }

    public String toString() {
        return "New";
    }
}