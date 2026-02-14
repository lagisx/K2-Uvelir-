package org.example.uvelirkurs.controllers;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.StackPane;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

import java.io.IOException;
import java.util.Stack;

public class NavigationController {
    private static NavigationController instance;
    private StackPane rootContainer;
    private Stack<Parent> navigationStack = new Stack<>();

    private NavigationController() {}

    public static NavigationController getInstance() {
        if (instance == null) {
            instance = new NavigationController();
        }
        return instance;
    }

    public void setRootContainer(StackPane container) {
        this.rootContainer = container;
    }

    public void navigateTo(String fxmlPath) {
        navigateTo(fxmlPath, null);
    }

    public void navigateTo(String fxmlPath, Object controller) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            if (controller != null) {
                loader.setController(controller);
            }
            Parent newView = loader.load();

            if (!navigationStack.isEmpty()) {
                Parent currentView = navigationStack.peek();
                navigationStack.push(newView);
                transitionTo(newView);
            } else {
                navigationStack.push(newView);
                rootContainer.getChildren().clear();
                rootContainer.getChildren().add(newView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void goBack() {
        if (navigationStack.size() > 1) {
            navigationStack.pop();
            Parent previousView = navigationStack.peek();
            transitionTo(previousView);
        }
    }

    private void transitionTo(Parent newView) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), rootContainer);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            rootContainer.getChildren().clear();
            rootContainer.getChildren().add(newView);
            
            FadeTransition fadeIn = new FadeTransition(Duration.millis(150), rootContainer);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();
        });
        fadeOut.play();
    }

    public boolean canGoBack() {
        return navigationStack.size() > 1;
    }

    public void clearHistory() {
        navigationStack.clear();
    }
}
