module in.sfit.miniproject.miniproject {
    requires javafx.controls;
    requires javafx.fxml;


    opens in.sfit.miniproject.miniproject to javafx.fxml;
    exports in.sfit.miniproject.miniproject;
}