package in.sfit.miniproject;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.geometry.*;
import javafx.animation.*;
import javafx.util.Duration;
import java.time.*;
import java.time.format.*;
import java.util.*;

public class Main extends Application {
    private ScheduleManager scheduleManager;
    private LocalDate currentDate;
    private LocalDate displayMonth;
    private VBox scheduleContainer;
    private VBox prioritiesArea;
    private VBox todoArea;
    private Label headerDateLabel;
    private GridPane calendarGrid;
    private Label monthYearLabel;
    private Map<LocalDate, Button> dateButtons;

    @Override
    public void start(Stage primaryStage) {
        scheduleManager = new ScheduleManager();
        currentDate = LocalDate.now();
        displayMonth = LocalDate.now();
        dateButtons = new HashMap<>();

        BorderPane root = new BorderPane();
        root.getStyleClass().add("root");

        // Header
        HBox header = createHeader();
        root.setTop(header);

        // Main Content
        HBox mainContent = new HBox(20);
        mainContent.setPadding(new Insets(20));

        // Left - Calendar
        VBox leftPanel = createCalendarPanel();

        // Center - Schedule
        VBox centerPanel = createSchedulePanel();
        HBox.setHgrow(centerPanel, Priority.ALWAYS);

        // Right - Priorities & Todo
        VBox rightPanel = createRightPanel();

        mainContent.getChildren().addAll(leftPanel, centerPanel, rightPanel);
        root.setCenter(mainContent);

        Scene scene = new Scene(root, 1400, 850);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

        primaryStage.setTitle("Student Daily Planner");
        primaryStage.setScene(scene);
        primaryStage.show();

        loadTodaySchedule();

        // Entrance animation
        FadeTransition fade = new FadeTransition(Duration.millis(600), root);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
    }

    private HBox createHeader() {
        HBox header = new HBox();
        header.getStyleClass().add("header");
        header.setPadding(new Insets(20, 30, 20, 30));
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("📚 Student Daily Planner");
        title.getStyleClass().add("header-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        headerDateLabel = new Label();
        headerDateLabel.getStyleClass().add("header-date");
        updateHeaderDate();

        header.getChildren().addAll(title, spacer, headerDateLabel);

        return header;
    }

    private void updateHeaderDate() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, MMMM dd, yyyy");
        headerDateLabel.setText(currentDate.format(formatter));
    }

    private VBox createCalendarPanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("card");
        panel.setPrefWidth(320);
        panel.setPadding(new Insets(20));

        // Calendar Header
        HBox calendarHeader = new HBox(10);
        calendarHeader.setAlignment(Pos.CENTER);

        Button prevMonth = new Button("◀");
        prevMonth.getStyleClass().add("nav-button");

        monthYearLabel = new Label();
        monthYearLabel.getStyleClass().add("month-label");
        updateMonthLabel();
        HBox.setHgrow(monthYearLabel, Priority.ALWAYS);
        monthYearLabel.setMaxWidth(Double.MAX_VALUE);
        monthYearLabel.setAlignment(Pos.CENTER);

        Button nextMonth = new Button("▶");
        nextMonth.getStyleClass().add("nav-button");

        prevMonth.setOnAction(e -> {
            displayMonth = displayMonth.minusMonths(1);
            updateCalendar();
            updateMonthLabel();
            animateCalendar();
        });

        nextMonth.setOnAction(e -> {
            displayMonth = displayMonth.plusMonths(1);
            updateCalendar();
            updateMonthLabel();
            animateCalendar();
        });

        calendarHeader.getChildren().addAll(prevMonth, monthYearLabel, nextMonth);

        // Calendar Grid
        calendarGrid = new GridPane();
        calendarGrid.getStyleClass().add("calendar-grid");
        calendarGrid.setHgap(8);
        calendarGrid.setVgap(8);
        calendarGrid.setAlignment(Pos.CENTER);

        updateCalendar();

        // Today Button
        Button todayButton = new Button("📅 Go to Today");
        todayButton.getStyleClass().add("today-button");
        todayButton.setMaxWidth(Double.MAX_VALUE);
        todayButton.setOnAction(e -> {
            currentDate = LocalDate.now();
            displayMonth = LocalDate.now();
            updateCalendar();
            updateMonthLabel();
            loadTodaySchedule();
            animateCalendar();
        });

