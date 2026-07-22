package org.math.vector;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.math.common.Shape;
import org.math.common.exception.ShapeException;

import static org.junit.jupiter.api.Assertions.*;

class ArrayVectorTest {

    @Test
    @DisplayName("Devrait instancier correctement un vecteur et copier ses données")
    void testConstructorAndGetters() {
        double[] input = {1.0, 2.0, 3.0};
        ArrayVector vector = new ArrayVector(input);

        assertEquals(3, vector.size());
        assertEquals(1.0, vector.getValue(0));
        assertEquals(2.0, vector.getValue(1));
        assertEquals(3.0, vector.getValue(2));

        // Validation de la Shape avec ton implémentation réelle
        Shape shape = vector.getShape();
        assertNotNull(shape);
        assertEquals(2, shape.countDimensions(), "Un vecteur doit avoir 2 dimensions (lignes x colonnes)");
        assertEquals(3, shape.getDimension(0), "La première dimension (lignes) doit être de 3");
        assertEquals(1, shape.getDimension(1), "La deuxième dimension (colonnes) doit être de 1");
    }

    @Test
    @DisplayName("Devrait rejeter la construction avec un tableau null ou vide")
    void testInvalidConstruction() {
        assertThrows(IllegalArgumentException.class, () -> new ArrayVector((double[]) null));
        assertThrows(IllegalArgumentException.class, () -> new ArrayVector(new double[0]));
    }

    @Test
    @DisplayName("Devrait être immuable (vérification de la copie défensive)")
    void testImmutability() {
        double[] input = {1.0, 2.0, 3.0};
        ArrayVector vector = new ArrayVector(input);

        // On modifie le tableau d'origine
        input[0] = 99.0;

        // Le vecteur ne doit pas avoir été altéré
        assertEquals(1.0, vector.getValue(0), "La modification du tableau d'origine a fuité dans le vecteur !");
    }

    @Test
    @DisplayName("Devrait lever une exception lors d'un accès hors limites")
    void testGetValueOutOfBounds() {
        ArrayVector vector = new ArrayVector(1.0, 2.0);
        assertThrows(IndexOutOfBoundsException.class, () -> vector.getValue(-1));
        assertThrows(IndexOutOfBoundsException.class, () -> vector.getValue(2));
    }

    @Test
    @DisplayName("Devrait additionner correctement deux vecteurs de même taille")
    void testAddNominal() {
        Vector v1 = new ArrayVector(1.0, 2.0, 3.0);
        Vector v2 = new ArrayVector(4.0, 5.0, 6.0);

        Vector result = v1.add(v2);

        assertEquals(3, result.size());
        assertEquals(5.0, result.getValue(0));
        assertEquals(7.0, result.getValue(1));
        assertEquals(9.0, result.getValue(2));
    }

    @Test
    @DisplayName("Devrait rejeter l'addition si les tailles sont différentes")
    void testAddIncompatible() {
        Vector v1 = new ArrayVector(1.0, 2.0);
        Vector v3 = new ArrayVector(1.0, 2.0, 3.0);

        assertThrows(ShapeException.class, () -> v1.add(v3));
    }

    @Test
    @DisplayName("Devrait soustraire correctement deux vecteurs de même taille")
    void testMinusNominal() {
        Vector v1 = new ArrayVector(5.0, 7.0, 9.0);
        Vector v2 = new ArrayVector(1.0, 2.0, 3.0);

        Vector result = v1.minus(v2);

        assertEquals(4.0, result.getValue(0));
        assertEquals(5.0, result.getValue(1));
        assertEquals(6.0, result.getValue(2));
    }

    @Test
    @DisplayName("Devrait rejeter la soustraction si les tailles sont différentes")
    void testMinusIncompatible() {
        Vector v1 = new ArrayVector(1.0, 2.0);
        Vector v3 = new ArrayVector(1.0, 2.0, 3.0);

        assertThrows(ShapeException.class, () -> v1.minus(v3));
    }

    @Test
    @DisplayName("Devrait calculer le produit scalaire (dot product)")
    void testDotProduct() {
        Vector v1 = new ArrayVector(1.0, 3.0, -5.0);
        Vector v2 = new ArrayVector(4.0, -2.0, -1.0);

        // (1 * 4) + (3 * -2) + (-5 * -1) = 4 - 6 + 5 = 3
        assertEquals(3.0, v1.dot(v2));
    }

    @Test
    @DisplayName("Devrait rejeter le produit scalaire si les tailles diffèrent")
    void testDotIncompatible() {
        Vector v1 = new ArrayVector(1.0, 2.0);
        Vector v3 = new ArrayVector(1.0, 2.0, 3.0);

        assertThrows(ShapeException.class, () -> v1.dot(v3));
    }

    @Test
    @DisplayName("Devrait calculer la norme Euclidienne d'un vecteur")
    void testNorm() {
        Vector v = new ArrayVector(3.0, 4.0); // sqrt(3^2 + 4^2) = 5
        assertEquals(5.0, v.norm(), 1e-9);
    }

    @Test
    @DisplayName("Vérification de la logique equals, hashCode et toString")
    void testEqualsAndHashCode() {
        ArrayVector v1 = new ArrayVector(1.0, 2.0);
        ArrayVector v2 = new ArrayVector(1.0, 2.0);
        ArrayVector v3 = new ArrayVector(1.0, 3.0);
        ArrayVector v4 = new ArrayVector(1.0, 2.0, 3.0);

        // Réflexivité
        assertEquals(v1, v1);

        // Symétrie et Équivalence de contenu
        assertEquals(v1, v2);
        assertEquals(v1.hashCode(), v2.hashCode());

        // Différences de valeurs
        assertNotEquals(v1, v3);
        assertNotEquals(v1.hashCode(), v3.hashCode());

        // Différences de tailles
        assertNotEquals(v1, v4);

        // Comparaison avec null ou un autre type
        assertNotEquals(null, v1);
        assertNotEquals("un string", v1);
    }

    @Test
    @DisplayName("Equals devrait fonctionner avec une autre implémentation de Vector (fallback path)")
    void testEqualsWithCustomVectorImplementation() {
        ArrayVector arrayVector = new ArrayVector(1.0, 2.0);

        // On simule une implémentation mock/alternative de Vector
        Vector customVector = new Vector() {
            @Override public int size() { return 2; }
            @Override public double getValue(int index) { return index == 0 ? 1.0 : 2.0; }
            @Override public Shape getShape() { return new Shape(2, 1); }
            @Override public Vector add(Vector other) { return null; }
            @Override public Vector minus(Vector other) { return null; }
            @Override public double dot(Vector other) { return 0; }
            @Override public double norm() { return 0; }
        };

        assertEquals(arrayVector, customVector, "Le equals de ArrayVector doit savoir comparer ses valeurs avec une autre implémentation de Vector");
    }

    @Test
    @DisplayName("Devrait formater correctement le vecteur en chaîne de caractères")
    void testToString() {
        ArrayVector vector = new ArrayVector(1.5, 2.0, -3.14);
        assertEquals("[1.5, 2.0, -3.14]", vector.toString());
    }
}