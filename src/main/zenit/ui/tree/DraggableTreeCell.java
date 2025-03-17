package main.zenit.ui.tree;


import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import main.zenit.ui.MainController;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class DraggableTreeCell extends TreeCell<String> {



    public DraggableTreeCell(TreeView<String> treeView) {

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

                FileTreeItem<String> selectedItem = (FileTreeItem<String>)
                        treeView.getSelectionModel().getSelectedItem();
                File sourceFile = selectedItem.getFile();

                if(selectedItem.getType() == 102) {
                    sourceFile.mkdirs();
                }
                TreeItem<String> thisItem = getTreeItem();

                if (thisItem instanceof FileTreeItem<?> fileTreeItem) {
                    FileTreeItem<String> targetItem = (FileTreeItem<String>) thisItem;
                    if (targetItem.getType() == 105 ||
                            targetItem.getType() == 101 ||
                            targetItem.getType() == 102) {

                        File targetDir = targetItem.getFile();
                        File targetFile = new File(targetDir, sourceFile.getName());

                        if (sourceFile.equals(targetFile)) {
                            System.err.println("Source and target are the same: " + sourceFile);
                        } else {
                            try {
                                Files.move(sourceFile.getAbsoluteFile().toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);


                                FileTreeItem<String> root = (FileTreeItem<String>) treeView.getRoot();
                                FileTree.removeFromFile(root, sourceFile);

                                TreeItem<String> parent = selectedItem.getParent();
                                if (parent != null) {
                                    parent.getChildren().remove(selectedItem);
                                }

                                FileTreeItem<String> droppedItem = new FileTreeItem<>(targetFile, targetFile.getName(), FileTreeItem.FILE);
                                targetItem.getChildren().add(droppedItem);

                                success = true;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
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
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            setText(item);
            TreeItem<String> treeItem = getTreeItem();
            if (treeItem instanceof FileTreeItem<String> fileTreeItem) {
                setGraphic(fileTreeItem.getIcon());
            } else {
                setGraphic(null);
            }
        }
    }
}
