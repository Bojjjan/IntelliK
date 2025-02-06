<div align="center">
  <h1><strong> IntelliK  </strong></h1>
</div>

## About
IntelliK is a simple yet efficient Java-based IDE developed as part of a school project.
It is a fork of a fellow student's project, [Zenit](https://github.com/strazan/zenit), 
building upon its source code to offer an improved and streamlined user experience.


## Getting Started

### 1. Install Java SE 21
Ensure that Java 21 or higher is installed. <br>You can download it from the official Java website:
[Download Java SE 21](https://www.oracle.com/java/technologies/javase/jdk21-archive-downloads.html)

<br>

### 2. Clone the Repository
Clone the IntelliK repository using the following command in the terminal:

```bash
git clone https://github.com/Bojjjan/IntelliK.git
```
<br>

### 2. Open using textEditor
#### IntelliJ

1. **Open IntelliJ IDEA**
2. Navigate to **Run** → **Edit Configurations...**
3. Click **Add New Run Configuration...** and select **Application**
4. In the **Main class** field, enter: ``main.java.zenit.Zenit``
5. Press **ALT + V**, then in the **VM Options** field, paste the VM options found in the [VM arguemnts](#3.-VM-arguemnts) section below
6. Click **Apply**, then **Run**

> [!Note]
> If the program does not start make sure that project structures source folder is ``SRC``

<br>

#### Eclipse (deprecated)
1. **Open Eclipse**
2. Navigate to **File** → **Import...** → **General** → **Existing Projects into Workspace**
3. Click **Next >**
4. Click **Browse...** and select the newly unzipped folder named **Zenit**
5. Click **Open**
6. Click **Finish**
7. Add the following in **[VM Arguments](#3.-VM-arguments)** inside **Run Configurations**:
   - Go to **Run** → **Run Configurations** → **Java Application** → **TestUI** → **Arguments**

> [!Note]
> If the `TestUI` class doesn't appear in the **Java Application** list, try running `TestUI` once. You'll get an error, but the class will then be available in the list.

8. Uncheck **"Use the -XstartOnFirstThread argument when launching with SWT"**
9. Run `src/zenit/ui/TestUI.java`


<br>

### 3. VM arguemnts

```` plaintext
--module-path lib/javafx-sdk-21.0.6/lib/ --add-modules=javafx.controls,javafx.fxml,javafx.web
 --add-opens
javafx.graphics/javafx.scene.text=ALL-UNNAMED
--add-exports
javafx.graphics/com.sun.javafx.text=ALL-UNNAMED
--add-opens
javafx.graphics/com.sun.javafx.text=ALL-UNNAMED
--add-exports
javafx.graphics/com.sun.javafx.scene.text=ALL-UNNAMED
--add-exports
javafx.graphics/com.sun.javafx.scene=ALL-UNNAMED
--add-exports
javafx.graphics/com.sun.javafx.geom=ALL-UNNAMED
-Dprism.allowhidpi=true
````