        panel.getChildren().addAll(calendarHeader, calendarGrid, todayButton);

        return panel;
    }

    private void updateMonthLabel() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy");
        monthYearLabel.setText(displayMonth.format(formatter));
    }

    private void animateCalendar() {
        ScaleTransition scale = new ScaleTransition(Duration.millis(200), calendarGrid);
        scale.setFromX(0.95);
        scale.setFromY(0.95);
        scale.setToX(1.0);
        scale.setToY(1.0);

        FadeTransition fade = new FadeTransition(Duration.millis(200), calendarGrid);
        fade.setFromValue(0.7);
        fade.setToValue(1.0);

        ParallelTransition parallel = new ParallelTransition(scale, fade);
        parallel.play();
    }

    private void updateCalendar() {
        calendarGrid.getChildren().clear();
        dateButtons.clear();

        // Get first day of month - CHANGE: use displayMonth
        LocalDate firstDay = displayMonth.withDayOfMonth(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue();

        // Add day buttons - CHANGE: use displayMonth
        int daysInMonth = displayMonth.lengthOfMonth();
        LocalDate today = LocalDate.now();

        int row = 1;
        int col = firstDayOfWeek - 1;

        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = displayMonth.withDayOfMonth(day);  // CHANGE: use displayMonth
            Button dayButton = new Button(String.valueOf(day));
            dayButton.getStyleClass().add("day-button");
            dayButton.setMinWidth(38);
            dayButton.setMaxWidth(38);
            dayButton.setMinHeight(38);
            dayButton.setMaxHeight(38);

            if (date.equals(today)) {
                dayButton.getStyleClass().add("today");
            } else if (date.equals(currentDate)) {  // This now correctly compares full dates
                dayButton.getStyleClass().add("selected");
            }

            if (scheduleManager.hasTasksForDate(date)) {
                dayButton.getStyleClass().add("has-tasks");
            }

            final LocalDate selectedDate = date;
            dayButton.setOnAction(e -> {
                currentDate = selectedDate;
                updateHeaderDate();
                loadScheduleForDate(selectedDate);
                updateMonthLabel();
                updateCalendar();
            });

            // Hover animation
            dayButton.setOnMouseEntered(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), dayButton);
                st.setToX(1.1);
                st.setToY(1.1);
                st.play();
            });

            dayButton.setOnMouseExited(e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(100), dayButton);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });

            dateButtons.put(date, dayButton);
            calendarGrid.add(dayButton, col, row);

            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }
    }

    private VBox createSchedulePanel() {
        VBox panel = new VBox(15);
        panel.getStyleClass().add("card");
        panel.setPadding(new Insets(20));

        // Header
        HBox scheduleHeader = new HBox(15);
        scheduleHeader.setAlignment(Pos.CENTER_LEFT);

        Label scheduleLabel = new Label("📋 SCHEDULE");
        scheduleLabel.getStyleClass().add("section-title");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addButton = new Button("+ Add Task");
        addButton.getStyleClass().add("add-button");
        addButton.setOnAction(e -> showAddTaskDialog());

        scheduleHeader.getChildren().addAll(scheduleLabel, spacer, addButton);

        // Schedule Container
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("schedule-scroll");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scheduleContainer = new VBox(10);
        scheduleContainer.setPadding(new Insets(10));
        scrollPane.setContent(scheduleContainer);

        panel.getChildren().addAll(scheduleHeader, scrollPane);

        return panel;
    }

    private VBox createRightPanel() {
        VBox panel = new VBox(20);
        panel.setPrefWidth(320);

        // Priorities Section
        VBox prioritiesBox = new VBox(10);
        prioritiesBox.getStyleClass().add("card");
        prioritiesBox.setPadding(new Insets(20));

        HBox prioritiesHeader = new HBox(10);
        prioritiesHeader.setAlignment(Pos.CENTER_LEFT);

        Label prioritiesLabel = new Label("⭐ TOP 3 PRIORITIES");
        prioritiesLabel.getStyleClass().add("section-title");
        HBox.setHgrow(prioritiesLabel, Priority.ALWAYS);

        Button addPriorityBtn = new Button("+");
        addPriorityBtn.getStyleClass().addAll("icon-button", "complete-button");
        addPriorityBtn.setOnAction(e -> showAddPriorityDialog());

        prioritiesHeader.getChildren().addAll(prioritiesLabel, addPriorityBtn);

        ScrollPane prioritiesScroll = new ScrollPane();
        prioritiesScroll.setFitToWidth(true);
        prioritiesScroll.getStyleClass().add("priority-scroll");
        VBox.setVgrow(prioritiesScroll, Priority.ALWAYS);

        prioritiesArea = new VBox(8);
        prioritiesArea.setPadding(new Insets(5));
        prioritiesScroll.setContent(prioritiesArea);

        prioritiesBox.getChildren().addAll(prioritiesHeader, prioritiesScroll);
        VBox.setVgrow(prioritiesBox, Priority.ALWAYS);

        // Todo Section
        VBox todoBox = new VBox(10);
        todoBox.getStyleClass().add("card");
        todoBox.setPadding(new Insets(20));

        HBox todoHeader = new HBox(10);
        todoHeader.setAlignment(Pos.CENTER_LEFT);

        Label todoLabel = new Label("✓ TO DO LIST");
        todoLabel.getStyleClass().add("section-title");
        HBox.setHgrow(todoLabel, Priority.ALWAYS);

        Button addTodoBtn = new Button("+");
        addTodoBtn.getStyleClass().addAll("icon-button", "complete-button");
        addTodoBtn.setOnAction(e -> showAddTodoDialog());

        todoHeader.getChildren().addAll(todoLabel, addTodoBtn);

        ScrollPane todoScroll = new ScrollPane();
        todoScroll.setFitToWidth(true);
        todoScroll.getStyleClass().add("todo-scroll");
        VBox.setVgrow(todoScroll, Priority.ALWAYS);

        todoArea = new VBox(8);
        todoArea.setPadding(new Insets(5));
        todoScroll.setContent(todoArea);

        todoBox.getChildren().addAll(todoHeader, todoScroll);
        VBox.setVgrow(todoBox, Priority.ALWAYS);

        panel.getChildren().addAll(prioritiesBox, todoBox);

        return panel;
    }

    private void showAddTaskDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add New Task");
        dialog.initOwner(scheduleContainer.getScene().getWindow());

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("dialog");

        GridPane form = new GridPane();
        form.setHgap(15);
        form.setVgap(15);

        Label dateLabel = new Label("📅 Date:");
        dateLabel.getStyleClass().add("form-label");
        DatePicker datePicker = new DatePicker(currentDate);
        datePicker.getStyleClass().add("date-picker");
        dateLabel.setMinWidth(Region.USE_PREF_SIZE);

        Label timeLabel = new Label("🕐 Time Slot:");
        timeLabel.getStyleClass().add("form-label");
        timeLabel.setMinWidth(Region.USE_PREF_SIZE);
        ComboBox<String> timeCombo = new ComboBox<>();
        timeCombo.getStyleClass().add("combo-box");
        for (int i = 0; i < 24; i++) {
            timeCombo.getItems().add(String.format("%02d:00 - %02d:00", i, i + 1));
        }
        timeCombo.setValue(timeCombo.getItems().get(9));

        Label taskLabel = new Label("📝 Task:");
        taskLabel.getStyleClass().add("form-label");
        taskLabel.setMinWidth(Region.USE_PREF_SIZE);
        TextField taskField = new TextField();
        taskField.getStyleClass().add("text-field");
        taskField.setPromptText("Enter task name...");

        Label descLabel = new Label("📄 Description:");
        descLabel.getStyleClass().add("form-label");
        TextArea descArea = new TextArea();
        descLabel.setMinWidth(Region.USE_PREF_SIZE);
        descArea.getStyleClass().add("text-area");
        descArea.setPromptText("Enter description...");
        descArea.setPrefRowCount(3);
        descArea.setWrapText(true);

        form.add(dateLabel, 0, 0);
        form.add(datePicker, 1, 0);
        form.add(timeLabel, 0, 1);
        form.add(timeCombo, 1, 1);
        form.add(taskLabel, 0, 2);
        form.add(taskField, 1, 2);
        form.add(descLabel, 0, 3);
        form.add(descArea, 1, 3);

        GridPane.setHgrow(datePicker, Priority.ALWAYS);
        GridPane.setHgrow(timeCombo, Priority.ALWAYS);
        GridPane.setHgrow(taskField, Priority.ALWAYS);
        GridPane.setHgrow(descArea, Priority.ALWAYS);

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button saveButton = new Button("✓ Save");
        saveButton.getStyleClass().addAll("dialog-button", "save-button");

        Button cancelButton = new Button("✗ Cancel");
        cancelButton.getStyleClass().addAll("dialog-button", "cancel-button");

        saveButton.setOnAction(e -> {
            if (taskField.getText().trim().isEmpty()) {
                showAlert("Error", "Please enter a task name!", Alert.AlertType.ERROR);
                return;
            }

            LocalDate date = datePicker.getValue();
            String timeSlot = timeCombo.getValue();
            String task = taskField.getText();
            String description = descArea.getText();

            Task newTask = new Task(date, timeSlot, task, description);
            scheduleManager.addTask(newTask);

            if (date.equals(currentDate)) {
                loadScheduleForDate(currentDate);
            }
            updateCalendar();

            showAlert("Success", "Task added successfully!", Alert.AlertType.INFORMATION);
            dialog.close();
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(saveButton, cancelButton);

        content.getChildren().addAll(form, buttonBox);

        Scene scene = new Scene(content, 500, 450);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialog.setScene(scene);

        // Dialog entrance animation
        FadeTransition fade = new FadeTransition(Duration.millis(300), content);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();

        dialog.show();
    }

    private void loadTodaySchedule() {
        loadScheduleForDate(LocalDate.now());
    }

    private void loadScheduleForDate(LocalDate date) {
        scheduleContainer.getChildren().clear();
        List<Task> tasks = scheduleManager.getTasksForDate(date);

        if (tasks.isEmpty()) {
            Label emptyLabel = new Label("📭 No tasks scheduled for this day");
            emptyLabel.getStyleClass().add("empty-label");
            scheduleContainer.getChildren().add(emptyLabel);
        } else {
            for (Task task : tasks) {
                HBox taskCard = createTaskCard(task);
                scheduleContainer.getChildren().add(taskCard);

                // Entrance animation
                TranslateTransition translate = new TranslateTransition(Duration.millis(300), taskCard);
                translate.setFromX(-50);
                translate.setToX(0);

                FadeTransition fade = new FadeTransition(Duration.millis(300), taskCard);
                fade.setFromValue(0);
                fade.setToValue(1);

                ParallelTransition parallel = new ParallelTransition(translate, fade);
                parallel.play();
            }
        }

        loadPriorities(date);
        loadTodos(date);
    }

    private void loadPriorities(LocalDate date) {
        prioritiesArea.getChildren().clear();
        List<PriorityItem> priorities = scheduleManager.getPriorities(date);

        if (priorities.isEmpty()) {
            Label emptyLabel = new Label("No priorities set");
            emptyLabel.getStyleClass().add("empty-label-small");
            prioritiesArea.getChildren().add(emptyLabel);
        } else {
            int index = 1;
            for (PriorityItem priority : priorities) {
                HBox priorityCard = createPriorityCard(priority, index++);
                prioritiesArea.getChildren().add(priorityCard);
            }
        }
    }

    private void loadTodos(LocalDate date) {
        todoArea.getChildren().clear();
        List<TodoItem> todos = scheduleManager.getTodos(date);

        if (todos.isEmpty()) {
            Label emptyLabel = new Label("No to-do items");
            emptyLabel.getStyleClass().add("empty-label-small");
            todoArea.getChildren().add(emptyLabel);
        } else {
            for (TodoItem todo : todos) {
                HBox todoCard = createTodoCard(todo);
                todoArea.getChildren().add(todoCard);
            }
        }
    }

    private HBox createPriorityCard(PriorityItem priority, int index) {
        HBox card = new HBox(10);
        card.getStyleClass().add("priority-item");
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);

        Label numberLabel = new Label(index + ".");
        numberLabel.getStyleClass().add("priority-number");
        numberLabel.setMinWidth(25);

        Label textLabel = new Label(priority.getText());
        textLabel.getStyleClass().add("priority-text");
        textLabel.setWrapText(true);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        Button deleteBtn = new Button("✗");
        deleteBtn.getStyleClass().addAll("mini-button", "delete-mini");
        deleteBtn.setOnAction(e -> {
            scheduleManager.deletePriority(currentDate, priority);
            loadPriorities(currentDate);
        });

        card.getChildren().addAll(numberLabel, textLabel, deleteBtn);
        return card;
    }

    private HBox createTodoCard(TodoItem todo) {
        HBox card = new HBox(10);
        card.getStyleClass().add("todo-item");
        if (todo.isCompleted()) {
            card.getStyleClass().add("completed");
        }
        card.setPadding(new Insets(10));
        card.setAlignment(Pos.CENTER_LEFT);

        CheckBox checkBox = new CheckBox();
        checkBox.setSelected(todo.isCompleted());
        checkBox.getStyleClass().add("todo-checkbox");
        checkBox.setOnAction(e -> {
            todo.setCompleted(checkBox.isSelected());
            scheduleManager.saveTodos();
            loadTodos(currentDate);
        });

        Label textLabel = new Label(todo.getText());
        textLabel.getStyleClass().add("todo-text");
        if (todo.isCompleted()) {
            textLabel.setStyle("-fx-strikethrough: true; -fx-text-fill: #888;");
        }
        textLabel.setWrapText(true);
        HBox.setHgrow(textLabel, Priority.ALWAYS);

        Button deleteBtn = new Button("✗");
        deleteBtn.getStyleClass().addAll("mini-button", "delete-mini");
        deleteBtn.setOnAction(e -> {
            scheduleManager.deleteTodo(currentDate, todo);
            loadTodos(currentDate);
        });

        card.getChildren().addAll(checkBox, textLabel, deleteBtn);
        return card;
    }

    private void showAddPriorityDialog() {
        List<Task> todayTasks = scheduleManager.getTasksForDate(currentDate);

        if (todayTasks.isEmpty()) {
            showAlert("No Tasks", "Please add some tasks first before setting priorities!", Alert.AlertType.INFORMATION);
            return;
        }

        List<PriorityItem> currentPriorities = scheduleManager.getPriorities(currentDate);
        if (currentPriorities.size() >= 3) {
            showAlert("Limit Reached", "You can only set 3 priorities per day!", Alert.AlertType.WARNING);
            return;
        }

        Stage dialog = new Stage();
        dialog.setTitle("Add Priority");
        dialog.initOwner(scheduleContainer.getScene().getWindow());

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("dialog");

        Label instruction = new Label("Select a task to add as priority:");
        instruction.getStyleClass().add("form-label");

        ComboBox<String> taskCombo = new ComboBox<>();
        taskCombo.getStyleClass().add("combo-box");
        taskCombo.setMaxWidth(Double.MAX_VALUE);

        for (Task task : todayTasks) {
            taskCombo.getItems().add(task.getTaskName());
        }

        if (!taskCombo.getItems().isEmpty()) {
            taskCombo.setValue(taskCombo.getItems().get(0));
        }

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addButton = new Button("✓ Add");
        addButton.getStyleClass().addAll("dialog-button", "save-button");

        Button cancelButton = new Button("✗ Cancel");
        cancelButton.getStyleClass().addAll("dialog-button", "cancel-button");

        addButton.setOnAction(e -> {
            String selectedTask = taskCombo.getValue();
            if (selectedTask != null && !selectedTask.isEmpty()) {
                PriorityItem priority = new PriorityItem(selectedTask);
                scheduleManager.addPriority(currentDate, priority);
                loadPriorities(currentDate);
                dialog.close();
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(addButton, cancelButton);
        content.getChildren().addAll(instruction, taskCombo, buttonBox);

        Scene scene = new Scene(content, 400, 200);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private void showAddTodoDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Add To-Do Item");
        dialog.initOwner(scheduleContainer.getScene().getWindow());

        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.getStyleClass().add("dialog");

        Label instruction = new Label("Enter to-do item:");
        instruction.getStyleClass().add("form-label");

        TextField todoField = new TextField();
        todoField.getStyleClass().add("text-field");
        todoField.setPromptText("e.g., Buy groceries, Call mom...");

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);

        Button addButton = new Button("✓ Add");
        addButton.getStyleClass().addAll("dialog-button", "save-button");

        Button cancelButton = new Button("✗ Cancel");
        cancelButton.getStyleClass().addAll("dialog-button", "cancel-button");

        addButton.setOnAction(e -> {
            String text = todoField.getText().trim();
            if (!text.isEmpty()) {
                TodoItem todo = new TodoItem(text);
                scheduleManager.addTodo(currentDate, todo);
                loadTodos(currentDate);
                dialog.close();
            } else {
                showAlert("Error", "Please enter a to-do item!", Alert.AlertType.ERROR);
            }
        });

        cancelButton.setOnAction(e -> dialog.close());

        buttonBox.getChildren().addAll(addButton, cancelButton);
        content.getChildren().addAll(instruction, todoField, buttonBox);

        Scene scene = new Scene(content, 400, 200);
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    private HBox createTaskCard(Task task) {
        HBox card = new HBox(15);
        card.getStyleClass().add("task-card");
        if (task.isCompleted()) {
            card.getStyleClass().add("completed");
        }
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.CENTER_LEFT);

        VBox timeBox = new VBox(5);
        timeBox.setAlignment(Pos.CENTER);
        timeBox.getStyleClass().add("time-box");
        Label timeLabel = new Label(task.getTimeSlot());
        timeLabel.getStyleClass().add("time-label");
        timeBox.getChildren().add(timeLabel);

        VBox contentBox = new VBox(5);
        HBox.setHgrow(contentBox, Priority.ALWAYS);

        Label taskLabel = new Label(task.getTaskName());
        taskLabel.getStyleClass().add("task-label");
        if (task.isCompleted()) {
            taskLabel.setStyle("-fx-text-fill: #888; -fx-strikethrough: true;");
        }

        Label descLabel = new Label(task.getDescription());
        descLabel.getStyleClass().add("desc-label");
        descLabel.setWrapText(true);

        contentBox.getChildren().addAll(taskLabel, descLabel);

        HBox actionBox = new HBox(8);
        actionBox.setAlignment(Pos.CENTER_RIGHT);

        Button completeButton = new Button(task.isCompleted() ? "↺" : "✓");
        completeButton.getStyleClass().addAll("icon-button", "complete-button");
        completeButton.setOnAction(e -> {
            task.setCompleted(!task.isCompleted());
            scheduleManager.saveTasks();
            loadScheduleForDate(currentDate);
            animateButton(completeButton);
        });

        Button deleteButton = new Button("🗑");
        deleteButton.getStyleClass().addAll("icon-button", "delete-button");
        deleteButton.setOnAction(e -> {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setTitle("Confirm Delete");
            confirm.setHeaderText("Delete Task");
            confirm.setContentText("Are you sure you want to delete this task?");

            confirm.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    scheduleManager.deleteTask(task);
                    loadScheduleForDate(currentDate);
                    updateCalendar();
                }
            });
        });

        actionBox.getChildren().addAll(completeButton, deleteButton);

        card.getChildren().addAll(timeBox, contentBox, actionBox);

        return card;
    }

    private void animateButton(Button button) {
        ScaleTransition scale = new ScaleTransition(Duration.millis(100), button);
        scale.setFromX(1.0);
        scale.setFromY(1.0);
        scale.setToX(0.9);
        scale.setToY(0.9);
        scale.setAutoReverse(true);
        scale.setCycleCount(2);
        scale.play();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public static void main(String[] args) {
        launch(args);
    }
}