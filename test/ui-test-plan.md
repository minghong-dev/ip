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
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
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

## Test Case 2: Add and list all task types

### Aim

Verify that todo, deadline, and event commands preserve their descriptions and date/time strings, display the correct type markers, and appear in the task list.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
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

## Test Case 3: Explain invalid input

### Aim

Verify that an empty todo description and an unknown command produce helpful error messages and that the application continues accepting commands.

### Command

```text
javac -d out src/main/java/NiuLai.java src/main/java/NiuLaiException.java src/main/java/Task.java src/main/java/Todo.java src/main/java/Deadline.java src/main/java/Event.java && java -cp out NiuLai
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
     OOPS!!! A todo needs a description. Try: todo <description>.
    ____________________________________________________________

    ____________________________________________________________
     OOPS!!! I don't recognise that command. Try 'list' to view your tasks.
    ____________________________________________________________

    ____________________________________________________________
     Bye. Hope not to see you again.
    ____________________________________________________________
```
