package main.zenit.ui.tree;

import java.io.File;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;

import main.zenit.filesystem.ProjectFile;
import main.zenit.filesystem.helpers.CodeSnippets;
import main.zenit.ui.MainController;

/**
 * Class that extends {@link javafx.scene.control.ContextMenu} with static menu items with dynamic
 * text. Also contains event handler.
 * @author Alexander Libot
 *
 */
public class TreeContextMenu extends ContextMenu implements EventHandler<ActionEvent>{
	
	private final MainController controller;
	private final TreeView<String> treeView;
	
	private final Menu createItem = new Menu("New...");
	private final MenuItem createClass = new MenuItem("New class");
	private final MenuItem createInterface = new MenuItem("New interface");
	private final MenuItem createPackage = new MenuItem("New package");
	private final MenuItem renameItem = new MenuItem("Rename");
	private final MenuItem deleteItem = new MenuItem("Delete");
	private final MenuItem importJar = new MenuItem("Import jar");
	private final MenuItem properties = new MenuItem("Properties");
	
	/**
	 * Creates a new {@link TreeContextMenu} that can manipulate a specific {@link
	 * javafx.scene.control.TreeView TreeView} instance and call methods in a specific
	 * {@link main.java.zenit.ui.MainController MainController}
	 * @param controller The {@link main.java.zenit.ui.MainController MainController} instance where methods
	 * will be called
	 * @param treeView The {@link javafx.scene.control.TreeView TreeView} instance which will
	 * be manipulated
	 */
	public TreeContextMenu(MainController controller, TreeView<String> treeView) {
		super();
		this.controller = controller;
		this.treeView = treeView;
		initContextMenu();
		treeView.setCellFactory(tv -> new DraggableTreeCell(treeView));
	}
	
	/**
	 * Updates the menu items with dynamic text.
	 * @param selectedNode The name of the node in the tree to be inserted dynamically
	 */
	private void setContext(String selectedNode) {
		String renameItemTitle = String.format("Rename \"%s\"", selectedNode);
		String deleteItemTitle = String.format("Delete \"%s\"", selectedNode);
		renameItem.setText(renameItemTitle);
		deleteItem.setText(deleteItemTitle);
				
		if (!createItem.getItems().contains(createPackage)) {
			createItem.getItems().add(createPackage);
		} else {
			createItem.getItems().remove(createPackage);
		}
		FileTreeItem<String> selectedItem = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
		if (selectedItem.getType() == FileTreeItem.PROJECT) {
			getItems().add(importJar);
			getItems().add(properties);
		} else {
			getItems().remove(importJar);
			getItems().remove(properties);
		}
	}
	
	/**
	 * Overrides {@link javafx.scene.control.ContextMenu#show(Node, double, double) show(...)} in
	 * {@link javafx.scene.control.ContextMenu ContextMenu}. Dynamically updates the menu
	 * items before showing the context menu.
	 */
	@Override
	public void show(Node node, double x, double y) {
		TreeItem<String> selectedItem = treeView.getSelectionModel().getSelectedItem();
		
		if (selectedItem != null) {
			setContext(selectedItem.getValue());
		}
		
		super.show(node, x, y);
	}
	
	/**
	 * Initializes the context menu by adding all menus and menu items and setting
	 * action listeners.
	 */
	private void initContextMenu() {
		createItem.getItems().add(createClass);
		createItem.getItems().add(createInterface);
		getItems().addAll(createItem, renameItem, deleteItem);
		createClass.setOnAction(this);
		createInterface.setOnAction(this);
		renameItem.setOnAction(this);
		deleteItem.setOnAction(this);
		createPackage.setOnAction(this);
		importJar.setOnAction(this);
		properties.setOnAction(this);

	}
	
	/**
	 * To create a new file, calls {@link main.java.zenit.ui.MainController#createFile(File, int)}
	 * @param typeCode The type of item to be created. Use constants from {@link 
	 * main.java.zenit.filesystem.helpers.CodeSnippets CodeSnippets}
	 */
	private void newFile(int typeCode) {

		FileTreeItem<String> parent = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
		controller.newFile(parent, typeCode);

	}

	/**
	 * Event handler for TreeContextMenu. Calls different methods in {@link main.java.zenit.ui.MainController
	 * MainController} depending on input.
	 */
	@Override
	public void handle(ActionEvent actionEvent) {
		FileTreeItem<String> selectedItem = (FileTreeItem<String>) treeView.getSelectionModel().getSelectedItem();
		File selectedFile = selectedItem.getFile();
		
		if (actionEvent.getSource().equals(createClass)) {
			newFile(CodeSnippets.CLASS);
		} else if (actionEvent.getSource().equals(createInterface)) {
			newFile(CodeSnippets.INTERFACE);
		} else if (actionEvent.getSource().equals(renameItem)) {
			File newFile = controller.renameFile(selectedFile);
			if (newFile != null) {
				selectedItem.setFile(newFile);
				selectedItem.setValue(newFile.getName());
				FileTree.changeFileForNodes(selectedItem, selectedItem.getFile());
			}
		} else if (actionEvent.getSource().equals(deleteItem)) {
			controller.deleteFile(selectedFile);
			selectedItem.getParent().getChildren().remove(selectedItem);
		} else if (actionEvent.getSource().equals(createPackage)) {
			File packageFile = controller.newPackage(selectedFile);
			if (packageFile != null) {
				FileTreeItem<String> packageNode = new FileTreeItem<String>(packageFile, packageFile.getName(), FileTreeItem.PACKAGE);
				selectedItem.getChildren().add(packageNode);
			}
		} else if (actionEvent.getSource().equals(importJar)) {
			ProjectFile projectFile = new ProjectFile(selectedFile.getPath());
			controller.chooseAndImportLibraries(projectFile);
		} else if (actionEvent.getSource().equals(properties) && selectedItem.getType() == FileTreeItem.PROJECT) {
			ProjectFile projectFile = new ProjectFile(selectedFile.getPath());
			controller.showProjectProperties(projectFile);
		}
	}
}
