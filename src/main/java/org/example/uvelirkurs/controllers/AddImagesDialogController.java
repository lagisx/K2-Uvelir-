package org.example.uvelirkurs.controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.example.uvelirkurs.BDandAPI.SupabaseServiceExtension;
import org.example.uvelirkurs.BDandAPI.SupabaseStorageService;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class AddImagesDialogController {

    @FXML private Label productNameLabel;
    @FXML private Label productIdLabel;
    @FXML private VBox dropZone;
    @FXML private Button browseButton;
    @FXML private FlowPane imagePreviewContainer;
    @FXML private Label emptyPreviewLabel;
    @FXML private VBox filesListContainer;
    @FXML private Label emptyFilesLabel;
    @FXML private VBox progressContainer;
    @FXML private ProgressIndicator uploadProgress;
    @FXML private Label progressLabel;
    @FXML private ProgressBar uploadProgressBar;

    private DialogPane dialogPane;
    private int productId;
    private String productName;
    private final List<File> selectedFiles = new ArrayList<>();
    private List<String> uploadedUrls = new ArrayList<>();

    private static final int MAX_FILES = 3;


    @FXML
    public void initialize() {
        setupDragAndDrop();
        setupBrowseButton();
    }

    public void setDialogPane(DialogPane dialogPane) {
        this.dialogPane = dialogPane;
        setupUploadButton();
    }

    public void setProductInfo(int productId, String productName) {
        this.productId = productId;
        this.productName = productName;
        productNameLabel.setText(productName);
        productIdLabel.setText("ID: #" + productId);
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
    }

    private void setupBrowseButton() {
        browseButton.setOnAction(e -> browseFiles());
    }

    private void setupUploadButton() {
        if (dialogPane != null) {
            Button uploadButton = (Button) dialogPane.lookupButton(
                    dialogPane.getButtonTypes().stream()
                            .filter(bt -> bt.getButtonData() == ButtonBar.ButtonData.OK_DONE)
                            .findFirst()
                            .orElse(null)
            );

            if (uploadButton != null) {
                uploadButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
                    if (selectedFiles.isEmpty()) {
                        Alert alert = new Alert(Alert.AlertType.WARNING);
                        alert.setTitle("Предупреждение");
                        alert.setContentText("Выберите хотя бы одно изображение");
                        alert.showAndWait();
                        event.consume();
                    } else {
                        uploadImages();
                        event.consume();
                    }
                });
            }
        }
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            event.acceptTransferModes(TransferMode.COPY);
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        Dragboard db = event.getDragboard();
        boolean success = false;

        if (db.hasFiles()) {
            List<File> validFiles = db.getFiles().stream()
                    .filter(SupabaseStorageService::isImageFile)
                    .filter(SupabaseStorageService::isFileSizeValid)
                    .collect(Collectors.toList());

            if (!validFiles.isEmpty()) {
                addFiles(validFiles);
                success = true;
            } else {
                showAlert("Ошибка", "Выбранные файлы не являются изображениями или превышают размер 5MB");
            }
        }

        event.setDropCompleted(success);
        event.consume();
    }

    private void browseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите изображения");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Изображения", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.webp")
        );

        List<File> files = fileChooser.showOpenMultipleDialog(browseButton.getScene().getWindow());
        if (files != null && !files.isEmpty()) {
            List<File> validFiles = files.stream()
                    .filter(SupabaseStorageService::isImageFile)
                    .filter(SupabaseStorageService::isFileSizeValid)
                    .collect(Collectors.toList());

            if (!validFiles.isEmpty()) {
                addFiles(validFiles);
            } else {
                showAlert("Ошибка", "Выбранные файлы не являются изображениями или превышают размер 5MB");
            }
        }
    }

    private void addFiles(List<File> files) {
            if (selectedFiles.size() + files.size() > MAX_FILES) {
                showAlert("Ошибка", "Максимум " + MAX_FILES + " изображений");
                return;
            }
        for (File file : files) {
            if (!selectedFiles.contains(file)) {
                selectedFiles.add(file);
                addFileToList(file);
                addImagePreview(file);
            }
        }

        if (!selectedFiles.isEmpty()) {
            emptyFilesLabel.setVisible(false);
            emptyPreviewLabel.setVisible(false);
        }
    }

    private void addImagePreview(File file) {
        try {
            VBox previewBox = new VBox(5);
            previewBox.setAlignment(Pos.CENTER);
            previewBox.setStyle(
                    "-fx-background-color: white; " +
                            "-fx-border-color: #e0e0e0; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 8; " +
                            "-fx-background-radius: 8; " +
                            "-fx-padding: 8; " +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);"
            );
            previewBox.setPrefWidth(120);
            previewBox.setPrefHeight(140);

            Image image = new Image(file.toURI().toString(), 100, 100, true, true);
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(100);
            imageView.setFitHeight(100);
            imageView.setPreserveRatio(true);
            imageView.setStyle("-fx-border-radius: 4;");

            String displayName = file.getName();
            if (displayName.length() > 15) {
                displayName = displayName.substring(0, 12) + "...";
            }
            Label nameLabel = new Label(displayName);
            nameLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: #666666;");
            nameLabel.setMaxWidth(100);

            Button removeBtn = new Button("✕");
            removeBtn.setStyle(
                    "-fx-background-color: #e74c3c; " +
                            "-fx-text-fill: white; " +
                            "-fx-font-size: 10px; " +
                            "-fx-font-weight: bold; " +
                            "-fx-cursor: hand; " +
                            "-fx-background-radius: 50%; " +
                            "-fx-min-width: 20px; " +
                            "-fx-min-height: 20px; " +
                            "-fx-max-width: 20px; " +
                            "-fx-max-height: 20px;"
            );
            removeBtn.setOnAction(e -> {
                selectedFiles.remove(file);
                imagePreviewContainer.getChildren().remove(previewBox);
                removeFileFromList(file);
                if (selectedFiles.isEmpty()) {
                    emptyPreviewLabel.setVisible(true);
                    emptyFilesLabel.setVisible(true);
                }
            });

            previewBox.getChildren().addAll(imageView, nameLabel, removeBtn);
            imagePreviewContainer.getChildren().add(previewBox);

        } catch (Exception e) {
            System.err.println("Ошибка создания превью для " + file.getName() + ": " + e.getMessage());
        }
    }

    private void addFileToList(File file) {
        HBox fileItem = new HBox(10);
        fileItem.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10; -fx-background-radius: 6;");
        fileItem.setUserData(file);

        Label fileIcon = new Label("🖼️");
        fileIcon.setStyle("-fx-font-size: 20px;");

        VBox fileInfo = new VBox(3);
        Label fileName = new Label(file.getName());
        fileName.setStyle("-fx-font-weight: bold; -fx-font-size: 13px;");

        long sizeKB = file.length() / 1024;
        Label fileSize = new Label(String.format("%.1f KB", sizeKB / 1024.0));
        fileSize.setStyle("-fx-font-size: 11px; -fx-text-fill: #666666;");

        fileInfo.getChildren().addAll(fileName, fileSize);

        Button removeButton = new Button("✕");
        removeButton.setStyle(
                "-fx-background-color: #e74c3c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-weight: bold; " +
                        "-fx-cursor: hand; " +
                        "-fx-background-radius: 50%; " +
                        "-fx-min-width: 25px; " +
                        "-fx-min-height: 25px; " +
                        "-fx-max-width: 25px; " +
                        "-fx-max-height: 25px;"
        );

        removeButton.setOnAction(e -> {
            selectedFiles.remove(file);
            filesListContainer.getChildren().remove(fileItem);
            removePreview(file);
            if (selectedFiles.isEmpty()) {
                emptyFilesLabel.setVisible(true);
                emptyPreviewLabel.setVisible(true);
            }
        });

        HBox.setHgrow(fileInfo, javafx.scene.layout.Priority.ALWAYS);
        fileItem.getChildren().addAll(fileIcon, fileInfo, removeButton);
        filesListContainer.getChildren().add(fileItem);
    }

    private void removeFileFromList(File file) {
        filesListContainer.getChildren().removeIf(node -> {
            if (node instanceof HBox) {
                return file.equals(node.getUserData());
            }
            return false;
        });
    }

    private void removePreview(File file) {
        imagePreviewContainer.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                return node.getUserData() != null && node.getUserData().equals(file);
            }
            return false;
        });
    }

    private void uploadImages() {
        progressContainer.setVisible(true);
        uploadProgressBar.setProgress(0);
        progressLabel.setText("Загрузка изображений...");

        setButtonsEnabled(false);

        AtomicInteger uploadedCount = new AtomicInteger(0);
        int totalFiles = selectedFiles.size();

        uploadFilesSequentially(0, totalFiles, uploadedCount);
    }

    private void uploadFilesSequentially(int index, int total, AtomicInteger uploadedCount) {
        if (index >= total) {
            Platform.runLater(() -> {
                finishUpload(uploadedCount.get(), total);
            });
            return;
        }

        File file = selectedFiles.get(index);
        int position = index;

        Platform.runLater(() -> {
            double progress = (double) index / total;
            uploadProgressBar.setProgress(progress);
            progressLabel.setText(String.format("Загрузка %d из %d...", index + 1, total));
        });

        SupabaseStorageService.uploadImage(file, productId).thenAccept(url -> {
            if (url != null) {
                uploadedUrls.add(url);

                SupabaseServiceExtension.addProductImage(productId, url, position).thenAccept(saved -> {
                    if (saved) {
                        uploadedCount.incrementAndGet();
                    } else {
                    }

                    uploadFilesSequentially(index + 1, total, uploadedCount);

                }).exceptionally(ex -> {
                    uploadFilesSequentially(index + 1, total, uploadedCount);
                    return null;
                });

            } else {
                uploadFilesSequentially(index + 1, total, uploadedCount);
            }

        }).exceptionally(ex -> {
            ex.printStackTrace();
            uploadFilesSequentially(index + 1, total, uploadedCount);
            return null;
        });
    }

    private void finishUpload(int uploaded, int total) {
        uploadProgressBar.setProgress(1.0);

        if (uploaded == 0) {
            progressLabel.setText("❌ Ошибка загрузки изображений");
            showAlert("Ошибка", "Не удалось загрузить ни одного изображения");
            setButtonsEnabled(true);
        } else {
            progressLabel.setText(String.format("✅ Загружено %d из %d изображений", uploaded, total));

            if (uploaded < total) {
                showAlert("Частичный успех",
                        String.format("Загружено %d из %d изображений.\nНекоторые файлы не удалось загрузить.", uploaded, total));
            }

            new Thread(() -> {
                try {
                    Thread.sleep(1500);
                    Platform.runLater(() -> {
                        if (dialogPane != null && dialogPane.getScene() != null) {
                            dialogPane.getScene().getWindow().hide();
                        }
                    });
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    private void setButtonsEnabled(boolean enabled) {
        if (dialogPane != null) {
            dialogPane.getButtonTypes().forEach(buttonType -> {
                Button button = (Button) dialogPane.lookupButton(buttonType);
                if (button != null) {
                    button.setDisable(!enabled);
                }
            });
        }
    }

    public List<String> getUploadedUrls() {
        return uploadedUrls;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
