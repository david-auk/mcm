# MCM

> A multi user/server minecraft manager

### Development

Within development i put my focus on a few categories:

 * DRY (Dont Repeat Yourself)
 * Keeping code readable and logical
 * Keeping code future-proof

##### Challenges i encountered concerning these categories

###### Complex high level coding solutions

> Meaning complex low-level solutions were coded in high-level places.

So for instance; database complex requests where requests were handled in the endpoint.
This whilst the logic was perfectly reusable for future expansion making this more `futre-proof` and `Keeping code future-proof`

###### Solution:

Dividing the logic into multiple levels:

 * `app/database/core` This is where the Database connection is managed and communication to the database.
    
    It also features building blocks for Object -> Table integration

    **Rules**:

    * **Abstraction**. Within the folder can no implementation specific code live. (So no implementation of a specific table for instance)


 * `app/api` This is where all the endpoints are defined.

   **Rules**:

   * Each endpoint must have a logical **HTTP Method**
   * Each endpoint must have one purpose only
   * Each endpoint must fulfill this purpose

 * `app/`