package facades;

import dto.PersonAddressDTO;
import dto.PersonDTO;
import dto.PersonsAddressesDTO;
import dto.PersonsDTO;
import entities.Address;
import entities.Person;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Query;
import java.util.Date;
import java.util.List;

/**
 * Rename Class to a relevant name Add add relevant facade methods
 */
public class PersonFacade implements IPersonFacade {

    private static PersonFacade instance;
    private static EntityManagerFactory emf;

    //Private Constructor to ensure Singleton
    private PersonFacade() {
    }


    /**
     * @param _emf
     * @return an instance of this facade class.
     */
    public static PersonFacade getFacade(EntityManagerFactory _emf) {
        if (instance == null) {
            emf = _emf;
            instance = new PersonFacade();
        }
        return instance;
    }

    private EntityManager getEntityManager() {
        return emf.createEntityManager();
    }


    public PersonAddressDTO getPerson(int id) throws PersonNotFoundException {
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, id);
        if (person == null) {
            throw new PersonNotFoundException("No person with provided id found");
        }
        return new PersonAddressDTO(person);
    }

    public PersonsAddressesDTO getAllPersons() {
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Select p FROM Person p");
        List<Person> persons = query.getResultList();
        PersonsAddressesDTO PersonsDTO = new PersonsAddressesDTO(persons);

        return PersonsDTO;
    }


    public PersonAddressDTO addPerson(String fName, String lName, String phone, String street, String zip, String city) throws MissingInputException {
        if (fName == null || fName.isEmpty() || lName == null || lName.isEmpty() || phone == null || phone.isEmpty()) {
            throw new MissingInputException("First Name, Last Name and/or Phone is missing");
        }
        if (street == null || street.isEmpty() || zip == null || zip.isEmpty() || city == null || city.isEmpty()) {
            throw new MissingInputException("Street,zip and/or city is missing");
        }
        EntityManager em = getEntityManager();
        Query query = em.createQuery("Select a FROM Address a WHERE a.street = :street and a.zip = :zip and a.city = :city");
        query.setParameter("street", street);
        query.setParameter("zip", zip);
        query.setParameter("city", city);

        List<Address> addresses = (List<Address>) query.getResultList();
        Address address;

        if (addresses.size() > 0) {
            //Address exist
            address = addresses.get(0);
        } else {
            //Address does not exist
            address = new Address(street, zip, city);
        }
        Person person = new Person(fName, lName, phone, address);
        try {
            em.getTransaction().begin();
            em.persist(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new PersonAddressDTO(person);
    }

    public PersonAddressDTO editPerson(PersonAddressDTO p) throws PersonNotFoundException, MissingInputException {
        if (p.getId() == null || p.getId() < 1 || p.getfName() == null || p.getfName().isEmpty() || p.getlName() == null || p.getlName().isEmpty() || p.getPhone() == null || p.getPhone().isEmpty()) {
            throw new MissingInputException("ID, First Name, Last Name and/or Phone is missing");
        }
        if (p.getStreet() == null || p.getStreet().isEmpty() || p.getZip() == null || p.getZip().isEmpty() || p.getCity() == null || p.getCity().isEmpty()) {
            throw new MissingInputException("Street,zip and/or city is missing");
        }

        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, p.getId());
        if (person == null) {
            throw new PersonNotFoundException("Could not edit, no person with provided id found");
        }

        Address address = person.getAddress();
        Integer addressToDeleteID = address.getId();
        Boolean deleteAddress = false;

        //Check if address is not the same
        if (!p.getStreet().equals(person.getAddress().getStreet()) || !p.getZip().equals(person.getAddress().getZip()) || !p.getCity().equals(person.getAddress().getCity())) {
            Query query1 = em.createQuery("Select a FROM Address a WHERE a.street = :street and a.zip = :zip and a.city = :city");
            query1.setParameter("street", p.getStreet());
            query1.setParameter("zip", p.getZip());
            query1.setParameter("city", p.getCity());

            List<Address> addresses = (List<Address>) query1.getResultList();

            if (addresses.size() > 0) {
                //Address exist
                address = addresses.get(0);
            } else {
                //Address does not exist
                address = new Address(p.getStreet(), p.getZip(), p.getCity());
                em.getTransaction().begin();
                em.persist(address);
                em.getTransaction().commit();

            }

            //Check if old address is used by anyone else
            Query query2 = em.createQuery("Select p FROM Person p JOIN p.address a WHERE a.id = :id", Person.class);
            query2.setParameter("id", person.getAddress().getId());

            List<Person> persons = (List<Person>) query2.getResultList();
            if (persons.size() == 1) {
                //Address only used current current person, delete
                deleteAddress = true;
            }
        }

        try {
            em.getTransaction().begin();
            person.setFirstName(p.getfName());
            person.setLastName(p.getlName());
            person.setPhone(p.getPhone());
            person.setAddress(address);
            person.setLastEdited(new Date());

            if (deleteAddress == true) {
                em.createQuery("delete from Address a where a.id = :id")
                        .setParameter("id", addressToDeleteID)
                        .executeUpdate();
            }
            em.getTransaction().commit();


        } finally {
            em.close();
        }
        return new PersonAddressDTO(person);

    }

    public PersonDTO deletePerson(int id) throws PersonNotFoundException {
        EntityManager em = getEntityManager();
        Person person = em.find(Person.class, id);
        if (person == null) {
            if (person == null) {
                throw new PersonNotFoundException("Could not delete, provided id does not exist");
            }
        }
        try {
            em.getTransaction().begin();
            em.remove(person);
            em.getTransaction().commit();
        } finally {
            em.close();
        }
        return new PersonDTO(person);
    }


}
