import java.io.*;
import java.time.*;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.*;
import java.util.stream.Collectors;

public class TM {
    public static void main(String[] args) throws IOException {
        if(args.length >= 1 && args.length <= 4) {
            TaskManager tm = TaskManager.getInstance();
            tm.run(args);
        } else {
            System.out.println("Usage: java TM <command> <data>\n" +
                                "For a list of commands, type help");
        }
    }
}

enum CommandType {
    START, STOP, DESCRIBE, SUMMARY, SIZE, RENAME, DELETE, HELP;
}

interface Command {
    final String[] SIZES = {"S", "M", "L", "XL"};
    final String HELPSTRING = "For a list of commands, type help";
    void execute(String[] input, Map<String, Task> taskMap) throws IOException;
    void checkCommandFormat(String[] input);
    Task parseLine(String[] input, Task task);
}

class StartCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) 
                                                            throws IOException {
        String taskName = input[1].toUpperCase();
        boolean anyRunning = taskMap.values().stream()
                            .anyMatch(task -> task != null && task.isRunning());
        if (anyRunning)
            throw new IllegalStateException("Cannot start a new task while " + 
                                            "another task is already running.");
        checkCommandFormat(input);
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + 
                            "\tStart\t\""+ taskName + "\"");
    }

    @Override
    public void checkCommandFormat(String[] input) {
        if (input.length != 2) {
            throw new IllegalStateException("Usage: java TM.java start " + 
                                            "<task name>\n" + HELPSTRING);
        }
        Set<String> sizes = new HashSet<>(Arrays.asList("S","M","L","XL"));
        if (sizes.contains(input[1].toUpperCase())) {
            throw new IllegalStateException("Error: Sizes [S, M, L, XL] " + 
                                            "cannot be used as names");
        }
    }

    public Task parseLine(String[] logLine, Task task) {
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        String taskName = logLine[2];
        
        if (task != null) {
            if (task.isRunning()) {
                throw new IllegalStateException("Task " + taskName + 
                                                " is already running at line ");
            }
            task.updateStart(timeStamp);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.updateStart(timeStamp);
            return newTask;
        }
    }
}

class StopCommand implements Command {    
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) 
                                                            throws IOException {
        String taskName = input[1].toUpperCase();
        checkCommandFormat(input);
        if(!taskMap.containsKey(taskName)) {
            throw new IllegalStateException("Task " + taskName + 
                                            " does not exist");
        }
        else if (!taskMap.get(taskName).isRunning()) {
            throw new IllegalStateException("Task " + taskName + 
                                            " is not running");
        }
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + 
                            "\tStop\t\"" + taskName + "\"");
    }

    @Override
    public void checkCommandFormat(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java stop" + 
                                            "<task name>\n" + HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        LocalDateTime timeStamp = LocalDateTime.parse(logLine[0]);
        if (task != null) {
            task.updateStop(timeStamp);
        } else {
            throw new IllegalStateException("No existing task for STOP " +
                                            "command at File Line ");
        }
        return null;
    }
}

class DescribeCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) 
                                                            throws IOException {
        checkCommandFormat(input);
        String taskName = input[1].toUpperCase();
        String log = "\"" + taskName  + "\"" + "\t\"" + input[2] + "\"";

        if (input.length == 4) {
            String size = input[3].toUpperCase();
            if (Arrays.asList(SIZES).contains(size)) {
                log += "\t" + size;
            } else {
                System.err.println("Invalid size, description will be applied" +
                " and size will be ignored...\n" + "Please enter a valid size" +
                " next time (S, M, L, XL)");
            }
        }
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + 
                                                    "\tDescribe\t" + log);
    }

    @Override
    public void checkCommandFormat(String[] input) {
        if (input.length < 2 || input.length > 4) {
            throw new IllegalStateException("Usage: java TM.java describe " +
                                            " <task name> " +
                                            "<description> [{S|M|L|XL}]\n" +
                                            HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        String taskName = logLine[2];
        String description = logLine[3];
        String size = "N/A";

        if (logLine.length == 5) {
            size = logLine[4];
        }
        if (task != null) {
            task.updateDescription(description);
            if (logLine.length == 5)
                task.updateSize(size);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.updateDescription(description);
            newTask.updateSize(size);
            return newTask;
        }
    }
}
class SizeCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) 
                                                        throws IOException {
        checkCommandFormat(input);
        String taskName = input[1].toUpperCase();
        String size = input[2].toUpperCase();   
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + 
                            "\tSize\t\"" + taskName  + "\"\t" + size);
    }

    @Override
    public void checkCommandFormat(String[] input) {
        boolean isProper = input.length == 3;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java size "+
                                            " <task name> " +
                                            "{S|M|L|XL}\n" + HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        String taskName = logLine[2];
        String taskSize = logLine[3];
        if (task != null) {
            task.updateSize(taskSize);
            return null;
        } else {
            Task newTask = new Task(taskName);
            newTask.updateSize(taskSize);
            return newTask;
        }
    }
}

class RenameCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) 
                                                throws IOException {
        String taskNameOld = input[1].toUpperCase();
        String taskRename = input[2].toUpperCase();
        checkCommandFormat(input);
        if (!taskMap.containsKey(taskNameOld))
            throw new IllegalStateException("Cannot rename nonexistent task");
        if (taskMap.containsKey(taskRename))
            throw new IllegalStateException("Cannot rename task to existing " +
                                            "task name");
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) +
                    "\tRename\t\"" + taskNameOld + "\"\t\"" + taskRename + "\"");
    }

    @Override
    public void checkCommandFormat(String[] input) {
        boolean isProper = input.length == 3;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java rename" +
                            "<old task name> <new task name>\n" + HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task task) {
        if (task == null) {
            throw new IllegalStateException("Can't rename a task that does"
                                            + "not exist");
        }
        String newTaskName = logLine[3];
        task.setName(newTaskName);
        return task;
    }
}

class DeleteCommand implements Command {
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) throws 
                        IOException {
        String taskName = input[1].toUpperCase();
        checkCommandFormat(input);
        if (!taskMap.containsKey(taskName))
            throw new IllegalStateException("Can't delete nonexistent task");
        FileUtil.writeToFile(LocalDateTime.now().withNano(0) + 
                                            "\tDelete\t\"" +  taskName + "\"");
    }

    @Override
    public void checkCommandFormat(String[] input) {
        boolean isProper = input.length == 2;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java delete"+
                                            "<task name> \n" + HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        if (existingTask == null) {
            throw new IllegalStateException("Can't delete a task that does " +
                                            "not exist");
        }
        existingTask.setName(null);
        return existingTask;
    }
}

class HelpCommand implements Command {
    private static final String HELP_MESSAGE = 
        "Usage: java TM.java <command>\nCommands:\nstart <task name>\n" +
        "stop <task name>\ndescribe <task name> <\"description\"> " + 
        "[{S|M|L|XL}]\nsize <task name> {S|M|L|XL}\nrename <old task name> " + 
        "<new task name>\ndelete <task name>\nsummary " + 
        "[<task name> | {S|M|L|XL}]\nhelp\n";
    @Override
    public void execute(String[] input, Map<String, Task> taskMap) {
        checkCommandFormat(input);
        System.out.println(HELP_MESSAGE);
    }
   
