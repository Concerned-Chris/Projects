# The Grade Explorer
The grade explorer is a FAU student project which was started in the summer term 2020 by the SWAT course of the chair 6.
The purpose of this project is to create a web based application in order to assist lecturers with the grading workflow.
This web page should include the following features: 
- One fact in one place for grades.
- Consistent Interface (SPARQL).
- Read and Write file formats from **MeinCampus** and **AMC**. *(partly implemented)*
- Calculate statistics such as fail rate, point distribution, ... *(yet to be implemented)*
- Provide assistance and documentation for processes like the inspection of examination records or post-correction. *(yet to be implemented)*

The main focus of this application will be however the import of the **AMC** csv file, the calculation of the grading and
the export of a file which includes the grades and which then can be imported into **MeinCampus**.

The following tools were used within this project:
- **Maven** as build managing system. *([Apache Maven Homepage][maven-home])*
- **Docker** as deployment tool. *([Docker Homepage][docker-home])*
- **Jena** for the database handling *([Jena-Homepage][jena-home])*.
- **Vaadin** as Framework to create the web frontend *([Vaadin-Homepage][vaadin-home])*:
- **Git** for Version control.
- **Gitlab** for Project Managing and Continuous Integration. *([CS6-Gitlab][home])*
- **JUnit** as Framework for testing. *(However there is plenty of technical debt...)*
- **JaCoCo** to verify code coverage. *([JaCoCo-Homepage][jacoco-home])*


## Vaadin file structure
Project follow the Maven's [standard directory layout structure](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html):
- Under the `srs/main/java` are located Application sources
   - `Application.java` is a runnable Java application class and a starting point
   - `GreetService.java` is a  Spring service class
   - `MainView.java` is a default view and entry point of the application
- Under the `srs/test` are located test files
- `src/main/resources` contains configuration files and static resources
- The `frontend` directory in the root folder contains client-side dependencies and resource files
   - All CSS styles used by the application are located under the root directory `frontend/styles`    
   - Templates would be stored under the `frontend/src`

### Project-specific file structure:
    .
    ├── frontend                   # Vaadin folder
    ├── src                        # Test files (alternatively `spec` or `tests`)
    │   ├── ...
    │   │   ├── tge                # Source Code
    │   │   │   ├── DatasetClass   # Model logic
    │   │   │   ├── *View          # View logic
    │   ├── main/resources/.       # Database files. To clear the content of the page delete content of Dataset folder
    │   ├── test                   # Unit tests
    ├── pom.xml                    # Maven file which describes the project.
    ├── Dockerfile                 # Commands to build the docker container.
    ├── docker-compose.yml         # Commands to deploy the docker container.
    ├── .gitlab-ci.yml             # Commands for gitlab Continuous Integration.
    ├── newfile.tll                # Optional file which logs the database content for debugging purposes.
    └── ...



