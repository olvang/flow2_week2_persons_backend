package rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.PersonAddressDTO;
import dto.PersonDTO;
import dto.PersonsAddressesDTO;
import dto.PersonsDTO;
import entities.Address;
import exceptions.MissingInputException;
import exceptions.PersonNotFoundException;
import utils.EMF_Creator;
import facades.PersonFacade;
import javax.persistence.EntityManagerFactory;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

//Todo Remove or change relevant parts before ACTUAL use
@Path("person")
public class PersonResource {

    private static final EntityManagerFactory EMF = EMF_Creator.createEntityManagerFactory();

    //An alternative way to get the EntityManagerFactory, whithout having to type the details all over the code
    //EMF = EMF_Creator.createEntityManagerFactory(DbSelector.DEV, Strategy.CREATE);

    private static final PersonFacade FACADE = PersonFacade.getFacade(EMF);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();


    @Path("/all")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getAllPersons() {
        PersonsAddressesDTO personAddressDTO = FACADE.getAllPersons();
        if (personAddressDTO.getAll().isEmpty()) return new Gson().toJson("No Persons Found");

        return new Gson().toJson(personAddressDTO);
    }

    @Path("/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public String getPersonByID(@PathParam("id") int id) throws PersonNotFoundException {
        PersonAddressDTO person = FACADE.getPerson(id);
        return new Gson().toJson(person);
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addPerson(String personJson) throws MissingInputException {
        PersonAddressDTO person = GSON.fromJson(personJson, PersonAddressDTO.class);
        person = FACADE.addPerson(person.getfName(), person.getlName(), person.getPhone(), person.getStreet(),person.getZip(), person.getCity());
        return Response.ok(GSON.toJson(person)).build();
    }

    @PUT
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response editPerson(String personJson) throws PersonNotFoundException, MissingInputException {
        PersonAddressDTO person;
            person = GSON.fromJson(personJson, PersonAddressDTO.class);

        person = FACADE.editPerson(person);
        return Response.ok(GSON.toJson(person)).build();
    }

    @Path("/{id}")
    @DELETE
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response deletePerson(@PathParam("id") int id) throws PersonNotFoundException {
        PersonDTO personDTO = FACADE.deletePerson(id);
        return Response.ok(GSON.toJson(personDTO)).build();
    }

}