    @Override
    public void checkCommandFormat(String[] input) {
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

class SummaryCommand implements Command {
    private Map<String, Task> taskMap;
    private Predicate<Task> summaryPredicate;

    @Override
    public void execute(String[] input, Map<String, Task> taskMap) {
        this.taskMap = taskMap;
        checkCommandFormat(input);
        if (input.length == 1) {
            this.summaryPredicate = task -> true;
        } else if (Arrays.asList(SIZES).contains(input[1].toUpperCase())) {
            this.summaryPredicate = task -> task.getSize()
                                                .equals(input[1].toUpperCase());
        } else {
            this.summaryPredicate = task -> task.getTaskName()
                                                .equals(input[1].toUpperCase());
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
    public void checkCommandFormat(String[] input) {
        boolean isProper = input.length == 1 || input.length == 2;
        if (!isProper) {
            throw new IllegalStateException("Usage: java TM.java summary " + 
                                            "[<task name> | {S|M|L|XL}]\n" +
                                            HELPSTRING);
        }
        else if (input.length == 2 &&
                !Arrays.asList(SIZES).contains(input[1].toUpperCase()) && 
                !taskMap.containsKey(input[1].toUpperCase())) {
                throw new IllegalStateException("Invalid size or task for " + 
                                              "summary command\n" + HELPSTRING);
        }
    }

    @Override
    public Task parseLine(String[] logLine, Task existingTask) {
        throw new IllegalStateException("Illegal Log Line Command Summary");
    }
}

class SummaryProcessor {
    private static Task getCurrentRunningTask(List<Task> tasks) {
        return tasks.stream().filter(Task::isRunning)
                             .findFirst().orElse(null);
    }

    private static Duration calculateTotalTime(List<Task> tasks) {
        return tasks.stream()
                    .map(Task::getDuration)
                    .reduce(Duration.ZERO, Duration::plus);
    }

    private static Duration calculateAverageTime(List<Task> tasks) {
        List<Task> nonZeroTasks = tasks.stream()
                .filter(task -> task.getDuration().toMillis() > 0).toList();
        Duration totalTime = calculateTotalTime(nonZeroTasks);
        return totalTime.dividedBy(nonZeroTasks.size());
    }

    private static Duration calculateMinTime(List<Task> tasks) {
        return tasks.stream().filter(task -> !task.getDuration()
                    .equals(Duration.ZERO)).map(Task::getDuration)
                    .min(Duration::compareTo).orElse(Duration.ZERO);
    }

    private static Duration calculateMaxTime(List<Task> tasks) {
        return tasks.stream().map(Task::getDuration)
                    .max(Duration::compareTo).orElse(Duration.ZERO);
    }

    private static void displayTaskOverview(List<Task> tasks){
        Duration minDuration = calculateMinTime(tasks);
        Duration maxDuration = calculateMaxTime(tasks);
        Duration avgDuration = calculateAverageTime(tasks);

        System.out.println("Min Duration of Started Tasks:     \t" + 
            DurationUtil.formatTotalTime(minDuration) +
            "\nMax Duration of Started Tasks:     \t" + 
            DurationUtil.formatTotalTime(maxDuration)+ 
            "\nAverage Duration of Started Tasks: \t" + 
            DurationUtil.formatTotalTime(avgDuration));
    }

    public static void printSummary(List<Task> tasks) {
        System.out.println("Summary:\n");

        if (getCurrentRunningTask(tasks) != null) {
            System.out.println("Current Running Task: \t" + 
                            getCurrentRunningTask(tasks).getTaskName() + "\n");
        }
        
        tasks.forEach(task -> System.out.println(task.getSummary())); 

        if (tasks.size() > 1) {
            displayTaskOverview(tasks);
        }
    }
}

class FileUtil {
    private static final String LOGFILE = "TM.log";

    public static void writeToFile(String content) throws IOException {
        FileWriter writer = new FileWriter(LOGFILE, true);
        writer.write(content + '\n');
        writer.close();
    }

    public static List<String[]> convertLogToList() throws FileNotFoundException {
        List<String[]> lines = new ArrayList<>();
        File file = new File(LOGFILE); 
        Scanner scanner = new Scanner(file);

        while (scanner.hasNext()) {
            String line = scanner.nextLine().trim().toUpperCase();
            if (line.isEmpty())
                continue;
            String[] lineArray = parseLine(line);
            lines.add(lineArray);
        }
        scanner.close();
        return lines;
    }

    private static String[] parseLine(String line) {
        List<String> lineList = new ArrayList<>();
        Matcher matchPattern = Pattern.compile("([^\"]\\S*|\".+?\")\\s*")
                           .matcher(line);
        while (matchPattern.find()) {
            lineList.add(matchPattern.group(1)
                      .replace("\"", ""));
        }
        return lineList.toArray(new String[0]);
    }
}

class TaskManager {
    private static TaskManager instance;
    private Map<CommandType, Command> commandMap; 
    private Map<String, Task> taskMap;
    
    private TaskManager() throws FileNotFoundException {
        this.commandMap = CommandMapFactory.createCommandMap();
        this.taskMap = TaskMapProcessor.createTaskMap(commandMap);
    }

    public void run(String[] input) throws IOException {
        String commandString = input[0].toUpperCase();

        try {
            validateCommand(commandString, "");
            CommandType action = CommandType.valueOf(commandString);
            Command command = commandMap.get(action);
            command.execute(input, this.taskMap);
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());
            System.exit(1);
        }
    }

    public static TaskManager getInstance() throws FileNotFoundException{
        if(instance == null) {
            instance = new TaskManager();
        }
        return instance;
    }

    protected static void validateCommand(String commandString,
                                          String errorSuffix) {
        if (!Arrays.stream(CommandType.values())
                    .anyMatch(command -> command.name()
                    .equals(commandString.toUpperCase()))) {
            throw new IllegalStateException("Invalid command " + commandString);
        }
    }
}

class Task {
    private String name, description, size;
    private LocalDateTime start;
    private Duration totalTime;
    private boolean isRunning;

    public Task(String name) {
        this.name = name;
        this.start = null;
        this.isRunning = false;
        this.totalTime = Duration.ZERO;
        this.description = "N/A";
        this.size = "N/A";
    }

    public void setName(String newTaskName) {
        this.name = newTaskName;
    }

    public void updateStart(LocalDateTime timeStamp) {
        if (isRunning) {
            throw new IllegalStateException("Task " + this.name + 
                                            " is already running");
        }
        this.start = timeStamp;
        this.isRunning = true;
    }

    public void updateStop(LocalDateTime timeStamp) {
        if (!isRunning) {
            throw new IllegalStateException("Task " + this.name + 
                                       " cannot stop since it has not started");
        }
        Duration duration = Duration.between(this.start, timeStamp);
        this.start = null;
        this.totalTime = this.totalTime.plus(duration);
        this.isRunning = false;
    }

    public void updateDescription(String description) {
        if (description != null)
            this.description = description;
    }

    public void updateSize(String taskSize) {
        if (size != null)
            this.size = taskSize;
    }

    public String getSummary() {
        return "Summary for Task  \t:\t " + this.name +
                "\nDescription     \t:\t " + this.description + 
                "\nTotal Time Spent\t:\t " + DurationUtil
                    .formatTotalTime(this.totalTime) + 
                "\nTask Size       \t:\t " + this.size + "\n";
    }

    public String getSize() {
        return this.size;
    }

    public String getTaskName() {
        return this.name;
    }

    public Duration getDuration() {
        return this.totalTime;
    }

    public boolean isRunning() {
        return this.isRunning;
    }
}

class DurationUtil {
    protected static String formatTotalTime(Duration duration){
        return String.format("%02d:%02d:%02d", duration.toHours(), 
                            duration.toMinutesPart(), duration.toSecondsPart());
    }
}

class TaskMapProcessor {
    private static Map<String, Task> taskMap = new HashMap<>();
    private static List<String[]> logList;
    private static Map<CommandType, Command> commandMap;

    public static Map<String, Task> createTaskMap(Map <CommandType, Command> 
                                        commands) throws FileNotFoundException {
        commandMap = commands;
        logList = FileUtil.convertLogToList();
        int lineNumber = 0;
        for (String logLine[] : logList) {
            lineNumber++;
            try {
                processLogLine(logLine, lineNumber, taskMap);
            } catch (IllegalStateException e) {
                System.out.println(e.getMessage() + " " + lineNumber);
                System.exit(1);
            }
        }
        taskMap.values().removeIf(element -> element.getTaskName() == null);
        return taskMap;
    }

    private static void processLogLine(String[] logLine, int lineNumber, 
                                        Map<String, Task> taskMap) {
        String commandString = logLine[1];
        TaskManager.validateCommand(commandString, " at File Line " + 
                                    lineNumber);
        String taskName = logLine[2];
        Task existingTask = taskMap.get(taskName);
        CommandType action = CommandType.valueOf(commandString.toUpperCase());
        Command command = commandMap.get(action);

        if (existingTask == null){
            Task returnedTask = command.parseLine(logLine, null); 
            taskMap.put(taskName, returnedTask);
        }
        else {
            removeExistingTask(logLine, existingTask, command);
        }
    }

    private static void removeExistingTask(String[] logLine, Task existingTask, 
                                           Command command) {
        String nameToRemove = existingTask.getTaskName();
        Task returnedTask = command.parseLine(logLine, existingTask);
        if (returnedTask != null) {
            taskMap.put(returnedTask.getTaskName(), returnedTask);
            taskMap.remove(nameToRemove);
        }
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