package zenit;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import zenit.setup.SetupController;
import zenit.ui.MainController;

import java.io.File;

/**
 * Main class used to launch the application.
 * Detects the operating system and initializes the main controller.
 * Stops the application if the OS is not supported.
 * @author Alexander Libot, Philip Boyde
 *
 */
public class Zenit extends Application {

	/**
	 * The detected operating system.
	 */
	public static GetOperatingSystem.OperatingSystem OS;

	static {
		try {
			OS = GetOperatingSystem.detectOS();
		} catch (UnsupportedOperationException e) {
			System.err.println(e.getMessage());
			Platform.exit();
			System.exit(1);
		}
	}

	/**
	 * Starts the application.
	 *
	 * @author Philip Boyde
	 * @param stage The primary stage for this application.
	 * @throws Exception if an error occurs during application startup.
	 */
	@Override
	public void start(Stage stage) throws Exception {
		File workspace = new File("res/workspace/workspace.dat");
		File JDK = new File("res/JDK/JDK.dat");
		File defaultJDK = new File ("res/JDK/DefaultJDK.dat");

		SetupController sc;
		if (!workspace.exists() || !JDK.exists() || !defaultJDK.exists()) {
			sc = new SetupController();
			sc.start();
		}

		switch (OS){
			case MAC:
				System.setProperty("com.apple.mrj.application.apple.menu.about.name", "WikiTeX");

			case LINUX, WINDOWS:
				new MainController(stage);
				break;
		}
	}

	/**
	 * Stops the application.
	 */
	@Override
	public void stop() {
		Platform.exit();
		System.exit(0);
	}

	/**
	 * The main method to launch the application.
	 *
	 * @param args The command line arguments.
	 */
	public static void main(String[] args) {
		launch(args);
	}

}
