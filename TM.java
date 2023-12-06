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
import java.time.Duration;
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
    // Maybe change to
    // Start plop 2023-12-05T13:00:00
    // Stop plop 2023-12-05T15:20:51
    // Describe foo "new description"

    // we can use this to check individually for each command if they have proper
    // amount of args and we can later check if it is valid ie not having two 
    // starts going at the same time
    boolean isProperCommand(String[] input);

    // we can use this to parse the line and store it in the task list
    Task parseLine(String[] input, Task task);
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

    public Task parseLine(String[] logLine, Task task) throws IllegalStateException {
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        String taskName = logLine[2];
        
        if (task != null) {
            if (task.isRunning()){
                throw new IllegalStateException("Task " + task.getTaskName() + " is already running");
            }
            task.updateStart(timeStamp);
            return task;
        } else {
            Task newTask = new Task(taskName);
            newTask.setStart(timeStamp);
            newTask.setIsRunning();
            return newTask;
        }
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

    @Override
    public Task parseLine(String[] logLine, Task task) {
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        if (task != null) {
            task.updateStop(timeStamp);
        }else{
            throw new IllegalStateException("No existing task for STOP command at File Line ");
        }
        return null;
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
            String size = input[3].toUpperCase();
            if (Arrays.asList(sizes).contains(size)) {
                log += "\t" + size;
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
    @Override
    public Task parseLine(String[] logLine, Task task) {
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        String taskName = logLine[2];
        String description = logLine[3];
        String size = null;
        
        if (logLine.length == 5){
            size = logLine[4];
        }
        if (task != null) {
            task.updateDescription(description, size);
            return null;
        }else{
            Task newTask = new Task(taskName);
            newTask.setDescription(description);
            newTask.setSize(size);
            return newTask;
        }
    }
}
class SizeCommand implements Command{
        @Override
    public void execute(String[] input) {
        if (isProperCommand(input))
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tSize\t" + input[1] + "\t" + input[2].toUpperCase());
    }

    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 3; // need to also check it is proper size
        if (!isProper){
            System.out.println("Usage: java TM.java size <task  name> " +
                               "{S|M|L|XL}\nFor a list of commands, type help");
        }
        return isProper;
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        String taskName = logLine[2];
        String taskSize = logLine[3];
        if (task != null) {
            task.setSize(taskSize);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.setSize(taskSize);
            return newTask;
        }
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
        boolean isProper = input.length == 3;
        if (!isProper){
            System.out.println("Usage: java TM.java rename <old task name>" + 
                               " <new task name>\n" +
                               "For a list of commands, type help");
        }
        return isProper;
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        if (task == null){
            throw new IllegalStateException("Can't rename a task that does not exist");
        }
        String taskName = logLine[3];
        task.setName(taskName);
        return null;
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
        boolean isProper = input.length == 2;
        if (!isProper){
            System.out.println("Usage: java TM.java delete <task name> \n" +
                               "For a list of commands, type help");
        }
        return isProper;
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        if (existingTask == null){
            throw new IllegalStateException("Can't delete a task that does not exist");
        }
        existingTask.setName(null);
        return null;
    }
}

class HelpCommand implements Command {
    private static final String HELP_MESSAGE = "Usage: java TM.java <command>\n" +
                                               "Commands:\n" +
                                               "start <task name>\n" +
                                               "stop <task name>\n" +
                                               "describe <task name> <description> [{S|M|L|XL}]\n" +
                                               "size <task name> {S|M|L|XL}\n" +
                                               "rename <old task name> <new task name>\n" +
                                               "delete <task name>\n" +
                                               "summary [<task name> | {S|M|L|XL}]\n" +
                                               "help\n";
    @Override
    public void execute(String[] input) {
        if (isProperCommand(input)) {
            System.out.println(HELP_MESSAGE);
        }
    }
   
    @Override
    public boolean isProperCommand(String[] input) {
        boolean isProper = input.length == 1 || input.length == 2;
        if (!isProper) {
            System.out.println("Usage: java TM.java help\n");
        }
        return isProper;
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'parseLine'");
    }
}


class SummaryCommand implements Command{
    @Override
    public void execute(String[] input) {
        if (!isProperCommand(input))
            return;   
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

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        throw new IllegalStateException("Illegal Log Line Command Summary");
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
        }
    }

    public static List<String[]> readLogAndStoreInList() {
        List<String[]> lines = new ArrayList<>();
        try {
            File file = new File(LOGFILE); // Specify the file
            Scanner scanner = new Scanner(file);

            while (scanner.hasNext()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty())
                    continue;
                String[] lineArray = line.toUpperCase().split("\t"); // Split the line into an array
                lines.add(lineArray);
                // Process the line as needed
            }
            scanner.close();
        } catch (IOException e) {
            System.out.println("An error occurred while reading from the file: " + LOGFILE);
        }
        return lines;
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance;
    private Map<CommandType, Command> commandMap; // obtained from a factory
    private List<Task> taskList; // should change this to a map!!!!!!!!
    private Task currentTask; // probably change to a string
    
    private TaskManager() {
        this.commandMap = CommandMapFactory.createCommandMap();
        taskList = createTaskList();
    }

    // probably change this function name to be more descriptive
    public void run(String[] input) {
        CommandType action = getCommandType(input[0]);
        Command command = commandMap.get(action);
        command.execute(input);
    }
    
    public static TaskManager getInstance() {
        if(instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    private List<Task> createTaskList(){
        this.taskList = new ArrayList<>(); 
        List<String[]> logList = FileUtil.readLogAndStoreInList();
        int lineNumber = 0;

        for (String logLine[] : logList) {
            lineNumber++;
            String commandString = logLine[1];
            String taskName = logLine[2];
            Task existingTask = findTaskByName(taskList, taskName);
            
            CommandType action = getCommandType(commandString);
            Command command = commandMap.get(action);
            try{ 
                if (existingTask == null){
                        Task returnedTask = command.parseLine(logLine, null); 
                        this.taskList.add(returnedTask);    
                }
                else {
                    command.parseLine(logLine, existingTask);
                }
            } catch (IllegalStateException e){
                System.err.println("Error: File Line " + lineNumber + ": " + e.getMessage());
                System.exit(1);
            }
        }
        taskList.removeIf(element -> element.getTaskName() == null);
        Arrays.asList(taskList).forEach(System.out::println);
        return taskList;
    }

    private Task findTaskByName(List<Task> taskList, String taskName) {
        for (Task task : taskList) {
            if (task.getTaskName() != null && task.getTaskName().equals(taskName)) {
                return task;
            }
        }
        return null; // Task not found
    }

    private CommandType getCommandType(String lineInput){
        return CommandType.valueOf(lineInput.toUpperCase());
    }
}

// Task Class
class Task {
    private String name;
    private String description;
    private LocalDateTime start;
    private Duration totalTime;
    private String size;
    private boolean isRunning;

    // a lot of parameters. Probably refactor to have less.
    public Task(String name) {
        this.name = name;
        this.start = null;
        this.isRunning = false;
        this.totalTime = Duration.ZERO;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void updateDescription(String description, String size) {
        this.description = description;
        if (size != null)
            this.size = size;
    }

    public boolean isRunning() {
        return this.isRunning;
    }

    public void setIsRunning() {
        this.isRunning = true;
    }

    // setters
    public void setDescription(String description) {
        this.description = description;
    }

    public void setStart(LocalDateTime timeStamp) {
        this.start = timeStamp;
    }

    public Object getTaskName() {
        return this.name;
    }

    // start, stop, describe, size, rename, methods


    @Override
    public String toString() {
        return "Task [name=" + name + ", description=" + description + ", start=" + start + ", totalTime=" + totalTime
                + ", size=" + size + ", currentlyRunning=" + isRunning + "]";
    }

    

    public void setSize(String size) {
        this.size = size;
    }

    public void updateStart(LocalDateTime timeStamp){
        if (isRunning){
            System.out.println("Test\n");
            throw new IllegalStateException("Task " + this.name + " is already running");
        }
        this.start = timeStamp;
        isRunning = true;
    }
    public void updateStop(LocalDateTime timeStamp){
        if (!isRunning){
            throw new IllegalStateException("Task " + this.name + " cannot stop since it has not started");
        }
        Duration duration = calculateTimeDifference(this.start, timeStamp);
        this.start = null;
        this.totalTime = this.totalTime.plus(duration);
        this.isRunning = false;
        System.out.println("Total Time: " + formatTotalTime(this.totalTime));
    }

    private static Duration calculateTimeDifference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return Duration.between(dateTime1, dateTime2);
    }
    private LocalDateTime parseTimestamp(String timeStamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(timeStamp, formatter);
    }

    private String formatTotalTime(Duration duration){
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
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
