package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.PersonAddressDTO;
import dto.PersonDTO;
import dto.PersonsAddressesDTO;
import entities.Address;
import entities.Person;
import exceptions.PersonNotFoundException;
import exceptions.PersonNotFoundExceptionMapper;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.parsing.Parser;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.server.ResourceConfig;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class PersonResourceTest {
    public PersonResourceTest() {
    }
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int SERVER_PORT = 7777;
    private static final String SERVER_URL = "http://localhost/api";
    private static Person p1,p2,p3,p4;
    private static Address a1,a2,a3,a4;

    static final URI BASE_URI = UriBuilder.fromUri(SERVER_URL).port(SERVER_PORT).build();
    private static HttpServer httpServer;
    private static EntityManagerFactory emf;

    static HttpServer startServer() {
        ResourceConfig rc = ResourceConfig.forApplication(new ApplicationConfig());
        return GrizzlyHttpServerFactory.createHttpServer(BASE_URI, rc);
    }

    @BeforeAll
    public static void setUpClass() {
        //This method must be called before you request the EntityManagerFactory
        EMF_Creator.startREST_TestWithDB();
        emf = EMF_Creator.createEntityManagerFactoryForTest();

        httpServer = startServer();
        //Setup RestAssured
        RestAssured.baseURI = SERVER_URL;
        RestAssured.port = SERVER_PORT;
        RestAssured.defaultParser = Parser.JSON;
    }

    @AfterAll
    public static void closeTestServer(){
        //System.in.read();
        //Don't forget this, if you called its counterpart in @BeforeAll
        EMF_Creator.endREST_TestWithDB();
        httpServer.shutdownNow();
    }

    // Setup the DataBase (used by the test-server and this test) in a known state BEFORE EACH TEST
    //TODO -- Make sure to change the EntityClass used below to use YOUR OWN (renamed) Entity class
    @BeforeEach
    public void setUp() {
        EntityManager em = emf.createEntityManager();
        a1 = new Address("Address1","1111", "City1");
        a2 = new Address("Address2","2222", "City2");
        a3 = new Address("Address3","3333", "City3");
        a4 = new Address("Address4","4444", "City4");
        p1 = new Person("fName1", "lName1", "111111111",a1);
        p2 = new Person("fName2", "lName2", "222222222",a2);
        p3 = new Person("fName3", "lName3", "333333333",a3);
        p4 = new Person("fName4", "lName4", "444444444",a4);

        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.persist(p1);
            em.persist(p2);
            em.persist(p3);
            em.persist(p4);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }

    @AfterEach
    public void afterEach() {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.createNamedQuery("Person.deleteAllRows").executeUpdate();
            em.getTransaction().commit();
        } finally {
            em.close();
        }
    }



        @Test
        public void logging() {
            given().log().all().when().get("/jokes/all").then().log().body();
        }

        @Test
        public void getAllPersonsSizeTest() {
            given()
                    .contentType("application/json")
                    .get("/person/all").then()
                    .assertThat()
                    .statusCode(HttpStatus.OK_200.getStatusCode())
                    .body("all", hasSize(4));
        }

        @Test
        public void getAllPersonsPersonTest() {
            given()
                    .contentType("application/json")
                    .get("/person/all").then()
                    .assertThat()
                    .statusCode(HttpStatus.OK_200.getStatusCode())
                    .body("all.fName", hasItem("fName3"));
        }

    @Test
    public void getPersonTest() {
        given()
                .contentType("application/json")
                .get("/person/" + p4.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("phone", equalTo(p4.getPhone()));
    }

    @Test
    public void getPersonAddressTest() {
        given()
                .contentType("application/json")
                .get("/person/" + p4.getId()).then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("street", equalTo(p4.getAddress().getStreet()));
    }

    @Test
    public void getPersonPersonNotFoundTest() {
            given()
                    .contentType("application/json")
                    .get("/person/0").then()
                    .assertThat()
                    .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                    .body("code", equalTo(404))
                    .body("message", equalTo("No person with provided id found"));;
    }


    @Test
    public void addPersonTest(){
        Address address = new Address("StreetAdd","ZipAdd", "CityAdd");
        PersonDTO personDTO = new PersonDTO(new Person("fNameAdd", "lNameAdd", "12344321Add",address));

        given()
                .contentType(ContentType.JSON)
                .body(GSON.toJson(personDTO))
                .post("/person/")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("phone", equalTo("12344321Add"));

    }

    @Test
    public void editPersonTest(){
        String newName = "EditlNameTest";
        p2.setLastName(newName);
        given()
                .contentType(ContentType.JSON)
                .body(GSON.toJson(new PersonDTO(p2)))
                .put("/person/")
                .then()
                .assertThat()
                .statusCode(HttpStatus.OK_200.getStatusCode())
                .body("lName", equalTo(newName));

    }

    @Test
    public void editPersonPersonNotFoundTest() {
        Address a5 = new Address("Address4","4444", "City4");
        Person p5 = new Person("fName1", "lName1", "111111111",a5);
        PersonAddressDTO personAddressDTO = new PersonAddressDTO(p5);
        personAddressDTO.setId(0);
        given().log().body()
                .contentType("application/json")
                .body(GSON.toJson(personAddressDTO))
                .put("/person").then()
                .assertThat()
                .statusCode(HttpStatus.NOT_FOUND_404.getStatusCode())
                .body("code", equalTo(404))
                .body("message", equalTo("No person with provided id found"));;
    }
}
