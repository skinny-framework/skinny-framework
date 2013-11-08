# Skinny Framework 

Skinny is a full-stack web app framework, which is built on [Scalatra](http://scalatra.org) and additional components are integrated. 

To put it simply, Skinny framework's concept is **Scala on Rails**. Skinny is highly inspired by [Ruby on Rails](http://rubyonrails.org/) and it is optimized for sustainable productivity for ordinary Servlet-based app development. 

![Logo](https://github.com/seratch/skinny-framework/raw/develop/img/logo.png)

**[Notice]** Still in alpha stage. Architecture and API compatibility won't be kept until 1.0 release (2013 4Q).

### Why Skinny?

What does the name of `Skinny` actually mean?

#### Application should be skinny

All the parts of web application - controllers, models, views, routings and other settings - should be skinny. If you use Skinny framework, you don't need to have non-essential code anymore. For instance, when you create a simple registration form, all you need to do is just defining parameters and validation rules and creating view templates in an efficient way (ssp, scaml, jade, FreeMarker or something else) in most cases.

#### Framework should be skinny

Even if you need to investigate Skinny's inside, don't worry. Skinny keeps itself skinny, too. I believe that if the framework is well-designed, eventually the implementation is skinny. 

#### "su-ki-ni" in Japanese means "as you like it"

A sound-alike word **"好きに (su-ki-ni)"** in Japanese means **"as you like it"**. This is only half kidding but it also represents Skinny's concept. Skinny framework should provide flexible APIs to empower developers as much as possible and shouldn't bother them.

## How to use

Actually, application built with Skinny framework is a Scalatra application. After preparing Scalatra app, just add the following dependency to your `project/Build.scala`.

```scala
libraryDependencies ++= Seq(
  "com.github.seratch" %% "skinny-framework" % "[0.9,)",
  "com.github.seratch" %% "skinny-task"      % "[0.9,)",
  "com.github.seratch" %% "skinny-test"      % "[0.9,)" % "test"
)
```

If you need only Skinny-ORM or Skinny-Validator, you can use only what you need. Even if you're a Play2 (or any others) user, these components are available for you as well.

```scala
libraryDependencies ++= Seq(
  "com.github.seratch" %% "skinny-orm"       % "[0.9,)",
  "com.github.seratch" %% "skinny-validator" % "[0.9,)",
  "com.github.seratch" %% "skinny-test"      % "[0.9,)" % "test"
)
```

## Try Skinny now

Download `skinny-blank-app.zip` and unzip it, then just run ./skinny command on your terminal. That's all!

If you're a Windows user, don't worry. Use skinny.bat on cmd.exe instead.

[![Download](https://github.com/seratch/skinny-framework/raw/develop/img/blank-app-download.png)](https://github.com/seratch/skinny-framework/releases/download/0.9.12/skinny-blank-app.zip)

Let's create your first Skinny app by using scaffold generator.

```sh
./skinny g scaffold members member name:String activated:Boolean luckyNumber:Option[Long] birthday:Option[LocalDate]
./skinny db:migrate
./skinny run
```

And then, access `http://localhost:8080/members`.

You can run generated tests.

```
./skinny db:migrate test
./skinny test
```

Let's create war file to deploy.

```sh
./skinny package
```

### Yeoman generator

![Yeoman](https://github.com/seratch/skinny-framework/raw/develop/img/yeoman.png)

If you're familiar with [Yeoman](http://yeoman.io), a generator for [Skinny framework](https://github.com/seratch/skinny-framework) is available.

[![NPM](https://nodei.co/npm/generator-skinny.png?downloads=true)](https://npmjs.org/package/generator-skinny)

```sh
# brew instsall node
npm install -g yo
npm install -g generator-skinny
mkdir skinny-app
cd skinny-app
yo skinny
./skinny run
```

## Components

### Routing & Controller & Validator

Skinny's routing mechanism and controller layer on MVC architecture is a **rich Scalatra**. Skinny's extension provides you much simpler/rich syntax. Of course, if you need to use Scalatra's API directly, Skinny never bothers you.

![Scalatra](https://github.com/seratch/skinny-framework/raw/develop/img/scalatra.png)

`SkinnyController` is a trait which extends `ScalatraFilter` and out-of-the-box components are integrated. 

```scala
// src/main/scala/controller/MembersController.scala
class MembersController extends SkinnyController {
  protectFromForgery()

  beforeAction(only = Seq('index, 'new)) { set("countries", Country.findAll()) }

  def index = {
    // set 'members' in the request scope, then you can use it in views
    set("members" -> Member.findAll())
    render("/members/index")
  }

  def newOne = render("/members/new")

  def createForm = validation(
    paramKey("name") is required & minLength(2), 
    paramKey("countryId") is numeric
  )

  def createFormParams = params.permit(
    "groupId" -> ParamType.Int , "countryId" -> ParamType.Long)

  def create = if (createForm.validate()) {
    Member.createWithPermittedAttributes(createFormParams)
    redirect("/members")
  } else {
    render("/members/new")
  }
}

// src/main/scala/controller/Controllers.scala
object Controllers {
  val members = new MembersController with Routes {
    get("/members/?")(index).as('index)
    get("/members/new")(newOne).as('new)
    post("/members/?")(create).as('create)
  }
}

// src/main/scala/ScalatraBootstrap.scala
class ScalatraBootstrap exntends SkinnyLifeCycle {
  override def initSkinnyApp(ctx: ServletContext) {
    // register routes
    Controllers.members.mount(ctx)
  }
}
```

Skinny-Validator is newly created validator which is based on [seratch/inputvalidator](https://github.com/seratch/inputvalidator) and much improved. Rules are so simple that you can easily add original validation rules. Furthermore, you can use this validator with any other frameworks.

```scala
import skinny.validator._
object alphabetOnly extends ValidationRule {
  def name = "alphabetOnly"
  def isValid(v: Any) = isEmpty(v) || v.toString.matches("^[a-zA-Z]*$")
}

def createForm = validation(
  paramKey("name") is required & minLength(2) & alphabetOnly, 
  paramKey("countryId") is numeric
)
```

`SkinnyResource` which is similar to Rails ActiveResource is available. That's a pretty DRY way.

```scala
object CompaniesController extends SkinnyResource {
  protectFromForgery()

  override def model = Company
  override def resourcesName = "companies"
  override def resourceName = "company"

  override def createForm = validation(
    paramKey("name") is required & maxLegnth(64), paramKey("registrationCode" is numeric)
  override def createFormStrongParameters = 
    Seq("name" -> ParamType.String, "registrationCode" -> ParamType.Int)

  override def updateForm = validation(paramKey("name") is required & maxLegnth(64))
  override def updateFormStrongParameters = Seq("name" -> ParamType.String)
}
```

`Company` object should implement `skinny.SkinnyModel` APIs and you should prepare some view templates under `src/main/webapp/WEB-INF/views/members/`.

### ORM

Skinny provides you Skinny-ORM as the default O/R mapper, which is built with [ScalikeJDBC](https://github.com/seratch/scalikejdbc).

![Logo](https://github.com/seratch/skinny-framework/raw/develop/img/scalikejdbc.png)

Skinny-ORM is much powerful, so you don't need to write much code. Your first model class and companion are here.

```scala
case class Member(id: Long, name: String, createdAt: DateTime)

object Member extends SkinnyCRUDMapper[Member] {
  // only define ResultSet extractor at minimum
  override def extract(rs: WrappedResultSet, n: ResultName[Member]) = new Member(
    id = rs.long(n.id),
    name = rs.string(n.name),
    createdAt = rs.dateTime(n.createdAt)
  )
}
```

That's all! Now you can use the following APIs.

```scala
Member.withAlias { m => // or "val m = Member.defaultAlias"
  // find by primary key
  val member: Option[Member] = Member.findById(123)
  val member: Option[Member] = Member.where('id -> 123).apply().headOption
  val members: List[Member] = Member.where('id -> Seq(123, 234, 345)).apply()
  // find many
  val members: List[Member] = Member.findAll()
  val groupMembers = Member.where('groupName -> "Scala Users", 'deleted -> false).apply()
  // count
  val allCount: Long = Member.countAll()
  val count = Member.countBy(sqls.isNotNull(m.deletedAt).and.eq(m.countryId, 123))
  val count = Member.where('deletedAt -> None, 'countryId -> 123).count.apply()

  // create with stong parameters
  val params = Map("name" -> "Bob")
  val id = Member.createWithPermittedAttributes(
    params.permit("name" -> ParamType.String))
  // create with unsafe parameters
  Member.createWithAttributes(
    'id -> 123,
    'name -> "Chris",
    'createdAt -> DateTime.now
  )

  // update with strong parameters
  Member.updateById(123).withAttributes(params.permit("name" -> ParamType.String))
  // update with unsafe parameters
  Member.updateById(123).withAttributes('name -> "Alice")

  // delete
  Member.deleteById(234)
}
```

If you need to join other tables, just add `belongsTo`, `hasOne` or `hasMany` (`hasManyThrough`) to the companion.

**[Notice]** Unfortunately, Skinny-ORM doesn't retrieve nested associations (e.g. members.head.groups.head.country) automatically though we're still seeking a way to resolve this issue.

```scala
class Member(id: Long, name: String, companyId: Long, 
  company: Option[Company] = None, skills: Seq[Skill] = Nil)

object Member extends SkinnyCRUDMapper[Member] {
  // If byDefault is called, this join condition is enabled by default
  belongsTo[Company](Company, (m, c) => m.copy(company = Some(c))).byDefault
  val skills = hasManyThrough[Skill](
    MemberSkill, Skill, (m, skills) => m.copy(skills = skills))
}

Member.findById(123) // without skills
Member.joins(Member.skills).findById(123) // with skills
```

If you need to add methods, just write methods that use ScalikeJDBC' APIs directly.

```scala
object Member extends SkinnyCRUDMapper[Member] {
  val m = defaultAlias
  def findByGroupId(groupId: Long)(implicit s: DBSession = autoSession): List[Member] = 
    withSQL { select.from(Member as m).where.eq(m.groupId, groupId) }
      .map(apply(m)).list.apply()
}
```

`timetamps` from `ActiveRecord` is available as the `TimestampsFeature` trait.

```scala
class Member(id: Long, name: String, createdAt: DateTime, updatedAt: Option[DateTime] = None)
object Member extends SkinnyCRUDMapper[Member] with TimestampsFeature[Member]
// created_at timestamp not null, updated_at timestamp
```

Soft delete support is also available.

```scala
object Member extends SkinnyCRUDMapper[Member] 
  with SoftDeleteWithTimestamp[Member]
// deleted_at timestamp
```

Furthermore, optimistic lock is also available.

```scala
object Member extends SkinnyCRUDMapper[Member] 
  with OptimisticLockWithVersionFeature[Member]
// lock_version bigint
```


### DB Migration

DB migration comes with [Flyway](http://flywaydb.org/). Usage is pretty simple.

![Flyway Logo](https://github.com/seratch/skinny-framework/raw/develop/img/flyway.png)

```sh
./skinny db:migrate [env]
````

This command expects `src/main/resources/db/migration/V***_***.sql` files. 

Try it with [blank-app](https://github.com/seratch/skinny-framework/releases) right now!


### View Templates

Skinny framework basically follows Scalatra's [Scalate](http://scalate.fusesource.org/)Support, but Skinny has an additional convention.

![Scalate Logo](https://github.com/seratch/skinny-framework/raw/develop/img/scalate.png)

Templates' path should be `{path}.{format}.{extension}`. Expected {format} are `html`, `json`, `js` and `xml`.

The following ssp is `src/main/webapp/WEB-INF/views/members/index.html.ssp`.

```scala
<%@val members: Seq[model.Member] %>
<h3>Members</h3>
<hr/>
<table class="table table-bordered">
<thead>
  <tr>
    <th>ID</th>
    <th>Name</th>
    <th></th>
  </tr>
</thead>
<tbody>
  #for (member <- members)
  <tr>
    <td>${member.id}</td>
    <td>${member.name}</td>
    <td>
      <a href="/members/${member.id}/edit" class="btn btn-info">Edit</a>
      <a data-method="delete" data-confirm="Are you sure?" href="/members/${member.id}" class="btn btn-danger">Delete</a>
    </td>
  </tr>
  #end
</tbody>
</table>
```

Your controller code will be like this:

```scala
class MembersController extends SkinnyController {
  def index = {
    set("members", Member.findAll())
    render("/members/index")
  }
}
```

If you need to customize view templates, override the settings.

```scala
class MembersController extends SkinnyServlet {
  override val scalateExtension = "scaml"
}
```

And then, use scaml instead.

### CoffeeScript & LESS support

First, add `skinny-assets` to libraryDependencies.

```scala
libraryDependencies ++= Seq(
  "com.github.seratch" %% "skinny-framework" % "[0.9,)",
  "com.github.seratch" %% "skinny-assets"    % "[0.9,)",
  "com.github.seratch" %% "skinny-test"      % "[0.9,)" % "test"
)
```

And then, add `AssetsController` to routes. Now you can easily use CoffeeScript, TypeScript and LESS.

```scala
// src/main/scala/ScalatraBootstrap.scala
class ScalatraBootstrap exntends SkinnyLifeCycle {
  override def initSkinnyApp(ctx: ServletContext) {
    AssetsController.mount(ctx)
  }
}
```

#### CoffeeScript 

![CoffeeScript Logo](https://github.com/seratch/skinny-framework/raw/develop/img/coffeescript.png)

If you use CoffeeScript, just put *.coffee files under `WEB-INF/assets/coffee`:

```coffeescript
# src/main/webapp/WEB-INF/assets/coffee/echo.coffee
echo = (v) -> console.log v
echo "foo"
```

You can access the latest compiled JavaScript code at `http://localhost:8080/assets/js/echo.js`.

```javascript
(function() {
  var echo;

  echo = function(v) {
    return console.log(v);
  };

  echo("foo");

}).call(this);
```

#### LESS 

![LESS Logo](https://github.com/seratch/skinny-framework/raw/develop/img/less.png)

If you use LESS, just put *.less files under `WEB-INF/assets/less`:

```less
// src/main/webapp/WEB-INF/assets/less/box.less
@base: #f938ab;
.box { 
  color: saturate(@base, 5%);
  border-color: lighten(@base, 30%);
}
```

You can access the latest compiled CSS file at `http://localhost:8080/assets/css/box.css`.

```css
.box {
  color: #fe33ac;
  border-color: #fdcdea;
}
```

In production environment, precompiling coffee/less files to js/css is recommended.


### Testing support

You can use Scalatra's great test support. Some optional feature is provided by skinny-test library.

```scala
class ControllerSpec extends ScalatraFlatSpec with SkinnyTestSupport {
  addFilter(MembersController, "/*")

  it should "show index page" in {
    withSession("userId" -> "Alice") {
      get("/members") { status should equal(200) }
    }
  }
}
```

You can see some examples here:

https://github.com/seratch/skinny-framework/tree/develop/example/src/test/scala

### FactoryGirl

Though Skinny's FactoryGirl is not a complete port of [thoughtbot/factory_girl](https://github.com/thoughtbot/factory_girl), this module will be quite useful when testing your apps.

```scala
case class Company(id: Long, name: String)
object Company extends SkinnyCRUDMapper[Company] {
  def extract ...
}

val company1 = FactoryGirl(Company).create()
val company2 = FactoryGirl(Company).create('name -> "FactoryPal, Inc.")

val country = FactoryGirl(Country, 'countryyy).create()

val memberFactory = FactoryGirl(Member).withValues('countryId -> country.id)
val member = memberFactory.create('companyId -> company1.id, 'createdAt -> DateTime.now)
```

Settings is not in yaml files but typesafe-config conf file. In this example, `src/test/resources/factories.conf` is like this:

```
countryyy {
  name="Japan"
}
member {
  countryId="#{countryId}"
}
company {
  name="FactoryGirl, Inc."
}
name {
  first="Kazuhiro"
  last="Sera"
}
skill {
  name="Scala Programming"
}
```

### TODO

These are major tasks that Skinny should fix.

 - Official website
 - Documentation (wiki)
 - Designing Authentication API
 - View helper API
 - Framework test coverage
 - Executable war file generation

Your feedback or pull requests are always welcome.

## License

(The MIT License)

Copyright (c) 2013 Kazuhiro Sera @seratch


