# UI Test Plan

This file is the source of truth for the scripted command-line UI tests. Run it from the repository root with:

```powershell
& "<python-3>" .codex/skills/test-ui/scripts/run_ui_tests.py
```

The runner executes test cases from top to bottom. It supplies the `Inputs` block to the command's standard input, compares the complete merged console output with `Expected output`, and stops immediately at the first failure. Line-ending differences and final newline characters are ignored; spaces inside the output are significant.

Java commands in this plan require JDK 25. The compile step is included in each command so that the plan can be run from a clean checkout.

## Test Case 1: Exit with `bye`

### Aim

Verify that the application starts successfully and exits with the expected farewell when the user enters `bye`.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 2: Reject invalid task numbers without changing completion state

### Aim

Verify that missing, non-numeric, zero, and out-of-range task numbers are rejected, while valid mark and unmark commands update the intended task only.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
todo submit assignment
mark
mark abc
mark 0
mark 1
unmark nope
unmark 2
unmark 1
list
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] submit assignment
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! 'mark' needs a task number, such as 'mark 1'.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task numbers must be positive whole numbers, such as 'mark 1'.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task 0 does not exist. Use 'list' to see your tasks.
    ____________________________________________________________

    ____________________________________________________________
     Nice! I've marked this task as done:
       [T][X] submit assignment
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task numbers must be positive whole numbers, such as 'unmark 1'.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task 2 does not exist. Use 'list' to see your tasks.
    ____________________________________________________________

    ____________________________________________________________
     OK, I've marked this task as not done yet:
       [T][ ] submit assignment
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] submit assignment
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 3: Handle blank and whitespace-padded input

### Aim

Verify that a blank command is rejected without adding a task and that a valid command with leading and internal extra whitespace is parsed correctly.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text

  todo   read book
list
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! I don't recognize that command. Try 'list' to view your tasks.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 4: Add and list all task types

### Aim

Verify that todo, deadline, and event commands preserve their descriptions and date/time strings, display the correct type markers, and appear in the task list.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
todo borrow book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
list
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] borrow book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] borrow book
     2.[D][ ] return book (by: June 6th)
     3.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 5: Explain invalid input

### Aim

Verify that an empty todo description and an unknown command produce helpful error messages and that the application continues accepting commands.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
todo
blah
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! A todo needs a description. Try: todo <description>.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! I don't recognize that command. Try 'list' to view your tasks.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 6: Reject malformed task creation without changing the list

### Aim

Verify that malformed todo, deadline, and event commands are rejected, while valid commands interleaved between them still create exactly the expected tasks.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
todo
todo buy milk
deadline /by tomorrow
deadline submit report /by tomorrow
event team sync /from 10am
event team sync /from 10am /to 11am
list
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! A todo needs a description. Try: todo <description>.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] buy milk
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! A deadline must look like: deadline <description> /by <date or time>.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] submit report (by: tomorrow)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! An event must look like: event <description> /from <start> /to <end>.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] team sync (from: 10am to: 11am)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] buy milk
     2.[D][ ] submit report (by: tomorrow)
     3.[E][ ] team sync (from: 10am to: 11am)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```

## Test Case 7: Delete tasks and validate task numbers

### Aim

Verify that a task can be deleted by its displayed number, that the remaining tasks are renumbered, and that invalid delete commands do not change the list.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/TaskStatus.java src/main/java/Command.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
```

### Inputs

```text
todo read book
deadline return book /by June 6th
event project meeting /from Aug 6th 2pm /to 4pm
delete 2
delete abc
delete 3
delete
list
bye
```

### Expected output

```text
|\ | | |  | |     /\  |
| \| | \__/ |___ /~~\ |

    ____________________________________________________________
     Hello! I'm NiuLai!
     What can I do for you?
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [T][ ] read book
     Now you have 1 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Got it. I've added this task:
       [E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
     Now you have 3 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     Noted. I've removed this task:
       [D][ ] return book (by: June 6th)
     Now you have 2 tasks in the list.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task numbers must be positive whole numbers, such as 'delete 1'.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! Task 3 does not exist. Use 'list' to see your tasks.
    ____________________________________________________________

    ____________________________________________________________
     NOOO!!! 'delete' needs a task number, such as 'delete 1'.
    ____________________________________________________________

    ____________________________________________________________
     Here are the tasks in your list:
     1.[T][ ] read book
     2.[E][ ] project meeting (from: Aug 6th 2pm to: 4pm)
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```
