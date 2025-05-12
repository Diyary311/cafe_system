package javaapplication9;

import javax.swing.*;
import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;
import java.util.*;
import javax.swing.border.EmptyBorder;

public class Cafe extends JFrame {

    public static class MenuItem {
        private final String name;
        private final double price;
        private final String category;
        private final String iconURL;

        public MenuItem(String name, double price, String category, String iconURL) {
            this.name = name;
            this.price = price;
            this.category = category;
            this.iconURL = iconURL;
        }

        public String getName() { return name; }
        public double getPrice() { return price; }
        public String getCategory() { return category; }
        public String getIconURL() { return iconURL; }
        public String toString() { return name; }
    }

    public static class OrderItem {
        private final MenuItem item;
        private int quantity;

        public OrderItem(MenuItem item) {
            this.item = item;
            this.quantity = 1;
        }

        public void increment() { quantity++; }
        public void decrement() { quantity = Math.max(0, quantity - 1); }
        public int getQuantity() { return quantity; }
        public MenuItem getItem() { return item; }
    }

    public static class Order {
        private final java.util.List<OrderItem> items = new ArrayList<>();

        public void addItem(MenuItem item) {
            Optional<OrderItem> existing = items.stream().filter(oi -> oi.getItem().equals(item)).findFirst();
            if (existing.isPresent()) {
                existing.get().increment();
            } else {
                items.add(new OrderItem(item));
            }
        }

        public void removeItem(MenuItem item) {
            items.removeIf(oi -> oi.getItem().equals(item) && oi.getQuantity() == 0);
        }

        public double calculateTotal() {
            return items.stream().mapToDouble(oi -> oi.getItem().getPrice() * oi.getQuantity()).sum();
        }

        public java.util.List<OrderItem> getItems() { return items; }
        public void clear() { items.clear(); }
    }

    private final Order order = new Order();
    private final JLabel totalLabel = new JLabel("Total: $0.00");
    private final java.util.List<MenuItem> fullMenuList = new ArrayList<>();
    private JPanel menuPanel, orderPanel;
    private final JTextArea receiptArea = new JTextArea();

    public Cafe() {
        setTitle("Cafe Management System");
        setSize(1200, 800);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        initUI();
        loadOnlineMenu();
        updateMenuPanel("All");
    }

    private void loadOnlineMenu() {
        MenuItem[] items = new MenuItem[]{
            new MenuItem("Espresso", 3.50, "Coffee", "https://img.icons8.com/fluency/48/coffee.png"),
            new MenuItem("Cappuccino", 4.50, "Coffee", "https://img.icons8.com/fluency/48/cappuccino.png"),
            new MenuItem("Latte", 4.75, "Coffee", "https://img.icons8.com/fluency/48/cafe-latte.png"),
            new MenuItem("Croissant", 3.25, "Food", "https://img.icons8.com/fluency/48/croissant.png"),
            new MenuItem("Muffin", 2.95, "Food", "https://img.icons8.com/fluency/48/muffin.png"),
            new MenuItem("Iced Tea", 2.50, "Drink", "https://img.icons8.com/fluency/48/iced-tea.png"),
            new MenuItem("Smoothie", 4.25, "Drink", "https://img.icons8.com/fluency/48/smoothie.png")
        };
        fullMenuList.addAll(Arrays.asList(items));
    }

