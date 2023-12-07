import java.util.Arrays;
import java.util.Map;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;

public class TM {
    public static void main(String[] args) throws IOException{
        TaskManager tm;
        try {
            if(args.length >= 1 && args.length <= 4){
                tm = TaskManager.getInstance();
                tm.run(args);
            }else{
                System.out.println("Usage: java TM <command> <data>\n" +
                                "For a list of commands, type help");
            }
        }
        catch (IllegalStateException e) {
            System.out.println(e.getMessage());
        }
        catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }
}

enum CommandType{
    START, STOP, DESCRIBE, SUMMARY, SIZE, RENAME, DELETE, HELP;
}

interface Command {
    void execute(String[] input, Map<String, Task> taskMap) throws IOException;
    // Maybe change to
    // Start plop 2023-12-05T13:00:00
    // Stop plop 2023-12-05T15:20:51
    // Describe foo "new description"
    void isProperCommand(String[] input);
    Task parseLine(String[] input, Task task);
}

class StartCommand implements Command{
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException {
        String taskName = input[1].toUpperCase();
        boolean anyRunning = taskMap.values().stream()
                            .anyMatch(task -> task != null && task.isRunning());
        if (anyRunning)
            throw new IllegalStateException("Cannot start a new task while another task is already running.");
        isProperCommand(input);
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tStart\t" + taskName);
    }

    @Override
    public void isProperCommand(String[] input) {
        if (input.length != 2){
            throw new IllegalStateException("Usage: java TM.java start <task name>\n" +
                               "For a list of commands, type help");
        }
        Set<String> sizes = new HashSet<>(Arrays.asList("S", "M", "L", "XL"));
        if (sizes.contains(input[1].toUpperCase())) {
            throw new IllegalStateException("Error: Sizes [S, M, L, XL] " + 
                               "cannot be used as names");
        }
    }

    public Task parseLine(String[] logLine, Task task){
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        String taskName = logLine[2];
        
        if (task != null) {
            if (task.isRunning()){
                throw new IllegalStateException("Task " + taskName + " is already running");
            }
            task.updateStart(timeStamp);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.setStart(timeStamp);
            newTask.setIsRunning();
            return newTask;
        }
    }
}

class StopCommand implements Command{    
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException {
        String taskName = input[1].toUpperCase();
        isProperCommand(input);
        if (taskMap.containsKey(taskName) && taskMap.get(taskName).isRunning()) {
            FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tStop\t" + taskName);
        } else {
            throw new IllegalStateException("Invalid stop command for task " + taskName);
        }
    }

    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper){
            throw new IllegalStateException("Usage: java TM.java stop <task name>\n" +
                               "For a list of commands, type help");
        }
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

class DescribeCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException {
        final String[] SIZES = {"S", "M", "L", "XL"};
        isProperCommand(input);
        String log = input[1].toUpperCase() + "\t\"" + input[2] + "\"";

        if (input.length == 4) {
            String size = input[3].toUpperCase();
            if (Arrays.asList(SIZES).contains(size)) {
                log += "\t" + size;
            } else {
                System.err.println("Invalid size, ignoring... Please enter a valid size next time (S, M, L, XL)");
            }
        }
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tDescribe\t" + log);
    }

    @Override
    public void isProperCommand(String[] input) {
        if (input.length < 2 || input.length > 4) {
            throw new IllegalStateException("Usage: java TM.java describe <task name> " +
                    "<description> [{S|M|L|XL}]\n" +
                    "For a list of commands, type help ");
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        String taskName = logLine[2];
        String description = logLine[3];
        String size = null;

        if (logLine.length == 5) {
            size = logLine[4];
        }
        if (task != null) {
            task.updateDescription(description, size);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.setDescription(description);
            newTask.setSize(size);
            return newTask;
        }
    }
}
class SizeCommand implements Command{
        @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException{
        isProperCommand(input);
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tSize\t" + input[1] + "\t" + input[2].toUpperCase());
    }

    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 3; // need to also check it is proper size
        if (!isProper){
            throw new IllegalStateException("Usage: java TM.java size <task  name> " +
                               "{S|M|L|XL}\nFor a list of commands, type help");
        }
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
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException{
        String taskName = input[1].toUpperCase();
        isProperCommand(input);
        if (!taskMap.containsKey(taskName))
            throw new IllegalStateException("Cannot rename nonexistent task");
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tRename\t" + input[1] + "\t" + input[2]);
    }

    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 3;
        if (!isProper){
            throw new IllegalStateException("Usage: java TM.java rename <old task name>" + 
                               " <new task name>\n" +
                               "For a list of commands, type help");
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        if (task == null){
            throw new IllegalStateException("Can't rename a task that does not exist");
        }
        String newTaskName = logLine[3];
        task.setName(newTaskName);
        return task;
    }
}

