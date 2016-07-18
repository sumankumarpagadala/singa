package de.bioforscher.mathematics.matrices;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

public class SymmetricMatrixTest {

    private SymmetricMatrix trueSymmetricMatrix;
    private Matrix testSymmetricMatrix;

    @Before
    public void initialize() {
        double[][] values = {{1.0, 2.0, 3.0}, {2.0, 4.0, 5.0}, {3.0, 5.0, 8.0}};
        this.trueSymmetricMatrix = new SymmetricMatrix(values);
        this.testSymmetricMatrix = new RegularMatrix(values);
    }

    @Test
    public void shouldInstantiateWithoutCompactValues() {
        double[][] values = {{1.0, 2.0, 3.0}, {2.0, 4.0, 5.0}, {3.0, 5.0, 8.0}};
        Matrix expected = new SymmetricMatrix(values);
        assertTrue(Arrays.deepEquals(new double[][]{{1.0}, {2.0, 4.0}, {3.0, 5.0, 8.0}}, expected.getElements()));
    }

    @Test
    public void shouldInstantiateWithCompactValues() {
        double[][] correctlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}};
        Matrix expected = new SymmetricMatrix(correctlyJaggedArray);
        assertTrue(Arrays.deepEquals(new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}}, expected.getElements()));

    }

    @Test
    public void shouldConvertToSymmetricMatrix() {
        SymmetricMatrix actual = this.testSymmetricMatrix.as(SymmetricMatrix.class);
        assertTrue(Arrays.deepEquals(actual.getElements(), this.trueSymmetricMatrix.getElements()));
    }

    @Test
    public void shouldGetRightElements() {
        assertEquals(3.0, this.trueSymmetricMatrix.getElement(0, 2), 0.0);
        assertEquals(3.0, this.trueSymmetricMatrix.getElement(2, 0), 0.0);
        assertEquals(4.0, this.trueSymmetricMatrix.getElement(1, 1), 0.0);
    }

    @Test
    public void shouldCheckForCompactArrays() {
        double[][] correctlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0}, {3.0, 6.0, 9.0}};
        assertTrue(SymmetricMatrix.isCompact(correctlyJaggedArray));
        double[][] inCorrectlyJaggedArray = new double[][]{{1.0}, {2.0, 5.0, 6.0}, {3.0, 6.0, 9.0}};
        assertFalse(SymmetricMatrix.isCompact(inCorrectlyJaggedArray));
    }

    @Test
    public void shouldAddSymmetricMatrices() {
        Matrix actual = this.testSymmetricMatrix.add(this.testSymmetricMatrix);
        Matrix expected = this.trueSymmetricMatrix.add(this.testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    public void shouldSubtractSymmetricMatrices() {
        Matrix actual = this.testSymmetricMatrix.subtract(this.testSymmetricMatrix);
        Matrix expected = this.trueSymmetricMatrix.subtract(this.testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    public void shouldMultiplySymmetricMatrices() {
        Matrix actual = this.testSymmetricMatrix.multiply(this.testSymmetricMatrix);
        Matrix expected = this.trueSymmetricMatrix.multiply(this.testSymmetricMatrix);
        assertTrue(Arrays.deepEquals(actual.getElements(), expected.getElements()));
    }

    @Test
    public void shouldRetrieveValueForLabel() {
        LabeledSymmetricMatrix<String> lsm = new LabeledSymmetricMatrix<>(trueSymmetricMatrix.getElements());
        lsm.setRowLabel("L1", 0);
        lsm.setRowLabel("L2", 1);
        lsm.setRowLabel("L3", 2);
        assertEquals(3.0, lsm.getValueForLabel("L1", "L3"), 0.0);
    }

}