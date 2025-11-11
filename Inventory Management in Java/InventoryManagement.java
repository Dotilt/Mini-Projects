import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

class Product {
    private String name, category;
    private int id, stock;
    private double price;

    public Product(String name, int id, int stock, double price, String category) {
        this.name = name;
        this.id = id;
        this.stock = stock;
        this.price = price;
        this.category = category;
    }

    public int getId() { return id; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public String getCategory() { return category; }
}

class Inventory {
    private Product[] products;
    private int productCount;

    public Inventory(int maxProducts) {
        products = new Product[maxProducts];
        productCount = 0;
    }

    public synchronized void addProduct(Product product) {
        if (productCount < products.length) {
            products[productCount++] = product;
        } else {
            System.out.println("Inventory is full!");
        }
    }

    public Product findProductById(int id) {
        for (int i = 0; i < productCount; i++) {
            if (products[i].getId() == id) return products[i];
        }
        return null;
    }

    public synchronized void displayStatistics() {
        int totalGood = 0, totalCargo = 0;
        for (int i = 0; i < productCount; i++) {
            if (products[i].getCategory().equals("Good")) totalGood += products[i].getStock();
            else if (products[i].getCategory().equals("Cargo")) totalCargo += products[i].getStock();
        }
        System.out.println("Total Good Products: " + totalGood);
        System.out.println("Total Cargo Products: " + totalCargo);
    }

    public void displayInventory() {
        System.out.println("\nInventory:");
        for (int i = 0; i < productCount; i++) {
            Product p = products[i];
            System.out.println("Name: " + p.getName() + ", ID: " + p.getId() + ", Stock: " + p.getStock() + ", Price: " + p.getPrice() + ", Category: " + p.getCategory());
        }
    }

    public synchronized boolean updateStock(int id, int quantity) {
        Product product = findProductById(id);
        if (product != null && product.getStock() >= quantity) {
            product.setStock(product.getStock() - quantity);
            return true;
        }
        return false;
    }

    public synchronized boolean isLowStock(Product product, int threshold) {
        return product.getStock() < threshold;
    }
}


class Order {
    private Product[] orderedProducts;
    private int[] quantities;
    private int count;

    public Order(int maxProducts) {
        orderedProducts = new Product[maxProducts];
        quantities = new int[maxProducts];
        count = 0;
    }

    public void addProduct(Product product, int quantity) {
        orderedProducts[count] = product;
        quantities[count++] = quantity;
    }

    public void displayOrderConfirmation() {
        double total = 0;
        System.out.println("Order Confirmation:");
        for (int i = 0; i < count; i++) {
            Product product = orderedProducts[i];
            total += product.getPrice() * quantities[i];
            String deliveryMode = product.getCategory().equals("Good") ? "Land" : "Sea";
            System.out.println(quantities[i] + " x " + product.getName() + " @ $" + product.getPrice() + " - " + deliveryMode + " shipment");
            if(deliveryMode.equals("Land")){
                System.out.println("expected delivery date is: 3 days");
            }else{
                System.out.println("expected delivery date is: 5 days");
            }
        }
        System.out.println("Total Amount: $" + total);
    }
}

class Manager {
    private Inventory inventory;
    private String username = "manager";
    private String password = "pass123";

    public Manager(Inventory inventory) { this.inventory = inventory; }

    public boolean authenticate(String user, String pass) { return user.equals(username) && pass.equals(password); }

    public void addProduct(Product product) { inventory.addProduct(product); }

    public void displayStatistics() { inventory.displayStatistics(); }
}

class Customer {
    private int id;
    private String name;
    private static final int LOW_STOCK_THRESHOLD = 5;

    public Customer(int id, String name) { this.id = id; this.name = name; }

    public void viewProducts(Inventory inventory) {
        inventory.displayInventory();
    }

    public void placeOrder(Inventory inventory, int[] productIds, int[] quantities, InventoryManagement inventoryManagement) {
        Order order = new Order(productIds.length);
        for (int i = 0; i < productIds.length; i++) {
            Product product = inventory.findProductById(productIds[i]);
            if (product != null && inventory.updateStock(productIds[i], quantities[i])) {
                order.addProduct(product, quantities[i]);
                if (inventory.isLowStock(product, LOW_STOCK_THRESHOLD)) {
                    inventoryManagement.recordLowStockNotification(product);
                }
            } else {
                System.out.println("Insufficient stock for product: " + product.getName());
            }
        }
        order.displayOrderConfirmation();
    }
}

public class InventoryManagement extends Application {
    private Inventory inventory = new Inventory(10);
    private Manager manager = new Manager(inventory);
    private Customer customer = new Customer(1, "John");
    private StringBuilder lowStockNotifications = new StringBuilder();

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Inventory Management System");

        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter Username");

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter Password");

