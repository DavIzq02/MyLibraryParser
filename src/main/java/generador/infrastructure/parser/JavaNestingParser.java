package generador.infrastructure.parser;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.body.TypeDeclaration;
import generador.core.domain.classifier.UmlClassifier;
import generador.core.domain.relationship.UmlNesting;
import generador.core.domain.relationship.UmlRelationship;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public final class JavaNestingParser {

    public List<UmlRelationship> parse(CompilationUnit compilationUnit, Map<String, UmlClassifier> classifiers) {
        List<UmlRelationship> relationships = new ArrayList<>();

        String packagePrefix = compilationUnit.getPackageDeclaration()
                .map(pd -> pd.getNameAsString().replace(".", "::") + "::")
                .orElse("");

        /*
         * Iteramos todos los TypeDeclaration del archivo (cualquier nivel)
         * y para cada uno buscamos sus miembros que sean también tipos.
         * De esa forma capturamos cualquier combinación:
         *   class → class, class → enum, class → interface,
         *   enum  → class, record → class, etc.
         */
        for (TypeDeclaration<?> outer : compilationUnit.findAll(TypeDeclaration.class)) {
            String outerKey = buildQualifiedKey(outer, packagePrefix);
            UmlClassifier outerClassifier = classifiers.get(outerKey);
            if (outerClassifier == null) continue;

            for (BodyDeclaration<?> member : outer.getMembers()) {
                if (!(member instanceof TypeDeclaration<?> inner)) continue;

                String innerKey = buildQualifiedKey(inner, packagePrefix);
                UmlClassifier innerClassifier = classifiers.get(innerKey);
                if (innerClassifier == null) continue;

                relationships.add(new UmlNesting(outerClassifier, innerClassifier, Optional.empty(), Optional.empty()));
            }
        }

        return List.copyOf(relationships);
    }

    /**
     * Construye la clave de búsqueda en el mapa de clasificadores
     * recorriendo la cadena de ancestros TypeDeclaration para
     * producir la forma: pkg::Outer::Inner.
     */
    private String buildQualifiedKey(TypeDeclaration<?> declaration, String packagePrefix) {
        List<String> chain = new ArrayList<>();
        chain.add(declaration.getNameAsString());

        // Subir por los ancestros TypeDeclaration (clases externas, etc.)
        TypeDeclaration<?> current = declaration.findAncestor(TypeDeclaration.class).orElse(null);
        while (current != null) {
            chain.add(0, current.getNameAsString());
            current = current.findAncestor(TypeDeclaration.class).orElse(null);
        }

        return packagePrefix + String.join("::", chain);
    }
}