## Siemens Java Internship - Code Refactoring Project

This repository contains a Spring Boot application that implements a simple CRUD system with some asynchronous processing capabilities.

## Getting Started
- Clone this repository
- To run the tests you can run:

        mvn test

- To run the application you can run (will use the properties from [application.properties](src/main/resources/application.properties)):

        mvn spring-boot:run

- To view debuging logs make sure you set the logging config appropriately. Example logging configuration:
  
        logging.level.root=INFO
        logging.level.com.siemens.internship=DEBUG


## Applied changes

### Error handling

* configured **ControllerAdvice** to return proper status codes and error details on certain exceptions:
  * **MethodArgumentNotValidException**. Returns 400 and a response body like this:
  
    ```json
     {
       "details": ["invalid email format", "name must not be empty"]       
     }
    ```
  * **EntityNotFoundException**. Returns 404 and a response body like this:
     ```json
     {
       "details": ["Item with id %d not found"]
     }
     ```
    
### Status codes

* modified status codes for endpoints as follows:
  * GET api/items 
    * returns 200 and  a list of items (if any)
  * POST api/items
    * returns 201 and the newly created item on success
    * returns 400 on invalid payload with the error details.
  * GET api/items/{id}
    * returns 200 and the existing item
    * returns 404 and an error message like "Item with id: {id} was not found" if the item does not exist
  * POST api/items/{id}
    * returns 200 and the updated item if the payload is valid
    * returns 400 and error details if the payload is not valid
    * returns 404 if the item does not exist
  * DELETE api/items/{id}
    * returns 204
  * GET api/items/process
    * returns 200 and a list of processed items (if any)

### Async processing

#### Issues found:
* improper usage of spring @Async: the method decorated with @Async should return either void or a Future
* used a custom Executor instead of the one managed by Spring
* there were concurrency issues, it modified a non thread-safe collection and processedCount
* there was no mechanism to wait for the tasks to complete
* there were to many responsibilities for the method i.e find ids, then define the task routine, sumbit the routine and take the results

#### Solution
* configured the Executor by defining a new bean. The method [taskExecutor](src/main/java/com/siemens/internship/Application.java), since this is the method name for which Spring searches.
* separated the concerns by creating a new interface [AsyncItemProcessor](src/main/java/com/siemens/internship/service/AsyncItemProcessor.java) and a concrete implementation which is injected into the ItemService class, with the role 
of processing an item i.e take it from db and update it. Also the processItemsAsync method from the ItemService now takes a List of ids to process. This separation made it easier to test both the actual processing of an item and the overall processing
* now, each task will complete either in the updated item or null and the ItemService waits for the completion of the tasks before returning the result
* this means that there is no need to update a collection inside the async task but create it when they are done omitting the tasks completed in null.
* also, modified the findAllIds to return the ids of the items that where not already process

### Validation

* the validation is minimal i.e just checking for '@' in the given email. Could use a more complex regex but that doesn't mean the email is valid. In my opinion a valid email address is one
that the user can verify, so a better validation "flow" would be to send a verification code and make the user send that code, only then save the email.

### Future modifications:

* another problem is the RequestBody Schema, for example on POST api/items the payload is expected to look as an Item that means that the client can send an id, that will cause problems:
  * the item with that id already exists => it will result in an update
  * the item does not exist => it will result in an insert with that id, this will have some conflicts with the id sequence in the database at some point
  * to solve this, currently i create a new Item object with status = ItemStatus.UNPROCESSED and id = null, but this can be modified using a dto. Did not make this modification because that would mean that any "existing" client will have to modify their request
* i would put a partial index on item.status where status = 'UNPROCESSED' thus optimizing the performance of the query with a smaller index size
