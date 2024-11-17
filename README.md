
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
  - [Pop and PopUntil Methods](#pop-and-popuntil-methods)
- [Example](#example)
- [Configuration](#configuration)
- [License](#license)

## Features
- **Simple Navigation:** Implement your class with AbstractNavigable.
- **Parameter Passing:** Use `@Param` to indicate required parameters for a page.
- **Type Safety:** Navigate between pages with parameter checks at compile time.
- **Code Generation:** Uses JavaPoet to create the Navigator class with necessary methods automatically.
- **Page History Management:** Includes `pop()` and `popUntil(Class<? extends AbstractNavigable> clazz)` to manage navigation history.

## Installation

### **Using Navigation as a Library**
1. **Download Navigation Jar File**
  
   Download the latest version of [ema-navigator-1.1.0.jar](./releases/ema-navigator-1.1.0.jar).
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
Define your desire page and extend it with AbstractNavigable:

- **AbstractNavigable:** An AbstractClass that every page must extend, containing the `display()` method.
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
    Scanner sc = new Scanner(System.in);

    @Override
    public void display() {
        System.out.println("insert your name:");
        String name = sc.nextLine();
        Navigator.navToHomePage("hi ", new Auth(name));
    }
}


public class Auth { // you can even pass POJO class vai Navigation
    public String name;

    public Auth(String name) {
        this.name = name;
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

## Pop and PopUntil Methods
Ema Navigator also provides methods to manage the navigation stack and handle "popping" pages from the history.

### pop()
The `pop()` method removes the most recently added page from the navigation stack.
If the stack is empty, it throws a `RuntimeException`.

Example usage:
``` java
Navigator.pop(); // Removes the most recent page from the stack
```
### popUntil(Class<? extends Navigable> page)
The `popUntil()` method removes pages from the stack until a page of the specified type is found.
If no page of the specified type is found, it throws a `RuntimeException`.

Example usage:
```java
Navigator.popUntil(AdminPage.class); // Removes pages until AdminPage is found in the stack
```
These methods allow for more dynamic navigation, giving you control over the page history.

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
    private static final Deque<Navigable> stack = new ArrayDeque<>();
    
    private Navigator() {
        throw new RuntimeException("You cannot create an instance of Navigator");
    }

    private static void setField(Object page, String fieldName, Object value) {
        try {
            Field field = page.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(page, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public static void navToHomePage() {
        HomePage page = new HomePage();
        page.display();
    }

    public static void navToAdminPage(String adminName) {
        AdminPage page = new AdminPage();
        setField(page, "adminName", adminName);
        page.display();
    }

    public static void pop() {
        if (!stack.isEmpty()) {
            stack.pop();
            if (!stack.isEmpty()) {
                ((Navigable)stack.peek()).display();
            } else {
                throw new RuntimeException("page stack couldn't be empty.");
            }
        } else {
            throw new RuntimeException("No pages to pop.");
        }
    }

    public static void popUntil(Class<? extends Navigable> page) {
        while(!stack.isEmpty() && !page.isInstance(stack.peek())) {
            stack.pop();
        }

        if (stack.isEmpty()) {
            throw new RuntimeException("No page of type " + page.getSimpleName() + " found.");
        } else {
            ((Navigable)stack.peek()).display();
        }
    }
}

```

**Using the Navigator**
```java
public class Main {
    public static void main(String[] args) {
        Navigator.navToHomePage();
        Navigator.navToAdminPage("Amir");
        Navigator.pop();
        Navigator.popUntil(HomePage.class);
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
