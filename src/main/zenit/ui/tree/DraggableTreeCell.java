package main.zenit.ui.tree;

import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DraggableTreeCell extends TreeCell<String>{
    public DraggableTreeCell() {
        setOnDragDetected(event -> {
            if (!isEmpty()) {
                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.putString(getItem());
                db.setContent(content);
                event.consume();
            }
        });

        setOnDragOver(event -> {
            if (event.getGestureSource() != this && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
            }
            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                TreeItem<String> thisItem = getTreeItem();
                if (thisItem instanceof FileTreeItem<?> fileTreeItem) {
                    FileTreeItem<String> targetItem = (FileTreeItem<String>) thisItem;
                    if (targetItem.getType() == 105 || targetItem.getType() == 101 || targetItem.getType() == 102) {
                        String droppedItemName = db.getString();
                        File sourceFile = new File(droppedItemName);
                        File targetDir = targetItem.getFile();
                        File targetFile = new File(targetDir, sourceFile.getName());

                        System.out.println("moving " + sourceFile + " to " + targetFile);
                            try {
                                Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                                TreeItem<String> droppedItem = new FileTreeItem<>(targetFile, targetFile.getName(), FileTreeItem.FILE);
                                targetItem.getChildren().add(droppedItem);
                                success = true;
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });


        setOnDragDone(DragEvent::consume);
    }

    @Override
    protected void updateItem(String item, boolean empty) {super.updateItem(item, empty);
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
            TreeItem<String> treeItem = getTreeItem();
            if (treeItem instanceof FileTreeItem) {
                FileTreeItem<String> fileTreeItem = (FileTreeItem<String>) treeItem;
                setGraphic(fileTreeItem.getIcon());
            } else {
                setGraphic(null);
            }
        }
    }
}