class DeleteCommand implements Command{
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws IOException{
        String taskName = input[1].toUpperCase();
        isProperCommand(input);
        if (!taskMap.containsKey(taskName))
            throw new IllegalStateException("Cannot rename nonexistent task");
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + "\tDelete\t" + input[1]);
    }

    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper){
            throw new IllegalStateException("Usage: java TM.java delete <task name> \n" +
                               "For a list of commands, type help");
        }
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
    private static final String HELP_MESSAGE = 
        "Usage: java TM.java <command>\nCommands:\nstart <task name>\n" +
        "stop <task name>\ndescribe <task name> <\"description\"> [{S|M|L|XL}]\n" +
        "size <task name> {S|M|L|XL}\nrename <old task name> <new task name>\n"+
        "delete <task name>\nsummary [<task name> | {S|M|L|XL}]\nhelp\n";
    @Override
    public void execute(String[] input, Map<String, Task> taskMap){
        isProperCommand(input);
        System.out.println(HELP_MESSAGE);
    }
   
    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 1;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java help\n");
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        throw new IllegalStateException("Illegal Log Line Command Help");

    }
}

class SummaryCommand implements Command{
    private Map<String, Task> taskMap;
    private Predicate<Task> summaryPredicate;
    private static final String[] SIZES = {"S", "M", "L", "XL"};

    @Override
    public void execute(String[] input, Map<String, Task> taskMap){
        this.taskMap = taskMap;
        isProperCommand(input);
        if (input.length == 1){
            this.summaryPredicate = task -> true;
        } else if (Arrays.asList(SIZES).contains(input[1].toUpperCase())){
            this.summaryPredicate = task -> task.getSize().equals(input[1].toUpperCase());
        } else {
            this.summaryPredicate = task -> task.getTaskName().equals(input[1].toUpperCase());
        }
        createSummary(taskMap);
    }

    private void createSummary(Map<String, Task> taskMap) {
        List<Task> filteredTasks = taskMap.values().stream()
                .filter(summaryPredicate)
                .collect(Collectors.toList());

        SummaryProcessor.printSummary(filteredTasks);
    }

    @Override
    public void isProperCommand(String[] input) {
        boolean isProper = input.length == 1 || input.length == 2;
        if (!isProper){
            throw new IllegalStateException("Usage: java TM.java summary " + 
                                            "[<task name> | {S|M|L|XL}]\n" +
                                            "For a list of commands, type help");
        }
        else if (input.length == 2 &&
                !Arrays.asList(SIZES).contains(input[1].toUpperCase()) && 
                !taskMap.containsKey(input[1].toUpperCase())){
                throw new IllegalStateException("Invalid size or task for summary command" + 
                                                "\nFor a list of commands, type help");
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        throw new IllegalStateException("Illegal Log Line Command Summary");
    }
}

// processes the summary of the tasks
class SummaryProcessor {
    //get current running task
    private static Task getCurrentRunningTask(List<Task> tasks) {
        return tasks.stream()
                .filter(Task::isRunning)
                .findFirst()
                .orElse(null);
    }


    private static Duration calculateTotalTime(List<Task> tasks) {
        return tasks.stream()
                .map(Task::getDuration)
                .reduce(Duration.ZERO, Duration::plus);
    }

    private static Duration calculateAverageTime(List<Task> tasks) {
        Duration totalTime = calculateTotalTime(tasks);
        return totalTime.dividedBy(tasks.size());
    }

    private static Duration calculateMinTime(List<Task> tasks) {
        return tasks.stream()
                .map(Task::getDuration)
                .min(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    private static Duration calculateMaxTime(List<Task> tasks) {
        return tasks.stream()
                .map(Task::getDuration)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);
    }

    public static void printSummary(List<Task> tasks) {
        System.out.println("Summary:\n");

        System.out.println("Current Running Task: \t" + getCurrentRunningTask(tasks).getTaskName() + "\n");

        tasks.forEach(task -> System.out.println(task.getSummary(task)));

        Duration minDuration = calculateMinTime(tasks);
        Duration maxDuration = calculateMaxTime(tasks);
        Duration avgDuration = calculateAverageTime(tasks);

        System.out.println("Min Duration of Started Tasks:     \t" + DurationUtil.formatTotalTime(minDuration));
        System.out.println("Max Duration of Started Tasks:     \t" + DurationUtil.formatTotalTime(maxDuration));
        System.out.println("Average Duration of Started Tasks: \t" + DurationUtil.formatTotalTime(avgDuration));
    }
}

class FileUtil {
    private static final String LOGFILE = "TM.log";

