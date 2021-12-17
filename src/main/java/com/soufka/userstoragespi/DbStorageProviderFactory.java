package com.soufka.userstoragespi;

import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;

public class DbStorageProviderFactory implements UserStorageProviderFactory<DbStorageProvider> {

    public static final String PROVIDER_NAME = "OpenERP_MySQL_Federation";
    protected static final List<ProviderConfigProperty> configMetadata;

    static {
        configMetadata = ProviderConfigurationBuilder.create()
                .property().name("conUrl")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Connection Url")
                .defaultValue("jdbc:mysql://localhost:3306/authentication_db")
                .helpText("Url to Mysql DB")
                .add()


                .property().name("username")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Connection Username")
                .defaultValue("root")
                .helpText("Username to Mysql DB")
                .add()

                .property().name("password")
                .type(ProviderConfigProperty.STRING_TYPE)
                .label("Connection Password")
                .defaultValue("dbasf0ruMy!@")
                .helpText("Password to Mysql DB")
                .add().build();
    }

    @Override
    public DbStorageProvider create(KeycloakSession session, ComponentModel model) {
        try {
            return new DbStorageProvider(session, model, getConnection(model));
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config) throws ComponentValidationException {
        try {
            Connection connection = getConnection(config);
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("select 1");
        } catch (ComponentValidationException e){
            throw e;
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException("Exception while connection validation");
        }
    }

    private Connection getConnection( ComponentModel config) throws Exception{
        validate(config);

        String conString = config.getConfig().getFirst("conUrl");
        String username = config.getConfig().getFirst("username");
        String pwd = config.getConfig().getFirst("password");

        Class.forName("com.mysql.cj.jdbc.Driver");

        Connection con = DriverManager.getConnection(conString,username,pwd);
        return con;

    }

    private void validate(ComponentModel config){

        String conString = config.getConfig().getFirst("conUrl");
        String username = config.getConfig().getFirst("username");
        String pwd = config.getConfig().getFirst("password");

        if(conString == null) {
            throw new ComponentValidationException("Connection String is empty");
        }else if(username == null){
            throw new ComponentValidationException("username is empty");
        }else if(pwd == null){
            throw new ComponentValidationException("password is empty");
        }

    }
}
