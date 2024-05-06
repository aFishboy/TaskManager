Task Manager

Usage:
To use the task manager open a command line and type: java TM.java <command> to run the task manager. For a full list of commands, see the commands.

Problem Statement:
Our goal was to create a lightweight, easy-to-use command line program to allow the user to track, modify, and summarize their day-to-day tasks. Users will need to start and stop tasks easily. Be able to describe and modify started task descriptions and names. Users should be able to associate a size with a task and delete any task. A summary should be given for all tasks, a specific or all tasks within a specific size category.

Implementation:

Program Input:
On running the program the command line is stored in an array of strings to be further processed. A simple check is done on the input to ensure an allowed amount of arguments are given. Once validated a task manager object is created and passed the arguments to run the program.

Task Manager:
We made our Task Manager class follow a singleton pattern as it makes sense for only one instance of it to exist in our program. The class has a high-level job of managing tasks and their respective commands. It does this through delegating the work needed to run the program. Once created, it receives a map from the CommandMapFactory class, this allows it to map command names given through user input to the respective class. We decided on a command map factory as it allows for better readability and modularity. If in the future a new command was needed, the task manager object would not need to be altered. The task manager also creates a map assigning each task to its respective task object containing all its information. Once it has obtained the necessary maps to run a command the task manager then executes the command given as input by calling the execute command with the class associated with that command. The execution of commands was implemented using the command pattern. This allowed for the removal of a long switch statement that would grow as more commands were needed. Using the command pattern allows the task manager to not need modification and only a new command class to be called is the only change needed. Now that the task manager has a command it validates, checks the format, and finally executes it in the command class. 

Task Class:
The task class is responsible for storing all the data needed per task: its name, description, size, total time, and state. The name is set upon initialization of a task, while everything else is set to their default values. Name, size, and description can be updated using setters, as these are necessary for specific commands such as “rename”, “size”, and “describe”. On the other hand, the total duration and state cannot be directly changed as they can only be updated between each update start and stop method. The tasks were implemented this way to better encapsulate the data. This allows an easy way for the task manager to keep track of the current tasks since they are stored on a map.

Command Logging & Execution:
Each command execution (besides summary and help) results in the modification of the log file. Due to the implementation of the command pattern, each command can have a unique implementation. This also allows for flexible input validation. Once each input has been validated the log file can be updated with the timestamp and command that was executed. Each successful execution of a command will be written in a log file called TM.log and will be in the format: [execution time] <command>. The command structure follows a similar format to the user input as seen in the table in the Commands section with the exceptions of names and descriptions being enclosed in quotation marks to allow multi-worded names and descriptions processing. We assumed that the user editing the log file should know how to edit it in the correct format, otherwise, it should be left untouched. Despite this, we still considered some log file editing errors, so that it will be easier to debug from a user perspective.

Task Creation and Naming:
The Start command creates the task that is needed to be tracked if it doesn’t exist. Alternatively, commands such as Describe and Size also allow the user to create a new task. We decided to implement it this way, as the user may think of tasks to do before actually needing to start them. We also decided to prevent these commands from being created or renamed with names that are sizes as it will just confuse the user especially when trying to run the Summary command with a filter.

Task Processor:
To create the map of task names to their task object, the log file must be converted. This is done by reading the log file line by line and translating it to a task map. We chose this approach because it allowed us to reuse the code we implemented for the command pattern. Once the line is received the command is extracted and the respective parse command is called with the line as an argument. Since each command needs to be handled uniquely, this approach allows each specific command to be created, modified, or deleted depending on its needs. This strategy also simplifies further additions since each command is not coupled and would only require the addition of a new command class and command in the command map.

Describe Command:
In our implementation, we decided to allow users to input an optional size parameter on top of the description. Instead of throwing an error if the user inputs the wrong size, we decided that the user should be able to still set the description, without changing the size. This way the user won’t have to rewrite the description, which can be especially useful if not implemented in a command line. Instead of an error, the user would just receive a warning notifying that the task description has been updated but not the size and would inform the user what are the correct sizes.

Summary Command:
The summary command gets executed when any of the types of summaries are input. To filter which tasks to print in each summary a predicate is utilized. This was chosen for its ease of use and it allows for adding more predicates in the future if the features are needed. Once the tasks have been filtered each task gets called to print its summary. This allows the summary command to delegate the printing of each task summary to the respective task. For summaries of more than one task the total, minimum, maximum, and average time of the set of summarized tasks is also printed. When calculating these values only started tasks are included in the calculation to not skew the results with tasks that have not been started while on the other hand, if there is, the current running task will be included in the calculation. 

Help Command:
A help command is provided to help the user know what commands are available and how to format them.

Error Handling:
There are three main cases where our program will output an error: invalid command, formatting, and task conflicts. These formatting errors are thrown whenever there is a formatting error in either the command line input or the log. If an invalid command is inputted, it will print an error message with the help message to guide the user on what proper commands to use while if it is parsed in the log file, it will print an error message and the corresponding line.  If a formatting error is found on the command line, an error message will be printed containing the command’s usage and a reference to the help command while if found on the log file, an error message will be printed containing the line where the error occurred. Task conflicts will print the appropriate messages depending on the context some of these include: when a user tries running a task already running, deleting/creating/updating a task that doesn’t exist, and stopping a task that is already stopped.

Appendix:

Commands:

To use a command type: java TM.java <command>
Multi-word names and descriptions should be enclosed with quotations.

start <task name>  -- Logs start time of the given task

stop <task name> -- Logs stop time of the given task

describe <task name> <description> [{S|M|L|XL}] -- Logs the description and optional size of a given task

size <task name> {S|M|L|XL} -- Logs the size of a given task

rename <old task name>  <new task name> -- Renames a task

delete <task name> -- Deletes given task

summary [<task name> | {S|M|L|XL}] -- Gives a summary of all tasks or optional single tasks or a subset of class sizes and also shows the currently running task.

help -- Displays usage and list of commands
