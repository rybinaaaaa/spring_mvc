# #spring/mvc/CRUD

## 0. Paramètres query

```
@GetMapping("/paged")
public String index(Model model, @RequestParam("p") int page,
                    @RequestParam("c") int bookPerPage) {
    List<Book> books = bookService.findAll(page, bookPerPage);
    model.addAttribute("books", books);
    return "books/index";
}
```

*@RequestParam("p") int page,  @RequestParam("c") int bookPerPage*

## 1. GET запрос
```
@GetMapping("/{id}")
public String show(@PathVariable("id") int id, Model model) {
    model.addAttribute("person", personDAO.show(id));
    return "people/show";
}

```

— фигурные скобки обозначают динамический путь,  взять параметр можно с помощью **@PathVariable("id")**

— модель передает информацию в шаблон автоматически, нам стоит просто указать **model.addAttribute**

## 2. POST запрос
Для начала мы создаем страничку с формой 

На этой страничке мы должны подготовить контроллер для формы

```
@GetMapping("/new")
public String newPerson(Model model) {
    model.addAttribute("person", new Person());
    return "people/new";
}
```


Форма будет выглядеть так: 

<form th:method="POST" th:action="@{/people}" th:object="${person}">
    <label for="name">Enter name: </label>
    <input type="text" th:field="*{name}" id="name"/>
    <br/>
    <input type="submit" value="Create!"/>
</form>

— выходит, что object - тот самый пустой объект и мы ему присваиваем значения

## 3. @ModelAttribute
> Интересная аннотация, которая в зависимости от ее позиции имеет разное свойство

- Рассмотрим ее **в качестве параметра**

```
@GetMapping("/new")
public String newPerson(@ModelAttribute("person") Person person) {
    return "people/new";
}

ЭТО ЕКВИВАЛЕНТНО

@GetMapping("/new")
public String newPerson(Model model) {
    model.addAttribute("person", new Person());
    return "people/new";
}

----------------------------------------------------------------------------

@PostMapping()
public String create(@ModelAttribute("person") Person person) {
    personDAO.save(person);
    return "redirect:people";
}
```

— тут она создает автоматически пустой объект типа Person и если это метод POST, то еще и **автоматически заполняет пустой объект данными, которые пришли с POST запроса И кладет это в модель обратно**
## 4. PATCH, DELETE
> В официальной версии хтмл 5 в форме всего два типа action: POST, GET, поэтому мы используем patch & delete через thymeleaf:

<form th:method="PATCH" th:action="@{/people/{id}(id=${person.getId()})}" th:object="${person}">

### Но! МЫ должны добавить в mySpringMvcDispatcherInitializer.java

```
    @Override
     public void onStartup(ServletContext aServletContext) throws ServletException {
         super.onStartup(aServletContext);
         registerHiddenFieldFilter(aServletContext);
     }

     private void registerHiddenFieldFilter(ServletContext aContext) {
         aContext.addFilter("hiddenHttpMethodFilter",
                 new HiddenHttpMethodFilter()).addMappingForUrlPatterns(null ,true, "/*");
     }
```


## 5. Validator

Для валидации форм мы используем hibernate-validator
### Объявляем аппликацию так:
```
@NotEmpty(message = "Name can not be empty")
@Size(min = 2, max = 30, message = "Name should be longer then 2 symbols & shorter then 30 symbols")
private String name;

@Min(value = 0, message = "male should not to be empty")
private int age;

@NotEmpty()
@Email()
private String email;
```
— то есть просто ставим сразу ограничения которые нам нужны в атрибуты **модели**
### Далее нам надо активировать валидацию в контроллере
```
@PatchMapping("/{id}")
public String update(@ModelAttribute("person") @Valid Person person, BindingResult bindingResult,
                     @PathVariable("id") int id) {
    if (bindingResult.hasErrors())
        return "people/edit";

    personDAO.update(id, person);
    return "redirect:/people";
}
```
— **ВАЖНО, что после валидированого объекта мы обязаны сразу поставить параметр BindingResult bindingResult**
### Используем в шаблоне

<input type="text" th:field="*{name}" id="name"/>
<div style="color:red" th:if="${#fields.hasErrors('name')}" th:errors="*{name}">Name Error</div>

Этот div автоматически скрывается если ошибок нет, если есть, то **таймлиф автоматически подставляет сообщение ошибки, а текст внутри блока стирает**
```
th:if="${#fields.hasErrors('name')}" th:errors="*{name}"
```

