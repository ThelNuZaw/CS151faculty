package s25.cs151.application;

import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.*;

public class officeHoursController {

    @FXML
    private ComboBox<String> semesterDropdown;

    @FXML
    private TextField yearInput;

    @FXML
    private CheckBox mondayCheckbox;

    @FXML
    private CheckBox tuesdayCheckbox;

    @FXML
    private CheckBox wednesdayCheckbox;

    @FXML
    private CheckBox thursdayCheckbox;

    @FXML
    private CheckBox fridayCheckbox;

    @FXML
    private String year;
    @FXML
    private String days;
    @FXML
    private String semester;

    @FXML
    public void initialize() {
        semesterDropdown.getItems().addAll("Spring", "Summer", "Fall", "Winter");
        semesterDropdown.setValue("Spring");


        yearInput.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                yearInput.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    @FXML
    protected void onSubmitButtonClick() {
        if (semesterDropdown.getValue() == null || yearInput.getText().isEmpty() || !yearInput.getText().matches("\\d{4}") || !mondayCheckbox.isSelected() && !tuesdayCheckbox.isSelected() &&
                !wednesdayCheckbox.isSelected() && !thursdayCheckbox.isSelected() &&
                !fridayCheckbox.isSelected()) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Input Error");
            alert.setHeaderText("Missing Required Fields");
            alert.setContentText("Please select a semester and enter a year.");
            alert.showAndWait();
            return;
        }
        year = yearInput.getText();
        semester = semesterDropdown.getSelectionModel().getSelectedItem();

        StringBuilder selectedDays = new StringBuilder("Selected days: ");
        boolean weekdaySelected = false;

        if (mondayCheckbox.isSelected()) {
            selectedDays.append("Monday, ");
            weekdaySelected = true;
        }
        if (tuesdayCheckbox.isSelected()) {
            selectedDays.append("Tuesday, ");
            weekdaySelected = true;
        }
        if (wednesdayCheckbox.isSelected()) {
            selectedDays.append("Wednesday, ");
            weekdaySelected = true;
        }
        if (thursdayCheckbox.isSelected()) {
            selectedDays.append("Thursday, ");
            weekdaySelected = true;
        }
        if (fridayCheckbox.isSelected()) {
            selectedDays.append("Friday, ");
            weekdaySelected = true;
        }

        if (weekdaySelected) {
            selectedDays.setLength(selectedDays.length() - 2);
        } else {
            selectedDays.append("None");
        }
        days = selectedDays.toString();
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Office Hours Set");
        alert.setHeaderText("Reciept of Submision");
        alert.setContentText("Year: " + yearInput.getText() + "\nSemester: " + semesterDropdown.getValue() +
                "\n" + selectedDays.toString());
        alert.showAndWait();
    try {
        writeToCSVFile();
    } catch (
    FileNotFoundException e) {
        throw new RuntimeException(e);
    }

    //Read CSV data
    ObservableList<ObservableList<String>> data = readCSVFile();

    //Create the TableView
    TableView<ObservableList<String>> table = createTableView(data);

    Stage stage2 = new Stage();

    VBox container = new VBox(); // Use a resizable layout
        container.getChildren().add(table); // Add the TableView to the layout
        container.setPrefSize(700, 400); // Set the preferred size of the container

        table.prefWidthProperty().bind(container.widthProperty());
        table.prefHeightProperty().bind(container.heightProperty());

    Scene scene2 = new Scene(container); // Use the VBox as the root

        stage2.setTitle("Faculty office hours");
        stage2.setScene(scene2);
        stage2.show();

}

public void writeToCSVFile() throws FileNotFoundException {
    File csvFile = new File("officeHours.csv");
    try (FileWriter fw = new FileWriter(csvFile, true); // Open file in append mode
         BufferedWriter bw = new BufferedWriter(fw);
         PrintWriter out = new PrintWriter(bw)) {

        out.println(year + ", " + semester + ",\"" + days + "\"");
    } catch (IOException e) {
        e.printStackTrace(); // Handle potential exceptions
    }
}

public ObservableList<ObservableList<String>> readCSVFile() {
    ObservableList<ObservableList<String>> data = FXCollections.observableArrayList();
    try (BufferedReader br = new BufferedReader(new FileReader("officeHours.csv"))) {
        String line;
        while ((line = br.readLine()) != null) {
            ObservableList<String> row = FXCollections.observableArrayList();
            String[] values = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String value : values) {
                row.add(value.replaceAll("^\"|\"$", "")); // Remove quotes if present
            }
           // data.add(row);
            if (row.size() == 3) {
                data.add(row);
            } else {
                System.err.println("Skipping malformed row: " + row);
            }
        }
        FXCollections.sort(data, (row1, row2) -> {
            int year1 = Integer.parseInt(row1.getFirst());
            int year2 = Integer.parseInt(row2.getFirst());

            if (year1 != year2) {
                return Integer.compare(year2, year1); // descending by year
            }

            int sem1 = TrackSemester(row1.get(1));
            int sem2 = TrackSemester(row2.get(1));
            return Integer.compare(sem2, sem1); // descending by semester
        });


    } catch (IOException e) {
        e.printStackTrace();
    }
    return data;
}
@FXML
private int TrackSemester(String semester) {
        return switch (semester.trim()) {
            case "Spring" -> 4;
            case "Summer" -> 3;
            case "Fall"   -> 2;
            case "Winter" -> 1;
            default       -> 0;
        };
    }



public static TableView<ObservableList<String>> createTableView(ObservableList<ObservableList<String>> data) {
    TableView<ObservableList<String>> table = new TableView<>();
    String[] columnTitles = {"Year", "Semester", "Selected Day(s)"};
    if (!data.isEmpty()) {
        int columnCount = data.getFirst().size();
        for (int i = 0; i < columnCount; i++) {
            final int colIndex = i;
            TableColumn<ObservableList<String>, String> column = new TableColumn<>(columnTitles[i]);
            column.setCellValueFactory(param -> new ReadOnlyStringWrapper(param.getValue().get(colIndex)));
            // Dynamically size the "Selected Day(s)" column
            if (i == columnCount - 1) { // Last column ("Selected Day(s)")
                column.setMinWidth(150); // Set a minimum width
            }

            table.getColumns().add(column);
        }
    }

    // Set the column resize policy to make columns adjust dynamically
    table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

    table.setItems(data);
    return table;
}
}
