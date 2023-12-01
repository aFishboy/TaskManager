import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TM {
    public static void main(String[] args) {
        // get input
        TaskManager tm = TaskManager.getInstance();
        // run time manager
        if(tm.isProperCommand(args)){
            tm.run(args);
        }   
    }
}

enum CommandType{
    START, STOP, DESCRIBE, SUMMARY, SIZE, RENAME, DELETE;
}

interface Command {
    public void execute(String[] input);

    // I feel like we don't need to necessarily have this method in the interface
    // and the formatting doesn't have to be the same as Posnett's, we can just use the default LocalDateTime format
    // especially in the case of the summary command, where we don't log it to the file
    default String getTime(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return currentTime.format(formatter);
    }
}

class StartCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(LocalDateTime.now() + "\tStart\t" + input[1]);
    }
}

class  StopCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(LocalDateTime.now() + "\tStop\t" + input[1]);
    }
}

class DescribeCommand implements Command{
    @Override
    public void execute(String[] input) {
        String[] sizes = {"S", "M", "L", "XL"};
        String log = input[1] + "\t\"" + input[2] + "\"";

        if (input.length == 4) {
            // check if size is correct format (S, M, L, XL), if not, ignore size since it is optional
            // should discuss if this should ignore the error (warn the user) or throw an exception
            if (Arrays.asList(sizes).contains(input[3])) {
                log += "\t" + input[3];
            }
            else {
                System.out.println("Invalid size, ignoring... Please enter a valid size next time (S, M, L, XL)");
            }
        }
        FileUtil.writeToFile(LocalDateTime.now() + "\tDescribe\t" + log);
    }
}

class SizeCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(LocalDateTime.now() + "\tSize\t" + input[1] + "\t" + input[2]);
    }
}

class RenameCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(LocalDateTime.now() + "\tRename\t" + input[1] + "\t" + input[2]);
    }
}

class DeleteCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(LocalDateTime.now() + "\tDelete\t" + input[1]);
    }
}

class FileUtil {
    private static final String LOGFILE = "TM.log";

    public static void writeToFile(String content) {
        try {
            FileWriter writer = new FileWriter(LOGFILE, true);
            writer.write(content + '\n');
            writer.close();
        } catch (IOException e) {
            System.out.println("An error occurred while writing to the file: " + LOGFILE);
            e.printStackTrace();
        }
    }

    public static String readFromFile() {
        StringBuilder content = new StringBuilder();
        try {
            File file = new File(LOGFILE); // Specify the file
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                content.append(line).append("\n");
                // Process the line as needed
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file: " + LOGFILE);
            e.printStackTrace();
        }
        return content.toString();
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance;
    private Map<CommandType, Command> commandMap = new HashMap<>(); // injected in, or obtained from a factory
    
    private TaskManager() {
        commandMap.put(CommandType.START, new StartCommand());
        commandMap.put(CommandType.STOP, new StopCommand());
        commandMap.put(CommandType.DESCRIBE, new DescribeCommand());
        commandMap.put(CommandType.SIZE, new SizeCommand());
        commandMap.put(CommandType.RENAME, new RenameCommand());
        commandMap.put(CommandType.DELETE, new DeleteCommand());
    }

    public void run(String[] input) {
        CommandType action = CommandType.valueOf(input[0].toUpperCase());
        Command command = commandMap.get(action);
        command.execute(input);
    }

    public boolean isProperCommand(String[] input) {
       return input.length >= 2 && input.length <= 4;
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

    // @Override
    // public void execute() {
    //     // TODO Auto-generated method stub
    //     throw new UnsupportedOperationException("Unimplemented method 'execute'");
    // }
}