## Technical information about this project.
There are several ways to start the application. Before you start, make sure that [Maven](https://maven.apache.org/install.html), [Docker](https://docs.docker.com/get-docker/)
and [Docker Compose](https://docs.docker.com/compose/install/) are installed on your developing machine.

### Running the Application with Vaadin and Spring Boot
There are two ways to run the application :  using `mvn spring-boot:run` or by running the `Application` class directly from your IDE.

You can use any IDE of your preference,but we suggest Eclipse or Intellij IDEA.
Below are the configuration details to start the project using a `spring-boot:run` command. Both Eclipse and Intellij IDEA are covered.

### Intellij IDEA
- On the right side of the window, select Maven --> Plugins--> `spring-boot` --> `spring-boot:run` goal
- Optionally, you can disable tests by clicking on a `Skip Tests mode` blue button.

Clicking on the green run button will start the application.

After the application has started, you can view your it at [http://localhost:8080/](http://localhost:8080/) in your browser.

If you want to run the application locally in the production mode, use `spring-boot:run -Pproduction` command instead.

### Running Integration Tests

Integration tests are implemented using [Vaadin TestBench](https://vaadin.com/testbench). The tests take a few minutes to run and are therefore included in a separate Maven profile. To run the tests using Google Chrome, execute

`mvn verify -Pit`

and make sure you have a valid TestBench license installed. If the tests fail because of an old Chrome Driver or you want to use a different browser, you'll need to update the `webdrivers.xml` file in the project root.

Profile `it` adds the following parameters to run integration tests:
```sh
-Dwebdriver.chrome.driver=path_to_driver
-Dcom.vaadin.testbench.Parameters.runLocally=chrome
```

If you would like to run a separate test make sure you have added these parameters to VM Options of JUnit run configuration

### Deploy Docker Container
In order to deploy a docker container just open the command line, go to the projects root directory and run the command `docker-compose up`.
This will deploy the website which can be viewed by browsing at [http://localhost:8000/](http://localhost:8000/) on your machine.

### Run Code coverage
Code coverage will be automatically run during mavens verification phase or by runing the command `mvn verify`.
In order to view a report of your coverage execute `mvn jacoco:report` in the project directory. Reports can be found under
*./target/site/jacoco/index.html/*.

### How to Branch on Gitlab
Pushing changes to both the branches **master** and **develop** is not allowed. Therefore, you should create your own branch in order to start developing.
This can be done by executing:
```sh
git checkout -b your_branch
```

Once you are finished you can stage your changes and then commit them by executing:
```sh
git add .
git commit -m"A commit message"
```

Then you can push your changes to the remote repository in the following way:
```sh
git push --set-upstream origin your_branch
```

On gitlab you can afterwards create a merge request in order to include your changes into the **develop** branch.
After a merge request has been accepted other developers can include your changes by rebasing their branches onto develop.
This can be achieved by pulling the changes from develop and then rebasing their project.
```sh
git checkout develop
git pull
git checkout my_branch
git rebase develop
```

**Before you do this, don't forget to stash/commit your local changes, or they will get lost during this process!!**


### More Information and Next Steps

**Vaadin:**
- Vaadin Basics [https://vaadin.com/docs](https://vaadin.com/docs)
- More components at [https://vaadin.com/components](https://vaadin.com/components) and [https://vaadin.com/directory](https://vaadin.com/directory)
- Using Vaadin and Spring [https://vaadin.com/docs/v14/flow/spring/tutorial-spring-basic.html](https://vaadin.com/docs/v14/flow/spring/tutorial-spring-basic.html) article
- Join discussion and ask a question at [https://vaadin.com/forum](https://vaadin.com/forum)

**Docker:**
- Get Started with Docker [https://docs.docker.com/get-started/](https://docs.docker.com/get-started/)

**Maven:**
- Learn more about maven [https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html](https://maven.apache.org/guides/getting-started/maven-in-five-minutes.html)

**Jena:**
- Get familiar with Apache Jena [https://jena.apache.org/tutorials/](https://jena.apache.org/tutorials/) 

**JaCoCo**
- Introduction to Code Coverage [https://www.baeldung.com/jacoco](https://www.baeldung.com/jacoco)

### Notes

If you run application from a command line, remember to prepend a `mvn` to the command.



## Contributors
Icon | Name | Role | E-Mail address |  
-----| -----| -----| -------------- | 
:mask: | Monique Mück | Developer | monique.mueck@fau.de | 
:see_no_evil: | Yannick Vorbrugg | Developer| yannick.vorbrugg@fau.de| 
:smirk: | Christoph Wohlwend | Developer | christoph.cw.wohlwend@studium.fau.de |
:blush: | David Haller | Product Owner | david.haller@fau.de |
:blush: | Demian Vöhringer | Scrum Master | demian.voehringer@fau.de |


[maven-home]: https://maven.apache.org/
[docker-home]: https://www.docker.com/
[jena-home]: https://jena.apache.org/
[vaadin-home]: https://www.vaadin.com/
[jacoco-home]: https://www.jacoco.org/
[home]: https://cs6-gitlab.cs6.fau.de/lehre/swat/ss2020/the-grade-explorer