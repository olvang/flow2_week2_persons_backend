package facades;

import dto.PersonAddressDTO;
import dto.PersonDTO;
import dto.PersonsAddressesDTO;
import dto.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import org.junit.jupiter.api.*;
import utils.EMF_Creator;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PersonFacadeTest {
    private static EntityManagerFactory emf;
    private static PersonFacade facade;
    private static Person p1,p2,p3,p4;
    private static Address a1,a2,a3,a4;
    public PersonFacadeTest() {
    }

    @BeforeAll
    public static void setUpClass() {
        emf = EMF_Creator.createEntityManagerFactoryForTest();
        facade = PersonFacade.getFacade(emf);
    }

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
    public void getPersonCorrectIDTest() throws PersonNotFoundException {
        assertEquals(p2.getFirstName(), facade.getPerson(p2.getId()).getfName());
    }

    @Test
    public void getPersonWrongIDTest() {
        Assertions.assertThrows(PersonNotFoundException.class, () -> {
            facade.getPerson( 0);
        });
    }

    @Test
    public void getAllPersonsTest() {
        PersonsAddressesDTO personsDTO = facade.getAllPersons();
        assertEquals(4, personsDTO.getAll().size());
    }

    @Test
    public void addPersonTest() throws PersonNotFoundException, MissingInputException {
        String fName = "fName5";
        String lName = "lName5";
        String phone = "55555555";
        String street = "streetTest";
        String zip = "zipTest";
        String city = "cityTest";

        PersonAddressDTO PersonAddressDTO = facade.addPerson(fName,lName,phone,street,zip,city);
        assertEquals(phone, facade.getPerson( PersonAddressDTO.getId()).getPhone());
    }

    @Test
    public void addPersonWithNoName() {
        String fName = "";
        String lName = "lName5";
        String phone = "55555555";
        String street = "streetTest";
        String zip = "zipTest";
        String city = "cityTest";


        Assertions.assertThrows(MissingInputException.class, () -> {
            facade.addPerson(fName,lName,phone,street,zip,city);
        });
    }

    @Test
    public void editPersonTest() throws PersonNotFoundException, MissingInputException {
        String fName = "fName99";
        PersonAddressDTO personAddressDTO = facade.getPerson( p3.getId());
        personAddressDTO.setfName(fName);
        facade.editPerson(personAddressDTO);


        personAddressDTO = facade.getPerson( p3.getId());
        assertEquals(fName, personAddressDTO.getfName());
    }

    @Test
    public void editPersonWithMissingPhoneTest() throws PersonNotFoundException, MissingInputException {
        String phone = "";
        PersonAddressDTO personAddressDTO = facade.getPerson( p3.getId());
        personAddressDTO.setPhone(phone);

        Assertions.assertThrows(MissingInputException.class, () -> {
            facade.editPerson(personAddressDTO);
        });
    }


    @Test
    public void deletePersonTest() throws PersonNotFoundException {
        int id = p2.getId();
        facade.deletePerson(id);

        assertEquals(null, facade.getPerson( id));
    }

    @Test
    public void deletePersonWrongIDTest() throws PersonNotFoundException {
        Assertions.assertThrows(PersonNotFoundException.class, () -> {
            facade.deletePerson(0);
        });
    }
}