    public static void writeToFile(String content) throws IOException {
        FileWriter writer = new FileWriter(LOGFILE, true);
        writer.write(content + '\n');
        writer.close();
    }

    public static List<String[]> readLogAndStoreInList() throws FileNotFoundException {
        List<String[]> lines = new ArrayList<>();
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
        return lines;
    }
}

// Task Manager Class (Singleton)
class TaskManager {
    private static TaskManager instance;
    private Map<CommandType, Command> commandMap; // obtained from a factory
    private Map<String, Task> taskMap;
    
    private TaskManager() throws FileNotFoundException {
        this.commandMap = CommandMapFactory.createCommandMap();
        this.taskMap = createTaskMap();
    }

    // probably change this function name to be more descriptive
    public void run(String[] input) throws IOException{
        String commandString = input[0];
        if (!validateCommand(commandString)){
            throw new IllegalStateException("Invalid command " + commandString + 
                                            "\nFor a list of commands, type help");
        }
        CommandType action = CommandType.valueOf(commandString.toUpperCase());
        Command command = commandMap.get(action);
        command.execute(input, this.taskMap);
    }

    public static TaskManager getInstance() throws FileNotFoundException{
        if(instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    private Map<String, Task> createTaskMap() throws FileNotFoundException{
        Map<String, Task> taskMap = new HashMap<>();
        List<String[]> logList = FileUtil.readLogAndStoreInList();
        int lineNumber = 0;

        for (String logLine[] : logList) {
            lineNumber++;
            String commandString = logLine[1];
            String taskName = logLine[2];
            Task existingTask = taskMap.get(taskName);
            if (!validateCommand(commandString)){
                throw new IllegalArgumentException("Invalid command " + commandString + 
                                                    " at File Line " + lineNumber);
            }
            CommandType action = CommandType.valueOf(commandString.toUpperCase());
            Command command = commandMap.get(action);

            if (existingTask == null){
                    Task returnedTask = command.parseLine(logLine, null); 
                    taskMap.put(taskName, returnedTask);
            }
            else {
                Task returnedTask = command.parseLine(logLine, existingTask);
                if (returnedTask != null){
                    taskMap.put(returnedTask.getTaskName(), returnedTask);
                    taskMap.remove(taskName);
                }
            }
        }
        taskMap.values().removeIf(element -> element.getTaskName() == null);
        return taskMap;
    }
    boolean validateCommand(String commandString){
        return Arrays.stream(CommandType.values()).anyMatch(command -> command.name().equals(commandString.toUpperCase()));
    }
}

// Task Class
class Task {
    private String name, description, size;
    private LocalDateTime start;
    private Duration totalTime;
    private boolean isRunning;

    // constructor
    public Task(String name) {
        this.name = name;
        this.start = null;
        this.isRunning = false;
        this.totalTime = Duration.ZERO;
        this.description = "N/A";
        this.size = "N/A";
    }

    public String getSummary(Task task) {
        String summary = "Summary for Task  \t:\t " + this.getTaskName() +
                         "\nDescription     \t:\t " + this.description + 
                         "\nTotal Time Spent\t:\t " + DurationUtil.formatTotalTime(this.totalTime) + 
                         "\nTask Size       \t:\t " + this.getSize() + "\n";
        return summary;
    }

    public String getSize() {
        return this.size;
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

    public String getTaskName() {
        return this.name;
    }

    public Duration getDuration(){
        return this.totalTime;
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
            throw new IllegalStateException("Task " + this.name + " is already running");
        }
        this.start = timeStamp;
        isRunning = true;
    }
    public void updateStop(LocalDateTime timeStamp){
        if (!isRunning){
            throw new IllegalStateException("Task " + this.name + " cannot stop since it has not started");
        }
        Duration duration = DurationUtil.calculateTimeDifference(this.start, timeStamp);
        this.start = null;
        this.totalTime = this.totalTime.plus(duration);
        //System.out.println(formatTotalTime(this.totalTime) + "\n");
        this.isRunning = false;
    }

    
}

// maybe we don't need this class, just use durantion methods directly
class DurationUtil {
    protected static Duration calculateTimeDifference(LocalDateTime dateTime1, LocalDateTime dateTime2) {
        return Duration.between(dateTime1, dateTime2);
    }

    protected static String formatTotalTime(Duration duration){
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
