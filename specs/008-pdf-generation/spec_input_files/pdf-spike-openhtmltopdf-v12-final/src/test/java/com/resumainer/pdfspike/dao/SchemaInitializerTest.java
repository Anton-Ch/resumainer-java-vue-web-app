package com.resumainer.pdfspike.dao;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class SchemaInitializerTest {
    @Test void splitSqlIgnoresLineCommentSemicolons() {
        List<String> statements = SchemaInitializer.splitSql("CREATE TABLE a(x TEXT); -- comment ; ignored\nCREATE TABLE b(y TEXT);");
        assertEquals(2, statements.size());
        assertTrue(statements.get(0).startsWith("CREATE TABLE a"));
        assertTrue(statements.get(1).startsWith("CREATE TABLE b"));
    }
}
