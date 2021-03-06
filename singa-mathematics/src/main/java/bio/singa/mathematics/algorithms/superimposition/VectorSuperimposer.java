package bio.singa.mathematics.algorithms.superimposition;

import bio.singa.core.utility.Pair;
import bio.singa.mathematics.algorithms.matrix.SVDecomposition;
import bio.singa.mathematics.algorithms.optimization.KuhnMunkres;
import bio.singa.mathematics.combinatorics.StreamPermutations;
import bio.singa.mathematics.matrices.*;
import bio.singa.mathematics.metrics.model.VectorMetricProvider;
import bio.singa.mathematics.vectors.Vector;
import bio.singa.mathematics.vectors.Vectors;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An implementation of the Kabsch algorithm that uses Singular Value Decomposition (SVD) to compute the ideal
 * superimposition of two point sets (a list of {@link Vector}s).
 *
 * @author fk
 * @see <a href="https://en.wikipedia.org/wiki/Kabsch_algorithm">Wikipedia: Kabsch algorithm</a>
 */
public class VectorSuperimposer extends AbstractSuperimposer<Vector> {

    private VectorSuperimposer(List<Vector> reference, List<Vector> candidate) {
        super(reference, candidate);
    }

    public static VectorSuperimposition<Vector> calculateVectorSuperimposition(List<Vector> reference, List<Vector> candidate) {
        return new VectorSuperimposer(reference, candidate).calculateSuperimposition();
    }

    public static VectorSuperimposition calculateIdealVectorSuperimposition(List<Vector> reference, List<Vector> candidate) {
        return new VectorSuperimposer(reference, candidate).calculateIdealSuperimposition();
    }

    /**
     * Finds the superimposition with the minimal distances between pairs of {@link Vector}s by using {@link
     * KuhnMunkres} optimization. This is only useful if candidate and reference vectors are already pre-aligned.
     *
     * @param reference The reference {@link Vector}s to which should be aligned.
     * @param candidate The candidate {@link Vector}s for the alignment should be carried out.
     * @return The {@link VectorSuperimposition} with the minimal distances between pairs.
     */
    public static VectorSuperimposition calculateKuhnMunkresSuperimposition(List<Vector> reference, List<Vector> candidate) {
        return new VectorSuperimposer(reference, candidate).calculateKuhnMunkresSuperimposition();
    }

    @Override
    protected void center() {
        referenceCentroid = Vectors.getCentroid(reference);
        shiftedReference = reference.stream()
                .map(vector -> vector.subtract(referenceCentroid))
                .collect(Collectors.toList());
        candidateCentroid = Vectors.getCentroid(candidate);
        shiftedCandidate = candidate.stream()
                .map(vector -> vector.subtract(candidateCentroid))
                .collect(Collectors.toList());
    }

    @Override
    protected VectorSuperimposition<Vector> calculateSuperimposition() {
        center();
        calculateRotation();
        calculateTranslation();
        applyMapping();
        calculateRMSD();
        return new VectorSuperimposition<>(rmsd, translation, rotation, reference, candidate, mappedCandidate);
    }

    @Override
    protected void applyMapping() {
        mappedCandidate = candidate.stream()
                .map(vector -> rotation.transpose().multiply(vector).add(translation))
                .collect(Collectors.toList());
    }

    private void calculateTranslation() {
        // calculate translation vector t = ca - R' * cb
        translation = referenceCentroid.subtract(rotation.transpose().multiply(candidateCentroid));
    }

    private void calculateRotation() {
        Matrix referenceMatrix = FastMatrices.assembleMatrixFromRows(shiftedReference);
        Matrix candidateMatrix = FastMatrices.assembleMatrixFromRows(shiftedCandidate);

        // calculate covariance
        Matrix covarianceMatrix = Matrices.calculateCovarianceMatrix(referenceMatrix, candidateMatrix);

        // solve using SVD
        SVDecomposition svd = new SVDecomposition(covarianceMatrix);
        Matrix u = svd.getMatrixU();
        Matrix v = svd.getMatrixV();
        Matrix ut = u.transpose();

        // calculate actual rotation matrix
        rotation = v.multiply(ut).transpose();

        // check for possible reflection
        if (rotation.as(SquareMatrix.class).determinant() < 0) {

            // get copy of V matrix
            Matrix matrixV = svd.getMatrixV().getCopy().transpose();
            matrixV.getElements()[2][0] = 0 - matrixV.getElement(2, 0);
            matrixV.getElements()[2][1] = 0 - matrixV.getElement(2, 1);
            matrixV.getElements()[2][2] = 0 - matrixV.getElement(2, 2);

            rotation = matrixV.transpose().multiply(ut).transpose();
        }
    }

    /**
     * Finds the ideal superimposition (LRMSD = min(RMSD)) for a list of candidate vectors by exhaustive calculation of
     * all permutations.
     *
     * @return The ideal {@link VectorSuperimposition}.
     */
    private VectorSuperimposition calculateIdealSuperimposition() {
        Optional<VectorSuperimposition<Vector>> optionalSuperimposition = StreamPermutations.of(
                candidate.toArray(new Vector[0]))
                .parallel()
                .map(s -> s.collect(Collectors.toList()))
                .map(permutedCandidates -> new VectorSuperimposer(reference, permutedCandidates)
                        .calculateSuperimposition())
                .reduce((VectorSuperimposition<Vector> s1, VectorSuperimposition<Vector> s2) ->
                        s1.getRmsd() < s2.getRmsd() ? s1 : s2);
        return optionalSuperimposition.orElse(null);
    }

    private VectorSuperimposition calculateKuhnMunkresSuperimposition() {
        // calculate pairwise squared distances between reference and candidate
        double[][] elements = new double[reference.size()][candidate.size()];
        for (int i = 0; i < reference.size(); i++) {
            for (int j = i; j < candidate.size(); j++) {
                double squaredDistance = VectorMetricProvider.SQUARED_EUCLIDEAN_METRIC.calculateDistance(reference.get(i), candidate.get(j));
                elements[i][j] = squaredDistance;
                elements[j][i] = squaredDistance;
            }
        }
        LabeledMatrix<Vector> labeledSquaredDistanceMatrix = new LabeledRegularMatrix<>(elements);
        labeledSquaredDistanceMatrix.setRowLabels(reference);
        labeledSquaredDistanceMatrix.setColumnLabels(candidate);
        // find optimal assignment with minimal pairwise distances using Kuhn-Munkres optimization
        KuhnMunkres<Vector> kuhnMunkres = new KuhnMunkres<>(labeledSquaredDistanceMatrix);
        List<Pair<Vector>> assignedPairs = kuhnMunkres.getAssignedPairs();
        List<Vector> updatedReference = assignedPairs.stream()
                .map(Pair::getFirst)
                .collect(Collectors.toList());
        List<Vector> updatedCandidate = assignedPairs.stream()
                .map(Pair::getSecond)
                .collect(Collectors.toList());
        return new VectorSuperimposer(updatedReference, updatedCandidate).calculateSuperimposition();
    }
}
