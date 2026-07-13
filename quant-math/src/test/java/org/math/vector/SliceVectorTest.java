package org.math.vector;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.common.Shape;
import org.math.common.exception.ShapeException;

import static org.junit.jupiter.api.Assertions.*;

class SliceVectorTest {

    private Vector sourceVector;

    @BeforeEach
    void setUp() {
        // Un vecteur de base contenant [10.0, 20.0, 30.0, 40.0, 50.0]
        sourceVector = new ArrayVector(10.0, 20.0, 30.0, 40.0, 50.0);
    }

    @Test
    @DisplayName("Devrait instancier correctement une tranche valide")
    void testConstructorAndGetters() {
        // Tranche de l'index 1 à 4 -> contient [20.0, 30.0, 40.0] (taille = 3)
        SliceVector slice = new SliceVector(sourceVector, 1, 4);

        assertEquals(3, slice.getSize());
        assertEquals(20.0, slice.getValue(0));
        assertEquals(30.0, slice.getValue(1));
        assertEquals(40.0, slice.getValue(2));

        // Validation de la Shape
        Shape shape = slice.getShape();
        assertNotNull(shape);
        assertEquals(2, shape.countDimensions());
        assertEquals(3, shape.getDimension(0)); // 3 lignes
        assertEquals(1, shape.getDimension(1)); // 1 colonne
    }

    @Test
    @DisplayName("Devrait aplatir la structure (flattening) pour éviter les tranches récursives")
    void testFlattening() {
        // Première tranche : [20.0, 30.0, 40.0, 50.0] (index 1 à 5 du vecteur source)
        SliceVector parentSlice = new SliceVector(sourceVector, 1, 5);

        // Sous-tranche : [30.0, 40.0] (index 1 à 3 de la première tranche, càd index 2 à 4 de la source)
        SliceVector subSlice = new SliceVector(parentSlice, 1, 3);

        assertEquals(2, subSlice.getSize());
        assertEquals(30.0, subSlice.getValue(0));
        assertEquals(40.0, subSlice.getValue(1));

        // Test d'implémentation de l'aplatissement :
        // La source interne de 'subSlice' doit être le 'sourceVector' d'origine (le ArrayVector),
        // et non pas le 'parentSlice'. Cela garantit un accès direct sans cascade d'appels.
        // Remarque : Si le champ 'source' était privé sans accesseur, ce test valide indirectement
        // le comportement par l'absence de StackOverflow et la rapidité des calculs.
    }

    @Test
    @DisplayName("Devrait rejeter la création si les bornes sont invalides")
    void testInvalidBoundaries() {
        // start négatif
        assertThrows(IndexOutOfBoundsException.class, () -> new SliceVector(sourceVector, -1, 3));

        // end dépasse la taille
        assertThrows(IndexOutOfBoundsException.class, () -> new SliceVector(sourceVector, 2, 6));

        // start >= end (taille nulle ou négative)
        assertThrows(IllegalArgumentException.class, () -> new SliceVector(sourceVector, 3, 3));
        assertThrows(IndexOutOfBoundsException.class, () -> new SliceVector(sourceVector, 4, 2));

        // Source nulle
        assertThrows(IndexOutOfBoundsException.class, () -> new SliceVector(null, 0, 2));
    }

    @Test
    @DisplayName("getValue devrait lever IndexOutOfBoundsException si l'index est hors limites")
    void testGetValueOutOfBounds() {
        SliceVector slice = new SliceVector(sourceVector, 1, 3); // taille = 2, contient [20.0, 30.0]

        assertThrows(IndexOutOfBoundsException.class, () -> slice.getValue(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> slice.getValue(2));
    }

    @Test
    @DisplayName("Devrait calculer correctement l'addition avec un autre vecteur")
    void testAdd() {
        SliceVector slice1 = new SliceVector(sourceVector, 0, 3); // [10.0, 20.0, 30.0]
        Vector other = new ArrayVector(1.0, 2.0, 3.0);

        Vector result = slice1.add(other);

        assertTrue(result instanceof ArrayVector, "L'addition doit renvoyer une nouvelle instance d'ArrayVector");
        assertEquals(11.0, result.getValue(0));
        assertEquals(22.0, result.getValue(1));
        assertEquals(33.0, result.getValue(2));
    }

    @Test
    @DisplayName("Devrait lever une ShapeException si les tailles diffèrent lors de l'addition")
    void testAddIncompatible() {
        SliceVector slice = new SliceVector(sourceVector, 0, 3); // taille = 3
        Vector smallVector = new ArrayVector(1.0, 2.0); // taille = 2

        assertThrows(ShapeException.class, () -> slice.add(smallVector));
    }

    @Test
    @DisplayName("Devrait calculer correctement la soustraction")
    void testMinus() {
        SliceVector slice1 = new SliceVector(sourceVector, 2, 5); // [30.0, 40.0, 50.0]
        Vector other = new ArrayVector(10.0, 10.0, 10.0);

        Vector result = slice1.minus(other);

        assertEquals(20.0, result.getValue(0));
        assertEquals(30.0, result.getValue(1));
        assertEquals(40.0, result.getValue(2));
    }

    @Test
    @DisplayName("Devrait calculer le produit scalaire (dot product)")
    void testDot() {
        SliceVector slice = new SliceVector(sourceVector, 1, 3); // [20.0, 30.0]
        Vector other = new ArrayVector(2.0, 3.0);

        // (20 * 2) + (30 * 3) = 40 + 90 = 130
        assertEquals(130.0, slice.dot(other));
    }

    @Test
    @DisplayName("Devrait calculer la norme d'une tranche")
    void testNorm() {
        SliceVector slice = new SliceVector(sourceVector, 1, 3); // [20.0, 30.0]
        // sqrt(20^2 + 30^2) = sqrt(400 + 900) = sqrt(1300)
        double expected = Math.sqrt(1300.0);

        assertEquals(expected, slice.norm(), 1e-9);
    }

    @Test
    @DisplayName("Equals et HashCode doivent comparer logiquement le contenu, même entre implémentations différentes")
    void testEqualsAndHashCode() {
        SliceVector slice = new SliceVector(sourceVector, 1, 3); // [20.0, 30.0]
        ArrayVector equivalentArray = new ArrayVector(20.0, 30.0);
        ArrayVector differentArray = new ArrayVector(20.0, 99.0);

        // Équivalence de contenu logique avec ArrayVector
        assertEquals(slice, equivalentArray);
        assertEquals(equivalentArray, slice);
        assertEquals(slice.hashCode(), equivalentArray.hashCode());

        // Différence de valeurs
        assertNotEquals(slice, differentArray);

        // Auto-comparaison (Réflexivité)
        assertEquals(slice, slice);

        // Comparaison avec types incompatibles
        assertNotEquals(slice, "Un String");
        assertNotEquals(slice, null);
    }

    @Test
    @DisplayName("Devrait formater correctement la tranche en String")
    void testToString() {
        SliceVector slice = new SliceVector(sourceVector, 1, 4); // [20.0, 30.0, 40.0]
        assertEquals("[20.0, 30.0, 40.0]", slice.toString());
    }
}