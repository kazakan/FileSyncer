import java.nio.ByteBuffer
import message.FSVarLenStringListField
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class FSVarLenStringListFieldTest {
    @Test
    fun testEmptyFieldMarshallUnmarshall() {
        // NONE MESSAGE
        val field = FSVarLenStringListField(listOf())
        val reconstructedField = FSVarLenStringListField()
        val byteBuffer = ByteBuffer.allocate(100)
        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.getByteNums(), reconstructedField.getByteNums())
        assertEquals(field.strs, reconstructedField.strs)
    }

    @Test
    fun testNonEmptyFieldMarchallUnmarshall() {
        // With MESSAGE
        val field = FSVarLenStringListField(listOf("a", "b", "c", "d", "e", "f"))
        val reconstructedField = FSVarLenStringListField()
        val byteBuffer = ByteBuffer.allocate(100)

        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.getByteNums(), reconstructedField.getByteNums())
        assertEquals(field.strs, reconstructedField.strs)
    }

    @Test
    fun testNonEnglishFieldMarchallUnmarshall() {
        // With MESSAGE
        val field = FSVarLenStringListField(listOf("가", "나", "다c", "d라", "e마", "f"))
        val reconstructedField = FSVarLenStringListField()
        val byteBuffer = ByteBuffer.allocate(100)

        field.marshall(byteBuffer)
        byteBuffer.clear()
        reconstructedField.unmarshall(byteBuffer)

        assertEquals(field.getByteNums(), reconstructedField.getByteNums())
        assertEquals(field.strs, reconstructedField.strs)
    }
}
