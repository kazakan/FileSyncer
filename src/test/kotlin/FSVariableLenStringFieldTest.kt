import java.nio.ByteBuffer
import message.FSVariableLenStringField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FSVariableLenStringFieldTest {
    @Test
    fun testEmptyFieldMarshallUnmarshall() {
        // NONE MESSAGE
        val field = FSVariableLenStringField("")
        val reconstructedField = FSVariableLenStringField()
        val byteBuffer = ByteBuffer.allocate(100)
        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.size, reconstructedField.size)
        assertEquals(field.str, reconstructedField.str)
    }

    @Test
    fun testNonEmptyFieldMarchallUnmarshall() {
        // With MESSAGE
        val field = FSVariableLenStringField("hello, nice to meet you. And you?")
        val reconstructedField = FSVariableLenStringField()
        val byteBuffer = ByteBuffer.allocate(100)

        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.size, reconstructedField.size)
        assertEquals(field.str, reconstructedField.str)
    }

    @Test
    fun testNonEnglishFieldMarchallUnmarshall() {
        // With MESSAGE
        val field = FSVariableLenStringField("안녕하세요? 만나서 반가워요")
        val reconstructedField = FSVariableLenStringField()
        val byteBuffer = ByteBuffer.allocate(100)

        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.size, reconstructedField.size)
        assertEquals(field.str, reconstructedField.str)
    }
}
