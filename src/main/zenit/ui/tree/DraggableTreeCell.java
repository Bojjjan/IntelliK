package main.zenit.ui.tree;


import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;


import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * This class is used to create a draggable tree cell in the tree view.
 * It works by overriding the updateItem method and adding drag and drop
 * This class gets added to the tree view in the FileTree class
 *
 * @author Mojtaba Hauari
 */

public class DraggableTreeCell extends TreeCell<String> {


    /**
     * This method is used to initialize the draggable tree cell
     * It adds drag and drop functionality to the tree cell
     * @param treeView The tree view that the cell is in
     *
     * @author Mojtaba Hauari
     */
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


         //* This method is used to handle the drop event
         //* It moves the file from the source to the target
         //* It also updates the tree view to reflect the changes
         //* This firstly checks if the target is the root directory
         //* then moves the file to the root directory
         //* if the target is any other folder like directory or package
         //* then it moves the file to that folder
        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasString()) {
                FileTreeItem<String> selectedItem = (FileTreeItem<String>)
                        treeView.getSelectionModel().getSelectedItem();
                File sourceFile = selectedItem.getFile();
                TreeItem<String> thisItem = getTreeItem();

                FileTreeItem<String> root = (FileTreeItem<String>) treeView.getRoot();
                File rootDir = root.getFile(); // Root directory of the file tree

                if (thisItem == null || thisItem == root) {
                    File targetFile = new File(rootDir, sourceFile.getName());

                    if (!sourceFile.equals(targetFile)) {
                        try {
                            Files.move(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                            FileTree.removeFromFile(root, sourceFile);
                            TreeItem<String> parent = selectedItem.getParent();
                            if (parent != null) {
                                parent.getChildren().remove(selectedItem);
                            }

                            int newType = sourceFile.isDirectory() ? FileTreeItem.FOLDER : FileTreeItem.FILE;
                            FileTreeItem<String> droppedItem = new FileTreeItem<>(targetFile, targetFile.getName(), newType);
                            droppedItem.setType(selectedItem.getType());

                            if(selectedItem.getType() == 105){
                                droppedItem.setType(105);
                            }else if(selectedItem.getType() == 101){
                                droppedItem.setType(101);
                            }else{
                                droppedItem.setType(102);
                            }


                            root.getChildren().add(droppedItem);
                            FileTreeItem<String> newRoot = new FileTreeItem<>(root.getFile(), root.getValue(), root.getType());
                            FileTree.createNodes(newRoot, root.getFile());
                            treeView.setRoot(newRoot);
                            treeView.refresh();

                            success = true;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }



                if (thisItem instanceof FileTreeItem<?> fileTreeItem){
                    FileTreeItem<String> targetItem = (FileTreeItem<String>) thisItem;
                    if (targetItem.getType() == 105 ||
                            targetItem.getType() == 101 ||
                            targetItem.getType() == 102 ||
                            targetItem.getType() == 104 ||
                            targetItem.getType() == 108 ||
                            targetItem.getType() == 109 ||
                            targetItem.getType() == 110){

                        File targetDir = targetItem.getFile();
                        File targetFile = new File(targetDir, sourceFile.getName());

                        if (sourceFile.equals(targetFile)) {
                            System.err.println("Source and target are the same: " + sourceFile);
                        } else {
                            try {
                                Files.move(sourceFile.getAbsoluteFile().toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

                                FileTreeItem<String> root1 = (FileTreeItem<String>) treeView.getRoot();
                                FileTree.removeFromFile(root1, sourceFile);

                                TreeItem<String> parent = selectedItem.getParent();
                                if (parent != null) {
                                    parent.getChildren().remove(selectedItem);
                                }

                                int newType = sourceFile.isDirectory() ? FileTreeItem.FOLDER : FileTreeItem.FILE;
                                FileTreeItem<String> droppedItem = new FileTreeItem<>(targetFile, targetFile.getName(), newType);
                                if(selectedItem.getType() == 105){
                                    droppedItem.setType(105);
                                }else if(selectedItem.getType() == 101){
                                    droppedItem.setType(101);
                                }else{
                                    droppedItem.setType(102);
                                }
                                targetItem.getChildren().add(droppedItem);

                                FileTreeItem<String> newRoot = new FileTreeItem<>(root1.getFile(), root1.getValue(), root1.getType());
                                FileTree.createNodes(newRoot, root1.getFile());
                                treeView.setRoot(newRoot);

                                treeView.refresh();

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


    /**
     * this method is used to update the item in the tree cell after a drag and drop
     * @param item The item to update
     * @param empty A boolean to check if the item is empty
     *
     * @author Mojtaba Hauari
     */

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
