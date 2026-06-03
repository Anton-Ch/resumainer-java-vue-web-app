package com.resumainer.dao;

import com.resumainer.model.ContactDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ContactDetailDaoTest {

    private DataSource dataSource;
    private Connection connection;
    private PreparedStatement preparedStatement;
    private ContactDetailDao contactDetailDao;

    @BeforeEach
    void setUp() throws Exception {
        dataSource = mock(DataSource.class);
        connection = mock(Connection.class);
        preparedStatement = mock(PreparedStatement.class);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        contactDetailDao = new ContactDetailDao(dataSource);
    }

    @Test
    void create_insertsContactDetail() throws Exception {
        when(preparedStatement.executeUpdate()).thenReturn(1);

        UUID userId = UUID.randomUUID();
        ContactDetail cd = ContactDetail.createEmpty(userId);
        contactDetailDao.create(cd);

        verify(connection).prepareStatement(
            "INSERT INTO contact_detail (user_id) VALUES (?)");
        verify(preparedStatement).setObject(1, userId);
        verify(preparedStatement).executeUpdate();
    }

    @Test
    void create_nullInput_throwsException() {
        assertThrows(IllegalArgumentException.class, () -> contactDetailDao.create(null));
    }
}
