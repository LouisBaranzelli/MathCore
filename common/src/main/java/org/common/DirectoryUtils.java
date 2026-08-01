package org.common;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

/**
 * Classe utilitaire pour la gestion et la suppression de répertoires.
 */
public final class DirectoryUtils {

    // Constructeur privé pour empêcher l'instanciation de la classe utilitaire
    private DirectoryUtils() {
        throw new UnsupportedOperationException("Classe utilitaire - instanciation non autorisée");
    }

    /**
     * Supprime récursivement un répertoire et tout son contenu (fichiers et sous-dossiers).
     *
     * @param dirPath Le chemin (Path) du répertoire à supprimer.
     * @throws IOException Si une erreur d'accès ou de suppression survient.
     */
    public static void deleteDirectory(Path dirPath) throws IOException {
        if (dirPath == null || Files.notExists(dirPath)) {
            return;
        }

        if (!Files.isDirectory(dirPath)) {
            throw new IllegalArgumentException("Le chemin spécifié n'est pas un répertoire : " + dirPath);
        }

        try (Stream<Path> tree = Files.walk(dirPath)) {
            tree.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException e) {
                            throw new RuntimeException("Impossible de supprimer : " + path, e);
                        }
                    });
        } catch (RuntimeException e) {
            // Unwrapping de l'exception causée par le forEach
            if (e.getCause() instanceof IOException) {
                throw (IOException) e.getCause();
            }
            throw e;
        }
    }

    /**
     * Surcharge acceptant un objet File au lieu de Path.
     *
     * @param directory Le répertoire File à supprimer.
     * @throws IOException Si une erreur d'accès ou de suppression survient.
     */
    public static void deleteDirectory(File directory) throws IOException {
        if (directory != null) {
            deleteDirectory(directory.toPath());
        }
    }

    /**
     * Vide le contenu d'un répertoire sans supprimer le répertoire lui-même.
     *
     * @param dirPath Le chemin du répertoire à vider.
     * @throws IOException Si une erreur survient.
     */
    public static void cleanDirectory(Path dirPath) throws IOException {
        if (dirPath == null || Files.notExists(dirPath)) {
            return;
        }

        try (Stream<Path> stream = Files.list(dirPath)) {
            for (Path child : stream.toList()) {
                if (Files.isDirectory(child)) {
                    deleteDirectory(child);
                } else {
                    Files.delete(child);
                }
            }
        }
    }
}