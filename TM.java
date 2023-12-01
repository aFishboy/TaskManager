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
        String[] input = args;
        // run time manager
        if(isProperCommand(input)){
            TaskManager.getInstance().run(input);
        }   
    }

    private static boolean isProperCommand(String[] input) {
       return input.length >= 2 && input.length <= 4;
    }
}

enum CommandType{
    START, STOP, DESCRIBE, SUMMARY, SIZE, RENAME, DELETE;
}

interface Command {
    public void execute(String[] input);

    default String getTime(){
        LocalDateTime currentTime = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return currentTime.format(formatter);
    }
}

class StartCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(getTime() + "\tStart\t" + input[1]);
    }
}

class  StopCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(getTime() + "\tStop\t" + input[1]);
    }
}

class DescribeCommand implements Command{
    @Override
    public void execute(String[] input) {
        String description = "\t\"" + input[2] + "\"";
        String size = "";
        if (input.length == 4) {
            size = "\t" + input[3];
        }
        FileUtil.writeToFile(getTime() + "\tDescribe\t" + input[1] + description + size);
    }
}

class SizeCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(getTime() + "\tSize\t" + input[1] + "\t" + input[2]);
    }
}

class RenameCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(getTime() + "\tRename\t" + input[1] + "\t" + input[2]);
    }
}

class DeleteCommand implements Command{
    @Override
    public void execute(String[] input) {
        FileUtil.writeToFile(getTime() + "\tDelete\t" + input[1]);
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

    /*
    private String[] splitInput(String input) {
        String[] split = input.split("\\s+");
        return split;
    }
    */

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