    private void initUI() {
        JPanel root = new JPanel(new BorderLayout());

        JPanel tabPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        String[] categories = {"All", "Coffee", "Food", "Drink"};
        for (String category : categories) {
            JButton tab = new JButton(category);
            tab.addActionListener(e -> updateMenuPanel(category));
            tabPanel.add(tab);
        }
        root.add(tabPanel, BorderLayout.NORTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

        menuPanel = new JPanel();
        menuPanel.setLayout(new BoxLayout(menuPanel, BoxLayout.Y_AXIS));
        JScrollPane menuScroll = new JScrollPane(menuPanel);
        splitPane.setLeftComponent(menuScroll);

        orderPanel = new JPanel();
        orderPanel.setLayout(new BoxLayout(orderPanel, BoxLayout.Y_AXIS));
        JScrollPane orderScroll = new JScrollPane(orderPanel);

        JPanel receiptContainer = new JPanel(new BorderLayout());
        receiptArea.setEditable(false);
        receiptArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        receiptArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        receiptContainer.add(new JScrollPane(receiptArea), BorderLayout.CENTER);

        JSplitPane rightSplit = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        rightSplit.setLeftComponent(orderScroll);
        rightSplit.setRightComponent(receiptContainer);
        rightSplit.setDividerLocation(300);

        splitPane.setRightComponent(rightSplit);
        splitPane.setDividerLocation(600);
        root.add(splitPane, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        JButton checkoutButton = new JButton("Checkout");
        checkoutButton.addActionListener(e -> checkoutOrder());
        bottom.add(totalLabel, BorderLayout.WEST);
        bottom.add(checkoutButton, BorderLayout.EAST);
        root.add(bottom, BorderLayout.SOUTH);

        setContentPane(root);
    }

    private void updateMenuPanel(String category) {
        menuPanel.removeAll();
        for (MenuItem item : fullMenuList) {
            if (category.equals("All") || item.getCategory().equalsIgnoreCase(category)) {
                JPanel itemPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel iconLabel = new JLabel(loadIcon(item.getIconURL()));
                JLabel nameLabel = new JLabel(item.getName() + " ($" + item.getPrice() + ")");
                JButton addButton = new JButton("Add");
                addButton.addActionListener(e -> {
                    order.addItem(item);
                    updateOrderPanel();
                });
                itemPanel.add(iconLabel);
                itemPanel.add(nameLabel);
                itemPanel.add(addButton);
                itemPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY));
                menuPanel.add(itemPanel);
            }
        }
        menuPanel.revalidate();
        menuPanel.repaint();
    }

    private void updateOrderPanel() {
        orderPanel.removeAll();
        for (OrderItem oi : order.getItems()) {
            if (oi.getQuantity() > 0) {
                JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                JLabel nameLabel = new JLabel(oi.getItem().getName() + " x" + oi.getQuantity()
                        + " ($" + String.format("%.2f", oi.getQuantity() * oi.getItem().getPrice()) + ")");
                JButton plus = new JButton("+");
                JButton minus = new JButton("-");
                plus.addActionListener(e -> {
                    oi.increment();
                    updateOrderPanel();
                });
                minus.addActionListener(e -> {
                    oi.decrement();
                    if (oi.getQuantity() == 0) {
                        order.getItems().remove(oi);
                    }
                    updateOrderPanel();
                });
                panel.add(nameLabel);
                panel.add(minus);
                panel.add(plus);
                orderPanel.add(panel);
            }
        }
        updateTotal();
        updateReceiptPanel();
        orderPanel.revalidate();
        orderPanel.repaint();
    }

    private void updateReceiptPanel() {
        StringBuilder receipt = new StringBuilder();
        receipt.append("===== Receipt =====\n");
        int totalQty = 0;
        for (OrderItem oi : order.getItems()) {
            if (oi.getQuantity() > 0) {
                double subtotal = oi.getItem().getPrice() * oi.getQuantity();
                receipt.append(String.format("%-15s x%-2d $%.2f\n", oi.getItem().getName(), oi.getQuantity(), subtotal));
                totalQty += oi.getQuantity();
            }
        }
        receipt.append("\n-------------------\n");
        receipt.append("Total Quantity: ").append(totalQty).append("\n");
        receipt.append("Total: $").append(String.format("%.2f", order.calculateTotal())).append("\n");
        receiptArea.setText(receipt.toString());
    }

    private void checkoutOrder() {
        if (order.getItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Your order is empty.");
            return;
        }
        JOptionPane.showMessageDialog(this, receiptArea.getText());
        order.clear();
        updateOrderPanel();
    }

    private void updateTotal() {
        totalLabel.setText("Total: $" + String.format("%.2f", order.calculateTotal()));
    }

    private ImageIcon loadIcon(String url) {
        try {
            Image img = ImageIO.read(new URL(url)).getScaledInstance(32, 32, Image.SCALE_SMOOTH);
            return new ImageIcon(img);
        } catch (Exception e) {
            System.err.println("Error loading icon: " + url);
            return new ImageIcon();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}
            new Cafe().setVisible(true);
        });
    }
}