## 6. JDBC API

> **JDBC API** - самый низкоуровненвый способ взаимодействия с БД

**Подключение к бд (условно говоря)**

```
private Connection connection;

static {
    Class.forName("org.postgresql.Driver");
    connection = DriverManager.getConnection(URL, USER, PASSWORD);
}
```


**Запрос на получение информации с бд**
```
Statement statement = connection.createStatement();
String SQL = "SELECT * FROM PERSON";
ResultSet resultSet = statement.executeQuery(SQL);

while (resultSet.next()) {
    Person person = new Person();
    person.setAge(resultSet.getInt("age"));
    person.setName(resultSet.getString("name"));
    person.setEmail(resultSet.getString("email"));

    people.add(person);
}
```

Либо же если нам надо что-то обновить в бд и она нам ничего не возвращает, то мы используем **statement.executeUpdate(SQL)**

## 7. SQL Injectionsк как предотвратить
В повседневности никто не пишет sql запрос строкой
**мы заменяем statement на preparedStatement**

```
PreparedStatement preparedStatement = connection.prepareStatement("UPDATE PERSON SET NAME = ?, AGE = ?, email = ? WHERE ID = ?");

preparedStatement.setString(1, updatedPerson.getName());
preparedStatement.setInt(2, updatedPerson.getAge());
preparedStatement.setString(3, updatedPerson.getEmail());
preparedStatement.setInt(4, id);

preparedStatement.executeUpdate();
```

> Данный способ намного производительнее потому, что запрос компилируется лишь раз, а в statement каждый раз когда мы меняем sql сроку, я думаю это логично

## 8. JDBC Template

### 1. Настройка
SpringConfig: добавляем два бина

```
@Bean
public DataSource dataSource() {
    final String URL = "jdbc:postgresql://localhost:5432/first_db";
    final String PASSWORD = "aqwsde322";
    final String USER = "postgres";
    DriverManagerDataSource dataSource = new DriverManagerDataSource();
    dataSource.setDriverClassName("org.postgresql.Driver");
    dataSource.setUrl(URL);
    dataSource.setPassword(PASSWORD);
    dataSource.setUsername(USER);

    return dataSource;
}

@Bean
public JdbcTemplate jdbcTemplate() {
    return new JdbcTemplate(dataSource());
}
```

Добавляем наш темплейт в ДАО: 
```
private final JdbcTemplate jdbcTemplate;

@Autowired
public PersonDAO(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
}
```

### 2. Используем 

- Query

```
return jdbcTemplate.query("SELECT * FROM PERSON WHERE ID = ?", new Object[]{id} ,new BeanPropertyRowMapper<>(Person.class)).stream().findAny().orElse(null);
```

— 1 параметром идет наш запрос, 
— 2 параметры, которые мы указали в запросе,
— 3 **класс, который будет превращать результат в объект, нужный нам, он называется RowMapper**

- Update

```
jdbcTemplate.update("INSERT INTO PERSON(NAME, EMAIL, AGE) VALUES(?, ?, ?)", person.getName(), person.getEmail(), person.getAge());
```

— 1 параметр - запрос
— 2+ параметр - наши параметры в запросе

>  Отличием в update и query служит то, что параметры запроса в 1 передаются просто в конце через запятую, а в query списком типа Object[].
> Также в  query запросе мы обязаны указать **RowMapper**

### 3. RowMapper
Реализация: 
```
public class PersonMapper implements RowMapper<Person> {

    @Override
    public Person mapRow(ResultSet resultSet, int i) throws SQLException {
        Person person = new Person();

        person.setAge(resultSet.getInt("age"));
        person.setName(resultSet.getString("name"));
        person.setEmail(resultSet.getString("email"));
        person.setId(resultSet.getInt("id"));

        return person;
    }
}
```

— просто класс, который реализует интерфейс, используемый для перевода из сущности бд в нашу сущность 

> Если у нас все поля в бд соответствуют названиям атрибутов, то мы используем **дефолтный BeanPropertyRowMapper<>(наш объект>.class))** - это реализованный RowMapper

## 9. Принятая конфигурация 

#### Создание скрытых данных

Для принятой конфигурации мы создаем в папке resources файл **database.properties**, пишем там наши переменные

```
~ database.properties:

DRIVER=org.postgresql.Driver
URL=jdbc:postgresql://localhost:5432/first_db
PASSWORD=aqwsde322
USER=postgres
```

