package com.soufka.userstoragespi;

import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.models.utils.UserModelDelegate;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.adapter.AbstractUserAdapter;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;
import org.keycloak.storage.user.UserLookupProvider;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

public class DbStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator {

    KeycloakSession keycloakSession;
    ComponentModel componentModel;
    protected Map<String, UserModel> loadedUsers = new HashMap<>();
    Connection connection;
    User user;

    public DbStorageProvider(KeycloakSession session, ComponentModel model, Connection connection) {
        this.keycloakSession = session;
        this.componentModel = model;
        this.connection = connection;
    }

    @Override
    public void close() {

    }


    @Override
    public UserModel getUserById(String id, RealmModel realm) {
        StorageId storageId = new StorageId(id);
        String username = storageId.getExternalId();
        return getUserByUsername(username, realm);
    }

    @Override
    public UserModel getUserByUsername(String username, RealmModel realm) {
        UserModel userAdapter = loadedUsers.get(username);
        if (userAdapter == null) {
            User user = getUserFromDb(username);
            if (user != null) {
                userAdapter = createAdapter(realm, username, user);
                loadedUsers.put(username, userAdapter);
            }
        }
        return userAdapter;
    }

    @Override
    public UserModel getUserByEmail(String email, RealmModel realm) {
        return null;
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        return credentialType.equals(PasswordCredentialModel.TYPE);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {

        try {
            return credentialType.equals(PasswordCredentialModel.TYPE) ;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
        if (!supportsCredentialType(credentialInput.getType())) return false;

        try {
            String password = this.user != null ? this.user.getPassword() : null;

            if(password == null) {
                User dbUser = getUserFromDb(user.getUsername());
                password = dbUser != null ? dbUser.getPassword() : null;
            }

            if (password == null) return false;

            return password.equals(getHashedString(credentialInput.getChallengeResponse()));
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private UserModel createAdapter(RealmModel realm, String username, User userObj) {

        UserModel local = keycloakSession.userLocalStorage().getUserByUsername(realm, username);
       if (local == null) {
           local = keycloakSession.userLocalStorage().addUser(realm, username);
           local.setFederationLink(componentModel.getId());
       }

        UserModelDelegate delegete = new UserModelDelegate(local);

        delegete.setSingleAttribute("account_uuid", userObj.getAccountUUID());
        delegete.setSingleAttribute("employee_uuid", userObj.getEmployeeUUID());
        delegete.setSingleAttribute("login", userObj.getLogin());
        delegete.setSingleAttribute("active", userObj.getActive());
        delegete.setSingleAttribute("role", userObj.getRole());

        delegete.setEnabled(true);
        return delegete;
    }

    private  User getUserFromDb(String username){
        try {
            String sql = "select * from accounts where  login = ? and active = '1'";
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, username);

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                User user = new User(resultSet.getString("password"),
                        resultSet.getString("account_uuid"), resultSet.getString("employee_uuid"), resultSet.getString("login")
                        , resultSet.getString("role"), resultSet.getString("active"));
                this.user = user;
                return user;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String getHashedString(String plainText) throws Exception {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.reset();
            messageDigest.update(plainText.getBytes());

            byte[] digestedMessage = messageDigest.digest();
            BigInteger bigInteger = new BigInteger(1, digestedMessage);


            String digestedString = bigInteger.toString(16);
            return digestedString;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
