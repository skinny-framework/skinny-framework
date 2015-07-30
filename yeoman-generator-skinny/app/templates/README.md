## Skinny application

Run Skinny app now!

    ./skinny run

Let's try scaffolding. Simple Bootstrap based CRUD pages will be generated.

    ./skinny g scaffold members member name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate]
    ./skinny db:migrate
    ./skinny run

Testing is also important. Running tests is like this:

    ./skinny db:migrate test
    ./skinny test

Skinny apps are Servlet applications. Let's create war file and deploy it to Servlet containers you prefer (e.g. Jetty, Tomcat, etc.).

   ./skinny package

### Skinny Framework

Skinny is a full-stack web app framework to build Servlet applications.

To put it simply, Skinny framework’s concept is Scala on Rails. 

Skinny is highly inspired by Ruby on Rails and it is optimized for sustainable productivity for Servlet-based web app development.

http://skinny-framework.org/

### Reference Links

Skinny is built upon several stable OSS libraries. 

#### ScalikeJDBC

Skinny ORM is built upon ScalikeJDBC which is a tidy JDBC wrapper library.
Learning ScalikeJDBC will help you to understand how to work with RDB effeciently.

http://scalikejdbc.org/

#### Scalate

The Skinny MVC's default template engine is Scalate.
When you go with Scalate, please read Scalate's documentation too.

- http://skinny-framework.org/documentation/view-templates.html
- http://scalate.github.io/scalate/

#### sbt

skinny script is a simple warpper of sbt to be approachable especially for beginners.
sbt is the de facto standard build tool in Scala. Understanding sbt will help you when building Skinny apps.

http://www.scala-sbt.org/

