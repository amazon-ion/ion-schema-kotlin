package software.amazon.ionschema

import org.junit.Assert.*
import org.junit.Test
import software.amazon.ion.IonValue
import software.amazon.ion.system.IonSystemBuilder
import software.amazon.ionschema.util.CloseableIterator
import java.io.StringReader

class TypeTest {
    private val ION = IonSystemBuilder.standard().build()
    private val iss = IonSchemaSystemBuilder.standard().addAuthority(
        object : Authority {
            override fun iteratorFor(iss: IonSchemaSystem, id: String) = object : CloseableIterator<IonValue> {
                private val iter = ION.iterate(StringReader(id))
                override fun hasNext() = iter.hasNext()
                override fun next() = iter.next()
                override fun close() { }
            }
        }
    ).build()

    private val type = iss.loadSchema("type::{ name: a, type: string }").getType("a")!!

    @Test
    fun name() = assertEquals("a", type.name())

    @Test
    fun isValid_true() = assertTrue(type.isValid(ION.singleValue("\"hello\"")))

    @Test
    fun isValid_false() = assertFalse(type.isValid(ION.singleValue("1")))

    @Test(expected = NoSuchElementException::class)
    fun validate_success() {
        val violations = type.validate(ION.singleValue("\"hello\""))
        assertNotNull(violations)
        assertTrue(violations.isValid())
        assertFalse(violations.iterator().hasNext())
        violations.iterator().next()
    }

    @Test
    fun validate_violations() {
        val violations = type.validate(ION.singleValue("1"))
        assertNotNull(violations)
        assertFalse(violations.isValid())
        assertTrue(violations.iterator().hasNext())

        val iter = violations.iterator()
        val violation = iter.next()
        assertEquals("type_mismatch", violation.code)
        assertEquals("type", violation.constraint?.fieldName)
        assertEquals(ION.singleValue("string"), violation.constraint)
    }
}

