package dto;

import entities.Person;

import java.util.ArrayList;
import java.util.List;

public class PersonsAddressesDTO {

    List<PersonAddressDTO> all = new ArrayList();

    public PersonsAddressesDTO(List<Person> personEntities) {
        personEntities.forEach((p) -> {
            all.add(new PersonAddressDTO(p));
        });
    }

    public List<PersonAddressDTO> getAll() {
        return all;
    }
}
