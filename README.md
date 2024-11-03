
# Ema Navigator

Ema Navigator is a Java library for navigating between CLI pages within a project. It provides code generation to facilitate seamless navigation between "pages" by automatically generating methods to navigate to different classes (or "screens"). This approach is particularly useful for projects requiring a command-line interface (CLI) or programmatic navigation within applications.

## Table of Contents
- [Features](#features)
- [Installation](#installation)
- [Setup](#setup)
- [Usage](#usage)
  - [Annotating Classes](#annotating-classes)
  - [Generated Navigator Class](#generated-navigator-class)
  - [Navigating to Pages](#navigating-to-pages)
- [Example](#example)
- [Configuration](#configuration)
- [License](#license)

## Features
- **Simple Navigation:** Implement your class with AbstractNavigable.
- **Parameter Passing:** Use `@Param` to indicate required parameters for a page.
- **Type Safety:** Navigate between pages with parameter checks at compile time.
- **Code Generation:** Uses JavaPoet to create the Navigator class with necessary methods automatically.

## Installation

### **Using Navigation as a Library**
1. **Download Navigation Jar File**
  
   Download the latest version of [ema-navigation-1.0.jar](./releases/ema-navigator-1.0.jar).
2. **Add JAR to Project Build Path**
 
   ![Steps to add external JARs in IntelliJ IDEA](./public/add-jar-intellij.png)

   Steps for adding external JARs in IntelliJ IDEA:

   1. Click **File** from the toolbar.
   2. Select the **Project Structure** option (`CTRL + SHIFT + ALT + S` on Windows/Linux, `⌘ + ;` on macOS).
   3. Select **Modules** from the left panel.
   4. Select the **Dependencies** tab.
   5. Click the **+** icon.
   6. Select **JARs or directories** option.
   

## Setup
Define Navigation Interfaces and Annotations:

- **AbstractNavigable:** An AbstractClass that every page must extends, containing the `display()` method.
- **@Param:** Annotate fields within pages that should be filled when navigating.

Use Code Generation: Navigator relies on an annotation processor to generate navigation methods in a class called Navigator. Make sure to enable annotation processing in your IDE (usually enabled by default).

## Usage
#### Extends AbstractNavigable:
Extend each page class from AbstractNavigable.

```java
public class HomePage extends AbstractNavigable {

    @Param
    private String greeting;

    @Param
    private Auth auth;

    @Override
    public void display() {
        System.out.println(greeting.concat(auth.name));
    }
}
```

#### Add Parameters (if required):
Use the `@Param` annotation to specify fields that are required for navigation.

```java
public class LoginPage extends AbstractNavigable {

    @Override
    public void display() {
        Navigator.navToHomePage("hi ", new Auth("emamagic"));
    }
}
```

## Generated Navigator Class
After building, Navigator will automatically generate a Navigator class with methods for each page based on `@Page` and `@Param` annotations.

- **No-Param Page:** For pages without `@Param`, Navigator will generate a simple method like `navToLoginPage()`.
- **Parameterized Page:** For pages with `@Param` fields, Navigator will create a method that accepts the required parameters, like `navToHomePage(String greeting, Auth auth)`.

## Navigating to Pages
Call the generated Navigator methods to navigate between pages:

The generated methods allow for easy, type-safe navigation between pages. The `Navigator` class takes care of setting required parameters, reducing manual setup and minimizing errors.

## Example
Given the following classes:
```java
public class HomePage extends AbstractNavigable {
    @Override
    public void display() {
        System.out.println("Welcome to the Home Page!");
    }
}

public class AdminPage extends AbstractNavigable {
    @Param
    private String adminName;

    @Override
    public void display() {
        System.out.println("Welcome, " + adminName);
    }
}

```

The generated `Navigator` class will look like:
```java
public final class Navigator {
    private Navigator() {
        throw new RuntimeException("You cannot create an instance of Navigator");
    }

    public static void navToHomePage() {
        HomePage page = new HomePage();
        page.display();
    }

    public static void navToAdminPage(String adminName) {
        AdminPage page = new AdminPage();
        try {
            Field field = page.getClass().getDeclaredField("adminName");
            field.setAccessible(true);
            field.set(page, adminName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        page.display();
    }
}

```

**Using the Navigator**
```java
public class Main {
    public static void main(String[] args) {
        Navigator.navToHomePage();
        Navigator.navToAdminPage("Amir");
    }
}

```
Output:
```css
Welcome to the Home Page!
Welcome, Amir
```

## Configuration

- **Package Flexibility:** Navigator dynamically resolves page classes without requiring a specific package. Ensure that all pages are compiled and available at runtime..
- **Error Handling:** Navigator throws a `RuntimeException` if it encounters issues setting parameters or finding fields. You may customize this to fit your needs.



## License
This project is licensed under the MIT License. See the [LICENSE](./LICENSE) file for details.


##
Feel free to add additional examples or suggestions to help users get the most out of Navigator!
