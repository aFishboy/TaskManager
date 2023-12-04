import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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
        if(args.length >= 1 && args.length <= 4){
            tm.run(args);
        }else{
            System.out.println("Usage: java TM <command> <data>\n" +
                               "For a list of commands, type help");
        }
    }
}

enum CommandType{
    START, STOP, DESCRIBE, SUMMARY, SIZE, RENAME, DELETE, HELP;
}

interface Command {
    void execute(String[] input);

    // we can use this to check individually for each command if they have proper
    // amount of args and we can later check if it is valid ie not having two 
    // starts going at the same time
    boolean isProperCommand(String[] input);
}

class StartCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tStart\t" + input[1]);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        if (input.length != 2){
            System.out.println("Usage: java TM.java start <task name>\n" +
                               "For a list of commands, type help");
            return false;
        }
        if (input[1].equals("S") || input[1].equals("M") ||
                     input[1].equals("L") || input[1].equals("XL")){
            System.out.println("Error: Sizes [S, M, L, XL] " + 
                               "cannot be used as names");
            return false;
        }
        return true;
    }
}

class  StopCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tStop\t" + input[1]);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper){
            System.out.println("Usage: java TM.java stop <task name>\n" +
                               "For a list of commands, type help");
        }
        return isProper;
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
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tDescribe\t" + log);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length >= 1 && input.length <= 4;
        if (!isProper){
            System.out.println("Usage: java TM.java describe <task name> " + 
                               "<description> [{S|M|L|XL}]\n" +
                               "For a list of commands, type help ");
        }
        return isProper;
    }
}

class SizeCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tSize\t" + input[1] + "\t" + input[2]);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 3;
        if (!isProper){
            System.out.println("Usage: java TM.java size <task  name> " +
                               "{S|M|L|XL}\nFor a list of commands, type help");
        }
        return isProper;
    }
}

class RenameCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tRename\t" + input[1] + "\t" + input[2]);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper){
            System.out.println("Usage: java TM.java rename <old task name>" + 
                               " <new task name>\n" +
                               "For a list of commands, type help");
        }
        return isProper;
    }
}

class DeleteCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tDelete\t" + input[1]);
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 1;
        if (!isProper){
            System.out.println("Usage: java TM.java delete <task name> \n" +
                               "For a list of commands, type help");
        }
        return isProper;
    }
}

class HelpCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            System.out.println("List of COMMMMMMMMMMMMMMMMMMAAAABABABAAABABANANANANANNDDDSS");
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 1 || input.length == 2;
        if (!isProper){
            System.out.println("Usage: java TM.java help\n");
        }
        return isProper;
    }

}

class SummaryCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (!isProperCommand(input))
            return;

        

        List<String> fileList = FileUtil.readFileAndStoreInList();
        for (String element : fileList) {
            System.out.println(element);
        }
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 1;
        if (!isProper){
            System.out.println("Usage: java TM.java summary " + 
                               "[<task name> | {S|M|L|XL}]\n" +
                               "For a list of commands, type help");
        }
        return isProper;
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

    public static List<String> readFileAndStoreInList() {
        List<String> lines = new ArrayList<>();
        try {
            File file = new File(LOGFILE); // Specify the file
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                lines.add(line);
                // Process the line as needed
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file: " + LOGFILE);
            e.printStackTrace();
        }
        return lines;
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance;
    private Map<CommandType, Command> commandMap; // obtained from a factory
    private List<Task> taskList;
    private Task currentTask;
    
    private TaskManager() {
        this.commandMap = CommandMapFactory.createCommandMap();
    }

    public void run(String[] input) {
        CommandType action = CommandType.valueOf(input[0].toUpperCase());
        Command command = commandMap.get(action);
        command.execute(input);
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

class CommandMapFactory {
    public static Map<CommandType, Command> createCommandMap() {
        Map<CommandType, Command> commandMap = new HashMap<>();
        commandMap.put(CommandType.START, new StartCommand());
        commandMap.put(CommandType.STOP, new StopCommand());
        commandMap.put(CommandType.DESCRIBE, new DescribeCommand());
        commandMap.put(CommandType.SIZE, new SizeCommand());
        commandMap.put(CommandType.RENAME, new RenameCommand());
        commandMap.put(CommandType.DELETE, new DeleteCommand());
        commandMap.put(CommandType.HELP, new HelpCommand());
        commandMap.put(CommandType.SUMMARY, new SummaryCommand());

        return commandMap;
    }
}