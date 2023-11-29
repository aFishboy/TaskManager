import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.FileWriter;

public class TM {
    public static void main(String[] args) {
        // get input
        String input = System.console().readLine();
        // run time manager
        TaskManager.getInstance().run(input);
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance = null;
    private static final String LOGFILE = "log.txt";
    private static Scanner scanner = new Scanner(System.in);
    
    private TaskManager() {
        // create or open log file
        File file = new File(LOGFILE);

        try {
            file.createNewFile();
        } catch (Exception e) {}

        // read log file
        try {
            scanner = new Scanner(file);
        } catch (Exception e) {}
    }

    public void run(String input) {
        String[] commandLine = splitInput(input);
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
    private String date;
    private String time;
    private TState status;

    public Task(String name, String description, String dateandtime) {
        this.name = name;
        this.description = description;
        this.date = dateandtime.split(" ")[0];
        this.time = dateandtime.split(" ")[1];
    }

    

    public String getStatus() {
        return this.status.toString();
    }
}

// State Pattern
interface TState {
    public void start();
    public void stop();
}

class TStateNew implements TState {
    public void start() {
        // do nothing
    }

    public void stop() {
        // do nothing
    }
}