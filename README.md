# Distributed Tracing Graph

The program takes the input graph and runs given tests on it.

## How to Run

You can run this program with maven and java command line, or you can run it with the provided docker image.
The default input graph file is present in the repo root folder.

### With `maven` and `java`
* First build the project to generate a `jar` file
    * In repo roo run `mvn install`
* It will generate the jar file in `target` folder named `distributed-tracing-graph-1.0-SNAPSHOT.jar`
* Now you can run it with following command:
  * ```java -jar target/distributed-tracing-graph-1.0-SNAPSHOT.jar <input file path>```
  * For input file path it is easy to provide absolute path
* console will output the result of program

For test cases run this command in repo root: `mvn test`

### With `docker`

* The project has a Dockerfile as well which build the project inside it and then we can run the container to run the 
application.
* There is also a `run.sh` script in repo which builds and then run the docker container.
* To run the application with docker run the file `run.sh` like this:
  * ```bash run.sh <input-file-path>```
  *  For input file path it is easy to provide absolute path
  * If no `input-file-path` is provided docker will use the default graph input file `input.txt`
* Console will output the `docker build` step and then output the `docker run` result

For test cases run the `test.sh` script: `bash test.sh` (it will download mvn repo in the container, host `.m2` is not mounted)