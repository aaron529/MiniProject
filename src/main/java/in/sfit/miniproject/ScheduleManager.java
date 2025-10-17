package in.sfit.miniproject;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ScheduleManager {
    private List<Task> tasks;
    private Map<String, List<PriorityItem>> priorities;
    private Map<String, List<TodoItem>> todos;
    private static final String TASKS_FILE = "tasks.dat";
    private static final String PRIORITIES_FILE = "priorities.dat";
    private static final String TODOS_FILE = "todos.dat";

    public ScheduleManager() {
        tasks = new ArrayList<>();
        priorities = new HashMap<>();
        todos = new HashMap<>();
        loadData();
    }

    public void addTask(Task task) {
        tasks.add(task);
        saveTasks();
    }

    public void deleteTask(Task task) {
        tasks.remove(task);
        saveTasks();
    }

    public List<Task> getTasksForDate(LocalDate date) {
        List<Task> dateTasks = new ArrayList<>();
        for (Task task : tasks) {
            if (task.getDate().equals(date)) {
                dateTasks.add(task);
            }
        }
        dateTasks.sort((t1, t2) -> t1.getTimeSlot().compareTo(t2.getTimeSlot()));
        return dateTasks;
    }

    public boolean hasTasksForDate(LocalDate date) {
        return tasks.stream().anyMatch(task -> task.getDate().equals(date));
    }

    public void addPriority(LocalDate date, PriorityItem priority) {
        String key = date.toString();
        List<PriorityItem> list = priorities.getOrDefault(key, new ArrayList<>());
        if (list.size() < 3) {
            list.add(priority);
            priorities.put(key, list);
            savePriorities();
        }
    }

    public void deletePriority(LocalDate date, PriorityItem priority) {
        String key = date.toString();
        List<PriorityItem> list = priorities.get(key);
        if (list != null) {
            list.remove(priority);
            priorities.put(key, list);
            savePriorities();
        }
    }

    public List<PriorityItem> getPriorities(LocalDate date) {
        return priorities.getOrDefault(date.toString(), new ArrayList<>());
    }

    public void addTodo(LocalDate date, TodoItem todo) {
        String key = date.toString();
        List<TodoItem> list = todos.getOrDefault(key, new ArrayList<>());
        list.add(todo);
        todos.put(key, list);
        saveTodos();
    }

    public void deleteTodo(LocalDate date, TodoItem todo) {
        String key = date.toString();
        List<TodoItem> list = todos.get(key);
        if (list != null) {
            list.remove(todo);
            todos.put(key, list);
            saveTodos();
        }
    }

    public List<TodoItem> getTodos(LocalDate date) {
        return todos.getOrDefault(date.toString(), new ArrayList<>());
    }

    public void saveTasks() {
        saveToFile(TASKS_FILE, tasks);
    }

    private void savePriorities() {
        saveToFile(PRIORITIES_FILE, priorities);
    }

    public void saveTodos() {
        saveToFile(TODOS_FILE, todos);
    }

    private void loadData() {
        tasks = (List<Task>) loadFromFile(TASKS_FILE);
        if (tasks == null) tasks = new ArrayList<>();

        priorities = (Map<String, List<PriorityItem>>) loadFromFile(PRIORITIES_FILE);
        if (priorities == null) priorities = new HashMap<>();

        todos = (Map<String, List<TodoItem>>) loadFromFile(TODOS_FILE);
        if (todos == null) todos = new HashMap<>();
    }

    private void saveToFile(String filename, Object data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename))) {
            oos.writeObject(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Object loadFromFile(String filename) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filename))) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            return null;
        }
    }
}
