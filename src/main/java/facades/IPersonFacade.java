package facades;

import dto.PersonAddressDTO;
import dto.PersonDTO;
import dto.PersonsAddressesDTO;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;

public interface IPersonFacade {
    public PersonAddressDTO addPerson(String fName, String lName, String phone, String street, String zip, String city) throws MissingInputException;
    public PersonDTO deletePerson(int id) throws PersonNotFoundException;
    public PersonAddressDTO getPerson(int id) throws PersonNotFoundException;
    public PersonsAddressesDTO getAllPersons();
    public PersonAddressDTO editPerson(PersonAddressDTO p) throws PersonNotFoundException, MissingInputException;
}