Также у нас есть файл **database.properties.origin**, который  хранит просто необходимый переменные без их значений

```
~ database.properties.origin:

DRIVER=
URL=
PASSWORD=
USER=
```

#### Включение данных в конфигурацию

1. Включаем наш ресурс в конфигурацию **SpringConfig**, прописываем путь 
   “classpath:database.properties” 

```
@Configuration
@ComponentScan("rybina")
@EnableWebMvc
@PropertySource("classpath:database.properties")
```

Что такое classpath?
![](Screenshot%202023-09-21%20at%2016.44.40.png)<!-- {"width":282} -->

— это то, что будет лежать после компиляции в файле classes. Наши файлы с папки resources после компиляции будут там лежать

2. Создаем переменную которая будет хранить все наши ключ-значения

```
private final ApplicationContext applicationContext;
private final Environment environment;

@Autowired
public SpringConfig(ApplicationContext applicationContext, Environment environment) {
    this.applicationContext = applicationContext;
    this.environment = environment;
}
```

3. Используем

```
final String URL = environment.getProperty("URL");
final String PASSWORD = environment.getProperty("PASSWORD");
final String USER = environment.getProperty("USER");
```

## 10. Batch update

>  **Batch update** - это когда мы n-ное кол-во запросов шлем одним целым. Так повышается производительность


```
jdbcTemplate.batchUpdate("INSERT INTO PERSON(NAME, EMAIL, AGE) VALUES(?, ?, ?)", new BatchPreparedStatementSetter() {
    @Override
    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
        preparedStatement.setString(1, people.get(i).getName());
        preparedStatement.setString(2, people.get(i).getEmail());
        preparedStatement.setInt(3, people.get(i).getAge());
    }

    @Override
    public int getBatchSize() {
        return people.size();
    }
});
```

— 2 параметр i - это обычный номер итерации

*подходит для большого количества данных*


— самое адекватное для меня - это создать ДАО, потом создать Контроллер, а потом создать модель

### 11. Hibernate 

Добавляем зависимость 

        <dependency>
             <groupId>org.hibernate</groupId>
             <artifactId>hibernate-core</artifactId>
             <version>${hibernate.version}</version>
         </dependency>

Добавляем нужные бины 

```
@PropertySource("classpath:hibernate.properties")
@EnableTransactionManagement
class SpringConfig {
...

  private Properties hibernateProperties() {
         Properties properties = new Properties();
         properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
         properties.put("hibernate.show_sql", environment.getRequiredProperty("hibernate.show_sql"));

         return properties;
     }

     @Bean
     public LocalSessionFactoryBean sessionFactory() {
         LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
         sessionFactory.setDataSource(dataSource());
         sessionFactory.setPackagesToScan("ru.alishev.springcourse.models");
         sessionFactory.setHibernateProperties(hibernateProperties());

         return sessionFactory;
     }

     @Bean
     public PlatformTransactionManager hibernateTransactionManager() {
         HibernateTransactionManager transactionManager = new HibernateTransactionManager();
         transactionManager.setSessionFactory(sessionFactory().getObject());

         return transactionManager;
     }
```


## 12. Data JPA

Добавляем зависимость 

      <dependency>
            <groupId>org.springframework.data</groupId>
            <artifactId>spring-data-jpa</artifactId>
            <version>2.4.7</version>
        </dependency>

Меняем sessionFactory, hibernateTransactionManager

```
     @Bean
     public LocalSessionFactoryBean sessionFactory() {
         LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
         sessionFactory.setDataSource(dataSource());
         sessionFactory.setPackagesToScan("ru.alishev.springcourse.models");
         sessionFactory.setHibernateProperties(hibernateProperties());

         return sessionFactory;
     }

     @Bean
     public PlatformTransactionManager hibernateTransactionManager() {
         HibernateTransactionManager transactionManager = new HibernateTransactionManager();
         transactionManager.setSessionFactory(sessionFactory().getObject());

         return transactionManager;
     }
```

На это entityManagerFactory, transactionManager

```
@Bean
public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
    final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();

    em.setDataSource(dataSource());
    em.setPackagesToScan("rybina.models");

    final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();

    em.setJpaVendorAdapter(vendorAdapter);
    em.setJpaProperties(hibernateProperties());

    return em;
}

@Bean
public PlatformTransactionManager transactionManager() {
    JpaTransactionManager transactionManager = new JpaTransactionManager();

    transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());

    return transactionManager;
}
