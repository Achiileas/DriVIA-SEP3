package dk.via.sep3.drivia.tier3.service;

import dk.via.sep3.drivia.tier3.dto.UserDTO;

public interface UserService{

    UserDTO registerCustomer( String name, String email, String password, String phoneNumber );

    UserDTO login( String email, String password );

    UserDTO getUser( int id );
}
