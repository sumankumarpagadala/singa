package bio.singa.mathematics.matrices;

import bio.singa.mathematics.vectors.RegularVector;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SymmetricMatrixTest {

    private static SymmetricMatrix trueSymmetricMatrix;
    private static Matrix testSymmetricMatrix;

    @BeforeAll
    static void initialize() {
        double[][] values = {{1.0, 2.0, 3.0}, {2.0, 4.0, 5.0}, {3.0, 5.0, 8.0}};
        trueSymmetricMatrix = new SymmetricMatrix(values);
        testSymmetricMatrix = new RegularMatrix(values);
    }

    @Test
    void shouldInstantiateWithoutCompactValues() {
        double[][] values = {{1.0, 2.0, 3.0}, {2.0, 4.0, 5.0}, {3.0, 5.0, 8.0}};
        Matrix expected = new SymmetricMatrix(values);
        assertTrue(Arrays.deepEquals(new double[][]{{1.0}, {2.0, 4.0}, {3.0, 5.0, 8.0}}, expected.getElements()));
    }

    @Test
    void shouldInstantiateWithCompactValues() {
        double[][] correctlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}};
        Matrix expected = new SymmetricMatrix(correctlyJaggedArray);
        assertTrue(Arrays.deepEquals(new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}}, expected.getElements()));

    }

    @Test
    void shouldConvertToSymmetricMatrix() {
        SymmetricMatrix actual = testSymmetricMatrix.as(SymmetricMatrix.class);
        assertTrue(Arrays.deepEquals(actual.getElements(), trueSymmetricMatrix.getElements()));
    }

    @Test
    void shouldGetRightElements() {
        assertEquals(3.0, trueSymmetricMatrix.getElement(0, 2));
        assertEquals(3.0, trueSymmetricMatrix.getElement(2, 0));
        assertEquals(4.0, trueSymmetricMatrix.getElement(1, 1));
    }

    @Test
    void shouldCheckForCompactArrays() {
        double[][] correctlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}};
        assertTrue(SymmetricMatrix.isCompact(correctlyJaggedArray));
        double[][] inCorrectlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0, 6.0}, {3.0, 6.0, 9.0}};
        assertFalse(SymmetricMatrix.isCompact(inCorrectlyJaggedArray));
    }

    @Test
    void shouldAddSymmetricMatrices() {
        Matrix actual = testSymmetricMatrix.add(testSymmetricMatrix);
        Matrix expected = trueSymmetricMatrix.add(testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    void shouldSubtractSymmetricMatrices() {
        Matrix actual = testSymmetricMatrix.subtract(testSymmetricMatrix);
        Matrix expected = trueSymmetricMatrix.subtract(testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    void shouldMultiplySymmetricMatrices() {
        Matrix actual = testSymmetricMatrix.multiply(testSymmetricMatrix);
        Matrix expected = trueSymmetricMatrix.multiply(testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    void shouldRetrieveValueForLabel() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        lsm.setRowLabel("L1", 0);
        lsm.setRowLabel("L2", 1);
        lsm.setRowLabel("L3", 2);
        assertEquals(3.0, lsm.getValueForLabel("L1", "L3"));
    }

    @Test
    void shouldGetStringRepresentation() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        lsm.setRowLabel("L1", 0);
        lsm.setRowLabel("L2", 1);
        lsm.setRowLabel("L3", 2);
        assertEquals(",L1,L2,L3\n" +
                "L1,1.000000,2.000000,3.000000\n" +
                "L2,2.000000,4.000000,5.000000\n" +
                "L3,3.000000,5.000000,8.000000", lsm.getStringRepresentation());
    }

    @Test
    void shouldGetStringRepresentationWithoutLabels() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        assertEquals("1.000000,2.000000,3.000000\n" +
                "2.000000,4.000000,5.000000\n" +
                "3.000000,5.000000,8.000000", lsm.getStringRepresentation());
    }

    @Test
    void shouldRetrieveLabelsOfSymmetricMatrix() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        lsm.setRowLabel("L1", 0);
        lsm.setRowLabel("L2", 1);
        lsm.setRowLabel("L3", 2);
        List<String> labelsToCheck = new ArrayList<>();
        labelsToCheck.add("L1");
        labelsToCheck.add("L2");
        labelsToCheck.add("L3");
        assertEquals(lsm.getRowLabels(), labelsToCheck);
        assertEquals(lsm.getColumnLabels(), labelsToCheck);
    }

    @Test
    void shouldCopy() {
        SymmetricMatrix copy1 = trueSymmetricMatrix.getCopy();
        SquareMatrix copy2 = trueSymmetricMatrix.getCopy();
        copy1.getElements()[0][0] = Double.MIN_VALUE;
        assertTrue(SymmetricMatrix.isCompact(copy2.getElements()));
        assertTrue(copy2.getElements()[0][0] != Double.MIN_VALUE);
    }

    @Test
    void shouldGetColumnAndRowByLabel() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        lsm.setRowLabel("L1", 0);
        lsm.setRowLabel("L2", 1);
        lsm.setRowLabel("L3", 2);
        RegularVector column = lsm.getColumnByLabel("L1");
        assertEquals(column, new RegularVector(1.0, 2.0, 3.0));
        RegularVector row = lsm.getRowByLabel("L1");
        assertEquals(row, new RegularVector(1.0, 2.0, 3.0));
    }

    @Test
    void shouldGetRow() {
        assertEquals(trueSymmetricMatrix.getRow(0), new RegularVector(1.0, 2.0, 3.0));
        assertEquals(trueSymmetricMatrix.getRow(1), new RegularVector(2.0, 4.0, 5.0));
        assertEquals(trueSymmetricMatrix.getRow(2), new RegularVector(3.0, 5.0, 8.0));
    }
}
