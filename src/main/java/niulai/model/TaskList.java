package niulai.model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Owns the tasks currently managed by NiuLai.
 *
 * <p>The underlying collection is kept private so that task-list operations can be
 * changed without exposing collection-management details to the chatbot.</p>
 */
public class TaskList implements Iterable<Task> {
    /** The tasks in their display order. */
    private final ArrayList<Task> tasks;

    /** Creates an empty task list. */
    public TaskList() {
        this.tasks = new ArrayList<>();
    }

    /**
     * Creates a task list containing the supplied tasks.
     *
     * @param tasks the initial tasks
     */
    public TaskList(List<Task> tasks) {
        this.tasks = new ArrayList<>(Objects.requireNonNull(tasks, "tasks"));
    }

    /**
     * Returns the task at the specified zero-based position.
     *
     * @param index the task position
     * @return the task at that position
     */
    public Task get(int index) {
        return tasks.get(index);
    }

    /**
     * Adds a task to the end of the list.
     *
     * @param task the task to add
     */
    public void add(Task task) {
        tasks.add(Objects.requireNonNull(task, "task"));
    }

    /**
     * Inserts a task at the specified position.
     *
     * @param index the insertion position
     * @param task the task to insert
     */
    public void add(int index, Task task) {
        tasks.add(index, Objects.requireNonNull(task, "task"));
    }

    /**
     * Removes and returns the task at the specified position.
     *
     * @param index the task position
     * @return the removed task
     */
    public Task remove(int index) {
        return tasks.remove(index);
    }

    /** @return the number of tasks in the list */
    public int size() {
        return tasks.size();
    }

    /**
     * Finds tasks whose descriptions contain the supplied keyword.
     *
     * @param keyword the case-insensitive text to search for
     * @return matching tasks in their original display order
     */
    public List<Task> findByDescription(String keyword) {
        String normalizedKeyword = Objects.requireNonNull(keyword, "keyword")
                .toLowerCase(Locale.ROOT);
        return tasks.stream()
                .filter(task -> task.getDescription().toLowerCase(Locale.ROOT)
                        .contains(normalizedKeyword))
                .toList();
    }

    /** @return an iterator over tasks in display order */
    @Override
    public Iterator<Task> iterator() {
        return tasks.iterator();
    }
}