        Button managerLogin = new Button("Manager Login");
        managerLogin.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                if (manager.authenticate(usernameField.getText(), passwordField.getText())) {
                    showManagerMenu();
                } else {
                    showAlert("Invalid credentials");
                }
            }
        });

        Button customerView = new Button("Customer View");
        customerView.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                showCustomerMenu();
            }
        });

        VBox loginLayout = new VBox(10, new Label("Manager Login"), usernameField, passwordField, managerLogin, customerView);
        loginLayout.setPadding(new Insets(10));
        primaryStage.setScene(new Scene(loginLayout, 300, 200));
        primaryStage.show();
    }

    private void showManagerMenu() {
        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");

        TextField idField = new TextField();
        idField.setPromptText("Product ID");

        TextField stockField = new TextField();
        stockField.setPromptText("Stock Quantity");

        TextField priceField = new TextField();
        priceField.setPromptText("Product Price");

        TextField categoryField = new TextField();
        categoryField.setPromptText("Category (Good/Cargo)");

        Button addProduct = new Button("Add Product");
        addProduct.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    Product product = new Product(
                            nameField.getText(),
                            Integer.parseInt(idField.getText()),
                            Integer.parseInt(stockField.getText()),
                            Double.parseDouble(priceField.getText()),
                            categoryField.getText()
                    );
                    manager.addProduct(product);
                    showAlert("Product added!");
                } catch (NumberFormatException ex) {
                    showAlert("Invalid input format");
                }
            }
        });

        Button viewStats = new Button("View Statistics");
        viewStats.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                manager.displayStatistics();
            }
        });

        // Button to view low stock alerts
        Button viewLowStockAlerts = new Button("View Low Stock Alerts");
        viewLowStockAlerts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                showLowStockNotifications();
            }
        });

        VBox managerLayout = new VBox(10, new Label("Manager Menu"), nameField, idField, stockField, priceField, categoryField, addProduct, viewStats, viewLowStockAlerts);
        managerLayout.setPadding(new Insets(10));
        Stage managerStage = new Stage();
        managerStage.setScene(new Scene(managerLayout, 400, 300));
        managerStage.show();
    }

    private void showCustomerMenu() {
        TextField productIdField = new TextField();
        productIdField.setPromptText("Enter Product ID (comma-separated for multiple)");

        TextField quantityField = new TextField();
        quantityField.setPromptText("Enter Quantity (comma-separated for multiple)");

        Button viewProducts = new Button("View Products");
        viewProducts.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                customer.viewProducts(inventory);
            }
        });

        Button placeOrder = new Button("Place Order");
        placeOrder.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    String[] ids = productIdField.getText().split(",");
                    String[] quantities = quantityField.getText().split(",");
                    int[] productIds = new int[ids.length];
                    int[] qtys = new int[quantities.length];

                    for (int i = 0; i < ids.length; i++) {
                        productIds[i] = Integer.parseInt(ids[i].trim());
                        qtys[i] = Integer.parseInt(quantities[i].trim());
                    }

                    customer.placeOrder(inventory, productIds, qtys, InventoryManagement.this);
                } catch (NumberFormatException ex) {
                    showAlert("Invalid input format");
                }
            }
        });

        VBox customerLayout = new VBox(10, new Label("Customer Menu"), productIdField, quantityField, viewProducts, placeOrder);
        customerLayout.setPadding(new Insets(10));
        Stage customerStage = new Stage();
        customerStage.setScene(new Scene(customerLayout, 400, 300));
        customerStage.show();
    }

    public void recordLowStockNotification(Product product) {
        lowStockNotifications.append("Low stock alert for ").append(product.getName()).append("\n");
    }

    private void showLowStockNotifications() {
        if (lowStockNotifications.length() > 0) {
            showAlert("Low Stock Alerts:\n" + lowStockNotifications.toString());
            lowStockNotifications.setLength(0);
        } else {
            showAlert("No low stock alerts at the moment.");
        }
    }

    private void showAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
